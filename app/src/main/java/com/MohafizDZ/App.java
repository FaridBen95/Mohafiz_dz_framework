package com.MohafizDZ;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.MohafizDZ.framework_repository.Utils.MySharedPreferences;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Account.MUser;
import com.MohafizDZ.framework_repository.core.Account.MainLogInActivity;
import com.MohafizDZ.framework_repository.core.Account.MyAccountManager;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.ModelRegistryUtils;
import com.MohafizDZ.framework_repository.core.MySqlite;
import com.MohafizDZ.framework_repository.core.SQLitesListSingleton;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.MohafizDZ.empty_project.R;
import com.MohafizDZ.project.models.UserModel;
import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class App extends MultiDexApplication {
    public static final boolean TEST_MODE = true;
    private static final String TAG = App.class.getSimpleName();
    public static final String CATEGORY_1 = "HIGH_CATEGORY";
    public static final String CATEGORY_2 = "LOW_CATEGORY";

    public static String ANDROID_NAME = MConstants.DEFAULT_ANDROID_NAME;
    private static HashMap<String, MySqlite> sqlites = new HashMap<>();
    private static ModelRegistryUtils modelRegistryUtils = new ModelRegistryUtils();

    public StorageReference storageReference;
    public FirebaseAuth firebaseAuth;
    private static App enableMultiDex;

    public static App getEnableMultiDexApp() {
        return enableMultiDex;
    }

    public App(){
        enableMultiDex=this;
    }

    public static ModelRegistryUtils getModelRegistryUtils() {
        return modelRegistryUtils;
    }

    public static HashMap<String, MySqlite> getSqlites() {
        return sqlites;
    }

    public static void setSqlites(HashMap<String, MySqlite> sqlites) {
        App.sqlites = sqlites;
    }

    public static void addSQLite(String modelName, MySqlite sqlite){
        App.sqlites.put(modelName, sqlite);
        SQLitesListSingleton.getSQLiteList().sqlites = sqlites;
    }

    public static <T> T getModel(Context context, String className, String username) {
        MUser user = MUser.getDetails(context, username);
        Class<? extends Model> modelCls = App.modelRegistryUtils.getModel(className);
        if (modelCls != null) {
            try {
                Constructor constructor = modelCls.getConstructor(Context.class);
                return (T) constructor.newInstance(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        TypefaceProvider.registerDefaultIconSets();
        storageReference = FirebaseStorage.getInstance().getReference("images");
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.getCurrentUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    try {
                        MySharedPreferences mySharedPreferences = new MySharedPreferences(App.this);
                        String lastToken = mySharedPreferences.getString(MUser.AUTH_TOKEN_KEY, "");
                        String currentToken = task.getResult().getToken();
                        if(!lastToken.equals(currentToken)) {
                            mySharedPreferences.putString(MUser.AUTH_TOKEN_KEY, currentToken);
                            mySharedPreferences.putString(MUser.AUTH_TOKEN_WRITE_DATE_KEY, MyUtil.getCurrentDate());
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
        }
        SQLitesListSingleton.getSQLiteList().sqlites = sqlites;
        modelRegistryUtils.makeReady(getApplicationContext());
        projectOnCreate();
    }

    private void projectOnCreate() {
        createNotificationCategory();
//        subscribeToUserIdTopic();
    }

    public void subscribeToUserIdTopic(DataRow currentUserRow) {
        if(currentUserRow == null){
            FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
            firebaseMessaging.subscribeToTopic("not_authenticated");
        }else{
            FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
            firebaseMessaging.unsubscribeFromTopic("not_authenticated");
            firebaseMessaging.subscribeToTopic(currentUserRow.getString(Col.SERVER_ID));
        }
    }

    private void unsubscribeUserFromTopic() {
        DataRow currentUserRow = getCurrentUser();
        if(currentUserRow == null){
            FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
            firebaseMessaging.unsubscribeFromTopic("not_authenticated");
        }else{
            FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
            firebaseMessaging.unsubscribeFromTopic(currentUserRow.getString(Col.SERVER_ID));
        }
    }

    private void createNotificationCategory() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel category1 = new NotificationChannel(
                    CATEGORY_1,
                    "High Category",
                    NotificationManager.IMPORTANCE_HIGH
            );
            category1.setDescription("This is high category message");

            NotificationChannel category2 = new NotificationChannel(
                    CATEGORY_2,
                    "Low Category",
                    NotificationManager.IMPORTANCE_LOW
            );
            category2.setDescription("This is low category message");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager != null){
                notificationManager.createNotificationChannel(category1);
                notificationManager.createNotificationChannel(category2);
            }
        }
    }

    public static SQLiteDatabase getDB( String databaseAccountName, boolean writableDatabase){
        return writableDatabase ? sqlites.get(databaseAccountName).getWritableDatabase() :
                sqlites.get(databaseAccountName).getReadableDatabase();
    }

    /**
     * Checks for network availability
     *
     * @return true, if network available
     */
    public boolean inNetwork() {
        return inNetwork(false);
    }
    public boolean inNetwork(boolean checkInternet) {
        boolean isConnected = false;
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = manager.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isConnectedOrConnecting()) {
            isConnected = true;
        }
        boolean internetConnected = false;
        if(isConnected && checkInternet){
            try{
                internetConnected = isInternetAvailable();
            }catch (Exception ignored){}
        }
        return isConnected && (!checkInternet || internetConnected);
    }

    public boolean isInternetAvailable()throws InterruptedException, IOException {
        String command = "ping -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public DataRow getCurrentUser(){
        try{
            String user_id = firebaseAuth.getCurrentUser().getUid();
            Log.d("user_id", user_id);
            UserModel userModel = new UserModel(this);
            DataRow userRow = userModel.browse(" firebase_user_id = ? ",
                    new String[]{user_id});
            if(userRow == null || !userRow.getBoolean("subscribed_to_fcm")){
                subscribeToUserIdTopic(userRow);
                Values values = new Values();
                values.put("subscribed_to_fcm", 1);
                values.put("_is_updated", userRow.getBoolean("_is_updated")? 1 : 0);
                userModel.update(userRow.getInteger(Col.ROWID), values);
            }
            userRow = userModel.getRelations(userRow);
            return userRow;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean isConnected(){
        return firebaseAuth.getCurrentUser() != null;
    }

    public void disconnect(final Activity activity) {
        //this needs a conceptions before activating it
        AccountManager accountManager = AccountManager.get(activity);
        MUser user = MUser.getCurrentMUser(activity.getApplicationContext());
        unsubscribeUserFromTopic();
        accountManager.setUserData(user.getAccount(), "isactive", "false");
        MyAccountManager.logout(activity, user.getAndroidAccountName());
        firebaseAuth.signOut();
        MyAccountManager.deleteCurrentAccount(accountManager, user, new AccountManagerCallback() {
            @Override
            public void run(AccountManagerFuture future) {
                new MySharedPreferences(activity).clearAll();
                deleteCache(activity);
                restartApp(activity);
            }
        });
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace();}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public void refreshLanguage(Activity activity, String language) {
        //todo change application language and refresh layouts
        MyUtil.setLocale(activity, language);
        MyUtil.toastIconInfo(activity, getResources().getString(R.string.language_changed));
        restartApp(activity);
    }

    public void refreshTheme(Activity activity) {
        //todo change application theme and refresh layouts
        MyUtil.toastIconInfo(activity, getResources().getString(R.string.theme_changed));
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseAuth.getCurrentUser();
    }
    public static void restartApp(Activity activity){
        Intent mainIntent = new Intent(activity, MainLogInActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.finishAffinity();
        activity.getApplicationContext().startActivity(mainIntent);
        System.exit(0);
    }

    public boolean checkAppFolderExist() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/MohafizDZ/"+ App.getApplicationName(this);
        return new File(path).exists();
    }

    public boolean checkAppImagesFolderExist() {
        String folder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            folder= getExternalFilesDir
                    (Environment.DIRECTORY_DCIM)  +"/MohafizDZ/"+ App.getApplicationName(this);
        }
        else
        {
            folder = Environment.getExternalStorageDirectory().getPath() + "/MohafizDZ/"+ App.getApplicationName(this) + "/";
        }
        return new File(folder).exists();
    }

    public void createApplicationFolder() {
        MConstants.applicationFolder = Environment.getExternalStorageDirectory().getPath() + "/MohafizDZ/"+ App.getApplicationName(this) + "/";
        MConstants.applicationImagesFolder = Environment.getExternalStorageDirectory().getPath() + "/MohafizDZ/"+ App.getApplicationName(this) + "/";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            MConstants.applicationImagesFolder= getExternalFilesDir
                    (Environment.DIRECTORY_DCIM)+"/MohafizDZ/"+ App.getApplicationName(this);
        }
        else
        {
            MConstants.applicationImagesFolder = Environment.getExternalStorageDirectory().getPath() + "/MohafizDZ/"+ App.getApplicationName(this) + "/";
        }
        File directory = new File(Environment.getExternalStorageDirectory().getPath()+  "/MohafizDZ");
        if(!directory.exists()){
            directory.mkdir();
            new File(MConstants.applicationFolder).mkdir();
        }
    }

    public boolean dateIsCorrect() {
        return inNetwork() && Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
    }

    public boolean forceAutomaticDate() {
        return true;
    }

    public void logoutDialog(final Activity activity) {
        SweetAlertDialog dialog = new SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE);
        dialog.setTitleText(getString(R.string.log_out));
        dialog.setContentText(getResources().getString(R.string.logout_message));
        dialog.setConfirmText(getResources().getString(R.string.dialog_ok));
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                disconnect(activity);
            }
        });
        dialog.setCancelText(getResources().getString(R.string.cancel));
        dialog.setCancelClickListener(null);
        dialog.setCancelable(true);
        dialog.show();
    }

    public void onAuthentified(FirebaseUser currentUser) {
    }
}
