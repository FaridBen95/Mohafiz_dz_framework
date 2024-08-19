package com.MohafizDZ.framework_repository.service.firebase;

import android.util.Pair;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreSyncDownBridge implements FirestoreSyncDownAdapter.FirestoreListener {
    private final Model model;
    private final Integer limit;
    private final String collectionName;
    private Integer offset = 0;
    private Pair<Integer, DocumentSnapshot> offsetPair;
    private FirestoreSyncDownAdapter firestoreSyncDownAdapter;
    private final Map<String, Map<String, DataRow>> relRecords = new HashMap<>();
    private final List<DataRow> records = new ArrayList<>();
    private final SyncListener syncListener;

    public FirestoreSyncDownBridge(Model model, Integer limit, SyncListener syncListener) {
        this(model, limit, syncListener, model.getModelName());
    }
    public FirestoreSyncDownBridge(Model model, Integer limit, SyncListener syncListener, String collectionName) {
        this.model = model;
        this.collectionName = collectionName;
        this.limit = limit;
        this.syncListener = syncListener;
        init();
    }

    private void init() {
        firestoreSyncDownAdapter = new FirestoreSyncDownAdapter(limit, collectionName, model.getDocumentReference());
        firestoreSyncDownAdapter.setFirestoreListener(this);
    }

    public void syncPaging(boolean refresh){
        if(refresh){
            offset = 0;
        }
        firestoreSyncDownAdapter.sync(!refresh);
    }

    @Override
    public boolean isSyncable(Col col) {
        return syncListener.isSyncable(col);
    }

    @Override
    public void onException(Exception exception) {
        exception.printStackTrace();
        syncListener.onSyncFailed(exception);
    }

    @Override
    public final void onResult(List<Map<String, Object>> records, DocumentSnapshot lastDoc) {
        offset += records.size();
        this.offsetPair = new Pair<>(offset, lastDoc);
        handleServerResult(records);
        handleRelRecords();
    }

    private void handleRelRecords() {
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        Map<String, Pair<Col, Model>> modelNameColMap = new HashMap<>();
        for(String colName : relRecords.keySet()){
            Col col = model.getColumn(colName);
            Model relModel = model.createInstance(col.getRelationalModel());
            fillTasks(tasks, modelNameColMap, relModel, col, relRecords);
        }
        firestoreSyncDownAdapter.syncRelInTransaction(tasks, modelNameColMap);
    }

    private void fillTasks(List<Task<QuerySnapshot>> tasks, Map<String, Pair<Col, Model>> modelNameModelList, Model relModel, Col col, Map<String, Map<String, DataRow>> relRecords) {
        List<String> ids = new ArrayList<>(relRecords.get(col.getName()).keySet());
        fillTasks(tasks, modelNameModelList, relModel, col, ids, 0);
    }

    private void fillTasks(List<Task<QuerySnapshot>> tasks, Map<String, Pair<Col, Model>> modelNameModelList, Model relModel, Col col, List<String> ids, int index) {
        if(index >= ids.size()){
            return;
        }
        int endIndex = Math.min(index + 10, ids.size());
        int length = endIndex - index;
        List<String> subIds = ids.subList(index, endIndex);
        tasks.add(firestoreSyncDownAdapter.syncRel(relModel, col, subIds));
        Pair<Col, Model> colModelPair = new Pair<>(col, relModel);
        modelNameModelList.put(relModel.getModelName(), colModelPair);
        if(length != 10){
            return;
        }
        fillTasks(tasks, modelNameModelList, relModel, col, ids, endIndex);
    }

    private List<String> getSyncableColumnsList() {
        List<String> syncableColumns = new ArrayList<>();
        for(Col col : model.getRelationColumns()){
            if(isSyncable(col)){
                syncableColumns.add(col.getName());
            }
        }
        return syncableColumns;
    }

    private void handleServerResult(List<Map<String, Object>> resultList) {
        records.clear();
        List<String> syncableColumns = getSyncableColumnsList();
        List<Col> columns = model.getColumns(false);
        prepareRelRecords(syncableColumns);
        for(int i = 0; i < resultList.size(); i++) {
            Map<String, Object> recordLineMap = resultList.get(i);
            DataRow row = new DataRow();
            for (Col col : columns) {
                String colName = col.getName();
                try {
                    Object value;
                    if(col.getColumnType() != Col.ColumnType.one2many){
                        value = recordLineMap.get(colName);
                    }else{
                        value = recordLineMap.get(Col.SERVER_ID);
                    }
                    if(value == null){
                        if(col.isFunctional() && !col.isFunctionalStoreOnly()){
                            List<String> depends = col.getFunctionalDepends();
                            Values dependValues = new Values();
                            for (String depend : depends) {
                                if (recordLineMap.containsKey(depend)) {
                                    dependValues.put(depend, recordLineMap.get(depend));
                                }
                            }
                            value  = model.getFunctionalMethodValue(col, dependValues);
                        }else {
                            value = col.getDefaultValue();
                        }
                    }
                    if(col.getColumnType() != Col.ColumnType.one2many && col.getColumnType() != Col.ColumnType.array) {
                        row.put(colName, value != null? value : col.getDefaultValue());
                    }else{
                        row.putRel(colName, value != null? value : col.getDefaultValue());
                    }
                    //array will arrayList for each => put in relRecords
                    //for many to one if value is null or -1 don't put in rel
                    //one to many will be the same in input but when processing the sync if rel is one to many then sync where base_col = currentId
                    if(syncableColumns.contains(colName)) {
                        Map<String, DataRow> relMap = relRecords.get(colName);
                        if (col.getColumnType() == Col.ColumnType.many2one || col.getColumnType() == Col.ColumnType.one2many) {
                            if(value != null && !value.equals("-1") && !value.equals("0")) {
                                relMap.put(String.valueOf(value), null);
                            }
                        } else if (col.getColumnType() == Col.ColumnType.array) {
                            if (value instanceof ArrayList) {
                                for (Object object : ((ArrayList) value)) {
                                    if(object != null && !object.equals("-1") && !object.equals("0")) {
                                        relMap.put(String.valueOf(object), null);
                                    }
                                }
                            }
                        }
                        relRecords.put(colName, relMap);
                    }
                } catch (Exception ignored) {
                }
            }
            records.add(row);
        }
    }

    private void prepareRelRecords(List<String> syncableColumns) {
        relRecords.clear();
        for(String col : syncableColumns){
            relRecords.put(col, new HashMap<>());
        }
    }

    @Override
    final public DocumentSnapshot setOffset() {
        if(offsetPair != null && offsetPair.first != 0){
            return offsetPair.second;
        }
        return null;
    }

    @Override
    public List<QueryClause> setQuery() {
        return syncListener.setQuery();
    }

    @Override
    public boolean orderByWriteDate() {
        return syncListener.orderByWriteDate();
    }

    @Override
    public String setOrderByField() {
        return syncListener.orderByField();
    }

    @Override
    public final void onRelResult(Map<String, Map<String, DataRow>> resultMap) {
        List<Col> relColumns = model.getRelationColumns();
        for(Col col : relColumns) {
            for (DataRow row : records) {
                String colName = col.getName();
                try {
                    if(col.getColumnType() == Col.ColumnType.one2many) {
                        DataRow relRow = resultMap.get(colName).get(row.getString(Col.SERVER_ID));
                        List<DataRow> o2mRows = null;
                        try{
                            o2mRows = row.getRelRowList(colName);
                        }catch (Exception ignored){}
                        o2mRows = o2mRows == null? new ArrayList<>() : o2mRows;
                        if(relRow != null) {
                            o2mRows.add(relRow);
                        }
                        row.putRel(colName, o2mRows);
                    }else{
                        DataRow relRow = resultMap.get(colName).get(row.getString(colName));
                        row.putRel(colName, relRow);
                    }
                }catch (Exception ignored){ }
            }
        }
        onSyncFinished(records);
    }

    @Override
    public void onRelResult(List<Pair<String, List<Map<String, Object>>>> records, Map<String, Pair<Col, Model>> modelNameModelList) {
        Map<String, Map<String, DataRow>> resultMap = new HashMap<>();
        for(Pair<String, List<Map<String, Object>>> record : records){
            String pathName = record.first;
            Pair<Col, Model> colModelPair = modelNameModelList.get(pathName);
            Col col = colModelPair.first;
            Model model = colModelPair.second;
            Map<String, DataRow> rowMap = resultMap.containsKey(col.getName()) ?
            resultMap.get(col.getName()) : new HashMap<>();
            rowMap = rowMap == null? new HashMap<>() : rowMap;
            for(Map<String, Object> recordLine : record.second) {
                DataRow row = model.mapToRow(recordLine);
                if(col.getColumnType() == Col.ColumnType.one2many){
                    rowMap.put(row.getString(col.getRelatedColumn()), row);
                }else {
                    rowMap.put(row.getString(Col.SERVER_ID), row);
                }
            }
            if(col.getColumnType() == Col.ColumnType.one2many) {
                //todo fix sync o2m records
                resultMap.put(col.getName(), rowMap);
            }else{
                resultMap.put(col.getName(), rowMap);
            }
        }
        onRelResult(resultMap);
    }

    protected void onSyncFinished(List<DataRow> records){
        syncListener.onSyncFinished(records);
    }

    public interface SyncListener{
        void onSyncFinished(List<DataRow> records);
        boolean isSyncable(Col col);
        List<QueryClause> setQuery();
        void onSyncFailed(Exception exception);
        boolean orderByWriteDate();
        String orderByField();
    }
}
