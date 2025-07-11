package com.etledge.database.db.connector.relationdb.impl;

import com.etledge.common.Constants;
import com.etledge.database.config.exception.ETLException;
import com.etledge.database.db.connector.relationdb.AbstractDatabaseConnector;
import com.etledge.database.db.connector.util.JdbcUtil;

import java.sql.Connection;

/**
 * MySQL连接器
 */
public class MySQLConnector extends AbstractDatabaseConnector {

    private static final String URL_FORMAT = "jdbc:mysql://%s:%s/%s?useCursorFetch=true&useSSL=false&useUnicode=yes&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&serverTimezone=Asia/Shanghai";

    /**
     * 表信息
     */
    private static final String TABLE_INFO_SQL = "SELECT `TABLE_NAME`,TABLE_COMMENT,TABLE_TYPE FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? ";

    /**
     * 列信息
     */
    private static final String COLUMN_INFO_SQL =
            "SELECT TABLE_NAME,COLUMN_NAME,COLUMN_COMMENT,COLUMN_TYPE,DATA_TYPE,CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION,NUMERIC_SCALE,IS_NULLABLE,COLUMN_KEY,ORDINAL_POSITION" +
                    " FROM INFORMATION_SCHEMA.COLUMNS  " +
                    "WHERE TABLE_SCHEMA = ? ";

    /**
     * 获取含有主键约束的表信息
     */
    private static final String PRIMARY_CONSTRAINT_TABLE_SQL = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE  CONSTRAINT_TYPE = 'PRIMARY KEY' AND TABLE_SCHEMA = ?";


    /**
     * 获取所有外键
     */
    private static final String FK_CONSTRAINT_TABLE_SQL = "SELECT b.CONSTRAINT_NAME, b.TABLE_NAME, b.REFERENCED_TABLE_NAME, GROUP_CONCAT(b.COLUMN_NAME ORDER BY b.ORDINAL_POSITION) as COLUMN_NAME, GROUP_CONCAT(b.REFERENCED_COLUMN_NAME order by b.ORDINAL_POSITION) as REFERENCED_COLUMN_NAME " +
            "FROM information_schema.TABLE_CONSTRAINTS a, INFORMATION_SCHEMA.KEY_COLUMN_USAGE b " +
            "WHERE a.CONSTRAINT_NAME = b.CONSTRAINT_NAME AND a.CONSTRAINT_TYPE = 'FOREIGN KEY' AND a.CONSTRAINT_SCHEMA = ? " +
            " group by b.CONSTRAINT_NAME, b.TABLE_NAME, b.REFERENCED_TABLE_NAME";

    /**
     * 获取所有索引
     */
    private static final String IDX_CONSTRAINT_TABLE_SQL = "SELECT TABLE_NAME, INDEX_NAME, NON_UNIQUE, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) as COLUMN_NAME " +
            "FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = ? AND INDEX_NAME != 'PRIMARY' " +
            "GROUP BY TABLE_NAME,INDEX_NAME,NON_UNIQUE";


    /**
     * 构造连接器
     *
     * @return
     */
    @Override
    public AbstractDatabaseConnector build() {
        this.setDriver(JdbcUtil.getDriver(Constants.DATABASE_TYPE.MYSQL));
        this.setUrl(JdbcUtil.getUrl(Constants.DATABASE_TYPE.MYSQL, getHost(), String.valueOf(getPort()), getDbName(), getExtConfig()));
        return this;
    }

    /**
     * 获取数据源连接
     *
     * @return
     */
    @Override
    public Connection getConnection() {
        Connection conn;
        try {
            conn = JdbcUtil.getConnection(getDriver(), getUrl(), getDatabaseVo());
        } catch (Exception e) {
            logger.error(null, e);
            throw new ETLException("创建数据库连接失败！" + e.getMessage());
        }

        return conn;
    }
}
