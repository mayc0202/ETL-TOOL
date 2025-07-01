package com.etledge.database.db.connector.relationdb;

import com.etledge.common.Constants;
import com.etledge.database.config.exception.ETLException;
import com.etledge.database.db.connector.relationdb.entity.ConResponse;
import com.etledge.database.db.connector.util.JdbcUtil;
import com.etledge.database.db.vo.DatabaseVo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * 数据源连接器抽象类
 */
@Data
public abstract class AbstractDatabaseConnector {

    public Logger logger = LoggerFactory.getLogger(AbstractDatabaseConnector.class);

    // 主机
    private String host;
    // 端口
    private Integer port;
    // 用户
    private String username;
    // 密码
    private String password;
    // 数据源类型
    private String type;
    // 数据库名称
    private String dbName;
    // 模式
    private String schema;
    // 驱动类
    private String driver;
    // url
    private String url;
    // 配置
    private String properties;
    // 扩展配置
    private String extConfig;


    /**
     * 设置完数据库连接信息后，调用build方法，构建连接url等信息；
     *
     * @return
     */
    public abstract AbstractDatabaseConnector build();

//    /**
//     * 获取数据库下所有的表基础信息
//     * @return Map<key,Value>  key=表名称；Value=表信息
//     */
//    public abstract Map<String, DBTableInfo> getTables() throws SQLException;
//
//    /**
//     * 获取数据库中指定表名的表信息
//     * @param tableName
//     * @return
//     */
//    public abstract DBTableWithColumnInfo getTable(String tableName);
//
//    /**
//     * 获取数据库下所有的表信息，包括表的字段信息；
//     * @return Map<key,Value>  key=表名称；Value=表信息
//     */
//    public abstract Map<String, DBTableWithColumnInfo> getTableWithColumnInfos();
//
//    /**
//     * 获取数据库下所有的表信息，包括表的字段信息；
//     * @param inTableNames   仅包含 inTableNames 指定的表
//     * @param notInTableNames  排除 notInTableNames 包含的表
//     * @return Map<key,Value>  key=表名称；Value=表信息
//     */
//    public abstract Map<String, DBTableWithColumnInfo> getTableWithColumnInfos(List<String> inTableNames, List<String> notInTableNames);

    /**
     * 获取数据源连接
     *
     * @return
     */
    public abstract Connection getConnection();


//    /**
//     * 获取指定表的列信息
//     * @param tableName 表名
//     * @return
//     */
//    public abstract List<DBColumnInfo> getColumns(String tableName);
//
//    /**
//     * 获取指定表的数据量
//     * @param tableName 表名
//     * @return
//     */
//    public abstract Long getDataVolumeByTableName(String tableName, Connection conn);
//
//    public abstract void checkDatabaseInfo(DatabaseForm form);
//
//
//    /**
//     * 获取所有外键
//     * @return
//     */
//    public abstract List<DBForeignKeyInfo> getForeignKeys();
//
//    /**
//     * 获取所有索引
//     * @return
//     */
//    public abstract List<DBIndexInfo> getIndexes();
//
//    public void executeIgnoreError(String sql, Connection conn) {
//        Statement stat = null;
//
//        try{
//            stat = conn.createStatement();
//
//            if(sql.endsWith(";")){
//                sql = sql.substring(0,sql.length()-1);
//            }
//            stat.execute(sql);
//
//        }catch (Exception e) {
//            //
//        }finally {
//            JdbcUtil.close(stat);
//        }
//    }

    /**
     * 封装数据源信息
     *
     * @return
     */
    public DatabaseVo getDatabaseVo() {
        DatabaseVo dbVo = new DatabaseVo();
        dbVo.setUsername(username);
        dbVo.setPassword(password);
        dbVo.setDbType(type);
        dbVo.setDbSchema(schema);
        dbVo.setProperties(properties);
        dbVo.setExtConfig(extConfig);
        return dbVo;
    }

    /**
     * 测试连接
     *
     * @param dbType
     * @return
     */
    public ConResponse test(String dbType) {
        Connection conn = null;
        Statement stat = null;
        ConResponse response = new ConResponse();
        ResultSet rs = null;
        try {

            conn = JdbcUtil.getConnection(this.driver, this.url, getDatabaseVo());

            if (StringUtils.isNotEmpty(schema)
                    && !"".equals(schema.trim())
                    && !Constants.DATABASE_TYPE.POSTGRESQL.equals(dbType)
                    && !Constants.DATABASE_TYPE.SQL_SERVER.equals(dbType)) {
                stat = conn.createStatement();
                stat.execute(" ALTER SESSION SET CURRENT_SCHEMA = \"" + schema + "\" ");
            }
            if (StringUtils.isNotEmpty(schema) && !"".equals(schema.trim()) && (Constants.DATABASE_TYPE.POSTGRESQL.equals(dbType))) {
                stat = conn.createStatement();
                rs = stat.executeQuery("SELECT EXISTS(SELECT 1 FROM pg_namespace WHERE nspname = '" + schema + "')");
                while (rs.next()) {
                    String verifyResult = rs.getString(1);
                    if ("f".equalsIgnoreCase(verifyResult)) {
                        throw new ETLException(String.format("模式Schema【%s】不存在！", schema));
                    }
                }
            }
            if (StringUtils.isNotEmpty(schema) && !"".equals(schema.trim()) && (Constants.DATABASE_TYPE.SQL_SERVER.equals(dbType))) {
                stat = conn.createStatement();
                rs = stat.executeQuery("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + schema + "'");
                if (null == rs) {
                    throw new ETLException(String.format("模式Schema【%s】不存在！", schema));
                }
            }
            response.setResult(true);
            response.setMsg("连接成功！");
        } catch (Exception e) {
            logger.error(null, e);
            response.setResult(false);
            response.setMsg(String.format("连接失败! 原因:%s", e.getMessage()));
        } finally {
            JdbcUtil.close(rs);
            JdbcUtil.close(stat);
            JdbcUtil.closeConnection(conn);
        }

        return response;
    }
}
