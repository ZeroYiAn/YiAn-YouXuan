package com.zero.yianyx.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zero.yianyx.model.activity.CouponInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/11
 */
@Repository
public interface CouponInfoMapper extends BaseMapper<CouponInfo> {
    /**
     * 查询优惠券信息
     * @param id
     * @param categoryId
     * @param userId
     * @return
     */
    List<CouponInfo> selectCouponInfoList(@Param("skuId") Long id,
                                          @Param("categoryId") Long categoryId,
                                          @Param("userId") Long userId);


    /**
     * 获取用户全部优惠券
     * @param userId
     * @return
     */
    List<CouponInfo> selectCartCouponInfoList(@Param("userId")Long userId);

}
