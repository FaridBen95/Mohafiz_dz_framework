package com.MohafizDZ.project.catalog_dir;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
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
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.cart_order_dir.CartOrderActivity;
import com.MohafizDZ.project.catalog_dir.catalog_presenters_dir.CatalogPresenterImpl;
import com.MohafizDZ.project.catalog_dir.catalog_presenters_dir.ICatalogPresenter;
import com.MohafizDZ.project.catalog_dir.filters_presenter_dir.FiltersPresenterImpl;
import com.MohafizDZ.project.catalog_dir.filters_presenter_dir.IFiltersPresenter;
import com.MohafizDZ.project.catalog_dir.models.Filters;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.catalog_dir.quantity_dialog_dir.IQtyDialogPresenter;
import com.MohafizDZ.project.catalog_dir.strategies_dir.ConcreteCatalogStrategy;
import com.MohafizDZ.project.dashboard_dir.DashboardActivity;
import com.MohafizDZ.project.expenses_list_dir.ExpensesListActivity;
import com.MohafizDZ.project.home_dir.HomeActivity;
import com.MohafizDZ.project.product_form_dir.ProductFormActivity;
import com.MohafizDZ.project.scan_dir.ScanActivity;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.sidesheet.SideSheetBehavior;
import com.google.android.material.sidesheet.SideSheetCallback;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class CatalogActivity extends MyAppCompatActivity implements ICatalogPresenter.View, View.OnClickListener,
        CatalogProductAdapter.OnItemClickListener, IFiltersPresenter.View, CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = CatalogActivity.class.getSimpleName();
    private static final String STRATEGY_NAME_KEY = "strategy_name_key";
    private static final String CUSTOMER_ID_KEY = "customer_id_key";
    private static final String BOTTOM_NAV_VISIBLE_KEY = "bottom_nav_visible_key";

    private ICatalogPresenter.Presenter catalogPresenter;
    private IFiltersPresenter.Presenter filtersPresenter;
    private String strategyClassName;
    private RecyclerView recyclerView;
    private FloatingActionButton addFloatingActionButton, validateFloatingActionButton;
    private View appBarLayout, validateContainer, validateDetailsContainer, sideSheetContainer;
    private TextView totalPriceTextView, customerTextView;
    private BottomNavigationView bottomNavigation;
    private ActivityResultLauncher<Intent> productFormResultLauncher, cartOrderResultLauncher, scanResultLauncher;
    private CatalogProductAdapter catalogProductAdapter;
    private String customerId;
    private boolean isBottomNavVisible;
    private SideSheetBehavior<View> sideSheetBehavior;
    private MaterialButton closeButton, resetButton;
    private MaterialSwitch availableSwitch, allCategoriesSwitch, reverseSwitch;
    private RadioGroup orderByRadioGroup;
    private ChipGroup categoriesChipGroup;
    private BadgeDrawable badgeDrawable;
    private Menu menu;
    private Integer chipId;
    private SearchBar searchBar;
    private SearchView searchView;

    @Override
    public Toolbar setToolBar() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_layout);
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
            isBottomNavVisible = data.getBoolean(BOTTOM_NAV_VISIBLE_KEY, true);
            return true;
        }else{
            finish();
            return false;
        }
    }

    private void init() {
        ConcreteCatalogStrategy strategy = getStrategy();
        DataRow currentUserRow = app().getCurrentUser();
        catalogPresenter = new CatalogPresenterImpl(this, this, currentUserRow, strategy);
        filtersPresenter = new FiltersPresenterImpl(this, this, currentUserRow);
        strategy.setPresenter(catalogPresenter);
        strategy.setCustomerId(customerId);
        initResultLaunchers();
    }

    private ConcreteCatalogStrategy getStrategy() {
        try {
            Class<?> strategyClass = Class.forName(strategyClassName);
            Constructor<?> constructor = strategyClass.getConstructor(Context.class, ICatalogPresenter.View.class, DataRow.class);
            return (ConcreteCatalogStrategy) constructor.newInstance(this, this, app().getCurrentUser());
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initResultLaunchers(){
        productFormResultLauncher = registerForActivityResult( new ActivityResultContracts.StartActivityForResult(), o -> {
            catalogPresenter.onRefresh();
        });

        cartOrderResultLauncher = registerForActivityResult( new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == Activity.RESULT_OK){
                setResult(RESULT_OK);
                finish();
            }
        });

        scanResultLauncher = registerForActivityResult( new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == Activity.RESULT_OK){
                Bundle data = result.getData().getExtras();
                catalogPresenter.onProductScan(data.getString("code", null));
            }
        });
    }

    private void findViewById() {
        appBarLayout = findViewById(R.id.appBarLayout);
        addFloatingActionButton = findViewById(R.id.addFloatingActionButton);
        validateFloatingActionButton = findViewById(R.id.validateFloatingActionButton);
        recyclerView = findViewById(R.id.recyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        validateContainer = findViewById(R.id.validateContainer);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        customerTextView = findViewById(R.id.customerTextView);
        validateDetailsContainer = findViewById(R.id.validateDetailsContainer);
        sideSheetContainer = findViewById(R.id.sideSheetContainer);
        sideSheetBehavior = SideSheetBehavior.from(sideSheetContainer);
        searchBar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.searchView);
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
        categoriesChipGroup = findViewById(R.id.categoriesChipGroup);
        orderByRadioGroup = findViewById(R.id.orderByRadioGroup);
        allCategoriesSwitch = findViewById(R.id.allCategoriesSwitch);
        availableSwitch = findViewById(R.id.availableSwitch);
        reverseSwitch = findViewById(R.id.reverseSwitch);
        resetButton = findViewById(R.id.resetButton);
        closeButton = findViewById(R.id.filterCloseButton);
    }

    private void setControls(){
        addFloatingActionButton.setOnClickListener(this);
        validateFloatingActionButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        reverseSwitch.setOnCheckedChangeListener(this);
        availableSwitch.setOnCheckedChangeListener(this);
        allCategoriesSwitch.setOnCheckedChangeListener(this);
        orderByRadioGroup.setOnCheckedChangeListener(this);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.homeMenuItem){
                startHomeActivity();
            }else if(id == R.id.expensesMenuItem){
                openExpensesList();
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
        searchBar.setNavigationOnClickListener(view -> searchView.show());
        searchBar.setOnClickListener(this);
        searchView.addTransitionListener(
                (searchView, previousState, newState) -> {
                    if (newState == SearchView.TransitionState.SHOWN) {
                        appBarLayout.setVisibility(View.INVISIBLE);
                        // Handle search view opened.
                    }else{
                        appBarLayout.setVisibility(View.VISIBLE);
                    }
                });
        searchBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if(id == R.id.menuScan){
                Intent intent = ScanActivity.getProductScanIntent(CatalogActivity.this);
                scanResultLauncher.launch(intent);
            }else if(id == R.id.menuEmpty){
                catalogPresenter.onEmptyClicked();
            }else if(id == R.id.menuFilter){
                SideSheetBehavior.from(sideSheetContainer).expand();
                SideSheetBehavior.from(sideSheetContainer).setDraggable(true);
            }
            return true;
        });
        final EditText editText = searchView
                .getEditText();
        editText.setOnEditorActionListener(
                        (v, actionId, event) -> {
                            catalogPresenter.onSearch(searchView.getText().toString());
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
                catalogPresenter.onSearch(editText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

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

    private void openExpensesList(){
        Intent intent = ExpensesListActivity.getIntent(this);
        IntentUtils.startActivity(this, intent);
        finish();
    }

    private void startHomeActivity(){
        IntentUtils.startActivity(this, HomeActivity.class, null);
        finish();
    }

    private void initView(){
        bottomNavigation.setSelectedItemId(R.id.catalogMenuItem);
        bottomNavigation.setVisibility(getViewVisibility(isBottomNavVisible));
        catalogPresenter.onViewCreated();
        sideSheetContainer.setVisibility(View.VISIBLE);
        filtersPresenter.onViewCreated();
        searchBar.inflateMenu(R.menu.catalog_menu);
        this.menu = searchBar.getMenu();
        catalogPresenter.onCreateOptionsMenu();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.addFloatingActionButton){
            requestOpenProductDetails(null, true);
        }else if(id == R.id.validateFloatingActionButton){
            catalogPresenter.onValidate();
        }else if(id == R.id.filterCloseButton){
            sideSheetBehavior.hide();
        }else if(id == R.id.resetButton){
            filtersPresenter.onViewCreated();
        }else if(chipId != null && id == chipId){
            int position = (int) view.getTag();
            filtersPresenter.onCategoryClicked(position);
        }else if(id == R.id.toolbar){
            searchView.show();
        }
    }

    @Override
    public void requestOpenProductDetails(String productId, boolean editable) {
        Intent intent = ProductFormActivity.getIntent(this, productId, editable);
        productFormResultLauncher.launch(intent);
    }

    @Override
    public void toggleAddProduct(boolean visible) {
        addFloatingActionButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleValidateContainer(boolean visible) {
        validateFloatingActionButton.setVisibility(getViewVisibility(visible));
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
    public void setTotalAmount(String text) {
        totalPriceTextView.setText(text);
    }

    @Override
    public void setCustomerName(String text) {
        customerTextView.setText(text);
    }

    @Override
    public void toggleEmptyMenuItem(boolean visible) {
        try {
            menu.findItem(R.id.menuEmpty).setVisible(visible);
        }catch (Exception ignored){}
    }

    @Override
    public void onListUpdated() {
        catalogProductAdapter.notifyItemRangeRemoved(0, catalogProductAdapter.getItemCount());
    }

    @Override
    public void openCartOrder(String strategyClassName) {
        Intent intent = CartOrderActivity.getIntent(this, strategyClassName, customerId);
        cartOrderResultLauncher.launch(intent);
    }

    private int getViewVisibility(boolean isVisible){
        return isVisible? View.VISIBLE : View.GONE;
    }

    @Override
    public void setToolbarTitle(String title) {
        searchBar.setTitle(title);
    }

    @Override
    public void goBack(int resultCode) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void toggleValidateDetailsContainer(boolean visible) {
        validateDetailsContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void showQtyDialog(IQtyDialogPresenter.Dialog qtyDialog) {
        qtyDialog.showDialog(this);
    }

    @Override
    public void onLineDeleted(int position) {
        catalogProductAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onLineUpdated(int position) {
        catalogProductAdapter.notifyItemChanged(position);
    }

    @Override
    public void setSearchFilter(String name) {
        searchView.setText(name);
        catalogPresenter.onSearch(searchView.getText().toString());
        searchBar.setText(searchView.getText());
        searchView.hide();
    }

    @Override
    public void initAdapter(List<ProductRow> rows, boolean showAvailability) {
        catalogProductAdapter = new CatalogProductAdapter(this, rows, this);
        catalogProductAdapter.setShowAvailability(showAvailability);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(catalogProductAdapter);
    }

    @Override
    public void onLoadFinished(List<ProductRow> rows) {
        catalogProductAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshItem(int position) {
        catalogProductAdapter.notifyItemChanged(position);
    }

    @Override
    public void onItemClick(int position) {
        catalogPresenter.onItemClick(position);
    }

    @Override
    public void onItemLongClick(int position) {
        catalogPresenter.onItemLongClick(position);
    }



    public static Intent getIntent(Context context, String strategyClassName, String customerId, boolean bottomNavVisible) {
        Intent intent = new Intent(context, CatalogActivity.class);
        Bundle data = new Bundle();
        data.putString(STRATEGY_NAME_KEY, strategyClassName);
        data.putString(CUSTOMER_ID_KEY, customerId);
        data.putBoolean(BOTTOM_NAV_VISIBLE_KEY, bottomNavVisible);
        intent.putExtras(data);
        return intent;
    }


    @Override
    public void setReverseChecked(boolean checked) {
        reverseSwitch.setChecked(checked);
    }

    @Override
    public void filterByAvailability(boolean checked) {
        availableSwitch.setChecked(checked);
    }

    @Override
    public void filterByAllCategories(boolean checked) {
        allCategoriesSwitch.setChecked(checked);
    }

    @Override
    public void orderByAvailability(boolean checked) {
        orderByRadioGroup.check(R.id.availabilityRadioButton);
    }

    @Override
    public void orderByCategory(boolean checked) {
        orderByRadioGroup.check(R.id.categoryRadioButton);
    }

    @Override
    public void orderByName(boolean checked) {
        orderByRadioGroup.check(R.id.nameRadioButton);
    }

    @Override
    public void orderByPrice(boolean checked) {
        orderByRadioGroup.check(R.id.priceRadioButton);
    }

    @Override
    public void clearCategoriesFilter() {
        categoriesChipGroup.removeAllViews();
    }

    @Override
    public void createCategoryChip(String name, int position) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.rightMargin = MyUtil.dpToPx(4);
        params.bottomMargin = MyUtil.dpToPx(8);
        Chip chip = new Chip(this);
        chipId = chipId == null? View.generateViewId() : chipId;
        chip.setId(chipId);
        chip.setLayoutParams(params);
        chip.setText(name);
        ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(this,
                null, 0, R.style.Widget_Material3_Chip_Filter_Close_Icon_Tint);
        chip.setChipDrawable(chipDrawable);
        chip.setTag(position);
        chip.setOnClickListener(this);
        chip.setCheckable(true);
        categoriesChipGroup.addView(chip);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int id = compoundButton.getId();
        if(id == R.id.reverseSwitch){
            filtersPresenter.reverseSortBy(b);
        }else if(id == R.id.availableSwitch){
            filtersPresenter.filterAvailability(b);
        }else if(id == R.id.allCategoriesSwitch){
            allCategoriesSwitch.setEnabled(!b);
            filtersPresenter.filterByAllCategories(b);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {
        if(id == R.id.nameRadioButton){
            filtersPresenter.orderByName();
        }else if(id == R.id.categoryRadioButton){
            filtersPresenter.orderByCategory();
        }else if(id == R.id.priceRadioButton){
            filtersPresenter.orderByPrice();
        }else if(id == R.id.availabilityRadioButton){
            filtersPresenter.orderByAvailability();
        }
    }

    @Override
    public void setFilters(Filters filters) {
        catalogPresenter.setFilters(filters);
        catalogPresenter.onRefresh();
    }
}
