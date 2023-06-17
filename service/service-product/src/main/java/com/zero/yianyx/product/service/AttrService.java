package com.zero.yianyx.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.product.Attr;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/9
 */
public interface AttrService extends IService<Attr> {
    /**
     * 根据属性分组id获取属性列表
     * @param attrGroupId
     * @return
     */
    List<Attr> findByAttrGroupId(Long attrGroupId);
}
