package com.MohafizDZ.project.customer_details_dir;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.MohafizDZ.framework_repository.Utils.BitmapUtils;
import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.customer_details_dir.form_dir.CustomerFormActivity;
import com.MohafizDZ.project.customers_dir.Filters;
import com.MohafizDZ.project.customers_dir.customers_map_dir.CustomersMapActivity;
import com.MohafizDZ.project.visit_action_dir.VisitActionActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CustomerDetailsActivity extends MyAppCompatActivity implements ICustomerDetailsPresenter.View,
        ICustomerVisitPresenter.View, View.OnClickListener {
    private static final String TAg = CustomerDetailsActivity.class.getSimpleName();
    private static final String TOUR_ID_KEY = "tour_id_key";
    private ICustomerDetailsPresenter.Presenter presenter;
    private ICustomerVisitPresenter.Presenter actionsPresenter;
    private String customerId, tourId;
    private ImageView imageView;
    private TextView noteTextView, addressTextView, gpsPositionTextView, regionTextView,
            categoryTextview, codeTextView, phoneTextView, balanceTextView, nameTextView,
            visitNetAmountTextView, visitDurationTextView, visitPaymentTextView, balanceLimitTextView;
    private View visitsContainer;
    private ChipGroup actionsChipGroup;
    private FloatingActionButton mapFloatingActionButton;
    private Menu menu;
    private ActivityResultLauncher<Intent> customerEditResultLauncher, refreshResultLauncher;
    private Integer chipId;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_details_layout);
        if (initArgs()) {
            init();
            findViewById();
            setControls();
            initView();
        } else {
            finish();
        }
    }

    private boolean initArgs(){
        Bundle data = getIntent().getExtras();
        if(data != null){
            customerId = data.getString(Col.SERVER_ID);
            tourId = data.getString(TOUR_ID_KEY);
            return customerId != null;
        }
        return false;
    }

    private void init(){
        presenter = new CustomerDetailsPresenterImpl(this, this, app().getCurrentUser(), customerId);
        actionsPresenter = new CustomerVisitPresenterImpl(this, this, app().getCurrentUser(), customerId);
        actionsPresenter.setTourId(tourId);
        initResultLauncher();
    }

    private void initResultLauncher(){
        customerEditResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
            if(o.getResultCode() == RESULT_OK){
                finish();
            }else {
                presenter.onRefresh();
                actionsPresenter.onRefresh();
            }
        });
        refreshResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
            presenter.onRefresh();
            actionsPresenter.onRefresh();
        });
    }

    private void findViewById() {
        noteTextView = findViewById(R.id.noteTextView);
        addressTextView = findViewById(R.id.addressTextView);
        gpsPositionTextView = findViewById(R.id.gpsPositionTextView);
        regionTextView = findViewById(R.id.regionTextView);
        categoryTextview = findViewById(R.id.categoryTextview);
        codeTextView = findViewById(R.id.codeTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        balanceTextView = findViewById(R.id.balanceTextView);
        balanceLimitTextView = findViewById(R.id.balanceLimitTextView);
        nameTextView = findViewById(R.id.nameTextView);
        visitPaymentTextView = findViewById(R.id.visitPaymentTextView);
        visitNetAmountTextView = findViewById(R.id.visitNetAmountTextView);
        visitDurationTextView = findViewById(R.id.visitDurationTextView);
        visitsContainer = findViewById(R.id.visitsContainer);
        imageView = findViewById(R.id.imageView);
        actionsChipGroup = findViewById(R.id.actionsChipGroup);
        mapFloatingActionButton = findViewById(R.id.mapFloatingActionButton);
    }

    private void setControls(){
        mapFloatingActionButton.setOnClickListener(this);
    }

    private void initView(){
        presenter.onViewCreated();
        actionsPresenter.onViewCreated();
    }

    @Override
    public void setName(String txt) {
        nameTextView.setText(txt);
    }

    @Override
    public void setCode(String txt) {
        codeTextView.setText(txt);
    }

    @Override
    public void setRegion(String txt) {
        regionTextView.setText(txt);
    }

    @Override
    public void setGpsPosition(String txt) {
        gpsPositionTextView.setText(txt);
    }

    @Override
    public void setPhoneNum(String txt) {
        phoneTextView.setText(txt);
    }

    @Override
    public void setAddress(String txt) {
        addressTextView.setText(txt);
    }

    @Override
    public void setNote(String txt) {
        noteTextView.setText(txt);
    }

    @Override
    public void setCategory(String txt) {
        categoryTextview.setText(txt);
    }

    @Override
    public void setBalance(String txt) {
        balanceTextView.setText(txt);
    }

    @Override
    public void setBalanceLimit(String txt) {
        balanceLimitTextView.setText(txt);
    }

    @Override
    public void setVisitDuration(String txt) {
        visitDurationTextView.setText(txt);
    }

    @Override
    public void setVisitNetAmount(String txt) {
        visitNetAmountTextView.setText(txt);
    }

    @Override
    public void setPaymentsAmount(String txt) {
        visitPaymentTextView.setText(txt);
    }

    @Override
    public void setImage(String base64, String name) {
        Bitmap img = null;
        try {
            img = BitmapUtils.getBitmapImage(this, base64);
        }catch (Exception ignored){}
        try{
            img = img == null? BitmapUtils.getAlphabetImage(this, name): img;
        }catch (Exception ignored){}
        imageView.setImageBitmap(img);
    }

    @Override
    public void toggleEditItem(boolean visible) {
        menu.findItem(R.id.menuEdit).setVisible(visible);
    }

    @Override
    public void toggleVisitContainer(boolean visible) {
        visitsContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void openMap(Filters filters) {
        Intent intent = CustomersMapActivity.getFileredIntent(this, filters, false, true);
        refreshResultLauncher.launch(intent);
    }

    private int getViewVisibility(boolean visible){
        return visible? View.VISIBLE : View.GONE;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.save_menu, menu);
        this.menu = menu;
        presenter.onCreateOptionsMenu();
        menu.findItem(R.id.menuSave).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
        }else if(id == R.id.menuEdit){
            requestEdit();
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestEdit(){
        Intent intent = CustomerFormActivity.getIntent(this, customerId, true);
        customerEditResultLauncher.launch(intent);
    }

    @Override
    public void clearChipGroup() {
        actionsChipGroup.removeAllViews();
    }

    @Override
    public void createActionChip(String action, int position) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.rightMargin = MyUtil.dpToPx(4);
        params.bottomMargin = MyUtil.dpToPx(8);
        Chip chip = new Chip(this);
        chipId = chipId == null? View.generateViewId() : chipId;
        chip.setId(chipId);
        chip.setLayoutParams(params);
        chip.setText(action);
        ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(this,
                null, 0, R.style.Widget_Material3_Chip_Filter_Close_Icon_Tint);
        chip.setChipDrawable(chipDrawable);
        chip.setTag(position);
        chip.setOnClickListener(this);
        chip.setCheckable(false);
        actionsChipGroup.addView(chip);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.mapFloatingActionButton){
            presenter.requestOpenMap();
        } else if(chipId != null && id == chipId){
            int position = (int) view.getTag();
            actionsPresenter.onChipClicked(position);
        }
    }

    @Override
    public void openActionView(String actionId) {
        Intent intent = VisitActionActivity.getIntent(this, actionId);
        refreshResultLauncher.launch(intent);
    }

    public static Intent getIntent(Context context, String customerId){
        return getIntent(context, customerId, null);
    }
    public static Intent getIntent(Context context, String customerId, String tourId){
        Intent intent = new Intent(context, CustomerDetailsActivity.class);
        Bundle data = new Bundle();
        data.putString(Col.SERVER_ID, customerId);
        data.putString(TOUR_ID_KEY, tourId);
        intent.putExtras(data);
        return intent;
    }

}
