package com.MohafizDZ.project.payment_dir;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.MohafizDZ.framework_repository.Utils.CurrentLocationUtil;
import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.framework_repository.Utils.SwipeHelper;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.order_details_dir.OrderDetailsActivity;
import com.MohafizDZ.project.payment_details_dir.PaymentDetailsActivity;
import com.MohafizDZ.project.payment_dir.strategies.ConcretePaymentStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PaymentActivity extends MyAppCompatActivity implements IPaymentPresenter.View, View.OnClickListener {
    private static final String TAG = PaymentActivity.class.getSimpleName();
    private static final String STRATEGY_NAME_KEY = "strategy_name_key";
    private static final String CUSTOMER_ID_KEY = "customer_id_key";
    private FloatingActionButton validateFloatingActionButton, addFloatingActionButton, editFloatingActionButton;
    private View paymentContainer;
    private TextView actualBalanceTextView, orderAmountTextView, paymentAmountTextview, totalAmountTextView,
            balanceTextView, balanceLimitTextView, nameTextView, paymentAmountLabelTextView, totalAmountLabelTextView;

    private IPaymentPresenter.Presenter presenter;
    private String strategyClassName;
    private String customerId;
    private CurrentLocationUtil currentLocationUtil;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_layout);
        if(initArgs()){
            init();
            findViewById();
            setControls();
            initView();
        }else{
            finish();
        }
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
        ConcretePaymentStrategy strategy = getStrategy();
        presenter = new PaymentPresenterImpl(this, this, strategy);
        strategy.setCustomerId(customerId);
        currentLocationUtil = new CurrentLocationUtil(this, this, null, false);
//        initResultLaunchers();
    }

    private ConcretePaymentStrategy getStrategy() {
        try {
            Class<?> strategyClass = Class.forName(strategyClassName);
            Constructor<?> constructor = strategyClass.getConstructor(Context.class, IPaymentPresenter.View.class, DataRow.class);
            return (ConcretePaymentStrategy) constructor.newInstance(this, this, app().getCurrentUser());
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void findViewById() {
        addFloatingActionButton = findViewById(R.id.addFloatingActionButton);
        editFloatingActionButton = findViewById(R.id.editFloatingActionButton);
        validateFloatingActionButton = findViewById(R.id.validateFloatingActionButton);
        nameTextView = findViewById(R.id.customerTextView);
        actualBalanceTextView = findViewById(R.id.actualBalanceTextView);
        orderAmountTextView = findViewById(R.id.orderAmountTextView);
        paymentAmountTextview = findViewById(R.id.paymentAmountTextview);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        balanceTextView = findViewById(R.id.balanceTextView);
        balanceLimitTextView = findViewById(R.id.balanceLimitTextView);
        nameTextView = findViewById(R.id.customerTextView);
        totalAmountLabelTextView = findViewById(R.id.totalAmountLabelTextView);
        paymentAmountLabelTextView = findViewById(R.id.paymentAmountLabelTextView);
        paymentContainer = findViewById(R.id.paymentContainer);
    }

    private void setControls(){
        addFloatingActionButton.setOnClickListener(this);
        editFloatingActionButton.setOnClickListener(this);
        validateFloatingActionButton.setOnClickListener(this);
    }

    private void initView(){
        presenter.onViewCreated();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.validateFloatingActionButton){
            presenter.onValidate();
        }else if(id == R.id.addFloatingActionButton){
            presenter.requestAddPayment();
//            openPaymentDialog();
        }else if(id == R.id.editFloatingActionButton){
            presenter.requestEditPayment();
//            openPaymentDialog();
        }
    }

    @Override
    public void openPaymentDialog(float totalToPay, Float paymentAmount){
        TextInputLayout textInputLayout = new TextInputLayout(this);
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.margin_small);
        layoutParams.rightMargin = (int) getResources().getDimension(R.dimen.margin_small);
        TextInputEditText editText = new TextInputEditText(this);
//        editText.setOnTouchListener(
//                new SwipeHelper(this) {
//                    @Override
//                    public void onSwipeLeftToRight() {
//                        editText.setText(String.valueOf(totalToPay));
//                    }
//                });

        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.add_amount_title))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.add_label), (dialogInterface, i) -> {
                    String value = String.valueOf(editText.getText());
                    presenter.onAddPayment(value);
                });
        if(paymentAmount != null){
            editText.setText(String.valueOf(paymentAmount));
            dialogBuilder.setNeutralButton(getString(R.string.delete_label), (dialogInterface, i) -> {
                presenter.onAddPayment("0");
            });
        }
        textInputLayout.setHint(getString(R.string.amount_label));
        editText.setHint(getString(R.string.amount_label));
        textInputLayout.addView(editText);
        dialogBuilder.setView(textInputLayout);
        AlertDialog dialog = dialogBuilder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

        wmlp.gravity = Gravity.BOTTOM;
        new Handler().postDelayed(() -> {
            editText.requestFocus();
        }, 250);
        dialog.show();

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
    public void goBack(int resultCode) {
        setResult(resultCode);
        finish();
    }

    @Override
    public void openOrderDetails(boolean openPayment, String orderId) {
        Intent intent;
        if(openPayment){
            intent = PaymentDetailsActivity.getIntent(this, orderId);
        }else{
            intent = OrderDetailsActivity.getIntent(this, orderId);
        }
        IntentUtils.startActivity(this, intent);
    }

    private int getViewVisibility(boolean visible){
        return visible? View.VISIBLE : View.GONE;
    }

    @Override
    public void togglePaymentContainer(boolean visible) {
        paymentContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleAddButton(boolean visible) {
        addFloatingActionButton.setVisibility(getViewVisibility(visible));
        editFloatingActionButton.setVisibility(getViewVisibility(!visible));
    }

    @Override
    public void setName(String text) {
        nameTextView.setText(text);
    }

    @Override
    public void setBalanceLimit(String text) {
        balanceLimitTextView.setText(text);
    }

    @Override
    public void setBalance(String text) {
        balanceTextView.setText(text);
    }

    @Override
    public void setTotalToPay(String text) {
        totalAmountTextView.setText(text);
    }

    @Override
    public void setPaymentAmount(String text) {
        paymentAmountTextview.setText(text);
    }

    @Override
    public void setActualBalance(String text) {
        actualBalanceTextView.setText(text);
    }

    @Override
    public void setOrderAmount(String text) {
        orderAmountTextView.setText(text);
    }

    @Override
    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void setTotalPaymentLabel(String text) {
        totalAmountLabelTextView.setText(text);
    }

    @Override
    public void setPaymentLabel(String text) {
        paymentAmountLabelTextView.setText(text);
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
        Intent intent = new Intent(context, PaymentActivity.class);
        Bundle data = new Bundle();
        data.putString(STRATEGY_NAME_KEY, strategyClassName);
        data.putString(CUSTOMER_ID_KEY, customerId);
        intent.putExtras(data);
        return intent;
    }

}
