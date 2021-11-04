package com.MohafizDZ.framework_repository.service.firestore;

import android.util.Pair;

import androidx.annotation.NonNull;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.service.FirestoreSingleton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FirestoreSyncDownAdapter {
    private FirebaseFirestore firebaseFirestore;
    private Integer limit;
    private String collectionName;
    private FirestoreListener firestoreListener;

    public void setFirestoreListener(FirestoreListener firestoreListener) {
        this.firestoreListener = firestoreListener;
    }

    public FirestoreSyncDownAdapter(Integer limit, String collectionName) {
        this.limit = limit;
        this.collectionName = collectionName;
        init();
    }

    private void init() {
        firebaseFirestore = FirestoreSingleton.get();
    }

    public void sync(){
        sync(false);
    }

    public void sync(boolean nextPage){
        Query query = firebaseFirestore.collection(collectionName);
        if(firestoreListener != null && firestoreListener.orderByWriteDate()){
            query = query.orderBy("write_date", Query.Direction.DESCENDING);
        }
        query = firestoreListener != null?
                prepareQuery(firestoreListener.setQuery(), query) : query;
        if(nextPage && firestoreListener != null){
            DocumentSnapshot documentSnapshot = firestoreListener.setOffset();
            if(documentSnapshot != null) {
                query = query.startAfter(documentSnapshot);
            }
        }
        if(limit != null && limit != 0 && limit != -1){
            query = query.limit(limit);
        }
        query.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                if(task.getResult().getDocuments().size() != 0) {
                    if (firestoreListener != null) {
                        List<Map<String, Object>> datas = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                            datas.add(documentSnapshot.getData());
                        }
                        firestoreListener.onResult(datas,
                                task.getResult().getDocuments().get(task.getResult().getDocuments().size() - 1));
                    }
                }else if (task.getResult().getMetadata().isFromCache()){
                    firestoreListener.onException(new Exception("Slow Internet connection"));
                }else{
                    firestoreListener.onResult(new ArrayList<>(),
                            null);
                }
            }else{
                firestoreListener.onException(task.getException());
            }
        });
    }

    public Query prepareQuery(List<QueryClause> queries, Query query){
        for(QueryClause queryClause : queries){
            String fieldName = queryClause.getFieldName();
            Object arg = queryClause.getArg();
            switch (queryClause.getOperator()){
                case equalTo:
                    query = query.whereEqualTo(fieldName, arg);
                    break;
                case whereIn:
                    query = query.whereIn(fieldName, (List<? extends Object>) arg);
                    break;
                case arrayContains:
                    query = query.whereArrayContains(fieldName, arg);
                    break;
                case arrayContainsAny:
                    query = query.whereArrayContainsAny(fieldName, (List<? extends Object>) arg);
                    break;
                case greaterOrEqualThan:
                    query = query.whereGreaterThanOrEqualTo(fieldName, arg);
                    break;
                case greaterThan:
                    query = query.whereGreaterThan(fieldName, arg);
                    break;
                case lessOrEqualThan:
                    query = query.whereLessThanOrEqualTo(fieldName, arg);
                    break;
                case lessThan:
                    query = query.whereLessThan(fieldName, arg);
                    break;
                case likeStartWith:
                    query = query.orderBy(fieldName).startAt(arg).endAt(arg+ "\uf8ff");
                    break;
                case startAt:
                    query = query.orderBy(fieldName).startAt(arg);
                    break;
                case endAt:
                    query = query.orderBy(fieldName).endAt(arg);
                    break;
                case orderAsc:
                    query = query.orderBy(fieldName, Query.Direction.ASCENDING);
                    break;
                case orderDesc:
                    query = query.orderBy(fieldName, Query.Direction.DESCENDING);
                    break;
            }
        }
        return query;
    }

    public Task<QuerySnapshot> syncRel(Model relModel, Col col, List<String> ids) {
        String fieldName;
        if(col.getColumnType() == Col.ColumnType.one2many){
            fieldName = col.getRelatedColumn();
        }else{
            fieldName = Col.SERVER_ID;
        }
        return firebaseFirestore.collection(relModel.getModelName()).
                whereIn(fieldName, ids).get();
    }

    public void syncRelInTransaction(List<Task<QuerySnapshot>> tasks,final Map<String, Pair<Col, Model>> modelNameModelList) {
        Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {
                List<Pair<String, List<Map<String, Object>>>> records = new ArrayList<>();
                for(Task t : task.getResult()) {
                    List<Map<String, Object>> recordList = new ArrayList<>();
                    for(DocumentSnapshot documentSnapshot : ((QuerySnapshot)t.getResult()).getDocuments()) {
                        Map<String, Object> recordMap = documentSnapshot.getData();
                        recordList.add(recordMap);
                    }
                    String pathName =recordList.size() != 0? ((QuerySnapshot)t.getResult()).getDocuments().get(0).getReference().getParent().getPath()
                            : null;
                    if(pathName == null) continue;
                    Pair<String, List<Map<String, Object>>> pathNameRecordMap = new Pair<>(pathName, recordList);
                    records.add(pathNameRecordMap);
                }
                firestoreListener.onRelResult(records, modelNameModelList);
            }
        });
    }

    public interface FirestoreListener{
        boolean isSyncable(Col col);
        void onException(Exception exception);
        void onResult(List<Map<String, Object>> records, DocumentSnapshot lastDoc);
        void onRelResult(Map<String, Map<String, DataRow>> resultMap);
        void onRelResult(List<Pair<String, List<Map<String, Object>>>> records, Map<String, Pair<Col, Model>> modelNameModelList);
        DocumentSnapshot setOffset();
        List<QueryClause> setQuery();
        boolean orderByWriteDate();
    }
}
