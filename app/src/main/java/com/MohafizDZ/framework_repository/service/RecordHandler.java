package com.MohafizDZ.framework_repository.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public abstract class RecordHandler {
    public static final String TAG = RecordHandler.class.getSimpleName();
    private final Context mContext;
    private List<DataRow> localRows;
    private Model model;
    private List<Values> toUpdateValues;
    private List<Values> toInsertValues;
    private List<DataRow> rowsToDelete;
    private Map<String, List<String>> manyToOneIdsMap;
    private Map<String, List<String>> oneToManyIdsMap;
    private Map<Model, Map<String, Object>> relRecords;
    private List<String> idsToUpdateOnServer;
    private Map<String, String> WriteDateOfRecordsToUpdateOnServer;
    public String savingDate;
    private String updatedDate;
    private Map<String, Map<String, Set<String>>> relValues;
    private int currentSyncDownQueryIndex;
    private long serverUpdateDate;

    public RecordHandler(Context mContext, Model model){
        this.model = model;
        this.mContext = mContext;
        init();
    }

    private void init() {
        localRows = model.getRows(" _is_active = ? ", new String[]{"1"});
        toUpdateValues = new ArrayList<>();
        toInsertValues = new ArrayList<>();
        idsToUpdateOnServer = new ArrayList<>();
        manyToOneIdsMap = new HashMap<>();
        oneToManyIdsMap = new HashMap<>();
        relRecords = new HashMap<>();
        WriteDateOfRecordsToUpdateOnServer = new HashMap<>();
        relValues = new HashMap<>();
    }

    protected void reset(){
        localRows = model.getRows(" _is_active = ? ", new String[]{"1"});
        if(toUpdateValues != null) {
            toUpdateValues.clear();
        }
        if(toInsertValues != null){
            toInsertValues.clear();
        }
        if(relValues != null){
            relValues.clear();
        }
    }

    protected void resetDeletion(){
        rowsToDelete.clear();
    }

    private void handleExistingRecord(Map<String, Object> serverDocument, DataRow localRow,
                                      String currentDate) {
        String serverId = String.valueOf(serverDocument.get(Col.SERVER_ID));
//        Date _write_date_obj = MyUtil.createDateObject(localRow.get("_write_date").toString(),
//                MyUtil.DEFAULT_DATE_FORMAT, false);
        String write_date = MyUtil.milliSecToDate(Long.valueOf(
                serverDocument.get("write_date").toString()));
        long _write_date_inMillis = MyUtil.dateToMilliSec(String.valueOf(localRow.get("_write_date")));
        long write_date_inMillis = Long.valueOf(String.valueOf(serverDocument.get("write_date")));
        serverDocument.put("write_date", write_date);
        long create_date = 0;
        try{
            create_date = Long.valueOf(serverDocument.get("create_date").toString());
        }catch (Exception ignored){}
        serverDocument.put("create_date", MyUtil.milliSecToDate(create_date));
//        Date write_date_obj = MyUtil.createDateObject(write_date,
//                MyUtil.DEFAULT_DATE_FORMAT, false);
        if (write_date_inMillis > _write_date_inMillis  || model.forceOverwriteOnLocal()) {
            Values values = model.recordToValues(serverDocument);
            values.put("id", serverId);
            values.put("_write_date", currentDate);
            values.put("_is_updated", 0);
            values.put("synced", 1);
            values =  prepareUpdateValues(values);
            collectSyncDownRelationIds(values);
            toUpdateValues.add(values);
        }
    }

    protected abstract Values prepareUpdateValues(Values values);

    private void handleNewRecords(Map<String, Object> serverDocument, String currentDate) {
        Values values = model.recordToValues(serverDocument);
        values.put("id", serverDocument.get(Col.SERVER_ID));
        values.put("_write_date", currentDate);
        values.put("_create_date", currentDate);
        values.put("synced", 1);
        values.put("_is_active", 1);
        values.put("removed", 0);
        values.put("_is_updated", 0);
        values = prepareInsertValues(values);
        collectSyncDownRelationIds(values);
        toInsertValues.add(values);
    }

    private void collectSyncDownRelationIds(Values values) {
        for(Col col : model.getRelationColumns()){
            if(col.canSyncRelations()) {
                if (col.getColumnType().equals(Col.ColumnType.one2many)) {
                    Object val = values.get(Col.SERVER_ID);
                    if (val != null && !val.equals("false") && !val.equals("")) {
                        List<String> oneToManyIds = oneToManyIdsMap.get(col.getName());
                        oneToManyIds = oneToManyIds != null ? oneToManyIds : new ArrayList<String>();
                        if (!oneToManyIds.contains(val)) {
                            oneToManyIds.add(String.valueOf(val));
                            oneToManyIdsMap.put(col.getName(), oneToManyIds);
                        }
                    }
                } else {
                    Object val = values.get(col.getName());
                    if (col.getColumnType().equals(Col.ColumnType.many2one)) {
                        if (val != null && !val.equals("false") && !val.equals("")) {
                            List<String> manyToOneIds = manyToOneIdsMap.get(col.getName());
                            manyToOneIds = manyToOneIds != null ? manyToOneIds : new ArrayList<String>();
                            if (!manyToOneIds.contains(val)) {
                                manyToOneIds.add(String.valueOf(val));
                                manyToOneIdsMap.put(col.getName(), manyToOneIds);
                            }
                        }
                    } else {
                        if (val != null && !val.equals("false") && !val.equals("")) {
                            HashSet<String> relIds = (HashSet<String>) val;
                            List<String> manyToOneIds = manyToOneIdsMap.get(col.getName());
                            manyToOneIds = manyToOneIds != null ? manyToOneIds : new ArrayList<String>();
                            manyToOneIds.addAll(relIds);
                            manyToOneIdsMap.put(col.getName(), manyToOneIds);
                        }
                    }
                }
            }
        }
    }

    protected abstract Values prepareInsertValues(Values values);

    private void performInsert() {
        String insertQuery = insertValues(toInsertValues);
        if(!insertQuery.equals("")){
            SQLiteDatabase database = model.getWritableDatabase();
            database.execSQL(insertQuery);
            model.onDatabaseModified();
        }
    }

    private String insertValues(List<Values> toInsertValues) {
        if(toInsertValues.size() != 0) {
            List<Col> columns = model.getColumns();
            StringBuilder insertSQL = new StringBuilder(" INSERT OR IGNORE INTO  '");
            insertSQL.append(model.getModelName());
            insertSQL.append("' (");
            for (Col col : columns) {
                if(!col.getName().equals(Col.ROWID) && !col.getColumnType().equals(Col.ColumnType.one2many) &&
                        !col.getColumnType().equals(Col.ColumnType.array)) {
                    insertSQL.append("'");
                    insertSQL.append(col.getName());
                    insertSQL.append("', ");
                }
            }
            insertSQL.deleteCharAt(insertSQL.lastIndexOf(","));
            insertSQL.append(") VALUES ");
            insertSQL.append("\n");
            for(Values values : toInsertValues) {
                insertSQL.append("(");
                for (Col col : columns) {
                    if(!col.getName().equals(Col.ROWID) && !col.getColumnType().equals(Col.ColumnType.one2many) &&
                            !col.getColumnType().equals(Col.ColumnType.array)) {
                        Object object = values.get(col.getName());
                        if(object.toString().contains("'")){
                            insertSQL.append('"');
                        }else {
                            insertSQL.append("'");
                        }
                        insertSQL.append(values.get(col.getName()));
                        if(object.toString().contains("'")){
                            insertSQL.append('"');
                        }else {
                            insertSQL.append("'");
                        }
                        insertSQL.append(", ");
                    }
                    if(col.getColumnType().equals(Col.ColumnType.array)){
                        Map<String, Set<String>> _relValues = relValues.containsKey(col.getName())?
                                relValues.get(col.getName()) : new HashMap<String, Set<String>>();
                        if((Set)values.get(col.getName()) != null) {
                            _relValues.put((String) values.get(Col.SERVER_ID), (Set) values.get(col.getName()));
                            if (_relValues != null) {
                                relValues.put(col.getName(), _relValues);
                            }
                        }
                    }
                }
                insertSQL.deleteCharAt(insertSQL.lastIndexOf(","));
                insertSQL.append("), \n");
            }
            insertSQL.deleteCharAt(insertSQL.lastIndexOf(","));
            insertSQL.append(";");
            return insertSQL.toString();
        }
        return "";
    }

    private void performUpdate() {
        String updateQuery = updateValues2(model, toUpdateValues);
        if(!updateQuery.equals("")){
            SQLiteDatabase database = model.getWritableDatabase();
            for(String sql : updateQuery.split(";\n")) {
                database.execSQL(sql);
            }
        }
        model.onDatabaseModified();
    }

    private String updateValues(List<Values> toUpdateValues) {
        if(toUpdateValues.size() != 0) {
            List<Col> serverColumns = model.getColumns(false);
            StringBuilder updateSQL = new StringBuilder("UPDATE ");
            updateSQL.append(model.getModelName());
            updateSQL.append("\n");
            updateSQL.append("SET ");
            for (Col col : serverColumns) {
                if (!col.getColumnType().equals(Col.ColumnType.one2many) && !col.getColumnType().equals(Col.ColumnType.array) && !col.getName().equals(Col.SERVER_ID)) {
                    updateSQL.append(col.getName());
                    updateSQL.append("= CASE \n");
                    for (Values values : toUpdateValues) {
                        Object object = values.get(col.getName());
                        if (!col.getColumnType().equals(Col.ColumnType.array)) {
                            updateSQL.append("WHEN id = '");
                            updateSQL.append(values.get(Col.SERVER_ID));
                            updateSQL.append("' THEN ");
                            if (col.getColumnType().equals(Col.ColumnType.text) ||
                                    col.getColumnType().equals(Col.ColumnType.low_quality_image) ||
                                    col.getColumnType().equals(Col.ColumnType.attachement) ||
                                    col.getColumnType().equals(Col.ColumnType.many2one) ||
                                    col.getColumnType().equals(Col.ColumnType.varchar)) {
                                if (object.toString().contains("'")) {
                                    updateSQL.append('"');
                                } else {
                                    updateSQL.append("'");
                                }
                            }
                            updateSQL.append(object);
                            if (col.getColumnType().equals(Col.ColumnType.text) ||
                                    col.getColumnType().equals(Col.ColumnType.low_quality_image) ||
                                    col.getColumnType().equals(Col.ColumnType.attachement) ||
                                    col.getColumnType().equals(Col.ColumnType.many2one) ||
                                    col.getColumnType().equals(Col.ColumnType.varchar)) {
                                if (object.toString().contains("'")) {
                                    updateSQL.append('"');
                                } else {
                                    updateSQL.append("'");
                                }
                            }
                            updateSQL.append("\n");
                        }else{
                            Map<String, Set<String>> _relValues = relValues.containsKey(col.getName())?
                                    relValues.get(col.getName()) : new HashMap<String, Set<String>>();
                            if((Set)values.get(col.getName()) != null){
                                _relValues.put((String)values.get(Col.SERVER_ID), (Set)values.get(col.getName()));
                                if(_relValues != null){
                                    relValues.put(col.getName(), _relValues);
                                }
                            }
                        }
                    }
                    updateSQL.append("END, ");
                    updateSQL.append("\n");
                }
            }
            int lastIndex = updateSQL.lastIndexOf(",");
            if(lastIndex != -1) {
                updateSQL.deleteCharAt(lastIndex);
            }
            return updateSQL.toString();
        }
        return "";
    }

    private String updateValues2(Model model, List<Values> toUpdateValues) {
        if(toUpdateValues.size() != 0) {
            List<Col> serverColumns = model.getColumns(false);
            StringBuilder updateSQL = new StringBuilder("");
            String modelName = model.getModelName();
            for(Values values : toUpdateValues){
                updateSQL.append("UPDATE ");
                updateSQL.append(modelName);
                updateSQL.append("\n");
                updateSQL.append("SET ");
                for(Col col : serverColumns){
                    if (!col.getColumnType().equals(Col.ColumnType.one2many) && !col.getColumnType().equals(Col.ColumnType.array)
                            && !col.getName().equals(Col.SERVER_ID)) {
                        updateSQL.append(col.getName()).append(" = ");
                        Object object = values.get(col.getName());
                        if (col.getColumnType().equals(Col.ColumnType.text) ||
                                col.getColumnType().equals(Col.ColumnType.low_quality_image) ||
                                col.getColumnType().equals(Col.ColumnType.attachement) ||
                                col.getColumnType().equals(Col.ColumnType.many2one) ||
                                col.getColumnType().equals(Col.ColumnType.varchar)) {
                            if(object != null && object.toString().contains("'")){
                                updateSQL.append('"');
                            }else {
                                updateSQL.append("'");
                            }
                        }if(col.getColumnType().equals(Col.ColumnType.bool) &&
                                object instanceof Boolean){
                            boolean v = (boolean) object;
                            object = v? 1 : 0;
                        }
                        updateSQL.append(object);
                        if (col.getColumnType().equals(Col.ColumnType.text) ||
                                col.getColumnType().equals(Col.ColumnType.low_quality_image) ||
                                col.getColumnType().equals(Col.ColumnType.attachement) ||
                                col.getColumnType().equals(Col.ColumnType.many2one) ||
                                col.getColumnType().equals(Col.ColumnType.varchar)) {
                            if(object != null && object.toString().contains("'")){
                                updateSQL.append('"');
                            }else {
                                updateSQL.append("'");
                            }
                        }
                        updateSQL.append(",");
                    }
                    if(col.getColumnType().equals(Col.ColumnType.array)){
                        Map<String, Set<String>> _relValues = relValues.containsKey(col.getName())?
                                relValues.get(col.getName()) : new HashMap<String, Set<String>>();
                        Set<String> list = values.get(col.getName()) != null && !values.get(col.getName()).toString().equals("")?
                                (Set)values.get(col.getName()) : new HashSet<>();
                        if(list != null) {
                            _relValues.put((String) values.get(Col.SERVER_ID), list);
                            if (_relValues != null) {
                                relValues.put(col.getName(), _relValues);
                            }
                        }
                    }
                }
                int lastIndex = updateSQL.lastIndexOf(",");
                if(lastIndex != -1) {
                    updateSQL.deleteCharAt(lastIndex);
                }
                updateSQL.append(" WHERE ").append(Col.SERVER_ID).append(" = '");
                updateSQL.append(values.get(Col.SERVER_ID)).append("'").append(";\n");
            }
            return updateSQL.toString();
        }
        return "";
    }

    public void handleDeletion(JSONArray response) {
        for(int i = 0; i < response.length(); i++) {
            try {
                JSONObject lineObject = response.getJSONObject(i);
                JSONObject document = lineObject.getJSONObject("document");
                JSONObject fields = document.getJSONObject("fields");
                JSONObject fieldObject = fields.getJSONObject("where");
                String where = fieldObject.getString("stringValue");
                model.delete(where, new String[]{}, true);
            }catch (Exception ignored){}
        }
    }

    public void saveSyncingDate(String modelName, String currentDate) {
        Values values = new Values();
        values.put("model_name", modelName);
        values.put("last_sync_date", currentDate);
        SyncingReport syncingReport = new SyncingReport(mContext);
        DataRow row = syncingReport.browse("model_name = ? ",
                new String[]{modelName});
        if(row != null){
            syncingReport.update(row.getInteger(Col.ROWID), values);
        }else{
            syncingReport.insert(values);
        }
    }

    public void recordsUpdatedOnServer(List<String> updatedServerIds) {
        Log.d(TAG, "recordsUpdatedOnServer");
        String[] whereArgs = new String[updatedServerIds.size() + 1];
        whereArgs[0] = updatedDate;
        for(int i = 1 ; i <= updatedServerIds.size(); i++){
            whereArgs[i] = updatedServerIds.get(i - 1);
        }
        Values values = new Values();
        values.put("synced", 1);
        values.put("_is_updated", 0);
        values.put("_write_date", updatedDate);
        values.put("write_date", serverUpdateDate);
        if(whereArgs.length != 0) {
//            model.select("create_date is not null and " + Col.SERVER_ID +
//                    " in (" + MyUtil.repeat("?, ", whereArgs.length - 2) + " ?)",
//                    new String[]{whereArgs[1]})
            model.update(values, " create_date is not null and  _write_date <= ? and " + Col.SERVER_ID +
                    " in (" + MyUtil.repeat("?, ", whereArgs.length - 2) + " ?)", whereArgs);
            values.put("create_date", serverUpdateDate);
            model.update(values, " create_date is null and _write_date <= ? and " + Col.SERVER_ID +
                    " in (" + MyUtil.repeat("?, ", whereArgs.length - 2) + " ?)", whereArgs);
        }
    }

    public void collectResponses(JSONArray response) {
        try {
            localRows = model.getRows("", null);
            final Map<String, Object> localRowsMap = MyUtil.rowsToMap(localRows);
            final String currentDate = MyUtil.getCurrentDate();
            for(int i = 0; i < response.length(); i++){
                JSONObject lineObject = response.getJSONObject(i);
                JSONObject document;
                try {
                    document = lineObject.getJSONObject("document");
                }catch (Exception ignored){
                    continue;
                }
                JSONObject fields = document.getJSONObject("fields");
                Map<String, Object> lineMap = new TreeMap<>();
                for (Iterator<String> it = fields.keys(); it.hasNext(); ) {
                    String key = it.next();
                    JSONObject fieldObject = fields.getJSONObject(key);
                    Object val = getFieldValue(fieldObject);
                    lineMap.put(key, val);
                }
                String serverId = String.valueOf(lineMap.get(Col.SERVER_ID));
                if (localRowsMap.containsKey(serverId)) {
                    DataRow row = (DataRow) localRowsMap.get(serverId);
                    handleExistingRecord(lineMap, row, currentDate);
                } else {
                    handleNewRecords(lineMap, currentDate);
                }
            }
            getCurrentQueryLength(currentSyncDownQueryIndex, response.length());
        } catch (JSONException e) {
            e.printStackTrace();
            getCurrentQueryLength(currentSyncDownQueryIndex, 0);
        }
    }

    protected abstract void getCurrentQueryLength(int currentSyncDownQueryIndex, int length);

    private Object getFieldValue(JSONObject fieldObject) {
        Object val = null;
        try{
            val = fieldObject.getString("stringValue");
        }catch (Exception ignored){}
        if(val == null){
            try{
                val = fieldObject.getLong("integerValue");
            }catch (Exception ignored){}
        }
        if(val == null){
            try{
                val = fieldObject.getBoolean("booleanValue");
            }catch (Exception ignored){}
        }
        if(val == null){
            try{
                val = fieldObject.getDouble("doubleValue");
            }catch (Exception ignored){}
        }
        if(val == null){
            try{
                val = fieldObject.get("timestampValue");
            }catch (Exception ignored){}
        }
        if(val == null){
            try{
                val = fieldObject.getJSONObject("arrayValue").getJSONArray("values");
            }catch (Exception ignored){}
        }
        return val;
    }

    void updateInLocal() {
        savingDate = MyUtil.getCurrentDate();
        performInsert();
        performUpdate();
        updateRelTable(model, relValues);
    }

    private void updateRelTable(Model model, Map<String, Map<String, Set<String>>> relValues) {
        for(String relField : relValues.keySet()){
            Map<String, Set<String>> _relValues = relValues.get(relField);
            emptyTable(model, relField, _relValues.keySet());
            insertRelValues(model, relField, _relValues);
        }
    }

    private void emptyTable(Model model, String relField, Set<String> keySet) {
        SQLiteDatabase db = model.getWritableDatabase();
        Col col = model.getColumn(relField);
        String relTableName = model.getArrayRelTableName(col);
        String[] ids = new String[keySet.size()];
        Iterator<String> setIterator = keySet.iterator();
        int i=0;
        while(setIterator.hasNext()){
            ids[i] = setIterator.next();
            i++;
        }
        String selection = "base_col_id in (" + MyUtil.repeat("?, ", ids.length - 1) + " ?)";
        int deletedNum = db.delete(relTableName, selection, ids);
        deletedNum++;
    }

    private void insertRelValues(Model model, String relField, Map<String, Set<String>> relValues) {
        if(relValues.size() != 0) {
            boolean blockInsert = true;
            Col col = model.getColumn(relField);
            String relTableName = model.getArrayRelTableName(col);
            StringBuilder sql = new StringBuilder();
            sql.append(" INSERT OR IGNORE INTO ");
            sql.append(relTableName);
            sql.append(" (");
            sql.append("base_col_id");
            sql.append(", rel_col");
            sql.append(")");
            sql.append(" VALUES");
            for(String baseServerId : relValues.keySet()){
                for (String valueServerId : relValues.get(baseServerId)) {
                    blockInsert = false;
                    sql.append("(");
                    sql.append("'");
                    sql.append(baseServerId);
                    sql.append("'");
                    sql.append(", ");
                    sql.append("'");
                    sql.append(valueServerId);
                    sql.append("'");
                    sql.append("),");
                }
            }
            sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(";");
            if(!blockInsert) {
                SQLiteDatabase db = model.getWritableDatabase();
                db.execSQL(sql.toString());
            }
        }
    }

    public String getDeleteArgs() {
        rowsToDelete = model.getRows(" _is_active = ? and removed = ? ",
                new String[]{"1", "1"});
        if(rowsToDelete.size() != 0) {
            String[] idsToDelete = new String[rowsToDelete.size()];
            StringBuilder argsString = new StringBuilder();
            argsString.append(Col.SERVER_ID);
            argsString.append(" in ( ");
            for (int i = 0; i < rowsToDelete.size(); i++) {
                DataRow row = rowsToDelete.get(i);
                idsToDelete[i] = row.getString(Col.SERVER_ID);
                argsString.append(" '");
                argsString.append(idsToDelete[i]);
                argsString.append("'");
                argsString.append(",");
            }
            argsString.deleteCharAt(argsString.lastIndexOf(","));
            argsString.append(") ");
            return argsString.toString();
        }
        return null;
    }

    public List<DataRow> getRowsToDelete() {
        return rowsToDelete;
    }

    public Map<String, Map<String, Object>> prepareSyncUpRecords() {
        Log.d(TAG, "performSyncUp");
        SyncUpCondition syncUpCondition = model.getUpdateOnServerCondition();
        String insertSelection = "removed = ? and _is_active = ? and " + Col.SYNCED +
                " = ? ";
        String updateSelection = "_is_active = ? and " +
                Col.SYNCED + " = ? and _is_updated = ? ";
        insertSelection = syncUpCondition == null? insertSelection: insertSelection + " and ( " +
                syncUpCondition.selection + ")";
        updateSelection = syncUpCondition == null? updateSelection: updateSelection + " and ( " +
                syncUpCondition.selection + ")";
        String[] insertArgs = {"0", "1","0"};
        String[] updateArgs = {"1", "1", "1"};
        if(syncUpCondition != null) {
            insertArgs = MyUtil.addArgs(insertArgs, syncUpCondition.args);
            updateArgs = MyUtil.addArgs(updateArgs, syncUpCondition.args);
        }
        final List<DataRow> newRows = model.getRows(insertSelection , insertArgs);
        final List<DataRow> updateRows = model.getRows(updateSelection, updateArgs);
        Map<String, Map<String, Object>> updatedRecords = new HashMap<>();
        idsToUpdateOnServer.clear();
        for(DataRow row : newRows) {
            Map<String, Object> insertRecord = model.prepareInsertRecords(row, serverUpdateDate);
//            collectSyncUpRelations(insertRecord);
            updatedRecords.put(row.getString(Col.SERVER_ID), insertRecord);
        }
        for(DataRow row : updateRows){
            Map<String, Object> updateRecord = model.prepareUpdateRecords(row, serverUpdateDate);
//            collectSyncUpRelations(updateRecord);
            if(!idsToUpdateOnServer.contains(row.getString(Col.SERVER_ID))){
                idsToUpdateOnServer.add(row.getString(Col.SERVER_ID));
            }
            WriteDateOfRecordsToUpdateOnServer.put(row.getString(Col.SERVER_ID), row.getString("_write_date"));
            updatedRecords.put(row.getString(Col.SERVER_ID), updateRecord);
        }
        return updatedRecords;
    }

    /*private void collectSyncUpRelations(Map<String, Object> records) {
        for(Col col : model.getRelationColumns()){
            if(col.getColumnType().equals(Col.ColumnType.one2many)){
                Object val = values.get(Col.SERVER_ID);
                if(val != null && !val.equals("false") && !val.equals("")) {
                    List<String> oneToManyIds = oneToManyIdsMap.get(col.getName());
                    oneToManyIds = oneToManyIds != null ? oneToManyIds : new ArrayList<String>();
                    if(!oneToManyIds.contains(val)) {
                        oneToManyIds.add(String.valueOf(val));
                        oneToManyIdsMap.put(col.getName(), oneToManyIds);
                    }
                }
            }else{
                Object val = values.get(col.getName());
                if(val != null && !val.equals("false") && !val.equals("")) {
                    List<String> manyToOneIds = manyToOneIdsMap.get(col.getName());
                    manyToOneIds = manyToOneIds != null ? manyToOneIds : new ArrayList<String>();
                    if(!manyToOneIds.contains(val)) {
                        manyToOneIds.add(String.valueOf(val));
                        manyToOneIdsMap.put(col.getName(), manyToOneIds);
                    }
                }
            }
        }
    }*/

    protected void deleteNonExistentRows(SyncingDomain syncingDomain) {
        if(syncingDomain != null) {
            List<DataRow> domainRows = model.getRows(syncingDomain.getSelection(), syncingDomain.getWhereArgs());
            List<String> serverIds = new ArrayList<>();
            List<String> idsToDelete = new ArrayList<>();
            for (DataRow row : domainRows) {
                serverIds.add(row.getString(Col.SERVER_ID));
            }
            List<DataRow> allRows = model.getRows();
            for (DataRow row : allRows) {
                if (!serverIds.contains(row.getString(Col.SERVER_ID))) {
                    idsToDelete.add(row.getString(Col.ROWID));
                }
            }
            String[] ids = new String[idsToDelete.size()];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = idsToDelete.get(i);
            }
            if (ids.length != 0) {
                model.delete(Col.ROWID +
                        " in (" + MyUtil.repeat("?, ", ids.length - 1) + " ?)", ids, true);
            }
        }
    }

    public Map<String, List<String>> getManyToOneIdsMap() {
        return manyToOneIdsMap;
    }

    public Map<String, List<String>> getOneToManyIdsMap() {
        return oneToManyIdsMap;
    }

    public List<String> getIdsToUpdateOnServer() {
        return idsToUpdateOnServer;
    }

    public Map<String, String> getWriteDateOfRecordsToUpdateOnServer() {
        return WriteDateOfRecordsToUpdateOnServer;
    }

    public RelRecordsHandler createRelRecordHandlerInstance(Model relModel){
        return new RelRecordsHandler(relModel);
    }

    public Map<String, Map<String, Object>> checkWriteDate(JSONArray response, Map<String,
            Map<String, Object>> toUpdateOnServerRecords, Map<String, String> writeDates) throws JSONException {
        for(int i = 0; i < response.length(); i++) {
            JSONObject lineObject = response.getJSONObject(i);
            JSONObject document = lineObject.getJSONObject("document");
            JSONObject fields = document.getJSONObject("fields");
            Map<String, Object> lineMap = new TreeMap<>();
            for (Iterator<String> it = fields.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONObject fieldObject = fields.getJSONObject(key);
                Object val = getFieldValue(fieldObject);
                lineMap.put(key, val);
            }
            String serverId = String.valueOf(lineMap.get(Col.SERVER_ID));
            Date _write_date_obj = MyUtil.createDateObject(writeDates.get(serverId),
                    MyUtil.DEFAULT_DATE_TIME_FORMAT, false);
            String write_date = MyUtil.milliSecToDate(Long.valueOf(
                    lineMap.get("write_date").toString()));
            Date write_date_obj = MyUtil.createDateObject(write_date,
                    MyUtil.DEFAULT_DATE_TIME_FORMAT, false);
            if (write_date_obj.compareTo(_write_date_obj) > 0) {
                toUpdateOnServerRecords.remove(serverId);
            }
        }
        return toUpdateOnServerRecords;
    }

    public void deletePermanently() {
        model.delete(" _is_active = ? and removed = ? ", new String[] {"1", "1"}, true);
    }

    public void setCurrentSyncDownQueryIndex(int index) {
        this.currentSyncDownQueryIndex = index;
    }

    public class RelRecordsHandler{
        private Model relModel;
        private List<DataRow> localRows;
        private List<Values> toUpdateValues;
        private List<Values> toInsertValues;
        private Map<String, Map<String, Set<String>>> relValues;

        public RelRecordsHandler(Model relModel){
            this.relModel = relModel;
            init();
        }

        private void init(){
            this.localRows = new ArrayList<>();
            this.toUpdateValues = new ArrayList<>();
            this.toInsertValues = new ArrayList<>();
            this.relValues = new HashMap<>();
        }
        public void collectResponses(JSONArray response){
            try {
                localRows = relModel.getRows("", null);
                final Map<String, Object> localRowsMap = MyUtil.rowsToMap(localRows);
                final String currentDate = MyUtil.getCurrentDate();
                for(int i = 0; i < response.length(); i++){
                    JSONObject lineObject = response.getJSONObject(i);
                    JSONObject document = lineObject.getJSONObject("document");
                    JSONObject fields = document.getJSONObject("fields");
                    Map<String, Object> lineMap = new TreeMap<>();
                    for (Iterator<String> it = fields.keys(); it.hasNext(); ) {
                        String key = it.next();
                        JSONObject fieldObject = fields.getJSONObject(key);
                        Object val = getFieldValue(fieldObject);
                        lineMap.put(key, val);
                    }
                    String serverId = String.valueOf(lineMap.get(Col.SERVER_ID));
                    if (localRowsMap.containsKey(serverId)) {
                        DataRow row = (DataRow) localRowsMap.get(serverId);
                        handleExistingRecord(lineMap, row, currentDate);
                    } else {
                        handleNewRecords(lineMap, currentDate);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void handleExistingRecord(Map<String, Object> serverDocument, DataRow localRow,
                                          String currentDate) {
            String serverId = String.valueOf(serverDocument.get(Col.SERVER_ID));
            Date _write_date_obj = MyUtil.createDateObject(localRow.get("_write_date").toString(),
                    MyUtil.DEFAULT_DATE_TIME_FORMAT, false);
            String write_date = MyUtil.milliSecToDate(Long.valueOf(
                    serverDocument.get("write_date").toString()));
            serverDocument.put("write_date", write_date);
            long create_date = 0;
            try{
                create_date = Long.valueOf(serverDocument.get("create_date").toString());
            }catch (Exception ignored){}
            serverDocument.put("create_date", MyUtil.milliSecToDate(create_date));
            Date write_date_obj = MyUtil.createDateObject(write_date,
                    MyUtil.DEFAULT_DATE_TIME_FORMAT, false);
            if (write_date_obj.compareTo(_write_date_obj) > 0 || relModel.forceOverwriteOnLocal()) {
                Values values = relModel.recordToValues(serverDocument);
                values.put("id", serverId);
                values.put("_write_date", currentDate);
                values.put("_is_updated", 0);
                values.put("synced", 1);
                values =  relModel.prepareUpdateValues(values);
                toUpdateValues.add(values);
            }
        }


        private void handleNewRecords(Map<String, Object> serverDocument, String currentDate) {
            Values values = relModel.recordToValues(serverDocument);
            values.put("id", serverDocument.get(Col.SERVER_ID));
            values.put("_write_date", currentDate);
            values.put("_create_date", currentDate);
            values.put("synced", 1);
            values.put("_is_active", 1);
            values.put("removed", 0);
            values.put("_is_updated", 0);
            values = relModel.prepareInsertValues(values);
            toInsertValues.add(values);
        }

        void updateInLocal() {
            performInsert();
            performUpdate();
            toUpdateValues.clear();
            toInsertValues.clear();
            updateRelTable(relModel, relValues);
        }

        private void performInsert() {
            String insertQuery = insertValues(toInsertValues);
            if(!insertQuery.equals("")){
                SQLiteDatabase database = relModel.getWritableDatabase();
                database.execSQL(insertQuery);
            }
            relModel.onDatabaseModified();
        }

        private String insertValues(List<Values> toInsertValues) {
            if(toInsertValues.size() != 0) {
                List<Col> columns = relModel.getColumns();
                StringBuilder insertSQL = new StringBuilder(" INSERT OR IGNORE INTO  '");
                insertSQL.append(relModel.getModelName());
                insertSQL.append("' (");
                for (Col col : columns) {
                    if(!col.getName().equals(Col.ROWID) && !col.getColumnType().equals(Col.ColumnType.one2many) &&
                            !col.getColumnType().equals(Col.ColumnType.array)) {
                        insertSQL.append("'");
                        insertSQL.append(col.getName());
                        insertSQL.append("', ");
                    }
                }
                insertSQL.deleteCharAt(insertSQL.lastIndexOf(","));
                insertSQL.append(") VALUES ");
                insertSQL.append("\n");
                for(Values values : toInsertValues) {
                    insertSQL.append("(");
                    for (Col col : columns) {
                        if(!col.getName().equals(Col.ROWID) && !col.getColumnType().equals(Col.ColumnType.one2many) &&
                                !col.getColumnType().equals(Col.ColumnType.array)) {
                            Object object = values.get(col.getName());
                            if(object.toString().contains("'")){
                                insertSQL.append('"');
                            }else {
                                insertSQL.append("'");
                            }
                            insertSQL.append(values.get(col.getName()));
                            if(object.toString().contains("'")){
                                insertSQL.append('"');
                            }else {
                                insertSQL.append("'");
                            }
                            insertSQL.append(", ");
                        }
                        if(col.getColumnType().equals(Col.ColumnType.array)){
                            Map<String, Set<String>> _relValues = relValues.containsKey(col.getName())?
                                    relValues.get(col.getName()) : new HashMap<String, Set<String>>();
                            if((Set)values.get(col.getName()) != null) {
                                _relValues.put((String) values.get(Col.SERVER_ID), (Set) values.get(col.getName()));
                                if (_relValues != null) {
                                    relValues.put(col.getName(), _relValues);
                                }
                            }
                        }
                    }
                    insertSQL.deleteCharAt(insertSQL.lastIndexOf(","));
                    insertSQL.append("), \n");
                }
                insertSQL.deleteCharAt(insertSQL.lastIndexOf(","));
                insertSQL.append(";");
                return insertSQL.toString();
            }
            return "";
        }

        private void performUpdate() {
            String updateQuery = updateValues2(relModel, toUpdateValues);
            if(!updateQuery.equals("")){
                SQLiteDatabase database = relModel.getWritableDatabase();
                database.execSQL(updateQuery);
            }
            relModel.onDatabaseModified();
        }

        private String updateValues(List<Values> toUpdateValues) {
            if(toUpdateValues.size() != 0) {
                List<Col> serverColumns = relModel.getColumns(false);
                StringBuilder updateSQL = new StringBuilder("UPDATE ");
                updateSQL.append(relModel.getModelName());
                updateSQL.append("\n");
                updateSQL.append("SET ");
                for (Col col : serverColumns) {
                    if (!col.getColumnType().equals(Col.ColumnType.one2many) && !col.getName().equals(Col.SERVER_ID)) {
                        updateSQL.append(col.getName());
                        updateSQL.append("= CASE \n");
                        for (Values values : toUpdateValues) {
                            Object object = values.get(col.getName());
                            updateSQL.append("WHEN id = '");
                            updateSQL.append(values.get(Col.SERVER_ID));
                            updateSQL.append("' THEN ");
                            if (col.getColumnType().equals(Col.ColumnType.text) ||
                                    col.getColumnType().equals(Col.ColumnType.low_quality_image) ||
                                    col.getColumnType().equals(Col.ColumnType.attachement) ||
                                    col.getColumnType().equals(Col.ColumnType.many2one) ||
                                    col.getColumnType().equals(Col.ColumnType.varchar)) {
                                if(object.toString().contains("'")){
                                    updateSQL.append('"');
                                }else {
                                    updateSQL.append("'");
                                }
                            }
                            updateSQL.append(object);
                            if (col.getColumnType().equals(Col.ColumnType.text) ||
                                    col.getColumnType().equals(Col.ColumnType.low_quality_image) ||
                                    col.getColumnType().equals(Col.ColumnType.attachement) ||
                                    col.getColumnType().equals(Col.ColumnType.many2one) ||
                                    col.getColumnType().equals(Col.ColumnType.varchar)) {
                                if(object.toString().contains("'")){
                                    updateSQL.append('"');
                                }else {
                                    updateSQL.append("'");
                                }
                            }
                            updateSQL.append("\n");
                        }
                        updateSQL.append("END, ");
                        updateSQL.append("\n");
                    }
                }
                int lastIndex = updateSQL.lastIndexOf(",");
                if(lastIndex != -1) {
                    updateSQL.deleteCharAt(lastIndex);
                }
                return updateSQL.toString();
            }
            return "";
        }
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setServerUpdateDate(long currentDateInMillis){
        this.serverUpdateDate = currentDateInMillis;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public static class SyncUpCondition{
        private String selection;
        private String[] args ;

        public SyncUpCondition(String selection, String[] args){
            this.selection = selection;
            this.args = args;
        }
    }
}
