package com.etledge.database.db.connector.ftp;

import com.etledge.common.Constants;
import com.etledge.database.config.exception.ETLException;
import com.etledge.database.db.connector.ftp.impl.FTPConnector;
import com.etledge.database.db.connector.ftp.impl.FTPSConnector;

/**
 * FTP连接工厂类
 */
public class FTPConnectorFactory {

    public static AbstractFTPConnector getConnector(String type) {
        switch (type) {
            case Constants.FTP_TYPE.FTP:
                return new FTPConnector();
            case Constants.FTP_TYPE.FTPS:
                return new FTPSConnector();
            default:
                throw new ETLException(String.format("暂不支持此文件服务类型![type:%s]",type));
        }
    }
}
