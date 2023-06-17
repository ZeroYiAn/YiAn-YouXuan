package com.zero.yianyx.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.zero.yianyx.common.auth.AuthContextHolder;
import com.zero.yianyx.common.constant.RedisConst;
import com.zero.yianyx.common.exception.YianyxException;
import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.common.result.ResultCodeEnum;
import com.zero.yianyx.enums.UserType;
import com.zero.yianyx.model.user.User;
import com.zero.yianyx.user.service.UserService;
import com.zero.yianyx.user.utils.ConstantPropertiesUtil;
import com.zero.yianyx.user.utils.HttpClientUtils;
import com.zero.yianyx.utils.helper.JwtHelper;
import com.zero.yianyx.vo.user.LeaderAddressVo;
import com.zero.yianyx.vo.user.UserLoginVo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/12
 */
@RestController
@RequestMapping("/api/user/weixin")
@Slf4j
public class WeiXinApiController {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "微信登录获取openid(小程序)")
    @GetMapping("/wxLogin/{code}")
    public Result callback(@PathVariable String code) {
        //1.得到微信返回的code临时票据值
        log.info("微信授权服务器回调------>{}",code);
        if(StringUtils.isEmpty(code)){
            throw new YianyxException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }
        //2.用code + 小程序id + 小程序密钥 请求微信接口服务,换取access_token
        //get 请求，拼接请求地址+参数
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/jscode2session")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&js_code=%s")
                .append("&grant_type=authorization_code");
        //按顺序向 baseAccessTokenUrl 传入参数
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantPropertiesUtil.WX_OPEN_APP_ID,
                ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                code);
        String result = null;
        try {

            //HttpClient发送get请求
            result = HttpClientUtils.get(accessTokenUrl);
        } catch (Exception e) {
            throw new YianyxException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        log.info("使用code换取的access_token结果：{}",result);
        //3.请求微信接口服务完成，返回两个值 session_key 和 open_id(个人微信唯一标识)
        JSONObject resultJson = JSONObject.parseObject(result);
        if(resultJson.getString("errcode") != null){
            throw new YianyxException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

        String accessToken = resultJson.getString("session_key");
        String openId = resultJson.getString("openid");


        //4.添加微信用户信息到数据库中
        //操作user表

        //判断是否是第一次使用微信授权登录：通过数据库中的open_id进行判断
        // 如果没有查到用户信息,那么调用微信个人信息获取的接口
        User user = userService.getByOpenid(openId);
        if(null == user){
            //把用户加入到数据库中
            user = new User();
            user.setOpenId(openId);
            user.setNickName(openId);
            user.setPhotoUrl("");
            user.setUserType(UserType.USER);
            user.setIsNew(0);
            userService.save(user);
        }
        //5.根据userId查询提货点user_delivery表 和团长信息leader表
        LeaderAddressVo leaderAddressVo = userService.getLeaderAddressVoByUserId(user.getId());
        Map<String, Object> map = new HashMap<>();

        //6.使用JWT工具根据userId 和 userName 生成token字符型
        String token = JwtHelper.createToken(user.getId(), user.getNickName());

        //7.获取当前登录用户信息，放到redis进行缓存，设置有效时间
        UserLoginVo userLoginVo = this.userService.getUserLoginVo(user.getId());
        redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + user.getId(), userLoginVo, RedisConst.USERKEY_TIMEOUT, TimeUnit.DAYS);
        map.put("user", user);
        map.put("token", token);
        map.put("leaderAddressVo", leaderAddressVo);
        //8.封装数据到map进行返回
        return Result.ok(map);

    }

    @PostMapping("/auth/updateUser")
    @ApiOperation(value = "更新用户昵称与头像")
    public Result updateUser(@RequestBody User user) {
        //获取当前登录用户
        User user1 = userService.getById(AuthContextHolder.getUserId());
        //把昵称更新为微信用户
        //把表情变成*号
        user1.setNickName(user.getNickName().replaceAll("[ue000-uefff]", "*"));
        user1.setPhotoUrl(user.getPhotoUrl());
        userService.updateById(user1);
        return Result.ok();
    }



}
