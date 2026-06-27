package com.kangkai.service;

import com.kangkai.vo.StatisticsVO;

public interface StatisticsService {
    StatisticsVO queryStatistics(Long projectId, String startMonth, String endMonth);
}
