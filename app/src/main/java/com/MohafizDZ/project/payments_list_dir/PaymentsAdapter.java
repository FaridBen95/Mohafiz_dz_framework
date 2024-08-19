package com.MohafizDZ.project.payments_list_dir;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.models.PaymentModel;
import com.MohafizDZ.project.models.VisitOrderModel;
import com.google.android.material.divider.MaterialDivider;

import java.util.List;

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.CustomerViewHolder> {
    private final String currency;
    private Context mContext;
    private List<DataRow> rows;
    private OnItemClickListener onItemClickListener;

    public PaymentsAdapter(Context context, List<DataRow> rows, OnItemClickListener onItemClickListener) {
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
        private MaterialDivider divider, errorDivider, validatedDivider;

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
            validatedDivider = itemView.findViewById(R.id.validatedDivider);
        }

        void bind(DataRow row, String currency) {
            DataRow customerRow = row.getRelRow("customer");
            String customerName = customerRow != null? customerRow.getString("name") : "-";
            String state = row.getString("state");
            if(row.getBoolean("is_expenses") || row.getString("customer_id").equals("false")) {
                DataRow userRow = row.getRelRow("user");
                String userName = userRow != null? userRow.getString("name") : "-";
                customerNameTextView.setText(userName);
                customerNameTextView.setVisibility(View.GONE);
                orderRefTextView.setText(row.getString("expense_subject"));
            }else{
                customerNameTextView.setVisibility(View.VISIBLE);
                customerNameTextView.setText(customerName);
                orderRefTextView.setText(row.getString("name"));
            }
            float totalAmount = row.getFloat("amount");
            String totalText = totalAmount + " " + currency;
            amountTextView.setText(totalText);
            dateTextView.setText(row.getString("payment_date"));
            if(totalAmount > 0) {
                backOrderImageView.setVisibility(View.GONE);
            }else{
                backOrderImageView.setVisibility(View.VISIBLE);
            }
            if(state.equals(VisitOrderModel.ORDER_STATE_CANCEL)){
                cancelStateTextView.setVisibility(View.VISIBLE);
                validatedDivider.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
                errorDivider.setVisibility(View.VISIBLE);
            }else if(state.equals(PaymentModel.STATE_EXPENSES_DONE)){
                cancelStateTextView.setVisibility(View.GONE);
                validatedDivider.setVisibility(View.VISIBLE);
                divider.setVisibility(View.GONE);
                errorDivider.setVisibility(View.GONE);
            }else{
                cancelStateTextView.setVisibility(View.GONE);
                validatedDivider.setVisibility(View.GONE);
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

