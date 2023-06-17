package com.zero.yianyx.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.zero.yianyx.client.activity.ActivityFeignClient;
import com.zero.yianyx.client.product.ProductFeignClient;
import com.zero.yianyx.common.auth.AuthContextHolder;
import com.zero.yianyx.enums.SkuType;
import com.zero.yianyx.model.product.Category;
import com.zero.yianyx.model.product.SkuInfo;
import com.zero.yianyx.model.search.SkuEs;
import com.zero.yianyx.search.repository.SkuRepository;
import com.zero.yianyx.search.service.SkuService;
import com.zero.yianyx.vo.search.SkuEsQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @description: 商品通过ES进行上下架操作服务
 * @author: ZeroYiAn
 * @time: 2023/6/10
 */
@Service
@Slf4j
public class SkuServiceImpl implements SkuService {
    @Resource
    private ProductFeignClient productFeignClient;
    @Resource
    private SkuRepository skuRepository;
    @Resource
    private ActivityFeignClient activityFeignClient;

    @Resource
    private RedisTemplate redisTemplate;



    @Override
    public List<SkuEs> findHotSkuList() {
        //0代表第一页
        Pageable pageable = PageRequest.of(0,10);
        List<SkuEs> content = skuRepository.findByOrderByHotScoreDesc(pageable).getContent();
        for (SkuEs skuEs : content) {
            log.info("爆款商品信息：{}",skuEs);
        }
        return content;
    }

    @Override
    public Page<SkuEs> search(Pageable pageable, SkuEsQueryVo skuEsQueryVo) {
        //1.向SkuEsQueryVO中设置当前登录用户的仓库id wareId
        skuEsQueryVo.setWareId(AuthContextHolder.getWareId());
        //2.调用SkuRepository方法，根据springData命名规则定义方法，进行条件查询
        Page<SkuEs> page = null;
        //判断keyword是否为空
        if(StringUtils.isEmpty(skuEsQueryVo.getKeyword())) {
            //为空，根据仓库id 和 分类id 进行条件查询
            page = skuRepository.findByCategoryIdAndWareId(skuEsQueryVo.getCategoryId(), skuEsQueryVo.getWareId(), pageable);
        } else {
            //不为空，根据仓库id + keyword 进行条件查询
            page = skuRepository.findByKeywordAndWareId(skuEsQueryVo.getKeyword(), skuEsQueryVo.getWareId(), pageable);
        }

        List<SkuEs>  skuEsList =  page.getContent();
        //3.获取sku对应的促销活动标签
        if(!CollectionUtils.isEmpty(skuEsList)) {
            //遍历skuEsList，得到所有skuId
            List<Long> skuIdList = skuEsList.stream().map(SkuEs::getId).collect(Collectors.toList());
            //根据skuId列表远程调用，调用service-activty里面的接口得到数据
            /*
                返回Map<Long, List<String>> ，其中key是skuId值
                value是List集合，即sku所参与活动的所有活动规则
             */
            Map<Long, List<String>> skuIdToRuleListMap = activityFeignClient.findActivity(skuIdList);
            if(null != skuIdToRuleListMap) {
                skuEsList.forEach(skuEs -> {
                    skuEs.setRuleList(skuIdToRuleListMap.get(skuEs.getId()));
                });
            }
        }
        return page;
    }

    @Override
    public void incrHotScore(Long skuId) {
        /*
            由于ES具有快查询、慢更改的特点
            利用redis实现商品热度数据的频繁更改，再根据自定义规则定期更新ES中的商品热度
            redis中的数据类型：Zset,有序集合，不能放重复元素，每个成员关联一个评分，根据评分对成员进行排序
         */
        String key = "hotScore";
        //redis保存数据，每次加1，返回值是更新后的值
        Double hotScore = redisTemplate.opsForZSet().incrementScore(key, "skuId:" + skuId, 1);
        //自定义更新规则，定期更新ES
        if(hotScore % 10 == 0){
            //更新es
            Optional<SkuEs> optional = skuRepository.findById(skuId);
            SkuEs skuEs = optional.get();
            skuEs.setHotScore(Math.round(hotScore));
            //更新商品热度
            skuRepository.save(skuEs);
        }

    }

    @Override
    public void upperSku(Long skuId) {

        //获取skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if(skuInfo == null){
            return;
        }
        //获取属性
        Category category = productFeignClient.getCategory(skuInfo.getCategoryId());
        SkuEs skuEs = new SkuEs();
        System.out.println(category.getId());
        if (null != category) {
            skuEs.setCategoryId(category.getId());
            skuEs.setCategoryName(category.getName());
        }
        skuEs.setId(skuInfo.getId());
        skuEs.setKeyword(skuInfo.getSkuName()+","+skuEs.getCategoryName());
        skuEs.setWareId(skuInfo.getWareId());
        skuEs.setIsNewPerson(skuInfo.getIsNewPerson());
        skuEs.setImgUrl(skuInfo.getImgUrl());
        skuEs.setTitle(skuInfo.getSkuName());
        if(skuInfo.getSkuType().equals(SkuType.COMMON.getCode())) {
            skuEs.setSkuType(0);
            skuEs.setPrice(skuInfo.getPrice().doubleValue());
            skuEs.setStock(skuInfo.getStock());
            skuEs.setSale(skuInfo.getSale());
            skuEs.setPerLimit(skuInfo.getPerLimit());
        } else {
            //TODO 待完善-秒杀商品

        }
        SkuEs save = skuRepository.save(skuEs);
        log.info("upperSku："+ JSON.toJSONString(save));

    }

    @Override
    public void lowerSku(Long skuId) {
        log.info("下架商品："+skuId);
        this.skuRepository.deleteById(skuId);
    }


}
