package com.MohafizDZ.framework_repository.service.firebase;

import com.MohafizDZ.framework_repository.service.FirestoreSingleton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Map;


public class FirestoreSyncUpAdapter implements IFirestoreSync.SyncUpInputListener {
    private FirebaseFirestore firebaseFirestore;
    private IFirestoreSync.SyncUpOutputListener syncListener;
    private WriteBatch writeBatch;

    private FirestoreSyncUpAdapter(){

    }

    private FirestoreSyncUpAdapter(IFirestoreSync.SyncUpOutputListener syncListener){
        this.syncListener = syncListener;
        init();
    }

    private void init() {
        firebaseFirestore = FirestoreSingleton.get();
    }

    private FirestoreSyncUpAdapter startWriteBatch(){
        writeBatch = firebaseFirestore.batch();
        return this;
    }

    @Override
    public FirestoreSyncUpAdapter addUpdate(String collectionName, String id, Map<String, Object> records) {
        String path = id != null? String.valueOf(records.get("id")) : null;
        CollectionReference collectionReference = firebaseFirestore.collection(collectionName);
        DocumentReference documentReference = collectionReference.document(path);
        writeBatch.update(documentReference, records);
        return this;
    }

    @Override
    public FirestoreSyncUpAdapter addInsertOrUpdate(String collectionName, String id, Map<String, Object> records) {
        String path = id != null? String.valueOf(records.get("id")) : null;
        CollectionReference collectionReference = firebaseFirestore.collection(collectionName);
        DocumentReference documentReference = collectionReference.document(path);
        writeBatch.set(documentReference, records);
        return this;
    }

    @Override
    public FirestoreSyncUpAdapter addDelete(String collectionName, String id) {
        if(id != null) {
            CollectionReference collectionReference = firebaseFirestore.collection(collectionName);
            DocumentReference documentReference = collectionReference.document(id);
            writeBatch.delete(documentReference);
        }
        return this;
    }

    @Override
    public void commit(){
        syncListener.onSyncStarted();
        writeBatch.commit().addOnCompleteListener(task -> {
            boolean success = task.isSuccessful();
            if(!success){
                syncListener.onSyncFailed(task.getException());
            }
            syncListener.onSyncFinished(success);
        });
    }

    public static class Builder{
        FirestoreSyncUpAdapter firestoreSyncUpAdapter;
        public Builder(IFirestoreSync.SyncUpOutputListener syncListener){
            firestoreSyncUpAdapter = new FirestoreSyncUpAdapter(syncListener);
        }

        public FirestoreSyncUpAdapter startWriteBatch(){
            return firestoreSyncUpAdapter.startWriteBatch();
        }
    }
}
