package com.example.employeemanagement.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SalaryCalculator {

    // Standard working hours per day
    public static final double STANDARD_HOURS_PER_DAY = 8.0;
    // Overtime multiplier (1.5x for weekdays, can be extended)
    public static final double OVERTIME_RATE = 1.5;

    /**
     * Calculate total hours between check-in and check-out times.
     * @param checkIn  Format: HH:mm:ss
     * @param checkOut Format: HH:mm:ss
     * @return hours worked (double)
     */
    public static double calculateHours(String checkIn, String checkOut) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Date inTime = sdf.parse(checkIn);
            Date outTime = sdf.parse(checkOut);
            if (inTime == null || outTime == null) return 0;
            long diffMs = outTime.getTime() - inTime.getTime();
            if (diffMs < 0) return 0; // Invalid checkout time
            return diffMs / (1000.0 * 60 * 60);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Calculate overtime hours given total hours worked in a day.
     */
    public static double calculateOvertimeHours(double totalHours) {
        return Math.max(0, totalHours - STANDARD_HOURS_PER_DAY);
    }

    /**
     * Calculate monthly salary.
     * @param totalHours    Total hours worked this month
     * @param hourlyRate    Employee's hourly rate
     * @return total salary
     */
    public static double calculateMonthlySalary(double totalHours, double hourlyRate) {
        // Standard hours (up to 8h/day * workdays)
        double overtimeHours = 0;
        // Simple monthly: first 160 hours normal, rest overtime
        double standardMonthlyHours = 160.0; // ~20 working days x 8h
        if (totalHours > standardMonthlyHours) {
            overtimeHours = totalHours - standardMonthlyHours;
            double normalPay = standardMonthlyHours * hourlyRate;
            double overtimePay = overtimeHours * hourlyRate * OVERTIME_RATE;
            return normalPay + overtimePay;
        } else {
            return totalHours * hourlyRate;
        }
    }

    /**
     * Calculate monthly overtime hours (hours beyond 160h/month).
     */
    public static double calculateMonthlyOvertime(double totalHours) {
        return Math.max(0, totalHours - 160.0);
    }

    /**
     * Format currency value to readable string (VND)
     */
    public static String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount) + " VND";
    }

    /**
     * Format hours to readable string.
     */
    public static String formatHours(double hours) {
        int h = (int) hours;
        int m = (int) ((hours - h) * 60);
        return h + "h " + m + "m";
    }
}
