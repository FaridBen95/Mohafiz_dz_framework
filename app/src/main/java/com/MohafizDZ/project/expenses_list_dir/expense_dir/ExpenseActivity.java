package com.MohafizDZ.project.expenses_list_dir.expense_dir;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.MohafizDZ.framework_repository.Utils.BitmapUtils;
import com.MohafizDZ.framework_repository.Utils.CurrentLocationUtil;
import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.framework_repository.Utils.SwipeHelper;
import com.MohafizDZ.framework_repository.controls.ImageAdapter;
import com.MohafizDZ.framework_repository.controls.KeyValueAutoComplete;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.payment_details_dir.PaymentDetailsActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import gun0912.tedimagepicker.builder.TedImagePicker;

public class ExpenseActivity extends MyAppCompatActivity implements IExpensePresenter.View, View.OnClickListener, ImageAdapter.OnImageClickListener {
    private static final String TAG = ExpenseActivity.class.getSimpleName();
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 7796;

    private IExpensePresenter.Presenter presenter;
    private FloatingActionButton validateFloatingActionButton, addFloatingActionButton, editFloatingActionButton, imagesFloatingActionButton;
    private View currentExpenseContainer, expensesLeftContainer, allowedExpensesContainer, expensesLimitContainer, attachmentsContainer;
    private TextView expensesLeftTextView, expenseAmountTextview, allowedAmountTextView,
            expensesTextView, expensesLimitTextView, nameTextView;
    private TextInputEditText noteTextInput;
    private KeyValueAutoComplete subjectDropDownView;
    private RecyclerView recyclerView;
    private CurrentLocationUtil currentLocationUtil;
    private ImageAdapter imageAdapter;
    private TedImagePicker.Builder tedImagePicker;


    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_layout);
        init();
        findViewById();
        setControls();
        initView();
    }

    private void init(){
        presenter = new ExpensePresenterImpl(this, this, app().getCurrentUser());
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
        addFloatingActionButton = findViewById(R.id.addFloatingActionButton);
        editFloatingActionButton = findViewById(R.id.editFloatingActionButton);
        imagesFloatingActionButton = findViewById(R.id.imagesFloatingActionButton);
        validateFloatingActionButton = findViewById(R.id.validateFloatingActionButton);
        nameTextView = findViewById(R.id.nameTextView);
        expensesLeftTextView = findViewById(R.id.expensesLeftTextView);
        expenseAmountTextview = findViewById(R.id.expenseAmountTextview);
        allowedAmountTextView = findViewById(R.id.allowedAmountTextView);
        expensesTextView = findViewById(R.id.expensesTextView);
        expensesLimitTextView = findViewById(R.id.expensesLimitTextView);
        currentExpenseContainer = findViewById(R.id.currentExpenseContainer);
        recyclerView = findViewById(R.id.recyclerView);
        noteTextInput = findViewById(R.id.noteTextInput);
        subjectDropDownView = findViewById(R.id.subjectDropDownView);
        expensesLimitContainer = findViewById(R.id.expensesLimitContainer);
        allowedExpensesContainer = findViewById(R.id.allowedExpensesContainer);
        expensesLeftContainer = findViewById(R.id.expensesLeftContainer);
        attachmentsContainer = findViewById(R.id.attachmentsContainer);
    }

    private void setControls(){
        addFloatingActionButton.setOnClickListener(this);
        editFloatingActionButton.setOnClickListener(this);
        validateFloatingActionButton.setOnClickListener(this);
        imagesFloatingActionButton.setOnClickListener(this);
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                presenter.onBackPressed();
            }
        });
    }

    @Override
    public void showIgnoreChangesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.ignore_changes_title));
        builder.setMessage(getString(R.string.ignore_expense_changes_msg));
        builder.setPositiveButton(getString(R.string.ignore_label), (dialogInterface, i) -> finish());
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.create().show();
    }

    private void initView(){
        presenter.onViewCreated();
    }


    @Override
    public void openPaymentDialog(float totalToPay, Float paymentAmount) {
        TextInputLayout textInputLayout = new TextInputLayout(this);
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.margin_small);
        layoutParams.rightMargin = (int) getResources().getDimension(R.dimen.margin_small);
        TextInputEditText editText = new TextInputEditText(this);
//        editText.setOnTouchListener(
//                new SwipeHelper(this) {
//                    @Override
//                    public void onSwipeLeftToRight() {
//                        editText.setText(String.valueOf(totalToPay));
//                    }
//                });

        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.add_amount_title))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.add_label), (dialogInterface, i) -> {
                    String value = String.valueOf(editText.getText());
                    presenter.onAddPayment(value);
                });
        if(paymentAmount != null){
            editText.setText(String.valueOf(paymentAmount));
            dialogBuilder.setNeutralButton(getString(R.string.delete_label), (dialogInterface, i) -> {
                presenter.onAddPayment("0");
            });
        }
        textInputLayout.setHint(getString(R.string.amount_label));
        editText.setHint(getString(R.string.amount_label));
        textInputLayout.addView(editText);
        dialogBuilder.setView(textInputLayout);
        AlertDialog dialog = dialogBuilder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

        wmlp.gravity = Gravity.BOTTOM;
        new Handler().postDelayed(() -> {
            editText.requestFocus();
        }, 250);
        dialog.show();

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.validateFloatingActionButton){
            requestValidate();
        }else if(id == R.id.addFloatingActionButton){
            presenter.requestAddPayment();
        }else if(id == R.id.editFloatingActionButton){
            presenter.requestEditPayment();
        }else if(id == R.id.imagesFloatingActionButton){
            requestAddImage(null);
        }
    }

    private void requestValidate() {
        String subject = subjectDropDownView.getText().toString();
        String note = noteTextInput.getText().toString();
        if(TextUtils.isEmpty(subject)){
            subjectDropDownView.setError(getString(R.string.subject_required));
            return;
        }
        presenter.onValidate(subject, note);
    }

    @Override
    public void requestCurrentLocation(IExpensePresenter.LocationListener locationListener) {
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
    public void loadDetails(String id) {
        Intent intent = PaymentDetailsActivity.getIntent(this, id);
        IntentUtils.startActivity(this, intent);
    }

    @Override
    public void goBack() {
        finish();
    }

    private void requestAddImage(Integer position){
        tedImagePicker.savedDirectoryName(MConstants.applicationImagesFolder).
                start(uri -> {
                    String img = BitmapUtils.uriToBase64(uri, getContentResolver());
                    Uri compressedUri;
                    try {
                        final File compressedImage = BitmapUtils.getCompressedImage(ExpenseActivity.this, img, 612, 816, 80);
                        compressedUri = Uri.fromFile(compressedImage);
                    }catch (Exception e){
                        e.printStackTrace();
                        compressedUri = uri;
                    }
                    presenter.requestAddImage(position, compressedUri.getPath());
                });
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
    public void onLoadFinished(List<String> attachments) {
        imageAdapter.notifyDataSetChanged();
        recyclerView.invalidate();
    }

    @Override
    public void initSubjectFilter(LinkedHashMap<String, String> subjectList) {
        subjectDropDownView.setDefaultValue("");
        subjectDropDownView.setKeyValueMap(subjectList, true);
    }

    @Override
    public void setExpensesLeft(String txt) {
        expensesLeftTextView.setText(txt);
    }

    @Override
    public void setExpense(String txt) {
        expenseAmountTextview.setText(txt);
    }

    @Override
    public void setName(String txt) {
        nameTextView.setText(txt);
    }

    @Override
    public void setAllowedLimit(String txt) {
        expensesLimitTextView.setText(txt);
    }

    @Override
    public void setExpenses(String txt) {
        expensesTextView.setText(txt);
    }

    @Override
    public void setAllowedExpenses(String txt) {
        allowedAmountTextView.setText(txt);
    }

    @Override
    public void toggleAmountContainer(boolean visible) {
        currentExpenseContainer.setVisibility(getViewVisibility(visible));
    }

    private int getViewVisibility(boolean isVisible){
        return isVisible? View.VISIBLE : View.GONE;
    }

    @Override
    public void toggleAddButton(boolean visible) {
        addFloatingActionButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleEditButton(boolean visible) {
        editFloatingActionButton.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleExpenseLimit(boolean visible) {
        expensesLimitContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleAllowedExpenses(boolean visible) {
        allowedExpensesContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleExpensesLeft(boolean visible) {
        expensesLeftContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void toggleAttachmentsContainer(boolean visible) {
        attachmentsContainer.setVisibility(getViewVisibility(visible));
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent getIntent(Context context){
        return new Intent(context, ExpenseActivity.class);
    }
}
