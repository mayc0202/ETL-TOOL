package com.etledge.database.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etledge.api.upms.UpmsServer;
import com.etledge.api.upms.vo.ApiUserVo;
import com.etledge.common.Constants;
import com.etledge.database.config.exception.ETLException;
import com.etledge.database.db.dao.DbBasicDao;
import com.etledge.database.db.dao.DbDatabaseDao;
import com.etledge.database.db.dao.DbGroupDao;
import com.etledge.database.db.entity.DbDatabase;
import com.etledge.database.db.entity.DbGroup;
import com.etledge.database.db.form.DbGroupForm;
import com.etledge.database.db.service.DbGroupService;
import com.etledge.database.db.vo.TreeVo;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * (DbGroup) ServiceImpl
 *
 * @author mayc
 * @since 2025-05-22 00:00:40
 */
@Service("dbGroupService")
public class DbGroupServiceImpl extends ServiceImpl<DbGroupDao, DbGroup> implements DbGroupService {

    @DubboReference(version = "1.0", loadbalance = "roundrobin", cluster = "broadcast", retries = 2)
    private UpmsServer upmsServer;

    @Autowired
    private DbBasicDao dbBasicDao;

    @Autowired
    private DbGroupDao dbGroupDao;

    @Autowired
    private DbDatabaseDao dbDatabaseDao;

    private static final Map<String, String> CACHE_ICON = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        dbBasicDao.selectDbBasicList().forEach(basic ->
                CACHE_ICON.put(basic.getName(), basic.getImg())
        );
    }

    /**
     * Tree
     *
     * @param name
     * @return
     */
    @Override
    public List<TreeVo> tree(String name) {

        // TODO 调用upms系统接口，解析cooike中的token，获取对应的角色权限，再做筛选
        ApiUserVo userInfo = getUserInfo();
        Set<String> roles = userInfo.getRoles();

        // 获取所有分组
        List<DbGroup> dbGroups = getAllDbGroup(name);
        if (dbGroups.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Integer> groupIds = dbGroups.stream()
                .map(DbGroup::getId)
                .collect(Collectors.toSet());

        List<DbDatabase> databaseList = dbDatabaseDao.selectList(
                new LambdaQueryWrapper<DbDatabase>()
                        .eq(DbDatabase::getDeleted, Constants.DELETE_FLAG.FALSE)
                        .in(DbDatabase::getGroupId, groupIds)
        );

        // key=groupId,value=List<DbDatabase>
        Map<Integer, List<DbDatabase>> groupDatabaseMap = databaseList.stream()
                .collect(Collectors.groupingBy(
                        DbDatabase::getGroupId,
                        Collectors.mapping(Function.identity(), Collectors.toList())
                ));


        return dbGroups.stream().map(group -> {
            TreeVo vo = new TreeVo();
            BeanUtils.copyProperties(group, vo);
            vo.setType(Constants.TREE_TYPE.GROUP);

            List<TreeVo> children = Optional.ofNullable(groupDatabaseMap.get(group.getId()))
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(db -> {
                        TreeVo child = new TreeVo();
                        child.setId(db.getId());
                        child.setIcon(CACHE_ICON.get(db.getType())); // deal icon
                        child.setName(db.getName());
                        child.setType(Constants.TREE_TYPE.DATABASE);
                        return child;
                    })
                    .collect(Collectors.toList());

            vo.setChildren(children);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * Add group
     *
     * @param form
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DbGroupForm form) {

        ApiUserVo userInfo = getUserInfo();

        LambdaQueryWrapper<DbGroup> ldq = new LambdaQueryWrapper<>();
        ldq.eq(DbGroup::getName, form.getName())
                .eq(DbGroup::getDeleted, Constants.DELETE_FLAG.FALSE);
        DbGroup dbGroup = dbGroupDao.selectOne(ldq);
        if (Objects.nonNull(dbGroup)) {
            throw new ETLException(String.format("分组名称【%s】已存在!", form.getName()));
        }

        DbGroup group = new DbGroup();
        group.setName(form.getName());
        group.setOrderBy(form.getOrderBy());
        group.setDeleted(Constants.DELETE_FLAG.FALSE);
        group.setCreatedBy(userInfo.getAccount());
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

        ApiUserVo userInfo = getUserInfo();

        LambdaQueryWrapper<DbGroup> ldq1 = new LambdaQueryWrapper<>();
        ldq1.eq(DbGroup::getId, form.getId())
                .eq(DbGroup::getDeleted, Constants.DELETE_FLAG.FALSE);
        DbGroup group = dbGroupDao.selectOne(ldq1);
        if (Objects.isNull(group)) {
            throw new ETLException("请校验分组是否存在!");
        }

        // 校验分组名称是否已存在
        verifyGroupIsExists(form.getName());

        group.setName(form.getName());
        group.setOrderBy(form.getOrderBy());
        group.setUpdatedBy(userInfo.getAccount());
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

        ApiUserVo userInfo = getUserInfo();

        LambdaQueryWrapper<DbGroup> groupQueryWrapper = new LambdaQueryWrapper<>();
        groupQueryWrapper.eq(DbGroup::getId, id)
                .eq(DbGroup::getDeleted, Constants.DELETE_FLAG.FALSE);
        DbGroup group = dbGroupDao.selectOne(groupQueryWrapper);
        if (Objects.isNull(group)) {
            throw new ETLException("请校验分组是否存在!");
        }

        // 校验分组下存在数据源不能删除
        verifyGroupHasDatabase(id);

        group.setDeleted(Constants.DELETE_FLAG.TRUE);
        group.setUpdatedBy(userInfo.getAccount());
        group.setUpdatedTime(new Date());
        dbGroupDao.updateById(group);
    }

    /**
     * 校验分组下是否存在数据源
     *
     * @param groupId
     */
    private void verifyGroupHasDatabase(Integer groupId) {
        LambdaQueryWrapper<DbDatabase> ldq = new LambdaQueryWrapper<>();
        ldq.eq(DbDatabase::getGroupId, groupId)
                .eq(DbDatabase::getDeleted, Constants.DELETE_FLAG.FALSE);

        List<DbDatabase> list = dbDatabaseDao.selectList(ldq);
        if (!list.isEmpty()) {
            throw new ETLException("当前分组下存在数据源!");
        }
    }

    /**
     * 校验分组是否存在
     *
     * @param name
     */
    private void verifyGroupIsExists(String name) {
        LambdaQueryWrapper<DbGroup> ldq = new LambdaQueryWrapper<>();
        ldq.eq(DbGroup::getName, name)
                .eq(DbGroup::getDeleted, Constants.DELETE_FLAG.FALSE);

        DbGroup group = dbGroupDao.selectOne(ldq);
        if (Objects.nonNull(group)) {
            throw new ETLException(String.format("分组名称【%s】已存在!", name));
        }
    }

    /**
     * 获取所有数据源分组
     *
     * @param name
     * @return
     */
    private List<DbGroup> getAllDbGroup(String name) {

        LambdaQueryWrapper<DbGroup> ldq = new LambdaQueryWrapper<>();
        ldq.eq(DbGroup::getDeleted, Constants.DELETE_FLAG.FALSE)
                .orderByAsc(DbGroup::getOrderBy);

        if (StringUtils.isNotBlank(name)) {
            ldq.like(DbGroup::getName, name);
        }

        return dbGroupDao.selectList(ldq);
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    private ApiUserVo getUserInfo() {

        // 获取token
        String token = getToken();
        ApiUserVo userInfo = null;
        try {
            userInfo = upmsServer.getUserInfo(token);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ETLException(e.getMessage());
        }

        return userInfo;
    }


    /**
     * 获取token
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
            throw new ETLException("请校验token是否为空!");
        }

        return token;
    }
}

