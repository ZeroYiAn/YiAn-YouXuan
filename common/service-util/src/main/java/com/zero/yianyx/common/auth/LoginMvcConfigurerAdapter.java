package com.zero.yianyx.common.auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.annotation.Resource;

/**
 * @description: 自定义拦截器配置类：设置拦截器拦截路径
 * @author: ZeroYiAn
 * @time: 2023/6/12
 */
@Configuration
public class LoginMvcConfigurerAdapter extends WebMvcConfigurationSupport {

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //添加自定义拦截器，设置路径
        registry.addInterceptor(
                        new UserLoginInterceptor(redisTemplate))
                //要拦截的路径
                .addPathPatterns("/api/**")
                //不拦截的路径
                .excludePathPatterns("/api/user/weixin/wxLogin/*");
        super.addInterceptors(registry);
    }
}