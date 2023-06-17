package com.zero.yianyx.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zero.yianyx.model.sys.RegionWare;
import com.zero.yianyx.vo.sys.RegionWareQueryVo;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */
public interface RegionWareService extends IService<RegionWare> {
    /**
     * 开通区域列表
     * @param pageParam
     * @param regionWareQueryVo
     * @return
     */
    IPage<RegionWare> selectPage(Page<RegionWare> pageParam, RegionWareQueryVo regionWareQueryVo);

    /**
     * 添加开通区域
     * @param regionWare
     */
    void saveRegionWare(RegionWare regionWare);

    /**
     * 取消开通区域
     * @param id
     * @param status
     */
    void updateStatus(Long id, Integer status);
}
