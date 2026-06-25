package com.kangkai.utils;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public static List<String> generateMonths(String startMonth, String endMonth) {
        YearMonth start = YearMonth.parse(startMonth, FORMATTER);
        YearMonth end = YearMonth.parse(endMonth, FORMATTER);
        List<String> months = new ArrayList<>();
        while (!start.isAfter(end)) {
            months.add(start.format(FORMATTER));
            start = start.plusMonths(1);
        }
        return months;
    }

    public static String formatYearMonth(YearMonth ym) {
        return ym.format(FORMATTER);
    }
}
