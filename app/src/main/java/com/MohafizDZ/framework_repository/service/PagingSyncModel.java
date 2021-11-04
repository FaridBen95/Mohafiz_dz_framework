package com.MohafizDZ.framework_repository.service;

import android.content.Context;
import android.os.AsyncTask;
import com.MohafizDZ.framework_repository.core.Model;
import java.util.ArrayList;
import java.util.List;

public abstract class PagingSyncModel {
    public static final String TAG = PagingSyncModel.class.getSimpleName();
    private Context mContext;
    private final ModelHelper modelHelper;
    private final List<Integer> responseSizeList = new ArrayList<>();
    public static int globalLimit = 20;
    private int numberOfQueries;
    private List<PagingObject> pagingObjectList = null;


    public PagingSyncModel(Context context, ModelHelper modelHelper) {
        this.mContext = context;
        this.modelHelper = modelHelper;
    }

    public void sync(){
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                performSync();
                return null;
            }
        };
        task.execute();
    }

    private void performSync(){
        Model currentModel = modelHelper.getModel();
        SyncModel syncModel = initSyncModel(currentModel);
        syncModel.setModel(currentModel);
        syncModel.performSync(modelHelper.getCustomDomain(), modelHelper.isCanSyncDown(),
                modelHelper.isCanSyncDownRelations(), modelHelper.isAllowDeleteInLocal(),
                modelHelper.isAllowDeleteOnServer(), modelHelper.isCanSyncUp(),
                modelHelper.isCanSyncUpRelations(),
                modelHelper.isAllowRemoveOutOfDomain(), modelHelper.getLimit(),
                modelHelper.getLikeValue(), modelHelper.getLikeFields(),
                modelHelper.getOrderBy());
    }

    private SyncModel initSyncModel(Model model){
        return new SyncModel(mContext, model) {
            @Override
            public void onSyncStart(Model model) {
                PagingSyncModel.this.onSyncStarted();
            }

            @Override
            public void onSyncFinished(Model model) {
                PagingSyncModel.this.onSyncFinished();
            }

            @Override
            public void onSyncFailed(Model model) {
                PagingSyncModel.this.onSyncFailed();
            }

            @Override
            public boolean usePaging() {
                return true;
            }

            @Override
            public PagingObject setPagingParams(int queryIndex) {
                return pagingObjectList.get(queryIndex);
            }

            @Override
            public void getCurrentQueryLength(int currentSyncDownQueryIndex, int length) {
                responseSizeList.set(currentSyncDownQueryIndex, length);
                if(length != pagingObjectList.get(currentSyncDownQueryIndex).getLimit() &&
                        pagingObjectList.get(currentSyncDownQueryIndex).getLimit() != -1){
                    numberOfQueries--;
                    pagingObjectList.get(currentSyncDownQueryIndex).setLimit(-1);
                }
            }

            @Override
            public void syncDownQueriesSize(int size) {
                if(pagingObjectList == null){
                    numberOfQueries = size;
                    initializePaging();
                }else{
                    recalculateLimit();
                    recalculateOffset();
                }
            }
        };
    }

    private void initializePaging() {
        pagingObjectList = new ArrayList<>();
        int tookSum = 0;
        for(int i = 0; i < numberOfQueries; i++){
            ;
            int limit = Math.min(globalLimit - tookSum,(int) Math.ceil((float)globalLimit / (float)numberOfQueries));
            tookSum += limit;
            pagingObjectList.add(new PagingObject(limit, 0));
            responseSizeList.add(0);
        }
    }

    private void recalculateOffset() {
        if(pagingObjectList != null){
            for(int i=0; i < pagingObjectList.size(); i++) {
                pagingObjectList.get(i).setOffset(pagingObjectList.get(i).getOffset() + responseSizeList.get(i));
            }
        }
    }

    private void recalculateLimit() {
        int tookSum = 0;
        if(numberOfQueries != 0) {
            for (int i = 0; i < pagingObjectList.size(); i++) {
                PagingObject pagingObject = pagingObjectList.get(i);
                if (pagingObject.getLimit() != -1) {
                    int limit = Math.min(globalLimit - tookSum, (int) Math.ceil((float)globalLimit / (float)numberOfQueries));
                    tookSum += limit;
                    pagingObjectList.get(i).setLimit(limit);
                }
            }
        }else{
            for (int i = 0; i < pagingObjectList.size(); i++) {
                pagingObjectList.get(i).setLimit(-1);
            }
        }
    }

    public abstract void onSyncStarted();
    public abstract void onSyncFinished();
    public abstract void onSyncFailed();
}
