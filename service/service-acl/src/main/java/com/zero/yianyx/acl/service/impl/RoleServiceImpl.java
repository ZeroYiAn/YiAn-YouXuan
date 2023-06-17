package com.zero.yianyx.acl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.acl.mapper.RoleMapper;
import com.zero.yianyx.acl.service.AdminRoleService;
import com.zero.yianyx.acl.service.RoleService;
import com.zero.yianyx.model.acl.AdminRole;
import com.zero.yianyx.model.acl.Role;
import com.zero.yianyx.vo.acl.RoleQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description: 角色管理服务实现类
 * @author: ZeroYiAn
 * @time: 2023/6/7
 */

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Resource
    private AdminRoleService adminRoleService;

    @Override
    public IPage<Role> selectRolePage(Page<Role> pageParam, RoleQueryVo roleQueryVo) {
        String roleName = roleQueryVo.getRoleName();
        //创建mp条件对象
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        //判断条件值是否为空，不空则封装条件查询
        if(!StringUtils.isEmpty(roleName)){
            wrapper.like(Role::getRoleName,roleName);
        }
        //调用方法实现条件分页查询
        IPage<Role> rolePage = baseMapper.selectPage(pageParam, wrapper);
        return rolePage;
    }

    @Override
    public Map<String, Object> getRoleByUserId(Long userId) {
        //1.查询设定了的所有角色
        List<Role> allRolesList = baseMapper.selectList(null);

        //2.查询用户拥有的角色id列表
        //2.1 查询admin_role 表中用户id 为给定userId的所有记录
        LambdaQueryWrapper<AdminRole> wrapper = new LambdaQueryWrapper<>();
        List<AdminRole> adminRoles = adminRoleService.getBaseMapper().selectList(wrapper.eq(AdminRole::getAdminId, userId));
        //2.2 adminRoles拥有的角色id
        List<Long> hasIdList = adminRoles.stream().map(AdminRole::getRoleId).collect(Collectors.toList());

        //2.3 对角色进行分类 即：如果用户拥有的角色id 等于roleId,则说明该角色已分配给用户，否则属于未分配
        List<Role>assignedRoles = new ArrayList<>();
        for (Role role : allRolesList) {
           if(hasIdList.contains(role.getId())){
               assignedRoles.add(role);
           }
        }

        //3.把查询到的1、2两部分数据封装到map集合中
        Map<String,Object>roleMap = new HashMap<>();
        roleMap.put("allRolesList",allRolesList);
        roleMap.put("assignRoles",assignedRoles);

        return roleMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserRoleRelationShip(Long userId, Long[] rolesId) {
        //1.删除用户分配的角色数据
        adminRoleService.remove(new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getAdminId,userId));

        //2.分配新的角色 比如userId：1，rolesId = 2，3
        List<AdminRole>userRoleList  = new ArrayList<>();
        for (Long roleId : rolesId) {
            if(StringUtils.isEmpty(roleId)){
                continue;
            }
            AdminRole userRole = new AdminRole();
            userRole.setAdminId(userId);
            userRole.setRoleId(roleId);
            userRoleList.add(userRole);
        }
        adminRoleService.saveBatch(userRoleList);
    }
}
