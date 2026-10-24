package com.example.employeemanagement.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.employeemanagement.R;
import com.example.employeemanagement.ui.auth.LoginActivity;
import com.example.employeemanagement.utils.SessionManager;

public class AdminDashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new SessionManager(this);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Xin chào, " + sessionManager.getUsername() + " (Admin)");

        // Card: Quản lý nhân viên
        CardView cardEmployee = findViewById(R.id.cardEmployee);
        cardEmployee.setOnClickListener(v ->
            startActivity(new Intent(this, EmployeeListActivity.class)));

        // Card: Quản lý lương
        CardView cardSalary = findViewById(R.id.cardSalary);
        cardSalary.setOnClickListener(v ->
            startActivity(new Intent(this, SalaryManagementActivity.class)));

        // Card: Báo cáo chấm công
        CardView cardAttendance = findViewById(R.id.cardAttendance);
        cardAttendance.setOnClickListener(v ->
            startActivity(new Intent(this, AttendanceReportActivity.class)));

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
