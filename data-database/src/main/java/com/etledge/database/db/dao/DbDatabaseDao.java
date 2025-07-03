package com.etledge.database.db.dao;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.etledge.database.db.entity.DbDatabase;

/**
 * (DbDatabase)表数据库访问层
 *
 * @author mayc
 * @since 2025-06-03 21:22:26
 */
@Mapper
public interface DbDatabaseDao extends BaseMapper<DbDatabase> {

}

