package com.etledge.database.db.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etledge.common.Constants;
import com.etledge.database.db.dao.DbBasicDao;
import com.etledge.database.db.dao.DbCategoryDao;
import com.etledge.database.db.entity.DbBasic;
import com.etledge.database.db.entity.DbCategory;
import com.etledge.database.db.service.DbBasicService;
import com.etledge.database.db.vo.DbBasicVo;
import com.etledge.database.db.vo.DbCategoryVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * (DbBasic)表服务实现类
 *
 * @author mayc
 * @since 2025-05-20 23:34:43
 */
@Service("dbBasicService")
public class DbBasicServiceImpl extends ServiceImpl<DbBasicDao, DbBasic> implements DbBasicService {

    private static final Logger logger = LoggerFactory.getLogger(DbBasicServiceImpl.class);

    @Autowired
    private DbCategoryDao dbCategoryDao;

    @Autowired
    private DbBasicDao dbBasicDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostConstruct
    private void init() {

        try {
            redisTemplate.opsForValue().set(
                    Constants.REDIS_KEY.CATEGORY,
                    convertToCategoryVoList(dbCategoryDao.selectList(null)),
                    1, TimeUnit.DAYS
            );

            redisTemplate.opsForValue().set(
                    Constants.REDIS_KEY.DB_BASIC,
                    dbBasicDao.selectDbBasicList(),
                    1, TimeUnit.DAYS
            );
        } catch (Exception e) {
            logger.error("数据库基础信息初始化失败", e);
        }
    }

    /**
     * 转换特殊分类
     *
     * @param categories
     * @return
     */
    private List<DbCategoryVo> convertToCategoryVoList(List<DbCategory> categories) {
        return categories.stream().map(category -> {
            DbCategoryVo vo = new DbCategoryVo();
            vo.setId(category.getId());
            vo.setImg(category.getImg());
            vo.setName(category.getName());
            vo.setOrderBy(category.getOrderBy());
            vo.setCreatedBy(category.getCreatedBy());
            vo.setCreatedTime(category.getCreatedTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 从缓存中获取集合
     *
     * @param key
     * @param loader
     * @param <T>
     * @return
     */
    private <T> List<T> getCachedList(String key, Supplier<List<T>> loader) {
        try {
            List<T> cache = (List<T>) redisTemplate.opsForValue().get(key);
            if (cache != null) {
                logger.debug("Cache hit for key: {}", key);
                return cache;
            }
        } catch (Exception e) {
            logger.warn("Cache read error for key: {}", key, e);
        }

        logger.debug("Cache miss for key: {}", key);
        List<T> data = loader.get();
        if (!data.isEmpty()) {
            redisTemplate.opsForValue().set(key, data, 1, TimeUnit.DAYS);
        }
        return data;
    }

    /**
     * 获取数据库特殊分类集合
     *
     * @return
     */
    @Override
    public List<DbCategoryVo> getDbCategoryList() {
        return getCachedList(Constants.REDIS_KEY.CATEGORY,
                () -> convertToCategoryVoList(dbCategoryDao.selectList(null)));
    }

    /**
     * 获取数据库基础信息
     *
     * @return
     */
    @Override
    public List<DbBasicVo> getDbBasicList() {
        return getCachedList(Constants.REDIS_KEY.DB_BASIC,
                dbBasicDao::selectDbBasicList);
    }


    /**
     * query database basic list by category id
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<DbBasicVo> getDbBasicListByCategoryId(Integer categoryId) {
        return dbBasicDao.getDbBasicListByCategoryId(categoryId);
    }

}

