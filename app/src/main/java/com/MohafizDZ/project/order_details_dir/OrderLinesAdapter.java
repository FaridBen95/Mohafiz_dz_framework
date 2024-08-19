package com.MohafizDZ.project.order_details_dir;

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
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.models.CompanyModel;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;

import java.util.List;

public class OrderLinesAdapter extends RecyclerView.Adapter<OrderLinesAdapter.CustomerViewHolder> {
    private Context mContext;
    private List<DataRow> rows;
    private OrderLinesAdapter.OnItemClickListener onItemClickListener;
    private CartItemSingleton cartItemSingleton;
    private String currencyCode;

    public OrderLinesAdapter(Context context, List<DataRow> rows, OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.rows = rows;
        this.cartItemSingleton = CartItemSingleton.getInstance();
        this.onItemClickListener = onItemClickListener;
        this.currencyCode = CompanyModel.getCompanyCurrency(context);
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_order_product_row, parent, false);
        return new OrderLinesAdapter.CustomerViewHolder(mContext, view);
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        DataRow productRow = rows.get(position);
        holder.bind(productRow, currencyCode);
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
        private BadgeDrawable badgeDrawable;

        CustomerViewHolder(Context context, @NonNull View itemView) {
            super(itemView);
            this.mContext = context;
            productItemContainer = itemView.findViewById(R.id.productItemContainer);
            badgeView = itemView.findViewById(R.id.badgeView);
            imageView = itemView.findViewById(R.id.imageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
        }

        @OptIn(markerClass = ExperimentalBadgeUtils.class)
        void bind(DataRow row, String currencyCode) {
            Bitmap bitmap = BitmapUtils.getBitmapImage(mContext, row.getString("picture_low"));
            imageView.setImageBitmap(bitmap);
            titleTextView.setText(row.getString("name"));
            String price = row.getFloat("total_price") + "";
            String priceText = price + " " + currencyCode;
            priceTextView.setText(priceText);
            BadgeDrawable badgeDrawable = this.badgeDrawable == null? BadgeDrawable.create(mContext):
                    this.badgeDrawable;
            float qty = row.getFloat("qty");
            badgeDrawable.setText(qty + "");
            badgeDrawable.setBadgeGravity(BadgeDrawable.TOP_END);
            this.badgeDrawable = badgeDrawable;
            productItemContainer.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> {
                BadgeUtils.attachBadgeDrawable(badgeDrawable, badgeView);
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

}


