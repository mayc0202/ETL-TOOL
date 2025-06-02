package com.etledge.database.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.etledge.database.db.entity.DbGroup;
import com.etledge.database.db.form.DbGroupForm;
import com.etledge.database.db.vo.TreeVo;

import java.util.List;

/**
 * (DbGroup) Service
 *
 * @author makejava
 * @since 2025-05-22 00:00:40
 */
public interface DbGroupService extends IService<DbGroup> {

    /**
     * List
     *
     * @param name
     * @return
     */
    List<TreeVo> list(String name);

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

