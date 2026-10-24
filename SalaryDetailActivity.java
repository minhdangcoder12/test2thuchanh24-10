package com.example.employeemanagement.ui.employee;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.database.AppDatabase;
import com.example.employeemanagement.data.model.Employee;
import com.example.employeemanagement.data.model.Salary;
import com.example.employeemanagement.utils.SalaryCalculator;
import com.example.employeemanagement.utils.SessionManager;

import java.util.Calendar;

public class SalaryDetailActivity extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerYear;
    private Button btnLoad;
    private TextView tvNormalHours, tvOvertimeHours, tvNormalPay, tvOvertimePay, tvTotalSalary, tvStatus, tvNoData;
    private ProgressBar progressBar;
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
        tvNormalHours = findViewById(R.id.tvNormalHours);
        tvOvertimeHours = findViewById(R.id.tvOvertimeHours);
        tvNormalPay = findViewById(R.id.tvNormalPay);
        tvOvertimePay = findViewById(R.id.tvOvertimePay);
        tvTotalSalary = findViewById(R.id.tvTotalSalary);
        tvStatus = findViewById(R.id.tvStatus);
        tvNoData = findViewById(R.id.tvNoData);
        progressBar = findViewById(R.id.progressBar);

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

    /**
     * Nhiệm vụ 3: Hiển thị chi tiết lương — giờ thường, OT, lương thường, lương OT, tổng.
     */
    private void loadSalary() {
        int month = spinnerMonth.getSelectedItemPosition() + 1;
        int year = Integer.parseInt((String) spinnerYear.getSelectedItem());
        int employeeId = sessionManager.getEmployeeId();

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            Salary salary = db.salaryDao().findByEmployeeMonthYear(employeeId, month, year);

            // Lấy đơn giá giờ của nhân viên để tính chi tiết
            Employee emp = db.employeeDao().findById(employeeId);
            double hourlyRate = (emp != null) ? emp.getHourlyRate() : 0;

            runOnUiThread(() -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (salary != null) {
                    tvNoData.setVisibility(View.GONE);

                    // Tính giờ thường (tối đa 160h/tháng)
                    double normalHours = Math.min(salary.getTotalHours(), 160.0);
                    double otHours = salary.getOvertimeHours();

                    // Tính tiền theo công thức
                    double normalPay = normalHours * hourlyRate;
                    double otPay = otHours * hourlyRate * SalaryCalculator.OVERTIME_RATE;

                    tvNormalHours.setText("Giờ làm thường: " + SalaryCalculator.formatHours(normalHours));
                    tvOvertimeHours.setText("Giờ làm thêm (OT): " + SalaryCalculator.formatHours(otHours));
                    tvNormalPay.setText("Lương thường: " + SalaryCalculator.formatCurrency(normalPay));
                    tvOvertimePay.setText("Lương OT (×" + SalaryCalculator.OVERTIME_RATE + "): "
                            + SalaryCalculator.formatCurrency(otPay));
                    tvTotalSalary.setText(SalaryCalculator.formatCurrency(salary.getTotalSalary()));

                    boolean isPaid = "PAID".equals(salary.getStatus());
                    tvStatus.setText(isPaid ? "✓ Đã thanh toán" : "⏳ Chờ thanh toán");
                    tvStatus.setTextColor(getResources().getColor(
                            isPaid ? R.color.green_paid : R.color.orange_pending, getTheme()));
                } else {
                    // Không có dữ liệu
                    tvNoData.setVisibility(View.VISIBLE);
                    tvNormalHours.setText("Giờ làm thường: --");
                    tvOvertimeHours.setText("Giờ làm thêm (OT): --");
                    tvNormalPay.setText("Lương thường: --");
                    tvOvertimePay.setText("Lương OT: --");
                    tvTotalSalary.setText("--");
                    tvStatus.setText("Trạng thái: --");
                    tvStatus.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
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
