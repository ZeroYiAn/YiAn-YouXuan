package com.zero.yianyx.acl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.acl.helper.PermissionHelper;
import com.zero.yianyx.acl.mapper.PermissionMapper;
import com.zero.yianyx.acl.service.PermissionService;
import com.zero.yianyx.acl.service.RolePermissionService;
import com.zero.yianyx.model.acl.Permission;
import com.zero.yianyx.model.acl.RolePermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
   @Resource
   private RolePermissionService rolePermissionService;



    @Override
    public List<Permission> queryAllMenu() {
        //获取所有权限数据
        List<Permission> allPermissionList = baseMapper.selectList(new QueryWrapper<Permission>().orderByAsc("CAST(id AS SIGNED)"));
        //把权限数据构建成树形结构数据
        List<Permission> result = PermissionHelper.bulid(allPermissionList);
        return result;

    }

    @Override
    public boolean removeChildById(Long id) {
        //2.递归获取当前菜单的所有子菜单,返回idList封装所有要删除的菜单id(包括菜单id及其子菜单id)
        List<Long> idList = new ArrayList<>();
        this.selectChildListById(id,idList);
        //把父菜单id加进去
        idList.add(id);
        //3.调用方法根据多个菜单id统一进行删除
        int isSuccess = baseMapper.deleteBatchIds(idList);
        if(isSuccess!=0){
            return true;
        }
        return false;
    }

    @Override
    public List<Permission> getPermissionByRoldId(Long roleId) {
        //1.查询设定了的所有权限数据
        //List<Permission> allPermissionsList = baseMapper.selectList(null);
        List<Permission> allPermissionsList = queryAllMenu();
        //2.查询角色拥有的权限id列表,即该角色有那些权限项
        //2.1查询role_permission表中角色id为给定roleId的所有记录
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        List<RolePermission> rolePermissions = rolePermissionService.getBaseMapper().selectList(wrapper.eq(RolePermission::getRoleId, roleId));
        //2.2角色权限rolePermissions拥有的权限id
        List<Long> hasIdList = rolePermissions.stream().map(RolePermission::getPermissionId).collect(Collectors.toList());

        //2.3 对权限进行分类 即：如果角色拥有的权限id等于permissionId，则说明该角色拥有这项权限
        List<Permission>assignPermissions = new ArrayList<>();
        for (Permission permission : allPermissionsList) {
            if(hasIdList.contains(permission.getId())){
                assignPermissions.add(permission);
            }
        }
        //3.把查询到的1.2两部分数据封装到map集合中
        //Map<String,Object>permissionMap = new HashMap<>();
        //permissionMap.put("allPermissions",allPermissionsList);
        //permissionMap.put("assignPermissions",assignPermissions);
        return assignPermissions;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRolePermission(Long roleId, Long[] permissionId) {
        //1.删除角色分配的权限数据
        rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId,roleId));

        //2.为角色分配新的权限
        List<RolePermission>rolePermissionList= new ArrayList<>();
        for (Long id : permissionId) {
            if(StringUtils.isEmpty(id)){
                continue;
            }
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(id);
            rolePermissionList.add(rolePermission);
        }
        rolePermissionService.saveBatch(rolePermissionList);
    }


    /**
     * 根据父菜单id递归获取子菜单
     * @param id  父菜单id
     * @param idList   子菜单id集合
     */
    private void selectChildListById(Long id,List<Long>idList) {
        //pid表示parentId   select("id")这里的id表示的下一级子菜单的id
        List<Permission> childList = baseMapper.selectList(new QueryWrapper<Permission>().eq("pid", id).select("id"));
        //递归查询是否还有子菜单，有就继续递归添加子菜单id
        childList.stream().forEach(item->{
            idList.add(item.getId());
            this.selectChildListById(item.getId(),idList);
        });
    }

}
