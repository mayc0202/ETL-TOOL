package com.etledge.database.db.connector.ftp;


import com.etledge.common.Constants;
import com.etledge.database.config.exception.ETLException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FTP管理器
 */
@Data
public class FTPManager {

    private static final Logger logger = LoggerFactory.getLogger(FTPManager.class);
    private static final int RETRY_TIMES_READ_DATA = 6;
    private static final int RETRY_TIMES_INIT = 3;
    private static final int DEFAULT_WAIT_MS = 2000;
    private static final int KEEP_ALIVE_TIMEOUT_SEC = 60;

    private FTPClient ftpClient;
    private String controlEncoding;
    private String mode;
    private String host;
    private int port;
    private String user;
    private String password;
    private int bufferSize = 4096;
    private int connectTimeout = 60000;
    private int connectSoTimeout = 60000;
    private int dataSoTimeout = 120000;

    public FTPManager() {
    }

    public FTPManager(String host, int port, String user, String password, String controlEncoding, String mode) {
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
                return;
            } catch (Exception e) {
                logger.warn("FTP登录失败，尝试第{}/{}次。原因: {}", attempt, RETRY_TIMES_INIT, e.getMessage());
                if (attempt >= RETRY_TIMES_INIT) {
                    throw new ETLException("FTP登录失败，已达最大重试次数");
                }
                sleep(DEFAULT_WAIT_MS);
                resetConnection();
            }
        }
    }

    public FTPClient getFTPClient() {
        if ((this.ftpClient != null) && (this.ftpClient.isConnected())) {
            return this.ftpClient;
        }

        this.ftpClient = new FTPClient();
        try {
            configureFtpClient();
            establishConnection();
            authenticate();
            configureTransferMode();
        } catch (IOException e) {
            throw new ETLException("FTP连接/登录失败: " + e.getMessage());
        }

        return this.ftpClient;
    }

    private void configureFtpClient() {
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
            throw new ETLException("FTP登录认证失败");
        }
        logServerReply();
        logger.info("FTP服务器登录成功");
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

    private void validateConnectionResponse() throws IOException {
        int replyCode = this.ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            this.ftpClient.disconnect();
            throw new ETLException("FTP服务器拒绝连接。响应码: " + replyCode);
        }
    }

    private void resetConnection() {
        try {
            if (ftpClient != null && this.ftpClient.isConnected()) {
                this.ftpClient.disconnect();
            }
        } catch (IOException e) {
            logger.warn("断开FTP连接时发生错误: {}", e.getMessage());
        }
    }

    private void logServerReply() {
        String[] replies = this.ftpClient.getReplyStrings();
        if (replies != null) {
            Arrays.stream(replies).forEach(reply -> logger.debug("FTP SERVER: {}", reply));
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 上传文件
     *
     * @param remoteDir
     * @param localFilePath
     * @param createSubDir
     * @return
     */
    public boolean uploadFile(String remoteDir, String localFilePath, boolean createSubDir) {
        validatePathParams(remoteDir, localFilePath);
        Path localPath = Paths.get(localFilePath);

        if (!Files.isRegularFile(localPath)) {
            throw new ETLException("无效的本地文件路径: " + localFilePath);
        }

        ensureRemoteDirectoryExists(remoteDir, createSubDir);

        try (InputStream is = Files.newInputStream(localPath)) {
            return this.ftpClient.storeFile(localPath.getFileName().toString(), is);
        } catch (IOException e) {
            throw new ETLException("文件上传失败: " + localFilePath);
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

    /**
     * 创建目录
     *
     * @param path
     */
    public void createRemoteDirectory(String path) {
        try {
            String[] parts = path.split("/");
            StringBuilder currentPath = new StringBuilder();

            for (String dir : parts) {
                if (dir.isEmpty()) continue;

                currentPath.append("/").append(dir);
                String current = currentPath.toString();

                if (!this.ftpClient.changeWorkingDirectory(current)) {
                    if (this.ftpClient.makeDirectory(current)) {
                        logger.info("创建远程目录: {}", current);
                    } else {
                        throw new ETLException("目录创建失败: " + current);
                    }
                }
            }
        } catch (IOException e) {
            throw new ETLException("创建目录失败: " + path);
        }
    }

    /**
     * 下载文件
     *
     * @param remotePath
     * @param fileName
     * @param localDir
     * @return
     */
    public boolean downloadFile(String remotePath, String fileName, String localDir) {
        Path localPath = Paths.get(localDir, fileName);
        ensureLocalDirectoryExists(localPath.getParent());

        boolean success = false;
        int attempt = 0;

        while (!success && attempt < RETRY_TIMES_READ_DATA) {
            try {
                attempt++;
                success = attemptDownload(remotePath, fileName, localPath);
            } catch (Exception e) {
                logger.error("下载失败({}/{}): {}", attempt, RETRY_TIMES_READ_DATA, e.getMessage());
                if (attempt >= RETRY_TIMES_READ_DATA) {
                    throw new ETLException("文件下载失败，已达最大重试次数");
                }
                resetConnection();
                init();
                sleep(DEFAULT_WAIT_MS);
            }
        }
        return success;
    }

    private boolean attemptDownload(String remotePath, String fileName, Path localPath) throws IOException {
        if (!this.ftpClient.changeWorkingDirectory(remotePath)) {
            throw new ETLException("远程目录不存在: " + remotePath);
        }

        long localSize = getExistingFileSize(localPath);
        this.ftpClient.setRestartOffset(localSize);

        try (OutputStream os = new BufferedOutputStream(
                new FileOutputStream(localPath.toFile(), localSize > 0))) {
            return this.ftpClient.retrieveFile(fileName, os);
        }
    }

    private long getExistingFileSize(Path path) {
        return path.toFile().exists() ? path.toFile().length() : 0;
    }

    private void ensureLocalDirectoryExists(Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new ETLException("本地目录创建失败: " + dir);
            }
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (this.ftpClient == null) return;

        try {
            if (this.ftpClient.isConnected()) {
                this.ftpClient.logout();
                this.ftpClient.disconnect();
                logger.debug("FTP连接已关闭");
            }
        } catch (IOException e) {
            logger.warn("关闭FTP连接时发生错误: {}", e.getMessage());
        } finally {
            this.ftpClient = null;
        }
    }

    /**
     * 获取到目录下文件列表
     * @param remoteDir
     * @return
     */
    public List<String> listFiles(String remoteDir) {
        try {
            if (!this.ftpClient.changeWorkingDirectory(remoteDir)) {
                throw new ETLException("目录不存在: " + remoteDir);
            }

            return Arrays.stream(this.ftpClient.listFiles())
                    .filter(FTPFile::isFile)
                    .map(FTPFile::getName)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ETLException("获取文件列表失败: " + remoteDir);
        }
    }
}
