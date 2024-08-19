package com.MohafizDZ.framework_repository.service.firebase;

import com.MohafizDZ.framework_repository.core.DataRow;

import java.util.List;
import java.util.Map;

public interface IFirestoreSync {

    interface SyncUpInputListener {
        FirestoreSyncUpAdapter addUpdate(String collectionName, String id, Map<String, Object> records);

        FirestoreSyncUpAdapter addInsertOrUpdate(String collectionName, String id, Map<String, Object> records);

        FirestoreSyncUpAdapter addDelete(String collectionName, String id);

        void commit();
    }

    interface SyncUpOutputListener {
        void onSyncStarted();
        void onSyncFinished(boolean success);
        void onSyncFailed(Exception exception);
    }

    interface SyncBridgeListener{
        void sync(List<DataRow> rows, FirestoreSyncUpBridge.SyncListener syncListener);
    }

    interface SyncDownListener{
        void onException(Exception exception);

        void onResult(List<Map<String, Object>> data);
    }
}
