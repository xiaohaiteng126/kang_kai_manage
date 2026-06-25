package com.kangkai.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LedgerRowVO {
    private Integer rowNo;
    private Long employeeId;
    private String employeeName;
    private BigDecimal totalWorkDays;
    private BigDecimal dailyWage;
    private BigDecimal totalPiecePay;
    private List<MonthDetailVO> months;
    private BigDecimal totalLoan;
    private BigDecimal totalFine;
    private BigDecimal totalToolsOther;
    private BigDecimal balance;
    private String signature;
    private String remark;
}
