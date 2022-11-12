package com.MohafizDZ.framework_repository.core;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.MohafizDZ.App;
import com.MohafizDZ.framework_repository.local_sentry.GlobalTouchListener;
import com.MohafizDZ.framework_repository.Utils.MySharedPreferences;
import com.MohafizDZ.framework_repository.Utils.FragmentUtils;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.service.MSyncStatusObserverListener;
import com.MohafizDZ.framework_repository.service.receiver.ISyncFinishReceiver;
import com.MohafizDZ.framework_repository.service.receiver.ISyncStartReceiver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public abstract class MyAppCompatActivity extends AppCompatActivity implements ActivityListener, GlobalTouchListener {
    private String info = "No info set for this activity";
    private ActivityListener activityListener;
    public static OnChangeView onChangeView;
    protected Toolbar toolbar;
    private DevicePermissionResultListener mDevicePermissionResultListener = null;
    private MSyncStatusObserverListener mSyncStatusObserverListener;
    private MSearchViewChangeListener mSearchViewChangeListener;
    private SearchView mSearchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        MyUtil.setLocale(this, MyUtil.getCurrentLanguageLocale(getApplicationContext()));
        activityListener = this;
        info = activityListener.setInfo();
        setLocale(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String currentLanguage = MUtil.getCurrentLanguageLocale(newBase);
        Resources resources = newBase.getResources();
        resources.getConfiguration().setLocale(new Locale(currentLanguage));
        applyOverrideConfiguration(resources.getConfiguration());
        super.attachBaseContext(newBase);
    }

    public void setLocale(Context context) {
        String lang = MUtil.getCurrentLanguageLocale(context);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = new Locale(lang);
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

    //to modify anything after the creation of action bar use the method below
    public abstract void setTitleBar(ActionBar actionBar);

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

    private ISyncFinishReceiver syncFinishReceiver = new ISyncFinishReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
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

    public void sethasSyncListener(MSyncStatusObserverListener mSyncStatusObserverListener){
        this.mSyncStatusObserverListener = mSyncStatusObserverListener;
    }

    @Override
    public void onResume() {
        super.onResume();
        getApplicationContext().registerReceiver(syncStartReceiver, new IntentFilter(ISyncStartReceiver.SYNC_START));
        getApplicationContext().registerReceiver(syncFinishReceiver, new IntentFilter(ISyncFinishReceiver.SYNC_FINISH));
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
}
