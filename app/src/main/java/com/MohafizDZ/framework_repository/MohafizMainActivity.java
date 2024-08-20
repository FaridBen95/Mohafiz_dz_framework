package com.MohafizDZ.framework_repository;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.MohafizDZ.App;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.framework_repository.Utils.FileManager;
import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.framework_repository.Utils.MySharedPreferences;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.controls.MMenuAdapter;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.framework_repository.service.MSyncStatusObserverListener;
import com.MohafizDZ.framework_repository.service.SyncUtilsInTheAppRun;
import com.MohafizDZ.framework_repository.service.SyncUtilsWithSyncAdapter;
import com.MohafizDZ.framework_repository.service.SyncingReport;
import com.MohafizDZ.framework_repository.service.receiver.LowStorageBroadcastReceiver;
import com.MohafizDZ.project.StartClassHelper;
import com.MohafizDZ.project.models.ConfigurationModel;
import com.MohafizDZ.project.models.UserModel;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import cn.pedant.SweetAlert.SweetAlertDialog;
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout;
import nl.psdcompany.duonavigationdrawer.views.DuoMenuView;
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle;

public class MohafizMainActivity extends MyAppCompatActivity implements DuoMenuView.OnMenuClickListener, MSyncStatusObserverListener {
    public static final String TAG = MohafizMainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_REQUIRED_PERMS = 13;
    public static final int FACEBOOK_REQUEST_CODE = 64206;
    //if a bundle contains this then open the corresponding fragment
    public static final String FRAGMENT_NAME = "fragment_name";
    private static final String FIRST_RUN = "first_run";
    public static final String FIRST_RUN_DATE = "first_run_date";
    private static final int KEEP_SERVICE_RUNNING = 5017;
    private static final String INTENT_TYPE_KEY = "intent_type";
    private String toOpenFragment = null;
    private Bundle data;
    private DuoDrawerLayout drawerLayout;
    private ArrayList<String> menuOptions;
    private MMenuAdapter menuAdapter;
    private DuoMenuView duoMenuView;
    private List<String> syncedModels = new ArrayList<>();
    private List<String> signUpProcessSyncedModels = new ArrayList<>();
    public AlertDialog progressDialog;
    private boolean opened;
    private boolean waitForFirstSync;
    private View imageView;
    private Animation animation;
    private View waitingFrameLayout;
    private LowStorageBroadcastReceiver lowStorageBroadcastReceiver;
    private boolean allowStartProjectCode = false;
    private int userResyncDelay = 15;
    private ActivityResultLauncher<Intent> storagePermissionResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_start);
        sethasSyncListener(this);
        imageView = findViewById(R.id.logo_up);
        waitingFrameLayout = findViewById(R.id.waitingFrameLayout);
        waitingFrameLayout.setVisibility(View.GONE);
        initAnimation();
        if(app().forceAutomaticDate() && !app().dateIsCorrect()){
            SweetAlertDialog dateDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
            dateDialog.setTitleText(getString(R.string.incorrect_date))
                    .setContentText(getString(R.string.incorrect_date_msg)).setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    });
            dateDialog.show();
            return;
        }
        if(appVersionIsLow()){
            SweetAlertDialog lowVersionDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
            lowVersionDialog.setTitleText(getResources().getString(R.string.low_version_title))
                    .setContentText(getResources().getString(R.string.low_version_message))
                    .setOnDismissListener(dialog -> finish());
            lowVersionDialog.setConfirmClickListener(dialog -> {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }).setConfirmText(getResources().getString(R.string.update));
            lowVersionDialog.show();
            return;
        }
        initResultLaunchers();
        initConfig();
        init();
        initArgs();
        data = null;
        if(toOpenFragment != null) {
            data = new Bundle();
            data.putString(FRAGMENT_NAME, toOpenFragment);
        }
        if(allowStartProjectCode) {
            startProjectCode();
        }
    }

    private boolean appVersionIsLow() {
        DataRow minVersionRow = new ConfigurationModel(this).
                getValue(ConfigurationModel.MIN_VERSION_TO_ALLOW);
        if(minVersionRow != null){
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                int versionCode = pInfo.versionCode;
                if(versionCode < minVersionRow.getInteger("value")){
                    return true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void initAnimation() {
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation_down_to_center);
        animation.setInterpolator(new AnticipateOvershootInterpolator());
        animation.setDuration(2000);
        imageView.startAnimation(animation);
        new Handler().postDelayed(() -> {
            waitingFrameLayout.setVisibility(View.VISIBLE);
        }, 2000);
    }

    private void startProjectCode() {
        verifyUserConnection();
    }

    private void verifyUserConnection() {
        Log.d(TAG, "verifyUserConnection");
        DataRow currentUserRow = app().getCurrentUser();
        if(app().isConnected()) {
            int userResyncDelay = App.TEST_MODE? 30 : this.userResyncDelay;
            boolean userSynced = new UserModel(this).syncWithSuccess(userResyncDelay);
            if (currentUserRow != null && (!app().inNetwork() || userSynced)) {
                openNextPage();
            }else{
                syncUser();
            }
        }
    }

    public void syncUser() {
        showProgressDialog();
        Bundle bundle = new Bundle();
        bundle.putString("from", TAG);
        Log.d(TAG, "Start Syncing user");
        cancelLastSync();
        if (App.syncUsingBackgroundServices) {
            SyncUtilsWithSyncAdapter.requestSync(this, UserModel.BASE_AUTHORITY, bundle);
        } else {
            SyncUtilsInTheAppRun.requestSync(this, UserModel.BASE_AUTHORITY, UserModel.class, bundle);
        }
    }

        private void cancelLastSync() {
            new ConfigurationModel(this).setSyncing(false);
            new UserModel(this).setSyncing(false);
        }

        private static final Intent[] POWERMANAGER_INTENTS = {
                new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
                new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
                new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
                new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
                new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.privacypermissionsentry.PermissionTopActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.FakeActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.privacypermissionsentry.PermissionTopActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.privacypermissionsentry.PermissionTopActivity.Startupmanager")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.startupapp.startupmanager")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startupmanager.startupActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startupapp.startupmanager")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startsettings")),
                new Intent().setComponent(new ComponentName("com.coloros.safe", "com.coloros.safe.permission.startupmanager.StartupAppListActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safe", "com.coloros.safe.permission.startupapp.StartupAppListActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safe", "com.coloros.safe.permission.startup.StartupAppListActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startupmanager.StartupAppListActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startupapp.StartupAppListActivity")),
                new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.FakeActivity")),
                new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
                new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
                new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
                new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
                new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity")),
                new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
                new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
                new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity")),
                new Intent().setComponent(new ComponentName("com.transsion.phonemanager", "com.itel.autobootmanager.activity.AutoBootMgrActivity"))
        };

        private void keepServicesInChineseDevices() {
            if(new MySharedPreferences(this).getBoolean(MySharedPreferences.KEEP_SERVICE_RUNNING_KEY, false)){
                return;
            }
//        Intent intent = new Intent();
//
//        String manufacturer = android.os.Build.MANUFACTURER;
//        boolean startIntent = true;
//        Toast.makeText(this, manufacturer, Toast.LENGTH_SHORT).show();
//
//        switch (manufacturer.toLowerCase()) {
//
//            case "xiaomi":
//                intent.setComponent(new ComponentName("com.miui.securitycenter",
//                        "com.miui.permcenter.autostart.AutoStartManagementActivity"));
//                break;
//            case "oppo":
//                intent.setComponent(new ComponentName("com.coloros.safecenter",
//                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
//
//                break;
//            case "vivo":
//                intent.setComponent(new ComponentName("com.vivo.permissionmanager",
//                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
//                break;
//            default:
//                startIntent = false;
//                new MySharedPreferences(this).setBoolean(MySharedPreferences.KEEP_SERVICE_RUNNING_KEY, true);
//                break;
//        }
//        if(startIntent) {
//            try {
//                startActivityForResult(intent, KEEP_SERVICE_RUNNING);
//            }catch (Exception ignored){}
//        }
            for (Intent intent : POWERMANAGER_INTENTS) {
                boolean success;
                if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                    try {
                        success = true;
                        startActivityForResult(intent, KEEP_SERVICE_RUNNING);
                    } catch (Exception e) {
                        success = false;
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    if(success) break;
                }
            }
        }

//    private void initOPPO() {
//        try {
//
//            Intent i = new Intent(Intent.ACTION_MAIN);
//            i.setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.floatwindow.FloatWindowListActivity"));
//            startActivity(i);
//        } catch (Exception e) {
//            e.printStackTrace();
//            try {
//
//                Intent intent = new Intent("action.coloros.safecenter.FloatWindowListActivity");
//                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.floatwindow.FloatWindowListActivity"));
//                startActivity(intent);
//            } catch (Exception ee) {
//
//                ee.printStackTrace();
//                try{
//
//                    Intent i = new Intent("com.coloros.safecenter");
//                    i.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity"));
//                    startActivity(i);
//                }catch (Exception e1){
//
//                    e1.printStackTrace();
//                }
//            }
//
//        }
//
//        if (Build.MANUFACTURER.equalsIgnoreCase("oppo")) {
//            try {
//                Intent intent = new Intent();
//                intent.setClassName("com.coloros.safecenter",
//                        "com.coloros.safecenter.permission.startup.StartupAppListActivity");
//                startActivity(intent);
//            } catch (Exception e) {
//                try {
//                    Intent intent = new Intent();
//                    intent.setClassName("com.oppo.safe",
//                            "com.oppo.safe.permission.startup.StartupAppListActivity");
//                    startActivity(intent);
//
//                } catch (Exception ex) {
//                    try {
//                        Intent intent = new Intent();
//                        intent.setClassName("com.coloros.safecenter",
//                                "com.coloros.safecenter.startupapp.StartupAppListActivity");
//                        startActivity(intent);
//                    } catch (Exception exx) {
//
//                    }
//                }
//            }
//        }
//
//        try
//        {
//            //Open the specific App Info page:
//            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//            intent.setData(Uri.parse("package:" + getPackageName()));
//            startActivity(intent);
//        }
//        catch ( ActivityNotFoundException e )
//        {
//            //Open the generic Apps page:
//            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
//            startActivity(intent);
//        }
//
//        AutoStartHelper.getInstance().getAutoStartPermission(this);
//    }

        private void syncModels() {
            //todo sync necessary models
        }

        @Override
        public void setTitleBar(androidx.appcompat.app.ActionBar actionBar) {
            if (actionBar != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("");
            }
        }

        private void initConfig() {
            checkForLowStorage();
            //todo some of these configs should be added to the launcher activity
            requestAppPermissions();
            if(App.syncUsingBackgroundServices) {
                keepServicesInChineseDevices();
                AutoStartHelper.getInstance().getAutoStartPermission(this);
            }
//        initOPPO();
            MySharedPreferences mySharedPreferences = new MySharedPreferences(this);
            if(mySharedPreferences.getBoolean(FIRST_RUN, true)){
                mySharedPreferences.setBoolean(FIRST_RUN, false);
                mySharedPreferences.putString(FIRST_RUN_DATE, MyUtil.getCurrentDate());
                onFirstRun();
            }
            FirebaseMessaging.getInstance().setAutoInitEnabled(true);
            // new code
            //previous code
        /*
        int WRITE_EXTERNAL_STORAGE_Check = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int CALL_PHONE_Check = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE);
        int ACCESS_FINE_LOCATION_Check = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int ACCESS_COARSE_LOCATION_Check = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if(CALL_PHONE_Check != PackageManager.PERMISSION_GRANTED ||
                ACCESS_FINE_LOCATION_Check != PackageManager.PERMISSION_GRANTED ||
                ACCESS_COARSE_LOCATION_Check != PackageManager.PERMISSION_GRANTED ||
                WRITE_EXTERNAL_STORAGE_Check != PackageManager.PERMISSION_GRANTED ) {
            showPermissionInfoDialog();
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.CALL_PHONE,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_REQUIRED_PERMS );
        }else{
            allowStartProjectCode = true;
            app().createApplicationFolder();
        }*/
            onChangeView = new OnChangeView() {
                @Override
                public void openedClass(Class opened) {
                    if(opened == null){
                        findViewById(R.id.waitingFrameLayout).setVisibility(View.VISIBLE);
                    }else{
                        findViewById(R.id.waitingFrameLayout).setVisibility(View.GONE);
                    }
//                new android.os.storage.StorageVolume.EXTRA_STORAGE_VOLUME
                }
            };
        }


        private void requestAppPermissions() {
            requestStoragePermission();
        }

        private void requestStoragePermission(){
            // Check if the permissions are already granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Permission is granted, proceed with the operation
                    app().createApplicationFolder();
                    allowStartProjectCode = true;
                } else {
                    // Request for MANAGE_EXTERNAL_STORAGE
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    storagePermissionResultLauncher.launch(intent);
                }
            } else {
                // Request WRITE_EXTERNAL_STORAGE for older versions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_REQUIRED_PERMS);
            }
        }
        private void showPermissionInfoDialog() {
//        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
//        dialog.setTitleText("Permissions");
//        dialog.setContentText("In this app we will need to access theses permissions to let you call in the market place or add ph");
//        dialog.setConfirmText(getResources().getString(R.string.dialog_ok));
//        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//            @Override
//            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                disconnect(activity);
//            }
//        });
//        dialog.setCancelText(getResources().getString(R.string.cancel));
//        dialog.setCancelClickListener(null);
//        dialog.setCancelable(true);
//        dialog.show();
        }

        private void checkForLowStorage() {
//        File cacheDir = getCacheDir();

            long mFreeMem = getDeviceCurrentStorage();
            long deviceLowStorageThreshold = getDeviceLowStorageThreshold();
//        Toast.makeText(this, (deviceLowStorageThreshold  ) + "", Toast.LENGTH_LONG).show();
//        Toast.makeText(this, (mFreeMem  ) + "", Toast.LENGTH_LONG).show();
            if (mFreeMem <= deviceLowStorageThreshold) {
                Toast.makeText(this, R.string.low_storage_error_message, Toast.LENGTH_LONG).show();
                // Handle storage low state
            }/* else {
            // Handle storage ok state
        }*/
        }

        private long getDeviceCurrentStorage() {

            long mFreeMem = 0;
            try {
                StatFs mDataFileStats = new StatFs("/data");
                mDataFileStats.restat("/data");
                mFreeMem = (long) mDataFileStats.getAvailableBlocksLong() *
                        mDataFileStats.getBlockSizeLong();
            } catch (IllegalArgumentException e) {
                // use the old value of mFreeMem
            }
            return mFreeMem;
        }

        private long getDeviceLowStorageThreshold() {

            long value = Settings.Secure.getInt(
                    getContentResolver(),
                    "sys_storage_threshold_percentage",
                    10);
            StatFs mDataFileStats = new StatFs("/data");
            long mTotalMemory = ((long) mDataFileStats.getBlockCountLong() *
                    mDataFileStats.getBlockSizeLong()) / 100L;
            value *= mTotalMemory;
//        Toast.makeText(this, value + "", Toast.LENGTH_SHORT).show();
            long maxValue = Settings.Secure.getInt(
                    getContentResolver(),
                    "sys_storage_threshold_max_bytes",
                    500*1024*1024);
//        Toast.makeText(this, maxValue + "", Toast.LENGTH_SHORT).show();
            return Math.min(value, maxValue);
        }

        private void registerLowStorageReceiver() {
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(lowStorageBroadcastReceiver, lowstorageFilter) != null;
//        getApplicationContext().registerReceiver(lowStorageBroadcastReceiver, new IntentFilter(LowStorageBroadcastReceiver.INTENT_FILTER));
        }

        private void unregisterLowStorageReceiver() {
            getApplicationContext().unregisterReceiver(lowStorageBroadcastReceiver);
        }

        private void onFirstRun() {
            new SyncingReport(this).getWritableDatabase();
            if(new MySharedPreferences(this).getBoolean(MySharedPreferences.KEEP_SERVICE_RUNNING_KEY, true)) {
                Bundle data = new Bundle();
                data.putString("from", TAG);
                if(App.syncUsingBackgroundServices) {
                    SyncUtilsWithSyncAdapter.requestSync(this, UserModel.BASE_AUTHORITY, data);
                    SyncUtilsWithSyncAdapter.requestSync(this, ConfigurationModel.AUTHORITY, data);
                    waitForFirstSync = true;
                }else{
                    SyncUtilsInTheAppRun.requestSync(this, UserModel.BASE_AUTHORITY, UserModel.class, data);
                    SyncUtilsInTheAppRun.requestSync(this, ConfigurationModel.AUTHORITY, ConfigurationModel.class, data);
                }
            }

        }

        private void initControls() {
        }

        private void initArgs() {
            Bundle data = getIntent().getExtras();
            if(data != null){
                toOpenFragment = data.containsKey(FRAGMENT_NAME)? data.getString(FRAGMENT_NAME) : null;
            }
        }

        private void initAdapter() {
        }

        private void init(){
            progressDialog = MyUtil.getProgressDialog(this);
        }

        private void initResultLaunchers(){
            storagePermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()){
                    app().createApplicationFolder();
                    startProjectCode();
                }else{
                    showToast(getString(R.string.accept_permission));
                    finish();
                }
            });
        }

        @Override
        public androidx.appcompat.widget.Toolbar setToolBar() {
            return (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.start_activity_menu, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if(item.getItemId() == android.R.id.home){
                if(drawerLayout.isDrawerOpen()){
                    drawerLayout.closeDrawer();
                } else{
                    drawerLayout.openDrawer();
                }
                return false;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode,
        String[] permissions, int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            switch (requestCode) {
                case PERMISSIONS_REQUEST_REQUIRED_PERMS: {
                    app().createApplicationFolder();
                    allowStartProjectCode = true;
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, permissions[i] + " not granted");
                            allowStartProjectCode = false;
                            Toast.makeText(this, getString(R.string.accept_permission), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                    startProjectCode();
                    return;
                }


                // other 'case' lines to check for other
                // permissions this app might request
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(requestCode == FACEBOOK_REQUEST_CODE || requestCode == FileManager.REQUEST_CAMERA ||
                    requestCode == FileManager.REQUEST_IMAGE ){
                getSupportFragmentManager().findFragmentById(R.id.fragment_container).
                        onActivityResult(requestCode, resultCode, data);
            }else if(requestCode == KEEP_SERVICE_RUNNING){
                Bundle bundle = new Bundle();
                bundle.putString("from", TAG);
                if(App.syncUsingBackgroundServices) {
                    SyncUtilsWithSyncAdapter.requestSync(this, ConfigurationModel.AUTHORITY, bundle);
                    waitForFirstSync = true;
                }else{
                    SyncUtilsInTheAppRun.requestSync(this, ConfigurationModel.AUTHORITY, ConfigurationModel.class, bundle);
                }
                verifyUserConnection();
            }
        }

        @Override
        protected void onStop() {
            super.onStop();
        }

        public DuoDrawerLayout getDrawerLayout() {
            return drawerLayout;
        }

        public DuoMenuView getDuoMenuView() {
            return duoMenuView;
        }

        @Override
        public void onFooterClicked() {
            //todo logout here
        }

        @Override
        public void onHeaderClicked() {
            //nothing
        }

        @Override
        public void onOptionClicked(int position, Object objectClicked) {
//        switch (position){
//        }
            drawerLayout.closeDrawer();
        }

        @Override
        public void onSyncStart(Bundle data, Model model) {

        }

        @Override
        public void onSyncFinish(Bundle data, Model model) {
            Log.d(TAG, "onsync finished1 " + model.getModelName());
            String from = data.containsKey("from") ? data.getString("from") : "";
            if (from != null) {
                if (from.equals(MohafizMainActivity.TAG) && !opened) {
                    Log.d(TAG, "onsync finished " + model.getModelName());
                    if(model.getModelName().equals("user")
                            || model.getModelName().equals("global_configuration")){
                        if (!syncedModels.contains(model.getModelName())) {
                            syncedModels.add(model.getModelName());
                        }
                    }
                    if((waitForFirstSync && syncedModels.size() == 2) || (!waitForFirstSync && syncedModels.size() >= 1 && syncedModels.contains("user"))) {
                        syncedModels.clear();
                        if(App.syncUsingBackgroundServices) {
                            if (waitForFirstSync) {
                                SyncUtilsWithSyncAdapter.setSyncPeriodic(this, ConfigurationModel.AUTHORITY, SyncUtilsWithSyncAdapter.SYNC_PERIOD_LOW_PRIORITY, null);
                                waitForFirstSync = false;
                            }
                        }
                        openNextPage();
                    }
                }
            }
        }

        private void openNextPage() {
            if(opened){
                return;
            }
            Log.d(TAG, "openNextPage" + opened);
            int userResyncDelay = App.TEST_MODE? 30 : this.userResyncDelay;
            boolean userSynced = new UserModel(MohafizMainActivity.this).syncWithSuccess(userResyncDelay);
            if(!app().inNetwork() || userSynced) {
                DataRow currentUserRow = app().getCurrentUser();
                if (currentUserRow == null ||
                        (!currentUserRow.getBoolean("is_anonymous")
                                && (!currentUserRow.getBoolean("_is_active")))) {
                    opened = true;
                    StartClassHelper.openSignUpActivity(this);
                }else{
                    opened = true;
                    StartClassHelper.openProjectMainActivity(this);
                }
            }else{//todo maybe i need to force finish if the user isn't synced
                Log.d(TAG, "user not synced");
                if(!new MySharedPreferences(this).getBoolean(LOCAL_DATE_INCORRECT, false)) {
                    finish();
                }
            }
        }

        private void updateUserModel() {
            UserModel userModel = new UserModel(this);
            DataRow currentUserRow = app().getCurrentUser();
            if(currentUserRow.getString("country").equals("Algeria")){
                Values values = new Values();
                values.put("country_code", "DZ");
                userModel.update(currentUserRow.getString(Col.SERVER_ID), values);
            }
        }

        public void showProgressDialog() {
//        if(findViewById(R.id.waitingFrameLayout).getVisibility() != View.VISIBLE){
//            progressDialog.show();
//        }
        }

        private void startSyncing() {
            Bundle bundle = new Bundle();
            bundle.putString("from", MohafizMainActivity.TAG);
        }

        @Override
        public void onBackPressed() {
            super.onBackPressed();
            animation.cancel();
        }

        @Override
        public void onResume() {
            super.onResume();
            registerLowStorageReceiver();
        }

        @Override
        protected void onPause() {
//        unregisterLowStorageReceiver();
            super.onPause();
        }

        public static Intent getIntent(Context context){
            Intent intent = new Intent(context, MohafizMainActivity.class);
            Bundle data = new Bundle();
            intent.putExtras(data);
            return intent;
        }

    }

