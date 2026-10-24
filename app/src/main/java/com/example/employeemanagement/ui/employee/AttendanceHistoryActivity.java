package com.example.employeemanagement.ui.employee;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.database.AppDatabase;
import com.example.employeemanagement.data.model.WorkLog;
import com.example.employeemanagement.ui.adapter.WorkLogAdapter;
import com.example.employeemanagement.utils.SalaryCalculator;
import com.example.employeemanagement.utils.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AttendanceHistoryActivity extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerYear;
    private Button btnLoad;
    private TextView tvSummary;
    private RecyclerView recyclerView;
    private WorkLogAdapter adapter;
    private List<WorkLog> workLogList = new ArrayList<>();
    private AppDatabase db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        db = AppDatabase.getDatabase(this);
        sessionManager = new SessionManager(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lịch sử chấm công");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnLoad = findViewById(R.id.btnLoad);
        tvSummary = findViewById(R.id.tvSummary);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkLogAdapter(workLogList);
        recyclerView.setAdapter(adapter);

        String[] months = {"Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
                           "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"};
        spinnerMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months));

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = {String.valueOf(currentYear - 1), String.valueOf(currentYear)};
        spinnerYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years));
        spinnerYear.setSelection(1);
        spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH));

        btnLoad.setOnClickListener(v -> loadHistory());
        loadHistory();
    }

    private void loadHistory() {
        int month = spinnerMonth.getSelectedItemPosition() + 1;
        int year = Integer.parseInt((String) spinnerYear.getSelectedItem());
        String monthStr = String.format("%02d", month);
        String yearStr = String.valueOf(year);
        int employeeId = sessionManager.getEmployeeId();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<WorkLog> list = db.workLogDao().getByEmployeeAndMonth(employeeId, monthStr, yearStr);
            double totalHours = 0;
            for (WorkLog wl : list) totalHours += wl.getTotalHours();
            double totalHoursFinal = totalHours;
            runOnUiThread(() -> {
                workLogList.clear();
                workLogList.addAll(list);
                adapter.notifyDataSetChanged();
                tvSummary.setText("Tổng giờ tháng " + month + "/" + year + ": " + SalaryCalculator.formatHours(totalHoursFinal));
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
