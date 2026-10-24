package com.example.employeemanagement.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.database.AppDatabase;
import com.example.employeemanagement.data.model.Employee;
import com.example.employeemanagement.data.model.Salary;
import com.example.employeemanagement.utils.SalaryCalculator;

import java.util.List;

public class SalaryAdapter extends RecyclerView.Adapter<SalaryAdapter.ViewHolder> {

    private List<Salary> list;
    private Context context;

    public SalaryAdapter(List<Salary> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_salary, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Salary salary = list.get(position);
        holder.tvPeriod.setText("Tháng " + salary.getMonth() + "/" + salary.getYear());
        holder.tvTotalHours.setText("Tổng giờ: " + SalaryCalculator.formatHours(salary.getTotalHours()));
        holder.tvOvertime.setText("Làm thêm: " + SalaryCalculator.formatHours(salary.getOvertimeHours()));
        holder.tvSalary.setText(SalaryCalculator.formatCurrency(salary.getTotalSalary()));
        holder.tvStatus.setText("PAID".equals(salary.getStatus()) ? "Đã trả ✓" : "Chờ trả");

        // Mark as paid button
        if ("PENDING".equals(salary.getStatus())) {
            holder.btnMarkPaid.setVisibility(View.VISIBLE);
            holder.btnMarkPaid.setOnClickListener(v -> {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    AppDatabase.getDatabase(context).salaryDao().markAsPaid(salary.getId());
                    salary.setStatus("PAID");
                    ((android.app.Activity) context).runOnUiThread(() -> notifyItemChanged(position));
                });
            });
        } else {
            holder.btnMarkPaid.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPeriod, tvTotalHours, tvOvertime, tvSalary, tvStatus;
        Button btnMarkPaid;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPeriod = itemView.findViewById(R.id.tvPeriod);
            tvTotalHours = itemView.findViewById(R.id.tvTotalHours);
            tvOvertime = itemView.findViewById(R.id.tvOvertime);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnMarkPaid = itemView.findViewById(R.id.btnMarkPaid);
        }
    }
}
