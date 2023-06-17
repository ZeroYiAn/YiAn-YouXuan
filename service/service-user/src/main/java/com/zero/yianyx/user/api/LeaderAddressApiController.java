package com.zero.yianyx.user.api;

import com.zero.yianyx.user.service.UserService;
import com.zero.yianyx.vo.user.LeaderAddressVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/12
 */
@Api(tags = "团长接口")
@RestController
@RequestMapping("/api/user/leader")
public class LeaderAddressApiController {
    @Resource
    private UserService userService;

    @ApiOperation("提货点地址信息")
    @GetMapping("/inner/getUserAddressByUserId/{userId}")
    public LeaderAddressVo getUserAddressByUserId(@PathVariable(value = "userId") Long userId) {
        return userService.getLeaderAddressVoByUserId(userId);
    }
}
