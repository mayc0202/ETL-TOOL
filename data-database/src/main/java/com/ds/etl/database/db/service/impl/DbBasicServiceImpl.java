package com.ds.etl.database.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ds.etl.database.db.dao.DbBasicDao;
import com.ds.etl.database.db.dao.DbCategoryDao;
import com.ds.etl.database.db.entity.DbBasic;
import com.ds.etl.database.db.entity.DbCategory;
import com.ds.etl.database.db.service.DbBasicService;
import com.ds.etl.database.db.vo.DbBasicVo;
import com.ds.etl.database.db.vo.DbCategoryVo;
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
     * 获取数据库类型集合
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
     * 获取数据库基础信息集合
     *
     * @param categoryId
     * @return
     */
    public List<DbBasicVo> getDbBasicList(Integer categoryId) {
        return dbBasicDao.selectDbBasicList(categoryId);
    }
}

