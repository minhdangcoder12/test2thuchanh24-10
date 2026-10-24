package com.example.employeemanagement.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.database.AppDatabase;
import com.example.employeemanagement.data.model.Account;
import com.example.employeemanagement.data.model.Employee;
import com.example.employeemanagement.ui.admin.AdminDashboardActivity;
import com.example.employeemanagement.ui.employee.EmployeeDashboardActivity;
import com.example.employeemanagement.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Auto-redirect nếu đã đăng nhập
        if (sessionManager.isLoggedIn()) {
            redirectByRole(sessionManager.getRole());
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        db = AppDatabase.getDatabase(this);

        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etUsername  = findViewById(R.id.etUsername);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Animate logo vào
        View logo = findViewById(R.id.ivLogo);
        if (logo != null) {
            logo.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        }

        btnLogin.setOnClickListener(v -> attemptLogin());

        // Cho phép nhấn Done trên keyboard để đăng nhập
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            attemptLogin();
            return true;
        });
    }

    private void attemptLogin() {
        // Reset lỗi cũ
        tilUsername.setError(null);
        tilPassword.setError(null);

        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        boolean hasError = false;

        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Vui lòng nhập tên đăng nhập");
            hasError = true;
        } else if (username.length() < 3) {
            tilUsername.setError("Tên đăng nhập tối thiểu 3 ký tự");
            hasError = true;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            hasError = true;
        } else if (password.length() < 6) {
            tilPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            hasError = true;
        }

        if (hasError) return;

        // Hiển thị loading
        setLoadingState(true);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            Account account = db.accountDao().login(username, password);
            runOnUiThread(() -> {
                setLoadingState(false);
                if (account != null) {
                    int employeeId = -1;
                    if ("EMPLOYEE".equals(account.getRole())) {
                        Employee emp = db.employeeDao().findByAccountId(account.getId());
                        if (emp != null) employeeId = emp.getId();
                    }
                    sessionManager.createSession(
                        account.getId(), account.getUsername(),
                        account.getRole(), employeeId
                    );
                    Toast.makeText(this, "Chào mừng, " + account.getUsername() + "!", Toast.LENGTH_SHORT).show();
                    redirectByRole(account.getRole());
                    finish();
                } else {
                    // Shake animation khi sai
                    btnLogin.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
                    tilUsername.setError("Sai tên đăng nhập hoặc mật khẩu");
                    tilPassword.setError("Sai tên đăng nhập hoặc mật khẩu");
                }
            });
        });
    }

    private void setLoadingState(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Đang đăng nhập..." : "ĐĂNG NHẬP");
        etUsername.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }

    private void redirectByRole(String role) {
        Intent intent;
        if ("ADMIN".equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, EmployeeDashboardActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
