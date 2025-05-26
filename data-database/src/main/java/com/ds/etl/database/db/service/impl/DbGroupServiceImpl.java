package com.ds.etl.database.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ds.etl.common.Constants;
import com.ds.etl.database.config.exception.ETLException;
import com.ds.etl.database.db.dao.DbGroupDao;
import com.ds.etl.database.db.entity.DbGroup;
import com.ds.etl.database.db.form.DbGroupForm;
import com.ds.etl.database.db.service.DbGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

/**
 * (DbGroup) ServiceImpl
 *
 * @author mayc
 * @since 2025-05-22 00:00:40
 */
@Service("dbGroupService")
public class DbGroupServiceImpl extends ServiceImpl<DbGroupDao, DbGroup> implements DbGroupService {

    @Autowired
    private DbGroupDao dbGroupDao;

    /**
     * Add group
     *
     * @param form
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DbGroupForm form) {

        LambdaQueryWrapper<DbGroup> ldq = new LambdaQueryWrapper<>();
        ldq.eq(DbGroup::getName, form.getName())
                .eq(DbGroup::getDeleted, Constants.DELETE_FLAG.FALSE);
        DbGroup dbGroup = dbGroupDao.selectOne(ldq);
        if (Objects.nonNull(dbGroup)) {
            throw new ETLException(String.format("Group name [%s] already exists!", form.getName()));
        }

        DbGroup group = new DbGroup();
        group.setName(form.getName());
        group.setOrderBy(form.getOrderBy());
        group.setDeleted(Constants.DELETE_FLAG.FALSE);
        group.setCreatedBy("system");
        group.setCreatedTime(new Date());
        dbGroupDao.insert(group);
    }

    /**
     * Edit group
     *
     * @param form
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(DbGroupForm form) {

        if (Objects.isNull(form.getId())) {
            throw new ETLException("Group name [%s] already exists!");
        }

        LambdaQueryWrapper<DbGroup> ldq1 = new LambdaQueryWrapper<>();
        ldq1.eq(DbGroup::getId, form.getId())
                .eq(DbGroup::getDeleted, Constants.DELETE_FLAG.FALSE);
        DbGroup group = dbGroupDao.selectOne(ldq1);
        if (Objects.isNull(group)) {
            throw new ETLException("Please verify if the grouping exists!");
        }

        LambdaQueryWrapper<DbGroup> ldq2 = new LambdaQueryWrapper<>();
        ldq2.eq(DbGroup::getName, form.getName())
                .eq(DbGroup::getDeleted, Constants.DELETE_FLAG.FALSE);
        DbGroup dbGroup = dbGroupDao.selectOne(ldq2);
        if (Objects.nonNull(dbGroup)) {
            throw new ETLException(String.format("Group name [%s] already exists!", form.getName()));
        }

        group.setName(form.getName());
        group.setOrderBy(form.getOrderBy());
        group.setUpdatedBy("system");
        group.setUpdatedTime(new Date());
        dbGroupDao.updateById(group);
    }

    /**
     * Delete group
     *
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Integer id) {

        LambdaQueryWrapper<DbGroup> ldq = new LambdaQueryWrapper<>();
        ldq.eq(DbGroup::getId, id)
                .eq(DbGroup::getDeleted, Constants.DELETE_FLAG.FALSE);
        DbGroup group = dbGroupDao.selectOne(ldq);
        if (Objects.isNull(group)) {
            throw new ETLException("Please verify if the grouping exists!");
        }

        group.setDeleted(Constants.DELETE_FLAG.TRUE);
        group.setUpdatedBy("system");
        group.setUpdatedTime(new Date());
        dbGroupDao.updateById(group);
    }
}

