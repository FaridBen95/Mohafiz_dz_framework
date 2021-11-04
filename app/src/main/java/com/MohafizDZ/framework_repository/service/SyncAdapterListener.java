package com.MohafizDZ.framework_repository.service;


import androidx.annotation.NonNull;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public interface SyncAdapterListener {
    void onSyncDownFinished(@NonNull Task<QuerySnapshot> task);
    void onUpdateDataBase();
    void onInsertOnServer(List<DataRow> rows);
    void onUpdateOnServer(List<DataRow> rows);
    void onSyncDownImagesFinished();
    void onSyncUpImagesFinished();
}
