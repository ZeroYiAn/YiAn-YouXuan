package com.zero.yianyx.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zero.yianyx.model.activity.ActivityInfo;
import com.zero.yianyx.model.activity.ActivityRule;
import com.zero.yianyx.model.activity.ActivitySku;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/11
 */
@Repository
public interface ActivityInfoMapper extends BaseMapper<ActivityInfo> {
    /**
     * 获取之前已经参加过该活动的skuId
     * @param skuIdList
     * @return
     */
    List<Long> selectExistSkuIdList(@Param("skuIdList")List<Long> skuIdList);

    /**
     * 根据skuId获取活动规则列表
     * @param skuId
     * @return
     */
    List<ActivityRule> selectActivityRuleList(@Param("skuId")Long skuId);

    /**
     * 根据skuId列表获取所有参与活动
     * @param skuIdList
     * @return
     */
    List<ActivitySku> selectCartActivityList(List<Long> skuIdList);
}
