package com.zero.yianyx.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zero.yianyx.model.product.SkuInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
@Repository
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {
    /**
     * 解锁库存
     * @param skuId
     * @param skuNum
     */
    Integer unlockStock(@Param("skuId")Long skuId, @Param("skuNum")Integer skuNum);

    /**
     * 验证库存
     * @param skuId
     * @param skuNum
     * @return
     */
    SkuInfo checkStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum);

    /**
     * 锁定库存
     * @param skuId
     * @param skuNum
     * @return
     */
    Integer lockStock(@Param("skuId")Long skuId, @Param("skuNum")Integer skuNum);


    /**
     * 扣减库存
     * @param skuId
     * @param skuNum
     * @return
     */
    Integer minusStock(@Param("skuId")Long skuId, @Param("skuNum")Integer skuNum);

}
