package com.MohafizDZ.framework_repository.service;

import android.os.Bundle;

import com.MohafizDZ.framework_repository.core.Model;

public interface MSyncStatusObserverListener {
    public static final String TAG = MSyncStatusObserverListener.class.getSimpleName();

    void onSyncStart(Bundle data, Model model);

    void onSyncFinish(Bundle data, Model model);
}
