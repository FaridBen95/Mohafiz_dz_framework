package com.MohafizDZ.framework_repository.service;

import android.content.Context;
import android.os.AsyncTask;

import com.MohafizDZ.framework_repository.core.Model;

import java.util.List;

public class SyncModels {
    private final Context context;
    private int index;
    private List<ModelHelper> modelList;
    private SyncModelsListener listener;

    public SyncModels(Context context, List<ModelHelper> modelList){
        this.modelList = modelList;
        this.context = context;
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

    public void performSync(){{
        if(index == 0 && listener != null){
            listener.onSyncStarted();
        }
        if(index < modelList.size()){
            ModelHelper currentModelHelper = modelList.get(index);
            Model currentModel = currentModelHelper.getModel();
            SyncModel syncModel = initSyncModel(currentModel);
            syncModel.setModel(currentModelHelper.getModel());
            syncModel.performSync(currentModelHelper.getCustomDomain(), currentModelHelper.isCanSyncDown(),
                    currentModelHelper.isCanSyncDownRelations(), currentModelHelper.isAllowDeleteInLocal(),
                    currentModelHelper.isAllowDeleteOnServer(), currentModelHelper.isCanSyncUp(),
                    currentModelHelper.isCanSyncUpRelations(),
                    currentModelHelper.isAllowRemoveOutOfDomain(), currentModelHelper.getLimit(),
                    currentModelHelper.getLikeValue(), currentModelHelper.getLikeFields(),
                    currentModelHelper.getOrderBy());
        }else if (listener != null){
            listener.onSyncFinished();
        }
    }
    }

    public SyncModel initSyncModel(Model model){
        return new SyncModel(context, model) {
            @Override
            public void onSyncStart(Model model) {
                if(listener != null){
                    listener.onSyncStart(model);
                }
            }

            @Override
            public void onSyncFinished(Model model) {
                if(listener != null){
                    listener.onSyncFinished(model);
                }
                index++;
                SyncModels.this.performSync();
            }

            @Override
            public void onSyncFailed(Model model) {
                if(listener != null){
                    listener.onSyncFailed(model);
                }
            }
        };
    }


    public SyncModels setListener(SyncModelsListener listener) {
        this.listener = listener;
        return this;
    }

    public interface SyncModelsListener{
        void onSyncStarted();
        void onSyncFinished();
        void onSyncStart(Model model);
        void onSyncFinished(Model model);
        void onSyncFailed(Model model);
    }
}
