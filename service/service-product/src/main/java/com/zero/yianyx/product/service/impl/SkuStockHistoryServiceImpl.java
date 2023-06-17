package com.zero.yianyx.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.model.product.SkuStockHistory;
import com.zero.yianyx.product.mapper.SkuStockHistoryMapper;
import com.zero.yianyx.product.service.SkuStockHistoryService;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
@Service
public class SkuStockHistoryServiceImpl extends ServiceImpl<SkuStockHistoryMapper, SkuStockHistory> implements SkuStockHistoryService {
}
