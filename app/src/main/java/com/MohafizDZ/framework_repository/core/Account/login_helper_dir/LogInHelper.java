package com.MohafizDZ.framework_repository.core.Account.login_helper_dir;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.MohafizDZ.empty_project.R;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Values;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


import java.io.File;
import java.util.Objects;

public class LogInHelper implements ILogIn.Presenter{
    private static final String TAG = LogInHelper.class.getSimpleName();
    private final ILogIn.View view;
    private final Context context;
    private final ComponentActivity activity;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleLogInResultLauncher;
    private boolean linkWithAccount;
    private ILogIn.LogInType logInType;

    public LogInHelper(ComponentActivity activity, ILogIn.View view) {
        this.view = view;
        this.context = activity;
        this.activity = activity;
    }

    @Override
    public void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, gso);
        setControls();
    }

    @Override
    public boolean isUserConnected() {
        firebaseUser = firebaseAuth.getCurrentUser();
        return firebaseUser != null;
    }

    private void setControls(){
        googleLogInResultLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Task<GoogleSignInAccount> googleSignInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        checkGoogleAuth(googleSignInAccountTask);
                    }
                });
    }

    @Override
    public void googleLogIn() {
        logInType = ILogIn.LogInType.google;
        Intent intent = googleSignInClient.getSignInIntent();
        googleLogInResultLauncher.launch(intent);
    }

    private void checkGoogleAuth(Task<GoogleSignInAccount> completedTask){
        try{
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            view.onGoogleAuthSuccess(account.getIdToken());
        }
        catch (ApiException e){
            view.showToast(getString(R.string.try_again));
        }
    }

    @Override
    public void FirebaseGoogleAuth(String token){
        AuthCredential authCredential = GoogleAuthProvider.getCredential(token, null);
        view.toggleLoading(true);
        if(!linkWithAccount) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null && firebaseUser.isAnonymous()){
                firebaseUser.delete().addOnCompleteListener(activity, Task::isSuccessful);
            }
            firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(activity, task -> {
                if (task.isSuccessful()) {
                    firebaseUser = firebaseAuth.getCurrentUser();
                    try {
                        if (Objects.requireNonNull(task.getResult().getAdditionalUserInfo()).isNewUser()) {
                            recoverData();
                            return;
                        }
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                        view.showToast(getString(R.string.try_again));
                    }
                    view.onAuthSuccess();
                } else {
                    if (task.getException() != null) {
                        view.showToast(task.getException().getMessage());
                    }
                    view.showToast(getString(R.string.try_again));
                }
            }
            );
        }else{
            firebaseUser = firebaseAuth.getCurrentUser();
            firebaseUser.linkWithCredential(authCredential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            view.toggleLoading(false);
                            view.onAuthSuccess();
                        } else {
                            Log.e(TAG, "Error linking emailLink credential", task.getException());
                            view.showToast(getString(R.string.try_again));
                        }
                    });
        }
    }

    @Override
    public void loginAsGuest() {
        view.toggleLoading(true);
        firebaseAuth.signInAnonymously().addOnCompleteListener(activity, task -> {
            if (task.isSuccessful()) {
                firebaseUser = firebaseAuth.getCurrentUser();
                try {
                    if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                        recoverData();
                        return;
                    }
                    view.onAuthSuccess();
                } catch (Exception ignored) {
                    view.showToast(getString(R.string.try_again));
                    view.showToast(task.getException().getMessage());
                }
                view.toggleLoading(false);
            } else {
                view.toggleLoading(false);
                if (task.getException() != null) {
                    view.showToast(task.getException().getMessage());
                }
                view.showToast(getString(R.string.try_again));
            }
        });
    }

    private void recoverData() {
        try {
            if (logInType == ILogIn.LogInType.google) {
                final GoogleSignInAccount accountInfo = GoogleSignIn.getLastSignedInAccount(activity);
                final Values userValues = new Values();
                userValues.put("name", accountInfo.getGivenName());
                userValues.put("last_name", accountInfo.getFamilyName());
                userValues.put("email", accountInfo.getEmail());
                userValues.put("user_id", firebaseUser.getUid());
                if (accountInfo.getPhotoUrl() != null) {
                    MyUtil.downloadPicture(context, accountInfo.getPhotoUrl().toString(),
                            new MyUtil.DownloadListener() {
                                @Override
                                public void onDownloadFinished(File imageFile) {
                                    if (imageFile != null) {
                                        userValues.putImage("image", imageFile);
                                        view.onDataRecovered(userValues);
                                        view.toggleLoading(false);
                                        view.onAuthSuccess();
                                    }
                                }
                            });
                } else {
                    view.onDataRecovered(userValues);
                    view.toggleLoading(false);
                    view.onAuthSuccess();
                }
            }else{
                final Values userValues = new Values();
                userValues.put("name", "Anonymous");
                userValues.put("last_name", "");
                userValues.put("email", "anonymous");
                userValues.put("user_id", firebaseUser.getUid());
                userValues.put("is_anonymous", 1);
                view.onDataRecovered(userValues);
                view.toggleLoading(false);
                view.onAuthSuccess();
            }
        }catch (Exception ignored){
            view.showToast(getString(R.string.try_again));
        }
    }


    private String getString(int id){
        return context.getString(id);
    }

}
