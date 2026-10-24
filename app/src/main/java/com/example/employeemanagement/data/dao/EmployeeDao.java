package com.example.employeemanagement.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.employeemanagement.data.model.Employee;

import java.util.List;

@Dao
public interface EmployeeDao {

    @Insert
    long insert(Employee employee);

    @Update
    void update(Employee employee);

    @Delete
    void delete(Employee employee);

    @Query("SELECT * FROM employee ORDER BY fullname ASC")
    List<Employee> getAll();

    @Query("SELECT * FROM employee WHERE id = :id LIMIT 1")
    Employee findById(int id);

    @Query("SELECT * FROM employee WHERE accountId = :accountId LIMIT 1")
    Employee findByAccountId(int accountId);

    @Query("SELECT * FROM employee WHERE code = :code LIMIT 1")
    Employee findByCode(String code);

    @Query("SELECT * FROM employee WHERE fullname LIKE '%' || :keyword || '%' OR code LIKE '%' || :keyword || '%' OR email LIKE '%' || :keyword || '%'")
    List<Employee> search(String keyword);

    @Query("DELETE FROM employee WHERE id = :id")
    void deleteById(int id);
}
