package com.MohafizDZ.framework_repository.core.Account;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.MohafizDZ.empty_project.R;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.framework_repository.Utils.EcomerceXTools;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.MohafizDZ.framework_repository.core.Account.MainLogInActivity.PHONE_CREDENTIAL_KEY;

public class PhoneVerificationActivity extends MyAppCompatActivity implements View.OnClickListener {
    public final String TAG = PhoneVerificationActivity.class.getSimpleName();

    private LinearLayout nextLinearLayout;
    private AlertDialog progressDialog;
    private String phoneNumber;
    private String verificationId;
    private TextView currentPhoneTextView;
    private TextView resendTextView;
    private EditText principalEditText;
    private EditText et1;
    private EditText et2;
    private EditText et3;
    private EditText et4;
    private EditText et5;
    private EditText et6;
    private ImageView btnBack;
    private String lastFrom;
    private boolean waitForCode = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_verification);
        init();
        initArgs();
        initToolBar();
        sendVerificationCode();
        setControls();
    }

    private void initToolBar() {
        EcomerceXTools.setSystemBarColor(this, R.color.main_theme_primary);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void init() {
        nextLinearLayout = findViewById(R.id.nextLinearLayout);
        btnBack = findViewById(R.id.btnBack);
        progressDialog = MyUtil.getProgressDialog(this);
        principalEditText = findViewById(R.id.principalEditText);
        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);
        et3 = findViewById(R.id.et3);
        et4 = findViewById(R.id.et4);
        et5 = findViewById(R.id.et5);
        et6 = findViewById(R.id.et6);
        currentPhoneTextView = findViewById(R.id.currentPhoneTextView);
        resendTextView = findViewById(R.id.resendTextView);
    }

    private void initArgs() {
        Bundle data = getIntent().getExtras();
        if (data != null) {
            phoneNumber = data.getString(MainLogInActivity.PHONE_KEY, "");
            currentPhoneTextView.setText(phoneNumber);
            lastFrom = data.containsKey("last_from")? data.getString("last_from") : null;
        }
    }

    private void sendVerificationCode() {
//        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                phoneNumber,        // Phone number to verify
//                60,                 // Timeout duration
//                TimeUnit.SECONDS,   // Unit of timeout
//                TaskExecutors.MAIN_THREAD,               // Activity (for callback binding)
//                mCallbacks);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(app().firebaseAuth).
                setPhoneNumber(preparePhoneNumber(phoneNumber))
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(mCallbacks)
                .setActivity(this)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private String preparePhoneNumber(String phoneNumber) {
        if(!phoneNumber.equals("") && !phoneNumber.equals("false")) {
            char firstDigit = phoneNumber.charAt(0);
            if (firstDigit == '0') {
                phoneNumber = phoneNumber.substring(1);
                phoneNumber = "+213" + phoneNumber;
            }
        }
        return phoneNumber;
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                    waitForCode = false;
                    makeText(PhoneVerificationActivity.this, R.string.code_sent, LENGTH_SHORT).show();
                    if(progressDialog != null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    PhoneAuthCredential credential = phoneAuthCredential;
                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null){
                        char[] codeChars = new char[6];
                        for(int i = 0 ; i < 6 ; i++) {
                            codeChars[i] = code.charAt(i);
                        }
                        principalEditText.setText(code);
                        et1.setText(String.valueOf(codeChars[0]));
                        et2.setText(String.valueOf(codeChars[1]));
                        et3.setText(String.valueOf(codeChars[2]));
                        et4.setText(String.valueOf(codeChars[3]));
                        et5.setText(String.valueOf(codeChars[4]));
                        et6.setText(String.valueOf(codeChars[5]));
                    }else{
                        Bundle data = new Bundle();
                        data.putParcelable(PHONE_CREDENTIAL_KEY,phoneAuthCredential);
                        Intent intent = new Intent();
                        intent.putExtras(data);
                        setResult(RESULT_OK, intent);
                        Toast.makeText(PhoneVerificationActivity.this,
                                getResources().getString(R.string.phone_verification_successful), Toast.LENGTH_SHORT).show();
                        PhoneVerificationActivity.this.finish();
                    }
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    waitForCode = false;
                    if(progressDialog != null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    Toast.makeText(PhoneVerificationActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    waitForCode = false;
                    super.onCodeSent(s, forceResendingToken);
                    if(progressDialog != null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    verificationId = s;
//                    Toast.makeText(PhoneVerificationActivity.this, "code sent to the number", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                    super.onCodeAutoRetrievalTimeOut(s);
                    verificationId = s;
                }
            };

    private void setControls() {
        nextLinearLayout.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        resendTextView.setOnClickListener(this);
        et1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    et2.requestFocus();
                } else {
                    et1.clearFocus();
                }
            }
        });

        et2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    et3.requestFocus();
                } else {
                    et1.requestFocus();
                }
            }
        });

        et3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    et4.requestFocus();
                } else {
                    et2.requestFocus();
                }
            }
        });

        et4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    et5.requestFocus();
                } else {
                    et3.requestFocus();
                }
            }
        });

        et5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    et6.requestFocus();
                } else {
                    et4.requestFocus();
                }
            }
        });

        et6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    et6.requestFocus();
                } else {
                    et5.requestFocus();
                }
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
        switch (v.getId()){
            case R.id.btnBack:
                finish();
                break;
            case R.id.nextLinearLayout:
                if(app().inNetwork()) {
                    verifyPhone();
                }else{
                    makeText(this, getResources().getString(R.string.you_need_internet_connection), LENGTH_SHORT).show();
                }
                break;
            case R.id.resendTextView:
                if(app().inNetwork()) {
                    progressDialog.show();
//                PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                        phoneNumber,        // Phone number to verify
//                        60,                 // Timeout duration
//                        TimeUnit.SECONDS,   // Unit of timeout
//                        TaskExecutors.MAIN_THREAD,               // Activity (for callback binding)
//                        mCallbacks);
                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(app().firebaseAuth).
                            setPhoneNumber(preparePhoneNumber(phoneNumber))
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setCallbacks(mCallbacks)
                            .setActivity(this)
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }else{
                    makeText(this, getResources().getString(R.string.you_need_internet_connection), LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void verifyPhone() {
        String code = principalEditText.getText().toString();
        if(waitForCode || TextUtils.isEmpty(code)) {
            makeText(this, R.string.please_wait_for_code, LENGTH_SHORT).show();
            return;
        }
//        char[] codeChar = new char[6];
//        try {
//            codeChar[0] = et1.getText().charAt(0);
//            codeChar[1] = et2.getText().charAt(0);
//            codeChar[2] = et3.getText().charAt(0);
//            codeChar[3] = et4.getText().charAt(0);
//            codeChar[4] = et5.getText().charAt(0);
//            codeChar[5] = et6.getText().charAt(0);
//        }catch (Exception ignored){
//            Toast.makeText(this, getResources().getString(R.string.enter_valid_code), Toast.LENGTH_SHORT).show();
//            return;
//        }
//        StringBuilder stringBuilder = new StringBuilder();
//        for(int i = 0 ; i < 6 ; i ++){
//            stringBuilder.append(codeChar[i]);
//        }
        PhoneAuthCredential credential;
        try {
            credential = PhoneAuthProvider.getCredential(verificationId, code);
        }catch (Exception e){
            e.printStackTrace();
            makeText(this, getResources().getString(R.string.error_occurred), LENGTH_SHORT).show();
            makeText(this, getResources().getString(R.string.try_again), LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        Bundle data = new Bundle();
        data.putParcelable(PHONE_CREDENTIAL_KEY, credential);
        intent.putExtras(data);
        PhoneVerificationActivity.this.setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onPause() {
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        super.onPause();
    }
}