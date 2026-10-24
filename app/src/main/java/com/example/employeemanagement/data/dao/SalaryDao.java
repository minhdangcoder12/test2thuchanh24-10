package com.example.employeemanagement.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.employeemanagement.data.model.Salary;

import java.util.List;

@Dao
public interface SalaryDao {

    @Insert
    long insert(Salary salary);

    @Update
    void update(Salary salary);

    @Delete
    void delete(Salary salary);

    @Query("SELECT * FROM salary WHERE employeeId = :employeeId ORDER BY year DESC, month DESC")
    List<Salary> getByEmployee(int employeeId);

    @Query("SELECT * FROM salary WHERE employeeId = :employeeId AND month = :month AND year = :year LIMIT 1")
    Salary findByEmployeeMonthYear(int employeeId, int month, int year);

    @Query("SELECT * FROM salary WHERE id = :id LIMIT 1")
    Salary findById(int id);

    @Query("SELECT * FROM salary ORDER BY year DESC, month DESC")
    List<Salary> getAll();

    @Query("SELECT * FROM salary WHERE month = :month AND year = :year")
    List<Salary> getByMonthYear(int month, int year);

    @Query("UPDATE salary SET status = 'PAID' WHERE id = :id")
    void markAsPaid(int id);
}
