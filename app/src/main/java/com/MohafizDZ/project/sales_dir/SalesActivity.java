package com.MohafizDZ.project.sales_dir;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.product_form_dir.ProductFormActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class SalesActivity extends MyAppCompatActivity implements ISalesPresenter.View, LinesAdapter.OnItemClickListener, View.OnClickListener {
    private static final String TAG = SalesActivity.class.getSimpleName();
    private static final String IS_EDITABLE_KEY = "is_editable_key";
    private static final String TOUR_ID_KEY = "tour_id_key";
    private ISalesPresenter.Presenter presenter;
    private FloatingActionButton validateFloatingActionButton;
    private RecyclerView recyclerView;
    private LinesAdapter adapter;
    private boolean isEditable;
    private String tourId;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sales_layout);
        initArgs();
        init();
        findViewById();
        setControls();
        initView();
    }

    private void initArgs(){
        Bundle data = getIntent().getExtras();
        isEditable = data != null && data.getBoolean(IS_EDITABLE_KEY);
        tourId = data != null? data.getString(TOUR_ID_KEY) : null;
    }

    private void init(){
        presenter = new SalesPresenterImpl(this, this, app().getCurrentUser());
        presenter.setTourId(tourId);
        presenter.setEditable(isEditable);
    }

    private void findViewById() {
        validateFloatingActionButton = findViewById(R.id.validateFloatingActionButton);
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void setControls(){
        validateFloatingActionButton.setOnClickListener(this);
    }

    private void initView(){
        presenter.onViewCreated();
    }

    @Override
    public void toggleValidateButton(boolean visible) {
        validateFloatingActionButton.setVisibility(getViewVisibility(visible));
    }

    private int getViewVisibility(boolean visible){
        return visible? View.VISIBLE : View.GONE;
    }

    @Override
    public void initAdapter(List<DataRow> rows) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LinesAdapter(this, rows, this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoadFinished(List<DataRow> rows) {
        adapter.notifyDataSetChanged();
        recyclerView.invalidate();
    }

    @Override
    public void requestOpenProductDetails(String productId, boolean editable) {
        Intent intent = ProductFormActivity.getIntent(this, productId, editable);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void onItemClick(int position) {
        presenter.onItemClick(position);
    }

    @Override
    public void onItemLongClick(int position) {

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.validateFloatingActionButton){
            requestValidate();
        }
    }

    private void requestValidate(){
        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.validate_sales_title))
                .setMessage(getString(R.string.validate_sales_msg))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.validate_label), (dialogInterface, i) -> {
                    presenter.onValidate();
                });
        dialogBuilder.create().show();
    }

    @Override
    public void goBack() {
        finish();
    }

    public static Intent getIntent(Context context, String tourId, boolean isEditable){
        Intent intent = new Intent(context, SalesActivity.class);
        Bundle data = new Bundle();
        data.putBoolean(IS_EDITABLE_KEY, isEditable);
        data.putString(TOUR_ID_KEY, tourId);
        intent.putExtras(data);
        return intent;
    }
}
