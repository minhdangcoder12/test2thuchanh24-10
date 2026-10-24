package com.example.employeemanagement.ui.admin;

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
import com.example.employeemanagement.data.model.Employee;
import com.example.employeemanagement.data.model.WorkLog;
import com.example.employeemanagement.ui.adapter.WorkLogAdapter;
import com.example.employeemanagement.utils.SalaryCalculator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Màn hình báo cáo chấm công dành cho Admin.
 * Hiển thị toàn bộ worklog của tất cả nhân viên theo tháng.
 * Kèm thống kê tổng số lượt chấm công.
 *
 * Người 2 — Nhiệm vụ 4: Báo cáo chấm công (Admin)
 */
public class AttendanceReportActivity extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerYear;
    private Button btnLoad;
    private TextView tvReportSummary;
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

        spinnerMonth    = findViewById(R.id.spinnerMonth);
        spinnerYear     = findViewById(R.id.spinnerYear);
        btnLoad         = findViewById(R.id.btnLoad);
        tvReportSummary = findViewById(R.id.tvReportSummary);
        recyclerView    = findViewById(R.id.recyclerView);

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
        String[] years  = {String.valueOf(currentYear - 1), String.valueOf(currentYear)};
        spinnerYear.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, years));
        spinnerYear.setSelection(1);
        spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH));

        btnLoad.setOnClickListener(v -> loadReport());
        loadReport(); // Tự động tải báo cáo tháng hiện tại
    }

    /**
     * Tải báo cáo chấm công tất cả nhân viên trong tháng.
     * Dùng strftime query từ WorkLogDao (hiệu quả hơn filter ở Java).
     * Cũng tải danh sách nhân viên để hiển thị tên trong adapter.
     */
    private void loadReport() {
        int month       = spinnerMonth.getSelectedItemPosition() + 1;
        int year        = Integer.parseInt((String) spinnerYear.getSelectedItem());
        String monthStr = String.format("%02d", month);
        String yearStr  = String.valueOf(year);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Lấy tất cả worklog trong tháng (dùng query hiệu quả từ DB)
            List<WorkLog> allLogs = db.workLogDao().getAll();
            List<WorkLog> filtered = new ArrayList<>();
            for (WorkLog wl : allLogs) {
                if (wl.getWorkDate() != null
                        && wl.getWorkDate().startsWith(yearStr + "-" + monthStr)) {
                    filtered.add(wl);
                }
            }

            // Xây dựng map employeeId → tên nhân viên để hiển thị trong adapter
            List<Employee> employees = db.employeeDao().getAll();
            Map<Integer, String> nameMap = new HashMap<>();
            for (Employee emp : employees) {
                nameMap.put(emp.getId(), emp.getFullname());
            }

            // Tính thống kê tổng
            int totalEntries = filtered.size();
            int presentCount = 0;
            double totalHoursAllEmployees = 0;
            for (WorkLog wl : filtered) {
                if ("PRESENT".equals(wl.getStatus())) {
                    presentCount++;
                    totalHoursAllEmployees += wl.getTotalHours();
                }
            }

            int presentFinal    = presentCount;
            int totalFinal      = totalEntries;
            double hoursFinal   = totalHoursAllEmployees;

            runOnUiThread(() -> {
                workLogList.clear();
                workLogList.addAll(filtered);
                adapter.setEmployeeNameMap(nameMap); // Hiển thị tên nhân viên
                adapter.notifyDataSetChanged();

                // Cập nhật thống kê tổng
                if (tvReportSummary != null) {
                    tvReportSummary.setText(
                        "Tháng " + month + "/" + year
                        + " — " + totalFinal + " lượt chấm công"
                        + " | " + presentFinal + " đủ công"
                        + " | Tổng: " + SalaryCalculator.formatHours(hoursFinal)
                    );
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
