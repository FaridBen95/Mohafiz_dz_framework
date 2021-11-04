package com.MohafizDZ.framework_repository.core.Account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.MohafizDZ.framework_repository.MohafizMainActivity;

import java.util.ArrayList;
import java.util.List;

import static com.MohafizDZ.framework_repository.datas.MConstants.KEY_ACCOUNT_TYPE;

public class MyAccountManager extends AbstractAccountAuthenticator {
    public static final String TAG = MyAccountManager.class.getSimpleName();
    private static final String KEY_NEW_ACCOUNT_REQUEST = "create_new_account";

    private Context mContext;

    public MyAccountManager(Context mContext){
        super(mContext);
        this.mContext = mContext;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        final Bundle result;
        final Intent intent;

        intent = new Intent(mContext, MohafizMainActivity.class);
        result = new Bundle();
        intent.putExtra(KEY_NEW_ACCOUNT_REQUEST, true);
        result.putParcelable(AccountManager.KEY_INTENT, intent);
        return  result;
    }

    //todo after fixing the sqlite fix this
    /*@Override
    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
        Bundle result = super.getAccountRemovalAllowed(response, account);
        if (result != null
                && result.containsKey(AccountManager.KEY_BOOLEAN_RESULT)
                && !result.containsKey(AccountManager.KEY_INTENT)) {
            final boolean removalAllowed = result
                    .getBoolean(AccountManager.KEY_BOOLEAN_RESULT);
            if (removalAllowed) {
                OUser user = OdooAccountManager.getDetails(mContext, account.name);
                OdooAccountManager.dropDatabase(user);
            }
        }
        return result;
    }*/


    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }

    /**
     * Logout user
     *
     * @param context
     * @param username
     * @return true, if successfully logged out
     */
    public static boolean logout(Context context, String username) {
        MUser user = getDetails(context, username);
        if (user != null) {
            if (cancelUserSync(user.getAccount())) {
                AccountManager accountManager = AccountManager.get(context);
                accountManager.setUserData(user.getAccount(), "isactive", "false");
                Log.i(TAG, user.getName() + " Logged out successfully");
                return true;
            }
        }
        return false;
    }
    /**
     * Returns OUser object with username
     *
     * @param context
     * @param username
     * @return instance for OUser class or null
     */
    public static MUser getDetails(Context context, String username) {
        for (MUser user : getAllAccounts(context))
            if (user.getAndroidAccountName().equals(username)) {
                return user;
            }
        return null;
    }

    /**
     * Gets all the account related Odoo Auth
     *
     * @param context
     * @return List of OUser instances if any
     */
    public static List<MUser> getAllAccounts(Context context) {
        List<MUser> users = new ArrayList<>();
        AccountManager aManager = AccountManager.get(context);
        for (Account account : aManager.getAccountsByType(KEY_ACCOUNT_TYPE)) {
            MUser user = new MUser();
            user.fillFromAccount(aManager, account);
            user.setAccount(account);
            users.add(user);
        }
        return users;
    }

    private static boolean cancelUserSync(Account account) {
        //TODO: Cancel user's sync services. if any.
        return true;
    }

    public static void deleteCurrentAccount(AccountManager accountManager, MUser user, AccountManagerCallback callback) {
        Account[] accounts = accountManager.getAccounts();
        for (Account accountToRemove : accounts) {
            if(accountToRemove.name.equals( user.getAccount().name)) {
                accountManager.removeAccount(accountToRemove, callback, null);
                break;
            }
        }
    }
}
