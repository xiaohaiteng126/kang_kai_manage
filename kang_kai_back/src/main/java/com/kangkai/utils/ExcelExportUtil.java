package com.kangkai.utils;

import com.kangkai.vo.LedgerRowVO;
import com.kangkai.vo.MonthDetailVO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExportUtil {

    public static void exportLedger(HttpServletResponse response, String projectName,
                                     List<String> months, List<LedgerRowVO> rows) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = projectName + "台账明细" + timestamp + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, "UTF-8").replace("+", "%20"));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("台账明细");

        // Create styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        // Write headers (2 rows)
        int rowIdx = 0;
        Row headerRow1 = sheet.createRow(rowIdx++);
        Row headerRow2 = sheet.createRow(rowIdx++);

        int col = 0;

        // Fixed columns (row 1)
        String[] fixedHeaders = {"序号", "姓名", "总工日", "日资", "总计件"};
        for (String h : fixedHeaders) {
            Cell cell1 = headerRow1.createCell(col);
            cell1.setCellValue(h);
            cell1.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));

            Cell cell2 = headerRow2.createCell(col);
            cell2.setCellStyle(headerStyle);
            col++;
        }

        // Month columns (merged top row, 6 sub-columns each)
        for (String month : months) {
            // Month header (merged across 6 cols)
            Cell monthCell = headerRow1.createCell(col);
            monthCell.setCellValue(month);
            monthCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, col, col + 5));

            // Sub-headers in row 2
            String[] subHeaders = {"工日", "计件", "借支", "罚款", "工具/其他", "备注"};
            for (String sh : subHeaders) {
                Cell subCell = headerRow2.createCell(col);
                subCell.setCellValue(sh);
                subCell.setCellStyle(headerStyle);
                col++;
            }
        }

        // Trailing fixed columns
        String[] trailHeaders = {"总借支", "总罚款", "工具/其他", "余额", "签字", "备注"};
        for (String h : trailHeaders) {
            Cell cell1 = headerRow1.createCell(col);
            cell1.setCellValue(h);
            cell1.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));

            Cell cell2 = headerRow2.createCell(col);
            cell2.setCellStyle(headerStyle);
            col++;
        }

        // Write data rows
        for (LedgerRowVO row : rows) {
            Row dataRow = sheet.createRow(rowIdx++);
            int dc = 0;

            createDataCell(dataRow, dc++, row.getRowNo(), dataStyle);
            createDataCell(dataRow, dc++, row.getEmployeeName(), dataStyle);
            createDataCell(dataRow, dc++, toDouble(row.getTotalWorkDays()), dataStyle);
            createDataCell(dataRow, dc++, toDouble(row.getDailyWage()), dataStyle);
            createDataCell(dataRow, dc++, toDouble(row.getTotalPiecePay()), dataStyle);

            for (MonthDetailVO md : row.getMonths()) {
                createDataCell(dataRow, dc++, toDouble(md.getWorkDays()), dataStyle);
                createDataCell(dataRow, dc++, toDouble(md.getPiecePay()), dataStyle);
                createDataCell(dataRow, dc++, toDouble(md.getLoan()), dataStyle);
                createDataCell(dataRow, dc++, toDouble(md.getFine()), dataStyle);
                createDataCell(dataRow, dc++, toDouble(md.getToolsOther()), dataStyle);
                createDataCell(dataRow, dc++, md.getRemark(), dataStyle);
            }

            createDataCell(dataRow, dc++, toDouble(row.getTotalLoan()), dataStyle);
            createDataCell(dataRow, dc++, toDouble(row.getTotalFine()), dataStyle);
            createDataCell(dataRow, dc++, toDouble(row.getTotalToolsOther()), dataStyle);
            createDataCell(dataRow, dc++, toDouble(row.getBalance()), dataStyle);
            createDataCell(dataRow, dc++, row.getSignature(), dataStyle);
            createDataCell(dataRow, dc++, row.getRemark(), dataStyle);
        }

        // Auto-size columns
        int totalCols = 11 + months.size() * 6;
        for (int i = 0; i < totalCols; i++) {
            sheet.setColumnWidth(i, 14 * 256);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static void createDataCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(style);
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value != null) {
            cell.setCellValue(value.toString());
        }
    }

    private static Double toDouble(java.math.BigDecimal val) {
        return val != null ? val.doubleValue() : 0.0;
    }
}
