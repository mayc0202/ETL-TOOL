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
    interface DataBaseType {
        String mysql = "MySQL";
        String oracle = "Oracle";
        String sqlServer = "SQLServer";
        String postgresql="PostgreSQL";
        String dm = "DM8";
        String starRocks= "StarRocks";
        String doris = "Doris";

        String kafka = "Kafka";

        String redis = "Redis";
        String mongoDB = "MongoDB";
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
     * delete flag
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
     * plugin code
     */
    interface PLUGIN_CODE {}
}
