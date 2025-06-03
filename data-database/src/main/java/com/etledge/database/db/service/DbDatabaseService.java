package com.etledge.database.db.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etledge.database.db.entity.DbDatabase;
import com.etledge.database.db.form.DbDatabaseForm;
import com.etledge.database.db.vo.DatabaseVo;

/**
 * (DbDatabase) Service
 *
 * @author mayc
 * @since 2025-06-03 21:22:28
 */
public interface DbDatabaseService extends IService<DbDatabase> {

    /**
     * Get database list
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
     * Add database
     *
     * @param form
     */
    void add(DbDatabaseForm form);

    /**
     * Update database
     *
     * @param form
     */
    void update(DbDatabaseForm form);

    /**
     * Delete database
     * @param id
     */
    void delete(Integer id);
}

