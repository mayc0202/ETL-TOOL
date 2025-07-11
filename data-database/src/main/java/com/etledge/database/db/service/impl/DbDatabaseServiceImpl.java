package com.etledge.database.db.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etledge.api.upms.UpmsServer;
import com.etledge.api.upms.vo.ApiUserVo;
import com.etledge.common.Constants;
import com.etledge.common.utils.AESUtil;
import com.etledge.common.utils.DataUtil;
import com.etledge.common.utils.RSAUtil;
import com.etledge.database.db.connector.ftp.AbstractFTPConnector;
import com.etledge.database.db.connector.ftp.FTPConnectorFactory;
import com.etledge.database.db.connector.relationdb.DatabaseConnectorFactory;
import com.etledge.database.db.connector.relationdb.entity.ConResponse;
import com.etledge.database.dict.service.DictService;
import com.etledge.database.config.exception.ETLException;
import com.etledge.database.db.dao.DbDatabaseDao;
import com.etledge.database.db.dao.DbGroupDao;
import com.etledge.database.db.entity.DbDatabase;
import com.etledge.database.db.entity.DbGroup;
import com.etledge.database.db.form.DbDatabaseForm;
import com.etledge.database.db.service.DbDatabaseService;
import com.etledge.database.db.vo.DatabaseVo;
import io.swagger.models.auth.In;
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

        // 校验token
        upmsServer.parseToken(getToken());

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
            vo.setType(record.getType());
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

        ApiUserVo userInfo = getUserInfo();

        //TODO FTP等数据源不需要数据库名称

        // 校验当前分类下数据源是否存在
        verifyDbNameExisted(form.getName(), form.getGroupId());

        // 密码入库加密
        String decrypt = RSAUtil.decryptByFixedPrivateKeytoString(form.getPassword());
        String pwd = AESUtil.encrypt(decrypt, AESUtil.getAesKey());

        DbDatabase dbDatabase = new DbDatabase();
        BeanUtils.copyProperties(form, dbDatabase, "password");
        dbDatabase.setPassword(pwd);
        dbDatabase.setCreatedBy(userInfo.getAccount());
        dbDatabase.setCreatedTime(new Date());

        // 处理数据源参数
        JSONObject properties = new JSONObject();
        handleProperties(form, properties);
        dbDatabase.setProperties(properties.toJSONString());

        // 处理扩展配置
        JSONObject extConfig = new JSONObject();
        handExtConfig(form,extConfig);
        dbDatabase.setExtConfig(extConfig.toJSONString());

        dbDatabaseDao.insert(dbDatabase);
    }

    /**
     * 修改数据源
     *
     * @param form
     */
    @Override
    public void update(DbDatabaseForm form) {

        if (Objects.isNull(form.getId())) {
            throw new ETLException("请校验数据源id是否为空!");
        }

        ApiUserVo userInfo = getUserInfo();

        DbDatabase dbDatabase = getDbDatabaseById(form.getId());
        if (Objects.isNull(dbDatabase)) {
            throw new ETLException("请校验数据源是否存在!");
        }

        //TODO FTP等数据源不需要数据库名称

        // 校验数据源是否已存在
        verifyDbNameExisted(form.getName(), form.getGroupId());

        // 密码入库加密
        String decrypt = RSAUtil.decryptByFixedPrivateKeytoString(form.getPassword());
        String pwd = AESUtil.encrypt(decrypt, AESUtil.getAesKey());

        BeanUtils.copyProperties(form, dbDatabase, "password");
        dbDatabase.setPassword(pwd);
        dbDatabase.setUpdatedBy(userInfo.getAccount());
        dbDatabase.setUpdatedTime(new Date());

        // 处理数据源参数
        JSONObject properties = new JSONObject();
        handleProperties(form, properties);
        dbDatabase.setProperties(properties.toJSONString());

        // 处理扩展配置
        JSONObject extConfig = new JSONObject();
        handExtConfig(form,extConfig);
        dbDatabase.setExtConfig(extConfig.toJSONString());

        dbDatabaseDao.updateById(dbDatabase);
    }

    /**
     * 删除数据源
     *
     * @param id
     */
    @Override
    public void delete(Integer id) {

        if (Objects.isNull(id)) {
            throw new ETLException("请校验数据源id是否为空!");
        }

        ApiUserVo userInfo = getUserInfo();

        DbDatabase dbDatabase = getDbDatabaseById(id);

        dbDatabase.setDeleted(Constants.DELETE_FLAG.TRUE);
        dbDatabase.setUpdatedBy(userInfo.getAccount());
        dbDatabase.setUpdatedTime(new Date());
        dbDatabaseDao.updateById(dbDatabase);
    }

    /**
     * 获取数据源详情
     *
     * @param id
     * @return
     */
    @Override
    public DatabaseVo detail(Integer id) {
        if (Objects.isNull(id)) {
            throw new ETLException("请校验数据源id是否为空!");
        }

        DbDatabase dbDatabase = getDbDatabaseById(id);
        DatabaseVo databaseVo = new DatabaseVo();
        BeanUtils.copyProperties(dbDatabase, databaseVo, "password");

        // AES解密再RSA加密
        try {
            String decrypt = AESUtil.decrypt(dbDatabase.getPassword(), AESUtil.getAesKey());
            String pwd = RSAUtil.encryptByFixedPublicKeytoString(decrypt);
            databaseVo.setPassword(pwd);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ETLException(String.format("数据源[%s]密码解密失败!", dbDatabase.getDbName()));
        }

        return databaseVo;
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
                connectDatabase(form, null);
                break;
            case Constants.FTP_TYPE.FTP:
            case Constants.FTP_TYPE.FTPS:
                connectFTP(form);
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
     * 连接数据库
     *
     * @param form
     */
    public void connectDatabase(DbDatabaseForm form, String properties) {

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
            // 解密
            String decrypt = RSAUtil.decryptByFixedPrivateKeytoString(form.getPassword());
            connector.setPassword(decrypt);
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

        // 处理参数
        JSONObject param = new JSONObject();
        handleProperties(form, param);

        //获取数据库中已保存的properties
        if (StringUtils.isNotBlank(properties)) {
            connector.setProperties(properties);
        } else {
            connector.setProperties(param.toJSONString());
        }

        JSONObject extConfig = new JSONObject();
        handExtConfig(form, extConfig);
        connector.setExtConfig(extConfig.toJSONString());

        connector.setType(form.getType());

        connector.build();
        ConResponse response = connector.connect(form.getType());

        // 判断ConResponse的标识，如果为false，则抛出异常
        if (!response.getResult()) {
            throw new ETLException(response.getMsg());
        }
    }

    /**
     * 处理参数
     *
     * @param form
     * @param properties
     */
    private void handleProperties(DbDatabaseForm form, JSONObject properties) {

        if (StringUtils.isNotBlank(form.getMode())) {
            properties.put("mode", form.getMode());
        }

        if (StringUtils.isNotBlank(form.getControlEncoding())) {
            properties.put("controlEncoding", form.getControlEncoding());
        }

//        String properties = form.getProperties();

//        String extConfig = form.getExtConfig();

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

    /**
     * 处理扩展配置
     *
     * @param form
     * @param extConfig
     */
    private void handExtConfig(DbDatabaseForm form, JSONObject extConfig) {
        if (StringUtils.isNotBlank(form.getFeAddress())) {
            extConfig.put("feAddress", form.getFeAddress());
        }

        if (StringUtils.isNotBlank(form.getBeAddress())) {
            extConfig.put("beAddress", form.getBeAddress());
        }
    }

    /**
     * 连接FTP/FTPS
     *
     * @param form
     */
    private void connectFTP(DbDatabaseForm form) {
        AbstractFTPConnector connector = FTPConnectorFactory.getConnector(form.getType());
        connector.setHost(form.getDbHost());
        connector.setPort(Integer.parseInt(form.getDbPort()));
        connector.setUsername(form.getUsername());
        connector.setPassword(form.getPassword());
        connector.setMode(form.getMode());
        connector.setControlEncoding(form.getControlEncoding());
        connector.connect();
    }

    /**
     * 校验数据源名称是否存在
     *
     * @param name
     */
    private void verifyDbNameExisted(String name, Integer groupId) {
        LambdaQueryWrapper<DbDatabase> ldq = new LambdaQueryWrapper<>();
        ldq.eq(DbDatabase::getDbName, name)
                .eq(DbDatabase::getGroupId, groupId)
                .eq(DbDatabase::getDeleted, Constants.DELETE_FLAG.FALSE);
        DbDatabase database = dbDatabaseDao.selectOne(ldq);
        if (Objects.nonNull(database)) {
            throw new ETLException("数据源已存在!");
        }
    }

    /**
     * 根据id获取数据源信息
     *
     * @param id
     * @return
     */
    private DbDatabase getDbDatabaseById(Integer id) {

        LambdaQueryWrapper<DbDatabase> ldq = new LambdaQueryWrapper<>();
        ldq.eq(DbDatabase::getId, id)
                .eq(DbDatabase::getDeleted, Constants.DELETE_FLAG.FALSE);

        DbDatabase database = dbDatabaseDao.selectOne(ldq);
        if (Objects.isNull(database)) {
            throw new ETLException("请校验数据源是否已存在!");
        }

        return database;
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

