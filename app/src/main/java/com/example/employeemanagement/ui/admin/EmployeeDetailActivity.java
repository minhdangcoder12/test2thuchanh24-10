package com.example.employeemanagement.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.database.AppDatabase;
import com.example.employeemanagement.data.model.Account;
import com.example.employeemanagement.data.model.Employee;

public class EmployeeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_EMPLOYEE_ID = "EMPLOYEE_ID";

    private TextView tvCode, tvFullname, tvEmail, tvPhone, tvHourlyRate, tvUsername, tvRole;
    private Button btnEdit, btnDelete;
    private AppDatabase db;
    private int employeeId;
    private Employee employee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_detail);

        db = AppDatabase.getDatabase(this);
        employeeId = getIntent().getIntExtra(EXTRA_EMPLOYEE_ID, -1);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết nhân viên");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvCode       = findViewById(R.id.tvCode);
        tvFullname   = findViewById(R.id.tvFullname);
        tvEmail      = findViewById(R.id.tvEmail);
        tvPhone      = findViewById(R.id.tvPhone);
        tvHourlyRate = findViewById(R.id.tvHourlyRate);
        tvUsername   = findViewById(R.id.tvUsername);
        tvRole       = findViewById(R.id.tvRole);
        btnEdit      = findViewById(R.id.btnEdit);
        btnDelete    = findViewById(R.id.btnDelete);

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditEmployeeActivity.class);
            intent.putExtra("EMPLOYEE_ID", employeeId);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> confirmDelete());

        loadEmployee();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEmployee(); // reload sau khi edit
    }

    private void loadEmployee() {
        if (employeeId == -1) { finish(); return; }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            employee = db.employeeDao().findById(employeeId);
            if (employee == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Không tìm thấy nhân viên!", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }
            Account account = db.accountDao().findById(employee.getAccountId());
            runOnUiThread(() -> {
                tvCode.setText("Mã NV: " + employee.getCode());
                tvFullname.setText(employee.getFullname());
                tvEmail.setText("📧 " + employee.getEmail());
                tvPhone.setText("📞 " + employee.getPhone());
                tvHourlyRate.setText(String.format("💰 %,.0f VND / giờ", employee.getHourlyRate()));
                if (account != null) {
                    tvUsername.setText("👤 TK: " + account.getUsername());
                    tvRole.setText("Vai trò: " + account.getRole());
                }
            });
        });
    }

    private void confirmDelete() {
        if (employee == null) return;
        new AlertDialog.Builder(this)
            .setTitle("⚠️ Xóa nhân viên")
            .setMessage("Bạn có chắc muốn xóa nhân viên \"" + employee.getFullname() + "\"?\nHành động này không thể hoàn tác.")
            .setPositiveButton("Xóa", (d, w) -> deleteEmployee())
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deleteEmployee() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.employeeDao().delete(employee);
            db.accountDao().deleteById(employee.getAccountId());
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã xóa nhân viên " + employee.getFullname(), Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
