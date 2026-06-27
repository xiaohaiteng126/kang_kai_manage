package com.kangkai.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StatisticsVO {
    private List<EmployeeStat> stats;

    @Data
    public static class EmployeeStat {
        private String employeeName;
        private BigDecimal totalWorkDays;
        private BigDecimal totalPiecePay;
        private BigDecimal totalLoan;
        private BigDecimal totalFine;
    }
}
