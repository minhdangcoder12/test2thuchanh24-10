package com.example.employeemanagement.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.model.WorkLog;

import java.util.List;

public class WorkLogAdapter extends RecyclerView.Adapter<WorkLogAdapter.ViewHolder> {

    private List<WorkLog> list;

    public WorkLogAdapter(List<WorkLog> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_work_log, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkLog log = list.get(position);
        holder.tvDate.setText("Ngày: " + log.getWorkDate());
        holder.tvCheckIn.setText("Vào: " + (log.getCheckInTime() != null ? log.getCheckInTime() : "--"));
        holder.tvCheckOut.setText("Ra: " + (log.getCheckOutTime() != null ? log.getCheckOutTime() : "--"));

        double hours = log.getTotalHours();
        int h = (int) hours;
        int m = (int) ((hours - h) * 60);
        holder.tvHours.setText("Giờ: " + h + "h " + m + "m");
        holder.tvStatus.setText("Trạng thái: " + log.getStatus());
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvCheckIn, tvCheckOut, tvHours, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCheckIn = itemView.findViewById(R.id.tvCheckIn);
            tvCheckOut = itemView.findViewById(R.id.tvCheckOut);
            tvHours = itemView.findViewById(R.id.tvHours);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
