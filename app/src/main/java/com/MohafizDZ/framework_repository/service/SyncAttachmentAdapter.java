package com.MohafizDZ.framework_repository.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;

import androidx.annotation.NonNull;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.ExecuteOnCaller;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.DefaultSyncListener;
import com.MohafizDZ.framework_repository.core.Values;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class SyncAttachmentAdapter extends MAbstractThreadedSyncAdapter {
    public final String TAG = SyncAttachmentAdapter.class.getSimpleName();
    public final String FIREBASE_STORAGE_LINK = "attachment";

    private Context context;
    protected Model model;
    private FirebaseStorage firebaseStorage;
    List<Map<String, String>> attachmentsMapToDownload = new ArrayList<>();
    List<Map<String, String>> attachmentsMapToUpload = new ArrayList<>();
    private StorageReference storageReference;
    private DefaultSyncListener defaultSyncListener;
    private CountDownLatch doneSignal = new CountDownLatch(1);

    public SyncAttachmentAdapter(Context context, Class<? extends Model> modelClass){
        this(context, modelClass, false);
    }

    public SyncAttachmentAdapter(Context context, Class<? extends Model> modelClass, boolean autoInitialize){
        super(context, autoInitialize);
        init(context, modelClass);
    }

    public SyncAttachmentAdapter(Context context, Class<? extends Model> modelClass,
                                 boolean autoInitialize, boolean allowParallelSyncs){
        super(context, autoInitialize, allowParallelSyncs);
        init(context, modelClass);
    }

    private void init(Context context, Class<? extends Model> modelClass){
        this.context = context;
        this.model = Model.createInstance(context, modelClass);
        this.firebaseStorage = FirebaseStorageSingleton.get();
        this.storageReference = firebaseStorage.getReference(FIREBASE_STORAGE_LINK);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        super.onPerformSync(account, extras, authority, provider, syncResult);
        Looper.prepare();
        setExecutor(new ExecuteOnCaller());
        performSync();
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void performSync() {
        if(model.isOnline()){
            prepareAttachmentDatas();
            syncDown();
        }
    }

    private void syncDown() {
        new SyncDown().sync();
    }

    private void prepareAttachmentDatas() {
        List<Col> columns = model.getColumns(false);
        String downloadSelection = generateAttachmentSearchQuery(true, columns);
        if(!downloadSelection.equals("")) {
            List<DataRow> rows = model.getRows(downloadSelection.substring(0, downloadSelection.lastIndexOf("or ")), new String[]{});
            for (DataRow row : rows) {
                for (String attachmentCol : model.getAttachmentCols()) {
                    String attachmentSyncCol = attachmentCol + "_saved_in_local";
                    if (row.getInteger(attachmentSyncCol) == 0) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("col", attachmentCol);
                        map.put("path", row.getString(attachmentCol));
                        map.put(Col.SERVER_ID, row.getString(Col.SERVER_ID));
                        attachmentsMapToDownload.add(map);
                    }
                }
            }
        }
        String uploadSelection = generateAttachmentSearchQuery(false, columns);
        if(!uploadSelection.equals("")) {
            List<DataRow> rows = model.getRows(uploadSelection.substring(0, uploadSelection.lastIndexOf("or ")), new String[]{});
            for (DataRow row : rows) {
                for (String attachmentCol : model.getAttachmentCols()) {
                    String attachmentSyncCol = attachmentCol + "_saved_in_server";
                    if (row.getInteger(attachmentSyncCol) == 0) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("col", attachmentCol);
                        map.put("path", row.getString(attachmentCol));
                        map.put(Col.SERVER_ID, row.getString(Col.SERVER_ID));
                        attachmentsMapToUpload.add(map);
                    }
                }
            }
        }
    }

    private String generateAttachmentSearchQuery(boolean local, List<Col> columns) {
        String savedIn = local? "_saved_in_local" :"_saved_in_server";
        StringBuilder selection = new StringBuilder("");
        for(Col col : columns){
            if(col.getColumnType().equals(Col.ColumnType.attachement)){
                selection.append("");
                String attachmentSyncColName = col.getName()+savedIn;
                selection.append(attachmentSyncColName);
                selection.append(" = '0' ");
                selection.append("or ");
            }
        }
        return selection.toString();
    }

    private void syncUp() {
        new SyncUp().sync();
    }

    @Override
    public Model onSetModel() {
        return model;
    }

    public class SyncDown implements OnCompleteListener<FileDownloadTask.TaskSnapshot> {
        private int index;

        private void sync(){
            if(index < attachmentsMapToDownload.size()){
                Map<String, String> currentAttachmentMap = attachmentsMapToDownload.get(index);
                String path = currentAttachmentMap.get("path");
                File file = new File(context.getCacheDir(), path);
                storageReference.child(path).getFile(file).
                        addOnCompleteListener(this);
            }else{
                onSyncDownDone();
            }
        }

        @Override
        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
            if(task.isSuccessful()){
                Map<String, String> currentAttachmentMap = attachmentsMapToDownload.get(index);
                final String serverId = currentAttachmentMap.get(Col.SERVER_ID);
                final String column = currentAttachmentMap.get("col");
                Values values = new Values();
                values.put(column+ "_saved_in_local", 1);
                model.update(serverId, values, true);
            }
            index++;
            sync();
        }
    }

    public class SyncUp implements OnCompleteListener<UploadTask.TaskSnapshot> {
        private int index;

        private void sync(){
            if(index < attachmentsMapToUpload.size()){
                Map<String, String> currentAttachmentMap = attachmentsMapToUpload.get(index);
                String path = currentAttachmentMap.get("path");
                Uri uri = Uri.fromFile(new File(context.getCacheDir().getPath(),
                        path));
                StorageReference fireStorageRef = storageReference.child(path);
                fireStorageRef.putFile(uri).addOnCompleteListener(this);
            }else{
                onSyncUpDone();
            }
        }

        @Override
        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
            if(task.isSuccessful()){
                Map<String, String> currentAttachmentMap = attachmentsMapToUpload.get(index);
                Values values = new Values();
                String serverId = currentAttachmentMap.get(Col.SERVER_ID);
                String attachmentCol = currentAttachmentMap.get("col");
                values.put(attachmentCol + "_saved_in_server", 1);
                model.update(serverId, values, true);
            }
            index++;
            sync();
        }
    }

    public void onSyncDownDone(){
        syncUp();
    }

    private void onSyncUpDone() {
        onSyncFinished();
    }

    protected void onSyncFinished() {
        model.onSyncImagesFinished();
        if(defaultSyncListener != null){
            defaultSyncListener.onSyncImagesFinished();
        }
        notifyDataChange(model);
        doneSignal.countDown();
    }

    public Model getModel() {
        return model;
    }
}
