package com.etledge.database.db.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etledge.database.dict.vo.DictItem;
import com.etledge.database.db.entity.DbDatabase;
import com.etledge.database.db.form.DbDatabaseForm;
import com.etledge.database.db.vo.DatabaseVo;

import java.util.List;

/**
 * (DbDatabase) Service
 *
 * @author mayc
 * @since 2025-06-03 21:22:28
 */
public interface DbDatabaseService extends IService<DbDatabase> {

    /**
     * 获取数据源集合
     *
     * @param groupId
     * @param dbId
     * @param name
     * @param pageNo
     * @param pageSize
     * @return
     */
    IPage<DatabaseVo> list(Integer groupId, Integer dbId, String name, Integer pageNo, Integer pageSize);

    /**
     * 接入数据源
     *
     * @param form
     */
    void add(DbDatabaseForm form);

    /**
     * 修改数据源
     *
     * @param form
     */
    void update(DbDatabaseForm form);

    /**
     * 删除数据源
     *
     * @param id
     */
    void delete(Integer id);

    /**
     * 测试连接
     *
     * @param form
     */
    void connect(DbDatabaseForm form);
}

