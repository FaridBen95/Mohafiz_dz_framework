package com.MohafizDZ.project.sales_dir;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.recyclerview.widget.RecyclerView;

import com.MohafizDZ.framework_repository.Utils.BitmapUtils;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.models.CartItemSingleton;
import com.MohafizDZ.project.models.CompanyModel;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;

import java.util.List;

public class LinesAdapter extends RecyclerView.Adapter<LinesAdapter.CustomerViewHolder> {
    private Context mContext;
    private List<DataRow> rows;
    private OnItemClickListener onItemClickListener;
    private CartItemSingleton cartItemSingleton;

    public LinesAdapter(Context context, List<DataRow> rows, OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.rows = rows;
        this.cartItemSingleton = CartItemSingleton.getInstance();
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_order_product_row, parent, false);
        return new CustomerViewHolder(mContext, view);
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        DataRow productRow = rows.get(position);
        holder.bind(productRow);
        holder.productItemContainer.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
        holder.productItemContainer.setOnLongClickListener(view -> {
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
        private ImageView imageView;
        private View productItemContainer, badgeView;
        private TextView priceTextView, titleTextView;

        CustomerViewHolder(Context context, @NonNull View itemView) {
            super(itemView);
            this.mContext = context;
            productItemContainer = itemView.findViewById(R.id.productItemContainer);
            badgeView = itemView.findViewById(R.id.badgeView);
            imageView = itemView.findViewById(R.id.imageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
        }

        void bind(DataRow row) {
            Bitmap bitmap = BitmapUtils.getBitmapImage(mContext, row.getString("picture_low"));
            imageView.setImageBitmap(bitmap);
            titleTextView.setText(row.getString("name"));
            String sumQty = row.getString("sum_qty");
            priceTextView.setText(sumQty);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

}


