package com.MohafizDZ.project.expenses_list_dir;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.MohafizDZ.project.catalog_dir.CatalogActivity;
import com.MohafizDZ.project.catalog_dir.strategies_dir.MainCatalogStrategy;
import com.MohafizDZ.project.dashboard_dir.DashboardActivity;
import com.MohafizDZ.project.expenses_list_dir.expense_dir.ExpenseActivity;
import com.MohafizDZ.project.home_dir.HomeActivity;
import com.MohafizDZ.project.payment_details_dir.PaymentDetailsActivity;
import com.MohafizDZ.project.payments_list_dir.PaymentsAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ExpensesListActivity extends MyAppCompatActivity implements IExpensesPresenter.View, View.OnClickListener, PaymentsAdapter.OnItemClickListener {
    private static final String TAG = ExpensesListActivity.class.getSimpleName();
    private static final String IS_VALIDATION_KEY = "is_validation_key";
    private static final String IS_EDITABLE_KEY = "is_editable_key";
    private static final String TOUR_ID_KEY = "tour_id_key";

    private IExpensesPresenter.Presenter presenter;
    private RecyclerView recyclerView;
    private FloatingActionButton addFloatingActionButton, validateFloatingActionButton;
    private BottomNavigationView bottomNavigation;
    private TextView expensesTextView, validatedTextView, expensesLimitTextView;
    private View totalAmountsContainer, expensesLimitContainer;
    private PaymentsAdapter adapter;
    private ActivityResultLauncher<Intent> refreshResultLauncher;
    private boolean isValidation, isEditable;
    private String tourId;


    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expenses_list);
        initArgs();
        init();
        findViewById();
        setControls();
        initView();
    }

    private void initArgs(){
        Bundle data = getIntent().getExtras();
        isValidation = data != null && data.getBoolean(IS_VALIDATION_KEY);
        isEditable = data != null && data.getBoolean(IS_EDITABLE_KEY);
        tourId = data != null? data.getString(TOUR_ID_KEY) : null;
    }

    private void init(){
        presenter = !isValidation? new BaseExpensesPresenterImpl(this, this, app().getCurrentUser()):
                new ExpensesValidationPresenterImpl(this, this, app().getCurrentUser());
        presenter.setTourId(tourId);
        presenter.setEditable(isEditable);
        initResultLauncher();
    }

    private void initResultLauncher(){
        refreshResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
            presenter.onRefresh();
        });
    }

    private void findViewById() {
        addFloatingActionButton = findViewById(R.id.addFloatingActionButton);
        validateFloatingActionButton = findViewById(R.id.validateFloatingActionButton);
        recyclerView = findViewById(R.id.recyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        expensesTextView = findViewById(R.id.expensesTextView);
        validatedTextView = findViewById(R.id.validatedTextView);
        totalAmountsContainer = findViewById(R.id.totalAmountsContainer);
        expensesLimitContainer = findViewById(R.id.expensesLimitContainer);
        expensesLimitTextView = findViewById(R.id.expensesLimitTextView);
    }

    private void setControls(){
        addFloatingActionButton.setOnClickListener(this);
        validateFloatingActionButton.setOnClickListener(this);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.homeMenuItem){
                startHomeActivity();
            }else if(id == R.id.catalogMenuItem){
                startCatalogActivity();
            }else if(id == R.id.dashboardMenuItem){
                openDashboard();
            }
            return true;
        });
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onCustomBackPressed();
            }
        });
    }

    private void openDashboard() {
        Intent intent = DashboardActivity.getIntent(this);
        IntentUtils.startActivity(this, intent);
    }

    private void onCustomBackPressed(){
        if(bottomNavigation.getVisibility() == View.VISIBLE) {
            startHomeActivity();
        }else{
            finish();
        }
    }

    private void startCatalogActivity(){
        Intent intent = CatalogActivity.getIntent(this, MainCatalogStrategy.class.getName(), null, true);
        IntentUtils.startActivity(this, intent);
        finish();
    }


    private void startHomeActivity(){
        IntentUtils.startActivity(this, HomeActivity.class, null);
        finish();
    }

    private void initView(){
        presenter.onViewCreated();
        bottomNavigation.setSelectedItemId(R.id.expensesMenuItem);
    }

    private int getViewVisibility(boolean isVisible){
        return isVisible? View.VISIBLE : View.GONE;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.addFloatingActionButton){
            presenter.requestCreateExpense();
        }else if(id == R.id.validateFloatingActionButton){
            presenter.requestValidateExpenses();
        }
    }

    @Override
    public void initAdapter(List<DataRow> rows) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentsAdapter(this, rows, this);
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
    public void onItemClick(int position) {
        presenter.onItemClick(position);
    }

    @Override
    public void onItemLongClick(int position) {
        presenter.onItemLongClick(position);
    }

    @Override
    public void requestOpenDetails(String paymentId, boolean canValidateExpense) {
        final Intent intent = PaymentDetailsActivity.getIntent(this, paymentId, canValidateExpense);
        refreshResultLauncher.launch(intent);
    }

    @Override
    public void openExpenseActivity() {
        Intent intent = ExpenseActivity.getIntent(this);
        refreshResultLauncher.launch(intent);
    }

    @Override
    public void toggleBottomNavigation(boolean isVisible) {
        bottomNavigation.setVisibility(getViewVisibility(isVisible));
    }

    @Override
    public void toggleTotalContainer(boolean isVisible) {
        totalAmountsContainer.setVisibility(isVisible? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setTotalExpenses(String txt) {
        expensesTextView.setText(txt);
    }

    @Override
    public void setValidatedExpenses(String txt) {
        validatedTextView.setText(txt);
    }

    @Override
    public void setExpensesLimit(boolean visible, String txt) {
        expensesLimitTextView.setText(txt);
        expensesLimitContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleValidateButton(boolean visible) {
        validateFloatingActionButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void showConfirmationDialog(String title, String msg) {
        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.validate_label), (dialogInterface, i) -> {
                    presenter.validateExpenses();
                });
        dialogBuilder.create().show();
    }

    @Override
    public void toggleCreateButton(boolean visible) {
        addFloatingActionButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void requestValidateItem(IExpensesPresenter.ValidateDialogListener dialogListener) {
        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.validate_expense_title))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.validate_label), (dialogInterface, i) -> {
                    dialogListener.onValidate();
                });
        dialogBuilder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            onCustomBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent getIntent(Context context) {
        return getIntent(context, null, false);
    }

    public static Intent getIntent(Context context, String tourId, boolean isValidation) {
        return getIntent(context, tourId, isValidation, false);
    }

    public static Intent getIntent(Context context, String tourId, boolean isValidation, boolean isEditable) {
        final Intent intent = new Intent(context, ExpensesListActivity.class);
        Bundle data = new Bundle();
        data.putBoolean(IS_VALIDATION_KEY, isValidation);
        data.putBoolean(IS_EDITABLE_KEY, isEditable);
        data.putString(TOUR_ID_KEY, tourId);
        intent.putExtras(data);
        return intent;
    }
}
