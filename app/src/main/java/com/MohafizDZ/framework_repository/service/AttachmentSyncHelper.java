package com.MohafizDZ.framework_repository.service;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Model;

public abstract class AttachmentSyncHelper extends SyncAttachmentAdapter {

    public AttachmentSyncHelper(Context context, Class<? extends Model> modelClass) {
        super(context, modelClass);
        onSyncStart(model);
    }

    @Override
    protected void onSyncFinished() {
        super.onSyncFinished();
        onSyncFinished(model);
    }

    public abstract void onSyncStart(Model model);

    public abstract void onSyncFinished(Model model);
}
