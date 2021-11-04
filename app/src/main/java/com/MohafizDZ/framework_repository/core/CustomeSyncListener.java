package com.MohafizDZ.framework_repository.core;

import com.MohafizDZ.framework_repository.service.SyncingDomain;

public interface CustomeSyncListener {
    SyncingDomain setSyncingDomain();
    void onSyncStarted();
    void onSyncFinished();
    void onSyncImagesFinished();
}
