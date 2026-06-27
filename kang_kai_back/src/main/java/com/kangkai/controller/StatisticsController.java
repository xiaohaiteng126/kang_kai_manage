package com.kangkai.controller;

import com.kangkai.common.Result;
import com.kangkai.service.StatisticsService;
import com.kangkai.vo.StatisticsVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    @GetMapping
    public Result<StatisticsVO> query(@RequestParam Long projectId,
                                       @RequestParam String startMonth,
                                       @RequestParam String endMonth) {
        return Result.success(statisticsService.queryStatistics(projectId, startMonth, endMonth));
    }
}
