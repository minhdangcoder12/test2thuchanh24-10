package com.example.employeemanagement.ui.employee;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.database.AppDatabase;
import com.example.employeemanagement.data.model.Account;
import com.example.employeemanagement.data.model.Employee;
import com.example.employeemanagement.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvCode, tvFullname, tvEmail, tvPhone, tvHourlyRate, tvUsername;
    private AppDatabase db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = AppDatabase.getDatabase(this);
        sessionManager = new SessionManager(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Hồ sơ cá nhân");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvCode = findViewById(R.id.tvCode);
        tvFullname = findViewById(R.id.tvFullname);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvHourlyRate = findViewById(R.id.tvHourlyRate);
        tvUsername = findViewById(R.id.tvUsername);

        loadProfile();
    }

    private void loadProfile() {
        int accountId = sessionManager.getAccountId();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Employee emp = db.employeeDao().findByAccountId(accountId);
            Account acc = db.accountDao().findById(accountId);
            runOnUiThread(() -> {
                if (emp != null) {
                    tvCode.setText("Mã NV: " + emp.getCode());
                    tvFullname.setText("Họ tên: " + emp.getFullname());
                    tvEmail.setText("Email: " + emp.getEmail());
                    tvPhone.setText("SĐT: " + emp.getPhone());
                    tvHourlyRate.setText("Lương/giờ: " + String.format("%,.0f VND", emp.getHourlyRate()));
                }
                if (acc != null) {
                    tvUsername.setText("Tên đăng nhập: " + acc.getUsername());
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
