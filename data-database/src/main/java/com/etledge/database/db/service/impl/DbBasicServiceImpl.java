package com.etledge.database.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * (DbBasic)表服务实现类
 *
 * @author mayc
 * @since 2025-05-20 23:34:43
 */
@Service("dbBasicService")
public class DbBasicServiceImpl extends ServiceImpl<DbBasicDao, DbBasic> implements DbBasicService {

    private static Logger logger = LoggerFactory.getLogger(DbBasicServiceImpl.class);

    @Autowired
    private DbCategoryDao dbCategoryDao;

    @Autowired
    private DbBasicDao dbBasicDao;

    /**
     * query database category list
     *
     * @return
     */
    public List<DbCategoryVo> getDbCategoryList() {
        List<DbCategoryVo> list = new ArrayList<>();

        LambdaQueryWrapper<DbCategory> ldq = new LambdaQueryWrapper();
        List<DbCategory> categories = dbCategoryDao.selectList(ldq);
        if (categories.isEmpty()) {
            return list;
        }

        categories.forEach(basic -> {
            DbCategoryVo vo = new DbCategoryVo();
            vo.setId(basic.getId());
            vo.setImg(basic.getImg());
            vo.setName(basic.getName());
            vo.setOrderBy(basic.getOrderBy());
            vo.setCreatedBy(basic.getCreatedBy());
            vo.setCreatedTime(basic.getCreatedTime());
            list.add(vo);
        });

        return list;
    }

    /**
     * query database basic list
     *
     * @return
     */
    @Override
    public List<DbBasicVo> getDbBasicList() {
        return dbBasicDao.selectDbBasicList();
    }

    /**
     *  query database basic list by category id
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<DbBasicVo> getDbBasicListByCategoryId(Integer categoryId) {
        return dbBasicDao.getDbBasicListByCategoryId(categoryId);
    }
}

