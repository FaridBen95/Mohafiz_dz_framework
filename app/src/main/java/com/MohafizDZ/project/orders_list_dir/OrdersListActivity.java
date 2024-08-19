package com.MohafizDZ.project.orders_list_dir;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.MohafizDZ.framework_repository.controls.KeyValueAutoComplete;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.order_details_dir.OrderDetailsActivity;
import com.MohafizDZ.project.orders_list_dir.filter_presenter_dir.Filters;
import com.MohafizDZ.project.orders_list_dir.filter_presenter_dir.FiltersPresenterImpl;
import com.MohafizDZ.project.orders_list_dir.filter_presenter_dir.IFiltersPresenter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.sidesheet.SideSheetBehavior;
import com.google.android.material.sidesheet.SideSheetCallback;

import java.util.LinkedHashMap;
import java.util.List;

public class OrdersListActivity extends MyAppCompatActivity implements IOrderListPresenter.View,
        OrdersAdapter.OnItemClickListener, IFiltersPresenter.View, View.OnClickListener, RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener, KeyValueAutoComplete.SelectionListener {
    private static final String TAG = OrdersListActivity.class.getSimpleName();
    private static final String TOUR_ID_KEY = "tour_id_key";
    private static final String CUSTOMER_ID_KEY = "CUSTOMER_ID_KEY";
    private static final String IS_SALES_KEY = "is_sales_key";
    private static final String IS_BACK_ORDERS_KEY = "is_back_orders_key";
    private IOrderListPresenter.Presenter presenter;
    private IFiltersPresenter.Presenter filterPresenter;


    private RecyclerView recyclerView;
    private View appBarLayout, sideSheetContainer;
    private MaterialButton filterCloseButton, resetButton, dateEndButton, dateStartButton;
    private RadioGroup orderByRadioGroup;
    private KeyValueAutoComplete customerDropDownView, regionDropDownView, tourDropDownView, productDropDownView;
    private MaterialSwitch reverseSwitch;
    private OrdersAdapter adapter;
    private ActivityResultLauncher<Intent> orderResultLauncher;
    private SideSheetBehavior<View> sideSheetBehavior;
    private SearchBar searchBar;
    private SearchView searchView;
    private String tourId, customerId;
    private boolean isSales, isBackOrders;

    @Override
    public Toolbar setToolBar() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orders_list_layout);
        initArgs();
        init();
        findViewById();
        setControls();
        initView();
    }

    private void initArgs(){
        Bundle data = getIntent().getExtras();
        tourId = data != null? data.getString(TOUR_ID_KEY) : null;
        customerId = data != null? data.getString(CUSTOMER_ID_KEY) : null;
        isSales = data != null && data.getBoolean(IS_SALES_KEY);
        isBackOrders = data != null && data.getBoolean(IS_BACK_ORDERS_KEY);
    }

    private void init(){
        DataRow currentUserRow = app().getCurrentUser();
        presenter = new OrdersListPresenterImpl(this, this, currentUserRow);
        filterPresenter = new FiltersPresenterImpl(this, this, currentUserRow);
        filterPresenter.setTourId(tourId);
        filterPresenter.setCustomerId(customerId);
        filterPresenter.setSalesFilter(isSales);
        filterPresenter.setBackOrdersFilter(isBackOrders);
        initResultLauncher();
    }

    private void initResultLauncher(){
        orderResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
            presenter.onRefresh();
        });
    }

    @Override
    public void setToolbarTitle(String title) {
        searchBar.setTitle(title);
    }

    private void findViewById() {
        recyclerView = findViewById(R.id.recyclerView);
        appBarLayout = findViewById(R.id.appBarLayout);
        sideSheetContainer = findViewById(R.id.sideSheetContainer);
        sideSheetContainer = findViewById(R.id.sideSheetContainer);
        sideSheetBehavior = SideSheetBehavior.from(sideSheetContainer);
        filterCloseButton = findViewById(R.id.filterCloseButton);
        resetButton = findViewById(R.id.resetButton);
        productDropDownView = findViewById(R.id.productDropDownView);
        tourDropDownView = findViewById(R.id.tourDropDownView);
        customerDropDownView = findViewById(R.id.customerDropDownView);
        regionDropDownView = findViewById(R.id.regionDropDownView);
        dateEndButton = findViewById(R.id.dateEndButton);
        dateStartButton = findViewById(R.id.dateStartButton);
        orderByRadioGroup = findViewById(R.id.orderByRadioGroup);
        reverseSwitch = findViewById(R.id.reverseSwitch);
        searchBar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.searchView);
    }

    private void setControls(){
        filterCloseButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);
        dateStartButton.setOnClickListener(this);
        dateEndButton.setOnClickListener(this);
        orderByRadioGroup.setOnCheckedChangeListener(this);
        reverseSwitch.setOnCheckedChangeListener(this);
        customerDropDownView.setSelectionListener(this);
        regionDropDownView.setSelectionListener(this);
        tourDropDownView.setSelectionListener(this);
        productDropDownView.setSelectionListener(this);
        searchBar.setNavigationOnClickListener(view -> searchView.show());
        searchBar.setOnClickListener(this);
        searchBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if(id == R.id.menuFilter){
                SideSheetBehavior.from(sideSheetContainer).expand();
            }
            return true;
        });
        searchView.addTransitionListener(
                (searchView, previousState, newState) -> {
                    if (newState == SearchView.TransitionState.SHOWN) {
                        appBarLayout.setVisibility(View.INVISIBLE);
                        // Handle search view opened.
                    }else{
                        appBarLayout.setVisibility(View.VISIBLE);
                    }
                });
        final EditText editText = searchView
                .getEditText();
        editText.setOnEditorActionListener(
                (v, actionId, event) -> {
                    presenter.onSearch(searchView.getText().toString());
                    searchBar.setText(searchView.getText());
                    searchView.hide();
                    return false;
                });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                presenter.onSearch(editText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void initView(){
        presenter.onViewCreated();
        filterPresenter.onViewCreated();
        new Handler().postDelayed(() -> sideSheetContainer.setVisibility(View.VISIBLE), 200);
        sideSheetBehavior.addCallback(new SideSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View sheet, int newState) {
                new Handler().postDelayed(() -> sideSheetContainer.setVisibility(View.VISIBLE), 200);

            }

            @Override
            public void onSlide(@NonNull View sheet, float slideOffset) {
                new Handler().postDelayed(() -> sideSheetContainer.setVisibility(View.VISIBLE), 200);

            }
        });
        searchBar.inflateMenu(R.menu.catalog_menu);
        Menu menu = searchBar.getMenu();
        menu.findItem(R.id.menuEmpty).setVisible(false);
        menu.findItem(R.id.menuScan).setVisible(false);
    }

    @Override
    public void initAdapter(List<DataRow> rows) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrdersAdapter(this, rows, this);
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
    public void requestOpenDetails(String orderId) {
        final Intent intent = OrderDetailsActivity.getIntent(this, orderId);
        orderResultLauncher.launch(intent);
    }


    @Override
    public void onItemClick(int position) {
        presenter.onItemClick(position);
    }

    @Override
    public void onItemLongClick(int position) {

    }

    @Override
    public void setReverseChecked(boolean checked) {
        reverseSwitch.setChecked(checked);
    }

    @Override
    public void setDateStartFilter(String dateStr) {
        dateStartButton.setText(dateStr);
    }

    @Override
    public void setDateEndFilter(String dateStr) {
        dateEndButton.setText(dateStr);
    }

    @Override
    public void setCustomerFilter(String customerName) {
        customerDropDownView.setText(customerName);
    }

    @Override
    public void setRegionFilter(String regionName) {
        regionDropDownView.setText(regionName);
    }

    @Override
    public void setTourFilter(String tourName) {
        tourDropDownView.setText(tourName);
    }

    @Override
    public void setProductFilter(String productName) {
        productDropDownView.setText(productName);
    }

    @Override
    public void orderByRef() {
        orderByRadioGroup.check(R.id.nameRadioButton);
    }

    @Override
    public void orderByDate() {
        orderByRadioGroup.check(R.id.dateRadioButton);
    }

    @Override
    public void orderByCustomer() {
        orderByRadioGroup.check(R.id.customerRadioButton);
    }

    @Override
    public void orderByAmount() {
        orderByRadioGroup.check(R.id.amountRadioButton);
    }

    @Override
    public void setFilters(Filters filters) {
        presenter.setFilters(filters);
        presenter.onRefresh();
    }

    @Override
    public void initCustomersFilter(LinkedHashMap<String, String> hashMap, String customerName) {
        customerDropDownView.setDefaultValue("");
        customerDropDownView.setKeyValueMap(hashMap, true);
        if(customerName != null){
            customerDropDownView.setText(customerName);
        }else {
            customerDropDownView.setText("");
        }
    }

    @Override
    public void initRegionsFilter(LinkedHashMap<String, String> hashMap) {
        regionDropDownView.setDefaultValue("");
        regionDropDownView.setKeyValueMap(hashMap, true);
        regionDropDownView.setText("");
    }

    @Override
    public void initToursFilter(LinkedHashMap<String, String> hashMap, String selectedTourName) {
        tourDropDownView.setDefaultValue("");
        tourDropDownView.setKeyValueMap(hashMap, true);
        if(selectedTourName != null){
            tourDropDownView.setText(selectedTourName);
        }else {
            tourDropDownView.setText("");
        }
    }

    @Override
    public void initProductsFilter(LinkedHashMap<String, String> hashMap) {
        productDropDownView.setDefaultValue("");
        productDropDownView.setKeyValueMap(hashMap, true);
        productDropDownView.setText("");
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.resetButton){
            filterPresenter.onResetClicked();
        }else if(id == R.id.filterCloseButton){
            sideSheetBehavior.hide();
        }else if(id == R.id.dateStartButton){
            filterPresenter.requestSelectStartDate();
        }else if(id == R.id.dateEndButton){
            filterPresenter.requestSelectEndDate();
        }else if(id == R.id.toolbar){
            searchView.show();
        }
    }

    @Override
    public void requestSelectDateRange(Pair<Long, Long> dateRange) {
        final MaterialDatePicker<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker()
                .setSelection(dateRange)
                .build();
        builder.addOnPositiveButtonClickListener(selection -> {
            filterPresenter.onSelectDateRange(selection);
        });
        builder.show(getSupportFragmentManager(), TAG);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {
        if(id == R.id.nameRadioButton){
            filterPresenter.orderByRef();
        }else if(id == R.id.dateRadioButton){
            filterPresenter.orderByDate();
        }else if(id == R.id.customerRadioButton){
            filterPresenter.orderByCustomer();
        }else if(id == R.id.amountRadioButton){
            filterPresenter.orderByAmount();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(compoundButton.getId() == R.id.reverseSwitch){
            filterPresenter.reverseSortBy(b);
        }
    }

    @Override
    public void onSelect(View view, String key, String value, boolean perTyping) {
        int id = view.getId();
        if(key.equals(KeyValueAutoComplete.DEFAULT_KEY)){
            key = null;
            value = "";
        }
        if(id == R.id.customerDropDownView){
            filterPresenter.onCustomerSelected(key, value);
        }else if(id == R.id.regionDropDownView){
            filterPresenter.onRegionSelected(key, value);
        }else if(id == R.id.productDropDownView){
            filterPresenter.onProductSelected(key, value);
        }else if(id == R.id.tourDropDownView){
            filterPresenter.onTourSelected(key, value);
        }
    }

    public static Intent getIntent(Context context){
        return getIntent(context, null);
    }

    public static Intent getIntent(Context context, String tourId){
        Intent intent = new Intent(context, OrdersListActivity.class);
        Bundle data = new Bundle();
        data.putString(TOUR_ID_KEY, tourId);
        intent.putExtras(data);
        return intent;
    }

    public static Intent getSalesIntent(Context context, String tourId){
        return getSalesIntent(context, tourId, null);
    }

    public static Intent getSalesIntent(Context context, String tourId, String customerId){
        Intent intent = new Intent(context, OrdersListActivity.class);
        Bundle data = new Bundle();
        data.putString(TOUR_ID_KEY, tourId);
        data.putString(CUSTOMER_ID_KEY, customerId);
        data.putBoolean(IS_SALES_KEY, true);
        intent.putExtras(data);
        return intent;
    }

    public static Intent getBackOrdersIntent(Context context, String tourId){
        return getBackOrdersIntent(context, tourId, null);
    }

    public static Intent getBackOrdersIntent(Context context, String tourId, String customerId){
        Intent intent = new Intent(context, OrdersListActivity.class);
        Bundle data = new Bundle();
        data.putString(TOUR_ID_KEY, tourId);
        data.putString(CUSTOMER_ID_KEY, customerId);
        data.putBoolean(IS_BACK_ORDERS_KEY, true);
        intent.putExtras(data);
        return intent;
    }
}
