package com.MohafizDZ.framework_repository.core.Account;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.MohafizDZ.empty_project.R;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Values;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;

import org.json.JSONObject;

import java.io.File;

public abstract class LogInWith {
    public static final String TAG = LogInWith.class.getSimpleName();

    public static final int GMAIL_SIGN_IN_KEY = 1 ;
    public static final int FACEBOOK_REQUEST_CODE = 64206;
    public static final String PHONE_CREDENTIAL_KEY = "phone_credential_key";
    public static final int PHONE_VERIFICATION_KEY = 317;

    private FragmentActivity activity;
    private Context mContext;
    private FirebaseAuth firebaseAuth;
    private CallbackManager mCallbackManager;
    private LoginButton facebookLoginButton;
    private LoginResult facebookLogInResult;

    public enum LoginType {gmail, facebook, phone}
    private LoginType loginType;
    private GoogleSignInClient googleSignInClient;
    private FirebaseUser firebaseUser;
    private JSONObject object;
    private boolean relink;

    public void setLoginType(LoginType loginType) {
        setLoginType(loginType, false);
    }

    public void setLoginType(LoginType loginType, boolean relink) {
        this.loginType = loginType;
        this.relink = relink;
    }

    public LogInWith(FragmentActivity activity){
        this.activity = activity;
        this.mContext = activity;
        init();
    }

    private void init(){
        firebaseAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(mContext.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(mContext, gso);
        mCallbackManager = CallbackManager.Factory.create();
        facebookLoginButton = new LoginButton(mContext);
        facebookLoginButton.setPermissions("email", "public_profile");
        facebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            public void onSuccess(final LoginResult loginResult) {
                facebookLogInResult = loginResult;
                LogInWith.this.onSuccess();
                FirebaseFacebookAuth(AccessToken.getCurrentAccessToken());
            }
            @Override
            public void onCancel() {
            }
            @Override
            public void onError(FacebookException error) {
            }
        });
    }

    public void signIn(){
        if(loginType == LoginType.gmail){
            Intent signInIntent = googleSignInClient.getSignInIntent();
            activity.startActivityForResult(signInIntent, GMAIL_SIGN_IN_KEY);
        }
        if(loginType == LoginType.facebook){
            if (!isLoggedInToFacebook()) {
                facebookLoginButton.setEnabled(true);
                facebookLoginButton.performClick();
            }else{
                FirebaseFacebookAuth(AccessToken.getCurrentAccessToken());
            }
        }
    }

    private boolean isLoggedInToFacebook() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GMAIL_SIGN_IN_KEY){
            Task<GoogleSignInAccount> googleSignInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            checkGoogleAuth(googleSignInAccountTask);
        }
        if(requestCode == FACEBOOK_REQUEST_CODE){
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
        if(requestCode == PHONE_VERIFICATION_KEY){
            if(resultCode == Activity.RESULT_OK) {
                linkWithCredential(data);
            }else{
                onErrorOccurred(mContext.getString(R.string.error_confirming_phone_code));
            }
        }
    }

    private void linkWithCredential(Intent data) {
        PhoneAuthCredential credential = data.getParcelableExtra(PHONE_CREDENTIAL_KEY);
        if(credential != null) {
            if (firebaseAuth.getCurrentUser() == null) {
                firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    onAuthenticated(false);
                                } else {
                                    onErrorOccurred(task.getException().getMessage());
                                }
                            }
                        });
            } else {
                firebaseAuth.getCurrentUser().linkWithCredential(credential)
                        .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    onAuthenticated(false);
                                } else {
                                    onErrorOccurred(task.getException().getMessage());
                                }
                            }
                        });
            }
        }else{
            onErrorOccurred(mContext.getString(R.string.try_again));
        }
    }

    private void checkGoogleAuth(Task<GoogleSignInAccount> completedTask){
        try{
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            FirebaseGoogleAuth(account.getIdToken());
        }
        catch (ApiException e){
            onErrorOccurred(e.getMessage());
        }
    }

    private void recoverData() {
        try {
            if (loginType == LoginType.gmail) {
                final GoogleSignInAccount accountInfo = GoogleSignIn.getLastSignedInAccount(activity);
                final Values userValues = new Values();
                userValues.put("name", accountInfo.getGivenName());
                userValues.put("last_name", accountInfo.getFamilyName());
                userValues.put("email", accountInfo.getEmail());
                userValues.put("user_id", firebaseUser.getUid());
                if (canRecoverImage()) {
                    if (accountInfo.getPhotoUrl() != null) {
                        MyUtil.downloadPicture(mContext, accountInfo.getPhotoUrl().toString(),
                                new MyUtil.DownloadListener() {
                                    @Override
                                    public void onDownloadFinished(File imageFile) {
                                        if (imageFile != null) {
                                            userValues.putImage("image", imageFile);
                                            onDataRecovered(userValues);
                                        }
                                    }
                                });
                    } else {
                        onDataRecovered(userValues);
                    }
                } else {
                    onDataRecovered(userValues);
                }
            }
            if(loginType == LoginType.facebook){
                GraphRequest request = GraphRequest.newMeRequest(
                        facebookLogInResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    final Values userValues = new Values();
                                    userValues.put("name", object.getString("first_name"));
                                    userValues.put("last_name", object.getString("last_name"));
                                    userValues.put("email", object.getString("email"));
                                    if (canRecoverImage() && object.has("picture")) {
                                        String profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                        MyUtil.downloadPicture(mContext, profilePicUrl, new MyUtil.DownloadListener() {
                                            @Override
                                            public void onDownloadFinished(File imageFile) {
                                                if(imageFile != null) {
                                                    userValues.putImage("image", imageFile);
                                                    onDataRecovered(userValues);
                                                }
                                            }
                                        });
                                    }else{
                                        onDataRecovered(userValues);
                                    }
                                }catch (Exception e){
                                    onErrorOccurred(e.getMessage());
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,picture.type(large)," +
                        "first_name,last_name");
                request.setParameters(parameters);
                request.executeAsync();
            }
        }catch (Exception ignored){
            onErrorOccurred(mContext.getResources().getString(R.string.try_again));
        }
    }

    private void FirebaseGoogleAuth(String token){
        AuthCredential authCredential = GoogleAuthProvider.getCredential(token, null);
        onSuccess();
        if(!relink) {
            firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                firebaseUser = firebaseAuth.getCurrentUser();
                                try {
                                    if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                        onAuthenticated(true);
                                        recoverData();
                                        return;
                                    }
                                    onAuthenticated(false);
                                } catch (Exception ignored) {
                                    onAuthenticated(false);
                                }
                            } else {
                                if (task.getException() != null) {
                                    onErrorOccurred(task.getException().getMessage());
                                } else {
                                    onErrorOccurred(mContext.getResources().getString(R.string.try_again));
                                }
                            }
                        }
                    }
            );
        }else{
            firebaseUser = firebaseAuth.getCurrentUser();
            firebaseUser.linkWithCredential(authCredential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Successfully linked emailLink credential!");
                            AuthResult result = task.getResult();
                            onAuthenticated(false);
                        } else {
                            Log.e(TAG, "Error linking emailLink credential", task.getException());
                            onErrorOccurred(task.getException().getMessage());
                        }
                    });
        }
    }

    private void FirebaseFacebookAuth(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            firebaseUser = firebaseAuth.getCurrentUser();
                            try {
                                if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                    onAuthenticated(true);
                                    recoverData();
                                    return;
                                }
                                onAuthenticated(false);
                            }catch (Exception ignored){
                                onAuthenticated(false);
                            }
                        }
                        else {
                            if(task.getException() != null) {
                                onErrorOccurred(task.getException().getMessage());
                            }else{
                                onErrorOccurred(mContext.getResources().getString(R.string.try_again));
                            }
                        }
                    }

                });
    }


    public abstract void onSuccess();

    public abstract boolean canRecoverImage();

    public abstract void onDataRecovered(Values userValues);

    public abstract void onErrorOccurred(String message);

    public abstract void onAuthenticated(Boolean waitForDataRecovery);
}