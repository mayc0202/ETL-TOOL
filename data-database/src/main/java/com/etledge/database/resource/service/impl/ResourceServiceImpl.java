package com.etledge.database.resource.service.impl;

import com.etledge.database.db.vo.TreeVo;
import com.etledge.database.resource.service.ResourceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceServiceImpl implements ResourceService {


    /**
     * 数据源结构
     *
     * @param name
     * @return
     */
    @Override
    public List<TreeVo> tree(String name) {
        // TODO 查询FTP数据源
        return null;
    }
}
