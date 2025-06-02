package com.etledge.collect.trans.util;

import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

/**
 * @Author: yc
 * @CreateTime: 2025-05-09
 * @Description: 转换步骤构造器
 * @Version: 1.0
 */
public abstract class AbstractStepMetaConstructor implements StepMetaAware {

    /**
     * 创建步骤
     * @param config
     * @param transMeta
     * @param o
     * @return
     */
    public abstract StepMeta createStepMeta(String config, TransMeta transMeta, Object o);

    /**
     * 前置步骤
     */
    @Override
    public void preProcessCreateStepMeta() {

    }

    /**
     * 后置步骤
     */
    @Override
    public void postProcessCreateStepMeta() {

    }
}