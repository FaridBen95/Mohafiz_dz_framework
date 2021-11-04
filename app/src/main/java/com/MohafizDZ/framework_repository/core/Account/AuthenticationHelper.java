package com.MohafizDZ.framework_repository.core.Account;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.NonNull;

import com.MohafizDZ.framework_repository.service.SyncUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public abstract class AuthenticationHelper {

    private Context mContext;
    private FirebaseAuth firebaseAuth;
    private MUser mUser;
    boolean skipped;

    AuthenticationHelper(Context mContext){
        this.mContext = mContext;
        init();
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        mUser = new MUser();
    }

    public void execute(){
        if (checkAuthentication()) {
            createAccountManager();
        }else{
            noUserIsConnected();
        }
    }

    private boolean checkAuthentication() {
        return firebaseAuth.getCurrentUser() != null;
    }

    void skip(){
        skipped = true;
        createAccountManager();
    }

    void logInWithEmailAndPassword(String email, String password){
        if(firebaseAuth.getCurrentUser() == null) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            onFirebaseAuthComplete(task);
                            if (task.isSuccessful()) {
                                execute();
                            }
                        }
                    });
        }else{
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);
            firebaseAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            onFirebaseAuthComplete(task);
                            if (task.isSuccessful()) {
                                execute();
                            }
                        }
                    });
        }
    }

    void createEmailAccoount(String email, String password) {
        if(firebaseAuth.getCurrentUser() == null) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            onFirebaseAuthComplete(task);
                            if (task.isSuccessful()) {
                                execute();
                            }
                        }
                    });
        }else{
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);
            firebaseAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            onFirebaseAuthComplete(task);
                            if (task.isSuccessful()) {
                                execute();
                            }
                        }
                    });
        }
    }

    void forgetPassword(String email) {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        onResetEmailSend(task);
                    }
                }
        );
    }
    private void createAccountManager() {
        if(firebaseAuth.getCurrentUser() != null) {
            String uid = firebaseAuth.getCurrentUser().getUid();
            mUser.setAndroidAccountName(uid);
            mUser.setUid(uid);
            mUser.setEmail(firebaseAuth.getCurrentUser().getEmail());
            mUser.setPhoneNumber(firebaseAuth.getCurrentUser().getPhoneNumber());
        }
        AccountCreator accountCreator = new AccountCreator(mContext,this, mUser);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            accountCreator.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            accountCreator.execute();
    }

    MUser getmUser() {
        return mUser;
    }

    void setmUser(MUser mUser) {
        this.mUser = mUser;
    }

    /**
     * this callback will be called at the last step of authentication
     *
     */
    protected abstract void onAccountManagerAuthenticated(boolean newAccount, MUser mUser, boolean skipped);

    /**
     * this callback will be called when authentication with firebase is complete
     * @param authResult is the authentication result from the firebase
     */
    protected abstract void onFirebaseAuthComplete(Task<AuthResult> authResult);

    protected abstract void onResetEmailSend(Task<Void> task);

    protected abstract void noUserIsConnected();
}
