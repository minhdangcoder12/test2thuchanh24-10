package com.example.employeemanagement.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.database.AppDatabase;
import com.example.employeemanagement.data.model.Employee;
import com.example.employeemanagement.ui.adapter.EmployeeAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class EmployeeListActivity extends AppCompatActivity
        implements EmployeeAdapter.OnEmployeeClickListener {

    private RecyclerView recyclerView;
    private EmployeeAdapter adapter;
    private List<Employee> employeeList = new ArrayList<>();
    private TextInputEditText etSearch;
    private TextView tvCount;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);

        db = AppDatabase.getDatabase(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý nhân viên");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etSearch    = findViewById(R.id.etSearch);
        tvCount     = findViewById(R.id.tvCount);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EmployeeAdapter(employeeList, this);
        recyclerView.setAdapter(adapter);

        // FAB thêm nhân viên
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v ->
            startActivity(new Intent(this, AddEditEmployeeActivity.class)));

        // Real-time search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchEmployees(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadEmployees();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Làm mới danh sách khi quay lại từ Add/Edit/Detail
        String keyword = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        if (keyword.isEmpty()) loadEmployees();
        else searchEmployees(keyword);
    }

    private void loadEmployees() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Employee> list = db.employeeDao().getAll();
            runOnUiThread(() -> {
                employeeList.clear();
                employeeList.addAll(list);
                adapter.notifyDataSetChanged();
                updateCount(list.size());
            });
        });
    }

    private void searchEmployees(String keyword) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Employee> list;
            if (keyword.isEmpty()) {
                list = db.employeeDao().getAll();
            } else {
                list = db.employeeDao().search(keyword);
            }
            final List<Employee> result = list;
            runOnUiThread(() -> {
                employeeList.clear();
                employeeList.addAll(result);
                adapter.notifyDataSetChanged();
                updateCount(result.size());
            });
        });
    }

    private void updateCount(int count) {
        if (tvCount != null) {
            tvCount.setText("Tổng: " + count + " nhân viên");
        }
    }

    // Click item → xem chi tiết
    @Override
    public void onItemClick(Employee employee) {
        Intent intent = new Intent(this, EmployeeDetailActivity.class);
        intent.putExtra(EmployeeDetailActivity.EXTRA_EMPLOYEE_ID, employee.getId());
        startActivity(intent);
    }

    // Click nút sửa
    @Override
    public void onEditClick(Employee employee) {
        Intent intent = new Intent(this, AddEditEmployeeActivity.class);
        intent.putExtra("EMPLOYEE_ID", employee.getId());
        startActivity(intent);
    }

    // Click nút xóa
    @Override
    public void onDeleteClick(Employee employee) {
        new AlertDialog.Builder(this)
            .setTitle("⚠️ Xóa nhân viên")
            .setMessage("Xóa nhân viên \"" + employee.getFullname() + "\"?\nHành động này không thể hoàn tác.")
            .setPositiveButton("Xóa", (d, w) -> deleteEmployee(employee))
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deleteEmployee(Employee employee) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.employeeDao().delete(employee);
            db.accountDao().deleteById(employee.getAccountId());
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã xóa " + employee.getFullname(), Toast.LENGTH_SHORT).show();
                loadEmployees();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
