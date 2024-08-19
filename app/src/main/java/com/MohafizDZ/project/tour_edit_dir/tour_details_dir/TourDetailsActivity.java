package com.MohafizDZ.project.tour_edit_dir.tour_details_dir;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

public class TourDetailsActivity extends MyAppCompatActivity implements ITourDetailsPresenter.View {
    private static final String TAG = TourDetailsActivity.class.getSimpleName();

    private ITourDetailsPresenter.Presenter presenter;
    private View endDateContainer, closingDateContainer, preClosingDateContainer, startDateContainer, planDateContainer;
    private TextView endDateTextView, closingDateTextView, preClosingDateTextView,
            startDateTextView, planDateTextView, regionTextView, vehicleTextView, nameTextView, stateTextView;
    private ChipGroup chipGroup;
    private String tourId;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tour_details_layout);
        initArgs();
        init();
        findViewById();
        initView();
    }

    private void initArgs(){
        Bundle data = getIntent().getExtras();
        tourId = data !=null? data.getString(Col.SERVER_ID) : null;
    }

    private void init(){
        presenter = new TourDetailsPresenterImpl(this, this, app().getCurrentUser(), tourId);
    }

    private void findViewById() {
        endDateContainer = findViewById(R.id.endDateContainer);
        closingDateContainer = findViewById(R.id.closingDateContainer);
        preClosingDateContainer = findViewById(R.id.preClosingDateContainer);
        startDateContainer = findViewById(R.id.startDateContainer);
        planDateContainer = findViewById(R.id.planDateContainer);
        endDateTextView = findViewById(R.id.endDateTextView);
        closingDateTextView = findViewById(R.id.closingDateTextView);
        preClosingDateTextView = findViewById(R.id.preClosingDateTextView);
        startDateTextView = findViewById(R.id.startDateTextView);
        planDateTextView = findViewById(R.id.planDateTextView);
        regionTextView = findViewById(R.id.regionTextView);
        vehicleTextView = findViewById(R.id.vehicleTextView);
        nameTextView = findViewById(R.id.nameTextView);
        stateTextView = findViewById(R.id.stateTextView);
        chipGroup = findViewById(R.id.chipGroup);
    }

    private void initView(){
        presenter.onViewCreated();
    }

    @Override
    public void goBack() {
        finish();
    }

    @Override
    public void setTourName(String text) {
        nameTextView.setText(text);
    }

    @Override
    public void setEndDate(String text) {
        endDateTextView.setText(text);
    }

    @Override
    public void setState(String text) {
        stateTextView.setText(text);
    }

    @Override
    public void setPlanDate(String text) {
        planDateTextView.setText(text);
    }

    @Override
    public void setStartDate(String text) {
        startDateTextView.setText(text);
    }

    @Override
    public void setPreClosingDate(String text) {
        preClosingDateTextView.setText(text);
    }

    @Override
    public void setClosingDate(String text) {
        closingDateTextView.setText(text);
    }

    @Override
    public void setVehicle(String text) {
        vehicleTextView.setText(text);
    }

    @Override
    public void setRegion(String text) {
        regionTextView.setText(text);
    }

    private int getViewVisibility(boolean visible){
        return visible? View.VISIBLE : View.GONE;
    }

    @Override
    public void togglePlanDate(boolean visible) {
        planDateContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleStartDate(boolean visible) {
        startDateContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void togglePreClosingDate(boolean visible) {
        preClosingDateContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleEndDate(boolean visible) {
        endDateContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleClosingDate(boolean visible) {
        closingDateContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void createChip(String id, String name) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.rightMargin = MyUtil.dpToPx(4);
        params.bottomMargin = MyUtil.dpToPx(8);
        Chip chip = new Chip(this);
        chip.setLayoutParams(params);
        chip.setText(name);
        ChipDrawable chipDrawable = ChipDrawable.createFromAttributes(this,
                null, 0, R.style.Widget_Material3_Chip_Filter_Close_Icon_Tint);
        chip.setChipDrawable(chipDrawable);
        chip.setCheckable(false);
        chipGroup.addView(chip);
    }

    @Override
    public void clearChips() {
        chipGroup.removeAllViews();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent getIntent(Context context, String tourId){
        Intent intent = new Intent(context, TourDetailsActivity.class);
        Bundle data = new Bundle();
        data.putString(Col.SERVER_ID, tourId);
        intent.putExtras(data);
        return intent;
    }
}
