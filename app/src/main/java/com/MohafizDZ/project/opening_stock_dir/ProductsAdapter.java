package com.MohafizDZ.project.opening_stock_dir;

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
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.models.CartItemSingleton;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.models.CompanyModel;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;

import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.CustomerViewHolder> {
    private Context mContext;
    private List<ProductRow> rows;
    private ProductsAdapter.OnItemClickListener onItemClickListener;

    public ProductsAdapter(Context context, List<ProductRow> rows, OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.rows = rows;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_row, parent, false);
        return new ProductsAdapter.CustomerViewHolder(mContext, view);
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        ProductRow productRow = rows.get(position);
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
        void bind(ProductRow row) {
            Bitmap bitmap = BitmapUtils.getBitmapImage(mContext, row.getString("picture_low"));
            imageView.setImageBitmap(bitmap);
            titleTextView.setText(row.getString("name"));
            BadgeDrawable badgeDrawable = this.badgeDrawable == null? BadgeDrawable.create(mContext):
                    this.badgeDrawable;
            float qty = row.getQty();
            boolean canShowQty = row.canShowQty();
            badgeDrawable.setText(qty + "");
            badgeDrawable.setVisible(canShowQty && qty > 0);
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


