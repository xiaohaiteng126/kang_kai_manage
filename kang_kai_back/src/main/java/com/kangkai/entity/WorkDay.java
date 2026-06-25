package com.kangkai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("work_day")
public class WorkDay implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long employeeId;
    private Long projectId;
    @TableField("`year_month`")
    private String yearMonth;
    private BigDecimal workDays;
    private BigDecimal piecePay;
    private BigDecimal loan;
    private BigDecimal fine;
    private BigDecimal toolsOther;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
