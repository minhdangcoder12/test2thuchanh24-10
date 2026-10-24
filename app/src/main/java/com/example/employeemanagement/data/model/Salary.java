package com.example.employeemanagement.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "salary",
    foreignKeys = @ForeignKey(
        entity = Employee.class,
        parentColumns = "id",
        childColumns = "employeeId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("employeeId")}
)
public class Salary {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int employeeId;
    private int month;
    private int year;
    private double totalHours;
    private double overtimeHours;
    private double totalSalary;
    private String status; // "PENDING", "PAID"

    public Salary() {}

    public Salary(int employeeId, int month, int year) {
        this.employeeId = employeeId;
        this.month = month;
        this.year = year;
        this.totalHours = 0;
        this.overtimeHours = 0;
        this.totalSalary = 0;
        this.status = "PENDING";
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getTotalHours() { return totalHours; }
    public void setTotalHours(double totalHours) { this.totalHours = totalHours; }

    public double getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(double overtimeHours) { this.overtimeHours = overtimeHours; }

    public double getTotalSalary() { return totalSalary; }
    public void setTotalSalary(double totalSalary) { this.totalSalary = totalSalary; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
