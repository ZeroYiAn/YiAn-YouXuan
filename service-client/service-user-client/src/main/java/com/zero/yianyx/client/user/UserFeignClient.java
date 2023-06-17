package com.zero.yianyx.client.user;

import com.zero.yianyx.vo.user.LeaderAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/12
 */
@FeignClient(value = "service-user")
public interface UserFeignClient {

    @GetMapping("/api/user/leader/inner/getUserAddressByUserId/{userId}")
    public LeaderAddressVo getUserAddressByUserId(@PathVariable(value = "userId") Long userId);
}
