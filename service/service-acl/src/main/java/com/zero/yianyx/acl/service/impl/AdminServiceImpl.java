package com.zero.yianyx.acl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.acl.mapper.AdminMapper;
import com.zero.yianyx.acl.service.AdminService;
import com.zero.yianyx.model.acl.Admin;
import com.zero.yianyx.vo.acl.AdminQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/7
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Override
    public IPage<Admin> selectAdminPage(Page<Admin> pageParam, AdminQueryVo adminQueryVo) {
        String name = adminQueryVo.getName();
        String userName = adminQueryVo.getUsername();
        //创建mp(mybatis-plus)条件查询对象
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        //判断条件值是否为空，不空则封装条件查询
        if(!StringUtils.isEmpty(userName)){
            wrapper.eq(Admin::getUsername,userName);
        }
        if(!StringUtils.isEmpty(name)){
            wrapper.like(Admin::getName,name);
        }
        //调用方法实现条件分页查询
        IPage<Admin> adminPage = baseMapper.selectPage(pageParam, wrapper);
        return adminPage;
    }
}
