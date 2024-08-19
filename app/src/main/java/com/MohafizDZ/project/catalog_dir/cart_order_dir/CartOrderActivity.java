package com.MohafizDZ.project.catalog_dir.cart_order_dir;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.CatalogProductAdapter;
import com.MohafizDZ.project.catalog_dir.cart_order_dir.strategies_dir.ConcreteCartOrderStrategy;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.payment_dir.PaymentActivity;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class CartOrderActivity extends MyAppCompatActivity implements ICartOrderPresenter.View, View.OnClickListener, CatalogProductAdapter.OnItemClickListener {
    private static final String TAG = CartOrderActivity.class.getSimpleName();
    private static final String STRATEGY_NAME_KEY = "strategy_name_key";
    private static final String CUSTOMER_ID_KEY = "customer_id_key";

    private ICartOrderPresenter.Presenter presenter;
    private String strategyClassName;
    private RecyclerView recyclerView;
    private FloatingActionButton validateFloatingActionButton;
    private View validateContainer;
    private TextView totalPriceTextView, customerTextView;
    private String customerId;
    private CatalogProductAdapter adapter;
    private BadgeDrawable badgeDrawable;
    private ActivityResultLauncher<Intent> paymentResultLauncher;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_order_layout);
        if(!initArgs()){
            return;
        }
        init();
        findViewById();
        setControls();
        initView();
    }

    private boolean initArgs(){
        Bundle data = getIntent().getExtras();
        if(data != null){
            strategyClassName = data.getString(STRATEGY_NAME_KEY);
            customerId = data.getString(CUSTOMER_ID_KEY);
            return true;
        }else{
            finish();
            return false;
        }
    }

    private void init() {
        ConcreteCartOrderStrategy strategy = getStrategy();
        presenter = new CartOrderPresenterImpl(this, this, strategy);
        strategy.setCustomerId(customerId);
        initResultLaunchers();
    }

    private void initResultLaunchers(){
        paymentResultLauncher = registerForActivityResult( new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == Activity.RESULT_OK){
                setResult(RESULT_OK);
                finish();
            }
        });
    }
    private ConcreteCartOrderStrategy getStrategy() {
        try {
            Class<?> strategyClass = Class.forName(strategyClassName);
            Constructor<?> constructor = strategyClass.getConstructor(Context.class, ICartOrderPresenter.View.class, DataRow.class);
            return (ConcreteCartOrderStrategy) constructor.newInstance(this, this, app().getCurrentUser());
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void findViewById() {
        validateFloatingActionButton = findViewById(R.id.validateFloatingActionButton);
        recyclerView = findViewById(R.id.recyclerView);
        validateContainer = findViewById(R.id.validateContainer);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        customerTextView = findViewById(R.id.customerTextView);
    }

    private void setControls(){
        validateFloatingActionButton.setOnClickListener(this);
    }

    private void initView(){
        presenter.onViewCreated();
    }

    @Override
    public void initAdapter(List<ProductRow> rows) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CatalogProductAdapter(this, rows, this, true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoadFinished(List<ProductRow> rows) {
        adapter.notifyDataSetChanged();
        recyclerView.invalidate();
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

    @Override
    public void toggleValidateContainer(boolean visible) {
        validateContainer.setVisibility(getViewVisibility(visible));
    }

    private int getViewVisibility(boolean visible){
        return visible? View.VISIBLE : View.GONE;
    }

    @Override
    public void setTotalAmount(String text) {
        totalPriceTextView.setText(text);
    }

    @Override
    public void setCustomerName(String text) {
        customerTextView.setText(text);
    }

    @Override
    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void openPaymentActivity(String strategyClassName) {
        Intent intent = PaymentActivity.getIntent(this, strategyClassName, customerId);
        paymentResultLauncher.launch(intent);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.validateFloatingActionButton){
            presenter.onValidate();
        }
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onItemLongClick(int position) {

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent getIntent(Context context, String strategyClassName, String customerId){
        Intent intent = new Intent(context, CartOrderActivity.class);
        Bundle data = new Bundle();
        data.putString(STRATEGY_NAME_KEY, strategyClassName);
        data.putString(CUSTOMER_ID_KEY, customerId);
        intent.putExtras(data);
        return intent;
    }
}
