package com.MohafizDZ.framework_repository.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.MohafizDZ.framework_repository.core.Account.MUser;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.datas.MConstants;

//note that some devices disable services so it should be set in the phone settings and there's no way
//to fix that programmatically, the solution either use this class and show a tutorial how to enable services
//in these devices otherwise use SyncUtilsInTheAppRun
public class SyncUtilsWithSyncAdapter {
    public static final Integer SYNC_PERIOD_VERY_HIGH_PRIORITY = 300;
    public static final Integer SYNC_PERIOD_HIGH_PRIORITY = 900;
    public static final Integer SYNC_PERIOD_LOW_PRIORITY = 43200;
    public static final Integer SYNC_PERIOD_NORMAL_PRIORITY = 3600;
    private static final String TAG = SyncUtilsWithSyncAdapter.class.getSimpleName();

    public static boolean requestSync(Context context){
        return requestSync(context, Model.BASE_AUTHORITY);
    }

    public static boolean requestSync(Context context, String authority){
        return requestSync(context, authority, null);
    }

    public static boolean requestSync(Context context, String authority, Bundle bundle){
        AccountManager aManager = AccountManager.get(context);
        try {
            Account account = aManager.getAccountsByType(MConstants.KEY_ACCOUNT_TYPE)[0];
            if (account != null) {
                requestSync(account, authority, bundle);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "No account Found");
        return false;
    }

    public static void requestSync(Account account, String authority){
        requestSync(account, authority, null);
    }

    public static void requestSync(Account account, String authority, Bundle extras){
        Bundle settingsBundle = extras == null? new Bundle() : extras;
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.setIsSyncable(account, authority, 1);
        ContentResolver.requestSync( account, authority, settingsBundle);
    }

    public static void requestSync(Account account){
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.setIsSyncable(account, Model.BASE_AUTHORITY, 1);
        ContentResolver.requestSync( account, Model.BASE_AUTHORITY, settingsBundle);
    }

    public static void setSyncPeriodic(Context context, String authority, long seconds) {
        setSyncPeriodic(context, authority, seconds, null);
    }

    public static void setSyncPeriodic(Context context, String authority, long seconds, Bundle bundle) {
        Account account = getCurrentAccount(context);
        Bundle extras = bundle == null? new Bundle() : bundle;
        setAutoSync(context, authority, true);
        ContentResolver.setIsSyncable(account, authority, 1);
        ContentResolver.addPeriodicSync(account, authority, extras,
                seconds);
    }

    public static void setAutoSync(Context context, String authority, boolean autoSync) {
        try {
            Account account = getCurrentAccount(context);
            ContentResolver.setIsSyncable(account, authority, 1);
            if (!ContentResolver.isSyncActive(account, authority)) {
                ContentResolver.setSyncAutomatically(account, authority, autoSync);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public static Account getCurrentAccount(Context context){
        return MUser.getCurrentMUser(context).getAccount();
//        AccountManager aManager = AccountManager.get(context);
//        try {
//            return aManager.getAccountsByType(MConstants.KEY_ACCOUNT_TYPE)[0];
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return null;
    }
}
