package com.zero.yianyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.common.constant.RedisConst;
import com.zero.yianyx.common.exception.YianyxException;
import com.zero.yianyx.common.result.ResultCodeEnum;
import com.zero.yianyx.model.product.SkuAttrValue;
import com.zero.yianyx.model.product.SkuImage;
import com.zero.yianyx.model.product.SkuInfo;
import com.zero.yianyx.model.product.SkuPoster;
import com.zero.yianyx.mq.constant.MqConst;
import com.zero.yianyx.mq.service.RabbitService;
import com.zero.yianyx.product.mapper.SkuInfoMapper;
import com.zero.yianyx.product.repository.SkuESRepository;
import com.zero.yianyx.product.service.SkuAttrValueService;
import com.zero.yianyx.product.service.SkuImageService;
import com.zero.yianyx.product.service.SkuInfoService;
import com.zero.yianyx.product.service.SkuPosterService;
import com.zero.yianyx.vo.product.SkuInfoQueryVo;
import com.zero.yianyx.vo.product.SkuInfoVo;
import com.zero.yianyx.vo.product.SkuStockLockVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
@Service
@Slf4j
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {
    @Resource
    private SkuPosterService skuPosterService;

    @Resource
    private SkuImageService skuImagesService;

    @Resource
    private SkuAttrValueService skuAttrValueService;

    @Resource
    private RabbitService rabbitService;

    @Resource
    private SkuESRepository skuESRepository;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;


    @Override
    public void minusStock(String orderNo) {
        // 从redis获取锁定库存的缓存信息
        List<SkuStockLockVo> skuStockLockVoList = (List<SkuStockLockVo>)this.redisTemplate.opsForValue().get(RedisConst.SROCK_INFO + orderNo);
        if (CollectionUtils.isEmpty(skuStockLockVoList)){
            return ;
        }

        // 减库存：遍历每个商品id 和 数量 进行库存扣减
        skuStockLockVoList.forEach(skuStockLockVo -> {
            baseMapper.minusStock(skuStockLockVo.getSkuId(), skuStockLockVo.getSkuNum());
        });

        // 解锁库存之后，删除锁定库存的缓存。以防止重复解锁库存
        this.redisTemplate.delete(RedisConst.SROCK_INFO + orderNo);
    }


    @Override
    public IPage<SkuInfo> selectPage(Page<SkuInfo> pageParam, SkuInfoQueryVo skuInfoQueryVo) {
        //获取条件值
        String keyword = skuInfoQueryVo.getKeyword();
        String skuType = skuInfoQueryVo.getSkuType();
        Long categoryId = skuInfoQueryVo.getCategoryId();
        //封装条件
        LambdaQueryWrapper<SkuInfo> wrapper = new LambdaQueryWrapper<>();
        if(!StringUtils.isEmpty(keyword)) {
            wrapper.like(SkuInfo::getSkuName,keyword);
        }
        if(!StringUtils.isEmpty(skuType)) {
            wrapper.eq(SkuInfo::getSkuType,skuType);
        }
        if(categoryId!=null) {
            wrapper.eq(SkuInfo::getCategoryId,categoryId);
        }
        //调用方法查询
        return baseMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public void saveSkuInfo(SkuInfoVo skuInfoVo) {
        //保存sku信息
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo, skuInfo);
        this.save(skuInfo);

        //保存sku海报
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if (!CollectionUtils.isEmpty(skuPosterList)) {
            for (SkuPoster skuPoster : skuPosterList) {
                skuPoster.setSkuId(skuInfo.getId());
            }
            skuPosterService.saveBatch(skuPosterList);
        }
        //保存sku图片
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if (!CollectionUtils.isEmpty(skuImagesList)) {
            int sort = 1;
            for (SkuImage skuImages : skuImagesList) {
                skuImages.setSkuId(skuInfo.getId());
                skuImages.setSort(sort);
                sort++;
            }
            skuImagesService.saveBatch(skuImagesList);
        }

        //保存sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if(!CollectionUtils.isEmpty(skuAttrValueList)) {
            int sort = 1;
            for(SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValue.setSort(sort);
                sort++;
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }

    }

    @Override
    public SkuInfoVo getSkuInfoVo(Long skuId) {
        SkuInfoVo skuInfoVo = new SkuInfoVo();
        //根据id查询sku基本信息
        SkuInfo skuInfo = baseMapper.selectById(skuId);
        //根据id查询商品图片列表
        List<SkuImage> skuImageList = skuImagesService.findBySkuId(skuId);
        //根据id查询商品海报列表
        List<SkuPoster> skuPosterList = skuPosterService.findBySkuId(skuId);
        //根据id查询商品属性信息列表
        List<SkuAttrValue> skuAttrValueList = skuAttrValueService.findBySkuId(skuId);

        BeanUtils.copyProperties(skuInfo, skuInfoVo);
        skuInfoVo.setSkuImagesList(skuImageList);
        skuInfoVo.setSkuPosterList(skuPosterList);
        skuInfoVo.setSkuAttrValueList(skuAttrValueList);
        return  skuInfoVo;
    }

    @Override
    public void updateSkuInfo(SkuInfoVo skuInfoVo) {
        Long skuId = skuInfoVo.getId();
        //1.修改sku基本信息: 把skuInfoVO
        SkuInfo skuInfo = new SkuInfo();
        BeanUtils.copyProperties(skuInfoVo,skuInfo);
        baseMapper.updateById(skuInfo);

        //2.修改sku图片信息:先全部删除再重新添加
        skuImagesService.remove(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId,skuId));
        List<SkuImage> skuImagesList = skuInfoVo.getSkuImagesList();
        if(!CollectionUtils.isEmpty(skuImagesList)){
            int sort =1;
            for (SkuImage skuImage : skuImagesList) {
                skuImage.setSkuId(skuId);
                skuImage.setSort(sort);
                sort++;
            }
            skuImagesService.saveBatch(skuImagesList);
        }
        //3.修改sku海报信息
        skuPosterService.remove(new LambdaQueryWrapper<SkuPoster>().eq(SkuPoster::getSkuId,skuId));
        List<SkuPoster> skuPosterList = skuInfoVo.getSkuPosterList();
        if(!CollectionUtils.isEmpty(skuPosterList)){
            for (SkuPoster skuPoster : skuPosterList) {
                skuPoster.setSkuId(skuId);
            }
            skuPosterService.saveBatch(skuPosterList);
        }

        //4.修改sku属性信息
        skuAttrValueService.remove(new LambdaQueryWrapper<SkuAttrValue>().eq(SkuAttrValue::getSkuId, skuId));
        //保存sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfoVo.getSkuAttrValueList();
        if(!CollectionUtils.isEmpty(skuAttrValueList)) {
            int sort = 1;
            for(SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuId);
                skuAttrValue.setSort(sort);
                sort++;
            }
            skuAttrValueService.saveBatch(skuAttrValueList);
        }
    }

    @Override
    public void check(Long skuId, Integer status) {
        SkuInfo skuInfo = baseMapper.selectById(skuId);
        skuInfo.setCheckStatus(status);
        baseMapper.updateById(skuInfo);
    }

    @Override
    public void publish(Long skuId, Integer status) {
        // 更改发布状态
        SkuInfo skuInfoUp = baseMapper.selectById(skuId);
        if(status == 1) {
            skuInfoUp.setPublishStatus(1);

            //商品上架 发送mq消息更新es数据
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT, MqConst.ROUTING_GOODS_UPPER, skuId);
        } else {
            skuInfoUp.setPublishStatus(0);
            // 商品下架 发送mq消息更新es数据
            rabbitService.sendMessage(MqConst.EXCHANGE_GOODS_DIRECT, MqConst.ROUTING_GOODS_LOWER, skuId);
        }
        baseMapper.updateById(skuInfoUp);
    }

    @Override
    public void isNewUser(Long skuId, Integer status) {
        SkuInfo skuInfoUp = baseMapper.selectById(skuId);
        skuInfoUp.setIsNewPerson(status);
        baseMapper.updateById(skuInfoUp);
    }

    @Override
    public void removeFromES(Long skuId) {
        SkuInfo skuInfo = baseMapper.selectById(skuId);
        if(skuInfo.getPublishStatus()==1){
            log.info("从ES中删除商品："+skuId);
            //先把数据从ES中删除再从数据库中删除，不然会报空指针
            this.skuESRepository.deleteById(skuId);
            baseMapper.deleteById(skuId);
        }
    }

    @Override
    public List<SkuInfo> findSkuInfoList(List<Long> skuIdList) {
        return baseMapper.selectBatchIds(skuIdList);
    }

    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        LambdaQueryWrapper<SkuInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(SkuInfo::getSkuName, keyword);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<SkuInfo> findNewPersonList() {
        //条件1： is_new_person =1
        //条件2： publish_status =1
        //条件3：最多显示3个
        //1.获取第一页，每页显示3条数据
        Page<SkuInfo>pageParam = new Page<>(1,3);
        //2.封装条件
        LambdaQueryWrapper<SkuInfo> queryWrapper = new LambdaQueryWrapper<SkuInfo>()
                .eq(SkuInfo::getIsNewPerson, 1)
                .eq(SkuInfo::getPublishStatus, 1)
                //按库存排序：由高到低
                .orderByDesc(SkuInfo::getStock);
        //调用方法查询
        IPage<SkuInfo> skuInfoPage = baseMapper.selectPage(pageParam, queryWrapper);
        List<SkuInfo> records = skuInfoPage.getRecords();
        return records;
    }


    @Transactional(rollbackFor = {Exception.class})
    @Override
    public Boolean checkAndLock(List<SkuStockLockVo> skuStockLockVoList, String orderToken) {
        //1.判空
        if (CollectionUtils.isEmpty(skuStockLockVoList)){
            throw new YianyxException(ResultCodeEnum.DATA_ERROR);
        }

        //2. 遍历所有商品，验库存并锁库存，要具备原子性
        skuStockLockVoList.forEach(skuStockLockVo -> {
            checkLock(skuStockLockVo);
        });

        //3. 只要有一个商品锁定失败，所有锁定成功的商品要解锁库存
        if (skuStockLockVoList.stream().anyMatch(skuStockLockVo -> !skuStockLockVo.getIsLock())) {
            // 获取所有锁定成功的商品，遍历解锁库存
            skuStockLockVoList.stream().filter(SkuStockLockVo::getIsLock).forEach(skuStockLockVo -> {
                baseMapper.unlockStock(skuStockLockVo.getSkuId(), skuStockLockVo.getSkuNum());
            });
            // 响应锁定状态
            return false;
        }

        //4. 如果所有商品都锁定成功的情况下，需要缓存锁定信息到redis。以方便将来解锁库存 或者 减库存
        // 以orderToken作为key，以lockVos锁定信息作为value
        this.redisTemplate.opsForValue().set(RedisConst.SROCK_INFO + orderToken, skuStockLockVoList);

        // 锁定库存成功之后，定时解锁库存。
        //this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.ttl", orderToken);
        return true;
    }

    private void checkLock(SkuStockLockVo skuStockLockVo){
        //公平锁，就是保证客户端获取锁的顺序，跟他们请求获取锁的顺序，是一样的。
        // 公平锁需要排队
        // ，谁先申请获取这把锁，
        // 谁就可以先获取到这把锁，是按照请求的先后顺序来的。 即在队列中等待时间最长的优先得到锁
        RLock rLock = this.redissonClient
                .getFairLock(RedisConst.SKUKEY_PREFIX + skuStockLockVo.getSkuId());
        rLock.lock();

        try {
            // 验库存：查询，返回的是满足要求的库存列表
            SkuInfo skuInfo = baseMapper.checkStock(skuStockLockVo.getSkuId(), skuStockLockVo.getSkuNum());
            // 如果没有一个仓库满足要求，这里就验库存失败
            if (null == skuInfo) {
                skuStockLockVo.setIsLock(false);
                return;
            }

            // 锁库存：更新 , 锁定库存
            Integer row = baseMapper.lockStock(skuStockLockVo.getSkuId(), skuStockLockVo.getSkuNum());
            if (row == 1) {
                //锁定
                skuStockLockVo.setIsLock(true);
            }
        } finally {
            rLock.unlock();
        }
    }

}
