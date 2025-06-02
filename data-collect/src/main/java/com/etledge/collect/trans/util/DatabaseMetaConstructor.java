package com.etledge.collect.trans.util;

import org.pentaho.di.core.database.DatabaseMeta;


/**
 * @Author: yc
 * @CreateTime: 2024-12-12
 * @Description: 数据源构造器
 * @Version: 1.0
 */
public class DatabaseMetaConstructor {

    static final String MYSQL = "MySQL";
    static final String POSTGRESQL = "PostgreSQL";
    static final String ORACLE = "Oracle";
    static final String CACHE = "Cache";
    static final String KYUUBI = "Kyuubi";
    static final String DM8 = "DM8";

    static final String ATRRIBUTE_CUSTOM_URL = "CUSTOM_URL";
    static final String ATRRIBUTE_CUSTOM_DRIVER_CLASS = "CUSTOM_DRIVER_CLASS";

    static final String IRIS_DRIVER = "com.intersystems.jdbc.IRISDriver";
    static final String DM8_DRIVER = "dm.jdbc.driver.DmDriver";
    static final String KYUUBI_HIVE_DRIVER = "org.apache.kyuubi.jdbc.KyuubiHiveDriver";

    /**
     * 获取数据源对象
     *
     * @return
     */
    public static DatabaseMeta getDatabaseMeta() {

        DatabaseMeta databaseMeta = null;
        databaseMeta = new DatabaseMeta(
                "xqh-ddtt",
                "MySQL",
                "Native",
                "192.168.1.101",
                "xqh-ddtt",
                "3306",
                "root",
                "1q2w3e4R");
//        if (DatabaseConstants.SERVICE.equals(config.getConnectType())) {
//            String dbName = String.format("(DESCRIPTION =(ADDRESS = (PROTOCOL = TCP)(HOST = %s)(PORT = %s))(CONNECT_DATA = (SERVER = DEDICATED)(SERVICE_NAME = %s)))",config.getHost(),config.getPort(),config.getDbName());
//            databaseMeta = new DatabaseMeta(config.getName(), newType, config.getAccess(), "", dbName, "-1", config.getUser(), config.getPassword());
//            databaseMeta.setServername(config.getDbName());
//        } else {
//            databaseMeta = new DatabaseMeta(
//                    config.getName(),
//                    newType,
//                    config.getAccess(),
//                    config.getHost(),
//                    config.getDbName(),
//                    config.getPort(),
//                    config.getUser(),
//                    config.getPassword());
//        }

        // schema
//        if (StringUtils.isNotBlank(config.getSchema())) {
//            databaseMeta.setPreferredSchemaName(config.getSchema());
//        }

        // 添加扩展
        addExtraOption(databaseMeta);
        return databaseMeta;
    }

    /**
     * 添加扩展参数
     *
     * @param databaseMeta
     */
    private static void addExtraOption(DatabaseMeta databaseMeta) {
        switch (MYSQL) {
            case MYSQL:
                databaseMeta.addExtraOption("MySQL", "characterEncoding", "UTF-8");
                databaseMeta.addExtraOption("MySQL", "useServerPrepStmts", "false");
                databaseMeta.addExtraOption("MySQL", "rewriteBatchedStatements", "true");
                databaseMeta.addExtraOption("MySQL", "useCompression", "true");
                databaseMeta.addExtraOption("MySQL", "useSSL", "false");
                databaseMeta.addExtraOption("MySQL", "serverTimezone", "Asia/Shanghai");
                databaseMeta.addExtraOption("MySQL", "zeroDateTimeBehavior", "convertToNull");
                break;
//            case DM8:
//                Properties attributes = databaseMeta.getAttributes();
//                attributes.put(ATRRIBUTE_CUSTOM_URL,
//                        String.format("jdbc:dm://%s:%s?schema=%s", config.getHost(), config.getPort(), config.getDbName()));
//                attributes.put(ATRRIBUTE_CUSTOM_DRIVER_CLASS, DM8_DRIVER);
//
//                //扩展属性解析拼接
//                String properties = config.getProperties();
//                if (StringUtils.isNotBlank(properties)) {
//                    JSONObject proObj = JSONObject.parseObject(properties);
//                    for (String key : proObj.keySet()) {
//                        attributes.put(key, proObj.getString(key));
//                    }
//                }
//                databaseMeta.setAttributes(attributes);
//
//                break;
            default:
                break;
        }
    }

}