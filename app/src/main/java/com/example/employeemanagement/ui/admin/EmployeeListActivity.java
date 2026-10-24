package com.example.employeemanagement.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.List;

public class EmployeeListActivity extends AppCompatActivity implements EmployeeAdapter.OnEmployeeClickListener {

    private RecyclerView recyclerView;
    private EmployeeAdapter adapter;
    private List<Employee> employeeList = new ArrayList<>();
    private EditText etSearch;
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

        etSearch = findViewById(R.id.etSearch);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EmployeeAdapter(employeeList, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditEmployeeActivity.class);
            startActivity(intent);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchEmployees(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadEmployees();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEmployees();
    }

    private void loadEmployees() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Employee> list = db.employeeDao().getAll();
            runOnUiThread(() -> {
                employeeList.clear();
                employeeList.addAll(list);
                adapter.notifyDataSetChanged();
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
            runOnUiThread(() -> {
                employeeList.clear();
                employeeList.addAll(list);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public void onEditClick(Employee employee) {
        Intent intent = new Intent(this, AddEditEmployeeActivity.class);
        intent.putExtra("EMPLOYEE_ID", employee.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Employee employee) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa nhân viên")
            .setMessage("Bạn có chắc muốn xóa " + employee.getFullname() + "?")
            .setPositiveButton("Xóa", (d, w) -> deleteEmployee(employee))
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deleteEmployee(Employee employee) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.employeeDao().delete(employee);
            // Also delete the linked account
            db.accountDao().deleteById(employee.getAccountId()); // Cần thêm method này
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã xóa nhân viên", Toast.LENGTH_SHORT).show();
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
