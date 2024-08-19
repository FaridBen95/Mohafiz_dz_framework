package com.MohafizDZ.project.customer_details_dir.form_dir;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.MohafizDZ.framework_repository.Utils.BitmapUtils;
import com.MohafizDZ.framework_repository.Utils.CurrentLocationUtil;
import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.framework_repository.controls.KeyValueAutoComplete;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.customer_details_dir.CustomerDetailsActivity;
import com.MohafizDZ.project.regions_map_dir.RegionsMapActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.LinkedHashMap;

import gun0912.tedimagepicker.builder.TedImagePicker;

public class CustomerFormActivity extends MyAppCompatActivity implements ICustomerFormPresenter.View, View.OnClickListener, KeyValueAutoComplete.SelectionListener {
    private static final String TAG = CustomerFormActivity.class.getSimpleName();
    private static final String sEDITABLE_KEY = "editable_key";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 4056;

    private ICustomerFormPresenter.Presenter presenter;
    private String customerId;
    private boolean editable;
    private TextInputEditText noteTextInput, addressTextInput, gpsPositionTextInput, codeTextInput, nameTextInput,
            phoneTextInput, balanceLimitTextInput;
    private KeyValueAutoComplete regionDropDownView, categoryDropDownView;
    private ShapeableImageView imageView;
    private TextInputLayout gpsPositionInputLayout;
    private Button validateMaterialButton;
    private CurrentLocationUtil currentLocationUtil;
    private boolean tedImagePickerIsShown;
    private TedImagePicker.Builder tedImagePicker;
    private ActivityResultLauncher<Intent> regionResultLauncher;
    private Menu menu;
    private View.OnClickListener gpsEndIconListener;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_form_layout);
        initArgs();
        init();
        findViewById();
        setControls();
        initView();
    }

    private void initArgs(){
        Bundle data = getIntent().getExtras();
        if(data != null){
            customerId = data.getString(Col.SERVER_ID, null);
            editable = data.getBoolean(sEDITABLE_KEY, true);
        }else{
            finish();
        }
    }

    private void init(){
        presenter = new CustomerFormPresenterImpl(this, this, app().getCurrentUser(), customerId);
        presenter.setEditable(editable);
        initConfig();
        initResultLaunchers();
    }

    private void initConfig(){
        tedImagePicker = TedImagePicker.with(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission is not granted, request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private void initResultLaunchers(){
        regionResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            try{
                regionDropDownView.setText("");
                presenter.onRegionCreated();
            }catch (Exception ignored){}
        });
    }

    private void findViewById() {
        regionDropDownView = findViewById(R.id.regionDropDownView);
        categoryDropDownView = findViewById(R.id.categoryDropDownView);
        noteTextInput = findViewById(R.id.noteTextInput);
        addressTextInput = findViewById(R.id.addressTextInput);
        gpsPositionTextInput = findViewById(R.id.gpsPositionTextInput);
        codeTextInput = findViewById(R.id.codeTextInput);
        nameTextInput = findViewById(R.id.nameTextInput);
        phoneTextInput = findViewById(R.id.phoneTextInput);
        balanceLimitTextInput = findViewById(R.id.balanceLimitTextInput);
        imageView = findViewById(R.id.imageView);
        gpsPositionInputLayout = findViewById(R.id.gpsPositionInputLayout);
        validateMaterialButton = findViewById(R.id.validateMaterialButton);
    }

    private void setControls(){
        validateMaterialButton.setOnClickListener(this);
        imageView.setOnClickListener(this);
        categoryDropDownView.setSelectionListener(this);
        regionDropDownView.setSelectionListener(this);
        gpsEndIconListener = view -> {
            presenter.requestCurrentLocation();
        };
        gpsPositionInputLayout.setEndIconOnClickListener(gpsEndIconListener);
        currentLocationUtil = new CurrentLocationUtil(this, this, new CurrentLocationUtil.MyLocationListener() {
            @Override
            public void loading() {
                showToast(getString(R.string.recovering_gps_msg));
            }

            @Override
            public boolean printLocation(Double latitude, Double longitude) {
                presenter.onGpsLocationRecovered(latitude, longitude);
                return true;
            }

            @Override
            public boolean checkLocation(boolean failedInformed) {
                return false;
            }
        }, false);
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                presenter.onBackPressed();
            }
        });
    }

    @Override
    public void requestCurrentLocation() {
        currentLocationUtil.execute();
    }

    private void initView(){
        presenter.onViewCreated();
    }

    public static Intent getIntent(Context context, String customerId){
        return getIntent(context, customerId, true);
    }

    public static Intent getIntent(Context context, String customerId, boolean isEditable){
        Intent intent = new Intent(context, CustomerFormActivity.class);
        Bundle data = new Bundle();
        data.putString(Col.SERVER_ID, customerId);
        data.putBoolean(sEDITABLE_KEY, isEditable);
        intent.putExtras(data);
        return intent;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.validateMaterialButton){
            validateData();
        }else if(id == R.id.imageView){
            requestLoadImage();
        }
    }

    private void requestLoadImage(){
        tedImagePicker.savedDirectoryName(MConstants.applicationImagesFolder).
                start(uri -> {
//                        Bitmap bitmap = BitmapUtils.getBitmapFromUri(CustomerFormActivity.this, uri);
                    String img = BitmapUtils.uriToBase64(uri, getContentResolver());
                    Uri compressedUri;
                    try {
                        final File compressedImage = BitmapUtils.getCompressedImage(CustomerFormActivity.this, img, 612, 816, 70);
                        compressedUri = Uri.fromFile(compressedImage);
                    }catch (Exception e){
                        e.printStackTrace();
                        compressedUri = uri;
                    }
                    presenter.requestUpdateImageView(BitmapUtils.uriToBase64(compressedUri, getContentResolver()));
                });
    }

    private void validateData(){
        String name = String.valueOf(nameTextInput.getText());
        String phoneNum = String.valueOf(phoneTextInput.getText());
        String code = String.valueOf(codeTextInput.getText());
        String categoryId = categoryDropDownView.getCurrentKey();
        String regionId = regionDropDownView.getCurrentKey();
        String geoHash = String.valueOf(gpsPositionTextInput.getText());
        Pair<Double, Double> latLng = (Pair) gpsPositionTextInput.getTag();
        String address = String.valueOf(addressTextInput.getText());
        String note = String.valueOf(noteTextInput.getText());
        String balanceLimit = String.valueOf(balanceLimitTextInput.getText());
        boolean canValidate = true;
        if(TextUtils.isEmpty(name)){
            canValidate = false;
            nameTextInput.setError(getString(R.string.name_required));
        }
        if(TextUtils.isEmpty(categoryId) || categoryId.equals(KeyValueAutoComplete.DEFAULT_KEY)){
            canValidate = false;
            categoryDropDownView.setError(getString(R.string.category_required));
        }
        if(TextUtils.isEmpty(regionId) || regionId.equals(KeyValueAutoComplete.DEFAULT_KEY)){
            canValidate = false;
            regionDropDownView.setError(getString(R.string.category_required));
        }
        Double latitude = 0.0d;
        Double longitude = 0.0d;
        if(TextUtils.isEmpty(geoHash)){
            canValidate = false;
            gpsPositionTextInput.setError(getString(R.string.gps_position_required));
        }else{
            latitude = latLng.first;
            longitude = latLng.second;
        }
        if(TextUtils.isEmpty(address)){
            canValidate = false;
            addressTextInput.setError(getString(R.string.address_required));
        }
        if(canValidate) {
            presenter.onValidate(name, phoneNum, code, balanceLimit, categoryId, regionId, latitude, longitude, geoHash, address, note);
        }
    }

    @Override
    public void loadDetails(String id) {
        Intent intent = CustomerDetailsActivity.getIntent(this, id);
        IntentUtils.startActivity(this, intent);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void goBack() {
        finish();
    }

    @Override
    public void setName(String text) {
        nameTextInput.setText(text);
    }

    @Override
    public void setCodeText(String text) {
        codeTextInput.setText(text);
    }

    @Override
    public void setPhoneNum(String text) {
        phoneTextInput.setText(text);
    }

    @Override
    public void setBalanceLimit(String text) {
        balanceLimitTextInput.setText(text);
    }

    @Override
    public void setCategory(String text) {
        categoryDropDownView.setText(text);
    }

    @Override
    public void setRegion(String text) {
        regionDropDownView.setText(text);
    }

    @Override
    public void setGpsLocation(String text, Double latitude, Double longitude) {
        gpsPositionTextInput.setEnabled(true);
        gpsPositionTextInput.setText(text);
        gpsPositionTextInput.setTag(new Pair<>(latitude, longitude));
        gpsPositionTextInput.setEnabled(false);
    }

    @Override
    public void setAddress(String text) {
        addressTextInput.setText(text);
    }

    @Override
    public void setNote(String text) {
        noteTextInput.setText(text);
    }

    @Override
    public void setImage(String text) {
        Bitmap img = BitmapUtils.getBitmapImage(this, text);
        imageView.setImageBitmap(img);
    }

    @Override
    public void showIgnoreChangesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.ignore_changes_title));
        builder.setMessage(getString(R.string.ignore_customer_changes_msg));
        builder.setPositiveButton(getString(R.string.ignore_label), (dialogInterface, i) -> finish());
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.create().show();
    }

    @Override
    public void initRegionsFilter(LinkedHashMap<String, String> regions) {
        regionDropDownView.setDefaultValue("");
        regionDropDownView.setKeyValueMap(regions, true);
    }

    @Override
    public void initCategoriesFilter(LinkedHashMap<String, String> categories) {
        categoryDropDownView.setDefaultValue("");
        categoryDropDownView.setKeyValueMap(categories, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        this.menu = menu;
        presenter.onCreateOptionsMenu();
        menu.findItem(R.id.menuSave).setVisible(false);
        return true;
    }

    @Override
    public void toggleEditItem(boolean visible) {
        try {
            menu.findItem(R.id.menuEdit).setVisible(visible);
        }catch (Exception ignored){}
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            presenter.onBackPressed();
        }else if(id == R.id.menuEdit){
            presenter.setEditable(true);
            presenter.onRefresh();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSelect(View view, String key, String value, boolean perTyping) {
        int id = view.getId();
        if(id == categoryDropDownView.getId()){
            presenter.onSelectCategory(key);
        }else if(id == regionDropDownView.getId()){
            presenter.onSelectRegion(key);
        }
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
    public void openRegionMap() {
        regionResultLauncher.launch(RegionsMapActivity.getIntent(this));
    }

    @Override
    public void setCodeEnabled(boolean enabled) {
        codeTextInput.setEnabled(enabled);
    }

    @Override
    public void setBalanceLimitEnabled(boolean enabled) {
        balanceLimitTextInput.setEnabled(enabled);
    }

    @Override
    public void setEditable(boolean isEditable) {
        phoneTextInput.setEnabled(isEditable);
        nameTextInput.setEnabled(isEditable);
        categoryDropDownView.setEnabled(isEditable);
        regionDropDownView.setEnabled(isEditable);
        gpsPositionInputLayout.setEndIconActivated(isEditable);
        addressTextInput.setEnabled(isEditable);
        noteTextInput.setEnabled(isEditable);
        if(isEditable){
            gpsPositionInputLayout.setEndIconOnClickListener(gpsEndIconListener);
            imageView.setOnClickListener(this);
        }else{
            gpsPositionInputLayout.setEndIconOnClickListener(null);
            imageView.setOnClickListener(null);
        }
    }

    @Override
    public void toggleValidateButton(boolean visible) {
        validateMaterialButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void setValidateTitle(String title) {
        validateMaterialButton.setText(title);
    }

    private int getViewVisibility(boolean isVisible){
        return isVisible? View.VISIBLE : View.GONE;
    }
}
