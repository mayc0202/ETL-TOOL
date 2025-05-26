package com.ds.etl.database.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ds.etl.database.db.entity.DbBasic;
import com.ds.etl.database.db.vo.DbBasicVo;
import com.ds.etl.database.db.vo.DbCategoryVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * (DbBasic)表服务接口
 *
 * @author mayc
 * @since 2025-05-20 23:34:43
 */
public interface DbBasicService extends IService<DbBasic> {

    /**
     * 获取数据库类型集合
     *
     * @return
     */
    List<DbCategoryVo> getDbCategoryList();

    /**
     * 获取数据库基础信息集合
     *
     * @param categoryId
     * @return
     */
    List<DbBasicVo> getDbBasicList(Integer categoryId);

}

