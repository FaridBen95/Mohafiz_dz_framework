package com.MohafizDZ.project.orders_list_dir;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.MohafizDZ.framework_repository.Utils.BitmapUtils;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.models.VisitOrderModel;
import com.google.android.material.divider.MaterialDivider;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.CustomerViewHolder> {
    private final String currency;
    private Context mContext;
    private List<DataRow> rows;
    private OnItemClickListener onItemClickListener;

    public OrdersAdapter(Context context, List<DataRow> rows, OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.rows = rows;
        this.onItemClickListener = onItemClickListener;
        this.currency = CompanyModel.getCompanyCurrency(context);
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_row_layout, parent, false);
        return new CustomerViewHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        holder.bind(rows.get(position), currency);
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
        private Context mContext;
        private TextView customerNameTextView, amountTextView, dateTextView, orderRefTextView, cancelStateTextView;
        private View backOrderImageView;
        private MaterialDivider divider, errorDivider;

        CustomerViewHolder(Context context, @NonNull View itemView) {
            super(itemView);
            this.mContext = context;
            customerNameTextView = itemView.findViewById(R.id.customerNameTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            orderRefTextView = itemView.findViewById(R.id.orderRefTextView);
            backOrderImageView = itemView.findViewById(R.id.backOrderImageView);
            cancelStateTextView = itemView.findViewById(R.id.cancelStateTextView);
            divider = itemView.findViewById(R.id.divider);
            errorDivider = itemView.findViewById(R.id.errorDivider);
        }

        void bind(DataRow row, String currency) {
            DataRow customerRow = row.getRelRow("customer");
            String customerName = customerRow != null? customerRow.getString("name") : "-";
            String state = row.getString("state");
            customerNameTextView.setText(customerName);
            float totalAmount = row.getFloat("total_amount");
            String totalText = totalAmount + " " + currency;
            amountTextView.setText(totalText);
            dateTextView.setText(row.getString("done_date"));
            orderRefTextView.setText(row.getString("name"));
            if(totalAmount > 0) {
                backOrderImageView.setVisibility(View.GONE);
            }else{
                backOrderImageView.setVisibility(View.VISIBLE);
            }
            if(state.equals(VisitOrderModel.ORDER_STATE_CANCEL)){
                cancelStateTextView.setVisibility(View.VISIBLE);
                divider.setVisibility(View.GONE);
                errorDivider.setVisibility(View.VISIBLE);
            }else{
                cancelStateTextView.setVisibility(View.GONE);
                divider.setVisibility(View.VISIBLE);
                errorDivider.setVisibility(View.GONE);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

}

