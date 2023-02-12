package com.MohafizDZ.framework_repository.core;

public interface DefaultSyncListener {
    void onSyncStarted();
    void onSyncFinished();
    void onSyncImagesFinished();
    void onSyncFailed();
}
