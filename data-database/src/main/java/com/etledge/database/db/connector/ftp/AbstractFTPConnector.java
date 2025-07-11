package com.etledge.database.db.connector.ftp;

import com.etledge.database.db.connector.relationdb.entity.ConResponse;
import lombok.Data;

@Data
public abstract class AbstractFTPConnector {

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口
     */
    private int port;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 编码
     */
    private String controlEncoding;

    /**
     * 模式
     */
    private String mode;

    /**
     * 连接测试
     *
     * @return
     */
    public abstract ConResponse connect();

}
