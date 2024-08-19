package com.MohafizDZ.project.dashboard_dir;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.framework_repository.controls.KeyValueAutoComplete;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.cash_box_dir.CashBoxActivity;
import com.MohafizDZ.project.catalog_dir.CatalogActivity;
import com.MohafizDZ.project.catalog_dir.strategies_dir.MainCatalogStrategy;
import com.MohafizDZ.project.customers_dir.CustomersListActivity;
import com.MohafizDZ.project.dashboard_dir.pre_closing_presenter_dir.PreClosingPresenterImpl;
import com.MohafizDZ.project.dashboard_dir.visit_presenter_dir.IVisitPresenter;
import com.MohafizDZ.project.dashboard_dir.visit_presenter_dir.VisitPresenterImpl;
import com.MohafizDZ.project.expenses_list_dir.ExpensesListActivity;
import com.MohafizDZ.project.home_dir.HomeActivity;
import com.MohafizDZ.project.home_dir.pre_closing_presenter_dir.IPreClosingPresenter;
import com.MohafizDZ.project.inventory_dir.InventoryActivity;
import com.MohafizDZ.project.opening_stock_dir.OpeningStockActivity;
import com.MohafizDZ.project.orders_list_dir.OrdersListActivity;
import com.MohafizDZ.project.payments_list_dir.PaymentsListActivity;
import com.MohafizDZ.project.sales_dir.SalesActivity;
import com.MohafizDZ.project.tour_edit_dir.tour_details_dir.TourDetailsActivity;
import com.MohafizDZ.project.visit_action_list_dir.ActionsListActivity;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardActivity extends MyAppCompatActivity implements IDashboardPresenter.View, View.OnClickListener,
        KeyValueAutoComplete.SelectionListener, IVisitPresenter.View, IPreClosingPresenter.View {
    private static final String TAG = DashboardActivity.class.getSimpleName();

    private IDashboardPresenter.Presenter mainPresenter;
    private IVisitPresenter.Presenter visitPresenter;
    private IPreClosingPresenter.Presenter preClosingPresenter;
    private TextView visitsProgressTextView, regionTextView, vehicleTextView, tourNameTextView;
    private KeyValueAutoComplete tourDropDownView;
    private FloatingActionButton goalFloatingActionButton, customersMaterialButton;
    private Chip otherChip, noActionChip, backOrderChip , saleChip, refundChip, paymentChip,
            expensesChip, cashBoxChip, salesChip, inventoryChip;
    private View openingContainer, tourDetailsContainer, planingContainer, preClosingContainer, tourContainer,
            tourProgressContainer, preClosingDetailsContainer, customerDetailsContainer;
    private MaterialButton preClosingToggleButton, tourToggleButton, openingViewDetailsButton,
            planingViewDetailsButton, dateStartButton, dateEndButton, visitStartButton,
            visitStopButton, visitRestartButton, visitViewActionsButton;
    private LinearProgressIndicator visitsProgressIndicator;
    private BottomNavigationView bottomNavigation;
    private Snackbar goalSnackBar;
    private Map<Integer, BadgeDrawable> badgeMap = new HashMap<Integer, BadgeDrawable>();

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_layout);
        findViewById();
        init();
        initView();
        setControls();
    }

    private void findViewById(){
        bottomNavigation = findViewById(R.id.bottomNavigation);
        otherChip = findViewById(R.id.otherChip);
        noActionChip = findViewById(R.id.noActionChip);
        backOrderChip = findViewById(R.id.backOrderChip);
        saleChip = findViewById(R.id.saleChip);
        paymentChip = findViewById(R.id.paymentChip);
        refundChip = findViewById(R.id.refundChip);
        cashBoxChip = findViewById(R.id.cashBoxChip);
        expensesChip = findViewById(R.id.expensesChip);
        salesChip = findViewById(R.id.salesChip);
        inventoryChip = findViewById(R.id.inventoryChip);
        goalFloatingActionButton = findViewById(R.id.goalFloatingActionButton);
        visitsProgressIndicator = findViewById(R.id.visitsProgressIndicator);
        visitsProgressTextView = findViewById(R.id.visitsProgressTextView);
        tourDetailsContainer = findViewById(R.id.tourDetailsContainer);
        openingContainer = findViewById(R.id.openingContainer);
        planingContainer = findViewById(R.id.planingContainer);
        tourContainer = findViewById(R.id.tourContainer);
        preClosingContainer = findViewById(R.id.preClosingContainer);
        regionTextView = findViewById(R.id.regionTextView);
        vehicleTextView = findViewById(R.id.vehicleTextView);
        tourNameTextView = findViewById(R.id.tourNameTextView);
        tourDropDownView = findViewById(R.id.tourDropDownView);
        preClosingToggleButton = findViewById(R.id.preClosingToggleButton);
        tourToggleButton = findViewById(R.id.tourToggleButton);
        openingViewDetailsButton = findViewById(R.id.openingViewDetailsButton);
        planingViewDetailsButton = findViewById(R.id.planingViewDetailsButton);
        dateEndButton = findViewById(R.id.dateEndButton);
        dateStartButton = findViewById(R.id.dateStartButton);
        tourProgressContainer = findViewById(R.id.tourProgressContainer);
        preClosingDetailsContainer = findViewById(R.id.preClosingDetailsContainer);
        customerDetailsContainer = findViewById(R.id.customerDetailsContainer);
        visitStartButton = findViewById(R.id.visitStartButton);
        visitStopButton = findViewById(R.id.visitStopButton);
        visitRestartButton = findViewById(R.id.visitRestartButton);
        customersMaterialButton = findViewById(R.id.customersMaterialButton);
        visitViewActionsButton = findViewById(R.id.visitViewActionsButton);
    }

    private void init(){
        DataRow currentUserRow = app().getCurrentUser();
        mainPresenter = new DashboardPresenterImpl(this, this, currentUserRow);
        visitPresenter = new VisitPresenterImpl(this, this, currentUserRow);
        preClosingPresenter = new PreClosingPresenterImpl(this, this, currentUserRow);
    }

    private void initView(){
        bottomNavigation.setSelectedItemId(R.id.dashboardMenuItem);
        mainPresenter.onViewCreated();
    }

    private void setControls(){
        tourDropDownView.setSelectionListener(this);
        goalFloatingActionButton.setOnClickListener(this);
        preClosingToggleButton.setOnClickListener(this);
        tourToggleButton.setOnClickListener(this);
        openingViewDetailsButton.setOnClickListener(this);
        planingViewDetailsButton.setOnClickListener(this);
        dateEndButton.setOnClickListener(this);
        dateStartButton.setOnClickListener(this);
        customersMaterialButton.setOnClickListener(this);
        saleChip.setOnClickListener(this);
        backOrderChip.setOnClickListener(this);
        paymentChip.setOnClickListener(this);
        refundChip.setOnClickListener(this);
        noActionChip.setOnClickListener(this);
        otherChip.setOnClickListener(this);
        inventoryChip.setOnClickListener(this);
        salesChip.setOnClickListener(this);
        cashBoxChip.setOnClickListener(this);
        expensesChip.setOnClickListener(this);
        visitViewActionsButton.setOnClickListener(this);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.homeMenuItem){
                startHomeActivity();
            }else if(id == R.id.catalogMenuItem){
                startCatalogActivity(MainCatalogStrategy.class.getName(), true);
            }else if(id == R.id.expensesMenuItem){
                openExpensesList();
            }
            return true;
        });
    }

    private void startHomeActivity(){
        IntentUtils.startActivity(this, HomeActivity.class, null);
        finish();
    }

    private void openExpensesList(){
        Intent intent = ExpensesListActivity.getIntent(this);
        IntentUtils.startActivity(this, intent);
        finish();
    }

    private void startCatalogActivity(String strategyClassName, boolean showBottomNav){
        Intent intent = CatalogActivity.getIntent(this, strategyClassName, null, showBottomNav);
        IntentUtils.startActivity(this, intent);
    }

    public static Intent getIntent(Context context){
        return new Intent(context, DashboardActivity.class);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.preClosingToggleButton) {
            mainPresenter.requestTogglePreClosingContainer();
        } else if (id == R.id.tourToggleButton) {
            mainPresenter.requestToggleProgressContainer();
        } else if (id == R.id.openingViewDetailsButton) {
            mainPresenter.requestOpenTourOpening();
        } else if (id == R.id.planingViewDetailsButton) {
            mainPresenter.requestOpenTourPlan();
        }else if(id == R.id.dateStartButton){
            mainPresenter.requestSelectStartDate();
        }else if(id == R.id.dateEndButton){
            mainPresenter.requestSelectEndDate();
        }else if(id == R.id.goalFloatingActionButton){
            visitPresenter.requestShowGoal();
        }else if(id == R.id.saleChip){
            visitPresenter.requestOpenSales();
        }else if(id == R.id.backOrderChip){
            visitPresenter.requestOpenBackOrders();
        }else if(id == R.id.paymentChip){
            visitPresenter.requestOpenPayments(false);
        }else if(id == R.id.refundChip){
            visitPresenter.requestOpenPayments(true);
        }else if(id == R.id.noActionChip){
            visitPresenter.requestOpenNoActions();
        }else if(id == R.id.otherChip){
            visitPresenter.requestOpenOtherActionsList();
        }else if(id == R.id.visitViewActionsButton){
            visitPresenter.requestOpenActionsList();
        }else if(id == R.id.customersMaterialButton){
            visitPresenter.requestOpenCustomersList();
        }else if(id == R.id.expensesChip){
            preClosingPresenter.requestOpenExpenses();
        }else if(id == R.id.cashBoxChip){
            preClosingPresenter.requestOpenCashBox();
        }else if(id == R.id.salesChip){
            preClosingPresenter.requestOpenSales();
        }else if(id == R.id.inventoryChip){
            preClosingPresenter.requestOpenInventory();
        }
    }

    @Override
    public void requestOpenActions(String tourId, String actionName) {
        Intent intent = ActionsListActivity.getIntent(this, tourId, null, actionName);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void requestOpenPayments(String tourId, boolean isRefund) {
        Intent intent = isRefund? PaymentsListActivity.getRefundsIntent(this, tourId, true) :
                PaymentsListActivity.getPaymentsIntent(this, tourId, true);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void requestOpenSales(String tourId) {
        Intent intent = OrdersListActivity.getSalesIntent(this, tourId);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void requestOpenBackOrders(String tourId) {
        Intent intent = OrdersListActivity.getBackOrdersIntent(this, tourId);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void requestOpenCustomersList(String tourId) {
        Intent intent = CustomersListActivity.getIntent(this, tourId);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void updateProgressToggleButtonIcon(int drawableResId) {
        tourToggleButton.setIcon(ContextCompat.getDrawable(this, drawableResId));
    }

    @Override
    public void updatePreClosingToggleButtonIcon(int drawableResId) {
        preClosingToggleButton.setIcon(ContextCompat.getDrawable(this, drawableResId));
    }

    @Override
    public void togglePreClosingContainer(boolean visible) {
        preClosingDetailsContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleProgressContainer(boolean visible) {
        tourProgressContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void requestSelectDateRange(Pair<Long, Long> dateRange) {
        final MaterialDatePicker<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker()
                .setSelection(dateRange)
                .build();
        builder.addOnPositiveButtonClickListener(selection -> {
            mainPresenter.onSelectDateRange(selection);
        });
        builder.show(getSupportFragmentManager(), TAG);
    }

    @Override
    public void setDateStart(String date) {
        dateStartButton.setText(date);
    }

    @Override
    public void setDateEnd(String date) {
        dateEndButton.setText(date);
    }

    @Override
    public void openTourPlanForm(String tourId) {
        final Intent intent = TourDetailsActivity.getIntent(this, tourId);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void openInitialStock(String tourId) {
        Intent intent = OpeningStockActivity.getIntent(this, tourId);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void setTourList(LinkedHashMap<String, String> tourList, DataRow currentTourRow) {
        tourDropDownView.setFilterBasedOnSelection(false);
        tourDropDownView.setDefaultValue("");
        tourDropDownView.setKeyValueMap(tourList, true);
        if(currentTourRow != null) {
            tourDropDownView.setText(currentTourRow.getString("name"));
        }else{
            tourDropDownView.setText("");
        }
    }

    private int getViewVisibility(boolean visible){
        return visible? View.VISIBLE : View.GONE;
    }

    @Override
    public void refreshVisitContainer(boolean enabled, String tourId) {
        tourDetailsContainer.setVisibility(getViewVisibility(enabled));
        visitPresenter.setTourId(tourId);
    }

    @Override
    public void refreshPreClosingContainer(boolean enabled, String tourId) {
        preClosingDetailsContainer.setVisibility(getViewVisibility(enabled));
        preClosingPresenter.setTourId(tourId);
    }

    @Override
    public void toggleTourPlaning(boolean visible) {
        planingContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleTourOpening(boolean visible) {
        openingContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleTourProgress(boolean visible) {
        tourContainer.setVisibility(getViewVisibility(visible));
        if(!visible){
            tourProgressContainer.setVisibility(getViewVisibility(false));
        }
    }

    @Override
    public void toggleTourPreClosing(boolean visible) {
        preClosingContainer.setVisibility(getViewVisibility(visible));
        if(!visible){
            preClosingDetailsContainer.setVisibility(getViewVisibility(false));
        }
    }

    @Override
    public void setTourName(String text) {
        tourNameTextView.setText(text);
    }

    @Override
    public void setVehicle(String text) {
        vehicleTextView.setText(text);
    }

    @Override
    public void setRegion(String text) {
        regionTextView.setText(text);
    }

    @Override
    public void onSelect(View view, String key, String value, boolean perTyping) {
        mainPresenter.onSelectTour(key);
    }

    @Override
    public void toggleCustomerDetails(boolean visible) {
        customerDetailsContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleStartVisit(boolean visible) {
        visitStartButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleStopVisit(boolean visible) {
        visitStopButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleRestartVisit(boolean visible) {
        visitRestartButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleCustomersListButton(boolean visible) {
        customersMaterialButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void checkSale(int count) {
        attachBadge(saleChip, count);
    }

    @Override
    public void checkBackOrder(int count) {
        attachBadge(backOrderChip, count);
    }

    @Override
    public void checkNoAction(int count) {
        attachBadge(noActionChip, count);
    }

    @Override
    public void checkOtherAction(int count) {
        attachBadge(otherChip, count);
    }

    @Override
    public void checkPaymentAction(int count) {
        attachBadge(paymentChip, count);
    }

    @Override
    public void checkRefundAction(int count) {
        attachBadge(refundChip, count);
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    private void attachBadge(Chip view, int count){
        view.setCheckable(true);
        view.setChecked(count > 0);
        view.setCheckable(false);
        BadgeDrawable badgeDrawable = badgeMap.getOrDefault(view.getId(), BadgeDrawable.create(this));
        badgeDrawable.setText(count + "");
        badgeDrawable.setVisible(count > 0);
        badgeDrawable.setBadgeGravity(BadgeDrawable.TOP_END);
        badgeMap.put(view.getId(), badgeDrawable);
        view.addOnLayoutChangeListener((v, i, i1, i2, i3, i4, i5, i6, i7) -> {
            BadgeUtils.attachBadgeDrawable(badgeDrawable, view);
        });
    }

    @Override
    public void toggleSaleChip(boolean visible) {
        saleChip.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleBackOrderChip(boolean visible) {
        backOrderChip.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleNoActionChip(boolean visible) {
        noActionChip.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleOtherChip(boolean visible) {
        otherChip.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void togglePaymentChip(boolean visible) {
        paymentChip.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleRefundChip(boolean visible) {
        refundChip.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void showGoalSnackBar(String goalText) {
        if(goalSnackBar == null){
            goalSnackBar = Snackbar.make(goalFloatingActionButton, goalText, BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setAnchorView(bottomNavigation)
                    .setAction(getString(R.string.hide_label), view -> {
                        goalSnackBar.dismiss();
                        goalFloatingActionButton.setVisibility(getViewVisibility(true));
                    });
        }
        goalSnackBar.setText(goalText);
        goalSnackBar.show();
        goalFloatingActionButton.setVisibility(getViewVisibility(false));

    }

    @Override
    public void setVisitsProgress(int visitedCount, int plannedVisitsCount) {
        if(plannedVisitsCount == 0){
            visitsProgressTextView.setVisibility(getViewVisibility(false));
            visitsProgressIndicator.setProgress(100);
        }else {
            visitsProgressTextView.setVisibility(getViewVisibility(true));
            visitsProgressIndicator.setProgress(visitedCount* 100 / plannedVisitsCount);
            String progressText = " " + visitedCount + " / " + plannedVisitsCount;
            visitsProgressTextView.setText(progressText);
        }
    }

    @Override
    public void toggleCashBox(Boolean visible) {
        cashBoxChip.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void checkExpenses(Boolean checked) {
        expensesChip.setCheckable(true);
        expensesChip.setChecked(checked);
        expensesChip.setCheckable(false);
    }

    @Override
    public void checkCashBox(Boolean checked) {
        cashBoxChip.setCheckable(true);
        cashBoxChip.setChecked(checked);
        cashBoxChip.setCheckable(false);
    }

    @Override
    public void checkSales(Boolean checked) {
        salesChip.setCheckable(true);
        salesChip.setChecked(checked);
        salesChip.setCheckable(false);
    }

    @Override
    public void checkInventory(Boolean checked) {
        inventoryChip.setCheckable(true);
        inventoryChip.setChecked(checked);
        inventoryChip.setCheckable(false);
    }

    @Override
    public void openExpenses(String tourId) {
        Intent intent = ExpensesListActivity.getIntent(this, tourId, true);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void openCashBox(String tourId) {
        Intent intent = CashBoxActivity.getIntent(this, tourId);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void openSales(String tourId) {
        Intent intent = SalesActivity.getIntent(this, tourId, false);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void openInventory(String tourId) {
        Intent intent = InventoryActivity.getIntent(this, tourId, false);
        IntentUtils.startActivity(this, intent);
    }
}
