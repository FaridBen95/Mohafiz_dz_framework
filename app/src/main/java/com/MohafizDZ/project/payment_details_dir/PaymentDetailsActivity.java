package com.MohafizDZ.project.payment_details_dir;

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
import com.MohafizDZ.framework_repository.controls.ImageAdapter;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.payment_dir.IPaymentPresenter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class PaymentDetailsActivity extends MyAppCompatActivity implements IPaymentDetailsPresenter.View, IPaymentPresenter.ValidateView, View.OnClickListener {
    private static final String TAG = PaymentDetailsActivity.class.getSimpleName();
    private static final String VALIDATION_KEY = "validation_key";
    private IPaymentDetailsPresenter.Presenter presenter;
    private String paymentId;
    private TextView remainingTextView, paymentAmountTextview, dateTextView,
            sellerTextView, tourTextview, distanceTextView, customerNameTextView, referenceTextView,
            orderTextView, expenseTextView, expensesLeftTextView, expenseNoteTextView, expenseSubjectTextView;
    private View orderContainer, expensesContainer, amountsContainer, customerContainer, attachmentsContainer;
    private RecyclerView recyclerView;
    private FloatingActionButton validateFloatingActionButton;
    private Menu menu;
    private CurrentLocationUtil currentLocationUtil;
    private ImageAdapter imageAdapter;
    private boolean isValidation;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    public void goBack() {
        finish();
    }

    @Override
    public void setReference(String txt) {
        referenceTextView.setText(txt);
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
    public void setPaymentAmount(String txt) {
        paymentAmountTextview.setText(txt);
        expenseTextView.setText(txt);
    }

    @Override
    public void setRemainingAmount(String txt) {
        remainingTextView.setText(txt);
    }

    @Override
    public void setExpensesLeft(String txt) {
        expensesLeftTextView.setText(txt);
    }

    @Override
    public void toggleOrderReference(boolean visible) {
        orderContainer.setVisibility(getViewVisibility(visible));
    }

    private int getViewVisibility(boolean isVisible){
        return isVisible? View.VISIBLE : View.GONE;
    }

    @Override
    public void setOrderName(String txt) {
        orderTextView.setText(txt);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_details_layout);
        if(initArgs()){
            init();
            findViewById();
            setControls();
            initView();
        }else{
            showToast(getString(R.string.error_occurred));
            finish();
        }
    }

    private boolean initArgs(){
        Bundle data = getIntent().getExtras();
        if(data != null){
            paymentId = data.getString(Col.SERVER_ID);
            isValidation = data.getBoolean(VALIDATION_KEY);
            if(paymentId != null){
                return true;
            }
        }
        return false;
    }

    private void init(){
        presenter = new PaymentDetailsPresenterImpl(this, this, app().getCurrentUser(), paymentId);
        presenter.setExpenseValidation(isValidation);
        currentLocationUtil = new CurrentLocationUtil(this, this, null, false);
    }

    private void findViewById(){
        remainingTextView = findViewById(R.id.remainingTextView);
        paymentAmountTextview = findViewById(R.id.paymentAmountTextview);
        dateTextView = findViewById(R.id.dateTextView);
        sellerTextView = findViewById(R.id.sellerTextView);
        tourTextview = findViewById(R.id.tourTextview);
        distanceTextView = findViewById(R.id.distanceTextView);
        customerNameTextView = findViewById(R.id.customerNameTextView);
        referenceTextView = findViewById(R.id.referenceTextView);
        orderContainer = findViewById(R.id.orderContainer);
        amountsContainer = findViewById(R.id.amountsContainer);
        expensesContainer = findViewById(R.id.expensesContainer);
        customerContainer = findViewById(R.id.customerContainer);
        orderTextView = findViewById(R.id.orderTextView);
        expenseTextView = findViewById(R.id.expenseTextView);
        expensesLeftTextView = findViewById(R.id.expensesLeftTextView);
        recyclerView = findViewById(R.id.recyclerView);
        attachmentsContainer = findViewById(R.id.attachmentsContainer);
        validateFloatingActionButton = findViewById(R.id.validateFloatingActionButton);
        expenseNoteTextView = findViewById(R.id.expenseNoteTextView);
        expenseSubjectTextView = findViewById(R.id.expenseSubjectTextView);
    }

    private void setControls(){
        validateFloatingActionButton.setOnClickListener(this);
    }

    private void initView(){
        presenter.onViewCreated();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete_menu, menu);
        this.menu = menu;
        menu.findItem(R.id.menuDelete).setVisible(true);
        presenter.onCreateOptionsMenu();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void toggleDeleteMenuItem(boolean visible) {
        menu.findItem(R.id.menuDelete).setVisible(visible);
    }

    @Override
    public void togglePaymentContainer(boolean visible) {
        amountsContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleExpenseContainer(boolean visible) {
        expensesContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void setExpenseSubject(String txt) {
        expenseSubjectTextView.setText(txt);
    }

    @Override
    public void setExpenseNote(String txt) {
        expenseNoteTextView.setText(txt);
    }

    @Override
    public void toggleCustomerContainer(boolean visible) {
        customerContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void initAdapter(List<String> attachments) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageAdapter = new ImageAdapter(this, R.layout.simple_image_item, attachments, null);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(imageAdapter);
    }

    @Override
    public void toggleAttachmentsContainer(boolean visible) {
        attachmentsContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void onLoadFinished(List<String> attachments) {
        imageAdapter.notifyDataSetChanged();
        recyclerView.invalidate();
    }

    @Override
    public void toggleValidateButton(boolean visible) {
        validateFloatingActionButton.setVisibility(getViewVisibility(visible));
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
                    presenter.cancelOrder( PaymentDetailsActivity.this);
                });
        dialogBuilder.create().show();
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
        Intent intent = PaymentDetailsActivity.getIntent(this, orderId);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void goBack(int resultCode) {
        setResult(resultCode);
        finish();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.validateFloatingActionButton){
            presenter.onValidate();
        }
    }

    public static Intent getIntent(Context context, String orderId){
        return getIntent(context, orderId, false);
    }

    public static Intent getIntent(Context context, String orderId, boolean onValidation){
        Intent intent = new Intent(context, PaymentDetailsActivity.class);
        Bundle data = new Bundle();
        data.putString(Col.SERVER_ID, orderId);
        data.putBoolean(VALIDATION_KEY, onValidation);
        intent.putExtras(data);
        return intent;
    }
}
