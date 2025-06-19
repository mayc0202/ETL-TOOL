package com.etledge.database.dict.service;

import com.etledge.database.dict.vo.DictItem;

import java.util.List;

public interface DictService {

    /**
     * 获取字典
     *
     * @param dictCode
     * @return
     */
    List<DictItem> getDict(String dictCode);

    /**
     * 获取字典项code
     *
     * @param dictCode
     * @param itemValue
     * @return
     */
    String getDictItemCode(String dictCode, String itemValue);
}
