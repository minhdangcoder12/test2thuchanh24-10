package com.example.employeemanagement.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.employeemanagement.data.model.WorkLog;

import java.util.List;

@Dao
public interface WorkLogDao {

    @Insert
    long insert(WorkLog workLog);

    @Update
    void update(WorkLog workLog);

    @Delete
    void delete(WorkLog workLog);

    @Query("SELECT * FROM work_log WHERE employeeId = :employeeId ORDER BY workDate DESC")
    List<WorkLog> getByEmployee(int employeeId);

    @Query("SELECT * FROM work_log WHERE employeeId = :employeeId AND workDate = :date LIMIT 1")
    WorkLog findByEmployeeAndDate(int employeeId, String date);

    @Query("SELECT * FROM work_log WHERE id = :id LIMIT 1")
    WorkLog findById(int id);

    // Get all worklogs for a specific month and year
    @Query("SELECT * FROM work_log WHERE employeeId = :employeeId AND strftime('%m', workDate) = :month AND strftime('%Y', workDate) = :year")
    List<WorkLog> getByEmployeeAndMonth(int employeeId, String month, String year);

    // Sum total hours for a specific month
    @Query("SELECT COALESCE(SUM(totalHours), 0) FROM work_log WHERE employeeId = :employeeId AND strftime('%m', workDate) = :month AND strftime('%Y', workDate) = :year AND status = 'PRESENT'")
    double sumHoursByMonth(int employeeId, String month, String year);

    // Get all worklogs (for admin reports)
    @Query("SELECT * FROM work_log ORDER BY workDate DESC")
    List<WorkLog> getAll();

    // Get all worklogs by date for admin
    @Query("SELECT * FROM work_log WHERE workDate = :date")
    List<WorkLog> getByDate(String date);
}
