package com.example.employeemanagement;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.employeemanagement.utils.SalaryCalculator;

/**
 * Unit Test cho SalaryCalculator.
 * Kiểm tra các trường hợp tính giờ, OT và lương.
 *
 * Người 2 — Nhiệm vụ 5: Tính giờ tự động + test
 * Chạy bằng: Run > SalaryCalculatorTest trong Android Studio
 */
public class SalaryCalculatorTest {

    // ============================================================
    // calculateHours() tests
    // ============================================================

    @Test
    public void calculateHours_normalShift_returnsCorrectHours() {
        // 08:00 → 17:00 = 9h
        double hours = SalaryCalculator.calculateHours("08:00:00", "17:00:00");
        assertEquals(9.0, hours, 0.001);
    }

    @Test
    public void calculateHours_halfHour_returnsPointFive() {
        // 08:00 → 08:30 = 0.5h
        double hours = SalaryCalculator.calculateHours("08:00:00", "08:30:00");
        assertEquals(0.5, hours, 0.001);
    }

    @Test
    public void calculateHours_checkoutBeforeCheckin_returnsZero() {
        // Giờ ra trước giờ vào → 0
        double hours = SalaryCalculator.calculateHours("17:00:00", "08:00:00");
        assertEquals(0.0, hours, 0.001);
    }

    @Test
    public void calculateHours_sameTime_returnsZero() {
        // Giờ vào = giờ ra → 0
        double hours = SalaryCalculator.calculateHours("08:00:00", "08:00:00");
        assertEquals(0.0, hours, 0.001);
    }

    @Test
    public void calculateHours_nullInput_returnsZero() {
        assertEquals(0.0, SalaryCalculator.calculateHours(null, "17:00:00"), 0.001);
        assertEquals(0.0, SalaryCalculator.calculateHours("08:00:00", null), 0.001);
        assertEquals(0.0, SalaryCalculator.calculateHours(null, null), 0.001);
    }

    @Test
    public void calculateHours_emptyInput_returnsZero() {
        assertEquals(0.0, SalaryCalculator.calculateHours("", "17:00:00"), 0.001);
    }

    @Test
    public void calculateHours_8Hours_returns8() {
        // Ca làm chuẩn 8h
        double hours = SalaryCalculator.calculateHours("08:00:00", "16:00:00");
        assertEquals(8.0, hours, 0.001);
    }

    // ============================================================
    // calculateOvertimeHours() tests (daily OT)
    // ============================================================

    @Test
    public void calculateOvertimeHours_under8h_returnsZero() {
        assertEquals(0.0, SalaryCalculator.calculateOvertimeHours(7.5), 0.001);
    }

    @Test
    public void calculateOvertimeHours_exactly8h_returnsZero() {
        assertEquals(0.0, SalaryCalculator.calculateOvertimeHours(8.0), 0.001);
    }

    @Test
    public void calculateOvertimeHours_over8h_returnsCorrect() {
        // 10h làm → 2h OT
        assertEquals(2.0, SalaryCalculator.calculateOvertimeHours(10.0), 0.001);
    }

    // ============================================================
    // calculateMonthlyOvertime() tests
    // ============================================================

    @Test
    public void calculateMonthlyOvertime_under160h_returnsZero() {
        assertEquals(0.0, SalaryCalculator.calculateMonthlyOvertime(150.0), 0.001);
    }

    @Test
    public void calculateMonthlyOvertime_exactly160h_returnsZero() {
        assertEquals(0.0, SalaryCalculator.calculateMonthlyOvertime(160.0), 0.001);
    }

    @Test
    public void calculateMonthlyOvertime_over160h_returnsCorrect() {
        // 180h làm → 20h OT
        assertEquals(20.0, SalaryCalculator.calculateMonthlyOvertime(180.0), 0.001);
    }

    // ============================================================
    // calculateMonthlySalary() tests
    // ============================================================

    @Test
    public void calculateMonthlySalary_normalHours_noOT() {
        // 160h × 50,000đ = 8,000,000đ
        double salary = SalaryCalculator.calculateMonthlySalary(160.0, 50000.0);
        assertEquals(8_000_000.0, salary, 0.01);
    }

    @Test
    public void calculateMonthlySalary_withOT_correct() {
        // 170h: 160h × 50,000 + 10h × 50,000 × 1.5
        // = 8,000,000 + 750,000 = 8,750,000
        double salary = SalaryCalculator.calculateMonthlySalary(170.0, 50000.0);
        assertEquals(8_750_000.0, salary, 0.01);
    }

    @Test
    public void calculateMonthlySalary_zeroHours_returnsZero() {
        assertEquals(0.0, SalaryCalculator.calculateMonthlySalary(0, 50000.0), 0.01);
    }

    @Test
    public void calculateMonthlySalary_zeroRate_returnsZero() {
        assertEquals(0.0, SalaryCalculator.calculateMonthlySalary(160.0, 0), 0.01);
    }

    // ============================================================
    // formatHours() tests
    // ============================================================

    @Test
    public void formatHours_exactHour() {
        assertEquals("8h 0m", SalaryCalculator.formatHours(8.0));
    }

    @Test
    public void formatHours_halfHour() {
        assertEquals("8h 30m", SalaryCalculator.formatHours(8.5));
    }

    @Test
    public void formatHours_zero() {
        assertEquals("0h 0m", SalaryCalculator.formatHours(0.0));
    }

    @Test
    public void formatHours_negative_returnsZero() {
        assertEquals("0h 0m", SalaryCalculator.formatHours(-1.0));
    }

    // ============================================================
    // formatCurrency() tests
    // ============================================================

    @Test
    public void formatCurrency_millions() {
        // 5,000,000 VND
        String result = SalaryCalculator.formatCurrency(5_000_000.0);
        assertTrue(result.contains("5,000,000"));
        assertTrue(result.contains("VND"));
    }
}
