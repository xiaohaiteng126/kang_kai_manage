package com.kangkai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kangkai.entity.Employee;
import com.kangkai.entity.LedgerSign;
import com.kangkai.entity.Project;
import com.kangkai.entity.WorkDay;
import com.kangkai.mapper.EmployeeMapper;
import com.kangkai.mapper.LedgerSignMapper;
import com.kangkai.mapper.ProjectMapper;
import com.kangkai.mapper.WorkDayMapper;
import com.kangkai.service.LedgerService;
import com.kangkai.utils.DateUtils;
import com.kangkai.utils.ExcelExportUtil;
import com.kangkai.vo.LedgerRowVO;
import com.kangkai.vo.LedgerTableVO;
import com.kangkai.vo.MonthDetailVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LedgerServiceImpl implements LedgerService {

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private WorkDayMapper workDayMapper;

    @Resource
    private ProjectMapper projectMapper;

    @Resource
    private LedgerSignMapper ledgerSignMapper;

    @Override
    public LedgerTableVO queryTable(Long projectId, String startMonth, String endMonth) {
        List<String> months = DateUtils.generateMonths(startMonth, endMonth);

        // Fetch active employees
        List<Employee> employees = employeeMapper.selectList(
                new LambdaQueryWrapper<Employee>()
                        .eq(Employee::getProjectId, projectId)
                        .eq(Employee::getStatus, "在项"));

        // Fetch all work day records in range
        List<WorkDay> workDays = workDayMapper.selectList(new LambdaQueryWrapper<WorkDay>()
                .eq(WorkDay::getProjectId, projectId)
                .ge(WorkDay::getYearMonth, startMonth)
                .le(WorkDay::getYearMonth, endMonth));

        // Group by employee
        Map<Long, List<WorkDay>> empWorkDayMap = workDays.stream()
                .collect(Collectors.groupingBy(WorkDay::getEmployeeId));

        // Fetch ledger signs
        List<LedgerSign> signs = ledgerSignMapper.selectList(new LambdaQueryWrapper<LedgerSign>()
                .eq(LedgerSign::getProjectId, projectId)
                .ge(LedgerSign::getYearMonth, startMonth)
                .le(LedgerSign::getYearMonth, endMonth));
        Map<Long, List<LedgerSign>> signMap = signs.stream()
                .collect(Collectors.groupingBy(LedgerSign::getEmployeeId));

        List<LedgerRowVO> rows = new ArrayList<>();
        int rowNo = 1;

        for (Employee emp : employees) {
            LedgerRowVO row = new LedgerRowVO();
            row.setRowNo(rowNo++);
            row.setEmployeeId(emp.getId());
            row.setEmployeeName(emp.getName());
            row.setDailyWage(emp.getDailyWage());

            List<WorkDay> empWdList = empWorkDayMap.getOrDefault(emp.getId(), Collections.emptyList());
            Map<String, WorkDay> wdByMonth = empWdList.stream()
                    .collect(Collectors.toMap(WorkDay::getYearMonth, wd -> wd, (a, b) -> a));

            // Monthly details
            List<MonthDetailVO> monthDetails = new ArrayList<>();
            BigDecimal totalWorkDays = BigDecimal.ZERO;
            BigDecimal totalPiecePay = BigDecimal.ZERO;
            BigDecimal totalLoan = BigDecimal.ZERO;
            BigDecimal totalFine = BigDecimal.ZERO;
            BigDecimal totalToolsOther = BigDecimal.ZERO;

            for (String month : months) {
                WorkDay wd = wdByMonth.get(month);
                MonthDetailVO md = new MonthDetailVO();
                md.setYearMonth(month);
                if (wd != null) {
                    md.setWorkDays(nn(wd.getWorkDays()));
                    md.setPiecePay(nn(wd.getPiecePay()));
                    md.setLoan(nn(wd.getLoan()));
                    md.setFine(nn(wd.getFine()));
                    md.setToolsOther(nn(wd.getToolsOther()));
                    md.setRemark(wd.getRemark() != null ? wd.getRemark() : "");

                    totalWorkDays = totalWorkDays.add(nn(wd.getWorkDays()));
                    totalPiecePay = totalPiecePay.add(nn(wd.getPiecePay()));
                    totalLoan = totalLoan.add(nn(wd.getLoan()));
                    totalFine = totalFine.add(nn(wd.getFine()));
                    totalToolsOther = totalToolsOther.add(nn(wd.getToolsOther()));
                }
                monthDetails.add(md);
            }

            row.setTotalWorkDays(totalWorkDays);
            row.setTotalPiecePay(totalPiecePay);
            row.setTotalLoan(totalLoan);
            row.setTotalFine(totalFine);
            row.setTotalToolsOther(totalToolsOther);
            row.setMonths(monthDetails);

            // Balance = (totalWorkDays * dailyWage) + totalPiecePay - totalLoan - totalFine - totalToolsOther
            BigDecimal wage = emp.getDailyWage() != null ? emp.getDailyWage() : BigDecimal.ZERO;
            BigDecimal balance = totalWorkDays.multiply(wage)
                    .add(totalPiecePay)
                    .subtract(totalLoan)
                    .subtract(totalFine)
                    .subtract(totalToolsOther);
            row.setBalance(balance);

            // Signature and remark from ledger_sign
            List<LedgerSign> empSigns = signMap.getOrDefault(emp.getId(), Collections.emptyList());
            String signature = empSigns.stream()
                    .map(LedgerSign::getSignature)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(","));
            String remark = empSigns.stream()
                    .map(LedgerSign::getRemark)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.joining(","));
            row.setSignature(signature);
            row.setRemark(remark);

            rows.add(row);
        }

        LedgerTableVO table = new LedgerTableVO();
        table.setMonths(months);
        table.setRows(rows);
        return table;
    }

    @Override
    public void exportExcel(Long projectId, String startMonth, String endMonth,
                            HttpServletResponse response) throws IOException {
        LedgerTableVO table = this.queryTable(projectId, startMonth, endMonth);
        Project project = projectMapper.selectById(projectId);
        String projectName = project != null ? project.getProjectName() : "未知项目";
        ExcelExportUtil.exportLedger(response, projectName, table.getMonths(), table.getRows());
    }

    @Override
    public void saveSign(LedgerSign sign) {
        // Upsert logic
        LedgerSign existing = ledgerSignMapper.selectOne(new LambdaQueryWrapper<LedgerSign>()
                .eq(LedgerSign::getEmployeeId, sign.getEmployeeId())
                .eq(LedgerSign::getProjectId, sign.getProjectId())
                .eq(LedgerSign::getYearMonth, sign.getYearMonth()));
        if (existing != null) {
            sign.setId(existing.getId());
            ledgerSignMapper.updateById(sign);
        } else {
            ledgerSignMapper.insert(sign);
        }
    }

    private BigDecimal nn(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
