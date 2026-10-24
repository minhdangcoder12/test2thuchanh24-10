package com.example.employeemanagement.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.model.WorkLog;
import com.example.employeemanagement.utils.SalaryCalculator;

import java.util.List;

/**
 * Adapter cho danh sách chấm công (WorkLog).
 * Hỗ trợ hiển thị cho cả màn hình nhân viên và báo cáo admin.
 *
 * Người 2 — Nhiệm vụ 3: Lịch sử chấm công + WorkLogAdapter
 */
public class WorkLogAdapter extends RecyclerView.Adapter<WorkLogAdapter.ViewHolder> {

    private List<WorkLog> list;

    // Map từ employeeId → tên nhân viên (dùng khi admin xem báo cáo)
    private java.util.Map<Integer, String> employeeNameMap;

    public WorkLogAdapter(List<WorkLog> list) {
        this.list = list;
        this.employeeNameMap = new java.util.HashMap<>();
    }

    /**
     * Thiết lập map tên nhân viên để adapter hiển thị tên thay vì employeeId.
     * Admin dùng khi xem báo cáo tất cả nhân viên.
     */
    public void setEmployeeNameMap(java.util.Map<Integer, String> map) {
        this.employeeNameMap = map;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_work_log, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkLog log = list.get(position);

        // Hiển thị ngày làm việc
        holder.tvDate.setText("📅 " + (log.getWorkDate() != null ? log.getWorkDate() : "--"));

        // Hiển thị tên nhân viên nếu có (admin view)
        if (employeeNameMap != null && !employeeNameMap.isEmpty()) {
            String name = employeeNameMap.get(log.getEmployeeId());
            if (name != null && !name.isEmpty()) {
                holder.tvDate.setText("👤 " + name + "  |  " + log.getWorkDate());
            }
        }

        // Giờ vào
        String checkIn = log.getCheckInTime() != null && !log.getCheckInTime().isEmpty()
                ? log.getCheckInTime() : "--:--";
        holder.tvCheckIn.setText(checkIn);

        // Giờ ra
        String checkOut = log.getCheckOutTime() != null && !log.getCheckOutTime().isEmpty()
                ? log.getCheckOutTime() : "--:--";
        holder.tvCheckOut.setText(checkOut);

        // Tổng giờ - dùng SalaryCalculator.formatHours()
        holder.tvHours.setText(SalaryCalculator.formatHours(log.getTotalHours()));

        // Trạng thái với màu sắc phù hợp
        String status = log.getStatus() != null ? log.getStatus() : "INCOMPLETE";
        switch (status) {
            case "PRESENT":
                holder.tvStatus.setText("✓ Đủ công");
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")); // Xanh lá
                break;
            case "INCOMPLETE":
                holder.tvStatus.setText("⚠ Chưa hoàn thành");
                holder.tvStatus.setTextColor(Color.parseColor("#E65100")); // Cam
                break;
            case "ABSENT":
                holder.tvStatus.setText("✗ Vắng mặt");
                holder.tvStatus.setTextColor(Color.parseColor("#C62828")); // Đỏ
                break;
            default:
                holder.tvStatus.setText(status);
                holder.tvStatus.setTextColor(Color.GRAY);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvCheckIn, tvCheckOut, tvHours, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate     = itemView.findViewById(R.id.tvDate);
            tvCheckIn  = itemView.findViewById(R.id.tvCheckIn);
            tvCheckOut = itemView.findViewById(R.id.tvCheckOut);
            tvHours    = itemView.findViewById(R.id.tvHours);
            tvStatus   = itemView.findViewById(R.id.tvStatus);
        }
    }
}
