package com.etledge.database.db.dao;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etledge.database.db.vo.DbBasicVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.etledge.database.db.entity.DbBasic;

/**
 * (DbBasic)表数据库访问层
 *
 * @author mayc
 * @since 2025-05-20 23:34:42
 */
@Mapper
public interface DbBasicDao extends BaseMapper<DbBasic> {

    /**
     * query database basic list
     *
     * @return
     */
    List<DbBasicVo> selectDbBasicList();

    /**
     * query database basic list by category id
     *
     * @param categoryId
     * @return
     */
    List<DbBasicVo> getDbBasicListByCategoryId(@Param("categoryId") Integer categoryId);
}

