package com.MohafizDZ.framework_repository.core.Account;

import static com.MohafizDZ.framework_repository.core.Account.LauncherActivity.GOOGLE_AUTH_TOKEN_KEY;
import static com.MohafizDZ.framework_repository.core.Account.LauncherActivity.LOG_IN_TYPE_KEY;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Account.login_helper_dir.ILogIn;
import com.MohafizDZ.framework_repository.core.Account.login_helper_dir.LogInHelper;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.framework_repository.core.Values;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.Objects;

public class MainLogInActivity extends MyAppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener, ILogIn.View {
    public static final String TAG = MainLogInActivity.class.getSimpleName();

    private TextView detailsTextView, descriptionTextView;
    private View confirmPasswordContainer, additionalLogInContainer, welcomeTextView;
    private TextInputEditText confirmPasswordTextInput, passwordTextInput, emailTextInput;
    private MaterialButton googleLoginButton, logInButton;
    private boolean isLogIn = true;
    private FirebaseAuth firebaseAuth;
    private View toFocusView;
    private ILogIn.Presenter logInPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_email);
        init();
        initData();
        setControls();
        initView();
    }

    private void initView() {

        setDetailsText(getString(R.string.login_details_txt));
    }

    private void setDetailsText(String text){
        String signUpTxt = text.replace("@", " ");
        Spannable spannable = new SpannableString(signUpTxt);
        int index = text.indexOf("@");
        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, android.R.color.holo_blue_light)), index, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        detailsTextView.setText(spannable, TextView.BufferType.SPANNABLE);
    }

    private void initData() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void init() {
        findViewById();
        logInPresenter = new LogInHelper(this, this);
        logInPresenter.init();
    }

    private void findViewById() {
        confirmPasswordContainer = findViewById(R.id.confirmPasswordContainer);
        confirmPasswordTextInput = findViewById(R.id.confirmPasswordTextInput);
        googleLoginButton = findViewById(R.id.googleLoginButton);
        detailsTextView = findViewById(R.id.detailsTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        logInButton = findViewById(R.id.logInButton);
        passwordTextInput = findViewById(R.id.passwordTextInput);
        emailTextInput = findViewById(R.id.emailTextInput);
        additionalLogInContainer = findViewById(R.id.additionalLogInContainer);
        welcomeTextView = findViewById(R.id.welcomeTextView);
    }

    private void setControls() {
        detailsTextView.setOnClickListener(this);
        logInButton.setOnClickListener(this);
        googleLoginButton.setOnClickListener(this);
        emailTextInput.setOnFocusChangeListener(this);
        passwordTextInput.setOnFocusChangeListener(this);
        confirmPasswordTextInput.setOnFocusChangeListener(this);
        KeyboardVisibilityEvent.setEventListener(
                MainLogInActivity.this,
                (KeyboardVisibilityEventListener) isOpen -> {
                    if(isOpen){
                        welcomeTextView.setVisibility(View.GONE);
                        additionalLogInContainer.setVisibility(View.GONE);
                        descriptionTextView.setVisibility(View.GONE);
                        new Handler().postDelayed(() -> toFocusView.requestFocus(), 300);
                    }else{
                        welcomeTextView.setVisibility(View.VISIBLE);
                        additionalLogInContainer.setVisibility(View.VISIBLE);
                        descriptionTextView.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void setTitleBar(ActionBar actionBar) {
        setToolbarTitle(null);
        actionBar.setTitle(null);
    }

    @Override
    public Toolbar setToolBar() {
        return findViewById(R.id.toolbar);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        MyUtil.preventDoubleClick(v);
        if (id == R.id.logInButton) {
            authenticate();
        }else if (id == R.id.googleLoginButton) {
            logInPresenter.googleLogIn();
        }else if (id == R.id.detailsTextView) {
            switchLogin();
        }
    }

    private void switchLogin() {
        isLogIn = !isLogIn;
        if(isLogIn){
            confirmPasswordContainer.setVisibility(View.GONE);
            logInButton.setText(getString(R.string.log_in));
            descriptionTextView.setText(getString(R.string.welcome_log_in_msg));
            setDetailsText(getString(R.string.login_details_txt));
        }else{
            confirmPasswordContainer.setVisibility(View.VISIBLE);
            logInButton.setText(getString(R.string.sign_up));
            descriptionTextView.setText(getString(R.string.welcome_sign_up_msg));
            setDetailsText(getString(R.string.signup_details_txt));
        }
    }

    private void authenticate() {
        String email = MyUtil.getAllowedText(Objects.requireNonNull(emailTextInput.getText()).toString());
        String password = Objects.requireNonNull(passwordTextInput.getText()).toString();
        String password2 = Objects.requireNonNull(confirmPasswordTextInput.getText()).toString();
        email = email.trim();
        if(TextUtils.isEmpty(email)){
            emailTextInput.setError(getString(R.string.email_required));
            return;
        }
        if(TextUtils.isEmpty(password)){
            passwordTextInput.setError(getString(R.string.password_required));
            return;
        }
        if(password.length() < 6){
            passwordTextInput.setError(getString(R.string.password_short_txt));
            return;
        }
        if(!isLogIn && !password2.equals(password)){
            confirmPasswordTextInput.setError(getString(R.string.passwords_differents_txt));
            return;
        }
        if(isLogIn){
            login(email, password);
        }else{
            signUp(email, password);
        }
    }

    private void login(String email, String password){
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Bundle data = new Bundle();
                data.putString(LOG_IN_TYPE_KEY, ILogIn.LogInType.email.name());
                Intent intent = new Intent();
                intent.putExtras(data);
                setResult(RESULT_OK, intent);
                finish();
            }else{
                Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signUp(String email, String password){
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Bundle data = new Bundle();
                data.putString(LOG_IN_TYPE_KEY, ILogIn.LogInType.email.name());
                Intent intent = new Intent();
                intent.putExtras(data);
                setResult(RESULT_OK, intent);
                finish();
            }else{
                Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if(b){
            toFocusView = view;
        }
    }

    @Override
    public void toggleLoading(boolean isRefreshing) {
        //this won't be called in this activity
    }

    @Override
    public void onDataRecovered(Values userValues) {
        //this won't be called in this activity
    }

    @Override
    public void onAuthSuccess() {
        //this won't be called in this activity
    }

    @Override
    public void onGoogleAuthSuccess(String token) {
        Bundle data = new Bundle();
        data.putString(LOG_IN_TYPE_KEY, ILogIn.LogInType.google.name());
        data.putString(GOOGLE_AUTH_TOKEN_KEY, token);
        Intent intent = new Intent();
        intent.putExtras(data);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
