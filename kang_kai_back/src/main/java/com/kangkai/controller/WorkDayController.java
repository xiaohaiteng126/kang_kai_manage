package com.kangkai.controller;

import com.kangkai.common.Result;
import com.kangkai.entity.WorkDay;
import com.kangkai.service.WorkDayService;
import com.kangkai.vo.WorkDayTableVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/work-days")
public class WorkDayController {

    @Resource
    private WorkDayService workDayService;

    @GetMapping
    public Result<WorkDayTableVO> query(@RequestParam Long projectId,
                                         @RequestParam String startMonth,
                                         @RequestParam String endMonth) {
        return Result.success(workDayService.queryTable(projectId, startMonth, endMonth));
    }

    @PostMapping("/batch")
    public Result<?> batchSave(@RequestBody List<WorkDay> workDays) {
        workDayService.batchSave(workDays);
        return Result.success("保存成功");
    }
}
