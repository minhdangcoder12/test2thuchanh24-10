package com.example.employeemanagement.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Tiện ích tính lương và giờ làm việc.
 *
 * Công thức lương:
 *   Giờ thường (≤160h/tháng): totalHours × hourlyRate
 *   Giờ OT (>160h/tháng):     160 × hourlyRate + (totalHours - 160) × hourlyRate × 1.5
 *
 * Người 2 — Nhiệm vụ 5: Kiểm tra và hoàn thiện SalaryCalculator
 */
public class SalaryCalculator {

    /** Giờ chuẩn mỗi ngày (8 tiếng) */
    public static final double STANDARD_HOURS_PER_DAY = 8.0;

    /** Tỷ lệ lương OT (1.5x) */
    public static final double OVERTIME_RATE = 1.5;

    /** Giờ chuẩn mỗi tháng (160h = 20 ngày × 8h) */
    public static final double STANDARD_MONTHLY_HOURS = 160.0;

    /**
     * Tính số giờ làm việc giữa giờ vào và giờ ra.
     *
     * @param checkIn  Giờ vào dạng "HH:mm:ss"
     * @param checkOut Giờ ra dạng "HH:mm:ss"
     * @return Số giờ làm việc (double), trả về 0 nếu không hợp lệ
     */
    public static double calculateHours(String checkIn, String checkOut) {
        if (checkIn == null || checkIn.isEmpty()
                || checkOut == null || checkOut.isEmpty()) {
            return 0;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            sdf.setLenient(false); // Không chấp nhận giờ không hợp lệ (vd: 25:00:00)
            Date inTime  = sdf.parse(checkIn);
            Date outTime = sdf.parse(checkOut);
            if (inTime == null || outTime == null) return 0;
            long diffMs = outTime.getTime() - inTime.getTime();
            if (diffMs <= 0) return 0; // Giờ ra phải sau giờ vào
            return diffMs / (1000.0 * 60 * 60);
        } catch (ParseException e) {
            return 0; // Định dạng không hợp lệ
        }
    }

    /**
     * Tính giờ OT trong ngày (phần vượt quá 8h/ngày).
     *
     * @param totalHours Tổng giờ làm trong ngày
     * @return Giờ OT (>= 0)
     */
    public static double calculateOvertimeHours(double totalHours) {
        return Math.max(0, totalHours - STANDARD_HOURS_PER_DAY);
    }

    /**
     * Tính lương tháng theo công thức:
     *   - ≤ 160h: lương = totalHours × hourlyRate
     *   - > 160h: lương = 160 × hourlyRate + (totalHours - 160) × hourlyRate × 1.5
     *
     * @param totalHours Tổng giờ làm trong tháng
     * @param hourlyRate Đơn giá giờ của nhân viên
     * @return Tổng lương (double)
     */
    public static double calculateMonthlySalary(double totalHours, double hourlyRate) {
        if (totalHours <= 0 || hourlyRate <= 0) return 0;
        if (totalHours > STANDARD_MONTHLY_HOURS) {
            double normalPay   = STANDARD_MONTHLY_HOURS * hourlyRate;
            double otHours     = totalHours - STANDARD_MONTHLY_HOURS;
            double overtimePay = otHours * hourlyRate * OVERTIME_RATE;
            return normalPay + overtimePay;
        } else {
            return totalHours * hourlyRate;
        }
    }

    /**
     * Tính số giờ OT trong tháng (phần vượt quá 160h/tháng).
     *
     * @param totalHours Tổng giờ làm trong tháng
     * @return Giờ OT tháng (>= 0)
     */
    public static double calculateMonthlyOvertime(double totalHours) {
        return Math.max(0, totalHours - STANDARD_MONTHLY_HOURS);
    }

    /**
     * Tính lương thường (phần không tính OT).
     *
     * @param totalHours Tổng giờ làm trong tháng
     * @param hourlyRate Đơn giá giờ
     * @return Lương thường
     */
    public static double calculateNormalPay(double totalHours, double hourlyRate) {
        double normalHours = Math.min(totalHours, STANDARD_MONTHLY_HOURS);
        return normalHours * hourlyRate;
    }

    /**
     * Tính lương OT.
     *
     * @param totalHours Tổng giờ làm trong tháng
     * @param hourlyRate Đơn giá giờ
     * @return Lương OT
     */
    public static double calculateOvertimePay(double totalHours, double hourlyRate) {
        double otHours = calculateMonthlyOvertime(totalHours);
        return otHours * hourlyRate * OVERTIME_RATE;
    }

    /**
     * Định dạng số tiền sang chuỗi VND.
     * Ví dụ: 5000000.0 → "5,000,000 VND"
     */
    public static String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount) + " VND";
    }

    /**
     * Định dạng giờ sang chuỗi dễ đọc.
     * Ví dụ: 8.5 → "8h 30m"
     */
    public static String formatHours(double hours) {
        if (hours < 0) hours = 0;
        int h = (int) hours;
        int m = (int) Math.round((hours - h) * 60);
        if (m == 60) { h++; m = 0; } // Làm tròn nếu cần
        return h + "h " + m + "m";
    }
}
