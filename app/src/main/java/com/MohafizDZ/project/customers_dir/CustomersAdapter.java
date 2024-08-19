package com.MohafizDZ.project.customers_dir;

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
import com.MohafizDZ.framework_repository.core.BitmapDataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyModel;

import java.util.List;

public class CustomersAdapter extends RecyclerView.Adapter<CustomersAdapter.CustomerViewHolder> {
    private Context mContext;
    private List<BitmapDataRow> rows;
    private OnItemClickListener onItemClickListener;
    private final String currency;

    public CustomersAdapter(Context context, List<BitmapDataRow> rows, OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.rows = rows;
        this.onItemClickListener = onItemClickListener;
        this.currency = CompanyModel.getCompanyCurrency(context);
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_row_layout, parent, false);
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
        private ImageView customerImageView;
        private TextView customerNameTextView, addressTextView, balanceLimitTextView, balanceTextView;
        private View visitedDivider, divider;

        CustomerViewHolder(Context context, @NonNull View itemView) {
            super(itemView);
            this.mContext = context;
            customerImageView = itemView.findViewById(R.id.customerImageView);
            customerNameTextView = itemView.findViewById(R.id.customerNameTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            balanceTextView = itemView.findViewById(R.id.balanceTextView);
            balanceLimitTextView = itemView.findViewById(R.id.balanceLimitTextView);
            divider = itemView.findViewById(R.id.divider);
            visitedDivider = itemView.findViewById(R.id.visitedDivider);
        }

        void bind(BitmapDataRow row, String currency) {
            Bitmap bitmap = row.getBitmap("picture_low", mContext);
            customerImageView.setImageBitmap(bitmap);
            customerNameTextView.setText(row.getString("name"));
            addressTextView.setText(row.getString("address"));
            String balanceLimitText = getPrice(row.getFloat("balance_limit"), currency);
            String balanceText = getPrice(row.getFloat("balance"), currency);
            balanceLimitTextView.setText(balanceLimitText);
            balanceTextView.setText(balanceText);
            if(row.getBoolean("visited")){
                divider.setVisibility(View.GONE);
                visitedDivider.setVisibility(View.VISIBLE);
            }else{
                divider.setVisibility(View.VISIBLE);
                visitedDivider.setVisibility(View.GONE);
            }
        }

        private String getPrice(float price, String currency){
            return price + " " + currency;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

}

