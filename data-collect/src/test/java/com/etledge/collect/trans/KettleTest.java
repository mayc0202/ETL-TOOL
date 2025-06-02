package com.etledge.collect.trans;

import com.etledge.collect.trans.util.DatabaseMetaConstructor;
import com.etledge.collect.trans.util.TransMetaConstructor;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;

import java.util.UUID;

/**
 * @Author: yc
 * @CreateTime: 2025-05-08
 * @Description:
 * @Version: 1.0
 */
public class KettleTest {

    @Test
    public void testKettlePlugin() throws KettleStepException {
        TransMetaConstructor.initEnvironment();

        DatabaseMeta databaseMeta = DatabaseMetaConstructor.getDatabaseMeta();

        TableInputMeta tableInputMeta = TransMetaConstructor.getTableInputStepMeta(databaseMeta);
        StepMetaInterface tableOutputStepMeta = TransMetaConstructor.getTableOutputStepMeta(databaseMeta);

        StepMeta fromStep = TransMetaConstructor.createStepMeta(true, 100, 100,
                "tableInput", "表输入", tableInputMeta);

        StepMeta toStep = TransMetaConstructor.createStepMeta(true, 200, 100,
                "tableOutput", "表输出", tableOutputStepMeta);

        TransMeta transMeta = TransMetaConstructor.createTransMeta(databaseMeta, databaseMeta, fromStep, toStep,
                null, null, UUID.randomUUID().toString().replaceAll("-", ""));

        Trans trans = null;
        String logChannelId = null;

        try {
            trans = new Trans(transMeta);
            trans.getTransMeta().setInternalKettleVariables();
            trans.copyParametersFrom(transMeta);
            trans.setPreview(true);

            logChannelId = trans.getLogChannelId();

            // 准备执行 转换
            trans.prepareExecution(null);

            // 执行
            trans.execute(null);

            // 等待完成
            trans.waitUntilFinished();

            // 继续睡眠2秒，保证trans执行完
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new KettleStepException(String.format("转换被中断：%s",e.getMessage()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("错误：" + e.getMessage());
        } finally {

        }
    }
}