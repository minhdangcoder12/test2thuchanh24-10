package com.example.employeemanagement.ui.admin;

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

public class AddEditEmployeeActivity extends AppCompatActivity {

    private EditText etCode, etFullname, etEmail, etPhone, etHourlyRate;
    private EditText etUsername, etPassword;
    private Button btnSave;
    private AppDatabase db;
    private int employeeId = -1;
    private Employee existingEmployee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_employee);

        db = AppDatabase.getDatabase(this);
        employeeId = getIntent().getIntExtra("EMPLOYEE_ID", -1);

        boolean isEdit = (employeeId != -1);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEdit ? "Sửa nhân viên" : "Thêm nhân viên");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etCode = findViewById(R.id.etCode);
        etFullname = findViewById(R.id.etFullname);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etHourlyRate = findViewById(R.id.etHourlyRate);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSave = findViewById(R.id.btnSave);

        if (isEdit) {
            loadEmployee();
            // Hide account fields when editing (account managed separately)
            etUsername.setEnabled(false);
            etPassword.setEnabled(false);
        }

        btnSave.setOnClickListener(v -> saveEmployee());
    }

    private void loadEmployee() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            existingEmployee = db.employeeDao().findById(employeeId);
            if (existingEmployee != null) {
                Account acc = db.accountDao().findById(existingEmployee.getAccountId());
                runOnUiThread(() -> {
                    etCode.setText(existingEmployee.getCode());
                    etFullname.setText(existingEmployee.getFullname());
                    etEmail.setText(existingEmployee.getEmail());
                    etPhone.setText(existingEmployee.getPhone());
                    etHourlyRate.setText(String.valueOf(existingEmployee.getHourlyRate()));
                    if (acc != null) {
                        etUsername.setText(acc.getUsername());
                        etPassword.setText(acc.getPassword());
                    }
                });
            }
        });
    }

    private void saveEmployee() {
        String code = etCode.getText().toString().trim();
        String fullname = etFullname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String hourlyRateStr = etHourlyRate.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(code)) { etCode.setError("Nhập mã nhân viên"); return; }
        if (TextUtils.isEmpty(fullname)) { etFullname.setError("Nhập họ tên"); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Nhập email"); return; }
        if (TextUtils.isEmpty(phone)) { etPhone.setError("Nhập số điện thoại"); return; }
        if (TextUtils.isEmpty(hourlyRateStr)) { etHourlyRate.setError("Nhập mức lương/giờ"); return; }

        double hourlyRate;
        try {
            hourlyRate = Double.parseDouble(hourlyRateStr);
            if (hourlyRate <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            etHourlyRate.setError("Mức lương phải là số dương"); return;
        }

        boolean isEdit = (employeeId != -1);
        if (!isEdit) {
            if (TextUtils.isEmpty(username)) { etUsername.setError("Nhập tên đăng nhập"); return; }
            if (TextUtils.isEmpty(password) || password.length() < 6) {
                etPassword.setError("Mật khẩu tối thiểu 6 ký tự"); return;
            }
        }

        double finalHourlyRate = hourlyRate;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (isEdit) {
                // Update employee
                existingEmployee.setCode(code);
                existingEmployee.setFullname(fullname);
                existingEmployee.setEmail(email);
                existingEmployee.setPhone(phone);
                existingEmployee.setHourlyRate(finalHourlyRate);
                db.employeeDao().update(existingEmployee);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                // Check duplicate username or code
                Account existingAcc = db.accountDao().findByUsername(username);
                if (existingAcc != null) {
                    runOnUiThread(() -> etUsername.setError("Tên đăng nhập đã tồn tại"));
                    return;
                }
                Employee existingEmp = db.employeeDao().findByCode(code);
                if (existingEmp != null) {
                    runOnUiThread(() -> etCode.setError("Mã nhân viên đã tồn tại"));
                    return;
                }

                // Create account first
                Account newAccount = new Account(username, password, "EMPLOYEE");
                long accountId = db.accountDao().insert(newAccount);

                // Create employee
                Employee newEmployee = new Employee(code, fullname, email, phone, finalHourlyRate, (int) accountId);
                db.employeeDao().insert(newEmployee);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Thêm nhân viên thành công", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
