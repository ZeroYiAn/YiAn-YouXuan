package com.zero.yianyx.acl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.yianyx.acl.service.AdminService;
import com.zero.yianyx.acl.service.RoleService;
import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.model.acl.Admin;
import com.zero.yianyx.utils.MD5;
import com.zero.yianyx.vo.acl.AdminQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @description: 用户管理处理器
 * @author: ZeroYiAn
 * @time: 2023/6/7
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/admin/acl/user")
public class AdminController {

    @Resource
    private AdminService adminService;

    @Resource
    private RoleService roleService;

    @ApiOperation(value = "获取用户角色数据")
    @GetMapping("toAssign/{userId}")
    public Result toAssign(@PathVariable Long userId){
        //返回的map集合包含两部分数据：1.所有角色选项 2.用户已分配的角色列表
        Map<String,Object> roleMap= roleService.getRoleByUserId(userId);
        return Result.ok(roleMap);
    }

    @ApiOperation(value = "为用户分配角色")
    @PostMapping("doAssign")
    public Result doAssign(@RequestParam  Long adminId,
                           @RequestParam Long[] roleId){
        roleService.saveUserRoleRelationShip(adminId,roleId);
        return Result.ok(null);
    }

    @ApiOperation(value = "获取管理用户分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit,
            @ApiParam(name = "userQueryVo", value = "查询对象", required = false)
                    AdminQueryVo userQueryVo) {
        Page<Admin> pageParam = new Page<>(page, limit);
        IPage<Admin> pageModel = adminService.selectAdminPage(pageParam, userQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "获取管理用户")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        Admin user = adminService.getById(id);
        return Result.ok(user);
    }

    @ApiOperation(value = "新增管理用户")
    @PostMapping("save")
    public Result save(@RequestBody Admin user) {
        //对密码进行MD5处理
        user.setPassword(MD5.encrypt(user.getPassword()));
        adminService.save(user);
        return Result.ok(null);
    }

    @ApiOperation(value = "修改管理用户")
    @PutMapping("update")
    public Result updateById(@RequestBody Admin user) {
        adminService.updateById(user);
        return Result.ok(null);
    }

    @ApiOperation(value = "删除管理用户")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        adminService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation(value = "根据id列表删除管理用户")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        adminService.removeByIds(idList);
        return Result.ok(null);
    }
}