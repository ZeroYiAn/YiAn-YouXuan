package com.zero.yianyx.acl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.acl.Permission;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */

public interface PermissionService extends IService<Permission> {
    /**
     * @return 所有菜单列表
     */
    List<Permission>queryAllMenu();

    /**
     * 递归删除,删除菜单及其子菜单
     * @param id 菜单id
     * @return 删除结果
     */
    boolean removeChildById(Long id);

    /**
     * 查看某个角色的权限列表
     * @param roleId  角色id
     * @return
     */
   List<Permission> getPermissionByRoldId(Long roleId);

    void saveRolePermission(Long roleId, Long[] permissionId);
}
