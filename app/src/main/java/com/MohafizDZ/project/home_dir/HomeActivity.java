package com.MohafizDZ.project.home_dir;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.MohafizDZ.framework_repository.Utils.CurrentLocationUtil;
import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.framework_repository.service.SyncUtilsInTheAppRun;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.cash_box_dir.CashBoxActivity;
import com.MohafizDZ.project.catalog_dir.CatalogActivity;
import com.MohafizDZ.project.catalog_dir.strategies_dir.MainCatalogStrategy;
import com.MohafizDZ.project.customer_details_dir.CustomerDetailsActivity;
import com.MohafizDZ.project.customers_dir.CustomersListActivity;
import com.MohafizDZ.project.dashboard_dir.DashboardActivity;
import com.MohafizDZ.project.expenses_list_dir.ExpensesListActivity;
import com.MohafizDZ.project.home_dir.details_presenter_dir.DetailsPresenterImpl;
import com.MohafizDZ.project.home_dir.details_presenter_dir.IDetailsPresenter;
import com.MohafizDZ.project.home_dir.guide_presenter_dir.GuidePresenterImpl;
import com.MohafizDZ.project.home_dir.guide_presenter_dir.IGuidePresenter;
import com.MohafizDZ.project.home_dir.pre_closing_presenter_dir.IPreClosingPresenter;
import com.MohafizDZ.project.home_dir.pre_closing_presenter_dir.PreClosingPresenterImpl;
import com.MohafizDZ.project.home_dir.visit_presenter_dir.IVisitPresenter;
import com.MohafizDZ.project.home_dir.visit_presenter_dir.VisitPresenterImpl;
import com.MohafizDZ.project.inventory_dir.InventoryActivity;
import com.MohafizDZ.project.models.PlannerModel;
import com.MohafizDZ.project.opening_stock_dir.OpeningStockActivity;
import com.MohafizDZ.project.orders_list_dir.OrdersListActivity;
import com.MohafizDZ.project.payment_dir.PaymentActivity;
import com.MohafizDZ.project.payments_list_dir.PaymentsListActivity;
import com.MohafizDZ.project.sales_dir.SalesActivity;
import com.MohafizDZ.project.scan_dir.ScanActivity;
import com.MohafizDZ.project.settings_dir.SettingsActivity;
import com.MohafizDZ.project.tour_edit_dir.TourFormActivity;
import com.MohafizDZ.project.visit_action_dir.VisitActionActivity;
import com.MohafizDZ.project.visit_action_list_dir.ActionsListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class HomeActivity extends MyAppCompatActivity implements IGuidePresenter.View, IDetailsPresenter.View,
        IVisitPresenter.View, IPreClosingPresenter.View,
        View.OnClickListener, View.OnLongClickListener {
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 7516;
    private final String TAG = HomeActivity.class.getSimpleName();
    private ImageView guideImageView;
    private TextView guideStepTextView, guideTitleTextView, visitViewDetailsButton, visitViewActionsButton,
            customerNameTextView, visitsProgressTextView;
    private MaterialButton guideMaterialButton, toggleGuideDetailsButton, visitStartButton, visitStopButton, visitRestartButton;
    private FloatingActionButton customersMaterialButton, goalFloatingActionButton;
    private View guideContainer, tourProgressContainer, guideDetailsContainer, preClosingContainer,
            visitActionsContainer, actionsContainer, customerDetailsContainer;
    private ImageView customerImageView;
    private Chip otherChip, noActionChip, backOrderChip , saleChip, refundChip, paymentChip,
            expensesChip, cashBoxChip, salesChip, inventoryChip;
    private LinearProgressIndicator visitsProgressIndicator;

    private IGuidePresenter.Presenter guidePresenter;
    private IDetailsPresenter.Presenter detailsPresenter;
    private IVisitPresenter.Presenter visitPresenter;
    private IPreClosingPresenter.Presenter preClosingPresenter;
    private ActivityResultLauncher<Intent> refreshResultLauncher, customersListResultLauncher,
            planingScanResultLauncher;
    private CurrentLocationUtil currentLocationUtil;
    private BottomNavigationView bottomNavigation;
    private Snackbar goalSnackBar, syncSnackBar;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    public void setTitleBar(ActionBar actionBar) {
        super.setTitleBar(actionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);
        findViewById();
        init();
        initView();
        setControls();
    }

    private void findViewById(){
        bottomNavigation = findViewById(R.id.bottomNavigation);
        guideContainer = findViewById(R.id.guideContainer);
        guideImageView = findViewById(R.id.guideImageView);
        guideMaterialButton = findViewById(R.id.guideMaterialButton);
        guideTitleTextView = findViewById(R.id.guideTitleTextView);
        guideStepTextView = findViewById(R.id.guideStepTextView);
        visitViewDetailsButton = findViewById(R.id.visitViewDetailsButton);
        visitViewActionsButton = findViewById(R.id.visitViewActionsButton);
        customerNameTextView = findViewById(R.id.customerNameTextView);
        toggleGuideDetailsButton = findViewById(R.id.toggleGuideDetailsButton);
        customersMaterialButton = findViewById(R.id.customersMaterialButton);
        guideDetailsContainer = findViewById(R.id.guideDetailsContainer);
        tourProgressContainer = findViewById(R.id.tourProgressContainer);
        preClosingContainer = findViewById(R.id.preClosingContainer);
        customerDetailsContainer = findViewById(R.id.customerDetailsContainer);
        customerImageView = findViewById(R.id.customerImageView);
        visitStopButton = findViewById(R.id.visitStopButton);
        visitStartButton = findViewById(R.id.visitStartButton);
        visitRestartButton = findViewById(R.id.visitRestartButton);
        actionsContainer = findViewById(R.id.actionsContainer);
        visitActionsContainer = findViewById(R.id.visitActionsContainer);
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
    }

    private void init(){
        DataRow currentUserRow = app().getCurrentUser();
        guidePresenter = new GuidePresenterImpl(this, this, currentUserRow);
        detailsPresenter = new DetailsPresenterImpl(this, this, currentUserRow);
        visitPresenter = new VisitPresenterImpl(this, this, currentUserRow);
        preClosingPresenter = new PreClosingPresenterImpl(this, this, currentUserRow);
        currentLocationUtil = new CurrentLocationUtil(this, this, null, false);
        initResultLaunchers();
    }

    private void initResultLaunchers(){
        refreshResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
            refreshPresenters(true);
        });
        customersListResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent intent = result.getData();
            if(result.getResultCode() == Activity.RESULT_OK && intent != null){
                Bundle data = intent.getExtras();
                String id = data != null? data.getString(Col.SERVER_ID) : null;
                visitPresenter.onSelectCustomer(id);
            }
        });
        planingScanResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK){
                guidePresenter.requestOpenTourForm();
            }
        });
    }

    @Override
    public void requestRefreshPresenters() {
        refreshPresenters(false);
    }

    private void refreshPresenters(boolean canRefreshGuide) {
        if(canRefreshGuide) {
            guidePresenter.onRefresh();
        }
        detailsPresenter.onRefresh();
        visitPresenter.onRefresh();
        preClosingPresenter.onRefresh();
    }

    private void initView(){
        detailsPresenter.onViewCreated();
//        guidePresenter.onViewCreated();
    }

    private void setControls(){
        guideMaterialButton.setOnClickListener(this);
        toggleGuideDetailsButton.setOnClickListener(this);
        customersMaterialButton.setOnClickListener(this);
        visitViewDetailsButton.setOnClickListener(this);
        visitViewActionsButton.setOnClickListener(this);
        visitStopButton.setOnClickListener(this);
        visitStartButton.setOnClickListener(this);
        visitRestartButton.setOnClickListener(this);
        otherChip.setOnClickListener(this);
        noActionChip.setOnClickListener(this);
        backOrderChip.setOnClickListener(this);
        saleChip.setOnClickListener(this);
        saleChip.setOnLongClickListener(this);
        backOrderChip.setOnClickListener(this);
        backOrderChip.setOnLongClickListener(this);
        paymentChip.setOnClickListener(this);
        paymentChip.setOnLongClickListener(this);
        refundChip.setOnClickListener(this);
        refundChip.setOnLongClickListener(this);
        noActionChip.setOnClickListener(this);
        noActionChip.setOnLongClickListener(this);
        otherChip.setOnClickListener(this);
        otherChip.setOnLongClickListener(this);
        expensesChip.setOnClickListener(this);
        expensesChip.setOnLongClickListener(this);
        cashBoxChip.setOnClickListener(this);
        cashBoxChip.setOnLongClickListener(this);
        salesChip.setOnClickListener(this);
        salesChip.setOnLongClickListener(this);
        inventoryChip.setOnClickListener(this);
        inventoryChip.setOnLongClickListener(this);
        goalFloatingActionButton.setOnClickListener(this);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.catalogMenuItem){
                startCatalogActivity(MainCatalogStrategy.class.getName());
            }else if(id == R.id.expensesMenuItem){
                openExpensesList();
            }else if(id == R.id.dashboardMenuItem){
                openDashboard();
            }
            return true;
        });
    }

    private void openDashboard(){
        Intent intent = DashboardActivity.getIntent(this);
        IntentUtils.startActivity(this, intent);
    }

    private void openExpensesList(){
        Intent intent = ExpensesListActivity.getIntent(this);
        IntentUtils.startActivity(this, intent);
        finish();
    }

    private void startCatalogActivity(String strategyClassName){
        startCatalogActivity(strategyClassName, null, true);
        finish();
    }

    private void startCatalogActivity(String strategyClassName, String customerId, boolean showBottomNav){
        Intent intent = CatalogActivity.getIntent(this, strategyClassName, customerId, showBottomNav);
        refreshResultLauncher.launch(intent);
    }

    @Override
    public void prepareView() {
        guidePresenter.onRefresh();
        visitPresenter.onViewCreated();
        preClosingPresenter.onViewCreated();
    }

    @Override
    public void toggleGuideContainer(boolean visible) {
        guideContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleGuideButton(boolean visible, String text) {
        guideMaterialButton.setVisibility(getViewVisibility(visible));
        guideMaterialButton.setText(text);
    }

    @Override
    public void toggleGuideStepTitle(boolean visible, String text) {
        guideStepTextView.setVisibility(getViewVisibility(visible));
        guideStepTextView.setText(text);
    }

    @Override
    public void toggleGuideTitle(String text) {
        guideTitleTextView.setText(text);
    }

    @Override
    public void setImage(int imageDrawable) {
        guideImageView.setImageDrawable(ContextCompat.getDrawable(this, imageDrawable));
    }

    @Override
    public boolean checkPhoneCallPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void requestPhonePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CALL_PHONE},
                MY_PERMISSIONS_REQUEST_CALL_PHONE);
    }

    @Override
    public void openTourForm() {
        refreshResultLauncher.launch(TourFormActivity.getIntent(this));
    }

    @Override
    public void toggleTourProgressContainer(boolean visible) {
        tourProgressContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void togglePreClosingContainer(boolean visible) {
        preClosingContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleGuideDetailsContainer(boolean visible) {
        guideDetailsContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void updateToggleButtonIcon(int drawableResId) {
        toggleGuideDetailsButton.setIcon(ContextCompat.getDrawable(this, drawableResId));
    }

    @Override
    public void requestCloseVisit() {
        visitStopButton.performClick();
    }

    @Override
    public void requestOpenInitialStock() {
        Intent intent = OpeningStockActivity.getIntent(this);
        refreshResultLauncher.launch(intent);
    }

    @Override
    public void requestPlanScan() {
        Intent intent = ScanActivity.getAdminIntent(this);
        planingScanResultLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_PHONE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                // Start the intent to make the phone call
                guidePresenter.onActionButtonClicked();
            } else {
                // Permission denied
                // Show a message to the user
                showToast(getString(R.string.permission_denied_msg));
            }
            return;
        }
    }


    private int getViewVisibility(boolean isVisible){
        return isVisible? TextView.VISIBLE : View.GONE;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.guideMaterialButton){
            guidePresenter.onActionButtonClicked();
        }else if(id == R.id.toggleGuideDetailsButton){
            guidePresenter.requestToggleGuideDetails();
        }else if(id == R.id.customersMaterialButton){
            visitPresenter.requestOpenCustomersList();
        }else if(id == R.id.visitViewDetailsButton){
            visitPresenter.requestCustomerDetails();
        }else if(id == R.id.visitStartButton){
            visitPresenter.onStartVisitClicked();
        }else if(id == R.id.visitStopButton){
            visitPresenter.onStopVisitClicked();
        }else if(id == R.id.visitRestartButton){
            visitPresenter.onRestartClicked();
        }else if(id == R.id.saleChip){
            visitPresenter.requestOpenSale();
        }else if(id == R.id.backOrderChip){
            visitPresenter.requestOpenBackOrder();
        }else if(id == R.id.noActionChip){
            visitPresenter.requestOpenNoAction();
        }else if(id == R.id.otherChip){
            visitPresenter.requestOpenOtherAction();
        }else if(id == R.id.paymentChip){
            visitPresenter.requestOpenPaymentAction(false);
        }else if(id == R.id.refundChip){
            visitPresenter.requestOpenPaymentAction(true);
        }else if(id == R.id.expensesChip){
            preClosingPresenter.requestOpenExpenses();
        }else if(id == R.id.cashBoxChip){
            preClosingPresenter.requestOpenCashBox();
        }else if(id == R.id.salesChip){
            preClosingPresenter.requestOpenSales();
        }else if(id == R.id.inventoryChip){
            preClosingPresenter.requestOpenInventory();
        }else if(id == R.id.visitViewActionsButton){
            visitPresenter.requestOpenActionsList();
        }else if(id == R.id.goalFloatingActionButton){
            visitPresenter.requestShowGoal();
        }
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
    public void openCustomersList(String tourId) {
        Intent intent = CustomersListActivity.getIntent(this, tourId, true);
        customersListResultLauncher.launch(intent);
    }

    @Override
    public void openExpenses(String tourId) {
        Intent intent = ExpensesListActivity.getIntent(this, tourId, true, true);
        refreshResultLauncher.launch(intent);
    }

    @Override
    public void openCashBox(String tourId) {
        Intent intent = CashBoxActivity.getIntent(this, tourId, true);
        refreshResultLauncher.launch(intent);
    }

    @Override
    public void openSales(String tourId) {
        Intent intent = SalesActivity.getIntent(this, tourId, true);
        refreshResultLauncher.launch(intent);
    }

    @Override
    public void openInventory(String tourId) {
        Intent intent = InventoryActivity.getIntent(this, tourId, true);
        refreshResultLauncher.launch(intent);
    }

    @Override
    public void openPaymentActivity(String strategyClassName, String customerId) {
        Intent intent = PaymentActivity.getIntent(this, strategyClassName, customerId);
        refreshResultLauncher.launch(intent);
    }

    @Override
    public void openNoActionForm(String action, String customerId) {
        Intent intent = VisitActionActivity.getIntent(this, action, customerId);
        refreshResultLauncher.launch(intent);
    }

    @Override
    public void openNoActionDetails(String actionId) {
        Intent intent = VisitActionActivity.getIntent(this, actionId, false);
        refreshResultLauncher.launch(intent);
    }

    @Override
    public void openOrderCatalog(String customerId, String strategyClassName) {
        startCatalogActivity(strategyClassName, customerId, false);
    }


    @Override
    public void toggleCustomerDetails(boolean visible) {
        customerDetailsContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void setCustomerName(String text) {
        customerNameTextView.setText(text);
    }

    @Override
    public void setCustomerImage(String text) {
//        Bitmap img = BitmapUtils.getBitmapImage(this, text);
//        customerImageView.setImageBitmap(img);
    }

    @Override
    public void openCustomerDetails(String customerId) {
        Intent intent = CustomerDetailsActivity.getIntent(this, customerId);
        refreshResultLauncher.launch(intent);
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
    public void requestCurrentLocation(IVisitPresenter.LocationListener locationListener) {
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
    public void toggleActionsContainer(boolean visible) {
        visitActionsContainer.setVisibility(getViewVisibility(visible));
        guidePresenter.onRefresh();
    }

    @Override
    public void checkSale(boolean checked) {
        saleChip.setCheckable(true);
        saleChip.setChecked(checked);
        saleChip.setCheckable(false);
    }

    @Override
    public void checkBackOrder(boolean checked) {
        backOrderChip.setCheckable(true);
        backOrderChip.setChecked(checked);
        backOrderChip.setCheckable(false);
    }

    @Override
    public void checkNoAction(boolean checked) {
        noActionChip.setCheckable(true);
        noActionChip.setChecked(checked);
        noActionChip.setCheckable(false);
    }

    @Override
    public void checkOtherAction(boolean checked) {
        otherChip.setCheckable(true);
        otherChip.setChecked(checked);
        otherChip.setCheckable(false);
    }

    @Override
    public void checkPaymentAction(boolean checked) {
        paymentChip.setCheckable(true);
        paymentChip.setChecked(checked);
        paymentChip.setCheckable(false);
    }

    @Override
    public void checkRefundAction(boolean checked) {
        refundChip.setCheckable(true);
        refundChip.setChecked(checked);
        refundChip.setCheckable(false);
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
    public void onResume() {
        super.onResume();
        try{
            bottomNavigation.setSelectedItemId(R.id.homeMenuItem);
        }catch (Exception ignored){}
    }

    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();
        if(id == R.id.saleChip|| id == R.id.backOrderChip){
            visitPresenter.requestOpenOrdersList(id == R.id.backOrderChip);
        }else if(id == R.id.noActionChip){
            visitPresenter.requestOpenNoActionsList();
        }else if(id == R.id.paymentChip || id == R.id.refundChip){
            visitPresenter.requestOpenPaymentsLis(id == R.id.refundChip);
        }else if(id == R.id.otherChip){
            visitPresenter.requestOpenOtherActionsList();
        }
        return true;
    }

    @Override
    public void openActionsList(String tourId, String customerId, String actionName) {
        Intent intent = ActionsListActivity.getIntent(this, tourId, customerId, actionName);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void openOrdersList(String tourId, String customerId, boolean isBackOrder){
        Intent intent = !isBackOrder? OrdersListActivity.getSalesIntent(this, tourId, customerId):
                OrdersListActivity.getBackOrdersIntent(this, tourId, customerId);
        refreshResultLauncher.launch(intent);
    }

    @Override
    public void openPaymentsList(String tourId, String customerId, boolean isRefund){
        Intent intent = isRefund? PaymentsListActivity.getPaymentsIntent(this, tourId, customerId, false) :
                PaymentsListActivity.getRefundsIntent(this, tourId, customerId, false);
        refreshResultLauncher.launch(intent);
    }

    @Override
    public void toggleGoalButton(boolean visible) {
        goalFloatingActionButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menuSettings){
            openSettings();
        }else if(id == R.id.menuSync){
            requestSyncUp();
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestSyncUp(){
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle(getString(R.string.sync_label));
        dialogBuilder.setMessage(getString(R.string.sync_up_msg));
        dialogBuilder.setPositiveButton(getString(R.string.sync_label), (dialogInterface, i) -> detailsPresenter.requestSyncUp());
        dialogBuilder.setNegativeButton(getString(R.string.cancel_label), null);
        dialogBuilder.create().show();;
    }

    private void openSettings(){
        Intent intent = SettingsActivity.getIntent(this);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void requestSyncPlannerModel() {
        Bundle data = new Bundle();
        data.putString("from", TAG);
        SyncUtilsInTheAppRun.requestSync(this, PlannerModel.class, PlannerModel.BASE_AUTHORITY, data);
    }

    @Override
    public void toggleSyncSnackBar(boolean visible) {
        if(visible) {
            if (syncSnackBar == null) {
                syncSnackBar = Snackbar.make(goalFloatingActionButton, "", BaseTransientBottomBar.LENGTH_INDEFINITE)
                        .setAnchorView(bottomNavigation)
                        .setAction(getString(R.string.hide_label), view -> {
                            syncSnackBar.dismiss();
                        });
            }
            runOnUiThread(() -> syncSnackBar.show());
        }else{
            runOnUiThread(() -> syncSnackBar.dismiss());
        }
    }

    @Override
    public void setSyncSnackBarTitle(String title) {
        runOnUiThread(() -> syncSnackBar.setText(title));
    }
}
