package com.MohafizDZ.project.order_details_dir;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.MohafizDZ.framework_repository.Utils.CurrentLocationUtil;
import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.payment_dir.IPaymentPresenter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class OrderDetailsActivity extends MyAppCompatActivity implements IOrderDetailsPresenter.View, IPaymentPresenter.ValidateView {
    private static final String TAG = OrderDetailsActivity.class.getSimpleName();
    private IOrderDetailsPresenter.Presenter presenter;
    private String orderId;
    private TextView remainingTextView, paymentAmountTextview, orderAmountTextview, dateTextView,
            sellerTextView, tourTextview, distanceTextView, customerNameTextView, orderNameTextView;
    private RecyclerView recyclerView;
    private View amountsContainer;
    private OrderLinesAdapter adapter;
    private Menu menu;
    private CurrentLocationUtil currentLocationUtil;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    public void goBack() {
        finish();
    }

    @Override
    public void setOrderName(String txt) {
        orderNameTextView.setText(txt);
    }

    @Override
    public void setCustomerName(String txt) {
        customerNameTextView.setText(txt);
    }

    @Override
    public void setDistanceToCustomer(String txt) {
        distanceTextView.setText(txt);

    }

    @Override
    public void setTourName(String txt) {
        tourTextview.setText(txt);
    }

    @Override
    public void setSellerName(String txt) {
        sellerTextView.setText(txt);
    }

    @Override
    public void setDate(String txt) {
        dateTextView.setText(txt);
    }

    @Override
    public void setOrderAmount(String txt) {
        orderAmountTextview.setText(txt);
    }

    @Override
    public void setPaymentAmount(String txt) {
        paymentAmountTextview.setText(txt);
    }

    @Override
    public void setRemainingAmount(String txt) {
        remainingTextView.setText(txt);
    }

    @Override
    public void initAdapter(List<DataRow> lines) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderLinesAdapter(this, lines, null);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoadFinished(List<DataRow> lines) {
        adapter.notifyDataSetChanged();
        recyclerView.invalidate();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_details_layout);
        if(initArgs()){
            init();
            findViewById();
            initView();
        }else{
            showToast(getString(R.string.error_occurred));
            finish();
        }
    }

    private boolean initArgs(){
        Bundle data = getIntent().getExtras();
        if(data != null){
            orderId = data.getString(Col.SERVER_ID);
            if(orderId != null){
                return true;
            }
        }
        return false;
    }

    private void init(){
        presenter = new OrderDetailsPresenterImpl(this, this, app().getCurrentUser(), orderId);
        currentLocationUtil = new CurrentLocationUtil(this, this, null, false);
    }

    private void findViewById(){
        amountsContainer = findViewById(R.id.amountsContainer);
        recyclerView = findViewById(R.id.recyclerView);
        remainingTextView = findViewById(R.id.remainingTextView);
        paymentAmountTextview = findViewById(R.id.paymentAmountTextview);
        orderAmountTextview = findViewById(R.id.orderAmountTextview);
        dateTextView = findViewById(R.id.dateTextView);
        sellerTextView = findViewById(R.id.sellerTextView);
        tourTextview = findViewById(R.id.tourTextview);
        distanceTextView = findViewById(R.id.distanceTextView);
        customerNameTextView = findViewById(R.id.customerNameTextView);
        orderNameTextView = findViewById(R.id.orderNameTextView);
    }

    private void initView(){
        presenter.onViewCreated();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete_menu, menu);
        this.menu = menu;
        presenter.onCreateOptionsMenu();
        menu.findItem(R.id.menuDelete).setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void toggleDeleteMenuItem(boolean visible) {
        menu.findItem(R.id.menuDelete).setVisible(visible);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
        }else if(id == R.id.menuDelete){
            presenter.requestCancelOrder();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showCancelDialog(String title, String msg, String positiveTitle){
        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(positiveTitle, (dialogInterface, i) -> {
                    presenter.cancelOrder( OrderDetailsActivity.this);
                });
        dialogBuilder.create().show();
    }

    public static Intent getIntent(Context context, String orderId){
        Intent intent = new Intent(context, OrderDetailsActivity.class);
        Bundle data = new Bundle();
        data.putString(Col.SERVER_ID, orderId);
        intent.putExtras(data);
        return intent;
    }

    @Override
    public void requestCurrentLocation(IPaymentPresenter.LocationListener locationListener) {
        currentLocationUtil.setLocationListener(new CurrentLocationUtil.MyLocationListener() {
            @Override
            public void loading() {
                locationListener.onStart();
            }

            @Override
            public boolean printLocation(Double latitude, Double longitude) {
                locationListener.onLocationChanged(latitude, longitude);
                return true;
            }

            @Override
            public boolean checkLocation(boolean failedInformed) {
                locationListener.onFailed();
                return false;
            }
        }).execute();
    }

    @Override
    public void openOrderDetails(boolean openPayment, String orderId) {
        Intent intent = OrderDetailsActivity.getIntent(this, orderId);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void goBack(int resultCode) {
        setResult(resultCode);
        finish();
    }
}
