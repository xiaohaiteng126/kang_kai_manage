package com.kangkai.service;

import com.kangkai.entity.LedgerSign;
import com.kangkai.vo.LedgerTableVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface LedgerService {
    LedgerTableVO queryTable(Long projectId, String startMonth, String endMonth);
    void exportExcel(Long projectId, String startMonth, String endMonth, HttpServletResponse response) throws IOException;
    void saveSign(LedgerSign sign);
}
