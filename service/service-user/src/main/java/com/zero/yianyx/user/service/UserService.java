package com.zero.yianyx.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.user.User;
import com.zero.yianyx.vo.user.LeaderAddressVo;
import com.zero.yianyx.vo.user.UserLoginVo;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/12
 */
public interface UserService extends IService<User> {
    LeaderAddressVo getLeaderAddressVoByUserId(Long userId);

    /**
     * 根据微信openid获取用户信息
     * @param openId
     * @return
     */
    User getByOpenid(String openId);

    /**
     * 获取当前登录用户信息
     * @param userId
     * @return
     */
    UserLoginVo getUserLoginVo(Long userId);
}
