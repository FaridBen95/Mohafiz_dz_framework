package com.MohafizDZ.framework_repository.core.Account;

import android.content.res.Resources;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.MohafizDZ.empty_project.R;
import com.MohafizDZ.framework_repository.MohafizMainActivity;
import com.MohafizDZ.framework_repository.Utils.MySharedPreferences;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginWithEmailActivity extends MyAppCompatActivity implements View.OnClickListener {
    public static final String TAG = LoginWithEmailActivity.class.getSimpleName();

    private LinearLayout nextLinearLayout;
    private ImageView btnBack;
    private TextView languageTextView;
    private LinearLayout languageLinearLayout;
    private String selectedLanguage;
    private TextView loginTextView;
    private TextView detailsTextView;
    private TextView mainTitleTextView;
    private EditText confirmPasswordEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private View confirmPasswordTextView;
    private View divider3;
    private View confirmPasswordContainer;
    private boolean isLogIn = true;
    private FirebaseAuth firebaseAuth;

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
        MyUtil.Language language = MyUtil.getCurrentLanguage(this);
        String languageStr = language == MyUtil.Language.english? "English" :
                (language == MyUtil.Language.french? "Français" : "العربية");
        languageTextView.setText(languageStr);
        setDetailsText(getString(R.string.login_details_txt));
    }

    private void setDetailsText(String text){
        String signUpTxt = text.replace("@", " ");
        Spannable spannable = new SpannableString(signUpTxt);
        int index = text.indexOf("@");
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.blue_900_)), index, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        detailsTextView.setText(spannable, TextView.BufferType.SPANNABLE);
    }

    private void initData() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void init() {
        nextLinearLayout = findViewById(R.id.nextLinearLayout);
        btnBack = findViewById(R.id.btnBack);
        languageLinearLayout = findViewById(R.id.languageLinearLayout);
        languageTextView = findViewById(R.id.languageTextView);
        loginTextView = findViewById(R.id.loginTextView);
        detailsTextView = findViewById(R.id.detailsTextView);
        mainTitleTextView = findViewById(R.id.mainTitleTextView);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        emailEditText = findViewById(R.id.emailEditText);
        confirmPasswordTextView = findViewById(R.id.confirmPasswordTextView);
        divider3 = findViewById(R.id.divider3);
        confirmPasswordContainer = findViewById(R.id.confirmPasswordContainer);
    }

    private void setControls() {
        nextLinearLayout.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        languageLinearLayout.setOnClickListener(this);
        detailsTextView.setOnClickListener(this);
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
        if (id == R.id.nextLinearLayout) {
            authenticate();
        } else if (id == R.id.btnBack) {
            onBackPressed();
        } else if (id == R.id.languageLinearLayout) {
            languagesDialog();
        } else if (id == R.id.detailsTextView) {
            switchLogin();
        }
    }

    private void switchLogin() {
        isLogIn = !isLogIn;
        if(isLogIn){
            confirmPasswordTextView.setVisibility(View.GONE);
            divider3.setVisibility(View.GONE);
            confirmPasswordContainer.setVisibility(View.GONE);
            mainTitleTextView.setText(getString(R.string.log_in));
            loginTextView.setText(getString(R.string.log_in));
            setDetailsText(getString(R.string.login_details_txt));
        }else{
            confirmPasswordTextView.setVisibility(View.VISIBLE);
            divider3.setVisibility(View.VISIBLE);
            confirmPasswordContainer.setVisibility(View.VISIBLE);
            mainTitleTextView.setText(getString(R.string.sign_up));
            loginTextView.setText(getString(R.string.sign_up));
            setDetailsText(getString(R.string.signup_details_txt));
        }
    }

    private void languagesDialog() {
        MyUtil.Language language = MyUtil.getCurrentLanguage(this);
        selectedLanguage = language == MyUtil.Language.english? "English" :
                (language == MyUtil.Language.french? "Français" : "العربية");
        String[] availableLocales = Resources.getSystem().getAssets().getLocales();
        List<String> supportedLocales = Arrays.asList(availableLocales);
        final Map<String, String> locales = new HashMap<>();
        List<String> supportedLanguages = new ArrayList<>();
        if(supportedLocales.contains("en")){
            locales.put("English", "en");
            supportedLanguages.add("English");
        }
        if(supportedLocales.contains("fr")){
            locales.put("Français", "fr");
            supportedLanguages.add("Français");
        }
        if(supportedLocales.contains("ar")){
            locales.put("العربية", "ar");
            supportedLanguages.add("العربية");
        }
        final String[] languages = supportedLanguages.toArray(new String[0]);
        int checkedItem = Arrays.asList(languages).indexOf(selectedLanguage);
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.language));
        builder.setSingleChoiceItems(languages, checkedItem, (dialogInterface, i) -> selectedLanguage = languages[i]);
        builder.setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> {
            String language1 = locales.get(selectedLanguage);
            new MySharedPreferences(LoginWithEmailActivity.this).putString(MohafizMainActivity.LANGUAGE_KEY, language1);
            app().refreshLanguage(LoginWithEmailActivity.this, language1);
            initView();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void authenticate() {
        String email = MyUtil.getAllowedText(emailEditText.getText().toString());
        String password = passwordEditText.getText().toString();
        String password2 = confirmPasswordEditText.getText().toString();
        email = email.trim();
        if(TextUtils.isEmpty(email)){
            emailEditText.setError(getString(R.string.email_required));
            return;
        }
        if(TextUtils.isEmpty(password)){
            passwordEditText.setError(getString(R.string.email_required));
            return;
        }
        if(password.length() < 6){
            passwordEditText.setError(getString(R.string.password_short_txt));
            return;
        }
        if(!isLogIn && !password2.equals(password)){
            confirmPasswordEditText.setError(getString(R.string.passwords_differents_txt));
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
                setResult(RESULT_OK);
                finish();
            }else{
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signUp(String email, String password){
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                setResult(RESULT_OK);
                finish();
            }else{
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
