package com.zero.yianyx.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.model.sys.Ware;
import com.zero.yianyx.sys.mapper.WareMapper;
import com.zero.yianyx.sys.service.WareService;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */
@Service
public class WareServiceImpl extends ServiceImpl<WareMapper, Ware> implements WareService {
}
