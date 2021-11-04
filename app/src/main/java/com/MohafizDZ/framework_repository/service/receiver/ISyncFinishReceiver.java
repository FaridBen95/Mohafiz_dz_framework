package com.MohafizDZ.framework_repository.service.receiver;

import android.content.BroadcastReceiver;

public abstract class ISyncFinishReceiver extends BroadcastReceiver {
    public static final String SYNC_FINISH = "ISyncFinishReceiver.SYNC_FINISH";
    public static final String AUTHORITY_KEY = "authority";
    public static final String MODEL_KEY = "model";
    public static final String USERNAME_KEY = "username";
    public static final String TYPE_KEY = "username";
}
