package com.kangkai.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class EmployeeWorkDayVO {
    private Long employeeId;
    private String employeeName;
    private BigDecimal dailyWage;
    private Map<String, MonthDataVO> monthData;
}
