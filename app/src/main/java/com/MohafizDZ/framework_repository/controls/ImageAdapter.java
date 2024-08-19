package com.MohafizDZ.framework_repository.controls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.MohafizDZ.own_distributor.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private Context mContext;
    private List<String> imagePathList;
    private OnImageClickListener onImageClickListener;
    private int itemResourceId;

    public ImageAdapter(Context context, @LayoutRes int itemResourceId, List<String> imagePathList, OnImageClickListener onImageClickListener) {
        this.mContext = context;
        this.itemResourceId = itemResourceId;
        this.imagePathList = imagePathList;
        this.onImageClickListener = onImageClickListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemResourceId, parent, false);
        return new ImageViewHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.bind(imagePathList.get(position));
        holder.itemView.setOnClickListener(view -> {
            if (onImageClickListener != null) {
                onImageClickListener.onImageClick(position);
            }
        });
        holder.itemView.setOnLongClickListener(view -> {
            if(onImageClickListener != null){
                onImageClickListener.onImageLongClick(position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return imagePathList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private Context mContext;
        private ImageView imageView;

        ImageViewHolder(Context context, @NonNull View itemView) {
            super(itemView);
            this.mContext = context;
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
        }

        void bind(String imagePath) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(bitmap);
        }
    }

    public static interface OnImageClickListener {
        void onAddImage();
        void onImageClick(int position);
        void onImageLongClick(int position);
    }

}