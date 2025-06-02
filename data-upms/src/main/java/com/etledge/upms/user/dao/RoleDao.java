package com.etledge.upms.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etledge.upms.user.entity.Role;
import com.etledge.upms.user.vo.RoleVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (Role)表数据库访问层
 *
 * @author mayc
 * @since 2025-05-23 00:24:45
 */
@Mapper
public interface RoleDao extends BaseMapper<Role> {

    /**
     * 根据用户id获取角色列表
     * @param userId
     * @return
     */
    List<RoleVo> getRoleListByUserId(@Param("userId")Integer userId);
}
