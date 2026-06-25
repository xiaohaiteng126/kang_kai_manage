package com.kangkai.vo;

import lombok.Data;

import java.util.List;

@Data
public class WorkDayTableVO {
    private List<String> months;
    private List<EmployeeWorkDayVO> employees;
}
