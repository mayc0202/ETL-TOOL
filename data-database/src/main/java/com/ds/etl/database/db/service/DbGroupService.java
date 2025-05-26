package com.ds.etl.database.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ds.etl.database.db.entity.DbGroup;
import com.ds.etl.database.db.form.DbGroupForm;

/**
 * (DbGroup) Service
 *
 * @author makejava
 * @since 2025-05-22 00:00:40
 */
public interface DbGroupService extends IService<DbGroup> {

    /**
     * Add group
     *
     * @param form
     */
    void add(DbGroupForm form);

    /**
     * Edit group
     *
     * @param form
     */
    void update(DbGroupForm form);

    /**
     * Delete group
     *
     * @param id
     */
    void delete(Integer id);
}

