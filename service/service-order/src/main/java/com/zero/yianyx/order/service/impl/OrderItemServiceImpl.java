package com.zero.yianyx.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.model.order.OrderItem;
import com.zero.yianyx.order.mapper.OrderItemMapper;
import com.zero.yianyx.order.service.OrderItemService;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/15
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
}
