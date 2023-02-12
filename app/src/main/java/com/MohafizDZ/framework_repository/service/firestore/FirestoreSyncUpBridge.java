package com.MohafizDZ.framework_repository.service.firestore;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FirestoreSyncUpBridge implements IFirestoreSync.SyncUpOutputListener, IFirestoreSync.SyncBridgeListener {
    private final Model mModel;
    private FirestoreSyncUpAdapter.Builder syncAdapterBuilder;
    private SyncListener syncListener;

    public FirestoreSyncUpBridge(Model mModel) {
        this.mModel = mModel;
        init();
    }

    private void init() {
        syncAdapterBuilder = new FirestoreSyncUpAdapter.Builder(this);
    }

    @Override
    public final void onSyncStarted() {
        if(syncListener != null){
            syncListener.onSyncStarted();
        }
    }

    @Override
    public void onSyncFinished(boolean success) {
        if(syncListener != null){
            syncListener.onSyncFinished(success);
        }
    }

    @Override
    public void onSyncFailed(Exception exception) {
        if(syncListener != null){
            syncListener.onSyncFailed(exception);
        }
    }

    @Override
    public void sync(List<DataRow> rows, SyncListener syncListener) {
        this.syncListener = syncListener;
        List<Col> serverColumns = mModel.getColumns(false);
        List<Col> relColumns = syncListener != null?
                getSyncableRelColumns(mModel.getRelationColumns(false), syncListener) :
                mModel.getRelationColumns(false);
        FirestoreSyncUpAdapter syncAdapter = prepareRelWriteBatch(relColumns, rows);
        syncAdapter = prepareBaseWriteBatch(syncAdapter, getColNames(serverColumns), rows);
        if(syncAdapter != null){
            syncAdapter.commit();
        }
    }

    private List<String> getColNames(List<Col> serverColumns) {
        List<String> colList = new ArrayList<>();
        for(Col col : serverColumns){
            colList.add(col.getName());
        }
        return colList;
    }

    private FirestoreSyncUpAdapter prepareRelWriteBatch(List<Col> relColumns, List<DataRow> rows) {
        Map<String, Model> relModels = new HashMap<>();
        if(relColumns.size() != 0 && rows.size() != 0){
            FirestoreSyncUpAdapter firestoreSyncUpAdapter = syncAdapterBuilder.startWriteBatch();
            for(DataRow row : rows){
                for(Col col : relColumns){
                    String colName = col.getName();
                    if(col.getColumnType() == Col.ColumnType.many2one) {
                        DataRow relRow = row.getRelRow(colName);
                        if (relRow == null) {
                            row = mModel.getRelations(row);
                            relRow = row.getRelRow(colName);
                        }
                        Map<String, Object> relRecordMap;
                        Class relationalModelClass = col.getRelationalModel();
                        if (!relModels.containsKey(relationalModelClass.getSimpleName())) {
                            relModels.put(relationalModelClass.getSimpleName(),
                                    mModel.createInstance(relationalModelClass));
                        }
//                        mModel.createInstance(col.getRelationalModel()).prepareUpdateRecords(relRow, 0)
                        Model relModel = relModels.get(relationalModelClass.getSimpleName());
                        long currentDateInMillis = MyUtil.dateToMilliSec(MyUtil.getCurrentDate());
                        if (relRow != null && relRow.getBoolean("synced")) {
                            relRecordMap = relModel.prepareUpdateRecords(relRow, currentDateInMillis);
                            firestoreSyncUpAdapter.addUpdate(relModel.getModelName(),
                                    relRow.getString(Col.SERVER_ID), relRecordMap);
                        } else {
                            relRecordMap = relModel.prepareInsertRecords(relRow, currentDateInMillis);
                            firestoreSyncUpAdapter.addInsertOrUpdate(relModel.getModelName(),
                                    String.valueOf(relRecordMap.get(Col.SERVER_ID)), relRecordMap);
                        }
                    }else{
                        List<String> relArrayIds = row.getRelStringList(colName);
                        if (relArrayIds == null) {
                            row = mModel.getRelations(row);
                            relArrayIds = row.getRelStringList(colName);
                        }
                        Map<String, Object> relRecordMap;
                        Class relationalModelClass = col.getRelationalModel();
                        if (!relModels.containsKey(relationalModelClass.getSimpleName())) {
                            relModels.put(relationalModelClass.getSimpleName(),
                                    mModel.createInstance(relationalModelClass));
                        }
//                        mModel.createInstance(col.getRelationalModel()).prepareUpdateRecords(relRow, 0)
                        Model relModel = null;
                        try {
                            relModel = relModels.get(relationalModelClass.getSimpleName());
                        }catch (Exception ignored){
                            continue;
                        }
                        long currentDateInMillis = MyUtil.dateToMilliSec(MyUtil.getCurrentDate());
                        String[] whereArgs = new String[relArrayIds.size()];
                        for(int i = 0 ; i < relArrayIds.size(); i++){
                            whereArgs[i] = relArrayIds.get(i);
                        }
                        List<DataRow> relRows = relModel.select(" _is_updated = 1 and " + Col.SERVER_ID +
                                " in (" + MyUtil.repeat("?, ", whereArgs.length - 1) + " ?)", whereArgs);
                        for(DataRow relRow : relRows) {
                            if (relRow != null && relRow.getBoolean("synced")) {
                                relRecordMap = relModel.prepareUpdateRecords(relRow, currentDateInMillis);
                                firestoreSyncUpAdapter.addUpdate(relModel.getModelName(),
                                        relRow.getString(Col.SERVER_ID), relRecordMap);
                            } else {
                                relRecordMap = relModel.prepareInsertRecords(relRow, currentDateInMillis);
                                firestoreSyncUpAdapter.addInsertOrUpdate(relModel.getModelName(),
                                        String.valueOf(relRecordMap.get(Col.SERVER_ID)), relRecordMap);
                            }
                        }
                    }
                }
            }
            return firestoreSyncUpAdapter;
        }
        return null;
    }

    private FirestoreSyncUpAdapter prepareBaseWriteBatch(FirestoreSyncUpAdapter syncAdapter,
                                                         List<String> serverColumns, List<DataRow> rows) {
        if(rows.size() != 0 && serverColumns.size() != 0){
            syncAdapter = syncAdapter != null ? syncAdapter : syncAdapterBuilder.startWriteBatch();
            for(DataRow row : rows){
                Map<String, Object> record;
                long currentDateInMillis = MyUtil.dateToMilliSec(MyUtil.getCurrentDate());
                if(row.getBoolean("synced")){
                    record = mModel.prepareUpdateRecords(row, currentDateInMillis, serverColumns);
                    syncAdapter.addUpdate(mModel.getModelName(), row.getString(Col.SERVER_ID), record);
                }else{
                    record = mModel.prepareInsertRecords(row, currentDateInMillis, serverColumns);
                    syncAdapter.addInsertOrUpdate(mModel.getModelName(), row.getString(Col.SERVER_ID), record);
                }
            }
        }
        return syncAdapter;
    }

    private List<Col> getSyncableRelColumns(List<Col> relationColumns, SyncListener syncListener) {
        List<Col> cols = new ArrayList<>();
        for(Col col : relationColumns){
            if(syncListener.isSyncable(col)){
                cols.add(col);
            }
        }
        return cols;
    }

    public interface SyncListener{
        void onSyncStarted();
        void onSyncFinished(boolean success);
        void onSyncFailed(Exception exception);
        boolean isSyncable(Col col);
    }
}
