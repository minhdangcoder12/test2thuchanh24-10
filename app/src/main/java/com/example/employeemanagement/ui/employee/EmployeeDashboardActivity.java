package com.example.employeemanagement.ui.employee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.database.AppDatabase;
import com.example.employeemanagement.data.model.Employee;
import com.example.employeemanagement.ui.auth.LoginActivity;
import com.example.employeemanagement.utils.SessionManager;

public class EmployeeDashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        sessionManager = new SessionManager(this);
        db = AppDatabase.getDatabase(this);

        TextView tvWelcome = findViewById(R.id.tvWelcome);

        // Load employee info
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Employee emp = db.employeeDao().findByAccountId(sessionManager.getAccountId());
            runOnUiThread(() -> {
                if (emp != null) {
                    tvWelcome.setText("Xin chào, " + emp.getFullname());
                }
            });
        });

        // Card: Chấm công
        CardView cardCheckIn = findViewById(R.id.cardCheckIn);
        cardCheckIn.setOnClickListener(v ->
            startActivity(new Intent(this, CheckInOutActivity.class)));

        // Card: Lịch sử chấm công
        CardView cardHistory = findViewById(R.id.cardHistory);
        cardHistory.setOnClickListener(v ->
            startActivity(new Intent(this, AttendanceHistoryActivity.class)));

        // Card: Xem lương
        CardView cardSalary = findViewById(R.id.cardSalary);
        cardSalary.setOnClickListener(v ->
            startActivity(new Intent(this, SalaryDetailActivity.class)));

        // Card: Hồ sơ
        CardView cardProfile = findViewById(R.id.cardProfile);
        cardProfile.setOnClickListener(v ->
            startActivity(new Intent(this, ProfileActivity.class)));

        // Logout
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc muốn đăng xuất?")
            .setPositiveButton("Đăng xuất", (d, w) -> {
                sessionManager.logout();
                startActivity(new Intent(this, LoginActivity.class));
                finishAffinity();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}
