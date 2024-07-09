package com.MohafizDZ.framework_repository.core.Account;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.MohafizDZ.App;
import com.MohafizDZ.empty_project.R;
import com.MohafizDZ.framework_repository.MohafizMainActivity;
import com.MohafizDZ.framework_repository.Utils.MySharedPreferences;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Account.login_helper_dir.ILogIn;
import com.MohafizDZ.framework_repository.core.Account.login_helper_dir.LogInHelper;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.framework_repository.core.Values;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class LauncherActivity extends MyAppCompatActivity implements View.OnClickListener, ILogIn.View {
    public static final String TAG = LauncherActivity.class.getSimpleName();
    public static final String LOG_IN_TYPE_KEY = "log_in_type_key";
    public static final String GOOGLE_AUTH_TOKEN_KEY = "google_auth_token_key";
    public static final String PHONE_KEY = "phone_key";
    public static final String LANGUAGE_KEY = "language_key";
    private static final int PERMISSIONS_REQUEST_REQUIRED_PERMS = 13;
    public static final String PHONE_CREDENTIAL_KEY = "phone_credential_key";
    public static final String FIRST_RUN_KEY = "first_run";
    private AuthenticationHelper authenticationHelper;

    private AlertDialog progressDialog;
    private boolean newAccount;
    private LogInWith2 logInWith2;
    private TextView languageTextView;
    private View languageContainer;
    private String selectedLanguage;
    private TextView termsOfUseTextView;
    private View logoView;
    private View circleImageView;
    private View intoAppContainer;
    private boolean loggedIn = true;
    private MohafizMainActivity.IntentType intentType = null;
    private View googleAnimationView;
    private boolean canShowTuto = true;
    private ActivityResultLauncher<Intent> logInActivityResultLauncher;

    @Override
    public void toggleLoading(boolean isRefreshing) {
        if(isRefreshing) {
            if (progressDialog == null) {
                progressDialog = MyUtil.getProgressDialog(this);
                progressDialog.setCancelable(false);
            }
            if(!progressDialog.isShowing()) {
                progressDialog.show();
            }
        }else{
            progressDialog.dismiss();
        }
    }

    @Override
    public void onDataRecovered(Values userValues) {
        authenticationHelper.getmUser().setName(String.valueOf(userValues.get("name")));
        authenticationHelper.getmUser().setLastName(String.valueOf(userValues.get("last_name")));
        authenticationHelper.getmUser().setImgPath(String.valueOf(userValues.get("image")));
    }

    @Override
    public void onAuthSuccess() {
        authenticationHelper.execute();
    }

    @Override
    public void onGoogleAuthSuccess(String token) {
        logInPresenter.FirebaseGoogleAuth(token);
    }

    private enum LogInType {phone, email}
    private LogInType logInType = LogInType.email;

    private MaterialButton logInButton;
    private ILogIn.Presenter logInPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
//        setContentView(R.layout.activity_start);
        setContentView(R.layout.main_login);
        initArgs();
        initConfig();
        initAuthentication();
        initActivity();
        prepareView();
//        prepareAnimationView();
    }

    private void initArgs() {
        final Intent intent = getIntent();
        Bundle extras = intent != null? intent.getExtras() : new Bundle();
        canShowTuto = extras == null || extras.getBoolean(FIRST_RUN_KEY, true);
    }

    private void prepareAnimationView() {
        circleImageView = findViewById(R.id.circleImageView);

        // Create a fade-in animation with a duration of 750 milliseconds
        Animation fadeInAnimation = new AlphaAnimation(0, 1);
        fadeInAnimation.setDuration(750);


        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(circleImageView, "scaleX", 0f, 1f);
        scaleXAnimator.setDuration(750);

        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(circleImageView, "scaleY", 0f, 1f);
        scaleYAnimator.setDuration(750);

        new Handler().postDelayed(() -> {
            circleImageView.setVisibility(View.VISIBLE
            );
            scaleXAnimator.start();
            scaleYAnimator.start();
        }, 750);
    }

    private boolean canShowCraftMen() {
        return false;
    }

    private void animate(View myView) {
        // Get the screen dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // Calculate the translation values
// Calculate the translation values
        float centerX = screenWidth / 2f;
        float centerY = screenHeight / 2f;
        float currentX = myView.getX() + myView.getWidth() / 2f;
        float currentY = myView.getY() + myView.getHeight() / 2f;
        float translateX = currentX - centerX;
        float translateY = currentY - centerY;
        // Create the translation animations
        ObjectAnimator translateXAnimator = ObjectAnimator.ofFloat(myView, "translationX", -translateX, 0f);
        translateXAnimator.setDuration(750);
        ObjectAnimator translateYAnimator = ObjectAnimator.ofFloat(myView, "translationY", -translateY, 0f);
        translateYAnimator.setDuration(750);

        // Start the animations
        translateXAnimator.start();
        translateYAnimator.start();
        myView.setVisibility(View.VISIBLE);
    }

    private void initActivity() {
        init();
        initLogInWith();
        setControls();
        if(canShowTuto) {
            new Handler().postDelayed(() -> logoView.performClick(), 500);
        }
    }

    private void init() {
        findViewById();
        initResultLaunchers();
    }

    private void initResultLaunchers(){
        logInActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_OK){
                        Bundle data = Objects.requireNonNull(result.getData()).getExtras();
                        if(data != null) {
                            ILogIn.LogInType type = ILogIn.LogInType.valueOf(data.getString(LOG_IN_TYPE_KEY, ILogIn.LogInType.anonymous.name()));
                            if(type == ILogIn.LogInType.email) {
                                onAuthSuccess();
                            }else{
                                String token = data.getString(GOOGLE_AUTH_TOKEN_KEY);
                                logInPresenter.FirebaseGoogleAuth(token);
                            }
                        }
                    }
                });
    }

    private void findViewById() {
        progressDialog = MyUtil.getProgressDialog(this);
        logInButton = findViewById(R.id.logInButton);
        languageContainer = findViewById(R.id.languageContainer);
        termsOfUseTextView = findViewById(R.id.termsOfUseTextView);
        logoView = findViewById(R.id.logo_up);
        circleImageView = findViewById(R.id.circleImageView);
    }

    private void initLogInWith() {
//        logInWith2 = new LogInWith2(this) {
//            @Override
//            public void onSuccess() {
//                if(progressDialog != null && !progressDialog.isShowing()){
//                    progressDialog.show();
//                }
//            }
//
//            @Override
//            public boolean canRecoverImage() {
//                return true;
//            }
//
//            @Override
//            public void onDataRecovered(final Values userValues) {
//                if (newAccount) {
//                    authenticationHelper.getmUser().setName(String.valueOf(userValues.get("name")));
//                    authenticationHelper.getmUser().setLastName(String.valueOf(userValues.get("last_name")));
//                    authenticationHelper.getmUser().setImgPath(String.valueOf(userValues.get("image")));
//                    authenticationHelper.execute();
//                }
//            }
//
//            @Override
//            public void onErrorOccurred(String message) {
//                Log.d(LogInWith2.TAG, message);
//                makeText(LauncherActivity.this, message, Toast.LENGTH_LONG).show();
//                makeText(LauncherActivity.this, getResources().getString(R.string.try_again), LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onAuthenticated(Boolean waitForDataRecovery) {
//                LauncherActivity.this.newAccount = waitForDataRecovery;
//                if (!waitForDataRecovery) {
//                    authenticationHelper.execute();
//                }
//            }
//        };
        logInPresenter = new LogInHelper(this, this);
        logInPresenter.init();
    }

    private void prepareView() {
        try {
            languageTextView = findViewById(R.id.languageTextView);
            MyUtil.Language language = MyUtil.getCurrentLanguage(this);
            String languageStr = language == MyUtil.Language.english ? getString(R.string.english_label) :
                    (language == MyUtil.Language.french ? getString(R.string.french_label) : getString(R.string.arabic_label));
            languageTextView.setText(languageStr);
            String termsOfUseText = getResources().getString(R.string.terms_of_use_text) ;
            String termsOfUseToShow = termsOfUseText.replace("@", " ");
            Spannable spannable = new SpannableString(termsOfUseToShow);
            int index = termsOfUseText.indexOf("@");
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, android.R.color.holo_blue_light)), index, termsOfUseText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            termsOfUseTextView.setText(spannable, TextView.BufferType.SPANNABLE);
        }catch (Exception ignored){}
        googleAnimationView = findViewById(R.id.googleAnimationView);
        {
            float startScaleX = 0.7f;  // Starting scale value
            float endScaleX = 1.0f;    // Ending scale value

            float startScaleY = 0.f;  // Starting scale value
            float endScaleY = 1.0f;    // Ending scale value
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(googleAnimationView, "scaleX", startScaleX, endScaleX);
            scaleXAnimator.setDuration(800);
            scaleXAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            scaleXAnimator.setRepeatMode(ObjectAnimator.REVERSE);

            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(googleAnimationView, "scaleY", startScaleY, endScaleY);
            scaleYAnimator.setDuration(800);
            scaleYAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            scaleYAnimator.setRepeatMode(ObjectAnimator.REVERSE);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleXAnimator, scaleYAnimator);
            animatorSet.start();
        }
    }

    private void setControls() {
        logInButton.setOnClickListener(this);
        findViewById(R.id.parentView).setOnClickListener(this);
        languageContainer.setOnClickListener(this);
        termsOfUseTextView.setOnClickListener(this);
        logoView.setOnClickListener(this);
    }

    private void initConfig() {
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
//        int WRITE_EXTERNAL_STORAGE_Check = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        int CALL_PHONE_Check = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE);
//        if(CALL_PHONE_Check != PackageManager.PERMISSION_GRANTED ||
//                WRITE_EXTERNAL_STORAGE_Check != PackageManager.PERMISSION_GRANTED ) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{
//                            android.Manifest.permission.CALL_PHONE,
//                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    PERMISSIONS_REQUEST_REQUIRED_PERMS );
//        }
//        validateTime();

    }

    /*

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
        intoAppContainer = findViewById(R.id.intoAppContainer);
        authenticationHelper = new AuthenticationHelper(this) {
            @Override
            protected void onStartAuthentication(boolean anonymous) {
                if(!anonymous) {
                    intoAppContainer.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            protected void onAccountManagerAuthenticated(boolean newAccount, MUser mUser,
                                                         boolean skipped) {
                final FirebaseUser currentUser = getFirebaseAuth().getCurrentUser();
                if(currentUser != null) {
                    currentUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        @Override
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            try {
                                loggedIn = true;
                                if(progressDialog != null && progressDialog.isShowing()){
                                    progressDialog.dismiss();
                                }
                                MySharedPreferences mySharedPreferences = new MySharedPreferences(LauncherActivity.this);
                                String lastToken = mySharedPreferences.getString(MUser.AUTH_TOKEN_KEY, "");
                                String currentToken = task.getResult().getToken();
                                if(!lastToken.equals(currentToken)) {
                                    mySharedPreferences.putString(MUser.AUTH_TOKEN_KEY, currentToken);
                                    mySharedPreferences.putString(MUser.AUTH_TOKEN_WRITE_DATE_KEY, MyUtil.getCurrentDate());
                                }
                            } catch (Exception ignored) {
                            }
                            requestSyncAdapters();
//                            if(!mFirstRun && !currentUser.isAnonymous()) {
//                                setResult(RESULT_OK);
//                                finish();
//                            }else {
//                            }
                            if(!canShowTuto && !currentUser.isAnonymous()){
                                canShowTuto = true;
                                logoView.performClick();
                            }else {
                                if (intentType != null) {
                                    startMainActivity(intentType);
                                }
                            }
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
                    makeText(LauncherActivity.this, getResources().getString(R.string.reset_code_sent), LENGTH_SHORT).show();
                }
            }

            @Override
            protected void noUserIsConnected() {
                loggedIn = false;
//                setContentView(R.layout.main_login);
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
                    makeText(this, getString(R.string.accept_permission), Toast.LENGTH_LONG).show();
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
        int id = v.getId();
        if (id == R.id.logInButton) {
            if (app().inNetwork()) {
                openEmailPasswordActivity();
            } else {
                makeText(this, getResources().getString(R.string.you_need_internet_connection), LENGTH_SHORT).show();
            }
        } else if (id == R.id.languageContainer) {
            languagesDialog();
        } else if (id == R.id.termsOfUseTextView) {
            showTermsOfUseDialog();
        } else if (id == R.id.logo_up) {
            if(canShowTuto) {
                prepareAnimationView();
            }
        }
    }

    private void openEmailPasswordActivity() {
        Intent intent = new Intent(this, MainLogInActivity.class);
        logInActivityResultLauncher.launch(intent);
    }

    private void onClickOnPhone() {
        FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
        mFunctions.getHttpsCallable("isPhoneAuthAvailable")
                .call()
                .continueWith(task -> {
                    String result = (String) task.getResult().getData();
                    if(task.isSuccessful() && result.equals("true")){
                        Intent intent = new Intent(this, PhoneLogInActivity.class);
                        startActivityForResult(intent, LogInWith2.PHONE_VERIFICATION_KEY);
                    }else{
//                        showPhoneAuthUnavailableDialog();
                    }
                    return result;
                });
    }

//    private void showPhoneAuthUnavailableDialog() {
//        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
//        dialog.setTitleText(getString(R.string.phone_auth_unavailable));
//        dialog.setContentText(getString(R.string.phone_auth_unavailable_msg));
//        dialog.setConfirmText(getString(R.string.dialog_ok));
//        dialog.setConfirmClickListener(Dialog::dismiss);
//        dialog.show();
//
//    }

    private void showTermsOfUseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String termsOfUseMessage = getTermsOfUseMessage();
        builder.setMessage(Html.fromHtml(termsOfUseMessage));
        builder.setPositiveButton(getResources().getString(R.string.dialog_ok), null);
        builder.show();
    }

    private String getTermsOfUseMessage() {
        String text = "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getAssets().open(getResources().getString(R.string.terms_of_use_file_name)), "UTF-8"))) {

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                text = text.concat(mLine);
            }
        } catch (IOException e) {
            //log the exception
        }
        //log the exception
        return text;
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
            new MySharedPreferences(LauncherActivity.this).putString(LANGUAGE_KEY, language1);
            app().refreshLanguage(LauncherActivity.this, language1);
            prepareView();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LogInWith2.PHONE_VERIFICATION_KEY){
            if(resultCode == Activity.RESULT_OK){
                if (logInWith2 != null) {
                    logInWith2.onActivityResult(requestCode, resultCode, data);
                    finish();
                }
            }
        }else if(requestCode == LogInWith2.EMAIL_AUTH_KEY){
            if(logInWith2 != null){
                logInWith2.onActivityResult(requestCode, resultCode, null);
            }
        }else {
            if (logInWith2 != null) {
                logInWith2.onActivityResult(requestCode, resultCode, data);
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

    private void startMainActivity(MohafizMainActivity.IntentType intentType) {
        if(!loggedIn) {
            this.intentType = intentType;
            logInPresenter.loginAsGuest();
        }else {
            if(intentType != MohafizMainActivity.IntentType.fromApp){
                Intent intent = MohafizMainActivity.getIntent(this, MohafizMainActivity.IntentType.home);
                startActivity(intent);
            }
            if (intentType != MohafizMainActivity.IntentType.home) {
                Intent intent = MohafizMainActivity.getIntent(this, intentType);
                startActivity(intent);
            }
            finish();
        }
    }

    private void requestSyncAdapters() {
    }
}
