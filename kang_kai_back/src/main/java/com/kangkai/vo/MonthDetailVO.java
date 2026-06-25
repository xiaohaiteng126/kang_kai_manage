package com.kangkai.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthDetailVO {
    private String yearMonth;
    private BigDecimal workDays = BigDecimal.ZERO;
    private BigDecimal piecePay = BigDecimal.ZERO;
    private BigDecimal loan = BigDecimal.ZERO;
    private BigDecimal fine = BigDecimal.ZERO;
    private BigDecimal toolsOther = BigDecimal.ZERO;
    private String remark = "";
}
