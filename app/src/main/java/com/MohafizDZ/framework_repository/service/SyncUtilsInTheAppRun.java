package com.MohafizDZ.framework_repository.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.MohafizDZ.framework_repository.core.Account.MUser;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.MohafizDZ.framework_repository.service.receiver.ISyncFinishReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncUtilsInTheAppRun {
    public static final Integer SYNC_PERIOD_VERY_HIGH_PRIORITY = 300;
    public static final Integer SYNC_PERIOD_HIGH_PRIORITY = 900;
    public static final Integer SYNC_PERIOD_LOW_PRIORITY = 43200;
    public static final Integer SYNC_PERIOD_NORMAL_PRIORITY = 3600;
    private static final String TAG = SyncUtilsInTheAppRun.class.getSimpleName();

//    public static boolean requestSync(Context context){
//        return requestSync(context, Model.BASE_AUTHORITY);
//    }

//    public static boolean requestSync(Context context, String authority, Model model){
//        return requestSync(context, model, authority, null);
//    }

//    public static boolean requestSync(Context context, String authority, Bundle bundle){
//        AccountManager aManager = AccountManager.get(context);
//        try {
//            Account account = aManager.getAccountsByType(MConstants.KEY_ACCOUNT_TYPE)[0];
//            if (account != null) {
////                requestSync(account, authority, bundle);
//                requestSync(account, authority, bundle);
//                return true;
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        Log.d(TAG, "No account Found");
//        return false;
//    }

    public static boolean requestSync(Context context, String authority, Class model){
        return requestSync(context, authority, model, null);
    }
    public static boolean requestSync(Context context, String authority, Class model, Bundle bundle){
        AccountManager aManager = AccountManager.get(context);
        try {
            Account account = aManager.getAccountsByType(MConstants.KEY_ACCOUNT_TYPE)[0];
            if (account != null) {
//                requestSync(account, authority, bundle);
                requestSync(context, model, authority, bundle);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "No account Found");
        return false;
    }

    public static void requestSync(Account account, String authority){
//        requestSync(account, authority, null);
    }

    public static void requestSync(Account account, String authority, Bundle extras){
//        Bundle settingsBundle = extras == null? new Bundle() : extras;
//        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
//        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
//        ContentResolver.setIsSyncable(account, authority, 1);
//        ContentResolver.requestSync( account, authority, settingsBundle);
    }

    public static void requestSync(Context context, Class modelClass, String authority, Bundle bundle){
        requestSync(context, modelClass, authority, bundle, null);
    }

    public static void requestSync(Context context, Class modelClass, String authority, Bundle bundle, SyncUtilsListener listener){
//        Bundle settingsBundle = extras == null? new Bundle() : extras;
//        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
//        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
//        ContentResolver.setIsSyncable(account, authority, 1);
//        ContentResolver.requestSync( account, authority, settingsBundle);
        ExecutorService executor = Executors.newSingleThreadExecutor();
//        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {

            //Background work here

            List<ModelHelper> modelHelperList = new ArrayList<>();
            Model currentModel = Model.createInstance(context, modelClass);
            currentModel.setCanSaveLastSyncDate(true);
            if(currentModel.isSyncing()){
                Log.d(TAG, currentModel.getModelName() + " is in progress");
                if(listener != null){
                    listener.onSyncFailed();
                }
                return;
            }
            modelHelperList.add(new ModelHelper(currentModel));
            SyncModels syncModels = new SyncModels(context, modelHelperList);
            syncModels.performSync();
            Intent intent = new Intent();
            intent.setAction(ISyncFinishReceiver.SYNC_FINISH);
            Bundle data = bundle == null? new Bundle() : bundle;
            data.putString(ISyncFinishReceiver.AUTHORITY_KEY, authority);
            data.putString(ISyncFinishReceiver.TYPE_KEY, "multi");
            data.putString(ISyncFinishReceiver.MODEL_KEY, modelClass.getName());
            data.putString(ISyncFinishReceiver.USERNAME_KEY, currentModel.getmUser().getAndroidAccountName());
            intent.putExtras(data);
            context.getApplicationContext().sendBroadcast(intent);
            if(listener != null){
                listener.onSyncFinished();
            }

//                handler.post(() -> {
//                    //UI Thread work here
//                });
        });
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

    public interface SyncUtilsListener{

        void onSyncFinished();

        void onSyncFailed();
    }
}
