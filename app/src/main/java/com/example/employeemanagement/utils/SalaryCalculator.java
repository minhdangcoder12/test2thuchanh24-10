package com.example.employeemanagement.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Shared working-hours and monthly-salary calculations. */
public class SalaryCalculator {

    public static final double STANDARD_HOURS_PER_DAY = 8.0;
    public static final double STANDARD_MONTHLY_HOURS = 160.0;
    public static double OVERTIME_RATE = 1.5;

    public static void setOvertimeRate(double rate) {
        if (!Double.isFinite(rate) || rate <= 1.0) {
            throw new IllegalArgumentException("Tỷ lệ OT phải lớn hơn 1.0");
        }
        OVERTIME_RATE = rate;
    }

    public static double getOvertimeRate() {
        return OVERTIME_RATE;
    }

    /** Returns zero for missing, invalid or non-increasing times. */
    public static double calculateHours(String checkIn, String checkOut) {
        if (checkIn == null || checkIn.isEmpty() || checkOut == null || checkOut.isEmpty()) {
            return 0;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            format.setLenient(false);
            Date inTime = format.parse(checkIn);
            Date outTime = format.parse(checkOut);
            if (inTime == null || outTime == null) return 0;
            long difference = outTime.getTime() - inTime.getTime();
            return difference > 0 ? difference / 3_600_000.0 : 0;
        } catch (ParseException e) {
            return 0;
        }
    }

    public static double calculateOvertimeHours(double totalHours) {
        return Math.max(0, totalHours - STANDARD_HOURS_PER_DAY);
    }

    public static double calculateMonthlyNormalHours(double totalHours) {
        return Math.max(0, Math.min(totalHours, STANDARD_MONTHLY_HOURS));
    }

    public static double calculateMonthlyOvertime(double totalHours) {
        return Math.max(0, totalHours - STANDARD_MONTHLY_HOURS);
    }

    public static double calculateNormalPay(double totalHours, double hourlyRate) {
        if (hourlyRate <= 0) return 0;
        return calculateMonthlyNormalHours(totalHours) * hourlyRate;
    }

    public static double calculateOvertimePay(double totalHours, double hourlyRate) {
        if (hourlyRate <= 0) return 0;
        return calculateMonthlyOvertime(totalHours) * hourlyRate * OVERTIME_RATE;
    }

    public static double calculateMonthlySalary(double totalHours, double hourlyRate) {
        return calculateNormalPay(totalHours, hourlyRate)
                + calculateOvertimePay(totalHours, hourlyRate);
    }

    public static String formatCurrency(double amount) {
        if (amount <= 0) return "0 VND";
        return new DecimalFormat("#,###").format(amount) + " VND";
    }

    public static String formatHours(double hours) {
        if (hours <= 0) return "0h 0m";
        int wholeHours = (int) hours;
        int minutes = (int) Math.round((hours - wholeHours) * 60);
        if (minutes == 60) {
            wholeHours++;
            minutes = 0;
        }
        return wholeHours + "h " + minutes + "m";
    }
}
