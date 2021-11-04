package com.MohafizDZ.framework_repository.service;

import android.content.Context;
import android.net.Uri;

import com.MohafizDZ.empty_project.BuildConfig;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.Model;

public class SyncingReport extends Model {
    public static final String TAG = SyncingReport.class.getSimpleName();
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".syncing_report_provider";

    public Col model_name = new Col(Col.ColumnType.varchar);
    public Col last_sync_date = new Col(Col.ColumnType.varchar);
//    public Col synced = new Col(Col.ColumnType.bool).setLocalColumn();

    public SyncingReport(Context mContext) {
        super(mContext, "syncing_report");
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public Uri uri() {
        return buildUri(AUTHORITY);
    }
}
