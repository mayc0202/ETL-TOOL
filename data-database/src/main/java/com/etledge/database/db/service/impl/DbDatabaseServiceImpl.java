package com.etledge.database.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etledge.api.upms.UpmsServer;
import com.etledge.api.upms.vo.UserVo;
import com.etledge.common.Constants;
import com.etledge.common.utils.DataUtil;
import com.etledge.database.config.exception.ETLException;
import com.etledge.database.db.dao.DbDatabaseDao;
import com.etledge.database.db.dao.DbGroupDao;
import com.etledge.database.db.entity.DbDatabase;
import com.etledge.database.db.entity.DbGroup;
import com.etledge.database.db.form.DbDatabaseForm;
import com.etledge.database.db.service.DbDatabaseService;
import com.etledge.database.db.vo.DatabaseVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (DbDatabase) ServiceImpl
 *
 * @author mayc
 * @since 2025-06-03 21:22:29
 */
@Service
public class DbDatabaseServiceImpl extends ServiceImpl<DbDatabaseDao, DbDatabase> implements DbDatabaseService {

    @DubboReference(version = "1.0", loadbalance = "roundrobin", cluster = "broadcast", retries = 2)
    private UpmsServer upmsServer;

    @Autowired
    private DbGroupDao dbGroupDao;

    @Autowired
    private DbDatabaseDao dbDatabaseDao;

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
    @Override
    public IPage<DatabaseVo> list(Integer groupId, Integer dbId, String name, Integer pageNo, Integer pageSize) {

        // 校验token
        UserVo userInfo = getUserInfo();

        // 查询出所有分组
        LambdaQueryWrapper<DbGroup> groupLdq = new LambdaQueryWrapper<>();
        groupLdq.eq(DbGroup::getDeleted, Constants.DELETE_FLAG.FALSE);
        List<DbGroup> dbGroups = dbGroupDao.selectList(groupLdq);
        Map<Integer, String> groupMap = dbGroups.stream().collect(Collectors.toMap(DbGroup::getId, DbGroup::getName));

        LambdaQueryWrapper<DbDatabase> ldq = new LambdaQueryWrapper<>();
        ldq.eq(DbDatabase::getDeleted, Constants.DELETE_FLAG.FALSE);

        if (Objects.nonNull(groupId)) {
            ldq.eq(DbDatabase::getGroupId, groupId);
        }

        if (Objects.nonNull(dbId)) {
            ldq.eq(DbDatabase::getId, dbId);
        }

        if (StringUtils.isNotBlank(name)) {
            ldq.like(DbDatabase::getName, name);
        }

        Page<DbDatabase> page = dbDatabaseDao.selectPage(new Page<>(pageNo, pageSize), ldq);

        List<DatabaseVo> databaseList = new ArrayList<>();
        page.getRecords().forEach(d -> {
            DatabaseVo vo = new DatabaseVo();
            vo.setDbId(d.getId());
            vo.setName(d.getName());
            vo.setDbName(d.getDbName());
            vo.setDbType(d.getType());
            vo.setGroupId(d.getGroupId());
            vo.setGroupName(groupMap.getOrDefault(vo.getGroupId(),""));
            vo.setCreatedTimeTxt(DataUtil.dateConvertString(d.getCreatedTime()));
            databaseList.add(vo);
        });

        Page<DatabaseVo> result = new Page<>();
        result.setRecords(databaseList);
        result.setTotal(page.getTotal());
        result.setSize(page.getSize());
        result.setCurrent(page.getCurrent());

        return result;
    }

    /**
     * Add database
     *
     * @param form
     */
    @Override
    public void add(DbDatabaseForm form) {

        UserVo userInfo = getUserInfo();

        // verify data source already exists
        verifyDataSourceExist(form.getName());

        // TODO 密码入库加密

        DbDatabase dbDatabase = new DbDatabase();
        BeanUtils.copyProperties(form,dbDatabase);
        dbDatabase.setCreatedBy(userInfo.getAccount());
        dbDatabase.setCreatedTime(new Date());
        dbDatabaseDao.insert(dbDatabase);
    }

    /**
     * Update database
     *
     * @param form
     */
    @Override
    public void update(DbDatabaseForm form) {

        UserVo userInfo = getUserInfo();

        LambdaQueryWrapper<DbDatabase> ldq1 = new LambdaQueryWrapper<>();
        ldq1.eq(DbDatabase::getDeleted, Constants.DELETE_FLAG.FALSE)
                .eq(DbDatabase::getId, form.getId());

        DbDatabase dbDatabase = dbDatabaseDao.selectOne(ldq1);
        if (Objects.isNull(dbDatabase)) {
            throw new ETLException("Please verify if the data source exists!");
        }

        // verify data source already exists
        verifyDataSourceExist(form.getName());

        BeanUtils.copyProperties(form, dbDatabase);
        dbDatabase.setUpdatedBy(userInfo.getAccount());
        dbDatabase.setUpdatedTime(new Date());
        dbDatabaseDao.updateById(dbDatabase);
    }

    /**
     * Delete database
     *
     * @param id
     */
    @Override
    public void delete(Integer id) {

        UserVo userInfo = getUserInfo();

        LambdaQueryWrapper<DbDatabase> ldq = new LambdaQueryWrapper<>();
        ldq.eq(DbDatabase::getDeleted, Constants.DELETE_FLAG.FALSE)
                .eq(DbDatabase::getId, id);

        DbDatabase dbDatabase = dbDatabaseDao.selectOne(ldq);
        if (Objects.isNull(dbDatabase)) {
            throw new ETLException("Please verify if the data source exists!");
        }

        dbDatabase.setDeleted(Constants.DELETE_FLAG.TRUE);
        dbDatabase.setUpdatedBy(userInfo.getAccount());
        dbDatabase.setUpdatedTime(new Date());
        dbDatabaseDao.updateById(dbDatabase);
    }

    /**
     *  Verify data source already exists
     *
     * @param name
     */
    private void verifyDataSourceExist(String name) {
        LambdaQueryWrapper<DbDatabase> ldq2 = new LambdaQueryWrapper<>();
        ldq2.eq(DbDatabase::getDeleted, Constants.DELETE_FLAG.FALSE)
                .eq(DbDatabase::getDbName, name);
        DbDatabase database = dbDatabaseDao.selectOne(ldq2);
        if (Objects.nonNull(database)) {
            throw new ETLException("The data source already exists!");
        }
    }

    /**
     * Get user information
     *
     * @return
     */
    private UserVo getUserInfo() {

        // 获取token
        String token = getToken();
        UserVo userInfo = null;
        try {
            userInfo = upmsServer.getUserInfo(token);
        } catch (ETLException e) {
            e.printStackTrace();
            throw new ETLException(e.getMessage());
        }

        if (Objects.isNull(userInfo)) {
            throw new ETLException("Please verify if the user is empty!");
        }

        return userInfo;
    }

    /**
     * Get token
     *
     * @return
     */
    private String getToken() {

        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        Cookie[] cookies = request.getCookies();
        String token = "";
        for (Cookie cookie : cookies) {
            if (Constants.ETL_EDGE_TOKEN_CONFIG.ETL_EDGE_TOKEN.equals(cookie.getName())) {
                token = cookie.getValue().replaceAll(Constants.ETL_EDGE_TOKEN_CONFIG.BEARER_PREFIX, "");
                break;
            }
        }

        if (StringUtils.isBlank(token)) {
            throw new ETLException("Please verify if the token is empty!");
        }

        return token;
    }
}

