package com.etledge.database.db.connector.relationdb;

import com.etledge.common.Constants;
import com.etledge.database.config.exception.ETLException;
import com.etledge.database.db.connector.relationdb.*;
/**
 * 数据源连接器工厂
 */
public class DatabaseConnectorFactory {

    public static AbstractDatabaseConnector getConnector(String type) {
        switch (type) {
            case Constants.DATABASE_TYPE.MYSQL:
                return new MySQLConnector();
//            case Constants.DATABASE_TYPE.ORACLE:
//                return new OracleDBConnector();
//            case Constants.DATABASE_TYPE.POSTGRESQL:
//                return new PostgresqlConnector();
//            case Constants.DATABASE_TYPE.SQL_SERVER:
//                return new SqlServerDBConnector();
//            case Constants.DATABASE_TYPE.DM8:
//                return new DmDBConnector();
//            case Constants.DATABASE_TYPE.DORIS:
//                return new DorisDBConnector();
//            case Constants.DATABASE_TYPE.STAR_ROCKS:
//                return new StarRocksDBConnector();
            default:
                throw new ETLException(String.format("暂不支持此类型数据库![type:%s]", type));
        }
    }

}
