package com.MohafizDZ.framework_repository.core;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.MohafizDZ.App;
import com.MohafizDZ.framework_repository.Utils.MyUtil;

import java.util.ArrayList;
import java.util.List;

public class MyBaseProvider extends ContentProvider {
    public final static String KEY_MODEL = "key_model";
    public final static String KEY_TYPE = "single";
    private static final String KEY_USERNAME = "key_username";
    public static final String QUERY_PARAMETER_LIMIT = "limit";
    public static final String QUERY_PARAMETER_OFFSET = "offset";
    private static boolean multi = false;
    private DatabaseListener.TransactionsListener transactionsListener;

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] baseProjection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        String limit = uri.getQueryParameter(QUERY_PARAMETER_LIMIT);
        String offset = uri.getQueryParameter(QUERY_PARAMETER_OFFSET);
        String limitString = null;
        if(limit != null && offset != null && !limit.equals("") && !offset.equals("")){
            limitString = offset + "," + limit;
        }
        Model model = getModel(uri);
        String[] projection = validateProjection(model, baseProjection);
        Cursor cursor = generateSelectQuery(model, projection, selection, selectionArgs, sortOrder, limitString);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private Cursor generateSelectQuery(Model model, String[] projection, String selection, String[] selectionArgs, String sortOrder, String limit) {
        SQLiteQueryBuilder query = new SQLiteQueryBuilder();
        query.setTables(model.getModelName());
        return query.query(model.getReadableDatabase(), projection, selection, selectionArgs,
                null, null, sortOrder, limit);
    }

    private String[] validateProjection(Model model, String[] baseProjection) {
        List<String> projection = new ArrayList<>();
        if(baseProjection == null){
            projection.add("*");
        }else{
            projection.add(Col.ROWID);
            if(!model.unAssigneFromModel()){
                projection.add(Col.LOCAL_WRITE_DATE);
                projection.add(Col.ENABLED);
                projection.add(Col.REMOVED);
            }
        }
        return projection.toArray(new String[projection.size()]);
    }

    public Model getModel(Uri uri) {
        String modelClassName = uri.getQueryParameter(KEY_MODEL);
        String username = uri.getQueryParameter(KEY_USERNAME);
        String type = uri.getQueryParameter(KEY_TYPE);
        assert type != null;
        multi = type.equals("multi");
        Model model = App.getModel(getContext(), modelClassName, username);
        assert model != null;
        model = getDirectChildOfModel(model, username);
        assert model != null;
        transactionsListener = model;
        return model;
    }

    private Model getDirectChildOfModel(Model model, String username) {
        if(model == null){
            return null;
        }
        Class modelClass = model.getClass().getSuperclass();
        if(modelClass == null){
            return null;
        }
        if(modelClass.equals(Model.class)){
            return model;
        }
        Model modelParent = App.getModel(getContext(), modelClass.getName(), username);
        return getDirectChildOfModel(modelParent, username);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Model model = getModel(uri);
        if(values == null){
            values = new ContentValues();
        }
        values.put("_write_date", MyUtil.getCurrentDate());
        values.put("_create_date", MyUtil.getCurrentDate());
        if (!values.containsKey("_is_active"))
            values.put("_is_active", 1);
        if (!values.containsKey("removed"))
            values.put("removed", 0);
        if (!values.containsKey("synced"))
            values.put("synced", 0);
        if (!values.containsKey(Col.SERVER_ID)){
            values.put(Col.SERVER_ID, model.getCreateXId());
        }
        for(String columnName : model.getAttachmentCols()){
            if(values.containsKey(columnName) && !values.containsKey("")){
                values.put(columnName+"_saved_in_local", 1);
            }
        }
//        ContentValues validatedValues = validateValues(values);
        SQLiteDatabase db = model.getWritableDatabase();
        if(transactionsListener != null){
            transactionsListener.onPreInsert(new Values().getValuesFrom(values));
        }
        long new_id = 0;
        try {
            new_id = insertOrUpdate(db, uri, model.getModelName(), values);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(transactionsListener != null){
            transactionsListener.onPostInsert(new Values().getValuesFrom(values));
        }
        notifyDataChange(model, uri);
        return Uri.withAppendedPath(uri, new_id + "");
    }

    private long insertOrUpdate(SQLiteDatabase db, Uri uri, String modelName, ContentValues values) throws Exception {
        long newId = -1;
        try{
            newId = db.insert(modelName, null, values);
        }catch (Exception e){
            newId = 0;
            int nmbreRows = update(uri, values, Col.SERVER_ID + "=?",
                    new String[]{values.getAsString(Col.SERVER_ID)});
            if(nmbreRows == 0){
                throw  new Exception("insert a line with an existing id");
            }
        }
        return newId;
    }

    private ContentValues validateValues(Model model, ContentValues values) {
        ContentValues contentValues = new ContentValues();
        contentValues.putAll(values);
        List<String> baseModelsKeys = new ArrayList<>();
        baseModelsKeys.add(Col.ROWID);
        if(!model.unAssigneFromModel()){
            baseModelsKeys.add(Col.REMOVED);
            baseModelsKeys.add(Col.ENABLED);
            baseModelsKeys.add(Col.LOCAL_WRITE_DATE);
            if(!values.containsKey(Col.ENABLED)) {
                contentValues.put(Col.ENABLED, 1);
            }
            if(!values.containsKey(Col.REMOVED)) {
                contentValues.put(Col.REMOVED, 1);
            }
            if(!values.containsKey(Col.LOCAL_WRITE_DATE)) {
                contentValues.put(Col.LOCAL_WRITE_DATE, MyUtil.getCurrentDate());
            }
        }
        for(Col col : model.getColumns()){
            if(!baseModelsKeys.contains(col.getName()) && !values.containsKey(col.getName())) {
                Object object = col.getDefaultValue();
                if (object.getClass().isAssignableFrom(Integer.class)) {
                    int i = Integer.valueOf(object.toString());
                    contentValues.put(col.getName(), i);
                } else if (object.getClass().isAssignableFrom(Boolean.class)) {
                    int b = object.equals(true) ? 1 : 0;
                    contentValues.put(col.getName(), b);
                } else {
                    contentValues.put(col.getName(), object.toString());
                }
            }else{
                contentValues.remove(Col.ROWID);
            }
        }
        return contentValues;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Model model = getModel(uri);
        SQLiteDatabase db = model.getWritableDatabase();
        if(transactionsListener != null){
            transactionsListener.onPreDelete(generateSelectionConditions(selection, selectionArgs));
        }
        int deleted = db.delete(model.getModelName(), selection, selectionArgs);
        if(transactionsListener != null){
            transactionsListener.onPostDelete(generateSelectionConditions(selection, selectionArgs));
        }
        notifyDataChange(model, uri);
        return deleted;
    }

    private String generateSelectionConditions(String selection, String[] selectionArgs) {
        String selectionConditions = selection;
        int i = 0;
        while (selectionConditions.contains("?")) {
            selectionConditions = selectionConditions.replaceFirst("\\?", selectionArgs[i]);
            i++;
        }
        return selectionConditions;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Model model = getModel(uri);
//        ContentValues valuesToUpdate = validateValues(values);
        if(values == null){
            values = new ContentValues();
        }
        if (!values.containsKey("_write_date")) {
            values.put("_write_date", MyUtil.getCurrentDate());
        }
        if (!values.containsKey("removed"))
            values.put("removed", 0);
        SQLiteDatabase db = model.getWritableDatabase();
        if(transactionsListener != null){
            transactionsListener.onPreUpdate(new Values().getValuesFrom(values), generateSelectionConditions(selection, selectionArgs));
        }
        Log.d("error_in_uri", uri.getPath());
        Log.d("error_in_modelName2", model.getModelName());
        Log.d("error_in_className2", model.getClass().getName());
        int updated = db.update(model.getModelName(), values, selection, selectionArgs);
        if(transactionsListener != null){
            transactionsListener.onPostUpdate(new Values().getValuesFrom(values), generateSelectionConditions(selection, selectionArgs));
        }
        notifyDataChange(model, uri);
        return updated;
    }

    public static Uri buildURI(String authority, String model, String username, boolean multi) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.appendPath(model);
        uriBuilder.appendQueryParameter(KEY_MODEL, model);
        uriBuilder.appendQueryParameter(KEY_USERNAME, username);
        String type = multi? "multi" : "single";
        uriBuilder.appendQueryParameter(KEY_TYPE, type);
        uriBuilder.scheme("content");
        return uriBuilder.build();
    }


    private void notifyDataChange(Model model, Uri uri) {
        // Send broadcast to registered ContentObservers, to refresh UI.
        assert getContext() != null;
        getContext().getContentResolver().notifyChange(uri, null);
//        model.getWritableDatabase().close();
    }

    public String setAuthority(){
        return null;
    }
}
