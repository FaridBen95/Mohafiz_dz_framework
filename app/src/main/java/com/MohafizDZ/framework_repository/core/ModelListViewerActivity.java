package com.MohafizDZ.framework_repository.core;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.MohafizDZ.framework_repository.Utils.CursorUtils;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.controls.AdapterGridScrollProgress;
import com.MohafizDZ.framework_repository.service.ModelHelper;
import com.MohafizDZ.framework_repository.service.OrderBy;
import com.MohafizDZ.framework_repository.service.OrderByLine;
import com.MohafizDZ.framework_repository.service.PagingSyncModel;
import com.MohafizDZ.framework_repository.service.SyncModel;
import com.MohafizDZ.framework_repository.service.SyncingDomain;
import com.MohafizDZ.own_distributor.R;

import java.util.ArrayList;
import java.util.List;

abstract public class ModelListViewerActivity extends MyAppCompatActivity implements AdapterGridScrollProgress.ViewBindListener, AdapterGridScrollProgress.OnItemClickListener, View.OnClickListener {
    public static final String TAG = ModelListViewerActivity.class.getSimpleName();

    private View noItemFoundLinearLayout;
    private TextView notItemFoundDescriptionTextView;
    private TextView noItemFoundTitleTextView;
    private ImageView noItemFoundImageView;
    private NoItemContainer noItemContainer;
    private View fetchingDataContainer;
    private RecyclerView recyclerView;
    private View loadMoreView;

    protected Model mModel;
    protected String serverRefreshDate;
    private List<DataRow> rows;
    protected int itemPerDisplay;
    protected int currentOffset = 0;
    private PagingSyncModel pagingSyncModel;
    protected AdapterGridScrollProgress mAdapter;
    private SyncModel syncModel;
    private ModelHelper globalSyncModelHelper;

    @NonNull
    abstract protected Model setModel();

    abstract protected boolean syncable();

    abstract protected List<DataRow> setCustomListInput();

    abstract protected SqlQuery setWhereClause(SqlQuery baseSqlQuery, int limit, int currentOffset);

    abstract protected boolean usePaging();

    abstract protected int setItemPerDisplay();

    protected abstract ModelHelper prepareModelHelper(Model mModel, SyncingDomain syncingDomain, OrderBy orderBy);

    abstract protected void onSyncStarted(boolean usePaging);

    abstract protected void onSyncFinished(boolean usePaging);

    abstract protected void onSyncFailed(boolean usePaging, Exception e);

    abstract protected boolean allowScrollLoadMore();

    protected abstract NoItemContainer setNoItemContainer();

    @IdRes
    abstract protected int setLoadMoreButtonId();

    @LayoutRes
    abstract protected int setListItemLayoutResource();

    @LayoutRes
    @Nullable
    abstract protected Integer setLayoutResource();

    protected abstract void onModelListViewCreated(Bundle savedInstanceState);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Integer layoutRes = setLayoutResource();
        layoutRes = layoutRes == null? R.layout.simple_recycler_view_layout: layoutRes;
        setContentView(layoutRes);
        onModelListViewCreated(savedInstanceState);
        init();
        setControls();
        initData();
        initView();
        initAdapter();
        initSync();
        if(syncable()){
            if(usePaging()) {
                if(app().inNetwork()) {
                    pagingSyncModel.sync();
                }else{
                    ModelListViewerActivity.this.onSyncFinished(usePaging());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadNextData(currentOffset);
                        }
                    });
                }
            }else{
                if(app().inNetwork()) {
                    syncGlobalSyncModel();
                }else{
                    onSyncFailed(false, null);
                }
            }
        }
    }

    private void init() {
        noItemFoundLinearLayout = findViewById(R.id.noItemFoundLinearLayout);
        notItemFoundDescriptionTextView = findViewById(R.id.notItemFoundDescriptionTextView);
        noItemFoundTitleTextView = findViewById(R.id.noItemFoundTitleTextView);
        noItemFoundImageView = findViewById(R.id.noItemFoundImageView);
        fetchingDataContainer = findViewById(R.id.fetchingDataContainer);
        recyclerView = findViewById(R.id.recyclerView);
        loadMoreView = findViewById(setLoadMoreButtonId());
        mModel = setModel();
        rows = new ArrayList<>();
    }

    private void setControls() {
        if(loadMoreView != null){
            loadMoreView.setOnClickListener(this);
        }
    }

    private void initData() {
        noItemContainer = setNoItemContainer();
        serverRefreshDate = MyUtil.getCurrentDate();
        itemPerDisplay = setItemPerDisplay();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initView() {
        if(noItemContainer == null){
            noItemFoundLinearLayout.setVisibility(View.GONE);
            return;
        }
        if(noItemContainer.title == null){
            noItemFoundTitleTextView.setVisibility(View.GONE);
        }else {
            noItemFoundTitleTextView.setVisibility(View.VISIBLE);
            noItemFoundTitleTextView.setText(noItemContainer.title);
        }
        if(noItemContainer.description == null){
            notItemFoundDescriptionTextView.setVisibility(View.GONE);
        }else {
            notItemFoundDescriptionTextView.setVisibility(View.VISIBLE);
            notItemFoundDescriptionTextView.setText(noItemContainer.description);
        }
        if(noItemContainer.icon == null){
            noItemFoundImageView.setVisibility(View.GONE);
        }else {
            noItemFoundImageView.setVisibility(View.VISIBLE);
            noItemFoundImageView.setImageDrawable(getResources().getDrawable(noItemContainer.icon));
        }
    }

    private void initAdapter() {
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        if(!syncable()){
            List<DataRow> _rows = setCustomListInput();
            if(_rows != null && _rows.size() != 0){
                rows.clear();
            }else {
                SqlQuery sqlQuery = setWhereClause(new SqlQuery(), 0, 0);
                if (sqlQuery != null) {
                    Uri uri = sqlQuery.uri;
                    String selection = sqlQuery != null ? sqlQuery.selection : null;
                    String[] args = sqlQuery != null ? sqlQuery.args : null;
                    String sort = sqlQuery.sort;
                    if (uri == null) {
                        _rows = mModel.select(null, selection, args, sort);
                    } else {
                        _rows = getLines(uri, selection, args, sort);
                    }
                }
            }
            rows.addAll(_rows);
            onResult(rows.size() == 0);
        }else{
            showLoading();
        }
//        if(!syncable() || !usePaging()){
//            itemPerDisplay = rows.size();
//        }
        mAdapter = new AdapterGridScrollProgress(this, itemPerDisplay, rows, setListItemLayoutResource());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter.setViewBindListener(this);
        mAdapter.setOnItemClickListener(this);
        if(allowScrollLoadMore()) {
            mAdapter.setOnLoadMoreListener(new AdapterGridScrollProgress.OnLoadMoreListener() {
                @Override
                public void onLoadMore(int current_page) {
                    loadMore();
                }
            });
        }else{
            mAdapter.setOnLoadMoreListener(null);
        }
        onAdapterPrepared(recyclerView);
    }

    protected abstract void onAdapterPrepared(RecyclerView recyclerView);

    private void initSync() {
        if(syncable()) {
            if (usePaging()){
                initPagingSync();
            }else{
                initGlobalSync();
            }
        }
    }

    private void initPagingSync() {
        SyncingDomain syncingDomain = new SyncingDomain();
        long serverRefreshDateInMillisSec = MyUtil.dateToMilliSec(serverRefreshDate);
        syncingDomain.addOperation("write_date", SyncingDomain.Operation.lessOrEqualThan, serverRefreshDateInMillisSec, false);
        OrderBy orderBy = new OrderBy();
        orderBy.addLine(new OrderByLine("write_date", OrderByLine.Direction.DESCENDING));
        ModelHelper modelHelper = prepareModelHelper(mModel, syncingDomain, orderBy);
        PagingSyncModel.globalLimit = itemPerDisplay;
        pagingSyncModel = new PagingSyncModel(this, modelHelper) {
            @Override
            public void onSyncStarted() {
                ModelListViewerActivity.this.onSyncStarted(usePaging());
                if(mAdapter.getItemCount() == 0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showLoading();
                        }
                    });
                }
            }

            @Override
            public void onSyncFinished() {
                ModelListViewerActivity.this.onSyncFinished(usePaging());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadNextData(currentOffset);
                    }
                });
            }

            @Override
            public void onSyncFailed() {
                ModelListViewerActivity.this.onSyncFailed(usePaging(), null);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ModelListViewerActivity.this, getResources().getString(R.string.sync_failed), Toast.LENGTH_SHORT).show();
                        loadNextData(currentOffset);
                    }
                });
            }
        };
    }

    protected void initGlobalSync() {
        syncModel = new SyncModel(this, mModel) {
            @Override
            public void onSyncStart(Model model) {
                ModelListViewerActivity.this.onSyncStarted(false);
            }

            @Override
            public void onSyncFinished(Model model) {
                ModelListViewerActivity.this.onSyncFinished(false);
            }

            @Override
            public void onSyncFailed(Model model) {
                ModelListViewerActivity.this.onSyncFailed(false, null);
            }
        };
        final SyncingDomain syncingDomain = new SyncingDomain();
        OrderBy orderBy = new OrderBy();
        orderBy.addLine(new OrderByLine("write_date", OrderByLine.Direction.DESCENDING));
        globalSyncModelHelper = prepareModelHelper(mModel, syncingDomain, orderBy);
        syncModel.setModel(mModel);
    }

    protected void syncGlobalSyncModel() {
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                syncModel.performSync(globalSyncModelHelper);
                return null;
            }
        };
        asyncTask.execute();
    }

    protected List<DataRow> getLines(Uri uri, String selection, String[] args, String sort) {
        Cursor cursor = getContentResolver().query(uri, null, selection, args, sort);
        return CursorUtils.cursorToList(cursor);
    }

    protected void onResult(boolean empty) {
        if(empty) {
            noItemFoundLinearLayout.setVisibility(View.VISIBLE);
            fetchingDataContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }else{
            fetchingDataContainer.setVisibility(View.GONE);
            noItemFoundLinearLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    protected void showLoading() {
        fetchingDataContainer.setVisibility(View.VISIBLE);
        noItemFoundLinearLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if(!allowScrollLoadMore() && loadMoreView != null && v.getId() == loadMoreView.getId()){
            loadMore();
        }
    }

    private void loadMore() {
        if(app().inNetwork()){
            if(syncable() && usePaging()) {
                syncFromServer();
            }else if (syncable()){
                initGlobalSync();
                syncGlobalSyncModel();
            }
        }else{
            loadNextData(currentOffset);
        }
    }

    private void syncFromServer() {
        pagingSyncModel.sync();
    }

    private void loadNextData(final int currentOffset) {
        mAdapter.setLoading();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.insertData(generateListItems(currentOffset));
                onResult(mAdapter.getItemCount() == 0);
            }
        }, 1500);
    }

    private List<DataRow> generateListItems(Integer currentPage) {
        if(currentPage != null && currentPage <  currentOffset){
            return new ArrayList<>();
        }
        SqlQuery sqlQuery = setWhereClause(new SqlQuery(), itemPerDisplay, currentOffset);
        Uri uri = sqlQuery.uri;
        String selection = sqlQuery != null? sqlQuery.selection : null;
        String[] args = sqlQuery != null? sqlQuery.args : null;
        String sort = sqlQuery.sort;
        List<DataRow> rows = getLines(uri, selection, args, sort);
        if(mAdapter != null) {
            currentOffset = mAdapter.getItemCount() + rows.size();
        }else{
            currentOffset = rows.size();
        }
        return rows;
    }


    public static class SqlQuery {
        @Nullable
        public Uri uri;
        public String selection = null;
        public String[] args = null;
        public String sort = null;
    }

    public static class NoItemContainer{
        public String title;
        public String description;
        public @DrawableRes Integer icon;
    }
}
