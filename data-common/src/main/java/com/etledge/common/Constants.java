package com.etledge.common;

public interface Constants {

    String DICT_FILE = "dict.json";

    /**
     * 字典标识
     */
    interface DICT {
        String DATA_SOURCE_LAYERING = "data_source_layering"; // 数据源分层
    }

    /**
     * 数据库类型
     */
    interface DATABASE_TYPE {
        String MYSQL = "MySQL";
        String ORACLE = "Oracle";
        String SQL_SERVER = "SQLServer";
        String POSTGRESQL = "PostgreSQL";
        String DM8 = "DM8";
        String STAR_ROCKS = "StarRocks";
        String DORIS = "Doris";

        String REDIS = "Redis";
        String MONGODB = "MongoDB";

        String KAFKA = "Kafka";
    }

    /**
     * FTP/FTPS类型
     */
    interface FTP_TYPE {
        String FTP = "Ftp";
        String FTPS = "FTPS";
    }

    /**
     * 数据类型
     */
    interface DATA_TYPE {
        String BLOB = "BLOB";
        String CLOB = "CLOB";
        String DATE = "DATE";
        String TIMESTAMP = "TIMESTAMP";
        String VARCHAR = "VARCHAR";
        String INT = "INT";
        String BIT = "BIT";
        String CHAR = "CHAR";
        String TEXT = "TEXT";
        String REAL = "REAL";
        String BINARY = "BINARY";
        String VARBINARY = "VARBINARY";
        String DOUBLE = "DOUBLE";
    }

    /**
     * 数据源扩展配置
     */
    interface DATABASE_EXT_CONFIG {
        String BE_ADDRESS = "beAddress";
        String FE_ADDRESS = "feAddress";
        String ORACLE_CONNECT_TYPE = "connectType";

    }

    interface ORACLE_CONNECT_TYPE {
        String SID = "sid";
        String SERVICE = "service";
    }

    /**
     * token
     */
    interface ETL_EDGE_TOKEN_CONFIG {
        String ETL_EDGE_TOKEN = "ETLedge-Token";
        String BEARER_PREFIX = "ETL-Bearer:";
        String SSO_KEY_PREFIX = "sso:user:";
        String USER_STATUS_PREFIX = "user:status:";
        String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";
        String RATE_LIMIT_PREFIX = "rate_limit:";
        String AUTHORIZATION = "Authorization";
    }

    /**
     * 删除标记
     */
    interface DELETE_FLAG {
        Boolean TRUE = true;
        Boolean FALSE = false;
    }

    interface TREE_TYPE {
        String GROUP = "GROUP";
        String DATABASE_SOURCE = "DATABASE_SOURCE";
    }

    /**
     * redis key
     */
    interface REDIS_KEY {
        String CATEGORY = "category";
        String DB_BASIC = "db_basic";
    }

    /**
     * 组件code
     */
    interface PLUGIN_CODE {
    }
}
