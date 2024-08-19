package com.MohafizDZ.project.visit_action_list_dir;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.google.android.material.divider.MaterialDivider;

import java.util.List;

public class ActionsAdapter extends RecyclerView.Adapter<ActionsAdapter.CustomerViewHolder> {
    private Context mContext;
    private List<DataRow> rows;
    private OnItemClickListener onItemClickListener;

    public ActionsAdapter(Context context, List<DataRow> rows, OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.rows = rows;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.action_row_layout, parent, false);
        return new CustomerViewHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        holder.bind(rows.get(position));
        holder.itemView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
        holder.itemView.setOnLongClickListener(view -> {
            if(onItemClickListener != null){
                onItemClickListener.onItemLongClick(position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        private TextView customerNameTextView, dateTextView, actionTextView, distanceTextview;

        CustomerViewHolder(Context context, @NonNull View itemView) {
            super(itemView);
            customerNameTextView = itemView.findViewById(R.id.customerNameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            actionTextView = itemView.findViewById(R.id.actionTextView);
            distanceTextview = itemView.findViewById(R.id.distanceTextview);
        }

        void bind(DataRow row) {
            DataRow customerRow = row.getRelRow("customer");
            String customerName = customerRow != null? customerRow.getString("name") : "-";
            customerNameTextView.setText(customerName);
            dateTextView.setText(row.getString("action_date"));
            actionTextView.setText(row.getString("action_name"));
            float distance = row.getFloat("distance_from_customer");
            distanceTextview.setText(Math.round(distance) + "");
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

}

