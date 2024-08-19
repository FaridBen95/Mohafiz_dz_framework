package com.MohafizDZ.project.cash_box_dir;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.MohafizDZ.framework_repository.controls.KeyValueAutoComplete;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.LinkedHashMap;
import java.util.List;

public class CashBoxActivity extends MyAppCompatActivity implements ICashBoxPresenter.View, View.OnClickListener, CashBoxLinesAdapter.OnItemClickListener {
    private static final String TAG = CashBoxActivity.class.getSimpleName();
    private static final String TOUR_ID_KEY = "tour_id_key";
    private static final String IS_EDITABLE_KEY = "IS_EDITABLE_KEY";

    private ICashBoxPresenter.Presenter presenter;
    private FloatingActionButton addFloatingActionButton, validateFloatingActionButton;
    private TextView cashBoxTextView, nameTextView, totalTextView, expensesTextView, refundsTextView, paymentsTextView, validatedDateTextView;
    private RecyclerView recyclerView;
    private View cashBoxDetailsContainer, detailsContainer, validatedDateContainer;
    private MaterialButton toggleDetailsButton;
    private CashBoxLinesAdapter adapter;
    private String tourId;
    private boolean isEditable;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cash_box_layout);
        initArgs();
        init();
        findViewById();
        setControls();
        initView();
    }

    private void initArgs(){
        Bundle data = getIntent().getExtras();
        tourId = data != null? data.getString(TOUR_ID_KEY) : null;
        isEditable = data != null && data.getBoolean(IS_EDITABLE_KEY);
    }

    private void init(){
        presenter = new CashBoxPresenterImpl(this, this, app().getCurrentUser());
        presenter.setTourId(tourId);
        presenter.setEditable(isEditable);
    }

    private void findViewById() {
        toggleDetailsButton = findViewById(R.id.toggleDetailsButton);
        addFloatingActionButton = findViewById(R.id.addFloatingActionButton);
        validateFloatingActionButton = findViewById(R.id.validateFloatingActionButton);
        recyclerView = findViewById(R.id.recyclerView);
        totalTextView = findViewById(R.id.totalTextView);
        expensesTextView = findViewById(R.id.expensesTextView);
        refundsTextView = findViewById(R.id.refundsTextView);
        paymentsTextView = findViewById(R.id.paymentsTextView);
        validatedDateTextView = findViewById(R.id.validatedDateTextView);
        validatedDateContainer = findViewById(R.id.validatedDateContainer);
        cashBoxDetailsContainer = findViewById(R.id.cashBoxDetailsContainer);
        detailsContainer = findViewById(R.id.detailsContainer);
        cashBoxTextView = findViewById(R.id.cashBoxTextView);
        nameTextView = findViewById(R.id.nameTextView);
    }

    private void setControls() {
        toggleDetailsButton.setOnClickListener(this);
        addFloatingActionButton.setOnClickListener(this);
        validateFloatingActionButton.setOnClickListener(this);
    }

    private void initView() {
        presenter.onViewCreated();
    }

    @Override
    public void onLoadFinished(List<DataRow> rows, boolean validated) {
        adapter.setValidated(validated);
        adapter.notifyDataSetChanged();
        recyclerView.invalidate();
    }

    @Override
    public void initAdapter(List<DataRow> rows, boolean validated) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CashBoxLinesAdapter(this, rows, this, validated);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    private int getViewVisibility(boolean isVisible){
        return isVisible? View.VISIBLE : View.GONE;
    }
    @Override
    public void toggleAddButton(boolean visible) {
        addFloatingActionButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleValidateButton(boolean visible) {
        validateFloatingActionButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void goBack() {
        finish();
    }

    @Override
    public void toggleCashBoxDetails(boolean visible) {
        detailsContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void setCashBoxTotal(String txt) {
        cashBoxTextView.setText(txt);
    }

    @Override
    public void setName(String txt) {
        nameTextView.setText(txt);
    }

    @Override
    public void setTotalPayments(String txt) {
        paymentsTextView.setText(txt);
    }

    @Override
    public void setTotalRefunds(String txt) {
        refundsTextView.setText(txt);
    }

    @Override
    public void setTotalExpenses(String txt) {
        expensesTextView.setText(txt);
    }

    @Override
    public void setTotal(String txt) {
        totalTextView.setText(txt);
    }

    @Override
    public void showValidateDate(String validateDate) {
        validatedDateContainer.setVisibility(getViewVisibility(true));
        validatedDateTextView.setText(validateDate);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.addFloatingActionButton){
            presenter.onAddClicked();
        }else if(id == R.id.validateFloatingActionButton){
            presenter.onValidate();
        }else if(id == R.id.toggleDetailsButton){
            presenter.requestToggleDetails();
        }
    }

    @Override
    public void updateToggleButtonIcon(int drawableResId) {
        toggleDetailsButton.setIcon(ContextCompat.getDrawable(this, drawableResId));
    }

    @Override
    public void toggleCashBoxDetailsContainer(boolean visible) {
        cashBoxDetailsContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void showLineCreationDialog(LinkedHashMap<String, String> denominations, Integer denominationValue, Integer count, boolean editable) {
        final View view = getLayoutInflater().inflate(R.layout.cash_box_line_dialog, null, false);
        TextInputEditText countTextInput = view.findViewById(R.id.countTextInput);
        KeyValueAutoComplete denominationDropDownView = view.findViewById(R.id.denominationDropDownView);
        denominationDropDownView.setDefaultValue("");
        denominationDropDownView.setKeyValueMap(denominations, true);
        if(denominationValue != null){
            denominationDropDownView.setText(String.valueOf(denominationValue));
            denominationDropDownView.setEnabled(false);
        }
        if(count != null){
            countTextInput.setText(String.valueOf(count));
        }
        String title = editable? getString(R.string.edit_line_title): getString(R.string.create_line_title);
        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(view)
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.validate_label), (dialogInterface, i) -> {
                    String denomination = denominationDropDownView.getText().toString();
                    String countValue = countTextInput.getText().toString();
                    presenter.createOrUpdateLine(editable, denomination, countValue);
                });
        if(editable){
            dialogBuilder.setNeutralButton(getString(R.string.delete_label), (dialogInterface, i) -> {
                presenter.deleteLine(denominationValue);
            });
        }
        dialogBuilder.create().show();
        new Handler().postDelayed(() -> {
            if(editable){
                countTextInput.requestFocus();
                countTextInput.setSelection(countTextInput.getText().length());
            }else{
                denominationDropDownView.requestFocus();
                denominationDropDownView.setSelection(denominationDropDownView.getText().length());
            }
                }, 250);
    }

    @Override
    public void onItemClick(int position) {
        presenter.onItemClick(position);
    }

    @Override
    public void onItemLongClick(int position) {

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent getIntent(Context context, String tourId){
        return getIntent(context, tourId, false);
    }

    public static Intent getIntent(Context context, String tourId, boolean isEditable){
        Intent intent = new Intent(context, CashBoxActivity.class);
        Bundle data = new Bundle();
        data.putString(TOUR_ID_KEY, tourId);
        data.putBoolean(IS_EDITABLE_KEY, isEditable);
        intent.putExtras(data);
        return intent;
    }

}
