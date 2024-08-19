package com.MohafizDZ.project.tour_edit_dir;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.controls.KeyValueAutoComplete;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.regions_map_dir.RegionsMapActivity;
import com.MohafizDZ.project.scan_dir.ScanActivity;
import com.MohafizDZ.project.tour_edit_dir.basic_details_presenter_dir.ITourBasicDetailsPresenter;
import com.MohafizDZ.project.tour_edit_dir.basic_details_presenter_dir.TourBasicDetailsPresenterImpl;
import com.MohafizDZ.project.tour_edit_dir.configuration_presenter_dir.ITourConfigurationPresenter;
import com.MohafizDZ.project.tour_edit_dir.configuration_presenter_dir.TourConfigurationPresenterImpl;
import com.MohafizDZ.project.tour_edit_dir.distributor_form_dir.DistributionFormActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class TourFormActivity extends MyAppCompatActivity implements ITourBasicDetailsPresenter.View, View.OnClickListener,
        KeyValueAutoComplete.SelectionListener, ITourPlanPresenter.View, ITourConfigurationPresenter.View, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = TourFormActivity.class.getSimpleName();
    private static final String IS_EDITABLE_KEY = "is_editable";

    private View basicDetailsContainer, configurationContainer;
    private TextInputEditText vehicleTextInput, tourNameTextView, nameTextView, goalTextInput, visitsGoalTextInput, expensesLimitTextInput;
    private TextInputLayout distributorInputLayout;
    private KeyValueAutoComplete regionDropDownView;
    private ChipGroup chipGroup;
    private MaterialButton validateMaterialButton;
    private ITourPlanPresenter.Presenter tourPlanPresenter;
    private ITourBasicDetailsPresenter.Presenter basicDetailsPresenter;
    private ITourConfigurationPresenter.Presenter configurationPresenter;
    private ActivityResultLauncher<Intent> regionResultLauncher, editDistributorScanResultLauncher, editDistributorResultLauncher;
    private String tourId;
    private boolean isEditable;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tour_form_layout);
        initArgs();
        init();
        findViewById();
        setControls();
        initView();
    }

    private void initArgs(){
        Bundle data = getIntent().getExtras();
        tourId = data != null? data.getString(Col.SERVER_ID) : null;
        isEditable = data == null || data.getBoolean(IS_EDITABLE_KEY, true);
    }

    private void init(){
        DataRow currentUserRow = app().getCurrentUser();
        tourPlanPresenter = new TourPlanPresenterImpl(this, this, currentUserRow);
        basicDetailsPresenter = new TourBasicDetailsPresenterImpl(this, this, currentUserRow);
        configurationPresenter = new TourConfigurationPresenterImpl(this, this, currentUserRow);
        initResultLaunchers();
    }

    private void initResultLaunchers(){
        regionResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            try{
                regionDropDownView.setText("");
                basicDetailsPresenter.onRefresh();
            }catch (Exception ignored){}
        });
        editDistributorScanResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK){
                openDistributorForm();
            }
        });
        editDistributorResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK){
                tourPlanPresenter.onDistributorChanged();
            }
        });
    }

    @Override
    public void openDistributorForm(){
        Intent intent = DistributionFormActivity.getIntent(this);
        editDistributorResultLauncher.launch(intent);
    }

    private void findViewById(){
        basicDetailsContainer = findViewById(R.id.basicDetailsContainer);
        configurationContainer = findViewById(R.id.configurationContainer);
        regionDropDownView = findViewById(R.id.regionDropDownView);
        vehicleTextInput = findViewById(R.id.vehicleTextInput);
        tourNameTextView = findViewById(R.id.tourNameTextView);
        nameTextView = findViewById(R.id.nameTextInput);
        expensesLimitTextInput = findViewById(R.id.expensesLimitTextInput);
        goalTextInput = findViewById(R.id.goalTextInput);
        visitsGoalTextInput = findViewById(R.id.visitsGoalTextInput);
        chipGroup = findViewById(R.id.chipGroup);
        validateMaterialButton = findViewById(R.id.validateMaterialButton);
        distributorInputLayout = findViewById(R.id.distributorInputLayout);
    }

    private void setControls(){
        validateMaterialButton.setOnClickListener(this);
        regionDropDownView.setSelectionListener(this);
        distributorInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tourPlanPresenter.requestEditDistributor();
            }
        });
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                tourPlanPresenter.onBackPressed();
            }
        });
    }

    @Override
    public void requestScanAdminQRCode() {
        Intent intent = ScanActivity.getAdminIntent(this);
        editDistributorScanResultLauncher.launch(intent);
    }

    private void initView(){
        tourPlanPresenter.onViewCreated();
        basicDetailsPresenter.onViewCreated();
        configurationPresenter.onViewCreated();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        MyUtil.preventDoubleClick(view);
        if(id == R.id.validateMaterialButton){
            String vehicleName = vehicleTextInput.getText().toString();
            String regionId = regionDropDownView.getCurrentKey();
            String expenseLimit = expensesLimitTextInput.getText().toString();
            String goal = goalTextInput.getText().toString();
            String visitsGoal = visitsGoalTextInput.getText().toString();
            if(TextUtils.isEmpty(vehicleName)){
                vehicleTextInput.setError(getString(R.string.vehicle_required));
                return;
            }
            if(regionId.equals("") || regionId.equals(KeyValueAutoComplete.DEFAULT_KEY)){
                regionDropDownView.setError(getString(R.string.region_required));
                return;
            }
            tourPlanPresenter.onValidate(vehicleName, regionId, expenseLimit, visitsGoal, goal, getConfigurations());
        }
    }

    private List<String> getConfigurations(){
        List<String> ids = new ArrayList<>();
        for(int chipId : chipGroup.getCheckedChipIds()){
            Chip chip = chipGroup.findViewById(chipId);
            String tag = String.valueOf(chip.getTag());
            ids.add(tag);
        }
        return ids;
    }

    @Override
    public void setTourName(String text) {
        tourNameTextView.setText(text);
    }

    @Override
    public void setDistributorName(String text) {
        nameTextView.setText(text);
    }

    @Override
    public void setVehicleName(String text) {
        vehicleTextInput.setText(text);
    }

    @Override
    public void setExpensesLimit(String txt) {
        expensesLimitTextInput.setText(txt);
    }

    @Override
    public void initRegionsFilter(LinkedHashMap<String, String> regions) {
        regionDropDownView.setDefaultValue("");
        regionDropDownView.setKeyValueMap(regions, true);
    }

    @Override
    public void openRegionMap() {
        regionResultLauncher.launch(RegionsMapActivity.getIntent(this));
    }

    @Override
    public void onSelect(View view, String key, String value, boolean perTyping) {
        basicDetailsPresenter.onSelectRegion(key);
    }

    private int getViewVisibility(boolean visible){
        return visible? View.VISIBLE : View.GONE;
    }
    @Override
    public void toggleBasicDetailsContainer(boolean visible) {
        basicDetailsContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleConfigurationContainer(boolean visible) {
        configurationContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void goBack() {
        finish();
    }

    @Override
    public void createChip(String key, String text) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.rightMargin = MyUtil.dpToPx(4);
        params.bottomMargin = MyUtil.dpToPx(8);
        Chip chip = new Chip(this);
        chip.setId(View.generateViewId());
        chip.setLayoutParams(params);
        chip.setText(text);
        ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(this,
                null, 0, R.style.Widget_Material3_Chip_Filter_Close_Icon_Tint);
        chip.setChipDrawable(chipDrawable);
        chip.setTag(key);
        chip.setOnCheckedChangeListener(this);
        chipGroup.addView(chip);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            tourPlanPresenter.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, TourFormActivity.class);
    }

    public static Intent getIntent(Context context, String tourId){
        Intent intent = new Intent(context, TourFormActivity.class);
        Bundle data = new Bundle();
        data.putString(Col.SERVER_ID, tourId);
        data.putBoolean(IS_EDITABLE_KEY, false);
        intent.putExtras(data);
        return intent;
    }
}
