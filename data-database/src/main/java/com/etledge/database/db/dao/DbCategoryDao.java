package com.etledge.database.db.dao;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.etledge.database.db.entity.DbCategory;

/**
 * (DbCategory)表数据库访问层
 *
 * @author makejava
 * @since 2025-05-21 00:48:36
 */
@Mapper
public interface DbCategoryDao extends BaseMapper<DbCategory> {

}

