package com.etledge.database.db.connector.ftp.impl;

import com.etledge.database.db.connector.ftp.AbstractFTPConnector;
import com.etledge.database.db.connector.ftp.FTPManager;
import com.etledge.database.db.connector.relationdb.entity.ConResponse;

public class FTPConnector extends AbstractFTPConnector {

    @Override
    public ConResponse connect() {
        ConResponse testResponse = new ConResponse();
        FTPManager ftpManager = new FTPManager(this.getHost(),
                this.getPort(),
                this.getUsername(),
                this.getPassword(),
                this.getControlEncoding(),
                this.getMode());
        try {
            ftpManager.getFTPClient();
            testResponse.setResult(true);
            testResponse.setMsg("连接成功！");
        } catch (Exception e) {
            testResponse.setResult(false);
            testResponse.setMsg(e.getMessage());
        } finally {
            // 关闭连接
            ftpManager.close();
        }
        return testResponse;
    }
}
