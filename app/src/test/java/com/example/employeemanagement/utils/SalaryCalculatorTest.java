package com.example.employeemanagement.utils;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SalaryCalculatorTest {

    @After
    public void resetOvertimeRate() {
        SalaryCalculator.setOvertimeRate(1.5);
    }

    @Test
    public void monthlySalaryUsesOvertimeOnlyAfter160Hours() {
        assertEquals(6_000_000, SalaryCalculator.calculateMonthlySalary(120, 50_000), 0.001);
        assertEquals(8_000_000, SalaryCalculator.calculateMonthlySalary(160, 50_000), 0.001);
        assertEquals(9_500_000, SalaryCalculator.calculateMonthlySalary(180, 50_000), 0.001);
        assertEquals(20, SalaryCalculator.calculateMonthlyOvertime(180), 0.001);
    }

    @Test
    public void configurableOvertimeRateChangesOvertimePay() {
        SalaryCalculator.setOvertimeRate(2.0);
        assertEquals(10_000_000, SalaryCalculator.calculateMonthlySalary(180, 50_000), 0.001);
        assertEquals(2_000_000, SalaryCalculator.calculateOvertimePay(180, 50_000), 0.001);
    }

    @Test
    public void checkOutBeforeCheckInDoesNotProduceHours() {
        assertEquals(9.5, SalaryCalculator.calculateHours("08:00:00", "17:30:00"), 0.001);
        assertEquals(0, SalaryCalculator.calculateHours("17:00:00", "08:00:00"), 0.001);
    }
}
