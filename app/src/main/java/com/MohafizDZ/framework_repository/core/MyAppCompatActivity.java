package com.MohafizDZ.framework_repository.core;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.MohafizDZ.App;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.framework_repository.local_sentry.GlobalTouchListener;
import com.MohafizDZ.framework_repository.Utils.MySharedPreferences;
import com.MohafizDZ.framework_repository.Utils.FragmentUtils;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.service.MSyncStatusObserverListener;
import com.MohafizDZ.framework_repository.service.receiver.IOnlineDateReceiver;
import com.MohafizDZ.framework_repository.service.receiver.ISyncFinishReceiver;
import com.MohafizDZ.framework_repository.service.receiver.ISyncStartReceiver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public abstract class MyAppCompatActivity extends AppCompatActivity implements ActivityListener, GlobalTouchListener {
    public static final String LOCAL_DATE_INCORRECT = "local_date_incorrect";
    public static final String TAG = MyAppCompatActivity.class.getSimpleName();
    private String info = "No info set for this activity";
    private ActivityListener activityListener;
    public static OnChangeView onChangeView;
    protected Toolbar toolbar;
    private DevicePermissionResultListener mDevicePermissionResultListener = null;
    private MSyncStatusObserverListener mSyncStatusObserverListener;
    private MSearchViewChangeListener mSearchViewChangeListener;
    private SearchView mSearchView;
    public SweetAlertDialog dateDialog;
    private AlertDialog progressDialog = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        MyUtil.setLocale(this, MyUtil.getCurrentLanguageLocale(getApplicationContext()));
        activityListener = this;
        info = activityListener.setInfo();
        setLocale(this);
    }

    //to modify anything after the creation of action bar use the method below
    public void setTitleBar(ActionBar actionBar) {
        if(actionBar != null){
            toolbar = findViewById(R.id.toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_chevron_left_black);
            actionBar.setTitle(null);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        String currentLanguage = MyUtil.getCurrentLanguageLocale(newBase);
        Resources resources = newBase.getResources();
        resources.getConfiguration().setLocale(new Locale(currentLanguage));
        applyOverrideConfiguration(resources.getConfiguration());
        super.attachBaseContext(newBase);
    }

    public void setLocale(Context context) {
        String lang = MyUtil.getCurrentLanguageLocale(context);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(lang));
        res.updateConfiguration(conf, dm);
    }

    @Override
    public String setInfo() {
        return info;
    }

    @Override
    public Class<?> trackActivity() {
        return MyAppCompatActivity.class;
    }

    private View parentView;
    private ArrayList<Field> fieldsList;
    private ArrayList<Field> trackVarList;
    private List<View> viewsList;
    private HashMap<View, String> viewName = new HashMap<>();
    private HashMap<String, Object> currentVariables;
    private MyViewGroup mainParentView;

    @Override
    public String ClickedOn(int x, int y) {
        if (startTrack()) {
            if (trackVariables() != null) {
                detectVariables();
            }
            StringBuilder log = new StringBuilder();
            for (View view : viewsList) {
                if (view != null) {
                    int start_x = mainParentView.getRelativeLeft(view);
                    int start_y = mainParentView.getRelativeTop(view);
                    int end_x = start_x + view.getWidth();
                    int end_y = start_y + view.getHeight();
                    if (x >= start_x && y >= start_y && x <= end_x && y <= end_y) {
                        log.append("\n\r").append("Clicked on View = ").append(viewName.get(view)).append(" of the class = ").append(getClass().getSimpleName());
                    }
                }
            }
            log.append("Les Variables detectés : \n\r");
            for (String key : currentVariables.keySet()) {
                log.append(key);
                log.append(" = ");
                log.append(currentVariables.get(key));
                log.append("\n");
            }

            return log.toString();
        }
        return "";
    }

    private void detectVariables() {
        try {
            List<Field> fieldsList = new ArrayList<>(Arrays.asList(getClass().getDeclaredFields()));
            List<String> trackVariables = trackVariables();
            for (Field field : fieldsList) {
                if(trackVariables.contains(field.getName())) {
                    field.setAccessible(true);
                    currentVariables.put(field.getName(), field.get(this));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean startTrack() {
        return false;
    }

    @Override
    public boolean trackByTag() {
        return false;
    }


    @Override
    public List<Class<?>> trackByClass() {
        return null;
    }

    @Override
    public List<String> trackVariables() {
        return null;
    }


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        toolbar = setToolBar();
        if(toolbar != null){
            setSupportActionBar(toolbar);
        }
        setTitleBar(getSupportActionBar());
        parentView = findViewById(android.R.id.content);
        View view = parentView;
        setViewsFromXML();
        ViewGroup parentView = (ViewGroup) view.getParent();
        if(parentView != null){
            ViewGroup parentparentView = (ViewGroup) parentView.getParent();
            mainParentView = new MyViewGroup(this);
            ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
            mainParentView.setLayoutParams(params);
            parentparentView.removeView(parentView);
            mainParentView.addView(parentView);
            parentparentView.addView(mainParentView);
            mainParentView.setGlobalTouchListener(this);
            if(startTrack()){
                currentVariables = new HashMap<>();
            }
        }
        if(startTrack()){
            currentVariables = new HashMap<>();
        }
    }

    private void setViewsFromXML() {
        if (startTrack()) {
            fieldsList = new ArrayList<>();
            viewsList = new ArrayList<>();
            trackVarList = new ArrayList<>();
            List<Class <?>> trackClassesList = trackByClass();
            List<String> variablesToTrack = trackVariables();
            for (View view : getAllChildrenBFS(parentView)) {
                Class<?> viewClass = null;
                try {
                    viewClass = Class.forName(view.getClass().getName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (viewClass != null) {
                    if((trackClassesList == null || trackClassesList.contains(viewClass))
                            && View.class.isAssignableFrom(viewClass)){
                        if(view.getId() != -1 && (!trackByTag()
                                || ( view.getTag() != null && view.getTag().toString().equals("track")))) {
                            viewsList.add(view);
                            viewName.put(view, getResources().getResourceEntryName(view.getId()));
                        }
                    }
                }
            }
        }
    }

    //this will
    private List<View> getAllChildrenBFS(View v) {
        List<View> visited = new ArrayList<View>();
        List<View> unvisited = new ArrayList<View>();
        unvisited.add(v);

        while (!unvisited.isEmpty()) {
            View child = unvisited.remove(0);
            visited.add(child);
            if (!(child instanceof ViewGroup)) continue;
            ViewGroup group = (ViewGroup) child;
            final int childCount = group.getChildCount();
            for (int i=0; i<childCount; i++) unvisited.add(group.getChildAt(i));
        }

        return visited;
    }

    @Override
    protected void onPause() {
        //this will hide the keyboard to prevent from bugs
        MyUtil.hideKeyboard(this);
        //this will save each activity when it exists
        MySharedPreferences mySharedPreferences = new MySharedPreferences(this);
        mySharedPreferences.putString(MySharedPreferences.LAST_ACTIVITY_KEY, activityListener.trackActivity().getName());
        super.onPause();
        try {
            try {
                getApplicationContext().unregisterReceiver(syncFinishReceiver);
                getApplicationContext().unregisterReceiver(syncStartReceiver);
                getApplicationContext().unregisterReceiver(onlineDateReceiver);
            } catch (Exception e) {
                // Skipping issue related to unregister receiver
            }
        } catch (Exception e) {
            // Skipping issue related to unregister receiver
        }
    }

    //in case you exit the application this can open the last opened activity
    private void openLastActivity(){
        MySharedPreferences mySharedPreferences = new MySharedPreferences(this);
        String lastActivityName = mySharedPreferences.getString(MySharedPreferences.LAST_ACTIVITY_KEY, "");
        if(!lastActivityName.equals("")){
            try {
                Class<?> c = Class.forName(lastActivityName);
                Intent intent = new Intent(this, c);
                startActivity(intent);
            } catch (ClassNotFoundException ignored) {
            }
        }
    }

    //open fragment directly without need to develop it again
    public void loadFragment(Fragment fragment, Boolean addToBackState, Bundle extra) {
        loadFragment(fragment, addToBackState, true, extra);
    }

    public void loadFragment(Fragment fragment, Boolean addToBackState, boolean replace, Bundle extra) {
        FragmentUtils.get(this, null).startFragment(fragment, addToBackState, extra, replace);
    }

    public void removeFragment(Fragment fragment) {
        FragmentUtils.get(this, null).removeFragment(fragment);
    }

    //set to null in case you want to use the standard action bar
    public abstract Toolbar setToolBar();

    public App app() {
        return (App) getApplicationContext();
    }

    // API23+ Permission model helper methods
    public void setOnDevicePermissionResultListener(DevicePermissionResultListener callback) {
        mDevicePermissionResultListener = callback;
    }

    public interface DevicePermissionResultListener {
        void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (mDevicePermissionResultListener != null) {
            mDevicePermissionResultListener.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private final ISyncFinishReceiver syncFinishReceiver = new ISyncFinishReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReciver sync finished");
            Bundle data = intent.getExtras();
            if(data != null){
                String modelClassName = data.getString(ISyncFinishReceiver.MODEL_KEY);
                String username = data.getString(ISyncFinishReceiver.USERNAME_KEY);
                String authority = data.getString(ISyncFinishReceiver.AUTHORITY_KEY);
                String type = data.getString(ISyncFinishReceiver.TYPE_KEY);
                Model model = App.getModel(context, modelClassName, username);
                if(mSyncStatusObserverListener != null){
                    mSyncStatusObserverListener.onSyncFinish(data, model);
                }
            }
        }
    };

    private ISyncStartReceiver syncStartReceiver = new ISyncStartReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            if(data != null){
                String modelClassName = data.getString(ISyncStartReceiver.MODEL_KEY);
                String username = data.getString(ISyncStartReceiver.USERNAME_KEY);
                String authority = data.getString(ISyncStartReceiver.AUTHORITY_KEY);
                String type = data.getString(ISyncStartReceiver.TYPE_KEY);
                Model model = App.getModel(context, modelClassName, username);
                if(mSyncStatusObserverListener != null){
                    mSyncStatusObserverListener.onSyncStart(data, model);
                }
            }
        }
    };

    private IOnlineDateReceiver onlineDateReceiver = new IOnlineDateReceiver() {
        @Override
        public void onReceive(Context context_, Intent intent) {
            Context context = MyAppCompatActivity.this;
            Bundle data = intent.getExtras();
            if(data != null){
                boolean localDateIncorrect = data.getBoolean(LOCAL_DATE_INCORRECT);
                if(localDateIncorrect){
                    new MySharedPreferences(context).setBoolean(LOCAL_DATE_INCORRECT, true);
                    try {
                        runOnUiThread(() -> {
                            if(dateDialog != null && dateDialog.isShowing()){
                                return;
                            }
                            dateDialog = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE);
                            dateDialog.setTitleText(getString(R.string.incorrect_date))
                                    .setContentText(getString(R.string.incorrect_date_msg)).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            Log.d(TAG + "date_incorrect", "date is incorrect");
                                            MyAppCompatActivity.this.finishAffinity();
                                            System.exit(0);
                                        }
                                    });
                            dateDialog.show();
                        });
                    }catch (Exception ignored){}
                }else{
                    new MySharedPreferences(context).setBoolean(LOCAL_DATE_INCORRECT, false);
                }
            }
        }
    };

    public void sethasSyncListener(MSyncStatusObserverListener mSyncStatusObserverListener){
        this.mSyncStatusObserverListener = mSyncStatusObserverListener;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            //todo check these
            IntentFilter intentFilter = new IntentFilter(ISyncFinishReceiver.SYNC_FINISH);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getApplicationContext().registerReceiver(syncStartReceiver, new IntentFilter(IOnlineDateReceiver.ONLINE_DATE_STARTED), RECEIVER_EXPORTED);
                getApplicationContext().registerReceiver(syncStartReceiver, new IntentFilter(ISyncStartReceiver.SYNC_START), RECEIVER_EXPORTED);
                getApplicationContext().registerReceiver(syncFinishReceiver, intentFilter, RECEIVER_EXPORTED);
            } else {
                getApplicationContext().registerReceiver(syncStartReceiver, new IntentFilter(IOnlineDateReceiver.ONLINE_DATE_STARTED));
                getApplicationContext().registerReceiver(syncStartReceiver, new IntentFilter(ISyncStartReceiver.SYNC_START));
                getApplicationContext().registerReceiver(syncFinishReceiver, intentFilter);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setHasSearchView(MSearchViewChangeListener listener,
                                 Menu menu, int menu_id) {
        mSearchViewChangeListener = listener;
        mSearchView = (SearchView) menu.findItem(menu_id).getActionView();
        if (mSearchView != null) {
            mSearchView.setOnCloseListener(closeListener);
            mSearchView.setOnQueryTextListener(searchViewQueryListener);
            mSearchView.setIconifiedByDefault(true);
        }
        mSearchView.onActionViewExpanded();
    }

    private SearchView.OnCloseListener closeListener = new SearchView.OnCloseListener() {

        @Override
        public boolean onClose() {
            // Restore the SearchView if a query was entered
            if (!TextUtils.isEmpty(mSearchView.getQuery())) {
                mSearchView.setQuery(null, true);
            }
            mSearchViewChangeListener.onSearchViewClose();
            return true;
        }
    };

    private SearchView.OnQueryTextListener searchViewQueryListener = new SearchView.OnQueryTextListener() {

        public boolean onQueryTextChange(String newText) {
            String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
            return mSearchViewChangeListener
                    .onSearchViewTextChange(newFilter);
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            // Don't care about this.
            return mSearchViewChangeListener.onTextSubmit(query);
        }
    };

    @NonNull
    public Resources getLocalizedResources(Context context, Locale desiredLocale) {
        Configuration conf = context.getResources().getConfiguration();
        conf = new Configuration(conf);
        conf.setLocale(desiredLocale);
        Context localizedContext = context.createConfigurationContext(conf);
        return localizedContext.getResources();
    }

    public void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    public void showSimpleDialog(String title, String msg) {
        MyUtil.showSimpleDialog(this, title, msg);
    }

    public boolean inNetwork() {
        return app().inNetwork();
    }

    public void toggleLoading(boolean isRefreshing){
        runOnUiThread(() -> {
            if(progressDialog == null) {
                progressDialog = MyUtil.getProgressDialog(this);
            }
            if(isRefreshing) {
                progressDialog.show();
            }else if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        });
    }

    public void setToolbarTitle(String title){
        getSupportActionBar().setTitle(title);
    }

}
