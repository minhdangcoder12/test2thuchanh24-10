package com.example.employeemanagement.ui.employee;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.database.AppDatabase;
import com.example.employeemanagement.data.model.Salary;
import com.example.employeemanagement.utils.SalaryCalculator;
import com.example.employeemanagement.utils.SessionManager;

import java.util.Calendar;

public class SalaryDetailActivity extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerYear;
    private Button btnLoad;
    private TextView tvTotalHours, tvOvertimeHours, tvTotalSalary, tvStatus;
    private AppDatabase db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_detail);

        db = AppDatabase.getDatabase(this);
        sessionManager = new SessionManager(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết lương");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnLoad = findViewById(R.id.btnLoad);
        tvTotalHours = findViewById(R.id.tvTotalHours);
        tvOvertimeHours = findViewById(R.id.tvOvertimeHours);
        tvTotalSalary = findViewById(R.id.tvTotalSalary);
        tvStatus = findViewById(R.id.tvStatus);

        String[] months = {"Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
                           "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"};
        spinnerMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months));

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = {String.valueOf(currentYear - 1), String.valueOf(currentYear)};
        spinnerYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years));
        spinnerYear.setSelection(1);
        spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH));

        btnLoad.setOnClickListener(v -> loadSalary());
        loadSalary();
    }

    private void loadSalary() {
        int month = spinnerMonth.getSelectedItemPosition() + 1;
        int year = Integer.parseInt((String) spinnerYear.getSelectedItem());
        int employeeId = sessionManager.getEmployeeId();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            Salary salary = db.salaryDao().findByEmployeeMonthYear(employeeId, month, year);
            runOnUiThread(() -> {
                if (salary != null) {
                    tvTotalHours.setText("Tổng giờ làm: " + SalaryCalculator.formatHours(salary.getTotalHours()));
                    tvOvertimeHours.setText("Giờ làm thêm: " + SalaryCalculator.formatHours(salary.getOvertimeHours()));
                    tvTotalSalary.setText("Lương tháng " + month + "/" + year + ": " + SalaryCalculator.formatCurrency(salary.getTotalSalary()));
                    tvStatus.setText("Trạng thái: " + ("PAID".equals(salary.getStatus()) ? "Đã thanh toán ✓" : "Chờ thanh toán"));
                } else {
                    tvTotalHours.setText("Tổng giờ làm: --");
                    tvOvertimeHours.setText("Giờ làm thêm: --");
                    tvTotalSalary.setText("Chưa có dữ liệu lương cho tháng này");
                    tvStatus.setText("Trạng thái: --");
                }
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
