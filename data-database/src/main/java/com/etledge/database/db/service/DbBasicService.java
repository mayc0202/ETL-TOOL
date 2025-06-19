package com.etledge.database.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.etledge.database.db.entity.DbBasic;
import com.etledge.database.db.vo.DbBasicVo;
import com.etledge.database.db.vo.DbCategoryVo;

import java.util.List;

/**
 * (DbBasic)表服务接口
 *
 * @author mayc
 * @since 2025-05-20 23:34:43
 */
public interface DbBasicService extends IService<DbBasic> {

    /**
     * query database category list
     *
     * @return
     */
    List<DbCategoryVo> getDbCategoryList();

    /**
     * query database basic list
     *
     * @return
     */
    List<DbBasicVo> getDbBasicList();

    /**
     * query database basic list by category id
     *
     * @param categoryId
     * @return
     */
    List<DbBasicVo> getDbBasicListByCategoryId(Integer categoryId);

}

