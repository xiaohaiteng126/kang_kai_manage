package com.kangkai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kangkai.entity.Employee;
import com.kangkai.entity.WorkDay;
import com.kangkai.mapper.EmployeeMapper;
import com.kangkai.mapper.WorkDayMapper;
import com.kangkai.service.WorkDayService;
import com.kangkai.utils.DateUtils;
import com.kangkai.vo.EmployeeWorkDayVO;
import com.kangkai.vo.MonthDataVO;
import com.kangkai.vo.WorkDayTableVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkDayServiceImpl extends ServiceImpl<WorkDayMapper, WorkDay> implements WorkDayService {

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private WorkDayMapper workDayMapper;

    @Override
    public WorkDayTableVO queryTable(Long projectId, String startMonth, String endMonth) {
        // 1. Generate month list
        List<String> months = DateUtils.generateMonths(startMonth, endMonth);

        // 2. Fetch all active employees for the project
        List<Employee> employees = employeeMapper.selectList(
                new LambdaQueryWrapper<Employee>()
                        .eq(Employee::getProjectId, projectId)
                        .eq(Employee::getStatus, "在项"));

        // 3. Fetch existing work day records
        List<WorkDay> workDays = this.list(new LambdaQueryWrapper<WorkDay>()
                .eq(WorkDay::getProjectId, projectId)
                .ge(WorkDay::getYearMonth, startMonth)
                .le(WorkDay::getYearMonth, endMonth));

        // 4. Group work days by employeeId -> yearMonth -> WorkDay
        Map<Long, Map<String, WorkDay>> workDayMap = workDays.stream()
                .collect(Collectors.groupingBy(WorkDay::getEmployeeId,
                        Collectors.toMap(WorkDay::getYearMonth, wd -> wd, (a, b) -> a)));

        // 5. Build response
        List<EmployeeWorkDayVO> empVOs = employees.stream().map(emp -> {
            EmployeeWorkDayVO vo = new EmployeeWorkDayVO();
            vo.setEmployeeId(emp.getId());
            vo.setEmployeeName(emp.getName());
            vo.setDailyWage(emp.getDailyWage());

            Map<String, MonthDataVO> monthData = new LinkedHashMap<>();
            Map<String, WorkDay> empWdMap = workDayMap.getOrDefault(emp.getId(), Collections.emptyMap());

            for (String month : months) {
                WorkDay wd = empWdMap.get(month);
                if (wd != null) {
                    monthData.put(month, new MonthDataVO(
                            wd.getWorkDays() != null ? wd.getWorkDays() : BigDecimal.ZERO,
                            wd.getPiecePay() != null ? wd.getPiecePay() : BigDecimal.ZERO,
                            wd.getLoan() != null ? wd.getLoan() : BigDecimal.ZERO,
                            wd.getFine() != null ? wd.getFine() : BigDecimal.ZERO,
                            wd.getToolsOther() != null ? wd.getToolsOther() : BigDecimal.ZERO,
                            wd.getRemark() != null ? wd.getRemark() : ""));
                } else {
                    monthData.put(month, new MonthDataVO());
                }
            }
            vo.setMonthData(monthData);
            return vo;
        }).collect(Collectors.toList());

        WorkDayTableVO table = new WorkDayTableVO();
        table.setMonths(months);
        table.setEmployees(empVOs);
        return table;
    }

    @Override
    @Transactional
    public void batchSave(List<WorkDay> workDays) {
        if (workDays == null || workDays.isEmpty()) {
            return;
        }
        // Filter out rows with all zeros to reduce payload
        List<WorkDay> filtered = workDays.stream()
                .filter(wd -> wd.getEmployeeId() != null && wd.getYearMonth() != null)
                .collect(Collectors.toList());

        if (!filtered.isEmpty()) {
            workDayMapper.upsert(filtered);
        }
    }
}
