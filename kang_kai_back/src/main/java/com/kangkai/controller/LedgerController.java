package com.kangkai.controller;

import com.kangkai.common.Result;
import com.kangkai.entity.LedgerSign;
import com.kangkai.service.LedgerService;
import com.kangkai.vo.LedgerTableVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    @Resource
    private LedgerService ledgerService;

    @GetMapping
    public Result<LedgerTableVO> query(@RequestParam Long projectId,
                                        @RequestParam String startMonth,
                                        @RequestParam String endMonth) {
        return Result.success(ledgerService.queryTable(projectId, startMonth, endMonth));
    }

    @GetMapping("/export")
    public void export(@RequestParam Long projectId,
                       @RequestParam String startMonth,
                       @RequestParam String endMonth,
                       HttpServletResponse response) throws IOException {
        ledgerService.exportExcel(projectId, startMonth, endMonth, response);
    }

    @PostMapping("/sign")
    public Result<?> saveSign(@RequestBody LedgerSign sign) {
        ledgerService.saveSign(sign);
        return Result.success("保存成功");
    }
}
