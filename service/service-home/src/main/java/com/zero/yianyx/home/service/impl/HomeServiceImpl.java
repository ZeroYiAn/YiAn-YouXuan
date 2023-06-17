package com.zero.yianyx.home.service.impl;

import com.zero.yianyx.client.product.ProductFeignClient;
import com.zero.yianyx.client.search.SkuFeignClient;
import com.zero.yianyx.client.user.UserFeignClient;
import com.zero.yianyx.home.service.HomeService;
import com.zero.yianyx.model.product.Category;
import com.zero.yianyx.model.product.SkuInfo;
import com.zero.yianyx.model.search.SkuEs;
import com.zero.yianyx.vo.user.LeaderAddressVo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/12
 */
@Service
public class HomeServiceImpl implements HomeService {
    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private ProductFeignClient productFeignClient;

    @Resource
    private SkuFeignClient skuFeignClient;


    @Override
    public Map<String, Object> home(Long userId) {
        Map<String,Object>result = new HashMap<>();
        //1.根据userId获取当前登录用户提货地址信息
        //调用远程service-user模块接口获取需要数据
        LeaderAddressVo leaderAddressVo = userFeignClient.getUserAddressByUserId(userId);
        result.put("leaderAddressVo",leaderAddressVo);
        //2.获取所有分类
        //远程调用service-product模块
        List<Category> allCategoryList = productFeignClient.findAllCategoryList();
        result.put("categoryList",allCategoryList);
        //3.获取新人专享商品
        //远程调用service-product模块接口
        List<SkuInfo> newPersonSkuInfoList = productFeignClient.findNewPersonSkuInfoList();
        result.put("newPersonSkuInfoList",newPersonSkuInfoList);
        //4.获取热销商品
        //远程调用service-search模块
        //score评分降序排序
         List<SkuEs> hotSkuList = skuFeignClient.findHotSkuList();
         if(!CollectionUtils.isEmpty(hotSkuList)){
             result.put("hotSkuList",hotSkuList);
         }
        //5.封装获取到的所有数据到map中进行返回
        return result;
    }
}
