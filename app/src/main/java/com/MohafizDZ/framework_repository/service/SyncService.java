package com.MohafizDZ.framework_repository.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.MohafizDZ.empty_project.R;
import com.MohafizDZ.framework_repository.MohafizMainActivity;
import com.MohafizDZ.framework_repository.Utils.MySharedPreferences;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.service.receiver.ISyncFinishReceiver;
import com.MohafizDZ.framework_repository.service.receiver.ISyncStartReceiver;
import com.google.android.gms.common.util.concurrent.HandlerExecutor;

import java.util.concurrent.Executor;

public abstract class SyncService extends Service {
    public static final String TAG = SyncService.class.getSimpleName();
    private static final Object sSyncAdapterLock = new Object();
    private MAbstractThreadedSyncAdapter sSyncAdapter = null;
    private Context mContext;
    private SyncService service;
    private Model currentModel;
    private Bundle extras;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        new MySharedPreferences(mContext).setBoolean(MySharedPreferences.KEEP_SERVICE_RUNNING_KEY, true);
        service = this;
        Log.i(TAG, "Service created");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = getSyncAdapter(service, mContext);
                Intent intent = new Intent();
                currentModel = sSyncAdapter.onSetModel();
                intent.setAction(ISyncStartReceiver.SYNC_START);
                Bundle data = new Bundle();
                data.putString(ISyncStartReceiver.AUTHORITY_KEY, setAuthority());
                data.putString(ISyncStartReceiver.TYPE_KEY, "multi");
                data.putString(ISyncStartReceiver.MODEL_KEY, currentModel.getClass().getName());
                data.putString(ISyncStartReceiver.USERNAME_KEY, currentModel.getmUser().getAndroidAccountName());
                intent.putExtras(data);
                getApplicationContext().sendBroadcast(intent);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        String input = "syncing started from foreground service";
//        createNotificationChannel();
//        Intent notificationIntent = new Intent(this, MohafizMainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                0, notificationIntent, 0);
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("Foreground Service")
//                .setContentText(input)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentIntent(pendingIntent)
//                .build();
//        startForeground(1, notification);
        return START_REDELIVER_INTENT ;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Executor getCurrentExecutor() {
        Executor executor;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            executor = getMainExecutor();
        }else{
            executor = new HandlerExecutor(getMainLooper());
        }
        return executor;
    }

    public void setService(SyncService service) {
        this.service = service;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed");
        Log.i(currentModel.getModelName(), "Service destroyed");
        Intent intent = new Intent();
        intent.setAction(ISyncFinishReceiver.SYNC_FINISH);
        Bundle data = sSyncAdapter.getExtras() == null ? new Bundle() : sSyncAdapter.getExtras();
        data.putString(ISyncFinishReceiver.AUTHORITY_KEY, setAuthority());
        data.putString(ISyncFinishReceiver.TYPE_KEY, "multi");
        data.putString(ISyncFinishReceiver.MODEL_KEY, currentModel.getClass().getName());
        data.putString(ISyncFinishReceiver.USERNAME_KEY, currentModel.getmUser().getAndroidAccountName());
        intent.putExtras(data);
        getApplicationContext().sendBroadcast(intent);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        extras = intent.getExtras();
        return sSyncAdapter.getSyncAdapterBinder();
    }

//    @Override
//    public void onTaskRemoved(Intent rootIntent){
//        Log.d("Service:","I am being closed!");
//        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
//        restartServiceIntent.setPackage(getPackageName());
//
//        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
//        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//        alarmService.set(
//                AlarmManager.ELAPSED_REALTIME,
//                SystemClock.elapsedRealtime() + 1000,
//                restartServicePendingIntent);
//        super.onTaskRemoved(rootIntent);
//    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +1000, restartServicePI);
    }

    public abstract MAbstractThreadedSyncAdapter getSyncAdapter(SyncService service, Context context);

    public abstract void performDataSync(SyncAdapter adapter, Bundle extras);

    public abstract String setAuthority();


}
