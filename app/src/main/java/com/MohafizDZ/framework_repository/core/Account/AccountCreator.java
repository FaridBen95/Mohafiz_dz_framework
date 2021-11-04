package com.MohafizDZ.framework_repository.core.Account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.AsyncTask;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.google.firebase.auth.FirebaseAuth;

import java.lang.ref.WeakReference;

public class AccountCreator extends AsyncTask<Void, Void, Boolean> {

    private MUser mUser;
    private WeakReference<Context> mContext;
    private AuthenticationHelper authenticationHelper;

    AccountCreator(Context mContext, AuthenticationHelper authenticationHelper, MUser mUser){
        this.mContext = new WeakReference<>(mContext);
        this.authenticationHelper = authenticationHelper;
        this.mUser = mUser;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return createAccount(mContext.get(), mUser, null);
    }

    private boolean createAccount(Context context, MUser mUser, String password) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = new Account(mUser.getAndroidAccountName(), MConstants.KEY_ACCOUNT_TYPE);
        return accountManager.addAccountExplicitly(account, String.valueOf(password),
                mUser.toBundle()) ;
    }

    @Override
    protected void onPostExecute(Boolean successfully) {
        super.onPostExecute(successfully);
        if(successfully){
            mUser = MUser.getCurrentMUser(mContext.get());
            authenticationHelper.setmUser(mUser);
        }
        authenticationHelper.onAccountManagerAuthenticated(successfully, mUser, authenticationHelper.skipped);
    }
}