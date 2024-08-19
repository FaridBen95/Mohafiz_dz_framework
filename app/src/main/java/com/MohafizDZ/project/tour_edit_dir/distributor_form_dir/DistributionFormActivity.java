package com.MohafizDZ.project.tour_edit_dir.distributor_form_dir;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.tour_edit_dir.TourFormActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class DistributionFormActivity extends MyAppCompatActivity implements IDistributorPresenter.View, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = DistributionFormActivity.class.getSimpleName();

    private IDistributorPresenter.Presenter presenter;
    private TextInputEditText nameTextInput, vehicleTextInput, joinDateTextInput, expensesLimitTextInput;
    private ChipGroup chipGroup;
    private MaterialButton validateMaterialButton;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.distributor_layout);
        init();
        findViewById();
        setControls();
        initView();
    }

    private void init(){
        presenter = new DistributorFormPresenterImpl(this, this, app().getCurrentUser());
    }

    private void findViewById() {
        validateMaterialButton = findViewById(R.id.validateMaterialButton);
        chipGroup = findViewById(R.id.chipGroup);
        vehicleTextInput = findViewById(R.id.vehicleTextInput);
        joinDateTextInput = findViewById(R.id.joinDateTextInput);
        expensesLimitTextInput = findViewById(R.id.expensesLimitTextInput);
        nameTextInput = findViewById(R.id.nameTextInput);
    }

    private void setControls() {
        validateMaterialButton.setOnClickListener(this );
    }

    private void initView() {
        presenter.onViewCreated();
    }

    @Override
    public void setDefaultVehicleName(String txt) {
        vehicleTextInput.setText(txt);
    }

    @Override
    public void setSellerName(String txt) {
        nameTextInput.setText(txt);
    }

    @Override
    public void setJoinDate(String txt) {
        joinDateTextInput.setText(txt);
    }

    @Override
    public void setExpensesLimit(String expensesLimit) {
        expensesLimitTextInput.setText(expensesLimit);
    }

    @Override
    public void createChip(String key, String value) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.rightMargin = MyUtil.dpToPx(4);
        params.bottomMargin = MyUtil.dpToPx(8);
        Chip chip = new Chip(this);
        chip.setId(View.generateViewId());
        chip.setLayoutParams(params);
        chip.setText(value);
        ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(this,
                null, 0, R.style.Widget_Material3_Chip_Filter_Close_Icon_Tint);
        chip.setChipDrawable(chipDrawable);
        chip.setTag(key);
        chip.setOnCheckedChangeListener(this);
        chipGroup.addView(chip);
    }

    @Override
    public void clearChips() {
        chipGroup.removeAllViews();
    }

    @Override
    public void selectChip(String key) {
        ((Chip)chipGroup.findViewWithTag(key)).setChecked(true);
    }

    @Override
    public void goBack() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.validateMaterialButton){
            onValidate();
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

    private void onValidate(){
        String vehicleName = String.valueOf(vehicleTextInput.getText());
        String expensesLimit = expensesLimitTextInput.getText().toString();
        presenter.onValidate(vehicleName, getConfigurations(), expensesLimit);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent getIntent(Context context){
        return new Intent(context, DistributionFormActivity.class);
    }
}
