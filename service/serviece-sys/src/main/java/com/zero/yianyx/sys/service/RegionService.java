package com.zero.yianyx.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.sys.Region;

import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */
public interface RegionService extends IService<Region> {
    List<Region> findRegionByKeyword(String keyword);
}
