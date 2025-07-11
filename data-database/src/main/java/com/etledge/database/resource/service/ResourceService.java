package com.etledge.database.resource.service;

import com.etledge.database.db.entity.DbGroup;
import com.etledge.database.db.vo.TreeVo;

import java.util.List;

/**
 * 资源管理 Service
 */
public interface ResourceService {

    /**
     * 树形结构
     *
     * @param name
     * @return
     */
    List<TreeVo> tree(String name);
}
