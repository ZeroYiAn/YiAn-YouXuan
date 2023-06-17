package com.zero.yianyx.acl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.yianyx.acl.service.RoleService;
import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.model.acl.Role;
import com.zero.yianyx.vo.acl.RoleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 角色管理处理器
 * @author: ZeroYiAn
 * @time: 2023/6/7
 */
@Api(tags = "角色管理")
@RestController
@RequestMapping("/admin/acl/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    /**
     * 1.角色列表(条件分页查询)
     */
    @ApiOperation("角色条件分页查询")
    @GetMapping("{current}/{limit}")
    public Result pageList(@PathVariable Long current,
                           @PathVariable Long limit,
                           RoleQueryVo roleQueryVo){
        //1.创建page对象，传递当前页和每页记录数
        Page<Role> pageParam = new Page<>(current, limit);
        //2.调用roleService方法实现分页查询，返回分页对象
        IPage<Role>pageModel = roleService.selectRolePage(pageParam,roleQueryVo);
        return Result.ok(pageModel);
    }

    /**
     * 2.根据id查询角色
     */
    @ApiOperation("根据id查询角色")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id){
        Role role = roleService.getById(id);
        return Result.ok(role);
    }

    /**
     * 3.添加角色
     */
    @ApiOperation("添加角色")
    @PostMapping("save")
    public Result save(@RequestBody Role role){//@RequestBody 接受json格式数据封装到对象中
        boolean isSuccess = roleService.save(role);
        if(!isSuccess){
            return Result.fail(null);
        }
        return Result.ok(null);
    }

    /**
     * 4.修改角色
     */
    @ApiOperation("修改角色")
    @PutMapping("update")
    public Result update(@RequestBody Role role){//@RequestBody 接受json格式数据封装到对象中
        boolean isSuccess = roleService.updateById(role);
        if(!isSuccess){
            return Result.fail(null);
        }
        return Result.ok(null);
    }

    /**
     * 5.根据id删除角色
     */
    @ApiOperation("根据id删除角色")
    @DeleteMapping("remove/{id}")
    public  Result remove(@PathVariable Long id){
        boolean isSuccess = roleService.removeById(id);
        if(!isSuccess){
            return Result.fail(null);
        }
        return Result.ok(null);
    }

    /**
     * 6.批量删除角色
     */
    @ApiOperation("批量删除角色")
    @DeleteMapping("batchRemove")
    public  Result remove(@RequestBody List<Long> ids){
        boolean isSuccess = roleService.removeByIds(ids);
        if(!isSuccess){
            return Result.fail(null);
        }
        return Result.ok(null);
    }


}
