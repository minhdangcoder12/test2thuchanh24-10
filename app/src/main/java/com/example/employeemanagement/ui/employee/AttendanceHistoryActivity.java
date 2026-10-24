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

/**
 * Màn hình lịch sử chấm công của nhân viên.
 * Hiển thị danh sách worklog theo tháng + tổng kết giờ làm.
 *
 * Người 2 — Nhiệm vụ 3: Lịch sử chấm công nhân viên
 */
public class AttendanceHistoryActivity extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerYear;
    private Button btnLoad;
    private TextView tvSummary, tvDaysWorked, tvOvertimeHours;
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

        spinnerMonth  = findViewById(R.id.spinnerMonth);
        spinnerYear   = findViewById(R.id.spinnerYear);
        btnLoad       = findViewById(R.id.btnLoad);
        tvSummary     = findViewById(R.id.tvSummary);
        tvDaysWorked  = findViewById(R.id.tvDaysWorked);
        tvOvertimeHours = findViewById(R.id.tvOvertimeHours);
        recyclerView  = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkLogAdapter(workLogList);
        recyclerView.setAdapter(adapter);

        // Khởi tạo Spinner tháng
        String[] months = {
            "Tháng 1","Tháng 2","Tháng 3","Tháng 4",
            "Tháng 5","Tháng 6","Tháng 7","Tháng 8",
            "Tháng 9","Tháng 10","Tháng 11","Tháng 12"
        };
        spinnerMonth.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, months));

        // Khởi tạo Spinner năm
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = {String.valueOf(currentYear - 1), String.valueOf(currentYear)};
        spinnerYear.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, years));
        spinnerYear.setSelection(1); // Chọn năm hiện tại
        spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH)); // Chọn tháng hiện tại

        btnLoad.setOnClickListener(v -> loadHistory());
        loadHistory(); // Tự động tải dữ liệu tháng hiện tại
    }

    /**
     * Tải lịch sử chấm công theo tháng đã chọn.
     * Tính tổng giờ, số ngày đi làm, giờ OT.
     */
    private void loadHistory() {
        int month = spinnerMonth.getSelectedItemPosition() + 1;
        int year  = Integer.parseInt((String) spinnerYear.getSelectedItem());
        String monthStr = String.format("%02d", month);
        String yearStr  = String.valueOf(year);
        int employeeId  = sessionManager.getEmployeeId();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<WorkLog> list = db.workLogDao().getByEmployeeAndMonth(employeeId, monthStr, yearStr);

            // Tính các chỉ số tổng kết
            double totalHours = 0;
            int daysPresent   = 0;
            for (WorkLog wl : list) {
                totalHours += wl.getTotalHours();
                if ("PRESENT".equals(wl.getStatus())) {
                    daysPresent++;
                }
            }

            double overtimeHours    = SalaryCalculator.calculateMonthlyOvertime(totalHours);
            double totalHoursFinal  = totalHours;
            int daysPresentFinal    = daysPresent;
            double otHoursFinal     = overtimeHours;

            runOnUiThread(() -> {
                workLogList.clear();
                workLogList.addAll(list);
                adapter.notifyDataSetChanged();

                // Cập nhật tổng kết
                tvSummary.setText("Tổng giờ: " + SalaryCalculator.formatHours(totalHoursFinal));
                tvDaysWorked.setText("Ngày công: " + daysPresentFinal + " ngày");
                tvOvertimeHours.setText("Giờ OT: " + SalaryCalculator.formatHours(otHoursFinal));
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
