package com.kangkai.vo;

import lombok.Data;

import java.util.List;

@Data
public class LedgerTableVO {
    private List<String> months;
    private List<LedgerRowVO> rows;
}
