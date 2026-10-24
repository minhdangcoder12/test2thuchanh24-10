package com.example.employeemanagement.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "work_log",
    foreignKeys = @ForeignKey(
        entity = Employee.class,
        parentColumns = "id",
        childColumns = "employeeId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("employeeId")}
)
public class WorkLog {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int employeeId;
    private String workDate;       // Format: yyyy-MM-dd
    private String checkInTime;    // Format: HH:mm:ss
    private String checkOutTime;   // Format: HH:mm:ss
    private double totalHours;
    private String status;         // "PRESENT", "INCOMPLETE", "ABSENT"

    public WorkLog() {}

    public WorkLog(int employeeId, String workDate, String checkInTime) {
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.checkInTime = checkInTime;
        this.status = "INCOMPLETE";
        this.totalHours = 0;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getWorkDate() { return workDate; }
    public void setWorkDate(String workDate) { this.workDate = workDate; }

    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }

    public double getTotalHours() { return totalHours; }
    public void setTotalHours(double totalHours) { this.totalHours = totalHours; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
