package com.example.employeemanagement.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.employeemanagement.data.model.Account;

import java.util.List;

@Dao
public interface AccountDao {

    @Insert
    long insert(Account account);

    @Update
    void update(Account account);

    @Delete
    void delete(Account account);

    @Query("SELECT * FROM account WHERE username = :username AND password = :password LIMIT 1")
    Account login(String username, String password);

    @Query("SELECT * FROM account WHERE username = :username LIMIT 1")
    Account findByUsername(String username);

    @Query("SELECT * FROM account WHERE id = :id LIMIT 1")
    Account findById(int id);

    @Query("SELECT * FROM account")
    List<Account> getAll();

    @Query("DELETE FROM account WHERE id = :id")
    void deleteById(int id);
}
