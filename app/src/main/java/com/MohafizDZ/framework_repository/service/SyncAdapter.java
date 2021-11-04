package com.MohafizDZ.framework_repository.service;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.MohafizDZ.App;
import com.MohafizDZ.framework_repository.MohafizMainActivity;
import com.MohafizDZ.framework_repository.Utils.MySharedPreferences;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Account.MUser;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.DefaultSyncListener;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class SyncAdapter extends MAbstractThreadedSyncAdapter {
    public final String TAG = SyncAdapter.class.getSimpleName();

    private Context context;
    protected Model model;
    private RecordHandler recordHandler;
    private ContentResolver contentResolver;
    private DefaultSyncListener defaultSyncListener;
    private SyncAdapter.SyncState syncState = SyncState.sync;
    private MFirestoreWrapper firestoreWrapper;
    private MySharedPreferences sharedPref;
    private String authorizationToken;
    private String authorizationTokenWriteDate;
    private CountDownLatch doneSignal = new CountDownLatch(1);
    private App app;

    @Override
    public Model onSetModel() {
        return model;
    }

    private enum SyncState {sync, syncUp}

    public SyncAdapter(Context context, Class<? extends Model> modelClass){
        this(context, modelClass, null);
    }

    public SyncAdapter(Context context, Class<? extends Model> modelClass, DefaultSyncListener defaultSyncListener){
        this(context, modelClass, defaultSyncListener, false);
    }

    public SyncAdapter(Context context, Class<? extends Model> modelClass, DefaultSyncListener defaultSyncListener, boolean autoInitialize){
        super(context, autoInitialize);
        init(context, modelClass);
    }

    public SyncAdapter(Context context, Class<? extends Model> modelClass, DefaultSyncListener defaultSyncListener,
                       boolean autoInitialize, boolean allowParallelSyncs){
        super(context, autoInitialize, allowParallelSyncs);
        init(context, modelClass);
    }

    private void init(Context context, final Class<? extends Model> modelClass) {
        this.context = context;
        this.app = (App)context.getApplicationContext();
        this.contentResolver = context.getApplicationContext().getContentResolver();
        this.model = Model.createInstance(context, modelClass);
        if(model.canSaveLastSyncDate() == null) {
            this.model.setCanSaveLastSyncDate(true);
        }
        sharedPref = new MySharedPreferences(context);
        firestoreWrapper = new MFirestoreWrapper(context);
        recordHandler = new RecordHandler(context, model) {
            @Override
            protected Values prepareUpdateValues(Values values) {
                return model.prepareUpdateValues(values);
            }

            @Override
            protected Values prepareInsertValues(Values values) {
                return model.prepareInsertValues(values);
            }

            @Override
            protected void getCurrentQueryLength(int currentSyncDownQueryIndex, int length) {
                SyncAdapter.this.getCurrentQueryLength(currentSyncDownQueryIndex, length);
            }
        };
        /*recordHandler = new RecordHandler(context, model) {
            @Override
            protected Values prepareUpdateValues(Values values) {
                Log.d(TAG, "prepareUpdateValues");
                return model.prepareUpdateValues(values);
            }

            @Override
            protected Values prepareInsertValues(Values values) {
                Log.d(TAG, "prepareInsertValues");
                return model.prepareInsertValues(values);
            }

            @Override
            protected void onHandlingInsertAndUpdateDone() {
                Log.d(TAG, "onHandlingInsertAndUpdateDone");
                if(model.allowDeleteInLocal()) {
                    deleteInLocal();
                }
            }

            @Override
            protected void onHandlingDeleteDone() {
                Log.d(TAG, "onHandlingDeleteDone");
                performSyncUp();
            }

            @Override
            protected void performDeleteOnServer(String[] idsToDelete, StringBuilder argsString) {
                Log.d(TAG, "performDeleteOnServer");
                if(model.allowDeleteRecordsOnServer()){
                    syncUpDeletion(idsToDelete, argsString);
                }
            }

            @Override
            protected void recordsUpdatedOnServer() {
                Log.d(TAG, "recordsUpdatedOnServer");
                if(syncState == SyncAdapter.SyncState.syncAndUpdateInLocal) {
                    onSyncFinished(domainIndex);
                }
                if(syncState == SyncAdapter.SyncState.syncUp){
                    onSyncFinished();
                }
            }

            @Override
            protected void onDeleteNonExistentRows() {
                Log.d(TAG, "onDeleteNonExistentRows");
                onSyncFinished();
            }

            @Override
            protected void unSuccessful() {
                Log.d(TAG, "unSuccessful");
                model.setSyncing(false);
            }
        };*/
    }

    public void setDefaultSyncListener(DefaultSyncListener defaultSyncListener) {
        this.defaultSyncListener = defaultSyncListener;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        super.onPerformSync(account, extras, authority, provider, syncResult);
        performSync(null, null, null,
                null, null, null, null, null, null, null, null, null);
    }

    public void performSync(ModelHelper modelHelper){
        performSync(modelHelper.getCustomDomain(), modelHelper.isCanSyncDown(),
                modelHelper.isCanSyncDownRelations(), modelHelper.isAllowDeleteInLocal(),
                modelHelper.isAllowDeleteOnServer(), modelHelper.isCanSyncUp(),
                modelHelper.isCanSyncUpRelations(),
                modelHelper.isAllowRemoveOutOfDomain(), modelHelper.getLimit(),
                modelHelper.getLikeValue(), modelHelper.getLikeFields(),
                modelHelper.getOrderBy());
    }

    public void performSync(SyncingDomain customDomain, Boolean canSyncDown, Boolean canSyncDownRelations,
                            Boolean allowDeleteInLocal, Boolean allowDeleteOnServer,
                            Boolean canSyncUp, Boolean canSyncUpRelations, Boolean allowRemoveOutOfDomain, Integer limit,
                            String likeValue, List<String> likeFields, OrderBy orderBy) {
        if(app.forceAutomaticDate() && !app.dateIsCorrect()){
            return;
        }
        if (model.isOnline()) {
            authorizationToken = sharedPref.getString(MUser.AUTH_TOKEN_KEY, null);
            authorizationTokenWriteDate = sharedPref.getString(MUser.AUTH_TOKEN_WRITE_DATE_KEY, null);
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if(firebaseUser != null && (app).inNetwork() && authorizationExpired()){
                firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        try {
                            MySharedPreferences mySharedPreferences = new MySharedPreferences(context.getApplicationContext());
                            String lastToken = mySharedPreferences.getString(MUser.AUTH_TOKEN_KEY, "");
                            String currentToken = task.getResult().getToken();
                            if(!lastToken.equals(currentToken)) {
                                mySharedPreferences.putString(MUser.AUTH_TOKEN_KEY, currentToken);
                                authorizationToken = currentToken;
                                authorizationTokenWriteDate = MyUtil.getCurrentDate();
                                mySharedPreferences.putString(MUser.AUTH_TOKEN_WRITE_DATE_KEY, authorizationTokenWriteDate);
                            }
                        } catch (Exception ignored) {
                        }
                        doneSignal.countDown();
                    }
                });
                try {
                    doneSignal.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(defaultSyncListener != null){
                defaultSyncListener.onSyncStarted();
            }
            model.setSyncing(true);
            onSyncStarted();
            SyncingDomain syncingDomain;
            if(customDomain == null) {
                DataRow syncingRow = new SyncingReport(context).browse(" model_name = ? ",
                        new String[]{model.getModelName()});
                String lastSyncDate = syncingRow != null ? syncingRow.getString("last_sync_date") : "";
                String firstRunDate = sharedPref.getString(MohafizMainActivity.FIRST_RUN_DATE, "");
                long lastSyncDateInMillis = MyUtil.dateToMilliSec(lastSyncDate);
                long firstRunTimeInMillis = MyUtil.dateToMilliSec(firstRunDate);
                SyncingDomain domain = new SyncingDomain();
                if (model.checkWriteDate()) {
                    long timeInMillis = model.syncUsingFirstRunDate()? Math.max(lastSyncDateInMillis, firstRunTimeInMillis) : lastSyncDateInMillis;
                    domain.addOperation("write_date", SyncingDomain.Operation.greaterThan,
                            timeInMillis, false);
                }
                syncingDomain = model.setDefaultDomain(domain);
            }else{
                syncingDomain = customDomain;
            }
            try {
                canSyncDown = canSyncDown == null? model.allowSyncDown() : canSyncDown;
                if(canSyncDown && syncingDomain != null) {
                    syncAndUpdateInLocal(syncingDomain, limit, likeValue, likeFields, orderBy);
                }
                canSyncDownRelations = canSyncDownRelations == null? model.canSyncDownRelations() : canSyncDownRelations;
                if(canSyncDownRelations){
                    syncDownRelations();
                }
                allowDeleteInLocal = allowDeleteInLocal == null? model.allowDeleteInLocal() : allowDeleteInLocal;
                if(allowDeleteInLocal) {
                    deleteInLocal();
                }
                allowDeleteOnServer = allowDeleteOnServer == null? model.allowDeleteRecordsOnServer() : allowDeleteOnServer;
                if(allowDeleteOnServer) {
                    performDeleteOnServer();
                }
                canSyncUp = canSyncUp == null? model.allowSyncUp() : canSyncUp;
                if(canSyncUp) {
                    performSyncUp();
                }
                canSyncUpRelations = canSyncUpRelations == null? model.canSyncUpRelations() : canSyncUpRelations;
                if(canSyncUpRelations) {
                    syncUpRelations();
                }
                allowRemoveOutOfDomain = allowRemoveOutOfDomain == null? model.allowRemoveRecordsOutOfDomain(): allowRemoveOutOfDomain;
                if(allowRemoveOutOfDomain) {
                    recordHandler.deleteNonExistentRows(syncingDomain);
                }
                if(model.canSaveLastSyncDate() != null && model.canSaveLastSyncDate()) {
                    recordHandler.saveSyncingDate(model.getModelName(), recordHandler.savingDate);
                }
                onSyncFinished();
                model.onSyncFinished();
            } catch (Exception e) {
                e.printStackTrace();
                onSyncFailed();
            }
            model.setSyncing(false);
            notifyDataChange(model);
        }
    }

    protected void onSyncStarted() {
    }

    private boolean authorizationExpired() {
        Date before56Min = MyUtil.createDateObject(MyUtil.getDateBeforeMins(56), MyUtil.DEFAULT_DATE_FORMAT, false);
        Date lastWriteDate = MyUtil.createDateObject(authorizationTokenWriteDate, MyUtil.DEFAULT_DATE_FORMAT, false);
        return authorizationToken == null || lastWriteDate == null || before56Min.compareTo(lastWriteDate) >= 0;
    }

    private void syncAndUpdateInLocal(SyncingDomain syncingDomain, Integer limit, String likeValue, List<String> likeFields, OrderBy orderBy) throws Exception {
        _syncAndUpdateInLocal(syncingDomain, limit == null? model.syncingLimit() : limit,
                likeValue == null? model.getLikeValue() : likeValue,
                likeFields == null? model.getLikeFields() : likeFields,
                orderBy == null? model.setOrderBy() : orderBy);
    }
    private void _syncAndUpdateInLocal(SyncingDomain syncingDomain, int limit, String likeValue,
                                       List<String> likeFields, OrderBy orderBy) throws Exception {
        List<FilterObject> queries = syncingDomain.getFilterObjects();
        recordHandler.reset();
        boolean containsError = false;
        int lastLimit = 0;
        //todo in or conditions won't work as expected because the limit are going to be divised on the queries.size for each query
        syncDownQueriesSize(queries.size());
        for(int i = 0;i < queries.size(); i++){
            FilterObject filterObject = queries.get(i);
            int currentLimit;
            String postData;
            if(!usePaging()) {
                currentLimit = (limit - lastLimit) / (queries.size() - i);
                lastLimit = currentLimit;
                postData = firestoreWrapper.generateReadQuery(model.getModelName(), filterObject, currentLimit, likeFields, likeValue, orderBy);
            }else{
                PagingObject pagingObject = setPagingParams(i);
                if(pagingObject == null){
                    continue;
                }
                currentLimit = pagingObject.getLimit();
                int offset = pagingObject.getOffset();
                if(currentLimit == -1){
                    continue;
                }
                postData = firestoreWrapper.generateReadQuery(model.getModelName(), filterObject, currentLimit, offset, likeFields, likeValue, orderBy);
            }
            MFirestoreResponse response = new MFirestoreResponse();
            firestoreWrapper.newJSONPOSTRequest(MFirestoreWrapper.RUN_QUERY_URL, postData,
                    authorizationToken, response);
            if(response.containsError()){
                Log.d(TAG, "Error : " + response.getError().toString());
                model.setSyncing(false);
                containsError = true;
                break;
            }else{
                recordHandler.setCurrentSyncDownQueryIndex(i);
                recordHandler.collectResponses(response.getResponse());
            }
        }
        if(!containsError) {
            recordHandler.updateInLocal();
            recordHandler.reset();
        }else{
            throw new Exception("Error occurred while recovering data");
        }
    }

    public void syncDownQueriesSize(int size) {

    }

    public PagingObject setPagingParams(int queryIndex) {
        return null;
    }

    public boolean usePaging() {
        return false;
    }

    private void syncDownRelations() throws Exception {
        syncManyToOneRecords(recordHandler.getManyToOneIdsMap());
        syncOneToManyRecords(recordHandler.getOneToManyIdsMap());
    }

    private void syncManyToOneRecords(Map<String, List<String>> manyToOneIdsMap) throws Exception {
        for(String colName : manyToOneIdsMap.keySet()){
            Col col = model.getColumn(colName);
            if (col != null) {
                List<String> ids = manyToOneIdsMap.get(colName);
                if(ids != null && ids.size() != 0) {
                    Model relModel = Model.createInstance(context, col.getRelationalModel());
                    if(relModel == null){
                        throw new Exception("null model exception");
                    }
                    SyncingDomain domain = new SyncingDomain();
                    domain = domain.addWhereInOperation(Col.SERVER_ID, ids, false);
                    List<FilterObject> queries = domain.getFilterObjects();
                    relModel.setSyncing(true);
                    RecordHandler.RelRecordsHandler relRecordsHandler = recordHandler.createRelRecordHandlerInstance(relModel);
                    for(int i = 0;i < queries.size(); i++) {
                        FilterObject filterObject = queries.get(i);
                        String postData = firestoreWrapper.generateReadQuery(relModel.getModelName(),
                                filterObject);
                        MFirestoreResponse response = new MFirestoreResponse();
                        firestoreWrapper.newJSONPOSTRequest(MFirestoreWrapper.RUN_QUERY_URL, postData,
                                authorizationToken, response);
                        if(response.containsError()){
                            Log.d(TAG, "Error : " + response.getError().toString());
                            relModel.setSyncing(false);
                            throw new Exception("Error in response" + response.getError());
                        }else{
                            relRecordsHandler.collectResponses(response.getResponse());
                        }
                    }
                    relRecordsHandler.updateInLocal();
                    relModel.onSyncFinishedFromRel(model);
                }
            }
        }
    }

    private void syncOneToManyRecords(Map<String, List<String>> oneToManyIdsMap) throws Exception {
        for(String colName : oneToManyIdsMap.keySet()){
            Col col = null;
            try {
                Field colField = model.getClass().getField(colName);
                col = model.getColumn(colField);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            if (col != null) {
                List<String> ids = oneToManyIdsMap.get(colName);
                if(ids != null && ids.size() != 0) {
                    Model relModel = Model.createInstance(context, col.getRelationalModel());
                    if(relModel == null){
                        throw new Exception("null model exception");
                    }
                    SyncingDomain domain = new SyncingDomain();
                    domain = domain.addWhereInOperation(col.getRelatedColumn(), ids, false);
                    List<FilterObject> queries = domain.getFilterObjects();
                    relModel.setSyncing(true);
                    RecordHandler.RelRecordsHandler relRecordsHandler = recordHandler.createRelRecordHandlerInstance(relModel);
                    for(int i = 0;i < queries.size(); i++) {
                        FilterObject filterObject = queries.get(i);
                        String postData = firestoreWrapper.generateReadQuery(relModel.getModelName(),
                                filterObject);
                        MFirestoreResponse response = new MFirestoreResponse();
                        firestoreWrapper.newJSONPOSTRequest(MFirestoreWrapper.RUN_QUERY_URL, postData,
                                authorizationToken, response);
                        if(response.containsError()){
                            Log.d(TAG, "Error : " + response.getError().toString());
                            relModel.setSyncing(false);
                            throw new Exception("Error in response" + response.getError());
                        }else{
                            relRecordsHandler.collectResponses(response.getResponse());
                        }
                    }
                    relRecordsHandler.updateInLocal();
                    relModel.onSyncFinishedFromRel(model);
                }
            }
        }
    }

    private void deleteInLocal() throws Exception{
        final String deletionModelName = model.getModelName() + "_deletion";
        DataRow syncingRow = new SyncingReport(context).browse(
                " model_name = ? ", new String[]{deletionModelName});
        String firstRunDate = sharedPref.getString(MohafizMainActivity.FIRST_RUN_DATE, "");
        String lastSyncDate = syncingRow != null ? syncingRow.getString("last_sync_date") : "";
        long firstRunTimeInMillis = MyUtil.dateToMilliSec(firstRunDate);
        long lastSyncDateInMillis = MyUtil.dateToMilliSec(lastSyncDate);
        long timeInMillis = model.syncUsingFirstRunDate()? Math.max(lastSyncDateInMillis, firstRunTimeInMillis) : lastSyncDateInMillis;
        FilterObject filterObject = new FilterObject();
        FieldFilter fieldFilter = new FieldFilter("write_date", FieldFilter.getOperation(SyncingDomain.Operation.greaterThan),
                timeInMillis);
        filterObject.add(fieldFilter);
        recordHandler.reset();
        String postData = firestoreWrapper.generateReadQuery(deletionModelName, filterObject);
        MFirestoreResponse response = new MFirestoreResponse();
        firestoreWrapper.newJSONPOSTRequest(MFirestoreWrapper.RUN_QUERY_URL, postData,
                authorizationToken, response);
        if(response.containsError()){
            Log.d(TAG, "Error : " + response.getError().toString());
            model.setSyncing(false);
        }else{
            recordHandler.handleDeletion(response.getResponse());
            final String currentDate = MyUtil.getCurrentDate();
            recordHandler.saveSyncingDate(deletionModelName, currentDate);
        }
    }

    private void performDeleteOnServer() throws JSONException {
        String deleteWhere = recordHandler.getDeleteArgs();
        if(deleteWhere != null && !deleteWhere.equals("")) {
            Map<String, Object> record = new HashMap<>();
            record.put("write_date", MyUtil.dateToMilliSec(MyUtil.getCurrentDate()));
            record.put("where", deleteWhere);
            String url = firestoreWrapper.generatePatchURL(model.getModelName() + "_deletion",
                    Model.getCreateXId(context), "write_date", "where");
            String postData = firestoreWrapper.generatePatchQuery(record);
            MFirestoreResponse response = new MFirestoreResponse();
            firestoreWrapper.newJSONPATCHRequest(url, postData, authorizationToken
                    , response);
            if (response.containsError()) {
                Log.d(TAG, "Error : " + response.getError().toString());
                model.setSyncing(false);
            } else {
                deleteOnServer();
            }
        }
    }

    private void deleteOnServer() throws JSONException {
        List<DataRow> rowsToDelete = recordHandler.getRowsToDelete();
        List<String> ids = new ArrayList<>();
        for(DataRow row : rowsToDelete){
            if(!ids.contains(row.getString(Col.SERVER_ID))){
                ids.add(row.getString(Col.SERVER_ID));
            }
        }
        String url = MFirestoreWrapper.COMMIT_UPDATES_URL;
        String postData = firestoreWrapper.generateBatchWriteDeleteQuery(model.getModelName(), ids);
        MFirestoreResponse response = new MFirestoreResponse();
        firestoreWrapper.newJSONBatchWriteRequest(url, postData, authorizationToken
                , response);
        if(response.containsError()){
            Log.d(TAG, "Error : " + response.getError().toString());
            model.setSyncing(false);
        }else{
            recordHandler.deletePermanently();
        }
        recordHandler.resetDeletion();
    }


    private void performSyncUp() throws Exception {
        recordHandler.setServerUpdateDate(MyUtil.dateToMilliSec(MyUtil.getCurrentDate()));
        Map<String, Map<String, Object>> toUpdateOnServerRecords = recordHandler.prepareSyncUpRecords();
        recordHandler.setUpdatedDate(MyUtil.getCurrentDate());
        if(!model.forceOverwriteOnServer()) {
            List<String> ids = recordHandler.getIdsToUpdateOnServer();
            Map<String, String> writeDates = recordHandler.getWriteDateOfRecordsToUpdateOnServer();
            SyncingDomain domain = new SyncingDomain();
            if(ids.size() != 0) {
                domain = domain.addWhereInOperation(Col.SERVER_ID, ids, false);
                String url = MFirestoreWrapper.RUN_QUERY_URL;
                for (FilterObject query : domain.getFilterObjects()) {
                    String postData = firestoreWrapper.generateReadQuery(model.getModelName(), query);
                    MFirestoreResponse response = new MFirestoreResponse();
                    firestoreWrapper.newJSONPOSTRequest(url, postData, authorizationToken, response);
                    if (response.containsError()) {
                        Log.d(TAG, "Error : " + response.getError().toString());
                        model.setSyncing(false);
                        throw new Exception("runQuery returns error " + response.getError());
                    } else {
                        toUpdateOnServerRecords = recordHandler.checkWriteDate(response.getResponse(), toUpdateOnServerRecords, writeDates);
                    }
                }
            }
        }
        if (toUpdateOnServerRecords.size() != 0) {
            String url = MFirestoreWrapper.COMMIT_UPDATES_URL;
            String postData = firestoreWrapper.generateBatchWriteUpdateQuery(model.getModelName(), toUpdateOnServerRecords);
            MFirestoreResponse response = new MFirestoreResponse();
            firestoreWrapper.newJSONBatchWriteRequest(url, postData, authorizationToken
                    , response);
            if (response.containsError()) {
                Log.d(TAG, "Error : " + response.getError().toString());
                model.setSyncing(false);
            } else {
                List<String> updatedServerIds = new ArrayList<>();
                for (Map<String, Object> record : toUpdateOnServerRecords.values()) {
                    updatedServerIds.add(String.valueOf(record.get(Col.SERVER_ID)));
                }
                recordHandler.recordsUpdatedOnServer(updatedServerIds);
            }
        }
    }

    private void syncUpRelations() {

    }

    protected void onSyncFinished(){

    }

    protected void onSyncFailed() {

    }

    public void setModel(Model model) {
        this.model = model;
        recordHandler.setModel(model);
    }
}