package com.MohafizDZ.framework_repository.core.Account;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.framework_repository.Utils.EcomerceXTools;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.tfcporciuncula.phonemoji.PhonemojiTextInputEditText;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class PhoneLogInActivity extends MyAppCompatActivity implements View.OnClickListener {
    public static final String TAG = PhoneLogInActivity.class.getSimpleName();
    private static final String PHONE_KEY = "phone_key";
    private static final int PHONE_VERIFICATION_KEY = 417;

    private LinearLayout nextLinearLayout;
    private TextInputEditText phoneTextInputEditText;
    private TextInputEditText phoneCodeInputEditText;
    private ImageView btnBack;
    private AlertDialog progressDialog;
    private TextView titleTextView;
    private PhonemojiTextInputEditText phonemojiTextInputEditText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_with_phone);
        init();
        initArgs();
        setControls();
        initToolBar();
        prepareView();
    }

    private void initArgs() {
        Bundle extras = getIntent().getExtras();
    }

    private void initToolBar() {
        EcomerceXTools.setSystemBarColor(this, R.color.main_theme_primary);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void prepareView() {
        titleTextView.setText(getResources().getString(R.string.link_with_phone_title));
        phonemojiTextInputEditText.setSelection(phonemojiTextInputEditText.getText().length());

    }

    private void init() {
//        phoneTextInputEditText = findViewById(R.id.phoneTextInputEditText);
        phonemojiTextInputEditText = findViewById(R.id.phonemojiTextInputEditText);
        nextLinearLayout = findViewById(R.id.nextLinearLayout);
//        phoneCodeInputEditText = findViewById(R.id.phoneCodeInputEditText);
        btnBack = findViewById(R.id.btnBack);
        titleTextView = findViewById(R.id.titleTextView);
        progressDialog = MyUtil.getProgressDialog(this);
    }

    private void setControls() {
        nextLinearLayout.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        phonemojiTextInputEditText.setOnEditorActionListener(new TextInputEditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    nextLinearLayout.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void setTitleBar(ActionBar actionBar) {

    }

    @Override
    public Toolbar setToolBar() {
        return null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnBack) {
            onBackPressed();
        } else if (id == R.id.nextLinearLayout) {
            if (app().inNetwork()) {
                logIn();
            } else {
                makeText(this, getResources().getString(R.string.you_need_internet_connection), LENGTH_SHORT).show();
            }
        }
    }

    private void logIn() {
        String phone = phonemojiTextInputEditText.getText().toString();
        if(phone.equals("")){
            phonemojiTextInputEditText.setError(getResources().getString(R.string.phone_required));
            return;
        }
        Intent intent = new Intent(this, PhoneVerificationActivity.class);
        Bundle data = new Bundle();
        data.putString(PHONE_KEY, phone);
        intent.putExtras(data);
        startActivityForResult(intent, PHONE_VERIFICATION_KEY);
    }

    private void logIn2() {
        String phone = phoneTextInputEditText.getText().toString();
        String phoneCode = phoneCodeInputEditText.getText().toString();
        if(phone.equals("")){
            phoneTextInputEditText.setError(getResources().getString(R.string.phone_required));
            return;
        }
        if(phoneCode.equals("")){
            phoneCodeInputEditText.setError(getResources().getString(R.string.phone_code_required));
            return;
        }
        if(phone.charAt(0) == '0'){
            phone = phone.substring(1);
        }
        String fullPhone = "+".concat(phoneCode).concat(phone);
        fullPhone = fullPhone.trim();
        Intent intent = new Intent(this, PhoneVerificationActivity.class);
        Bundle data = new Bundle();
        data.putString(PHONE_KEY, fullPhone);
        intent.putExtras(data);
        startActivityForResult(intent, PHONE_VERIFICATION_KEY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PHONE_VERIFICATION_KEY){
            if(resultCode == Activity.RESULT_OK) {
                Intent intent = new Intent();
                Bundle extras = data != null? data.getExtras() : null;
                if(data != null) {
                    intent.putExtras(data);
                }
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        }
    }
}
