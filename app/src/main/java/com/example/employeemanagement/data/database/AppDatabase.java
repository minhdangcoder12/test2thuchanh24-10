package com.example.employeemanagement.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.employeemanagement.data.dao.AccountDao;
import com.example.employeemanagement.data.dao.EmployeeDao;
import com.example.employeemanagement.data.dao.SalaryDao;
import com.example.employeemanagement.data.dao.WorkLogDao;
import com.example.employeemanagement.data.model.Account;
import com.example.employeemanagement.data.model.Employee;
import com.example.employeemanagement.data.model.Salary;
import com.example.employeemanagement.data.model.WorkLog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {Account.class, Employee.class, WorkLog.class, Salary.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AccountDao accountDao();
    public abstract EmployeeDao employeeDao();
    public abstract WorkLogDao workLogDao();
    public abstract SalaryDao salaryDao();

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "employee_management_db"
                    )
                    .addCallback(DemoDataSeeder.callback(context.getApplicationContext()))
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
