package com.example.employeemanagement.ui.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.database.AppDatabase;
import com.example.employeemanagement.data.model.Employee;
import com.example.employeemanagement.data.model.Salary;
import com.example.employeemanagement.data.model.WorkLog;
import com.example.employeemanagement.ui.adapter.SalaryAdapter;
import com.example.employeemanagement.utils.SalaryCalculator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SalaryManagementActivity extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerYear;
    private Button btnCalculate, btnLoadSalaries;
    private RecyclerView recyclerView;
    private SalaryAdapter adapter;
    private List<Salary> salaryList = new ArrayList<>();
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_management);

        db = AppDatabase.getDatabase(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý lương");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnLoadSalaries = findViewById(R.id.btnLoadSalaries);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SalaryAdapter(salaryList, this);
        recyclerView.setAdapter(adapter);

        // Setup month spinner (1-12)
        String[] months = {"Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
                           "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"};
        spinnerMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months));

        // Setup year spinner (current year -2 to +1)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = {String.valueOf(currentYear - 1), String.valueOf(currentYear), String.valueOf(currentYear + 1)};
        spinnerYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years));
        spinnerYear.setSelection(1); // Default current year

        // Default selection to current month
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        spinnerMonth.setSelection(currentMonth);

        btnCalculate.setOnClickListener(v -> calculateSalariesForAll());
        btnLoadSalaries.setOnClickListener(v -> loadSalaries());
    }

    private void calculateSalariesForAll() {
        int month = spinnerMonth.getSelectedItemPosition() + 1;
        int year = Integer.parseInt((String) spinnerYear.getSelectedItem());
        String monthStr = String.format("%02d", month);
        String yearStr = String.valueOf(year);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Employee> employees = db.employeeDao().getAll();
            for (Employee emp : employees) {
                double totalHours = db.workLogDao().sumHoursByMonth(emp.getId(), monthStr, yearStr);
                double overtimeHours = SalaryCalculator.calculateMonthlyOvertime(totalHours);
                double totalSalary = SalaryCalculator.calculateMonthlySalary(totalHours, emp.getHourlyRate());

                Salary existing = db.salaryDao().findByEmployeeMonthYear(emp.getId(), month, year);
                if (existing != null) {
                    existing.setTotalHours(totalHours);
                    existing.setOvertimeHours(overtimeHours);
                    existing.setTotalSalary(totalSalary);
                    db.salaryDao().update(existing);
                } else {
                    Salary salary = new Salary(emp.getId(), month, year);
                    salary.setTotalHours(totalHours);
                    salary.setOvertimeHours(overtimeHours);
                    salary.setTotalSalary(totalSalary);
                    db.salaryDao().insert(salary);
                }
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã tính lương cho tháng " + month + "/" + year, Toast.LENGTH_SHORT).show();
                loadSalaries();
            });
        });
    }

    private void loadSalaries() {
        int month = spinnerMonth.getSelectedItemPosition() + 1;
        int year = Integer.parseInt((String) spinnerYear.getSelectedItem());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Salary> list = db.salaryDao().getByMonthYear(month, year);
            runOnUiThread(() -> {
                salaryList.clear();
                salaryList.addAll(list);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSalaries();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
