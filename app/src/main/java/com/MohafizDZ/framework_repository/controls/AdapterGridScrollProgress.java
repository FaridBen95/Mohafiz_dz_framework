package com.MohafizDZ.framework_repository.controls;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.MohafizDZ.framework_repository.Utils.ViewAnimation;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.empty_project.R;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

public class AdapterGridScrollProgress extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROGRESS = 0;

    @LayoutRes
    private int listItemRes;
    private int item_per_display = 0;
    private List<DataRow> items = new ArrayList<>();
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener = null;

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private ViewBindListener viewBindListener;

    public List<DataRow> getItems() {
        return items;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, DataRow obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public interface ViewBindListener{
        void onViewBind(View parentView, int position, DataRow row);
    }

    public void setViewBindListener(ViewBindListener viewBindListener) {
        this.viewBindListener = viewBindListener;
    }

    public AdapterGridScrollProgress(Context context, int item_per_display, List<DataRow> items, @NonNull @LayoutRes int listItemRes) {
        this.items = items;
        this.item_per_display = item_per_display;
        ctx = context;
        this.listItemRes = listItemRes;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public AVLoadingIndicatorView progressDots;
        public FrameLayout lyt_progress;

        public ProgressViewHolder(View v) {
            super(v);
            progressDots = (AVLoadingIndicatorView) v.findViewById(R.id.progressDots);
            lyt_progress = v.findViewById(R.id.lyt_progress);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(listItemRes, parent, false);
            vh = new OriginalViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final DataRow s = items.get(position);
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener == null) return;
                    mOnItemClickListener.onItemClick(view, s, position);
                }
            });
            if(viewBindListener != null){
                viewBindListener.onViewBind(view.lyt_parent, position, s);
            }
        } else {
            ((ProgressViewHolder) holder).progressDots.show();
            animateProgress(((ProgressViewHolder) holder).lyt_progress);
        }
        Boolean progress = s.getBoolean("progress");

        if (progress != null && progress) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
//            layoutParams.setFullSpan(true);
        }
    }

    private void animateProgress(final FrameLayout lyt_progress) {
        lyt_progress.setVisibility(View.VISIBLE);
        lyt_progress.setAlpha(1.0f);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewAnimation.fadeOut(lyt_progress);
            }
        }, 1500);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Boolean progress = this.items.get(position).getBoolean("progress");
        return progress != null && progress ? VIEW_PROGRESS : VIEW_ITEM;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        lastItemViewDetector(recyclerView);
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void insertData(List<DataRow> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            Boolean progress = this.items.get(i).getBoolean("progress");
            if (progress != null && progress) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            DataRow row = new DataRow();
            row.put("progress", true);
            this.items.add(row);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void resetListData() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = getLastVisibleItem(layoutManager.findLastVisibleItemPositions(null));
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        int current_page = getItemCount() / item_per_display;
                        onLoadMoreListener.onLoadMore(current_page);
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    private int getLastVisibleItem(int[] into) {
        int last_idx = into[0];
        for (int i : into) {
            if (last_idx < i) last_idx = i;
        }
        return last_idx;
    }
}
