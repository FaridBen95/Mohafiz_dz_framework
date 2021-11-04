package com.MohafizDZ.framework_repository.core;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.MohafizDZ.App;
import com.MohafizDZ.framework_repository.local_sentry.GlobalTouchListener;
import com.MohafizDZ.framework_repository.service.MSyncStatusObserverListener;
import com.MohafizDZ.framework_repository.service.receiver.ISyncFinishReceiver;
import com.MohafizDZ.framework_repository.service.receiver.ISyncStartReceiver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class BaseFragment extends DialogFragment implements BaseFragmentInterface, ChangingViewListener, GlobalTouchListener {
    public Context mContext;
    private ChangingViewListener changingViewListener;
    public String info;
    private View parentView;
    private ArrayList<Field> fieldsList;
    private ArrayList<Field> trackVarList;
    private List<View> viewsList;
    private HashMap<View, String> viewName = new HashMap<>();
    private MyViewGroup fragmentParentView;
    private HashMap<String, Object> currentVariables;
    public View mView;
    private SearchView mSearchView;
    private MSearchViewChangeListener mSearchViewChangeListener;
    private MSyncStatusObserverListener mSyncStatusObserverListener;

    public void setChangingViewListener(ChangingViewListener changingViewListener){
        this.changingViewListener = changingViewListener;
    }

    public void setTitle(String title) {
        getActivity().setTitle(title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        info = setInfo();
    }

    public Model db(){
        if(mContext != null){
            Class<?> model = database();
            if(model != null){
                return new Model(mContext, null).createInstance(model);
            }
        }
        return null;
    }

    public boolean inNetwork() {
        App app = (App) mContext.getApplicationContext();
        return app.inNetwork();
    }

    public void startFragment(Fragment fragment, Boolean addToBackState, Bundle extra) {
        startFragment(fragment, addToBackState, true, extra);
    }

    public void startFragment(Fragment fragment, Boolean addToBackState, boolean replace, Bundle extra) {
        if(changingViewListener != null){
            changingViewListener.openedClass(fragment.getClass());
        }
        parent().loadFragment(fragment, addToBackState, replace, extra);
    }

    public App app() {
        return (App) mContext.getApplicationContext();
    }


    public MyAppCompatActivity parent() {
        return (MyAppCompatActivity) mContext;
    }

    public abstract int setLayout();

    void onCreateView(View view){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(setLayout(), container, false);
        onCreateView(mView);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        parentView = view;
        setViewsFromXML();
        ViewGroup parentView = (ViewGroup) view.getParent();
        if(parentView != null){
            ViewGroup parentparentView = (ViewGroup) parentView.getParent();
            fragmentParentView = new MyViewGroup(getActivity());
            ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fragmentParentView.setLayoutParams(params);
            parentparentView.removeView(parentView);
            fragmentParentView.addView(parentView);
            parentparentView.addView(fragmentParentView);
            fragmentParentView.setGlobalTouchListener(this);
            if(startTrack()){
                currentVariables = new HashMap<>();
            }
        }
        super.onViewCreated(view, savedInstanceState);
    }

    private void setViewsFromXML() {
        if (startTrack()) {
            fieldsList = new ArrayList<>();
            viewsList = new ArrayList<>();
            trackVarList = new ArrayList<>();
            List<Class <?>> trackClassesList = trackByClass();
            List<String> variablesToTrack = trackVariables();
//                for (Field field : getClass().getDeclaredFields()) {
            for (View view : getAllChildrenBFS(parentView)) {
                Class<?> viewClass = null;
                try {
                    viewClass = Class.forName(view.getClass().getName());
                } catch (ClassNotFoundException e) {
                    //                    e.printStackTrace();
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
    public String ClickedOn(int x, int y) {
        if(trackVariables() != null){
            detectVariables();
        }
        StringBuilder log = new StringBuilder();
        for(View view : viewsList){
            if (view != null) {
                int start_x = fragmentParentView.getRelativeLeft(view);
                int start_y = fragmentParentView.getRelativeTop(view);
                int end_x = start_x + view.getWidth();
                int end_y = start_y + view.getHeight();
                if (x >= start_x && y >= start_y && x <= end_x && y <= end_y) {
                    log.append("\n\r").append("Clicked on View = ").append(viewName.get(view)).append(" of the class = ").append(getClass().getSimpleName());
                }
            }
        }
        log.append("Les Variables detectés : \n\r");
        for(String key : currentVariables.keySet()){
            log.append(key);
            log.append(" = ");
            log.append(currentVariables.get(key));
            log.append("\n");
        }

        return log.toString();
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
    public <T> Class<T> database() {
        return null;
    }

    @Override
    public Class<?> trackActivity() {
        return null;
    }

    @Override
    public void openedClass(Class opened) {

    }

    @Override
    public String setInfo() {
        return null;
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

    public void setHasSearchView(MSearchViewChangeListener listener,
                                 Menu menu, int menu_id) {
        mSearchViewChangeListener = listener;
        mSearchView = (SearchView) MenuItemCompat.getActionView(menu
                .findItem(menu_id));
        if (mSearchView != null) {
            mSearchView.setOnCloseListener(closeListener);
            mSearchView.setOnQueryTextListener(searchViewQueryListener);
            mSearchView.setIconifiedByDefault(true);
        }
    }

    private ISyncFinishReceiver syncFinishReceiver = new ISyncFinishReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            if(data != null){
                String modelName = data.getString(ISyncFinishReceiver.MODEL_KEY);
                String username = data.getString(ISyncFinishReceiver.USERNAME_KEY);
                String authority = data.getString(ISyncFinishReceiver.AUTHORITY_KEY);
                String type = data.getString(ISyncFinishReceiver.TYPE_KEY);
                try {
                    Model model = Model.createInstance(context, Class.forName(modelName));
                    if(mSyncStatusObserverListener != null){
                        mSyncStatusObserverListener.onSyncFinish(data, model);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private ISyncStartReceiver syncStartReceiver = new ISyncStartReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            if(data != null){
                String modelName = data.getString(ISyncStartReceiver.MODEL_KEY);
                String username = data.getString(ISyncStartReceiver.USERNAME_KEY);
                String authority = data.getString(ISyncStartReceiver.AUTHORITY_KEY);
                String type = data.getString(ISyncStartReceiver.TYPE_KEY);
                try {
                    Model model = Model.createInstance(context, Class.forName(modelName));
                    if(mSyncStatusObserverListener != null){
                        mSyncStatusObserverListener.onSyncStart(data, model);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
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
        parent().registerReceiver(syncStartReceiver, new IntentFilter(ISyncStartReceiver.SYNC_START));
        parent().registerReceiver(syncFinishReceiver, new IntentFilter(ISyncFinishReceiver.SYNC_FINISH));
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            parent().unregisterReceiver(syncFinishReceiver);
            parent().unregisterReceiver(syncStartReceiver);
        } catch (Exception e) {
            // Skipping issue related to unregister receiver
        }
    }
}
