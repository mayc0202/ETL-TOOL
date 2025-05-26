package com.ds.etl.database.db.dao;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ds.etl.database.db.vo.DbBasicVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.ds.etl.database.db.entity.DbBasic;

/**
 * (DbBasic)表数据库访问层
 *
 * @author mayc
 * @since 2025-05-20 23:34:42
 */
@Mapper
public interface DbBasicDao extends BaseMapper<DbBasic> {

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<DbBasic> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<DbBasic> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<DbBasic> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<DbBasic> entities);

    /**
     * 获取数据库基础信息集合
     *
     * @param categoryId
     * @return
     */
    List<DbBasicVo> selectDbBasicList(@Param("category_id") Integer categoryId);
}

