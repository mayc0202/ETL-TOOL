package com.etledge.collect.trans.util;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;

/**
 * @Author: yc
 * @CreateTime: 2024-12-13
 * @Description: 转换对象构造器
 * @Version: 1.0
 */
public class TransMetaConstructor {

    public static final String TABLE_OUTPUT = "tableOutput";
    public static final String CONSTANT = "Constant";
    public static final String CONSTANT_TXT = "常量组件";
    public static final String SYSTEM_DATE = "SystemDate";
    public static final String SYSTEM_DATE_TXT = "系统时间";

    public static final String MYSQL = "MySQL";
    public static final String POSTGRESQL = "PostgreSQL";
    public static final String SQLSERVER = "SQLServer";
    public static final String ORACLE = "Oracle";
    public static final String CACHE = "Cache";
    public static final String KYUUBI = "Kyuubi";

    /**
     * 初始化环境
     */
    public static void initEnvironment() {
        try {
            KettleEnvironment.init();
            EnvUtil.environmentInit();
            PluginRegistry.addPluginType(StepPluginType.getInstance());
        } catch (KettleException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取 表输入StepMeta
     *
     * @param databaseMeta
     * @return
     */
    public static TableInputMeta getTableInputStepMeta(DatabaseMeta databaseMeta) {

        TableInputMeta tableInput = new TableInputMeta();
        tableInput.setDefault();
        tableInput.setDatabaseMeta(databaseMeta);                                                        // 来源表数据源

        String sql = "select * from app_user limit 100";
        tableInput.setSQL(sql);                                                                          // 查询sql
        return tableInput;
    }

    /**
     * 获取表输出组件
     *
     * @param databaseMeta
     * @return
     * @throws KettleStepException
     */
    public static StepMetaInterface getTableOutputStepMeta(DatabaseMeta databaseMeta) throws KettleStepException {
        Boolean clear = true;
        switch ("tableOutput") {
            case TABLE_OUTPUT:
                TableOutputMeta tableOutputMeta = new TableOutputMeta();
                tableOutputMeta.setDefault();
                tableOutputMeta.setDatabaseMeta(databaseMeta);                                                    // 目标表数据源
//                tableOutputMeta.setSchemaName(StringUtils.isEmpty(config.getSchema()) ? "" : config.getSchema()); // schema
                tableOutputMeta.setTableName("app_user2");                               // 数据表名
                tableOutputMeta.setCommitSize(2000);                                                              // 批次不宜过大，会导致mysql报错
                tableOutputMeta.setSpecifyFields(true);
                String[] fieldStream = {"id","name","email","phone","gender","password","age","create_time","update_time"};
                tableOutputMeta.setFieldStream(fieldStream);                          // 流中字段
                tableOutputMeta.setFieldDatabase(fieldStream);                        // 目标表字段
                if (null != clear) {
                    tableOutputMeta.setTruncateTable(clear);                                                      // 全量同步之前，需要删除目标表中所有的数据
                }
                return tableOutputMeta;
            default:
                throw new KettleStepException("请校验组件是否存在！");
        }
    }

    /**
     * 构建 TransMeta
     *
     * @param sourceDataMeta
     * @param targetDataMeta
     * @param fromStep
     * @param toStep
     * @param constantStep
     * @param systemDateStep
     * @param uuid
     * @return
     */
    public static TransMeta createTransMeta(DatabaseMeta sourceDataMeta,
                                            DatabaseMeta targetDataMeta,
                                            StepMeta fromStep,
                                            StepMeta toStep,
                                            StepMeta constantStep,
                                            StepMeta systemDateStep,
                                            String uuid
    ) {

        // 初始化 TransMeta 对象
        TransMeta transMeta = new TransMeta();
        transMeta.setCarteObjectId(uuid);                      // 设置唯一 ID

        // 添加数据源
        transMeta.addDatabase(sourceDataMeta);                 // 添加表输入数据源
        transMeta.addDatabase(targetDataMeta);                 // 添加表输出数据源

        // 添加步骤
        transMeta.addStep(fromStep);                           // 添加来源步骤

        if (constantStep != null && systemDateStep == null) {
            transMeta.addStep(constantStep);                   // 添加常量步骤
            // 连线 fromStep -> constantStep
            TransHopMeta hop1 = new TransHopMeta(fromStep, constantStep);
            transMeta.addTransHop(hop1);
            // 连线 constantStep -> toStep
            TransHopMeta hop2 = new TransHopMeta(constantStep, toStep);
            transMeta.addTransHop(hop2);
        } else if (constantStep == null && systemDateStep != null) {
            transMeta.addStep(systemDateStep);                 // 添加系统日期步骤
            // 连线 fromStep -> systemDateStep
            TransHopMeta hop1 = new TransHopMeta(fromStep, systemDateStep);
            transMeta.addTransHop(hop1);
            // 连线 systemDateStep -> toStep
            TransHopMeta hop2 = new TransHopMeta(systemDateStep, toStep);
            transMeta.addTransHop(hop2);
        } else if (constantStep != null && systemDateStep != null) {
            transMeta.addStep(constantStep);                   // 添加常量步骤
            transMeta.addStep(systemDateStep);                 // 添加系统日期步骤
            // 连线 fromStep -> constantStep
            TransHopMeta hop1 = new TransHopMeta(fromStep, constantStep);
            transMeta.addTransHop(hop1);
            // 连线 constantStep -> systemDateStep
            TransHopMeta hop2 = new TransHopMeta(constantStep, systemDateStep);
            transMeta.addTransHop(hop2);
            // 连线 systemDateStep -> toStep
            TransHopMeta hop3 = new TransHopMeta(systemDateStep, toStep);
            transMeta.addTransHop(hop3);
        } else {
            // 连线 fromStep -> toStep
            TransHopMeta hop = new TransHopMeta(fromStep, toStep);
            transMeta.addTransHop(hop);
        }
        transMeta.addStep(toStep);                             // 添加目标步骤
        return transMeta;
    }

    /**
     * 创建step
     *
     * @param draw
     * @param x
     * @param y
     * @param code
     * @param description
     * @return
     */
    public static StepMeta createStepMeta(boolean draw,
                                          int x,
                                          int y,
                                          String code,
                                          String description,
                                          StepMetaInterface stepMeta) {

        StepMeta step = new StepMeta(code, description, stepMeta);
        step.setDraw(draw);
        step.setLocation(x, y); // 位置
        return step;
    }
}