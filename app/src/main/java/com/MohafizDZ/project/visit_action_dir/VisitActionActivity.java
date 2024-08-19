package com.MohafizDZ.project.visit_action_dir;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.MohafizDZ.framework_repository.Utils.BitmapUtils;
import com.MohafizDZ.framework_repository.Utils.CurrentLocationUtil;
import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.framework_repository.controls.ImageAdapter;
import com.MohafizDZ.framework_repository.controls.KeyValueAutoComplete;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.home_dir.visit_presenter_dir.IVisitPresenter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import gun0912.tedimagepicker.builder.TedImagePicker;

public class VisitActionActivity extends MyAppCompatActivity implements IVisitActionPresenter.View, ImageAdapter.OnImageClickListener, View.OnClickListener, KeyValueAutoComplete.SelectionListener {
    private static final String TAG = VisitActionActivity.class.getSimpleName();
    private static final String IS_EDITABLE_KEY = "is_editable_key";
    private static final String CUSTOMER_ID_KEY = "customer_id_key";
    private static final String ACTION_KEY = "action_key";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 7796;
    private IVisitActionPresenter.Presenter presenter;
    private String actionId, customerId, action;
    private TextView customerNameTextView, noteTextView, dateTextView, distanceTextView, actionTextView, tourTextview;
    private TextInputEditText noteTextInput;
    private View attachmentsContainer, noteTextInputLayout, noActionCategoryInputLayout;
    private KeyValueAutoComplete categoryDropDownView;
    private RecyclerView recyclerView;
    private FloatingActionButton addFloatingActionButton, validateFloatingActionButton;
    private ImageAdapter imageAdapter;
    private boolean isEditable;
    private TedImagePicker.Builder tedImagePicker;
    private CurrentLocationUtil currentLocationUtil;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.action_details_layout);
        if (initArgs()) {
            init();
            findViewById();
            setControls();
            initView();
        }else{
            showToast(getString(R.string.error_occurred));
            finish();
        }
    }

    private boolean initArgs(){
        Bundle data = getIntent().getExtras();
        actionId = data != null? data.getString(Col.SERVER_ID) : null;
        isEditable = data != null && data.getBoolean(IS_EDITABLE_KEY);
        customerId = data != null? data.getString(CUSTOMER_ID_KEY) : null;
        action = data != null? data.getString(ACTION_KEY) : null;
        return actionId != null || action != null;
    }

    private void init(){
        presenter = new VisitActionPresenterImpl(this, this, app().getCurrentUser(), actionId);
        presenter.setEditable(isEditable);
        if(customerId != null){
            presenter.setCustomerId(customerId);
        }
        if(action != null){
            presenter.setAction(action);
        }
        initConfig();
    }

    private void initConfig(){
        currentLocationUtil = new CurrentLocationUtil(this, this, null, false);
        tedImagePicker = TedImagePicker.with(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission is not granted, request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private void findViewById() {
        noteTextView = findViewById(R.id.noteTextView);
        dateTextView = findViewById(R.id.dateTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        actionTextView = findViewById(R.id.actionTextView);
        tourTextview = findViewById(R.id.tourTextview);
        customerNameTextView = findViewById(R.id.customerNameTextView);
        recyclerView = findViewById(R.id.recyclerView);
        attachmentsContainer = findViewById(R.id.attachmentsContainer);
        addFloatingActionButton = findViewById(R.id.addFloatingActionButton);
        validateFloatingActionButton = findViewById(R.id.validateFloatingActionButton);
        noteTextInput = findViewById(R.id.noteTextInput);
        noteTextInputLayout = findViewById(R.id.noteTextInputLayout);
        noActionCategoryInputLayout = findViewById(R.id.noActionCategoryInputLayout);
        categoryDropDownView = findViewById(R.id.categoryDropDownView);
    }

    private void setControls(){
        addFloatingActionButton.setOnClickListener(this);
        validateFloatingActionButton.setOnClickListener(this);
        categoryDropDownView.setSelectionListener(this);
    }

    private void initView(){
        presenter.onViewCreated();
    }

    @Override
    public void setCustomerName(String txt) {
        customerNameTextView.setText(txt);
    }

    @Override
    public void setTourName(String txt) {
        tourTextview.setText(txt);
    }

    @Override
    public void setActionType(String txt) {
        actionTextView.setText(txt);
    }

    @Override
    public void setDistance(String txt) {
        distanceTextView.setText(txt);
    }

    @Override
    public void setDate(String txt) {
        dateTextView.setText(txt);
    }

    @Override
    public void setNote(String txt) {
        noteTextView.setText(txt);
        noteTextInput.setText(txt);
    }

    @Override
    public void goBack() {
        finish();
    }

    @Override
    public void initAdapter(List<String> attachments) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageAdapter = new ImageAdapter(this, R.layout.simple_image_item, attachments, this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(imageAdapter);
    }

    @Override
    public void onLoadFinished(List<String> attachments) {
        imageAdapter.notifyDataSetChanged();
        recyclerView.invalidate();
    }

    @Override
    public void showModifyImageDialog(int position) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.modify_image_title))
                .setNegativeButton(getString(R.string.cancel), null)
                .setNeutralButton(getString(R.string.delete_label), (dialogInterface, i) -> {
                    presenter.deleteImage(position);
                })
                .setPositiveButton(getString(R.string.modify_label), (dialogInterface, i) -> {
                    requestAddImage(position);
                }).create().show();
    }

    @Override
    public void requestCurrentLocation(IVisitActionPresenter.LocationListener locationListener) {
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
    public void loadDetails(String actionId) {
        Intent intent = getIntent(this, actionId, false);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void toggleAttachmentsContainer(boolean visible) {
        attachmentsContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleAddImageButton(boolean visible) {
        addFloatingActionButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleValidateButton(boolean visible) {
        validateFloatingActionButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleNoteInput(boolean visible) {
        noteTextInputLayout.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleNoteTextView(boolean visible) {
        noteTextView.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleNoActionCategory(boolean visible) {
        noActionCategoryInputLayout.setVisibility(getViewVisibility(visible));
    }

    private int getViewVisibility(boolean visible){
        return visible? View.VISIBLE : View.GONE;
    }


    @Override
    public void onAddImage() {

    }

    @Override
    public void onImageClick(int position) {
        presenter.requestModifyImage(position);
    }

    @Override
    public void onImageLongClick(int position) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.addFloatingActionButton){
            requestAddImage(null);
        }else if(id == R.id.validateFloatingActionButton){
            requestValidate();
        }
    }
    
    private void requestValidate(){
        String categoryId = categoryDropDownView.getCurrentKey();
        String note = noteTextInput.getText().toString();
        if(noActionCategoryInputLayout.getVisibility() == View.GONE) {
            categoryId = "false";
        }else {
            if (categoryId.equals(KeyValueAutoComplete.DEFAULT_KEY)) {
                categoryDropDownView.setError(getString(R.string.category_required));
                return;
            }
        }
        presenter.onValidate(categoryId, note);
    }

    private void requestAddImage(Integer position){
        tedImagePicker.savedDirectoryName(MConstants.applicationImagesFolder).
                start(uri -> {
                    String img = BitmapUtils.uriToBase64(uri, getContentResolver());
                    Uri compressedUri;
                    try {
                        final File compressedImage = BitmapUtils.getCompressedImage(VisitActionActivity.this, img, 612, 816, 80);
                        compressedUri = Uri.fromFile(compressedImage);
                    }catch (Exception e){
                        e.printStackTrace();
                        compressedUri = uri;
                    }
                    presenter.requestAddImage(position, compressedUri.getPath());
                });
    }

    public static Intent getIntent(Context context, String action, String customerId){
        Intent intent = new Intent(context, VisitActionActivity.class);
        Bundle data = new Bundle();
        data.putString(CUSTOMER_ID_KEY, customerId);
        data.putString(ACTION_KEY, action);
        data.putBoolean(IS_EDITABLE_KEY, true);
        intent.putExtras(data);
        return intent;
    }

    @Override
    public void onSelect(View view, String key, String value, boolean perTyping) {
        int id = view.getId();
        if(id == categoryDropDownView.getId()){
            presenter.onSelectCategory(key);
        }
    }

    @Override
    public void setCategory(String category) {
        categoryDropDownView.setText(category);
    }

    @Override
    public void setCategoryEnabled(boolean enabled) {
        noActionCategoryInputLayout.setEnabled(enabled);
        categoryDropDownView.setEnabled(enabled);
    }

    @Override
    public void initCategoriesFilter(LinkedHashMap<String, String> categories) {
        categoryDropDownView.setDefaultValue("");
        categoryDropDownView.setKeyValueMap(categories, true);
    }

    @Override
    public void requestCreateCustomerCategory() {
        categoryDropDownView.setText("");
        TextInputLayout textInputLayout = new TextInputLayout(this);
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.margin_small);
        layoutParams.rightMargin = (int) getResources().getDimension(R.dimen.margin_small);
        TextInputEditText editText = new TextInputEditText(this);
        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.create_category_label))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.create_label), (dialogInterface, i) -> {
                    String name = String.valueOf(editText.getText());
                    presenter.createCategory(name);
                });
        textInputLayout.setHint(getString(R.string.name_label));
        editText.setHint(getString(R.string.name_label));
        textInputLayout.addView(editText);
        dialogBuilder.setView(textInputLayout);
        dialogBuilder.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent getIntent(Context context, String actionId){
        return getIntent(context, actionId, false);
    }

    public static Intent getIntent(Context context, String actionId, boolean isEditable){
        Intent intent = new Intent(context, VisitActionActivity.class);
        Bundle data = new Bundle();
        data.putString(Col.SERVER_ID, actionId);
        data.putBoolean(IS_EDITABLE_KEY, isEditable);
        intent.putExtras(data);
        return intent;
    }
}
