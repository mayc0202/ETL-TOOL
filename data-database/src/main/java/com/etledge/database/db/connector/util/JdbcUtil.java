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
import java.util.Properties;


/**
 * JDBC工具类
 */
public class JdbcUtil {

    private static Logger logger = LoggerFactory.getLogger(JdbcUtil.class);

    private static final String MYSQL_URL_FORMAT = "jdbc:mysql://%s:%s/%s";

    private static final String ORACLE_SERVICE_URL_FORMAT = "jdbc:oracle:thin:@//%s:%s/%s";

    private static final String ORACLE_SID_URL_FORMAT = "jdbc:oracle:thin:@%s:%s:%s";

    private static final String POSTGRESQL_URL_FORMAT = "jdbc:postgresql://%s:%s/%s";

    private static final String SQLSERVER_URL_FORMAT = "jdbc:sqlserver://%s:%s;DatabaseName=%s";

    private static final String DM_URL_FORMAT = "jdbc:dm://%s:%s/%s";

    private static final String OPENGAUSS_URL_FORMAT = "jdbc:postgresql://%s:%s/%s";

    private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";

    private static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";

    private static final String SQLSERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    private static final String DM_DRIVER = "dm.jdbc.driver.DmDriver";


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
        String jdbcUrl = null;

        if (Constants.DATABASE_TYPE.MYSQL.equals(type) ||
                Constants.DATABASE_TYPE.DORIS.equals(type) ||
                Constants.DATABASE_TYPE.STAR_ROCKS.equals(type)) {
            jdbcUrl = String.format(MYSQL_URL_FORMAT, host, port, dbName);
        } else if (Constants.DATABASE_TYPE.ORACLE.equals(type)) {
            JSONObject jsonObject = JSON.parseObject(extConfig);
            String connectType = jsonObject.getString(Constants.DATABASE_EXT_CONFIG.ORACLE_CONNECT_TYPE);
            if (connectType != null && connectType.equals(Constants.ORACLE_CONNECT_TYPE.SID)) {
                jdbcUrl = String.format(ORACLE_SID_URL_FORMAT, host, port, dbName);
            } else {
                jdbcUrl = String.format(ORACLE_SERVICE_URL_FORMAT, host, port, dbName);
            }
        } else if (Constants.DATABASE_TYPE.POSTGRESQL.equals(type)) {
            jdbcUrl = String.format(POSTGRESQL_URL_FORMAT, host, port, dbName);
        } else if (Constants.DATABASE_TYPE.SQL_SERVER.equals(type)) {
            jdbcUrl = String.format(SQLSERVER_URL_FORMAT, host, port, dbName);
        } else if (Constants.DATABASE_TYPE.DM8.equals(type)) {
            jdbcUrl = String.format(DM_URL_FORMAT, host, port, dbName);
        }

        return jdbcUrl;
    }

    /**
     * 获取Driver
     *
     * @param type
     * @return
     */
    public static String getDriver(String type) {
        String driverName = null;

        if (Constants.DATABASE_TYPE.MYSQL.equals(type) ||
                Constants.DATABASE_TYPE.DORIS.equals(type) ||
                Constants.DATABASE_TYPE.STAR_ROCKS.equals(type)) {
            driverName = MYSQL_DRIVER;
        } else if (Constants.DATABASE_TYPE.ORACLE.equals(type)) {
            driverName = ORACLE_DRIVER;
        } else if (Constants.DATABASE_TYPE.POSTGRESQL.equals(type)) {
            driverName = POSTGRESQL_DRIVER;
        } else if (Constants.DATABASE_TYPE.SQL_SERVER.equals(type)) {
            driverName = SQLSERVER_DRIVER;
        } else if (Constants.DATABASE_TYPE.DM8.equals(type)) {
            driverName = DM_DRIVER;
        }

        return driverName;
    }

    /**
     * 获取连接1
     *
     * @param driverName
     * @param url
     * @param user
     * @param password
     * @return
     */
    public static Connection getConnection(String driverName, String url, String user, String password) {
        Connection conn = null;
        try {
            // 加载驱动
            Class.forName(driverName);
            DriverManager.setLoginTimeout(10);
            conn = DriverManager.getConnection(url, user, password);
            return conn;
        } catch (Exception e) {
            logger.error(null, e);
            throw new ETLException("获取数据库连接失败！");
        }

    }

    /**
     * 获取连接2
     *
     * @param driverName
     * @param url
     * @param user
     * @param password
     * @param schema
     * @return
     */
    public static Connection getConnection(String driverName, String url, String user, String password, String schema) {
        Connection conn = null;
        try {
            // 加载驱动
            Class.forName(driverName);
            DriverManager.setLoginTimeout(10);
            Properties properties = new Properties();
            properties.put("user", user);
            properties.put("password", password);
            properties.put("schema", schema);
            conn = DriverManager.getConnection(url, properties);
            return conn;
        } catch (Exception e) {
            logger.error(null, e);
            throw new ETLException("获取数据库连接失败！");
        }

    }

    /**
     * 获取连接3
     *
     * @param driverName
     * @param url
     * @param dbVo
     * @return
     */
    public static Connection getConnection(String driverName, String url, DatabaseVo dbVo) {
        Connection conn = null;
        try {
            // 加载驱动
            Class.forName(driverName);
            DriverManager.setLoginTimeout(10);
            Properties properties = new Properties();

            // 设置用户名、密码
            properties.put("user", dbVo.getUsername());
            properties.put("password", dbVo.getPassword());

            // Schema 设置（按数据库类型适配）
            if (StringUtils.isNotBlank(dbVo.getDbSchema())) {
                if (Constants.DATABASE_TYPE.POSTGRESQL.equals(dbVo.getType())) {
                    properties.setProperty("currentSchema", dbVo.getDbSchema());
                } else {
                    properties.setProperty("schema", dbVo.getDbSchema());
                }
            }

            // mysql类型数据库设置默认连接参数 按照系统情况修改默认参数
            if (Constants.DATABASE_TYPE.MYSQL.equals(dbVo.getType())
                    || Constants.DATABASE_TYPE.DORIS.equals(dbVo.getType())) {
                properties.put("useCursorFetch", "true");
                properties.put("useSSL", "false");
                properties.put("useUnicode", "yes");
                properties.put("characterEncoding", "UTF-8");
                properties.put("zeroDateTimeBehavior", "convertToNull");
                properties.put("allowMultiQueries", "true");
                properties.put("serverTimezone", "Asia/Shanghai");
                properties.put("useOldAliasMetadataBehavior", "true");
            }

            if (StringUtils.isNotBlank(dbVo.getProperties())) {
                HashMap<String, String> hashMap = JSONObject.parseObject(dbVo.getProperties(), HashMap.class);
                properties.putAll(hashMap);
            }

            conn = DriverManager.getConnection(url, properties);
            return conn;
        } catch (Exception e) {
            logger.error(null, e);
            throw new ETLException(e.getMessage());
        }

    }

    public static Connection getConnectionForDDL(String driverName, String url, DatabaseVo dbVo) {
        Connection conn = null;
        try {
            // 加载驱动
            Class.forName(driverName);
            DriverManager.setLoginTimeout(10);
            Properties properties = new Properties();

            // 设置用户名、密码
            properties.put("user", dbVo.getUsername());
            properties.put("password", dbVo.getPassword());

            // Schema 设置（按数据库类型适配）
            if (StringUtils.isNotBlank(dbVo.getDbSchema())) {
                if (Constants.DATABASE_TYPE.POSTGRESQL.equals(dbVo.getType())) {
                    properties.setProperty("currentSchema", dbVo.getDbSchema());
                } else {
                    properties.setProperty("schema", dbVo.getDbSchema());
                }
            }

            // mysql类型数据库设置默认连接参数 按照系统情况修改默认参数
            if (Constants.DATABASE_TYPE.MYSQL.equals(dbVo.getType())
                    || Constants.DATABASE_TYPE.DORIS.equals(dbVo.getType())) {
                properties.put("useSSL", "false");
                properties.put("useUnicode", "yes");
                properties.put("characterEncoding", "UTF-8");
                properties.put("zeroDateTimeBehavior", "convertToNull");
                properties.put("allowMultiQueries", "true");
                properties.put("serverTimezone", "Asia/Shanghai");
                properties.put("useOldAliasMetadataBehavior", "true");
            }

            if (StringUtils.isNotBlank(dbVo.getProperties())) {
                HashMap<String, String> hashMap = JSONObject.parseObject(dbVo.getProperties(), HashMap.class);
                properties.putAll(hashMap);
            }

            properties.remove("useCursorFetch");

            conn = DriverManager.getConnection(url, properties);
            return conn;
        } catch (Exception e) {
            logger.error(null, e);
            throw new ETLException(e.getMessage());
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

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("关闭 jdbc Connection 失败！", e);
            }
        }
    }

    public static void closeStatement(Statement state) {
        if (state != null) {
            try {
                state.close();
            } catch (Exception e) {
                logger.error("关闭jdbc Statement 失败！", e);
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("关闭jdbc ResultSet 失败！", e);
            }
        }
    }

    public static void commit(Connection conn) {
        if (conn != null) {
            try {
                conn.commit();
            } catch (SQLException e) {
                logger.error("提交事务失败!", e);
            }
        }
    }

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
