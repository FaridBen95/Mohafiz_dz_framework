package com.MohafizDZ.framework_repository.service;

import android.content.Context;
import android.widget.Toast;

import com.MohafizDZ.framework_repository.core.DefaultSyncListener;
import com.MohafizDZ.framework_repository.core.Model;

import java.util.List;

public class ModelsSyncingTask {
    private Context context;
    private List<Model> modelList;
    private int index = 0;

    public ModelsSyncingTask(Context context, List<Model> modelList){
        this.context = context;
        this.modelList = modelList;
    }

    public void syncModels() {
        if(index < modelList.size()){
            Model currentModel = modelList.get(index);
            currentModel.sync(new DefaultSyncListener() {
                @Override
                public void onSyncStarted() {

                }

                @Override
                public void onSyncFinished() {
                    index ++;
                    syncModels();
                }

                @Override
                public void onSyncImagesFinished() {

                }

                @Override
                public void onSyncFailed() {
                    Toast.makeText(context, "syncing failed", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(context, "syncing successfully", Toast.LENGTH_SHORT).show();
        }
    }
}
