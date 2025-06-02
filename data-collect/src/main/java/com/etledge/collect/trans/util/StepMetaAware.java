package com.etledge.collect.trans.util;

/**
 * @Author: yc
 * @CreateTime: 2025-05-09
 * @Description: 初始化Step回调机制
 * @Version: 1.0
 */
public interface StepMetaAware {

    /**
     * 前置处理
     */
    void preProcessCreateStepMeta();

    /**
     * 后置处理
     */
    void postProcessCreateStepMeta();
}
