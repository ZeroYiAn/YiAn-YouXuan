package com.zero.yianyx.acl.controller;

import com.zero.yianyx.acl.service.PermissionService;
import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.model.acl.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 菜单管理处理器
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */
@Api(tags = "菜单管理")
@RestController
@RequestMapping("/admin/acl/permission")
public class PermissionController {
    @Resource
    private PermissionService permissionService;

    @ApiOperation("获取某个角色的所有权限数据")
    @GetMapping("toAssign/{roleId}")
    public Result toAssign(@PathVariable Long roleId){
        //Map<String,Object> permissionMap = permissionService.getPermissionByRoldId(roleId);
        //List<Permission> assignPermissions = permissionService.getPermissionByRoldId(roleId);
        //这里只实现了获取当前所有权限数据，而没有获取指定角色的权限数据。前端对应不上？
        List<Permission> permissionByRoldId = permissionService.queryAllMenu();
        return Result.ok(permissionByRoldId);
    }

    @ApiOperation("给某个角色授予权限")
    @PostMapping("doAssign")
    public Result doAssign(@RequestParam Long roleId,
                           @RequestParam Long[] permissionId){
        permissionService.saveRolePermission(roleId,permissionId);
        return Result.ok(null);
    }

    @ApiOperation("获取菜单")
    @GetMapping
    public Result index(){
        List<Permission> permissionList = permissionService.queryAllMenu();
        return Result.ok(permissionList);
    }

    @ApiOperation("新增菜单")
    @PostMapping("save")
    public Result save(@RequestBody Permission permission){
        permissionService.save(permission);
        return Result.ok(null);
    }

    @ApiOperation("修改菜单")
    @PutMapping("update")
    public Result updateById(@RequestBody Permission permission){
        permissionService.updateById(permission);
        return Result.ok(null);
    }

    @ApiOperation("递归删除菜单")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        permissionService.removeChildById(id);
        return Result.ok(null);
    }




}
