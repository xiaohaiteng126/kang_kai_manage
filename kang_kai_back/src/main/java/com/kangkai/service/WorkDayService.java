package com.kangkai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kangkai.entity.WorkDay;
import com.kangkai.vo.WorkDayTableVO;

import java.util.List;

public interface WorkDayService extends IService<WorkDay> {
    WorkDayTableVO queryTable(Long projectId, String startMonth, String endMonth);
    void batchSave(List<WorkDay> workDays);
}
