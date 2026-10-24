package com.example.employeemanagement.ui.employee;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

    private TextView tvDate, tvClock, tvStatus, tvCheckIn, tvCheckOut, tvTotalHours;
    private Button btnCheckIn, btnCheckOut;
    private SessionManager sessionManager;
    private AppDatabase db;
    private WorkLog todayLog;
    private int employeeId;

    // Handler để cập nhật đồng hồ real-time
    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateClock();
            clockHandler.postDelayed(this, 1000); // Cập nhật mỗi giây
        }
    };

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

        tvDate        = findViewById(R.id.tvDate);
        tvClock       = findViewById(R.id.tvClock);
        tvStatus      = findViewById(R.id.tvStatus);
        tvCheckIn     = findViewById(R.id.tvCheckIn);
        tvCheckOut    = findViewById(R.id.tvCheckOut);
        tvTotalHours  = findViewById(R.id.tvTotalHours);
        btnCheckIn    = findViewById(R.id.btnCheckIn);
        btnCheckOut   = findViewById(R.id.btnCheckOut);

        // Hiển thị ngày hôm nay
        String today = getTodayDate();
        tvDate.setText("Ngày: " + today);

        btnCheckIn.setOnClickListener(v -> checkIn());
        btnCheckOut.setOnClickListener(v -> checkOut());

        // Bắt đầu đồng hồ real-time
        clockHandler.post(clockRunnable);

        loadTodayLog();
    }

    /**
     * Cập nhật đồng hồ real-time mỗi giây
     */
    private void updateClock() {
        if (tvClock != null) {
            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            tvClock.setText(currentTime);
        }
    }

    private void loadTodayLog() {
        String today = getTodayDate();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            todayLog = db.workLogDao().findByEmployeeAndDate(employeeId, today);
            runOnUiThread(this::updateUI);
        });
    }

    private void updateUI() {
        if (todayLog == null) {
            // Chưa chấm công hôm nay
            tvStatus.setText("Chưa chấm công");
            tvStatus.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            tvCheckIn.setText("--:--");
            tvCheckOut.setText("--:--");
            tvTotalHours.setText("0h 0m");
            btnCheckIn.setEnabled(true);
            btnCheckOut.setEnabled(false);
        } else if (todayLog.getCheckOutTime() != null && !todayLog.getCheckOutTime().isEmpty()) {
            // Đã chấm công đầy đủ
            tvStatus.setText("Đã chấm công ✓");
            tvStatus.setTextColor(0xFF2E7D32); // Màu xanh lá
            tvCheckIn.setText(todayLog.getCheckInTime() != null ? todayLog.getCheckInTime() : "--:--");
            tvCheckOut.setText(todayLog.getCheckOutTime());
            tvTotalHours.setText(SalaryCalculator.formatHours(todayLog.getTotalHours()));
            btnCheckIn.setEnabled(false);
            btnCheckOut.setEnabled(false);
        } else {
            // Đang làm việc (đã check-in, chưa check-out)
            tvStatus.setText("Đang làm việc...");
            tvStatus.setTextColor(0xFFE65100); // Màu cam
            tvCheckIn.setText(todayLog.getCheckInTime() != null ? todayLog.getCheckInTime() : "--:--");
            tvCheckOut.setText("--:--");
            tvTotalHours.setText("Đang tính...");
            btnCheckIn.setEnabled(false);
            btnCheckOut.setEnabled(true);
        }
    }

    private void checkIn() {
        String today = getTodayDate();
        String now = getCurrentTime();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Kiểm tra đã chấm công hôm nay chưa (tránh check-in trùng)
            WorkLog existing = db.workLogDao().findByEmployeeAndDate(employeeId, today);
            if (existing != null) {
                runOnUiThread(() ->
                    Toast.makeText(this, "Bạn đã chấm công vào hôm nay rồi!", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            // Tạo WorkLog mới với trạng thái INCOMPLETE
            WorkLog log = new WorkLog(employeeId, today, now);
            db.workLogDao().insert(log);
            todayLog = db.workLogDao().findByEmployeeAndDate(employeeId, today);

            runOnUiThread(() -> {
                Toast.makeText(this, "✓ Chấm công vào lúc " + now, Toast.LENGTH_SHORT).show();
                updateUI();
            });
        });
    }

    private void checkOut() {
        String now = getCurrentTime();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Kiểm tra đã có giờ vào chưa
            if (todayLog == null) {
                runOnUiThread(() ->
                    Toast.makeText(this, "Chưa có giờ vào! Hãy chấm công vào trước.", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            // Kiểm tra đã check-out chưa
            if (todayLog.getCheckOutTime() != null && !todayLog.getCheckOutTime().isEmpty()) {
                runOnUiThread(() ->
                    Toast.makeText(this, "Bạn đã chấm công ra rồi!", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            // Kiểm tra giờ ra phải sau giờ vào
            double hours = SalaryCalculator.calculateHours(todayLog.getCheckInTime(), now);
            if (hours <= 0) {
                runOnUiThread(() ->
                    Toast.makeText(this, "Giờ ra phải sau giờ vào! (Vào: " + todayLog.getCheckInTime() + ")", Toast.LENGTH_LONG).show()
                );
                return;
            }

            // Cập nhật WorkLog với giờ ra và tổng giờ
            todayLog.setCheckOutTime(now);
            todayLog.setTotalHours(hours);
            todayLog.setStatus("PRESENT");
            db.workLogDao().update(todayLog);

            runOnUiThread(() -> {
                Toast.makeText(this, "✓ Chấm công ra lúc " + now + " | Tổng: " + SalaryCalculator.formatHours(hours), Toast.LENGTH_LONG).show();
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
    protected void onDestroy() {
        super.onDestroy();
        // Dừng đồng hồ khi activity bị hủy để tránh memory leak
        clockHandler.removeCallbacks(clockRunnable);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
