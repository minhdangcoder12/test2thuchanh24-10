package com.example.employeemanagement.data.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteStatement;

import com.example.employeemanagement.utils.SalaryCalculator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Adds example records once, including on devices that already have the database. */
final class DemoDataSeeder {
    private static final String PREFS_NAME = "demo_data";
    private static final String SEEDED_KEY = "seeded_v1";

    private DemoDataSeeder() {}

    static RoomDatabase.Callback callback(Context context) {
        return new RoomDatabase.Callback() {
            @Override
            public void onOpen(SupportSQLiteDatabase db) {
                super.onOpen(db);
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                if (prefs.getBoolean(SEEDED_KEY, false)) return;

                db.beginTransaction();
                try {
                    if (findId(db, "SELECT id FROM account WHERE username = ? LIMIT 1", "admin") == -1) {
                        insert(db, "INSERT INTO account (username, password, role) VALUES (?, ?, ?)",
                                "admin", "admin123", "ADMIN");
                    }

                    LocalDate today = LocalDate.now();
                    addEmployee(db, today, "demo.an", "DEMO001", "Nguyễn Văn An",
                            "an.demo@example.com", "0000000001", 50000, 9, "08:00:00", "17:00:00");
                    addEmployee(db, today, "demo.binh", "DEMO002", "Trần Thị Bình",
                            "binh.demo@example.com", "0000000002", 60000, 8, "08:00:00", "16:00:00");
                    addEmployee(db, today, "demo.chi", "DEMO003", "Lê Minh Chi",
                            "chi.demo@example.com", "0000000003", 75000, 9, "09:00:00", "18:00:00");

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                prefs.edit().putBoolean(SEEDED_KEY, true).apply();
            }
        };
    }

    private static void addEmployee(SupportSQLiteDatabase db, LocalDate today, String username,
                                    String code, String name, String email, String phone,
                                    double hourlyRate, double dailyHours,
                                    String checkIn, String checkOut) {
        // Preserve a real account or employee that happens to use a demo identifier.
        if (findId(db, "SELECT id FROM account WHERE username = ? LIMIT 1", username) != -1
                || findId(db, "SELECT id FROM employee WHERE code = ? LIMIT 1", code) != -1) {
            return;
        }

        long accountId = insert(db, "INSERT INTO account (username, password, role) VALUES (?, ?, ?)",
                username, "demo123", "EMPLOYEE");
        long employeeId = insert(db,
                "INSERT INTO employee (code, fullname, email, phone, hourlyRate, accountId) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                code, name, email, phone, hourlyRate, accountId);

        LocalDate previousMonth = today.minusMonths(1).withDayOfMonth(1);
        seedMonth(db, employeeId, previousMonth, today, 20, dailyHours,
                checkIn, checkOut, hourlyRate, "PAID", false);
        seedMonth(db, employeeId, today.withDayOfMonth(1), today, 8, dailyHours,
                checkIn, checkOut, hourlyRate, "PENDING", true);
    }

    private static void seedMonth(SupportSQLiteDatabase db, long employeeId, LocalDate monthStart,
                                  LocalDate today, int maxDays, double dailyHours,
                                  String checkIn, String checkOut, double hourlyRate,
                                  String status, boolean recentDays) {
        List<LocalDate> weekdays = new ArrayList<>();
        for (LocalDate date = monthStart; date.getMonthValue() == monthStart.getMonthValue()
                && date.isBefore(today); date = date.plusDays(1)) {
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY
                    && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                weekdays.add(date);
            }
        }

        int from = recentDays ? Math.max(0, weekdays.size() - maxDays) : 0;
        int to = recentDays ? weekdays.size() : Math.min(maxDays, weekdays.size());
        double totalHours = 0;
        for (int i = from; i < to; i++) {
            insert(db, "INSERT INTO work_log "
                            + "(employeeId, workDate, checkInTime, checkOutTime, totalHours, status) "
                            + "VALUES (?, ?, ?, ?, ?, ?)",
                    employeeId, weekdays.get(i).format(DateTimeFormatter.ISO_LOCAL_DATE),
                    checkIn, checkOut, dailyHours, "PRESENT");
            totalHours += dailyHours;
        }

        if (totalHours > 0) {
            insert(db, "INSERT INTO salary "
                            + "(employeeId, month, year, totalHours, overtimeHours, totalSalary, status) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    employeeId, monthStart.getMonthValue(), monthStart.getYear(), totalHours,
                    SalaryCalculator.calculateMonthlyOvertime(totalHours),
                    SalaryCalculator.calculateMonthlySalary(totalHours, hourlyRate), status);
        }
    }

    private static long findId(SupportSQLiteDatabase db, String sql, String value) {
        try (Cursor cursor = db.query(sql, new Object[]{value})) {
            return cursor.moveToFirst() ? cursor.getLong(0) : -1;
        }
    }

    private static long insert(SupportSQLiteDatabase db, String sql, Object... values) {
        SupportSQLiteStatement statement = db.compileStatement(sql);
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (value instanceof String) statement.bindString(i + 1, (String) value);
            else if (value instanceof Double) statement.bindDouble(i + 1, (Double) value);
            else if (value instanceof Number) statement.bindLong(i + 1, ((Number) value).longValue());
        }
        return statement.executeInsert();
    }
}
