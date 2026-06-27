package com.kangkai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kangkai.entity.Employee;
import com.kangkai.entity.WorkDay;
import com.kangkai.mapper.EmployeeMapper;
import com.kangkai.mapper.WorkDayMapper;
import com.kangkai.service.StatisticsService;
import com.kangkai.vo.StatisticsVO;
import com.kangkai.vo.StatisticsVO.EmployeeStat;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private WorkDayMapper workDayMapper;

    @Override
    public StatisticsVO queryStatistics(Long projectId, String startMonth, String endMonth) {
        // Fetch active employees
        List<Employee> employees = employeeMapper.selectList(
                new LambdaQueryWrapper<Employee>()
                        .eq(Employee::getProjectId, projectId)
                        .eq(Employee::getStatus, "在项"));

        // Fetch work day records in range
        List<WorkDay> workDays = workDayMapper.selectList(
                new LambdaQueryWrapper<WorkDay>()
                        .eq(WorkDay::getProjectId, projectId)
                        .ge(WorkDay::getYearMonth, startMonth)
                        .le(WorkDay::getYearMonth, endMonth));

        // Group by employee
        Map<Long, List<WorkDay>> empMap = workDays.stream()
                .collect(Collectors.groupingBy(WorkDay::getEmployeeId));

        List<EmployeeStat> stats = new ArrayList<>();
        for (Employee emp : employees) {
            EmployeeStat stat = new EmployeeStat();
            stat.setEmployeeName(emp.getName());

            List<WorkDay> empWds = empMap.getOrDefault(emp.getId(), new ArrayList<>());
            stat.setTotalWorkDays(sum(empWds, WorkDay::getWorkDays));
            stat.setTotalPiecePay(sum(empWds, WorkDay::getPiecePay));
            stat.setTotalLoan(sum(empWds, WorkDay::getLoan));
            stat.setTotalFine(sum(empWds, WorkDay::getFine));

            stats.add(stat);
        }

        StatisticsVO vo = new StatisticsVO();
        vo.setStats(stats);
        return vo;
    }

    private BigDecimal sum(List<WorkDay> list, java.util.function.Function<WorkDay, BigDecimal> getter) {
        return list.stream()
                .map(wd -> getter.apply(wd) != null ? getter.apply(wd) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
