package com.example.employeemanagement.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.employeemanagement.R;
import com.example.employeemanagement.data.model.Employee;

import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder> {

    private List<Employee> list;
    private OnEmployeeClickListener listener;

    public interface OnEmployeeClickListener {
        void onEditClick(Employee employee);
        void onDeleteClick(Employee employee);
    }

    public EmployeeAdapter(List<Employee> list, OnEmployeeClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Employee emp = list.get(position);
        holder.tvCode.setText(emp.getCode());
        holder.tvName.setText(emp.getFullname());
        holder.tvEmail.setText(emp.getEmail());
        holder.tvPhone.setText(emp.getPhone());
        holder.tvHourlyRate.setText(String.format("%,.0f VND/giờ", emp.getHourlyRate()));

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(emp));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(emp));
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvName, tvEmail, tvPhone, tvHourlyRate;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvHourlyRate = itemView.findViewById(R.id.tvHourlyRate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
