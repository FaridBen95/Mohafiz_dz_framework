package com.MohafizDZ.project.product_form_dir;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.framework_repository.controls.KeyValueAutoComplete;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.MohafizDZ.own_distributor.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.LinkedHashMap;

import gun0912.tedimagepicker.builder.TedImagePicker;

public class ProductFormActivity extends MyAppCompatActivity implements IProductPresenter.View, View.OnClickListener, KeyValueAutoComplete.SelectionListener {
    private static final String TAG = ProductFormActivity.class.getSimpleName();
    private static final String sEDITABLE_KEY = "editable_key";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 4056;
    private String customerId;
    private boolean editable;
    private TextInputEditText descriptionTextInput, codeTextInput, nameTextInput, priceTextInput;
    private KeyValueAutoComplete categoryDropDownView;
    private ShapeableImageView imageView;
    private TextInputLayout codeTextInputLayout;
    private Button validateMaterialButton;
    private TedImagePicker.Builder tedImagePicker;
    private Menu menu;

    private IProductPresenter.Presenter presenter;
    private ActivityResultLauncher<Intent> productScanResultLauncher;
    private View.OnClickListener codeScanEndClickListener;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_form_layout);
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
        presenter = new ProductPresenterImpl(this, this, app().getCurrentUser(), customerId);
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
        productScanResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK) {
                Bundle data = result.getData().getExtras();
                presenter.onCodeScan(data.getString(ProductScanActivity.PRODUCT_CODE_KEY));
            }
        });
    }

    private void findViewById() {
        categoryDropDownView = findViewById(R.id.categoryDropDownView);
        descriptionTextInput = findViewById(R.id.descriptionTextInput);
        codeTextInput = findViewById(R.id.codeTextInput);
        nameTextInput = findViewById(R.id.nameTextInput);
        priceTextInput = findViewById(R.id.priceTextInput);
        imageView = findViewById(R.id.imageView);
        codeTextInputLayout = findViewById(R.id.codeTextInputLayout);
        validateMaterialButton = findViewById(R.id.validateMaterialButton);
    }

    private void setControls(){
        validateMaterialButton.setOnClickListener(this);
        imageView.setOnClickListener(this);
        categoryDropDownView.setSelectionListener(this);
        codeScanEndClickListener = view -> {
            requestScanCode();
        };
        codeTextInputLayout.setEndIconOnClickListener(codeScanEndClickListener);
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                presenter.onBackPressed();
            }
        });
    }

    private void requestScanCode() {
        Intent intent = ProductScanActivity.getIntent(this);
        productScanResultLauncher.launch(intent);
    }

    private void initView(){
        presenter.onViewCreated();
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

    private void validateData(){
        String name = String.valueOf(nameTextInput.getText());
        String price = String.valueOf(priceTextInput.getText());
        String code = String.valueOf(codeTextInput.getText());
        String categoryId = categoryDropDownView.getCurrentKey();
        String description = String.valueOf(descriptionTextInput.getText());
        boolean canValidate = true;
        if(TextUtils.isEmpty(name)){
            canValidate = false;
            nameTextInput.setError(getString(R.string.name_required));
        }
        if(TextUtils.isEmpty(price)){
            canValidate = false;
            priceTextInput.setError(getString(R.string.price_required));
        }
        if(TextUtils.isEmpty(categoryId) || categoryId.equals(KeyValueAutoComplete.DEFAULT_KEY)){
            canValidate = false;
            categoryDropDownView.setError(getString(R.string.category_required));
        }
        if(canValidate) {
            presenter.onValidate(name, price, code, categoryId, description);
        }
    }

    @Override
    public void onSelect(View view, String key, String value, boolean perTyping) {
        int id = view.getId();
        if(id == categoryDropDownView.getId()){
            presenter.onSelectCategory(key);
        }
    }

    private void requestLoadImage(){
        showToast(MConstants.applicationImagesFolder);
        tedImagePicker.savedDirectoryName(MConstants.applicationImagesFolder).
                start(uri -> {
//                        Bitmap bitmap = BitmapUtils.getBitmapFromUri(CustomerFormActivity.this, uri);
                    String img = BitmapUtils.uriToBase64(uri, getContentResolver());
                    Uri compressedUri;
                    try {
                        final File compressedImage = BitmapUtils.getCompressedImage(ProductFormActivity.this, img, 612, 816, 70);
                        compressedUri = Uri.fromFile(compressedImage);
                    }catch (Exception e){
                        e.printStackTrace();
                        compressedUri = uri;
                    }
                    presenter.requestUpdateImageView(BitmapUtils.uriToBase64(compressedUri, getContentResolver()));
                });
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
    public void setEditable(boolean isEditable) {
        nameTextInput.setEnabled(isEditable);
        priceTextInput.setEnabled(isEditable);
        categoryDropDownView.setEnabled(isEditable);
        codeTextInputLayout.setEndIconActivated(isEditable);
        if (isEditable) {
            codeTextInputLayout.setEndIconOnClickListener(codeScanEndClickListener);
            imageView.setOnClickListener(this);
        }else{
            imageView.setOnClickListener(null);
            codeTextInputLayout.setEndIconOnClickListener(null);
        }
        codeTextInput.setEnabled(editable);
        descriptionTextInput.setEnabled(isEditable);
    }

    @Override
    public void toggleValidateButton(boolean visible) {
        validateMaterialButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void setValidateTitle(String title) {
        validateMaterialButton.setText(title);
    }

    @Override
    public void initCategoriesFilter(LinkedHashMap<String, String> categories) {
        categoryDropDownView.setDefaultValue("");
        categoryDropDownView.setKeyValueMap(categories, true);
    }

    @Override
    public void setName(String text) {
        nameTextInput.setText(text);
    }

    @Override
    public void setPrice(String text) {
        priceTextInput.setText(text);
    }

    @Override
    public void setImage(String text) {
        Bitmap img = BitmapUtils.getBitmapImage(this, text);
        imageView.setImageBitmap(img);
    }

    @Override
    public void setCodeText(String text) {
        codeTextInput.setText(text);
    }

    @Override
    public void setCategory(String text) {
        categoryDropDownView.setText(text);
    }

    @Override
    public void setNote(String text) {
        descriptionTextInput.setText(text);
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
    public void goBack() {
        finish();
    }

    @Override
    public void restartActivity(String id, boolean editable) {
        Intent intent = getIntent(this, id, editable);
        IntentUtils.startActivity(this, intent);
        finish();
    }

    @Override
    public void enablePrice(boolean enabled) {
        priceTextInput.setEnabled(enabled);
    }

    private int getViewVisibility(boolean isVisible){
        return isVisible? View.VISIBLE : View.GONE;
    }

    public static Intent getIntent(Context context, String productId){
        return getIntent(context, productId, true);
    }

    public static Intent getIntent(Context context, String productId, boolean isEditable){
        Intent intent = new Intent(context, ProductFormActivity.class);
        Bundle data = new Bundle();
        data.putString(Col.SERVER_ID, productId);
        data.putBoolean(sEDITABLE_KEY, isEditable);
        intent.putExtras(data);
        return intent;
    }
}
