
package com.MohafizDZ.framework_repository.core;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.MohafizDZ.App;
import com.MohafizDZ.framework_repository.Utils.MySharedPreferences;
import com.MohafizDZ.empty_project.BuildConfig;
import com.MohafizDZ.framework_repository.Utils.CursorUtils;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.Utils.SQLUtil;
import com.MohafizDZ.framework_repository.core.Account.MUser;
import com.MohafizDZ.framework_repository.service.OrderBy;
import com.MohafizDZ.framework_repository.service.SyncAdapter;
import com.MohafizDZ.framework_repository.service.SyncingDomain;
import com.MohafizDZ.framework_repository.service.SyncingReport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Model implements DatabaseListener, DefaultSyncListener, DatabaseListener.TransactionsListener {
    public static final String TAG = Model.class.getSimpleName();
    public static String BASE_AUTHORITY = BuildConfig.APPLICATION_ID + ".main_provider";
    public static String ATTACHMENT_BASE_AUTHORITY = BuildConfig.APPLICATION_ID + ".attachment_main_provider";

    public static final Integer INVALID_ROW_ID = -1;

    public Context mContext;
    private MUser mUser;
    private String modelName;
    protected MySqlite sqlite;
    private boolean multi;
    private boolean syncing = false;
    private Boolean saveLastSyncDate = null;

    public boolean isSyncing() {
        return new MySharedPreferences(mContext).getBoolean("SYNC_" + getModelName(), false);
    }

    public void setSyncing(boolean syncing) {
        new MySharedPreferences(mContext).setBoolean("SYNC_" + getModelName(), syncing);
    }

    public String getModelName() {
        return modelName;
    }

    //this id will be server id generated from the application
    public Col id = new Col(Col.ColumnType.varchar).setDefaultValue("").setUnique();
    //this will be the primary key of the row in the local database (this should be unique in the local databse)
    public Col _id = new Col(Col.ColumnType.integer).setAutoIncrement(true).setLocalColumn();
    /*
    //every line should have a unique xid that exist only once within the server and the local database
    public Col xid = new Col(Col.ColumnType.varchar).setSize(64);
    */
    //to save the creation date on the server
    public Col create_date = new Col(Col.ColumnType.varchar).setSize(32);
    //to save the creation date in the local database
    public Col _create_date = new Col(Col.ColumnType.varchar).setSize(32).setLocalColumn();
    //if this is set to true than this row will be uploaded to the server
    public Col _is_active = new Col(Col.ColumnType.bool).setDefaultValue(1).setLocalColumn();
    //to save the last update date on the server
    public Col write_date = new Col(Col.ColumnType.text);
    //to save the last update date on the local database
    public Col _write_date = new Col(Col.ColumnType.text).setLocalColumn();
    //if this is true then it should be removed from the server and then the row will be deleted
    public Col removed = new Col(Col.ColumnType.bool).setLocalColumn();
    //if this is true then this row will be updated to the server
    public Col _is_updated = new Col(Col.ColumnType.bool).setLocalColumn();
    //if this is true then the row is already uploaded to the database
    public Col synced = new Col(Col.ColumnType.bool).setLocalColumn();

    private List<Field> fields;

    public Model(Context mContext, String modelName){
        this(mContext, modelName, null);
    }

    public Model(Context mContext, String modelName, MUser mUser){
        this.mContext = mContext;
        this.modelName = modelName;
        this.mUser = mUser == null ? MUser.getCurrentMUser(mContext) : mUser;
        if(this.mUser != null) {
            sqlite = SQLitesListSingleton.getSQLiteList().sqlites.get(this.mUser.getAndroidAccountName());
            if (sqlite == null ) {
                sqlite = new MySqlite(mContext.getApplicationContext(), this.mUser);
                App.addSQLite(this.mUser.getAndroidAccountName(), sqlite);
            }
        }
    }

    public Uri createUri(String authority){
        BASE_AUTHORITY = authority;
        String path = getClass().getName();
        return MyBaseProvider.buildURI(BASE_AUTHORITY, path, mUser.getAndroidAccountName(), multi);
    }

    public Uri uri(){
        return buildUri(BASE_AUTHORITY);
    }

    public Uri buildUri(String authority){
        String path = getClass().getName();
        return MyBaseProvider.buildURI(authority, path, mUser.getAndroidAccountName(), multi);
    }

    private void initColumns(){
        try {this.fields = new ArrayList<>();
            List<Field> fields = new ArrayList<>();
            boolean unassignFromModel = unAssigneFromModel();
            if(!unassignFromModel) {
                fields.addAll(Arrays.asList(getClass().getSuperclass().getDeclaredFields()));
            }else{
                fields.add(getClass().getSuperclass().getField("_id"));
            }
            fields.addAll(Arrays.asList(getClass().getDeclaredFields()));
            for (Field field : fields) {
                if (field.getType().isAssignableFrom(Col.class)) {
                    field.setAccessible(true);
                    Col col = (Col) field.get(this);
                    col.setName(field.getName());
                    this.fields.add(field);
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public List<Col> getColumns(){
        Class superClass = this.getClass().getSuperclass();
        if(superClass.equals(Model.class)) {
            initColumns();
            List<Col> columns = new ArrayList<>();
            for (Field field : fields) {
                columns.add(getColumn(field));
            }
            return columns;
        }else{
            return createInstance(superClass).getColumns();
        }
    }

    public List<Col> getColumns(boolean local){
        Class superClass = this.getClass().getSuperclass();
        if(superClass.equals(Model.class)) {
            initColumns();
            List<Col> columns = new ArrayList<>();
            for (Field field : fields) {
                Col column = getColumn(field);
                if (column.isLocal() == local) {
                    columns.add(column);
                }
            }
            return columns;
        }else{
            return createInstance(superClass).getColumns(local);
        }
    }

    public List<Col> getRelationColumns(){
        Class superClass = this.getClass().getSuperclass();
        if(superClass.equals(Model.class)) {
            initColumns();
            List<Col> columns = new ArrayList<>();
            for (Field field : fields) {
                Col column = getColumn(field);
                if (column.getColumnType().equals(Col.ColumnType.one2many) ||
                        column.getColumnType().equals(Col.ColumnType.many2one) ||
                        column.getColumnType().equals(Col.ColumnType.array)) {
                    columns.add(column);
                }
            }
            return columns;
        }else{
            return createInstance(superClass).getRelationColumns();
        }
    }

    public List<Col> getRelationColumns(boolean local){
        Class superClass = this.getClass().getSuperclass();
        if(superClass.equals(Model.class)) {
            initColumns();
            List<Col> columns = new ArrayList<>();
            for (Field field : fields) {
                Col column = getColumn(field);
                if (column.isLocal() == local && (
                        column.getColumnType().equals(Col.ColumnType.one2many) ||
                        column.getColumnType().equals(Col.ColumnType.many2one) ||
                        column.getColumnType().equals(Col.ColumnType.array))) {
                    columns.add(column);
                }
            }
            return columns;
        }else{
            return createInstance(superClass).getRelationColumns();
        }
    }


    public Col getColumn(Field field){
        try {
            field.setAccessible(true);
            Col col = (Col) field.get(this);
            col.setName(field.getName());
            return col;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Col getColumn(String colName){
        return getColumn(colName, getClass());
    }

    public Col getColumn(String colName, Class modelClass) {
        Col col;
        Field colField;
        try {
            colField = modelClass.getDeclaredField(colName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            if(getClass().getSuperclass() != null) {
                return getColumn(colName, getClass().getSuperclass());
            }else{
                return null;
            }
        }
        Model model = createInstance(mContext, modelClass);
        col = model.getColumn(colField);
        return col;
    }

    public SQLiteDatabase getReadableDatabase(){
        return sqlite.getReadableDatabase();
    }

    public SQLiteDatabase getWritableDatabase(){
        return sqlite.getWritableDatabase();
    }

    public boolean startTransaction(DatabaseObserver databaseObserver){
        return SQLUtil.startTransaction(getWritableDatabase(), databaseObserver);
    }

    //take effect after creating a table so it will be used after first run or after upgrading database
    @Override
    public boolean unAssigneFromModel() {
        return false;
    }

    //take effect after creating a table so it will be used after first run or after upgrading database
    @Override
    public boolean sortableColumns() {
        return true;
    }

    public List<DataRow> select(String selection, String[] selectionArgs){
        return select(null, selection, selectionArgs, "");
    }

    public List<DataRow> select(String[] projections, String selection, String[] selectionArgs, String sort){
        List<DataRow> allRows = new ArrayList<>();
//        try (Cursor cr = mContext.getApplicationContext().getContentResolver().
//                query(createUri(BASE_AUTHORITY), projections, selection, selectionArgs, sort)) {
//            if (cr != null && cr.moveToFirst()) {
//                do {
//                    DataRow row = CursorUtils.toDatarow(cr);
//                    allRows.add(row);
//                } while (cr.moveToNext());
//            }
//        } catch (Exception e) {
//            return select(projections, selection, selectionArgs, sort);
//        }
        Cursor cr = null;
        try{
            cr = mContext.getContentResolver().
                    query(createUri(BASE_AUTHORITY), projections, selection, selectionArgs, sort);
        }catch (IllegalStateException e){
            e.printStackTrace();
            return select(projections, selection, selectionArgs, sort);
        }
        try{
            if (cr != null && cr.moveToFirst()) {
                do {
                    DataRow row = CursorUtils.toDatarow(cr);
                    allRows.add(row);
                } while (cr.moveToNext());
            }
        }finally {
            if(cr != null){
                cr.close();
            }
        }
        return allRows;
    }

    public int insert(Values values){
        return insert(values, false);
    }

    public int insert(Values values, boolean fromServer){
        if(!fromServer && !values.containsKey("_is_updated")){
            values.put("_is_updated", 1);
        }
        Uri uri = mContext.getApplicationContext().getContentResolver().insert(createUri(BASE_AUTHORITY), values.toContentValues());
        if (uri != null) {
            return Integer.parseInt(uri.getLastPathSegment());
        }
        return -1;
    }

    public DataRow insertAndBrowse(Values values, boolean fromServer){
        int row_id = insert(values, fromServer);
        if(values.containsKey(Col.SERVER_ID)) {
            return browse(String.valueOf(values.get(Col.SERVER_ID)));
        }
        return browse(row_id);
    }

    public DataRow insertAndBrowse(Values values){
        return insertAndBrowse(values, false);
    }

    public int update(int row_id, Values values){
        return update(values, Col.ROWID + " = ? ", new String[]{String.valueOf(row_id)});
    }

    public int update(int row_id, Values values, boolean fromServer){
        return update(values, Col.ROWID + " = ? ", new String[]{String.valueOf(row_id)}, fromServer);
    }

    public int update(String serverId, Values values){
        return update(values, Col.SERVER_ID + " = ? ", new String[]{serverId});
    }

    public int update(String serverId, Values values, boolean fromServer){
        return update(values, Col.SERVER_ID + " = ? ", new String[]{serverId}, fromServer);
    }

    public int update(@Nullable Values values, @Nullable String selection, @Nullable String[] selectionArgs){
        return update(values, selection, selectionArgs, false);
    }

    public int update(@Nullable Values values, @Nullable String selection, @Nullable String[] selectionArgs, boolean fromServer){
        if(!fromServer && !values.containsKey("_is_updated")){
            values.put("_is_updated", 1);
        }
        assert values != null;
        return mContext.getApplicationContext().getContentResolver().update(createUri(BASE_AUTHORITY), values.toContentValues(), selection, selectionArgs);
    }

    public int insertOrUpdate(String selection, String[] args, Values values, boolean fromServer) {
        int count = update(values, selection, args, fromServer);
        if (count <= 0) {
            return insert(values, fromServer);
        } else {
            return selectRowId(selection, args);
        }
    }

    public int insertOrUpdate(String serverId, Values values) {
        String selection = Col.SERVER_ID + " = ? ";
        String[] args = {serverId};
        int count = update(values, selection, args, false);
        if (count <= 0) {
            return insert(values, false);
        } else {
            return selectRowId(selection, args);
        }
    }

    public int insertOrUpdate(String serverId, Values values, boolean fromServer) {
        String selection = Col.SERVER_ID + " = ? ";
        String[] args = {serverId};
        int count = update(values, selection, args, fromServer);
        if (count <= 0) {
            return insert(values, fromServer);
        } else {
            return selectRowId(selection, args);
        }
    }

    public int insertOrUpdate(int rowId, Values values) {
        String selection = Col.SERVER_ID + " = ? ";
        String[] args = {rowId + ""};
        int count = update(values, selection, args, false);
        if (count <= 0) {
            return insert(values, false);
        } else {
            return selectRowId(selection, args);
        }
    }

    public int insertOrUpdate(int rowId, Values values, boolean fromServer) {
        String selection = Col.SERVER_ID + " = ? ";
        String[] args = {rowId + ""};
        int count = update(values, selection, args, fromServer);
        if (count <= 0) {
            return insert(values, fromServer);
        } else {
            return selectRowId(selection, args);
        }
    }

    private int selectRowId(String selection, String[] args) { ;
        return browse(selection, args).getInteger(Col.ROWID);
    }

    public int delete(int _id){
        return delete(_id, false);
    }

    public int delete(int _id, boolean permanently){
        String selection = Col.ROWID + " = ? ";
        String[] where = new String[]{_id + ""};
        return delete(selection, where, permanently);
    }

    public int delete(@Nullable String selection, @Nullable String[] selectionArgs){
        return delete(selection, selectionArgs, false);
    }

    public int delete(@Nullable String selection , @Nullable String[] selectionArgs, boolean permanently){
        if(permanently){
            return mContext.getApplicationContext().getContentResolver().delete(createUri(BASE_AUTHORITY), selection, selectionArgs);
        }else{
            Values values = new Values();
            values.put("removed", 1);
            return update(values, selection, selectionArgs);
        }
    }

    //for inserting or updating multi rows at a time this should be called with multi = true and it will automatically desactivate after the transaction
    public void setMultiTransactions(boolean multi){
        this.multi = multi;
    }

    public List<DataRow> getRows(){
        return getRows("", new String[]{});
    }

    public List<DataRow> getRows(String selection, String[] selectionArgs){
//        Cursor cursor = select(selection, selectionArgs);
//        List<DataRow> allRows = new ArrayList<>();
//        if(cursor.moveToFirst()){
//            do{
//                DataRow row = CursorUtils.toDatarow(cursor);
//                allRows.add(row);
//            }while(cursor.moveToNext());
//        }
        return select(selection, selectionArgs);
    }

    public DataRow browse(int _id){
        return browse(" _id = ? ", new String[]{_id + ""});
    }

    public DataRow browse(String id){
        return browse(" id = ? ", new String[]{id});
    }

    public DataRow browse(String selection, String[] selectionArgs){
        List<DataRow> rows = getRows(selection, selectionArgs);
        if (rows != null && rows.size() != 0){
            return rows.get(0);
        }
        return null;
    }

    public DataRow getRelations(DataRow row){
        List<Col> colList = getRelationColumns();
        for(Col col : colList){
            String colName = col.getName();
            if(row.containsKey(colName)){
                if(col.getColumnType() == Col.ColumnType.array){
                    row.putRel(colName, getRelArray(row, colName));
                }else if(col.getColumnType() == Col.ColumnType.many2one){
                    Model relModel = createInstance(mContext, col.getRelationalModel());
                    row.putRel(colName, relModel.browse(row.getString(colName)));
                }
            }
        }
        return row;
    }

    public DataRow browse(String selection, String[] args, String sort){
        List<DataRow> rows = select(null, selection, args, sort);
        if (rows != null && rows.size() != 0){
            return rows.get(0);
        }
        return null;
    }

    public Model createInstance(Class<?> classType) {
        try {
            Constructor<?> constructor = classType.getConstructor(Context.class);
            return (Model) constructor.newInstance(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Model createInstance(Context mContext, Class<?> classType) {
        try {
            Constructor<?> constructor = classType.getConstructor(Context.class);
            return (Model) constructor.newInstance(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int insertRecord(@NonNull Map<String, Object> document){

        return INVALID_ROW_ID;
    }

    public Values recordToValues(Map<String, Object> documentRecord) {
        Values values = new Values();
        for(String key : documentRecord.keySet()){
            Object value = documentRecord.get(key);
            if(value instanceof JSONArray){
                JSONArray relRecords = (JSONArray) value;
                Set<String> relIds = getRelValues(relRecords);
                values.put(key, relIds);
            }else {
                values.put(key, value);
            }
        }
        return  values;
    }

    private Set<String> getRelValues(JSONArray relRecords) {
        Set<String> relIds = new HashSet<>();
        for(int i=0; i < relRecords.length(); i++){
            try {
                JSONObject jsonObject = relRecords.getJSONObject(i);
                relIds.add(jsonObject.getString("stringValue"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return relIds;
    }

    public void prepareServerData(List<DataRow> rows) {
        if(rows.size() != 0) {
            Values values = new Values();
            for (Col col : getColumns(false)) {

            }
        }
    }

    public String getCreateXId() {
        String device_id = "--UNKOWN--";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(mContext.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                device_id = "NO_DEVICE";
            } else {
                device_id = telephonyManager.getDeviceId();
            }
        } catch (Exception e) {
        }

        return device_id + "." +  MyUtil.uniqid("",true) ;
    }

    public static String getCreateXId(Context mContext) {
        String device_id = "--UNKOWN--";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(mContext.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                device_id = "NO_DEVICE";
            } else {
                device_id = telephonyManager.getDeviceId();
            }
        } catch (Exception e) {
        }

        return device_id + "." +  MyUtil.uniqid("",true) ;
    }

    public List<String> getAttachmentCols(){
        List<String> columnNames = new ArrayList<>();
        for( Col col : getColumns()){
            if(col.getColumnType().equals(Col.ColumnType.attachement) && !col.isLocal()) {
                columnNames.add(col.getName());
            }
        }
        return  columnNames;
    }
    public boolean isOnline() {
        return true;
    }

    public void sync(){
        sync(null);
    }

    public void sync(DefaultSyncListener defaultSyncListener){
//        new SyncAdapter(mContext, this.getClass(), defaultSyncListener).performSync();
        new SyncAdapter(mContext, this.getClass()).performSync(null, null, null,
                null, null, null, null, null, null,
                null, null, null);
    }

    public void syncUp(){
        syncUp(null, null);
    }

    public void syncUp(DefaultSyncListener defaultSyncListener){
        syncUp(null, defaultSyncListener);
    }

    //these rows should be inserted before syncing
    public void syncUp(List<DataRow> rows, final DefaultSyncListener defaultSyncListener){
        //todo add sync up later
        /*List<DataRow> syncUpRows = rows;
        if(rows == null){
            syncUpRows = getRows("",new String[]{});
        }
        if(syncUpRows != null) {
            new SyncModel(mContext, User_Current.class){

                @Override
                public void onSyncStart(Model model) {
                    defaultSyncListener.onSyncStarted();
                }

                @Override
                public void onSyncFinished(Model model) {
                    defaultSyncListener.onSyncFinished();
                }
            }.syncUp(syncUpRows);
        }*/
    }

    public void onSyncImagesFinished() {
    }

    @Override
    public void onSyncStarted() {

    }

    @Override
    public void onSyncFinished() {

    }

    public int getSequence(){
        return 0;
    }

    public SyncingDomain setDefaultDomain(SyncingDomain domain){
        return domain;
    }

    public void onDatabaseModified() {

    }

    public Values prepareUpdateValues(Values values) {
        Values updateValues = new Values();
        updateValues.addAll(values);
        for(Col col : getColumns()){
            if(!col.getColumnType().equals(Col.ColumnType.one2many)) {
                if(col.getColumnType().equals(Col.ColumnType.attachement)){
                    if(values.get(col.getName()) == null){
                        updateValues.put(col.getName() + "_saved_in_local", 1);
                        updateValues.put(col.getName() + "_saved_in_server", 1);
                    }
                    else if(values.containsKey(col.getName()) && !Objects.requireNonNull(values.get(col.getName())).toString().equals("")) {
                        updateValues.put(col.getName() + "_saved_in_local", 0);
                        updateValues.put(col.getName() + "_saved_in_server", 1);
                    }
                }
                if (!values.containsKey(col.getName()) || values.get(col.getName()) == null) {
                    updateValues.put(col.getName(), col.getDefaultValue());
                }
            }
        }
        return updateValues;
    }

    public Values prepareInsertValues(Values values) {
        Values insertValues = new Values();
        insertValues.addAll(values);
        for(Col col : getColumns()){
            if(!col.getColumnType().equals(Col.ColumnType.one2many) && !col.getColumnType().equals(Col.ColumnType.array)) {
                if(col.getColumnType().equals(Col.ColumnType.attachement)){
                    if(values.get(col.getName()) == null){
                        insertValues.put(col.getName() + "_saved_in_local", 1);
                        insertValues.put(col.getName() + "_saved_in_server", 1);
                    }
                    else if(values.containsKey(col.getName()) && !Objects.requireNonNull(values.get(col.getName())).toString().equals("")) {
                        insertValues.put(col.getName() + "_saved_in_local", 0);
                        insertValues.put(col.getName() + "_saved_in_server", 1);
                    }
                }
                if (!values.containsKey(col.getName()) || values.get(col.getName()) == null) {
                    insertValues.put(col.getName(), col.getDefaultValue());
                }
            }
        }
        return insertValues;
    }

    public boolean allowDeleteInLocal() {
        return true;
    }

    public boolean allowDeleteRecordsOnServer() {
        return true;
    }

    public Map<String, Object> prepareInsertRecords(DataRow row, long currentDateInMillis) {
        Map<String, Object> record = new HashMap<>();
        for (Col col : getColumns(false)) {
            if(col.canSyncUpCol() || col.canInsertOnServer()) {
                if (!col.getColumnType().equals(Col.ColumnType.one2many) && !col.getColumnType().equals(Col.ColumnType.array)) {
                    Object val = row != null? row.get(col.getName()) : null;
                    if(val == null){
                        val = col.getDefaultValue();
                    }
                    record.put(col.getName(), val);
                }
                if (col.getColumnType().equals(Col.ColumnType.array)) {
                    Object val = row != null? row.getRelArray(this, col) : null;
                    if(val == null){
                        val = new ArrayList<>();
                    }
                    record.put(col.getName(), val);
                }
            }
        }
        record.put("write_date", currentDateInMillis);
        record.put("create_date", currentDateInMillis);
        return record;
    }

    public Map<String, Object> prepareUpdateRecords(DataRow row, long currentDateInMillis) {
        Map<String, Object> record = new HashMap<>();
        for (Col col : getColumns(false)) {
            if(col.canSyncUpCol()) {
                if (!col.getColumnType().equals(Col.ColumnType.one2many) && !col.getColumnType().equals(Col.ColumnType.array)) {
                    Object val = row != null? row.get(col.getName()) : null;
                    if(val == null){
                        val = col.getDefaultValue();
                    }
                    record.put(col.getName(), val);
                }
                if (col.getColumnType().equals(Col.ColumnType.array)) {
                    Object val = row != null? row.getRelArray(this, col) : null;
                    if(val == null){
                        val = new ArrayList<>();
                    }
                    record.put(col.getName(), val);
                }
            }
        }
        record.put("write_date", currentDateInMillis);
        record.remove("create_date");
        return record;
    }

    public Map<String, Object> prepareInsertRecords(DataRow row, long currentDateInMillis, List<String> syncableColumns) {
        Map<String, Object> record = new HashMap<>();
        for (Col col : getColumns(false)) {
            if(syncableColumns.contains(col.getName()) && (
                    col.canSyncUpCol() || col.canInsertOnServer())) {
                if (!col.getColumnType().equals(Col.ColumnType.one2many) && !col.getColumnType().equals(Col.ColumnType.array)) {
                    Object val = row != null? row.get(col.getName()) : null;
                    if(val == null){
                        val = col.getDefaultValue();
                    }
                    record.put(col.getName(), val);
                }
                if (col.getColumnType().equals(Col.ColumnType.array)) {
                    Object val = row != null? row.getRelArray(this, col) : null;
                    if(val == null){
                        val = new ArrayList<>();
                    }
                    record.put(col.getName(), val);
                }
            }
        }
        record.put("write_date", currentDateInMillis);
        record.put("create_date", currentDateInMillis);
        return record;
    }

    public Map<String, Object> prepareUpdateRecords(DataRow row, long currentDateInMillis, List<String> syncableColumns) {
        Map<String, Object> record = new HashMap<>();
        for (Col col : getColumns(false)) {
            if(syncableColumns.contains(col.getName()) && col.canSyncUpCol()) {
                if (!col.getColumnType().equals(Col.ColumnType.one2many) && !col.getColumnType().equals(Col.ColumnType.array)) {
                    Object val = row != null? row.get(col.getName()) : null;
                    if(val == null){
                        val = col.getDefaultValue();
                    }
                    record.put(col.getName(), val);
                }
                if (col.getColumnType().equals(Col.ColumnType.array)) {
                    Object val = row != null? row.getRelArray(this, col) : null;
                    if(val == null){
                        val = new ArrayList<>();
                    }
                    record.put(col.getName(), val);
                }
            }
        }
        record.put("write_date", currentDateInMillis);
        record.remove("create_date");
        return record;
    }

    public MUser getmUser() {
        return mUser;
    }

    public static boolean startTransaction(Context mContext, OnStartTransactionListener listener){
        boolean transactionSuccessful = false;
        MUser mUser =  MUser.getCurrentMUser(mContext) ;
        SQLiteDatabase db = App.getDB(mUser.getAndroidAccountName(), false);
        db.beginTransactionNonExclusive();
        try {
            transactionSuccessful = listener.startedTransaction();
            if(transactionSuccessful){
                db.setTransactionSuccessful();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
        return transactionSuccessful;
    }

    @Override
    public void onPreInsert(Values values) {

    }

    @Override
    public void onPreUpdate(Values values, String selectionConditions) {

    }

    @Override
    public void onPreDelete(String selectionConditions) {

    }

    @Override
    public void onPostInsert(Values values) {

    }

    @Override
    public void onPostUpdate(Values values, String selectionConditions) {

    }

    @Override
    public void onPostDelete(String selectionConditions) {

    }

    public boolean canSyncRelations(){
        return false;
    }

    public boolean canSyncDownRelations(){
        return false;
    }

    public boolean canSyncUpRelations(){
        return false;
    }

    public boolean allowSyncDown() {
        return true;
    }

    public boolean allowSyncUp() {
        return true;
    }

    public boolean allowRemoveRecordsOutOfDomain() {
        return true;
    }

    public boolean checkWriteDate() {
        return true;
    }

    public boolean forceOverwriteOnServer() {
        return false;
    }

    public boolean forceOverwriteOnLocal() {
        return false;
    }

    public int syncingLimit() {
        return 0;
    }

    public String getLikeValue() {
        return null;
    }

    public List<String> getLikeFields() {
        return null;
    }

    public OrderBy setOrderBy() {
        return null;
    }

    public Boolean canSaveLastSyncDate() {
        return saveLastSyncDate;
    }

    public void setCanSaveLastSyncDate(Boolean canSaveLastSyncDate){
        this.saveLastSyncDate = canSaveLastSyncDate;
    }

    public DataRow mapToRow(Map<String, Object> data) {
        List<Col> cols = getColumns(false);
        DataRow row = new DataRow();
        for (Col col : cols) {
            String colName = col.getName();
            try {
                Object relValue = data.get(colName);
                row.put(colName, relValue != null? relValue : col.getDefaultValue());
            } catch (Exception ignored) {
            }
        }
        return row;
    }

    public void insertOrUpdateRowFromServerRecord(DataRow row, List<String> toUpdateRelCol) {
        row.put("synced", 1);
        row.put("_is_updated", 0);
        Values values = new Values();
        for(Col col : getColumns(false)){
            String colName = col.getName();
            if(!row.containsKey(colName) && !row.containsRelKey(colName)) continue;
            if(col.getColumnType() != Col.ColumnType.array &&
                    col.getColumnType() != Col.ColumnType.one2many){
                values.put(colName, row.get(colName));
            }
            if(col.getColumnType() == Col.ColumnType.array){
                List<String> array = row.getRelStringList(colName);
                emptyRelArray(row, colName);
                insertRelArray(row, colName, array);
            }
            if(col.getColumnType() == Col.ColumnType.many2one){
                if(toUpdateRelCol != null && toUpdateRelCol.contains(colName)){
                    Model relModel = createInstance(col.getRelationalModel());
                    relModel.insertOrUpdateRowFromServerRecord(row.getRelRow(colName), null);
                }
            }
        }
        values.put("_is_updated", 0);
        values.put("synced", 1);
        String serverId = row.getString(Col.SERVER_ID);
        DataRow existedRow = browse(serverId);
        if(existedRow == null){
            insert(values);
        }else{
            update(serverId, values);
        }
    }

    public interface OnStartTransactionListener{
        public boolean startedTransaction();
    }

    public void onSyncFinishedFromRel(Model baseModel){

    }

    public boolean syncWithSuccess(int beforeMinutes){
        return new SyncingReport(mContext).browse(" model_name = ? and last_sync_date > ? ",
                new String[]{getModelName(), MyUtil.getDateBeforeMins(beforeMinutes)}) != null;
    }

    public int countUpdatedRows(){
        return select(" _is_active = ? and (_is_updated = ? or removed = ? or synced = ? ) ",
                new String[]{"1", "1", "1", "0"}).size();
    }

    public long insertRelArray(String baseServerId, String field, String relServerId){
        DataRow row = new DataRow();
        row.put(Col.SERVER_ID, baseServerId);
        return insertRelArray(row, field, relServerId);
    }
    public long insertRelArray(DataRow row, String field, String relServerId){
        Col col = getColumn(field);
        String relTableName = getArrayRelTableName(col);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("base_col_id", row.getString(Col.SERVER_ID));
        contentValues.put("rel_col", relServerId);
        long insertedNum = db.insert(relTableName, null, contentValues);
        if(insertedNum != 0) {
            Values values = new Values();
            values.put("_is_updated", 1);
            update(row.getInteger(Col.ROWID), values);
        }
        return insertedNum;
    }

    public void insertRelArray(String baseServerId, String field, List<String> relServerIds){
        DataRow row = new DataRow();
        row.put(Col.SERVER_ID, baseServerId);
        insertRelArray(row, field, relServerIds);
    }

    public void insertRelArray(DataRow row, String field, List<String> relServerIds){
        if(relServerIds != null && relServerIds.size() != 0) {
            Col col = getColumn(field);
            String relTableName = getArrayRelTableName(col);
            StringBuilder sql = new StringBuilder();
            sql.append(" INSERT INTO ");
            sql.append(relTableName);
            sql.append(" (");
            sql.append("base_col_id");
            sql.append(", rel_col");
            sql.append(")");
            sql.append(" VALUES");
            for (String valueServerId : relServerIds) {
                sql.append("(");
                sql.append("'");
                sql.append(row.getString(Col.SERVER_ID));
                sql.append("'");
                sql.append(", ");
                sql.append("'");
                sql.append(valueServerId);
                sql.append("'");
                sql.append("),");
            }
            sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(";");
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL(sql.toString());
            Values values = new Values();
            values.put("_is_updated", 1);
            update(row.getInteger(Col.ROWID), values);
        }
    }

    public int deleteRelArrayServerId(String baseServerId, String field, String relServerId){
        DataRow row = new DataRow();
        row.put(Col.SERVER_ID, baseServerId);
        return deleteRelArrayServerId(row, field, relServerId);
    }

    public int deleteRelArrayServerId(DataRow row, String field, String relServerId){
        SQLiteDatabase db = getWritableDatabase();
        Col col = getColumn(field);
        String relTableName = getArrayRelTableName(col);
        String selection = "base_col_id = ? and rel_col  = ? ";
        String[] args = new String[2];
        args[0] = row.getString(Col.SERVER_ID);
        args[1] = relServerId;
        int deletedNum = db.delete(relTableName, selection, args);
        if(deletedNum != 0) {
            Values values = new Values();
            values.put("_is_updated", 1);
            update(row.getInteger(Col.ROWID), values);
        }
        return deletedNum;
    }

    public int emptyRelArray(String baseServerId, String field){
        DataRow row = new DataRow();
        row.put(Col.SERVER_ID, baseServerId);
        return emptyRelArray(row, field);
    }

    public int emptyRelArray(DataRow row, String field){
        SQLiteDatabase db = getWritableDatabase();
        Col col = getColumn(field);
        String relTableName = getArrayRelTableName(col);
        String selection = "base_col_id = ? ";
        String[] args = new String[1];
        args[0] = row.getString(Col.SERVER_ID);
        int deletedNum = db.delete(relTableName, selection, args);
        if(deletedNum != 0) {
            Values values = new Values();
            values.put("_is_updated", 1);
            update(row.getInteger(Col.ROWID), values);
        }
        return deletedNum;
    }

    public String getArrayRelTableName(Col col){
        return getModelName() + "_" + col.getName() + "_rel_table";
    }

    public List<String> getRelArray(String baseServerId, String field){
        DataRow row = new DataRow();
        row.put(Col.SERVER_ID, baseServerId);
        return getRelArray(row, field);
    }

    public List<String> getRelArray(DataRow row, String field){
        Col col = getColumn(field);
        return getRelArray(row, col);
    }
    public List<String> getRelArray(DataRow row, Col col){
        try {
            SQLiteDatabase db = getReadableDatabase();
            String tableName = getArrayRelTableName(col);
            String selection = " base_col_id = ?";
            String[] args = {row.getString(Col.SERVER_ID)};
            Cursor cursor = db.query(tableName, null, selection, args, null, null, null);
            List<String> relArray = new ArrayList<>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ;
                    int index = cursor.getColumnIndex("rel_col");
                    relArray.add(cursor.getString(index));
                } while (cursor.moveToNext());
            }
            if(cursor != null){
                cursor.close();
            }
            return relArray;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getExistingColumns(SQLiteDatabase db){
        List<String> existing_columns = new ArrayList<>();

        Cursor info_cursor = db.rawQuery("pragma table_info ("+getModelName()+");", null);
        if (info_cursor.moveToFirst()) {
            do {
                existing_columns.add(info_cursor.getString(1));
            } while (info_cursor.moveToNext());
        }
        info_cursor.close();

        return existing_columns;
    }

    private String getColumnType(Col col) {
        try {
            StringBuilder colStatement = new StringBuilder();
            if (col.getColumnType() != Col.ColumnType.many2one &&
                    col.getColumnType() != Col.ColumnType.array) {
                String type = col.getType();
                colStatement.append(type);
                if (col.getSize() != 0) {
                    colStatement.append(" (");
                    colStatement.append(col.getSize());
                    colStatement.append(") ");
                }
                return colStatement.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void onModelUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Override in model
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"+getModelName()+"';", null);
        if(c.getCount()<=0){
            // CREATE TABLE
            SQLUtil.generateCreateStatement(this);
            HashMap<String, String> sqlCreateStatement = SQLUtil.getSqlCreateStatement();
            for (String key : sqlCreateStatement.keySet()) {
                String query = sqlCreateStatement.get(key);
                db.execSQL(query);
                Log.i(TAG, "NEW TABLE CREATED : " + key);
            }
        } else {
            List<Col> columns = getColumns();
            List<String> existing_columns = getExistingColumns(db);

            StringBuffer column_statement = new StringBuffer();
            List<String> finishedColumns = new ArrayList<>();
            for (Col column : columns) {

                if (!existing_columns.contains(column.getName()))
                    if (!finishedColumns.contains(column.getName())) {
                        finishedColumns.add(column.getName());
                        String type = getColumnType(column);
                        if (type != null && column.getColumnType() != Col.ColumnType.one2many &&
                                column.getColumnType() != Col.ColumnType.array) {

                            column_statement.append("ALTER TABLE ");
                            column_statement.append(getModelName()  );
                            column_statement.append(" ADD COLUMN ");

                            column_statement.append(column.getName());
                            column_statement.append(" " + type + " ");
                            if (column.isAutoIncrement()) {
                                column_statement.append(" PRIMARY KEY ");
                                column_statement.append(" AUTOINCREMENT ");
                            }
                            Object default_value = column.getDefaultValue();
                            if (default_value != null) {
                                column_statement.append(" DEFAULT ");
                                if (default_value instanceof String) {
                                    column_statement.append("'" + default_value + "'");
                                } else {
                                    column_statement.append(default_value);
                                }
                            }
                            column_statement.append("; ");
                        }
                        if (column.getColumnType() == Col.ColumnType.array) {
                            SQLUtil.createArrayRelationalTable(this, column);
                            String tableName = getArrayRelTableName(column);
                            String sql = SQLUtil.getSqlCreateStatement().get(tableName);
                            if(sql != null && sql.length()>0) {
                                column_statement.append(sql);
                                column_statement.append("; ");
                            }
                        }
                    }
            }

            if(column_statement.length() > 0) {
                try {
                    String[] sqlArray = column_statement.toString().split(";");
                    for(String sql : sqlArray) {
                        db.execSQL(sql);
                    }
                    Log.i(TAG, "NEW FIELDS CREATED : " + column_statement.toString());
                } catch (Exception e){
                    Log.i(TAG, "UNSUPPORTED SQLITE VERSION TO RUN THIS QUERY: " + column_statement.toString());
                }
            }
        }

    }

    public boolean syncUsingFirstRunDate(){
        return false;
    }

    public void insertRecords(final List<DataRow> records) {
        insertRecords(records, true);
    }

    private void insertRecords(final List<DataRow> records, boolean insertRel){
        List<Col> cols = getColumns();
        Map<String, Map<String, List<String>>> relArray = new HashMap<>();
        Map<String, List<DataRow>> m2oRowsMap = new HashMap<>();
        Map<String, Model> modelNameModelMap = new HashMap<>();
        for(DataRow row : records){
            Values values = new Values();
            for(Col col : cols) {
                String colName = col.getName();
                if(colName.equals(Col.ROWID) || colName.equals("_write_date") || colName.equals("_create_date")){
                    continue;
                }
                if(col.getColumnType() != Col.ColumnType.array && col.getColumnType() != Col.ColumnType.one2many) {
                    Object val = null;
                    try{
                        val = row.get(colName);
                    }catch (Exception ignored){
                    }
                    val = val == null? col.getDefaultValue() : val;
                    values.put(colName, val);
                }
                if(col.getColumnType() == Col.ColumnType.array){
                    if(col.getColumnType().equals(Col.ColumnType.array)){
                        Map<String, List<String>> _relValues = relArray.containsKey(col.getName())?
                                relArray.get(col.getName()) : new HashMap<String, List<String>>();
                        List list;
                        if(row.containsRelKey(colName)){
                            list = row.getRelStringList(col.getName()) != null?
                                    row.getRelStringList(colName) : new ArrayList<>();
                        }else {
                            list = row.get(colName) != null && !row.get(colName).toString().equals("") ?
                                    (List) row.get(colName) : new ArrayList<>();
                        }
                        if(list != null) {
                            _relValues.put((String) row.get(Col.SERVER_ID), list);
                            if (_relValues != null) {
                                relArray.put(col.getName(), _relValues);
                            }
                        }
                    }
                }else if(insertRel && col.getColumnType() == Col.ColumnType.many2one){
                    DataRow relRow = null;
                    try{
                        relRow = row.getRelRow(colName);
                    }catch (Exception ignored){ }
                    if(relRow != null) {
                        Model relModel = createInstance(col.getRelationalModel());
                        List<DataRow> rows = m2oRowsMap.containsKey(relModel.getModelName()) ? m2oRowsMap.get(relModel.getModelName()) : new ArrayList<>();
                        rows.add(relRow);
                        m2oRowsMap.put(relModel.getModelName(), rows);
                        if(!modelNameModelMap.containsKey(relModel.getModelName())){
                            modelNameModelMap.put(relModel.getModelName(), relModel);
                        }
                    }
                }
            }
            insertOrUpdate(row.getString(Col.SERVER_ID), values);
        }
        if(relArray.size() != 0){
            updateRelTable(this, relArray);
        }
        for(String modelName : m2oRowsMap.keySet()){
            Model relModel = modelNameModelMap.get(modelName);
            List<DataRow> rows = m2oRowsMap.get(modelName);
            relModel.insertRecords(rows, false);
        }
    }

    private void updateRelTable(Model model, Map<String, Map<String, List<String>>> relValues) {
        for(String relField : relValues.keySet()){
            Map<String, List<String>> _relValues = relValues.get(relField);
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

    private void insertRelValues(Model model, String relField, Map<String, List<String>> relValues) {
        if(relValues.size() != 0) {
            boolean blockInsert = true;
            Col col = model.getColumn(relField);
            String relTableName = model.getArrayRelTableName(col);
            StringBuilder sql = new StringBuilder();
            sql.append(" INSERT INTO ");
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
}