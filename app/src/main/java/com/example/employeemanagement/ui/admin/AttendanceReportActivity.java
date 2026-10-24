package com.example.employeemanagement.ui.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.database.AppDatabase;
import com.example.employeemanagement.data.model.WorkLog;
import com.example.employeemanagement.ui.adapter.WorkLogAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AttendanceReportActivity extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerYear;
    private Button btnLoad;
    private RecyclerView recyclerView;
    private WorkLogAdapter adapter;
    private List<WorkLog> workLogList = new ArrayList<>();
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);

        db = AppDatabase.getDatabase(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Báo cáo chấm công");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnLoad = findViewById(R.id.btnLoad);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkLogAdapter(workLogList);
        recyclerView.setAdapter(adapter);

        // Setup spinners
        String[] months = {"Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
                           "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"};
        spinnerMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months));

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = {String.valueOf(currentYear - 1), String.valueOf(currentYear)};
        spinnerYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years));
        spinnerYear.setSelection(1);
        spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH));

        btnLoad.setOnClickListener(v -> loadReport());
        loadReport();
    }

    private void loadReport() {
        int month = spinnerMonth.getSelectedItemPosition() + 1;
        int year = Integer.parseInt((String) spinnerYear.getSelectedItem());
        String monthStr = String.format("%02d", month);
        String yearStr = String.valueOf(year);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Get all work logs for this month across all employees
            List<WorkLog> list = db.workLogDao().getAll();
            List<WorkLog> filtered = new ArrayList<>();
            for (WorkLog wl : list) {
                if (wl.getWorkDate() != null && wl.getWorkDate().startsWith(yearStr + "-" + monthStr)) {
                    filtered.add(wl);
                }
            }
            runOnUiThread(() -> {
                workLogList.clear();
                workLogList.addAll(filtered);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
