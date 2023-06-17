package com.zero.yianyx.common.auth;

import com.zero.yianyx.common.constant.RedisConst;
import com.zero.yianyx.utils.helper.JwtHelper;
import com.zero.yianyx.vo.user.UserLoginVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description:  自定义登录拦截器
 * @author: ZeroYiAn
 * @time: 2023/6/12
 */

public class UserLoginInterceptor implements HandlerInterceptor {

    private RedisTemplate redisTemplate;

    public UserLoginInterceptor(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        this.initUserLoginVo(request);
        return true;
    }

    private void initUserLoginVo(HttpServletRequest request){
        //从请求头获取token
        String token = request.getHeader("token");
        //判断token不为空
        if (!StringUtils.isEmpty(token)) {
            //从token中获取用户id
            Long userId = JwtHelper.getUserId(token);
            //根据用户id从redis中获取用户信息
            UserLoginVo userLoginVo = (UserLoginVo)redisTemplate.opsForValue().get(RedisConst.USER_LOGIN_KEY_PREFIX + userId);
            if(userLoginVo != null) {
                //将UserInfo放入上下文中
                AuthContextHolder.setUserId(userLoginVo.getUserId());
                AuthContextHolder.setWareId(userLoginVo.getWareId());
                AuthContextHolder.setUserLoginVo(userLoginVo);
            }
        }
    }
}
