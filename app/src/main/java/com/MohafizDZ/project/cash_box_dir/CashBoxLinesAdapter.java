package com.MohafizDZ.project.cash_box_dir;

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

public class CashBoxLinesAdapter extends RecyclerView.Adapter<CashBoxLinesAdapter.CustomerViewHolder> {
    private final String currency;
    private Context mContext;
    private List<DataRow> rows;
    private OnItemClickListener onItemClickListener;
    private boolean validated;

    public CashBoxLinesAdapter(Context context, List<DataRow> rows, OnItemClickListener onItemClickListener, boolean validated) {
        this.mContext = context;
        this.rows = rows;
        this.onItemClickListener = onItemClickListener;
        this.currency = CompanyModel.getCompanyCurrency(context);
        this.validated = validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cash_box_row, parent, false);
        return new CustomerViewHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        holder.bind(rows.get(position), currency, validated);
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
        private final TextView denominationTextView, totalTextView, countTextView;
        private final MaterialDivider divider, validatedDivider;

        CustomerViewHolder(Context context, @NonNull View itemView) {
            super(itemView);
            countTextView = itemView.findViewById(R.id.countTextView);
            totalTextView = itemView.findViewById(R.id.totalTextView);
            denominationTextView = itemView.findViewById(R.id.denominationTextView);
            divider = itemView.findViewById(R.id.divider);
            validatedDivider = itemView.findViewById(R.id.validatedDivider);
        }

        void bind(DataRow row, String currency, boolean validated) {
            String totalText = (int) (float)row.getFloat("total") + " "  + currency;
            String denominationTxt = row.getString("denomination_value") + " " + currency;
            String countTxt = row.getString("count");
            totalTextView.setText(totalText);
            countTextView.setText(countTxt);
            denominationTextView.setText(denominationTxt);
            if(validated){
                validatedDivider.setVisibility(View.VISIBLE);
                divider.setVisibility(View.GONE);
            }else{
                validatedDivider.setVisibility(View.GONE);
                divider.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

}

