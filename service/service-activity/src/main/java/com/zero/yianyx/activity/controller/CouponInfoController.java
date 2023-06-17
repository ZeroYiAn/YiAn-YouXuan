package com.zero.yianyx.activity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.yianyx.activity.service.CouponInfoService;
import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.model.activity.CouponInfo;
import com.zero.yianyx.vo.activity.CouponRuleVo;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/11
 */
@RestController
@RequestMapping("/admin/activity/couponInfo")
public class CouponInfoController {
    @Resource
    private CouponInfoService couponInfoService;

    @GetMapping("{page}/{limit}")
    public Result index(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit
    ) {
        Page<CouponInfo> pageParam = new Page<>(page, limit);
        IPage<CouponInfo> pageModel = couponInfoService.selectPage(pageParam);
        return Result.ok(pageModel);
    }

    @GetMapping("get/{id}")
    public Result get(@PathVariable String id) {
        CouponInfo couponInfo = couponInfoService.getCouponInfo(id);
        return Result.ok(couponInfo);
    }

    @PostMapping("save")
    public Result save(@RequestBody CouponInfo couponInfo) {
        couponInfoService.save(couponInfo);
        return Result.ok();
    }

    @PutMapping("update")
    public Result updateById(@RequestBody CouponInfo couponInfo) {
        couponInfoService.updateById(couponInfo);
        return Result.ok();
    }

    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable String id) {
        couponInfoService.removeById(id);
        return Result.ok();
    }

    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<String> idList){
        couponInfoService.removeByIds(idList);
        return Result.ok();
    }

    @GetMapping("findCouponRuleList/{id}")
    public Result findActivityRuleList(@PathVariable Long id) {
        return Result.ok(couponInfoService.findCouponRuleList(id));
    }

    @PostMapping("saveCouponRule")
    public Result saveCouponRule(@RequestBody CouponRuleVo couponRuleVo) {
        couponInfoService.saveCouponRule(couponRuleVo);
        return Result.ok();
    }


    @GetMapping("findCouponByKeyword/{keyword}")
    public Result findCouponByKeyword(@PathVariable("keyword") String keyword) {
        return Result.ok(couponInfoService.findCouponByKeyword(keyword));
    }
}
