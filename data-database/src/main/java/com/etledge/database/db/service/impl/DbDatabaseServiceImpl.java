package com.etledge.database.db.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etledge.api.upms.UpmsServer;
import com.etledge.api.upms.vo.UserVo;
import com.etledge.common.Constants;
import com.etledge.common.utils.AESUtil;
import com.etledge.common.utils.DataUtil;
import com.etledge.database.db.connector.relationdb.DatabaseConnectorFactory;
import com.etledge.database.db.connector.relationdb.entity.ConResponse;
import com.etledge.database.db.form.PropertiesForm;
import com.etledge.database.dict.service.DictService;
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
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.etledge.database.db.connector.relationdb.*;

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

    @Autowired
    private DictService dictService;

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
    @Override
    public IPage<DatabaseVo> list(Integer groupId, Integer dbId, String name, Integer pageNo, Integer pageSize) {

        // TODO 校验token
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
        for (DbDatabase record : page.getRecords()) {
            DatabaseVo vo = new DatabaseVo();
            vo.setId(record.getId());
            vo.setName(record.getName());
            vo.setDbName(record.getDbName());
            vo.setDbType(record.getType());
            vo.setGroupId(record.getGroupId());
            vo.setGroupName(groupMap.getOrDefault(vo.getGroupId(), ""));
            vo.setLabel(record.getLabel());
            vo.setLabelName(dictService.getDictItemCode(Constants.DICT.DATA_SOURCE_LAYERING, record.getLabel()));
            vo.setCreatedTimeTxt(DataUtil.dateConvertString(record.getCreatedTime()));
            databaseList.add(vo);
        }

        Page<DatabaseVo> result = new Page<>();
        result.setRecords(databaseList);
        result.setTotal(page.getTotal());
        result.setSize(page.getSize());
        result.setCurrent(page.getCurrent());

        return result;
    }

    /**
     * 添加数据源
     *
     * @param form
     */
    @Override
    public void add(DbDatabaseForm form) {

        UserVo userInfo = getUserInfo();

        // 校验数据源是否存在
        verifyDataSourceExist(form.getName());

        // TODO 密码入库加密

        DbDatabase dbDatabase = new DbDatabase();
        BeanUtils.copyProperties(form, dbDatabase);
        dbDatabase.setCreatedBy(userInfo.getAccount());
        dbDatabase.setCreatedTime(new Date());
        dbDatabaseDao.insert(dbDatabase);
    }

    /**
     * 修改数据源
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
            throw new ETLException("请校验数据源是否存在!");
        }

        // verify data source already exists
        verifyDataSourceExist(form.getName());

        BeanUtils.copyProperties(form, dbDatabase);
        dbDatabase.setUpdatedBy(userInfo.getAccount());
        dbDatabase.setUpdatedTime(new Date());
        dbDatabaseDao.updateById(dbDatabase);
    }

    /**
     * 删除数据源
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
            throw new ETLException("请校验数据源是否存在!");
        }

        dbDatabase.setDeleted(Constants.DELETE_FLAG.TRUE);
        dbDatabase.setUpdatedBy(userInfo.getAccount());
        dbDatabase.setUpdatedTime(new Date());
        dbDatabaseDao.updateById(dbDatabase);
    }

    /**
     * 测试连接
     *
     * @param form
     */
    @Override
    public void connect(DbDatabaseForm form) {
        switch (form.getType()) {
            case Constants.DATABASE_TYPE.MYSQL:
            case Constants.DATABASE_TYPE.ORACLE:
            case Constants.DATABASE_TYPE.POSTGRESQL:
            case Constants.DATABASE_TYPE.SQL_SERVER:
            case Constants.DATABASE_TYPE.DM8:
            case Constants.DATABASE_TYPE.DORIS:
            case Constants.DATABASE_TYPE.STAR_ROCKS:
                testDatabaseConnection(form, null);
                break;
            case Constants.DATABASE_TYPE.REDIS:
                break;
            case Constants.DATABASE_TYPE.MONGODB:
                break;
            case Constants.DATABASE_TYPE.KAFKA:
                break;
            default:
                throw new ETLException(String.format("未知的数据源类型:%s!", form.getType()));
        }
    }

    /**
     * 测试数据库连接
     *
     * @param form
     */
    public void testDatabaseConnection(DbDatabaseForm form, String properties) {

        // 单独校验schema
        if ((Constants.DATABASE_TYPE.ORACLE.equals(form.getType()) ||
                Constants.DATABASE_TYPE.DM8.equals(form.getType())) &&
                StringUtils.isBlank(form.getDbSchema())) {
            throw new ETLException("请校验模式(Schema)是否为空!");
        }

        // 获取连接器
        AbstractDatabaseConnector connector = DatabaseConnectorFactory.getConnector(form.getType());
        connector.setDbName(form.getDbName());
        connector.setUsername(form.getUsername());

        //判断密码是否为空
        if (StringUtils.isNotBlank(form.getPassword()) || null == form.getId()) {
            connector.setPassword(form.getPassword());
        } else {
            DbDatabase dbDatabase = dbDatabaseDao.selectById(form.getId());
            String decodePassword = AESUtil.decrypt(dbDatabase.getPassword(), AESUtil.getAesKey());
            connector.setPassword(decodePassword);
        }

        connector.setHost(form.getDbHost().trim());
        if (StringUtils.isNotBlank(form.getDbPort())) {
            connector.setPort(Integer.parseInt(form.getDbPort()));
        }
        connector.setSchema(form.getDbSchema());
        Map<String, Object> map = new HashMap<>();

        handleProperties(form, map);

        //获取数据库中已保存的properties
        if (StringUtils.isNotBlank(properties)) {
            connector.setProperties(properties);
        } else {
            connector.setProperties(JSONObject.toJSONString(map));
        }

        Map<String, Object> extConfigMap = new HashMap<>();
        setExtConfigProperties(form, extConfigMap);
        connector.setExtConfig(JSONObject.toJSONString(extConfigMap));
        connector.setType(form.getType());

        connector.build();
        ConResponse response = connector.test(form.getType());

        // 判断TestResponse的标识，如果为false，则抛出异常
        if (!response.getResult()) {
            throw new ETLException(response.getMsg());
        }
    }

    private void handleProperties(DbDatabaseForm form, Map<String, Object> map) {
//        if (null != form.getUseSSL()) {
//            map.put("useSSL", form.getUseSSL());
//        }
//        if (null != form.getUseCopy()) {
//            map.put("useCopy", form.getUseCopy());
//        }
//        if (StringUtils.isNotBlank(form.getAuthWay())) {
//            map.put("authWay", form.getAuthWay());
//        }
//        if (null != form.getConnectionTimeout()) {
//            map.put("connectionTimeout", form.getConnectionTimeout());
//        }
//        if (null != form.getReadTimeout()) {
//            map.put("readTimeout", form.getReadTimeout());
//        }
//        if (StringUtils.isNotBlank(form.getControlEncoding())) {
//            map.put("controlEncoding", form.getControlEncoding());
//        }
//        if (StringUtils.isNotBlank(form.getMode())) {
//            map.put("mode", form.getMode());
//        }
//        if (StringUtils.isNotBlank(form.getSecretId())) {
//            map.put("secretId", form.getSecretId());
//        }
//        if (StringUtils.isNotBlank(form.getSecretKey())) {
//            map.put("secretKey", form.getSecretKey());
//        }
//        if (StringUtils.isNotBlank(form.getOdpsEndpoint())) {
//            map.put("odpsEndpoint", form.getOdpsEndpoint());
//        }
//        if (StringUtils.isNotBlank(form.getTunnelEndpoint())) {
//            map.put("tunnelEndpoint", form.getTunnelEndpoint());
//        }
//        if (StringUtils.isNotBlank(form.getProject())) {
//            map.put("project", form.getProject());
//        }
//        if (StringUtils.isNotBlank(form.getAccessKeyId())) {
//            map.put("accessKeyId", form.getAccessKeyId());
//        }
//        if (StringUtils.isNotBlank(form.getAccessKeySecret())) {
//            map.put("accessKeySecret", form.getAccessKeySecret());
//        }
//        if (Objects.nonNull(form.getPropertiesList())) {
//            for (PropertiesForm propertiesForm : form.getPropertiesList()) {
//                map.put(propertiesForm.getName(), propertiesForm.getValue());
//            }
//        }
    }

    private void setExtConfigProperties(DbDatabaseForm form, Map<String, Object> map) {
//        if (null != form.getFeAddress()) {
//            map.put("feAddress", form.getFeAddress());
//        }
//        if (null != form.getBeAddress()) {
//            map.put("beAddress", form.getBeAddress());
//        }
//        if (null != form.getConnectType() && !"".equals(form.getConnectType())) {
//            map.put("connectType", form.getConnectType());
//        }
//        if (StringUtils.isNotBlank(form.getAccessKey())) {
//            map.put("accessKey", form.getAccessKey());
//        }
//        if (StringUtils.isNotBlank(form.getSecretKey())) {
//            map.put("secretKey", form.getSecretKey());
//        }
    }

    /**
     * 校验苏剧院是否存在
     *
     * @param name
     */
    private void verifyDataSourceExist(String name) {
        LambdaQueryWrapper<DbDatabase> ldq2 = new LambdaQueryWrapper<>();
        ldq2.eq(DbDatabase::getDeleted, Constants.DELETE_FLAG.FALSE)
                .eq(DbDatabase::getDbName, name);
        DbDatabase database = dbDatabaseDao.selectOne(ldq2);
        if (Objects.nonNull(database)) {
            throw new ETLException("数据源已存在!");
        }
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    private UserVo getUserInfo() {

        // 获取token
        String token = getToken();
        UserVo userInfo = null;
        try {
            userInfo = upmsServer.getUserInfo(token);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ETLException(e.getMessage());
        }

        if (Objects.isNull(userInfo)) {
            throw new ETLException("请校验用户信息是否为空!");
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

