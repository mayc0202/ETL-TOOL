package com.etledge.database.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etledge.api.upms.UpmsServer;
import com.etledge.api.upms.vo.UserVo;
import com.etledge.common.Constants;
import com.etledge.database.config.exception.ETLException;
import com.etledge.database.db.dao.DbGroupDao;
import com.etledge.database.db.entity.DbGroup;
import com.etledge.database.db.form.DbGroupForm;
import com.etledge.database.db.service.DbGroupService;
import com.etledge.database.db.vo.TreeVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * (DbGroup) ServiceImpl
 *
 * @author mayc
 * @since 2025-05-22 00:00:40
 */
@Service("dbGroupService")
public class DbGroupServiceImpl extends ServiceImpl<DbGroupDao, DbGroup> implements DbGroupService {

    @DubboReference(version = "1.0",loadbalance = "roundrobin",cluster = "broadcast", retries = 2)
    private UpmsServer upmsServer;

    @Autowired
    private DbGroupDao dbGroupDao;

    /**
     * List
     * @param name
     * @return
     */
    @Override
    public List<TreeVo> list(String name) {

        List<TreeVo> result = new ArrayList<>();

        LambdaQueryWrapper<DbGroup> ldq = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(name)) {
            ldq.like(DbGroup::getName,name);
        }
        ldq.eq(DbGroup::getDeleted,Constants.DELETE_FLAG.FALSE);
        ldq.orderByDesc(DbGroup::getOrderBy);

        // TODO 调用upms系统接口，解析cooike中的token，获取对应的角色权限，再做筛选

        List<DbGroup> dbGroups = dbGroupDao.selectList(ldq);
        if (dbGroups.isEmpty()) {
            return result;
        }

        dbGroups.forEach(g -> {
            TreeVo vo = new TreeVo();
            BeanUtils.copyProperties(g,vo);
            vo.setType("");
            result.add(vo);
        });

        return result;
    }

    /**
     * Add group
     *
     * @param form
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(DbGroupForm form) {

        // 获取用户信息
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
                token = cookie.getValue().replaceAll(Constants.ETL_EDGE_TOKEN_CONFIG.BEARER_PREFIX,"");
                break;
            }
        }

        if (StringUtils.isBlank(token)) {
            throw new ETLException("Please verify if the token is empty!");
        }

        return token;
    }
}

