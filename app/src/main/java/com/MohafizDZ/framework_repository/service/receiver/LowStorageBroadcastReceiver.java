package com.MohafizDZ.framework_repository.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.MohafizDZ.empty_project.R;

public class LowStorageBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = LowStorageBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, R.string.low_storage_error_message, Toast.LENGTH_LONG);
    }
}
