package com.zero.yianyx.acl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.acl.mapper.AdminRoleMapper;
import com.zero.yianyx.acl.service.AdminRoleService;
import com.zero.yianyx.model.acl.AdminRole;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/7
 */
@Service
public class AdminRoleServiceImpl extends ServiceImpl<AdminRoleMapper, AdminRole> implements AdminRoleService {
}
