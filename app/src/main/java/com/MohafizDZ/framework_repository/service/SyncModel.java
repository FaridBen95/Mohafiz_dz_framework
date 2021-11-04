package com.MohafizDZ.framework_repository.service;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Model;

public abstract class SyncModel extends SyncAdapter {

    public SyncModel(Context context, Model model) {
        super(context, model.getClass());
        setModel(model);
    }

    @Override
    protected void onSyncStarted() {
        super.onSyncStarted();
        onSyncStart(model);
    }

    @Override
    protected void onSyncFinished() {
        super.onSyncFinished();
        onSyncFinished(model);
    }

    @Override
    protected void onSyncFailed() {
        super.onSyncFailed();
        onSyncFailed(model);
    }

    public abstract void onSyncStart(Model model);

    public abstract void onSyncFinished(Model model);

    public abstract void onSyncFailed(Model model);

    @Override
    public boolean usePaging() {
        return super.usePaging();
    }
}
