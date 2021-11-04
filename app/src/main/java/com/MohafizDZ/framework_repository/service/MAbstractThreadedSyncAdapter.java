package com.MohafizDZ.framework_repository.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;

import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.service.receiver.ISyncStartReceiver;

import java.util.concurrent.Executor;

public abstract class MAbstractThreadedSyncAdapter extends AbstractThreadedSyncAdapter {
    private Bundle extras;
    private Executor executor;

    public MAbstractThreadedSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public MAbstractThreadedSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    public abstract Model onSetModel();

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        this.extras = extras;
    }

    public Bundle getExtras() {
        return extras;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Executor getExecutor() {
        return executor;
    }


    void notifyDataChange(Model model) {
        // Send broadcast to registered ContentObservers, to refresh UI.
        assert getContext() != null;
        getContext().getContentResolver().notifyChange(model.uri(), null);
    }

    public  void getCurrentQueryLength(int currentSyncDownQueryIndex, int length){

    }
}
