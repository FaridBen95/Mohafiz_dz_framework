package com.MohafizDZ.project.inventory_dir;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.CatalogActivity;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.catalog_dir.quantity_dialog_dir.IQtyDialogPresenter;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class InventoryActivity extends MyAppCompatActivity implements IInventoryPresenter.View, View.OnClickListener, InventoryLineAdapter.OnItemClickListener {
    private static final String TAG = InventoryActivity.class.getSimpleName();
    private static final String TOUR_ID_KEY = "TOUR_ID_KEY";
    private static final String IS_EDITABLE_KEY = "is_editable_key";
    private IInventoryPresenter.Presenter presenter;
    private RecyclerView recyclerView;
    private FloatingActionButton addFloatingActionButton, validateFloatingActionButton;
    private ActivityResultLauncher<Intent> refreshResultLauncher;
    private InventoryLineAdapter adapter;
    private Menu menu;
    private BadgeDrawable badgeDrawable;
    private String tourId;
    private boolean isEditable;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_list);
        initArgs();
        init();
        findViewById();
        setControls();
        prepareView();
    }

    private void initArgs(){
        Bundle data = getIntent().getExtras();
        tourId = data != null? data.getString(TOUR_ID_KEY) : null;
        isEditable = data != null && data.getBoolean(IS_EDITABLE_KEY);
    }

    private void init(){
        presenter = new InventoryPresenterImpl(this, this, app().getCurrentUser());
        presenter.setTourId(tourId);
        presenter.setEditable(isEditable);
        initResultLauncher();
    }

    private void initResultLauncher(){
        refreshResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
            if(o.getResultCode() != RESULT_OK) {
                presenter.onCatalogCanceled();
            }
            presenter.onListUpdated();
        });
    }

    private void findViewById() {
        addFloatingActionButton = findViewById(R.id.addFloatingActionButton);
        validateFloatingActionButton = findViewById(R.id.validateFloatingActionButton);
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void setControls(){
        addFloatingActionButton.setOnClickListener(this);
        validateFloatingActionButton.setOnClickListener(this);
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                presenter.onBackPressed(false);
            }
        });
    }

    private void prepareView(){
        presenter.onViewCreated();
    }
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.addFloatingActionButton){
            presenter.requestAddLine();
        }else if(id == R.id.validateFloatingActionButton){
            onValidate();
        }
    }


    private void onValidate(){
        new MaterialAlertDialogBuilder(this).
                setTitle(getString(R.string.validate_label)).
                setMessage(getString(R.string.validate_invetory_msg)).
                setPositiveButton(getString(R.string.validate_label), (dialogInterface, i) -> presenter.onValidate()).
                setNegativeButton(getString(R.string.cancel_label), null).show();
    }

    @Override
    public void startCatalogActivity(String strategyClassName){
        Intent intent = CatalogActivity.getIntent(this, strategyClassName, null, false);
        refreshResultLauncher.launch(intent);
    }

    @Override
    public void initAdapter(List<ProductRow> rows, boolean canShowTheoQty) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryLineAdapter(this, rows, this, canShowTheoQty);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoadFinished(List<ProductRow> rows) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position) {
        presenter.onItemClick(position);
    }

    @Override
    public void onItemLongClick(int position) {

    }

    @Override
    public void showQtyDialog(IQtyDialogPresenter.Dialog qtyDialog) {
        qtyDialog.showDialog(this);
    }

    @Override
    public void onLineUpdated(int position) {
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onLineDeleted(int position) {
        adapter.notifyItemRemoved(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inventory_menu, menu);
        this.menu = menu;
        menu.findItem(R.id.menuDelete).setVisible(true);
        presenter.onCreateOptionsMenu();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void toggleInitMenuItem(boolean visible) {
        try {
            menu.findItem(R.id.menuInit).setVisible(visible);
        }catch (Exception ignored){}
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menuDelete){
            requestEmptyLines();
        }else if(id == android.R.id.home){
            presenter.onBackPressed(false);
        }else if(id == R.id.menuInit){
            requestInit();
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestInit(){
        new MaterialAlertDialogBuilder(this).
                setTitle(getString(R.string.init_label)).
                setMessage(getString(R.string.init_dialog_msg)).
                setPositiveButton(getString(R.string.init_label), (dialogInterface, i) -> presenter.initLines()).
                setNegativeButton(getString(R.string.cancel_label), null).
                create().show();
    }

    private void requestEmptyLines(){
        new MaterialAlertDialogBuilder(this).
                setTitle(getString(R.string.empty_lines_title)).
                setMessage(getString(R.string.empty_lines_msg)).
                setPositiveButton(getString(R.string.empty_label), (dialogInterface, i) -> presenter.emptyList()).
                setNegativeButton(getString(R.string.cancel_label), null).create().show();
    }

    @Override
    public void toggleDeleteMenuItem(boolean visible) {
        try {
            menu.findItem(R.id.menuDelete).setVisible(visible);
        }catch (Exception ignored){}
    }

    @Override
    public void goBack() {
        finish();
    }

    @Override
    public void showIgnoreChangesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.ignore_changes_title));
        builder.setMessage(getString(R.string.ignore_initial_stock_msg));
        builder.setPositiveButton(getString(R.string.ignore_label), (dialogInterface, i) -> presenter.onBackPressed(true));
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.create().show();
    }

    @Override
    public void toggleValidateButton(boolean visible) {
        validateFloatingActionButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleAddButton(boolean visible) {
        addFloatingActionButton.setVisibility(getViewVisibility(visible));
    }

    private int getViewVisibility(boolean visible){
        return visible? View.VISIBLE : View.GONE;
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    @Override
    public void refreshValidateButtonBadge(int count) {
        BadgeDrawable badgeDrawable = this.badgeDrawable == null? BadgeDrawable.create(this):
                this.badgeDrawable;
        badgeDrawable.setText(count + "");
        badgeDrawable.setVisible(count > 0);
        badgeDrawable.setBadgeGravity(BadgeDrawable.TOP_END);
        this.badgeDrawable = badgeDrawable;
        validateFloatingActionButton.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> {
            BadgeUtils.attachBadgeDrawable(badgeDrawable, validateFloatingActionButton);
        });

    }

    public static Intent getIntent(Context context){
        return getIntent(context, null, false);
    }

    public static Intent getIntent(Context context, String tourId, boolean isEditable){
        Intent intent = new Intent(context, InventoryActivity.class);
        Bundle data = new Bundle();
        data.putString(TOUR_ID_KEY, tourId);
        data.putBoolean(IS_EDITABLE_KEY, isEditable);
        intent.putExtras(data);
        return intent;
    }
}
