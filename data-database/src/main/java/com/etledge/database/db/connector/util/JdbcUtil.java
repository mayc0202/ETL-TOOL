package com.etledge.database.db.connector.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.etledge.common.Constants;
import com.etledge.database.config.exception.ETLException;
import com.etledge.database.db.vo.DatabaseVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * JDBC工具类
 */
public class JdbcUtil {

    private static final Logger logger = LoggerFactory.getLogger(JdbcUtil.class);

    // URL格式常量
    private static final String MYSQL_URL_FORMAT = "jdbc:mysql://%s:%s/%s";
    private static final String ORACLE_SERVICE_URL_FORMAT = "jdbc:oracle:thin:@//%s:%s/%s";
    private static final String ORACLE_SID_URL_FORMAT = "jdbc:oracle:thin:@%s:%s:%s";
    private static final String POSTGRESQL_URL_FORMAT = "jdbc:postgresql://%s:%s/%s";
    private static final String SQLSERVER_URL_FORMAT = "jdbc:sqlserver://%s:%s;DatabaseName=%s";
    private static final String DM_URL_FORMAT = "jdbc:dm://%s:%s/%s";

    // 驱动类名常量
    private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
    private static final String SQLSERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String DM_DRIVER = "dm.jdbc.driver.DmDriver";

    // 连接超时时间（秒）
    private static final int LOGIN_TIMEOUT = 10;


    /**
     * 获取URL
     *
     * @param type
     * @param host
     * @param port
     * @param dbName
     * @param extConfig
     * @return
     */
    public static String getUrl(String type, String host, String port, String dbName, String extConfig) {
        switch (type) {
            case Constants.DATABASE_TYPE.MYSQL:
            case Constants.DATABASE_TYPE.DORIS:
            case Constants.DATABASE_TYPE.STAR_ROCKS:
                return String.format(MYSQL_URL_FORMAT, host, port, dbName);

            case Constants.DATABASE_TYPE.ORACLE:
                JSONObject jsonObject = JSON.parseObject(extConfig);
                String connectType = jsonObject.getString(Constants.DATABASE_EXT_CONFIG.ORACLE_CONNECT_TYPE);
                return Constants.ORACLE_CONNECT_TYPE.SID.equals(connectType)
                        ? String.format(ORACLE_SID_URL_FORMAT, host, port, dbName)
                        : String.format(ORACLE_SERVICE_URL_FORMAT, host, port, dbName);

            case Constants.DATABASE_TYPE.POSTGRESQL:
                return String.format(POSTGRESQL_URL_FORMAT, host, port, dbName);

            case Constants.DATABASE_TYPE.SQL_SERVER:
                return String.format(SQLSERVER_URL_FORMAT, host, port, dbName);

            case Constants.DATABASE_TYPE.DM8:
                return String.format(DM_URL_FORMAT, host, port, dbName);

            default:
                throw new IllegalArgumentException("Unsupported database type: " + type);
        }
    }

    /**
     * 获取Driver
     *
     * @param type
     * @return
     */
    public static String getDriver(String type) {
        switch (type) {
            case Constants.DATABASE_TYPE.MYSQL:
            case Constants.DATABASE_TYPE.DORIS:
            case Constants.DATABASE_TYPE.STAR_ROCKS:
                return MYSQL_DRIVER;

            case Constants.DATABASE_TYPE.ORACLE:
                return ORACLE_DRIVER;

            case Constants.DATABASE_TYPE.POSTGRESQL:
                return POSTGRESQL_DRIVER;

            case Constants.DATABASE_TYPE.SQL_SERVER:
                return SQLSERVER_DRIVER;

            case Constants.DATABASE_TYPE.DM8:
                return DM_DRIVER;

            default:
                throw new IllegalArgumentException("Unsupported database type: " + type);
        }
    }

    /**
     * 获取数据库连接（基本方式）
     */
    public static Connection getConnection(String driverName, String url, String user, String password) {
        return getConnection(driverName, url, user, password, null);
    }

    /**
     * 获取数据库连接（带schema）
     */
    public static Connection getConnection(String driverName, String url, String user, String password, String schema) {
        Properties properties = new Properties();
        properties.put("user", user);
        properties.put("password", password);
        if (StringUtils.isNotBlank(schema)) {
            properties.put("schema", schema);
        }
        return createConnection(driverName, url, properties);
    }

    /**
     * 获取数据库连接（使用DatabaseVo）
     */
    public static Connection getConnection(String driverName, String url, DatabaseVo dbVo) {
        return createConnection(driverName, url, buildConnectionProperties(dbVo, true));
    }

    /**
     * 获取数据库连接（用于DDL操作）
     */
    public static Connection getConnectionForDDL(String driverName, String url, DatabaseVo dbVo) {
        Properties properties = buildConnectionProperties(dbVo, false);
        properties.remove("useCursorFetch"); // DDL操作不需要游标获取
        return createConnection(driverName, url, properties);
    }

    /**
     * 创建数据库连接（核心方法）
     */
    private static Connection createConnection(String driverName, String url, Properties properties) {
        try {
            Class.forName(driverName);
            DriverManager.setLoginTimeout(LOGIN_TIMEOUT);
            return DriverManager.getConnection(url, properties);
        } catch (ClassNotFoundException e) {
            String errorMsg = "JDBC driver not found: " + driverName;
            logger.error(errorMsg, e);
            throw new ETLException(errorMsg);
        } catch (SQLException e) {
            String errorMsg = "Failed to get database connection to: " + url;
            logger.error(errorMsg, e);
            throw new ETLException(errorMsg);
        }
    }

    /**
     * 构建连接属性
     */
    private static Properties buildConnectionProperties(DatabaseVo dbVo, boolean useCursorFetch) {
        Properties properties = new Properties();
        properties.put("user", dbVo.getUsername());
        properties.put("password", dbVo.getPassword());

        // 设置schema
        if (StringUtils.isNotBlank(dbVo.getDbSchema())) {
            String schemaKey = Constants.DATABASE_TYPE.POSTGRESQL.equals(dbVo.getType())
                    ? "currentSchema" : "schema";
            properties.setProperty(schemaKey, dbVo.getDbSchema());
        }

        // MySQL类型数据库的特殊配置
        if (Constants.DATABASE_TYPE.MYSQL.equals(dbVo.getType())
                || Constants.DATABASE_TYPE.DORIS.equals(dbVo.getType())) {
            addMysqlDefaultProperties(properties, useCursorFetch);
        }

        // 添加额外配置属性
        if (StringUtils.isNotBlank(dbVo.getProperties())) {
            Map<String, String> extraProps = JSON.parseObject(dbVo.getProperties(), HashMap.class);
            properties.putAll(extraProps);
        }

        return properties;
    }

    /**
     * 添加MySQL默认连接属性
     */
    private static void addMysqlDefaultProperties(Properties properties, boolean useCursorFetch) {
        properties.put("useSSL", "false");
        properties.put("useUnicode", "yes");
        properties.put("characterEncoding", "UTF-8");
        properties.put("zeroDateTimeBehavior", "convertToNull");
        properties.put("allowMultiQueries", "true");
        properties.put("serverTimezone", "Asia/Shanghai");
        properties.put("useOldAliasMetadataBehavior", "true");

        if (useCursorFetch) {
            properties.put("useCursorFetch", "true");
        }
    }


    public static PreparedStatement preparedStatement(Connection connection, String sql) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            return preparedStatement;
        } catch (SQLException e) {
            logger.error("获取PreparedStatement失败！", e);
            throw new ETLException("获取PreparedStatement失败！");
        }
    }

    /**
     * 关闭 Connection
     *
     * @param conn
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("关闭 jdbc Connection 失败！", e);
            }
        }
    }

    /**
     * 关闭 Statement
     *
     * @param state
     */
    public static void closeStatement(Statement state) {
        if (state != null) {
            try {
                state.close();
            } catch (Exception e) {
                logger.error("关闭jdbc Statement 失败！", e);
            }
        }
    }

    /**
     * 关闭 ResultSet
     *
     * @param rs
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("关闭jdbc ResultSet 失败！", e);
            }
        }
    }

    /**
     * 提交
     *
     * @param conn
     */
    public static void commit(Connection conn) {
        if (conn != null) {
            try {
                conn.commit();
            } catch (SQLException e) {
                logger.error("提交事务失败!", e);
            }
        }
    }

    /**
     * 回滚
     *
     * @param conn
     */
    public static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                logger.error("事务回滚失败!", e);
            }
        }
    }

    public static void close(ResultSet rs) {
        closeResultSet(rs);
    }

    public static void close(Statement state) {
        closeStatement(state);
    }

    public static void close(Connection conn) {
        closeConnection(conn);
    }

}
