package com.example.employeemanagement.ui.employee;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.database.AppDatabase;
import com.example.employeemanagement.data.model.WorkLog;
import com.example.employeemanagement.utils.SalaryCalculator;
import com.example.employeemanagement.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CheckInOutActivity extends AppCompatActivity {

    private TextView tvDate, tvStatus, tvCheckIn, tvCheckOut, tvTotalHours;
    private Button btnCheckIn, btnCheckOut;
    private SessionManager sessionManager;
    private AppDatabase db;
    private WorkLog todayLog;
    private int employeeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_out);

        sessionManager = new SessionManager(this);
        db = AppDatabase.getDatabase(this);
        employeeId = sessionManager.getEmployeeId();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chấm công hôm nay");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvDate = findViewById(R.id.tvDate);
        tvStatus = findViewById(R.id.tvStatus);
        tvCheckIn = findViewById(R.id.tvCheckIn);
        tvCheckOut = findViewById(R.id.tvCheckOut);
        tvTotalHours = findViewById(R.id.tvTotalHours);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCheckOut = findViewById(R.id.btnCheckOut);

        String today = getTodayDate();
        tvDate.setText("Ngày: " + today);

        btnCheckIn.setOnClickListener(v -> checkIn());
        btnCheckOut.setOnClickListener(v -> checkOut());

        loadTodayLog();
    }

    private void loadTodayLog() {
        String today = getTodayDate();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            todayLog = db.workLogDao().findByEmployeeAndDate(employeeId, today);
            runOnUiThread(() -> updateUI());
        });
    }

    private void updateUI() {
        if (todayLog == null) {
            tvStatus.setText("Chưa chấm công");
            tvCheckIn.setText("Giờ vào: --:--");
            tvCheckOut.setText("Giờ ra: --:--");
            tvTotalHours.setText("Tổng giờ: 0h 0m");
            btnCheckIn.setEnabled(true);
            btnCheckOut.setEnabled(false);
        } else {
            tvCheckIn.setText("Giờ vào: " + todayLog.getCheckInTime());
            if (todayLog.getCheckOutTime() != null && !todayLog.getCheckOutTime().isEmpty()) {
                tvStatus.setText("Đã chấm công ✓");
                tvCheckOut.setText("Giờ ra: " + todayLog.getCheckOutTime());
                tvTotalHours.setText("Tổng giờ: " + SalaryCalculator.formatHours(todayLog.getTotalHours()));
                btnCheckIn.setEnabled(false);
                btnCheckOut.setEnabled(false);
            } else {
                tvStatus.setText("Đang làm việc...");
                tvCheckOut.setText("Giờ ra: --:--");
                tvTotalHours.setText("Tổng giờ: đang tính...");
                btnCheckIn.setEnabled(false);
                btnCheckOut.setEnabled(true);
            }
        }
    }

    private void checkIn() {
        String today = getTodayDate();
        String now = getCurrentTime();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Prevent duplicate check-in
            WorkLog existing = db.workLogDao().findByEmployeeAndDate(employeeId, today);
            if (existing != null) {
                runOnUiThread(() -> Toast.makeText(this, "Bạn đã chấm công hôm nay rồi!", Toast.LENGTH_SHORT).show());
                return;
            }

            WorkLog log = new WorkLog(employeeId, today, now);
            db.workLogDao().insert(log);
            todayLog = db.workLogDao().findByEmployeeAndDate(employeeId, today);

            runOnUiThread(() -> {
                Toast.makeText(this, "Chấm công vào lúc " + now, Toast.LENGTH_SHORT).show();
                updateUI();
            });
        });
    }

    private void checkOut() {
        String now = getCurrentTime();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (todayLog == null) {
                runOnUiThread(() -> Toast.makeText(this, "Chưa có giờ vào!", Toast.LENGTH_SHORT).show());
                return;
            }
            if (todayLog.getCheckOutTime() != null && !todayLog.getCheckOutTime().isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "Đã chấm công ra rồi!", Toast.LENGTH_SHORT).show());
                return;
            }

            // Validate checkout after checkin
            double hours = SalaryCalculator.calculateHours(todayLog.getCheckInTime(), now);
            if (hours <= 0) {
                runOnUiThread(() -> Toast.makeText(this, "Giờ ra phải sau giờ vào!", Toast.LENGTH_SHORT).show());
                return;
            }

            todayLog.setCheckOutTime(now);
            todayLog.setTotalHours(hours);
            todayLog.setStatus("PRESENT");
            db.workLogDao().update(todayLog);

            runOnUiThread(() -> {
                Toast.makeText(this, "Chấm công ra lúc " + now, Toast.LENGTH_SHORT).show();
                updateUI();
            });
        });
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
