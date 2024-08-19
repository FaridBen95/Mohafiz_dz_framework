package com.MohafizDZ.project.customers_dir.customers_map_dir;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;

import com.MohafizDZ.framework_repository.Utils.CurrentLocationUtil;
import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.framework_repository.controls.KeyValueAutoComplete;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.customer_details_dir.CustomerDetailsActivity;
import com.MohafizDZ.project.customers_dir.Filters;
import com.MohafizDZ.project.customers_dir.filters_presenter_dir.FiltersPresenterImpl;
import com.MohafizDZ.project.customers_dir.filters_presenter_dir.IFiltersPresenter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.sidesheet.SideSheetBehavior;
import com.google.android.material.sidesheet.SideSheetCallback;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.LinkedHashMap;

public class CustomersMapActivity extends MyAppCompatActivity implements ICustomersMapPresenter.View,
        CurrentLocationUtil.MyLocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerDragListener,
        IFiltersPresenter.View, View.OnClickListener, RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener, KeyValueAutoComplete.SelectionListener, GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = CustomersMapActivity.class.getSimpleName();
    private static final String TOUR_ID_KEY = "TOUR_ID_KEY";
    private static final String SELECTED_CUSTOMER_ID_KEY = "selected_customer_id_key";
    private static final String SELECT_CUSTOMER_MODE_KEY = "select_customer_mode_key";
    private static final String EDITABLE_KEY = "editable_key";
    private static final String FILTERS_KEY = "filters_key";
    private ICustomersMapPresenter.Presenter presenter;
    private IFiltersPresenter.Presenter filtersPresenter;
    private GoogleMap map;
    private SearchBar searchBar;
    private SearchView searchView;
    private MaterialButton filterCloseButton, resetButton, dateStartButton, dateEndButton;
    private MaterialSwitch reverseSwitch, hasBalanceLimitSwitch, plannedCustomersSwitch;
    private TextInputEditText balanceStartEditText, balanceEndEditText;
    private TextInputLayout regionTextInputLayout, tourTextInputLayout;
    private KeyValueAutoComplete tourDropDownView, regionDropDownView, categoryDropDownView, visitStateDropDownView, proximityDropDownView;
    private RadioGroup orderByRadioGroup;
    private View appBarLayout, datesContainer, dateRadioButton, visitStateContainer;
    private View sideSheetContainer;
    private SideSheetBehavior<View> sideSheetBehavior;
    private CurrentLocationUtil currentLocationUtil;
    private String tourId, selectedCustomerId;
    private boolean selectCustomerMode, editable;
    private Filters filters;

    @Override
    public Toolbar setToolBar() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customers_map_layout);
        initArgs();
        init();
        findViewById();
        setControls();
        initView();
    }

    private void initArgs(){
        Bundle data = getIntent().getExtras();
        tourId = data != null? data.getString(TOUR_ID_KEY) : null;
        selectedCustomerId = data != null? data.getString(SELECTED_CUSTOMER_ID_KEY) : null;
        selectCustomerMode = data != null && data.getBoolean(SELECT_CUSTOMER_MODE_KEY);
        editable = data != null && data.getBoolean(EDITABLE_KEY);
        filters = data != null? (Filters) data.getSerializable(FILTERS_KEY) : null;
    }

    private void init(){
        DataRow currentUserRow = app().getCurrentUser();
        presenter = new CustomersMapPresenterImpl(this, this, currentUserRow);
        filtersPresenter = new FiltersPresenterImpl(this, this, currentUserRow);
        filtersPresenter.setTourId(tourId);
        filtersPresenter.setFilters(filters);
        filtersPresenter.setSelectCustomerMode(selectCustomerMode);
        presenter.setSelectCustomerMode(selectCustomerMode);
        presenter.setEditable(editable);
        currentLocationUtil = new CurrentLocationUtil(this, this, this, false);
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(googleMap -> {
                    map = googleMap;
                    filtersPresenter.onViewCreated();
                    map.setOnMarkerDragListener(CustomersMapActivity.this);
                    map.setOnInfoWindowClickListener(this);
                    map.setOnMyLocationClickListener(location -> {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));
                        map.animateCamera(CameraUpdateFactory.zoomTo(14), 800, null);
                    });
//                    map.setOnMyLocationButtonClickListener(() -> {
////                        getCurrentLocation();
//                        return true;
//                    });
                });
    }

    private void findViewById() {
        appBarLayout = findViewById(R.id.appBarLayout);
        resetButton = findViewById(R.id.resetButton);
        tourDropDownView = findViewById(R.id.tourDropDownView);
        regionDropDownView = findViewById(R.id.regionDropDownView);
        regionTextInputLayout = findViewById(R.id.regionTextInputLayout);
        tourTextInputLayout = findViewById(R.id.tourTextInputLayout);
        categoryDropDownView = findViewById(R.id.categoryDropDownView);
        visitStateDropDownView = findViewById(R.id.visitStateDropDownView);
        proximityDropDownView = findViewById(R.id.proximityDropDownView);
        balanceEndEditText = findViewById(R.id.balanceEndEditText);
        balanceStartEditText = findViewById(R.id.balanceStartEditText);
        hasBalanceLimitSwitch = findViewById(R.id.hasBalanceLimitSwitch);
        plannedCustomersSwitch = findViewById(R.id.plannedCustomersSwitch);
        dateEndButton = findViewById(R.id.dateEndButton);
        dateStartButton = findViewById(R.id.dateStartButton);
        orderByRadioGroup = findViewById(R.id.orderByRadioGroup);
        reverseSwitch = findViewById(R.id.reverseSwitch);
        filterCloseButton = findViewById(R.id.filterCloseButton);
        searchBar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.searchView);
        sideSheetContainer = findViewById(R.id.sideSheetContainer);
        datesContainer = findViewById(R.id.datesContainer);
        visitStateContainer = findViewById(R.id.visitStateContainer);
        dateRadioButton = findViewById(R.id.dateRadioButton);
        sideSheetBehavior = SideSheetBehavior.from(sideSheetContainer);
    }

    private void setControls(){
        searchBar.setNavigationOnClickListener(view -> toggleSearchView(true));
        searchBar.setOnClickListener(this);
        searchBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if(id == R.id.menuScan){
                //todo implement scan when adding the possibility to print the Qr Code for customers
//                Intent intent = ScanActivity.getProductScanIntent(CustomersListActivity.this);
//                scanResultLauncher.launch(intent);
            }else if(id == R.id.menuFilter){
                sideSheetBehavior.expand();
                sideSheetBehavior.setDraggable(true);
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
                    toggleSearchView(false);
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

    @Override
    public void setFiltersControls() {
        dateEndButton.setOnClickListener(this);
        dateStartButton.setOnClickListener(this);
        filterCloseButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);
        orderByRadioGroup.setOnCheckedChangeListener(this);
        reverseSwitch.setOnCheckedChangeListener(this);
        hasBalanceLimitSwitch.setOnCheckedChangeListener(this);
        plannedCustomersSwitch.setOnCheckedChangeListener(this);
        proximityDropDownView.setSelectionListener(this);
        visitStateDropDownView.setSelectionListener(this);
        categoryDropDownView.setSelectionListener(this);
        regionDropDownView.setSelectionListener(this);
        tourDropDownView.setSelectionListener(this);
        balanceStartEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filtersPresenter.onBalanceStartChanged(balanceStartEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        balanceEndEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filtersPresenter.onBalanceEndChanged(balanceEndEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    private void toggleSearchView(boolean toggled){
        if(toggled){
            searchView.show();
        }else{
            searchView.hide();
        }
    }

    private void initView(){
        presenter.onViewCreated();
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
        menu.findItem(R.id.menuScan).setVisible(false);    }

    @Override
    public void loading() {

    }

    @Override
    public boolean printLocation(Double latitude, Double longitude) {
        map.setMyLocationEnabled(true);

//        CircleOptions circleOptions = new CircleOptions();
//        circleOptions.center(new LatLng(latitude, longitude));
//        circleOptions.fillColor(getColor(android.R.color.holo_blue_dark));
//        circleOptions.strokeColor(getColor(android.R.color.holo_blue_dark));
//        circleOptions.radius(10);
//        map.addCircle(circleOptions);
        return true;
    }

    @Override
    public boolean checkLocation(boolean failedInformed) {
        return false;
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        final LatLng position = marker.getPosition();
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle(getString(R.string.save_customer_gps_title));
        dialogBuilder.setMessage(getString(R.string.save_customer_gps_msg));
        dialogBuilder.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> presenter.onRefresh());
        dialogBuilder.setPositiveButton(getString(R.string.save_label), (dialogInterface, i) ->
                presenter.onMarkerDragged(Integer.valueOf(String.valueOf(marker.getTag())), position.latitude, position.longitude));
        dialogBuilder.create().show();
    }

    @Override
    public void onCustomerUpdated() {
        setResult(RESULT_OK);
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }

    @Override
    public void initRegionsFilter(LinkedHashMap<String, String> hashMap, boolean enabled, String regionName) {
        regionDropDownView.setDefaultValue("");
        regionDropDownView.setKeyValueMap(hashMap, true);
        if(regionName != null){
            regionDropDownView.setText(regionName);
        }else {
            regionDropDownView.setText("");
        }
        regionTextInputLayout.setEnabled(enabled);
    }

    @Override
    public void initCategoriesFilter(LinkedHashMap<String, String> hashMap) {
        categoryDropDownView.setDefaultValue("");
        categoryDropDownView.setKeyValueMap(hashMap, true);
        categoryDropDownView.setText("");
    }

    @Override
    public void initProximityFilter(LinkedHashMap<String, String> hashMap) {
        proximityDropDownView.setDefaultValue("");
        proximityDropDownView.setKeyValueMap(hashMap, true);
        proximityDropDownView.setText("");
    }

    @Override
    public void initVisitStateFilter(LinkedHashMap<String, String> hashMap) {
        visitStateDropDownView.setDefaultValue("");
        visitStateDropDownView.setKeyValueMap(hashMap, true);
        visitStateDropDownView.setText("");
    }

    @Override
    public void initToursFilter(LinkedHashMap<String, String> hashMap, boolean enabled, String selectedTour) {
        tourDropDownView.setDefaultValue("");
        tourDropDownView.setKeyValueMap(hashMap, true);
        if(selectedTour != null){
            tourDropDownView.setText(selectedTour);
        }else {
            tourDropDownView.setText("");
        }
        tourTextInputLayout.setEnabled(enabled);
    }

    @Override
    public void setFilters(Filters filters) {
        presenter.setFilters(filters);
        presenter.onRefresh();
    }

    @Override
    public void orderByName() {
        orderByRadioGroup.check(R.id.nameRadioButton);
    }

    @Override
    public void orderByProximity() {
        orderByRadioGroup.check(R.id.proximityRadioButton);
    }

    @Override
    public void orderByVisitDate() {
        orderByRadioGroup.check(R.id.dateRadioButton);
    }

    @Override
    public void setHasBalanceChecked(boolean checked) {
        hasBalanceLimitSwitch.setChecked(checked);
    }

    @Override
    public void setPlannedCustomersChecked(boolean checked) {
        plannedCustomersSwitch.setChecked(checked);
    }

    @Override
    public void setReverseChecked(Boolean checked) {
        reverseSwitch.setChecked(checked);
    }

    @Override
    public void setDateEndFilter(String text) {
        dateEndButton.setText(text);
    }

    @Override
    public void setDateStartFilter(String text) {
        dateStartButton.setText(text);
    }

    @Override
    public void setBalanceStart(String text) {
        balanceStartEditText.setText(text);
    }

    @Override
    public void setBalanceEnd(String text) {
        balanceEndEditText.setText(text);
    }

    @Override
    public void setProximityFilter(String text) {
        proximityDropDownView.setText(text);
    }

    @Override
    public void setVisitStateFilter(String text) {
        visitStateDropDownView.setText(text);
    }

    @Override
    public void setCategoryFilter(String text) {
        categoryDropDownView.setText(text);
    }

    @Override
    public void setRegionFilter(String text) {
        regionDropDownView.setText(text);
    }

    @Override
    public void setTourFilter(String text) {
        tourDropDownView.setText(text);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {
        if(id == R.id.nameRadioButton){
            filtersPresenter.orderByName();
        }else if(id == R.id.dateRadioButton){
            filtersPresenter.orderByVisitDate();
        }else if(id == R.id.proximityRadioButton){
            filtersPresenter.orderByProximity();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int id = compoundButton.getId();
        if(id == R.id.reverseSwitch){
            filtersPresenter.reverseSortBy(b);
        }else if(id == R.id.hasBalanceLimitSwitch){
            filtersPresenter.showHasBalanceLimit(b);
        }else if(id == R.id.plannedCustomersSwitch){
            filtersPresenter.showPlannedCustomers(b);
        }
    }

    @Override
    public void onSelect(View view, String key, String value, boolean perTyping) {
        int id = view.getId();
        if(key.equals(KeyValueAutoComplete.DEFAULT_KEY)){
            key = null;
            value = "";
        }
        if(id == R.id.proximityDropDownView){
            filtersPresenter.onProximitySelected(key, value);
        }else if(id == R.id.visitStateDropDownView){
            filtersPresenter.onVisitStateSelected(key, value);
        }else if(id == R.id.categoryDropDownView){
            filtersPresenter.onCategorySelected(key, value);
        }else if(id == R.id.regionDropDownView){
            filtersPresenter.onRegionSelected(key, value);
        }else if(id == R.id.tourDropDownView){
            filtersPresenter.onTourSelected(key, value);
        }
    }

    @Override
    public void requestCurrentLocation(IFiltersPresenter.LocationListener locationListener) {
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
    public void requestSelectDateRange(Pair<Long, Long> dateRange) {
        final MaterialDatePicker<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker()
                .setSelection(dateRange)
                .build();
        builder.addOnPositiveButtonClickListener(selection -> {
            filtersPresenter.onSelectDateRange(selection);
        });
        builder.show(getSupportFragmentManager(), TAG);
    }

    @Override
    public void enableVisitStateFilter(boolean enabled) {
        visitStateContainer.setVisibility(getViewVisibility(enabled));
    }

    @Override
    public void enableVisitDatesFilter(boolean enabled) {
        datesContainer.setVisibility(getViewVisibility(enabled));
    }

    @Override
    public void enableVisitDateOrder(boolean enabled) {
        dateRadioButton.setVisibility(getViewVisibility(enabled));
    }

    private int getViewVisibility(boolean isVisible){
        return isVisible? View.VISIBLE : View.GONE;
    }
    @Override
    public void onClick(View view) {
        int id = view.getId();if(id == R.id.filterCloseButton){
            SideSheetBehavior.from(sideSheetContainer).hide();
        }else if(id == R.id.resetButton) {
            filtersPresenter.onResetClicked();
        }else if(id == R.id.dateStartButton){
            filtersPresenter.requestSelectStartDate();
        }else if(id == R.id.dateEndButton){
            filtersPresenter.requestSelectEndDate();
        }else if(id == R.id.toolbar){
            toggleSearchView(true);
        }
    }

    @Override
    public void addMarker(String name, double latitude, double longitude, boolean draggable, int position) {
        final Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).draggable(draggable).title(name));
        marker.setTag(position);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 14));
        map.animateCamera(CameraUpdateFactory.zoomTo(14), 800, null);
    }

    @Override
    public void clearMarkers() {
        map.clear();
    }

    @Override
    public void getCurrentLocation() {
        currentLocationUtil.setLocationListener(this);
        currentLocationUtil.execute();
    }

    @Override
    public void openCustomerDetails(String customerId, String tourId) {
        Intent intent = CustomerDetailsActivity.getIntent(this, customerId, tourId);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void goBack() {
        finish();
    }

    @Override
    public void onSelectCustomer(String customerId) {
        Intent intent = new Intent();
        Bundle data = new Bundle();
        data.putString(Col.SERVER_ID, customerId);
        intent.putExtras(data);
        setResult(RESULT_OK, intent);
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        presenter.onCustomerClick(Integer.valueOf(String.valueOf(marker.getTag())));
    }

    public static Intent getIntent(Context context, String tourId, String customerId, boolean selectCustomerMode){
        Intent intent = new Intent(context, CustomersMapActivity.class);
        Bundle data = new Bundle();
        data.putString(TOUR_ID_KEY, tourId);
        data.putString(SELECTED_CUSTOMER_ID_KEY, customerId);
        data.putBoolean(SELECT_CUSTOMER_MODE_KEY, selectCustomerMode);
        intent.putExtras(data);
        return intent;
    }

    public static Intent getFileredIntent(Context context, Filters filters, boolean selectCustomerMode, boolean editable){
        Intent intent = new Intent(context, CustomersMapActivity.class);
        Bundle data = new Bundle();
        data.putSerializable(FILTERS_KEY, filters);
        data.putBoolean(EDITABLE_KEY, editable);
        data.putBoolean(SELECT_CUSTOMER_MODE_KEY, selectCustomerMode);
        intent.putExtras(data);
        return intent;
    }
}
