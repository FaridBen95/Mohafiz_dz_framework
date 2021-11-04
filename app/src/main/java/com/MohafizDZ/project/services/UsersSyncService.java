package com.MohafizDZ.project.services;

import android.content.Context;
import android.os.Bundle;

import com.MohafizDZ.framework_repository.service.MAbstractThreadedSyncAdapter;
import com.MohafizDZ.framework_repository.service.SyncAdapter;
import com.MohafizDZ.framework_repository.service.SyncService;
import com.MohafizDZ.project.models.UserModel;

public class UsersSyncService extends SyncService {
    public static final String TAG = UsersSyncService.class.getSimpleName();

    @Override
    public MAbstractThreadedSyncAdapter getSyncAdapter(SyncService service, Context context) {
        return new SyncAdapter(context, UserModel.class, null, true);
    }

    @Override
    public void performDataSync(SyncAdapter adapter, Bundle extras) {

    }

    @Override
    public String setAuthority() {
        return UserModel.AUTHORITY;
    }
}
