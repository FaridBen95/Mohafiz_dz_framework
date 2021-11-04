package com.MohafizDZ.framework_repository.core.Account;

import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class AuthService extends Service {
    private MyAccountManager myAccountManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        IBinder binder = null;
        if (intent.getAction().equals(AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
            myAccountManager = new MyAccountManager(this);
            binder = myAccountManager.getIBinder();
        }
        return binder;
    }
}
