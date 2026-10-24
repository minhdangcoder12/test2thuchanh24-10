package com.example.employeemanagement.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.database.AppDatabase;
import com.example.employeemanagement.data.model.Account;
import com.example.employeemanagement.data.model.Employee;
import com.example.employeemanagement.ui.admin.AdminDashboardActivity;
import com.example.employeemanagement.ui.employee.EmployeeDashboardActivity;
import com.example.employeemanagement.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private SessionManager sessionManager;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Auto-redirect if already logged in
        if (sessionManager.isLoggedIn()) {
            redirectByRole(sessionManager.getRole());
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        db = AppDatabase.getDatabase(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        // Run on background thread
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Account account = db.accountDao().login(username, password);
            runOnUiThread(() -> {
                if (account != null) {
                    int employeeId = -1;
                    if ("EMPLOYEE".equals(account.getRole())) {
                        Employee emp = db.employeeDao().findByAccountId(account.getId());
                        if (emp != null) employeeId = emp.getId();
                    }
                    sessionManager.createSession(account.getId(), account.getUsername(), account.getRole(), employeeId);
                    redirectByRole(account.getRole());
                    finish();
                } else {
                    Toast.makeText(this, "Sai tên đăng nhập hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void redirectByRole(String role) {
        Intent intent;
        if ("ADMIN".equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, EmployeeDashboardActivity.class);
        }
        startActivity(intent);
    }
}
