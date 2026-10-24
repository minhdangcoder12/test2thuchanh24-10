package com.example.employeemanagement.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * SalaryCalculator — Nhiệm vụ 5 (Người 3)
 *
 * Công thức lương:
 *   Lương = Giờ_thường × Đơn_giá + Giờ_OT × Đơn_giá × OT_RATE
 *   Giờ OT = Tổng_giờ - STANDARD_MONTHLY_HOURS (nếu > 160h/tháng)
 *
 * Tỷ lệ OT có thể tùy chỉnh qua setOvertimeRate().
 */
public class SalaryCalculator {

    // Giờ tiêu chuẩn mỗi ngày
    public static final double STANDARD_HOURS_PER_DAY = 8.0;

    // Giờ tiêu chuẩn mỗi tháng (20 ngày × 8h)
    public static final double STANDARD_MONTHLY_HOURS = 160.0;

    // Tỷ lệ nhân lương OT (mặc định 1.5x, có thể tùy chỉnh)
    public static double OVERTIME_RATE = 1.5;

    // ---------------------------------------------------------------
    // Nhiệm vụ 5: Tùy chỉnh tỷ lệ OT
    // ---------------------------------------------------------------

    /**
     * Đặt tỷ lệ nhân lương OT mới.
     * @param rate Tỷ lệ mới (ví dụ: 1.5, 2.0, 3.0...)
     * @throws IllegalArgumentException nếu rate <= 1.0
     */
    public static void setOvertimeRate(double rate) {
        if (rate <= 1.0) {
            throw new IllegalArgumentException("Tỷ lệ OT phải lớn hơn 1.0 (thường là 1.5)");
        }
        OVERTIME_RATE = rate;
    }

    /**
     * Lấy tỷ lệ OT hiện tại.
     */
    public static double getOvertimeRate() {
        return OVERTIME_RATE;
    }

    // ---------------------------------------------------------------
    // Tính giờ từ check-in / check-out
    // ---------------------------------------------------------------

    /**
     * Tính số giờ làm việc giữa checkIn và checkOut.
     * @param checkIn  Định dạng: HH:mm:ss
     * @param checkOut Định dạng: HH:mm:ss
     * @return Số giờ (double), 0 nếu không hợp lệ
     */
    public static double calculateHours(String checkIn, String checkOut) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Date inTime = sdf.parse(checkIn);
            Date outTime = sdf.parse(checkOut);
            if (inTime == null || outTime == null) return 0;
            long diffMs = outTime.getTime() - inTime.getTime();
            if (diffMs < 0) return 0; // check-out trước check-in
            return diffMs / (1000.0 * 60 * 60);
        } catch (Exception e) {
            return 0;
        }
    }

    // ---------------------------------------------------------------
    // Tính OT trong ngày (vượt 8h/ngày)
    // ---------------------------------------------------------------

    /**
     * Tính giờ OT trong một ngày (vượt quá 8h/ngày).
     */
    public static double calculateOvertimeHours(double totalHoursInDay) {
        return Math.max(0, totalHoursInDay - STANDARD_HOURS_PER_DAY);
    }

    // ---------------------------------------------------------------
    // Tính lương tháng (Nhiệm vụ 1 & 5)
    // ---------------------------------------------------------------

    /**
     * Tính tổng lương tháng theo công thức:
     *   - 160h đầu: giờ × đơn_giá
     *   - Vượt 160h: giờ × đơn_giá × OVERTIME_RATE
     *
     * @param totalHours  Tổng giờ làm trong tháng
     * @param hourlyRate  Đơn giá/giờ của nhân viên
     * @return Tổng lương tháng
     */
    public static double calculateMonthlySalary(double totalHours, double hourlyRate) {
        if (totalHours <= 0 || hourlyRate <= 0) return 0;

        if (totalHours <= STANDARD_MONTHLY_HOURS) {
            // Toàn bộ là giờ thường
            return totalHours * hourlyRate;
        } else {
            // Tách thành giờ thường và giờ OT
            double normalPay = STANDARD_MONTHLY_HOURS * hourlyRate;
            double otHours = totalHours - STANDARD_MONTHLY_HOURS;
            double otPay = otHours * hourlyRate * OVERTIME_RATE;
            return normalPay + otPay;
        }
    }

    /**
     * Tính giờ OT trong tháng (vượt 160h/tháng).
     */
    public static double calculateMonthlyOvertime(double totalHours) {
        return Math.max(0, totalHours - STANDARD_MONTHLY_HOURS);
    }

    /**
     * Tính giờ thường trong tháng (tối đa 160h).
     */
    public static double calculateMonthlyNormalHours(double totalHours) {
        return Math.min(totalHours, STANDARD_MONTHLY_HOURS);
    }

    /**
     * Tính tiền lương phần giờ thường.
     */
    public static double calculateNormalPay(double totalHours, double hourlyRate) {
        double normalHours = calculateMonthlyNormalHours(totalHours);
        return normalHours * hourlyRate;
    }

    /**
     * Tính tiền lương phần OT.
     */
    public static double calculateOvertimePay(double totalHours, double hourlyRate) {
        double otHours = calculateMonthlyOvertime(totalHours);
        return otHours * hourlyRate * OVERTIME_RATE;
    }

    // ---------------------------------------------------------------
    // Format hiển thị
    // ---------------------------------------------------------------

    /**
     * Format số tiền thành chuỗi VND.
     * Ví dụ: 12500000 → "12,500,000 VND"
     */
    public static String formatCurrency(double amount) {
        if (amount <= 0) return "0 VND";
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount) + " VND";
    }

    /**
     * Format giờ thành chuỗi dễ đọc.
     * Ví dụ: 8.5 → "8h 30m"
     */
    public static String formatHours(double hours) {
        if (hours <= 0) return "0h 0m";
        int h = (int) hours;
        int m = (int) Math.round((hours - h) * 60);
        if (m == 60) { h++; m = 0; } // xử lý làm tròn
        return h + "h " + m + "m";
    }

    // ---------------------------------------------------------------
    // Test cases — Nhiệm vụ 5 (kiểm tra nhiều trường hợp)
    // ---------------------------------------------------------------

    /**
     * Chạy các test case kiểm tra tính đúng của công thức.
     * Trả về chuỗi kết quả để log hoặc hiển thị.
     */
    public static String runSelfTest() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SalaryCalculator Self Test ===\n\n");
        double savedRate = OVERTIME_RATE;

        // Test 1: Dưới 160h — không có OT
        double t1 = calculateMonthlySalary(120, 50000);
        sb.append("Test 1 (120h, 50k/h) → ").append(formatCurrency(t1))
          .append(t1 == 6_000_000 ? " ✅" : " ❌ (expected 6,000,000)").append("\n");

        // Test 2: Đúng 160h — không có OT
        double t2 = calculateMonthlySalary(160, 50000);
        sb.append("Test 2 (160h, 50k/h) → ").append(formatCurrency(t2))
          .append(t2 == 8_000_000 ? " ✅" : " ❌ (expected 8,000,000)").append("\n");

        // Test 3: 180h — có 20h OT @ 1.5x
        double t3 = calculateMonthlySalary(180, 50000);
        double expected3 = 160 * 50000 + 20 * 50000 * 1.5;
        sb.append("Test 3 (180h, 50k/h, OT 1.5x) → ").append(formatCurrency(t3))
          .append(Math.abs(t3 - expected3) < 0.01 ? " ✅" : " ❌ (expected " + formatCurrency(expected3) + ")").append("\n");

        // Test 4: OT rate tùy chỉnh 2.0x
        setOvertimeRate(2.0);
        double t4 = calculateMonthlySalary(180, 50000);
        double expected4 = 160 * 50000 + 20 * 50000 * 2.0;
        sb.append("Test 4 (180h, 50k/h, OT 2.0x) → ").append(formatCurrency(t4))
          .append(Math.abs(t4 - expected4) < 0.01 ? " ✅" : " ❌ (expected " + formatCurrency(expected4) + ")").append("\n");

        // Test 5: Tính giờ từ check-in/out
        double t5 = calculateHours("08:00:00", "17:30:00");
        sb.append("Test 5 (08:00 → 17:30) → ").append(formatHours(t5))
          .append(Math.abs(t5 - 9.5) < 0.01 ? " ✅" : " ❌ (expected 9h 30m)").append("\n");

        // Test 6: Check-out trước check-in → 0
        double t6 = calculateHours("17:00:00", "08:00:00");
        sb.append("Test 6 (17:00 → 08:00 invalid) → ").append(formatHours(t6))
          .append(t6 == 0 ? " ✅" : " ❌ (expected 0)").append("\n");

        // Test 7: Format giờ
        sb.append("Test 7 formatHours(8.5) → ").append(formatHours(8.5))
          .append(" (expected: 8h 30m)\n");

        // Test 8: Format tiền
        sb.append("Test 8 formatCurrency(12500000) → ").append(formatCurrency(12_500_000))
          .append(" (expected: 12,500,000 VND)\n");

        // Khôi phục rate gốc
        OVERTIME_RATE = savedRate;

        sb.append("\n=== Tests hoàn thành ===");
        return sb.toString();
    }
}
