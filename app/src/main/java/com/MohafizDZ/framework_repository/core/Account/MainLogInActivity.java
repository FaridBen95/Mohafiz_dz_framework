package com.MohafizDZ.framework_repository.core.Account;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.MohafizDZ.empty_project.R;
import com.MohafizDZ.framework_repository.MohafizMainActivity;
import com.MohafizDZ.framework_repository.Utils.MySharedPreferences;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.framework_repository.core.Values;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class MainLogInActivity extends MyAppCompatActivity implements View.OnClickListener {
    public static final String TAG = MainLogInActivity.class.getSimpleName();
    public static final String PHONE_KEY = "phone_key";
    private static final int PERMISSIONS_REQUEST_REQUIRED_PERMS = 13;
    public static final String PHONE_CREDENTIAL_KEY = "phone_credential_key";
    private AuthenticationHelper authenticationHelper;

    private AlertDialog progressDialog;
    private boolean newAccount;
    private LogInWith logInWith;
    private TextView languageTextView;
    private LinearLayout languageLinearLayout;
    private String selectedLanguage;

    private enum LogInType {phone, email}
    private LogInType logInType = LogInType.email;

    private LinearLayout facebookLinearLayout, gmailLinearLayout, phoneLinearLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initConfig();
        initAuthentication();
        prepareView();
    }

    private void initActivity() {
        init();
        initLogInWith();
        setControls();
    }

    private void init() {
        progressDialog = MyUtil.getProgressDialog(this);
        facebookLinearLayout = findViewById(R.id.facebookLinearLayout);
        gmailLinearLayout = findViewById(R.id.gmailLinearLayout);
        phoneLinearLayout = findViewById(R.id.phoneLinearLayout);
        languageLinearLayout = findViewById(R.id.languageLinearLayout);
        languageTextView = findViewById(R.id.languageTextView);
    }

    private void initLogInWith() {
        logInWith = new LogInWith(this) {
            @Override
            public void onSuccess() {
                if(progressDialog != null && !progressDialog.isShowing()){
                    progressDialog.show();
                }
            }

            @Override
            public boolean canRecoverImage() {
                return true;
            }

            @Override
            public void onDataRecovered(final Values userValues) {
                if (newAccount) {
                    authenticationHelper.getmUser().setName(String.valueOf(userValues.get("name")));
                    authenticationHelper.getmUser().setLastName(String.valueOf(userValues.get("last_name")));
                    authenticationHelper.getmUser().setImgPath(String.valueOf(userValues.get("image")));
                    authenticationHelper.execute();
                }
            }

            @Override
            public void onErrorOccurred(String message) {
                Log.d(LogInWith.TAG, message);
                makeText(MainLogInActivity.this, message, Toast.LENGTH_LONG).show();
                makeText(MainLogInActivity.this, getResources().getString(R.string.try_again), LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticated(Boolean waitForDataRecovery) {
                MainLogInActivity.this.newAccount = waitForDataRecovery;
                if (!waitForDataRecovery) {
                    authenticationHelper.execute();
                }
            }
        };
    }

    private void prepareView() {
        try {
            MyUtil.Language language = MyUtil.getCurrentLanguage(this);
            String languageStr = language == MyUtil.Language.english ? "English" :
                    (language == MyUtil.Language.french ? "Français" : "العربية");
            languageTextView.setText(languageStr);
        }catch (Exception ignored){}
    }

    private void setControls() {
        facebookLinearLayout.setOnClickListener(this);
        gmailLinearLayout.setOnClickListener(this);
        phoneLinearLayout.setOnClickListener(this);
        findViewById(R.id.parentView).setOnClickListener(this);
        languageLinearLayout.setOnClickListener(this);
    }

    private void initConfig() {
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        int WRITE_EXTERNAL_STORAGE_Check = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int CALL_PHONE_Check = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE);
        if(CALL_PHONE_Check != PackageManager.PERMISSION_GRANTED ||
                WRITE_EXTERNAL_STORAGE_Check != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.CALL_PHONE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_REQUIRED_PERMS );
        }
//        validateTime();
    }/*

    private void validateTime() {
        DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double offset = snapshot.getValue(Double.class);
                double estimatedServerTimeMs = System.currentTimeMillis() + offset;
                MyUtil.milliSecToDate((long) (System.currentTimeMillis() + offset));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });
        if(app().inNetwork()){
        }
    }*/

//    @ServerTimestamp
//    private Date TIME_STAMP;
    private void initAuthentication() {
        authenticationHelper = new AuthenticationHelper(this) {
            @Override
            protected void onAccountManagerAuthenticated(boolean newAccount, MUser mUser,
                                                         boolean skipped) {
                if(getFirebaseAuth().getCurrentUser() != null) {
                    getFirebaseAuth().getCurrentUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        @Override
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            try {
                                if(progressDialog != null && progressDialog.isShowing()){
                                    progressDialog.dismiss();
                                }
                                MySharedPreferences mySharedPreferences = new MySharedPreferences(MainLogInActivity.this);
                                String lastToken = mySharedPreferences.getString(MUser.AUTH_TOKEN_KEY, "");
                                String currentToken = task.getResult().getToken();
                                if(!lastToken.equals(currentToken)) {
                                    mySharedPreferences.putString(MUser.AUTH_TOKEN_KEY, currentToken);
                                    mySharedPreferences.putString(MUser.AUTH_TOKEN_WRITE_DATE_KEY, MyUtil.getCurrentDate());
                                }
                            } catch (Exception ignored) {
                            }
                            requestSyncAdapters();
                            startMainActivity();
                        }
                    });
                }
                app().onAuthentified(getFirebaseAuth().getCurrentUser());
            }

            @Override
            protected void onFirebaseAuthComplete(Task<AuthResult> authResult) {
            }

            @Override
            protected void onResetEmailSend(Task<Void> task) {
                if(task.isSuccessful()){
                    makeText(MainLogInActivity.this, getResources().getString(R.string.reset_code_sent), LENGTH_SHORT).show();
                }
            }

            @Override
            protected void noUserIsConnected() {
                setContentView(R.layout.main_login);
                initActivity();
            }
        };
        authenticationHelper.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_REQUIRED_PERMS) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.accept_permission), Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
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
            case R.id.gmailLinearLayout:
                if(app().inNetwork()) {
                    logInWith.setLoginType(LogInWith.LoginType.gmail);
                    logInWith.signIn();
                }else{
                    makeText(this, getResources().getString(R.string.you_need_internet_connection), LENGTH_SHORT).show();
                }
                break;
            case R.id.facebookLinearLayout:
                if(app().inNetwork()) {
                    logInWith.setLoginType(LogInWith.LoginType.facebook);
                    logInWith.signIn();
                }else{
                    makeText(this, getResources().getString(R.string.you_need_internet_connection), LENGTH_SHORT).show();
                }
                break;
            case R.id.phoneLinearLayout:
                if(app().inNetwork()) {
                    Intent intent = new Intent(this, PhoneLogInActivity.class);
                    startActivityForResult(intent, LogInWith.PHONE_VERIFICATION_KEY);
                }else{
                    makeText(this, getResources().getString(R.string.you_need_internet_connection), LENGTH_SHORT).show();
                }
                break;
            case R.id.languageLinearLayout:
            	languagesDialog();
                break;
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
            new MySharedPreferences(MainLogInActivity.this).putString(MohafizMainActivity.LANGUAGE_KEY, language1);
            app().refreshLanguage(MainLogInActivity.this, language1);
            prepareView();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LogInWith.PHONE_VERIFICATION_KEY){
            if(resultCode == Activity.RESULT_OK){
                if (logInWith != null) {
                    logInWith.onActivityResult(requestCode, resultCode, data);
                    finish();
                }
            }
        }
        else {
            if (logInWith != null) {
                logInWith.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    protected void onPause() {
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        super.onPause();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MohafizMainActivity.class);
        startActivity(intent);
        finish();
    }

    private void requestSyncAdapters() {
    }
}
