package com.zero.yianyx.acl.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.acl.Admin;
import com.zero.yianyx.vo.acl.AdminQueryVo;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/7
 */
public interface AdminService extends IService<Admin> {
    /**
     * 角色列表(条件分页查询)
     * @param pageParam 分页参数
     * @param adminQueryVo 分页值对象
     * @return 分页对象
     */
    IPage<Admin> selectAdminPage(Page<Admin> pageParam, AdminQueryVo adminQueryVo);
}
