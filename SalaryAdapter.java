package com.example.employeemanagement.ui.adapter;

import android.content.Context;
import android.graphics.Color;
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

        // Load employee name on background thread then update UI
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Employee emp = AppDatabase.getDatabase(context).employeeDao().findById(salary.getEmployeeId());
            String empName = (emp != null) ? emp.getFullname() : ("NV #" + salary.getEmployeeId());
            String empCode = (emp != null) ? emp.getCode() : "";

            ((android.app.Activity) context).runOnUiThread(() -> {
                holder.tvEmployeeName.setText(empName);
                holder.tvEmployeeCode.setText(empCode);
            });
        });

        holder.tvPeriod.setText("Tháng " + salary.getMonth() + "/" + salary.getYear());

        // Tính giờ thường và OT
        double normalHours = Math.min(salary.getTotalHours(), 160.0);
        double otHours = salary.getOvertimeHours();

        holder.tvTotalHours.setText("Giờ thường: " + SalaryCalculator.formatHours(normalHours));
        holder.tvOvertime.setText("Giờ OT: " + SalaryCalculator.formatHours(otHours));
        holder.tvSalary.setText(SalaryCalculator.formatCurrency(salary.getTotalSalary()));

        boolean isPaid = "PAID".equals(salary.getStatus());
        holder.tvStatus.setText(isPaid ? "✓ Đã trả" : "⏳ Chờ trả");
        holder.tvStatus.setTextColor(isPaid
                ? context.getResources().getColor(R.color.green_paid, context.getTheme())
                : context.getResources().getColor(R.color.orange_pending, context.getTheme()));

        // Mark as paid button
        if (!isPaid) {
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
        TextView tvEmployeeName, tvEmployeeCode, tvPeriod, tvTotalHours, tvOvertime, tvSalary, tvStatus;
        Button btnMarkPaid;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvEmployeeCode = itemView.findViewById(R.id.tvEmployeeCode);
            tvPeriod = itemView.findViewById(R.id.tvPeriod);
            tvTotalHours = itemView.findViewById(R.id.tvTotalHours);
            tvOvertime = itemView.findViewById(R.id.tvOvertime);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnMarkPaid = itemView.findViewById(R.id.btnMarkPaid);
        }
    }
}
