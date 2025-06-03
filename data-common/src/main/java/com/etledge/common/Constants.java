package com.etledge.common;

public interface Constants {

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
