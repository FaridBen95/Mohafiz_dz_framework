package com.MohafizDZ.project.settings_dir;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.MohafizDZ.App;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.own_distributor.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import ru.bullyboo.encoder.Encoder;
import ru.bullyboo.encoder.methods.AES;

public class SettingsActivity extends MyAppCompatActivity implements ISettingsPresenter.View, MaterialButtonToggleGroup.OnButtonCheckedListener {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static final int QR_CODE_WIDTH = 500; // Width in pixels
    private static final int QR_CODE_HEIGHT = 500; // Height in pixels

    private ISettingsPresenter.Presenter presenter;
    private TextView companyNameTextView, nameTextView, joinDateTextView, roleTextView;
    private View companyQrCodeContainer;
    private MaterialButton distInviteButton, adminButton, adminInviteButton;
    private MaterialButtonToggleGroup toggleGroup;
    private ImageView qrCodeImageView;

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        initArgs();
        init();
        findViewById();
        setControls();
        initView();
    }

    private void initArgs(){
        Bundle data = getIntent().getExtras();
    }

    private void init(){
        DataRow currentUserRow = app().getCurrentUser();
        presenter = new SettingsPresenterImpl(this, this, currentUserRow);
    }

    private void findViewById() {
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        adminButton = findViewById(R.id.adminButton);
        adminInviteButton = findViewById(R.id.adminInviteButton);
        distInviteButton = findViewById(R.id.distInviteButton);
        toggleGroup = findViewById(R.id.toggleGroup);
        companyQrCodeContainer = findViewById(R.id.companyQrCodeContainer);
        roleTextView = findViewById(R.id.roleTextView);
        joinDateTextView = findViewById(R.id.joinDateTextView);
        nameTextView = findViewById(R.id.nameTextView);
        companyNameTextView = findViewById(R.id.companyNameTextView);
    }

    private void setControls(){
        toggleGroup.addOnButtonCheckedListener(this);
    }

    private void initView(){
        presenter.onViewCreated();
    }


    @Override
    public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
        if(checkedId == R.id.distInviteButton && isChecked){
            prepareDistInvite(false);
        }
        if(checkedId == R.id.adminInviteButton && isChecked){
            prepareDistInvite(true);
        }
        if(checkedId == R.id.adminButton && isChecked){
            presenter.generateAdminQrCode();
        }
    }

    private void prepareDistInvite(boolean isAdmin){
        TextInputLayout textInputLayout = new TextInputLayout(this);
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.margin_small);
        layoutParams.rightMargin = (int) getResources().getDimension(R.dimen.margin_small);
        TextInputEditText editText = new TextInputEditText(this);
        final MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle(isAdmin? getString(R.string.admin_invite_label) : getString(R.string.distributor_invite_label))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.validate_label), (dialogInterface, i) -> {
                    String code = String.valueOf(editText.getText());
                    if(isAdmin){
                        presenter.generateAdminInviteQrCode(code);
                    }else {
                        presenter.generateDistInviteQrCode(code);
                    }
                });
        textInputLayout.setHint(getString(R.string.van_code_label));
        editText.setHint(getString(R.string.van_code_label));
        textInputLayout.addView(editText);
        dialogBuilder.setView(textInputLayout);
        dialogBuilder.show();
    }

    @Override
    public void setCompanyName(String text) {
        companyNameTextView.setText(text);
    }

    @Override
    public void setRole(String text) {
        roleTextView.setText(text);
    }

    @Override
    public void setUserName(String text) {
        nameTextView.setText(text);
    }

    @Override
    public void setJoinDate(String text) {
        joinDateTextView.setText(text);
    }

    private int getViewVisibility(boolean visible){
        return visible? View.VISIBLE : View.GONE;
    }

    @Override
    public void toggleQrCodeContainer(boolean visible) {
        companyQrCodeContainer.setVisibility(getViewVisibility(visible));
    }

    @Override
    public void showQrCode(String qrCodeContent) {
        try {
            // Generate QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(getEncryptedQrCodeContent(qrCodeContent), BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
            // Convert BitMatrix to Bitmap
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            // Set the Bitmap to the ImageView
            qrCodeImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private String getEncryptedQrCodeContent(String qrCodeContent){
        return App.TEST_MODE? qrCodeContent : Encoder.BuilderAES()
                .message(qrCodeContent)
                .method(AES.Method.AES_CBC_PKCS5PADDING)
                .key("JWYXelshE9saPgnpcRVuJWYXelshE9sa")
                .keySize(AES.Key.SIZE_256)
                .encrypt();
    }

    public static Intent getIntent(Context context){
        Intent intent = new Intent(context, SettingsActivity.class);
        Bundle data = new Bundle();
        intent.putExtras(data);
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
