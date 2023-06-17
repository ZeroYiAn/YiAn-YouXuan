package com.zero.yianyx.acl.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.acl.Role;
import com.zero.yianyx.vo.acl.RoleQueryVo;

import java.util.Map;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/7
 */

public interface RoleService extends IService<Role> {
    /**
     * 角色列表(条件分页查询)
     * @param pageParam 分页参数
     * @param roleQueryVo 分页值对象
     * @return 分页对象
     */
    IPage<Role> selectRolePage(Page<Role> pageParam, RoleQueryVo roleQueryVo);

    /**
     * 查询所有角色 以及 根据用户id查询用户已分配的角色列表
     * 返回的map集合包含两部分数据：1.所有角色选项 2.用户已分配的角色列表
     * @param userId 用户id
     * @return 返回用户对应的角色列表map
     */
    Map<String, Object> getRoleByUserId(Long userId);

    /**
     * 为用户分配角色
     * @param userId  用户id
     * @param rolesId  角色id
     */
    void saveUserRoleRelationShip(Long userId, Long[] rolesId);
}
