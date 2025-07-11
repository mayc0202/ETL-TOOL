package com.etledge.database.db.connector.ftp;


import com.etledge.common.Constants;
import com.etledge.database.config.exception.ETLException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FTPS管理器
 */
@Data
public class FTPSManager {

    private static final Logger logger = LoggerFactory.getLogger(FTPSManager.class);
    private static final int RETRY_TIMES_READ_DATA = 6;
    private static final int RETRY_TIMES_INIT = 3;
    private static final int DEFAULT_WAIT_MS = 2000;
    private static final int KEEP_ALIVE_TIMEOUT_SEC = 60;
    private static final String[] SECURE_PROTOCOLS = {"TLSv1.2", "TLSv1.3"};

    private FTPSClient ftpClient;
    private String host;
    private String controlEncoding;
    private String mode;
    private int port;
    private String user;
    private String password;
    private int bufferSize = 4096;
    private int connectTimeout = 60000;
    private int connectSoTimeout = 60000;
    private int dataSoTimeout = 120000;
    private boolean isPassiveMode = true;
    private boolean trustAllCertificates = false; // 生产环境应设为false

    public FTPSManager() {}

    public FTPSManager(String host, int port, String user, String password,
                       String controlEncoding, String mode) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.controlEncoding = controlEncoding;
        this.mode = mode;
    }

    public void init() {
        int attempt = 0;
        while (attempt < RETRY_TIMES_INIT) {
            try {
                attempt++;
                getFTPClient();
                logger.info("FTPS连接成功建立");
                return;
            } catch (Exception e) {
                logger.warn("FTPS登录失败，尝试第{}/{}次。原因: {}",
                        attempt, RETRY_TIMES_INIT, e.getMessage());

                if (attempt >= RETRY_TIMES_INIT) {
                    throw new ETLException("FTPS登录失败，已达最大重试次数");
                }
                safeSleep(DEFAULT_WAIT_MS);
                resetConnection();
            }
        }
    }

    public FTPSClient getFTPClient() {
        if ((this.ftpClient != null) && (this.ftpClient.isConnected())) {
            return this.ftpClient;
        }

        this.ftpClient = new FTPSClient();
        
        try {
            createSecureClient();
            configureClientSettings();
            establishConnection();
            authenticate();
            configureTransferMode();
            configureDataProtection();
        } catch (Exception e) {
            throw new ETLException("FTPS连接失败: " + e.getMessage());
        }
        
        return ftpClient;
    }

    private void createSecureClient() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = trustAllCertificates
                ? new TrustManager[]{new TrustAllCertificatesManager()}
                : null;

        sslContext.init(null, trustManagers, null);
        ftpClient = new FTPSClient(true, sslContext);

        // 设置安全协议版本
        this.ftpClient.setEnabledProtocols(SECURE_PROTOCOLS);
    }

    private void configureClientSettings() {
        this.ftpClient.setControlEncoding(controlEncoding);

        FTPClientConfig conf = new FTPClientConfig();
        conf.setServerLanguageCode("zh");
        this.ftpClient.configure(conf);

        this.ftpClient.setConnectTimeout(connectTimeout);
        this.ftpClient.setDefaultTimeout(connectSoTimeout);
        this.ftpClient.setDataTimeout(dataSoTimeout);
        this.ftpClient.setBufferSize(bufferSize);
        this.ftpClient.setControlKeepAliveTimeout(KEEP_ALIVE_TIMEOUT_SEC);
        this.ftpClient.setCopyStreamListener(new DGPCopyStreamListener());
    }

    private void establishConnection() throws IOException {
        this.ftpClient.connect(host, port);
        logServerReply();
        validateConnectionResponse();
    }

    private void authenticate() throws IOException {
        if (!this.ftpClient.login(user, password)) {
            throw new ETLException("FTPS登录认证失败");
        }
        logServerReply();
    }

    private void configureTransferMode() throws IOException {
        if (Constants.MODE_TYPE.PASSIVE.equals(mode)) {
            this.ftpClient.enterLocalPassiveMode();
        } else if (Constants.MODE_TYPE.ACTIVE.equals(mode)) {
            this.ftpClient.enterLocalActiveMode();
        }
        this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        this.ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
    }

    private void configureDataProtection() throws IOException {
        // 设置保护缓冲区大小
        this.ftpClient.execPBSZ(0);
        // 设置数据通道保护为私有
        this.ftpClient.execPROT("P");
    }

    private void validateConnectionResponse() throws IOException {
        int replyCode = this.ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            this.ftpClient.disconnect();
            throw new ETLException("FTPS服务器拒绝连接。响应码: " + replyCode);
        }
    }

    private void resetConnection() {
        try {
            if (ftpClient != null && this.ftpClient.isConnected()) {
                this.ftpClient.disconnect();
            }
        } catch (IOException e) {
            logger.warn("断开FTPS连接时发生错误: {}", e.getMessage());
        }
    }

    private void logServerReply() {
        String[] replies = this.ftpClient.getReplyStrings();
        if (replies != null) {
            Arrays.stream(replies).forEach(reply -> logger.debug("FTPS SERVER: {}", reply));
        }
    }

    private void safeSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean uploadFile(String remoteDir, String localFilePath, boolean createSubDir) {
        validatePathParams(remoteDir, localFilePath);
        Path localPath = Paths.get(localFilePath);

        if (!Files.isRegularFile(localPath)) {
            throw new ETLException("无效的本地文件: " + localFilePath);
        }

        ensureRemoteDirectoryExists(remoteDir, createSubDir);

        try (InputStream is = Files.newInputStream(localPath)) {
            boolean success = this.ftpClient.storeFile(localPath.getFileName().toString(), is);
            if (!success) {
                throw new ETLException("文件上传失败: " + localFilePath);
            }
            return true;
        } catch (IOException e) {
            throw new ETLException("文件上传异常: " + localFilePath);
        }
    }

    private void validatePathParams(String remoteDir, String localPath) {
        if (StringUtils.isBlank(remoteDir)) {
            throw new ETLException("远程目录不能为空");
        }
        if (StringUtils.isBlank(localPath)) {
            throw new ETLException("本地路径不能为空");
        }
    }

    private void ensureRemoteDirectoryExists(String remoteDir, boolean createIfMissing) {
        try {
            if (!this.ftpClient.changeWorkingDirectory(remoteDir)) {
                if (createIfMissing) {
                    createRemoteDirectory(remoteDir);
                } else {
                    throw new ETLException("远程目录不存在: " + remoteDir);
                }
            }
        } catch (IOException e) {
            throw new ETLException("访问远程目录失败: " + remoteDir);
        }
    }

    public void createRemoteDirectory(String path) {
        try {
            String[] parts = path.split("/");
            StringBuilder currentPath = new StringBuilder("/");

            for (String dir : parts) {
                if (dir.isEmpty()) continue;

                currentPath.append(dir).append("/");
                String fullPath = currentPath.toString();

                if (!this.ftpClient.changeWorkingDirectory(fullPath)) {
                    if (this.ftpClient.makeDirectory(fullPath)) {
                        logger.info("创建FTPS目录: {}", fullPath);
                    } else {
                        throw new ETLException("目录创建失败: " + fullPath);
                    }
                }
            }
        } catch (IOException e) {
            throw new ETLException("创建目录失败: " + path);
        }
    }

    public boolean downloadFile(String remotePath, String fileName, String localDir) {
        Path localPath = Paths.get(localDir, fileName);
        ensureLocalDirectoryExists(localPath.getParent());

        boolean success = false;
        int attempt = 0;

        while (!success && attempt < RETRY_TIMES_READ_DATA) {
            try {
                attempt++;
                success = attemptSecureDownload(remotePath, fileName, localPath);
            } catch (Exception e) {
                logger.error("安全下载失败({}/{}): {}",
                        attempt, RETRY_TIMES_READ_DATA, e.getMessage());

                if (attempt >= RETRY_TIMES_READ_DATA) {
                    throw new ETLException("文件下载失败，已达最大重试次数");
                }
                resetConnection();
                init();
                safeSleep(DEFAULT_WAIT_MS);
            }
        }
        return success;
    }

    private boolean attemptSecureDownload(String remotePath, String fileName, Path localPath)
            throws IOException {

        if (!this.ftpClient.changeWorkingDirectory(remotePath)) {
            throw new ETLException("远程目录不存在: " + remotePath);
        }

        long localSize = getExistingFileSize(localPath);
        this.ftpClient.setRestartOffset(localSize);

        try (OutputStream os = new BufferedOutputStream(
                new FileOutputStream(localPath.toFile(), localSize > 0))) {

            if (!this.ftpClient.retrieveFile(fileName, os)) {
                throw new ETLException("文件传输失败: " + fileName);
            }
            return true;
        }
    }

    private long getExistingFileSize(Path path) {
        return path.toFile().exists() ? path.toFile().length() : 0;
    }

    private void ensureLocalDirectoryExists(Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
                logger.debug("创建本地目录: {}", dir);
            } catch (IOException e) {
                throw new ETLException("本地目录创建失败: " + dir);
            }
        }
    }

    public void close() {
        if (ftpClient == null) return;

        try {
            if (this.ftpClient.isConnected()) {
                this.ftpClient.logout();
                this.ftpClient.disconnect();
                logger.info("FTPS连接安全关闭");
            }
        } catch (IOException e) {
            logger.warn("关闭FTPS连接时发生错误: {}", e.getMessage());
        } finally {
            ftpClient = null;
        }
    }

    public List<String> listFiles(String remoteDir) {
        try {
            if (!this.ftpClient.changeWorkingDirectory(remoteDir)) {
                throw new ETLException("目录不存在: " + remoteDir);
            }

            FTPFile[] files = this.ftpClient.listFiles();
            if (files == null) {
                return new ArrayList<>();
            }

            return Arrays.stream(files)
                    .filter(FTPFile::isFile)
                    .map(FTPFile::getName)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ETLException("获取文件列表失败: " + remoteDir);
        }
    }

    public boolean isConnectionActive() {
        return ftpClient != null && this.ftpClient.isConnected();
    }

    public void setTrustAllCertificates(boolean trustAll) {
        this.trustAllCertificates = trustAll;
    }

    /**
     * 信任所有证书管理器（仅用于测试环境）
     */
    static class TrustAllCertificatesManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
