package com.zero.yianyx.activity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zero.yianyx.activity.service.ActivityInfoService;
import com.zero.yianyx.common.result.Result;
import com.zero.yianyx.model.activity.ActivityInfo;
import com.zero.yianyx.vo.activity.ActivityRuleVo;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/11
 */
@RestController
@RequestMapping("/admin/activity/activityInfo")
public class ActivityInfoController {
    @Resource
    private ActivityInfoService activityInfoService;

    @GetMapping("{page}/{limit}")
    public Result index(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {
        Page<ActivityInfo> pageParam = new Page<>(page, limit);
        IPage<ActivityInfo> pageModel = activityInfoService.selectPage(pageParam);
        return Result.ok(pageModel);
    }


    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        ActivityInfo activityInfo = activityInfoService.getById(id);
        activityInfo.setActivityTypeString(activityInfo.getActivityType().getComment());
        return Result.ok(activityInfo);
    }


    @PostMapping("save")
    public Result save(@RequestBody ActivityInfo activityInfo) {
        activityInfo.setCreateTime(new Date());
        activityInfoService.save(activityInfo);
        return Result.ok();
    }

    @PutMapping("update")
    public Result updateById(@RequestBody ActivityInfo activityInfo) {
        activityInfoService.updateById(activityInfo);
        return Result.ok();
    }


    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        activityInfoService.removeById(id);
        return Result.ok();
    }


    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<String> idList){
        activityInfoService.removeByIds(idList);
        return Result.ok();
    }


    @GetMapping("findActivityRuleList/{id}")
    public Result findActivityRuleList(@PathVariable Long id) {
        return Result.ok(activityInfoService.findActivityRuleList(id));
    }


    @PostMapping("saveActivityRule")
    public Result saveActivityRule(@RequestBody ActivityRuleVo activityRuleVo) {
        activityInfoService.saveActivityRule(activityRuleVo);
        return Result.ok();
    }

    /**
     * 根据关键字获取sku列表，活动使用
     * @param keyword
     * @return
     */
    @GetMapping("findSkuInfoByKeyword/{keyword}")
    public Result findSkuInfoByKeyword(@PathVariable("keyword") String keyword) {
        return Result.ok(activityInfoService.findSkuInfoByKeyword(keyword));
    }
}
