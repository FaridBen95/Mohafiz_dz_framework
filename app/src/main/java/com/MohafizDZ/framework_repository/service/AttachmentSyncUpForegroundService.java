package com.MohafizDZ.framework_repository.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.MohafizDZ.App;
import com.MohafizDZ.empty_project.R;
import com.MohafizDZ.framework_repository.core.Account.MainLogInActivity;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.MohafizDZ.project.models.AttachmentLocalModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.util.List;

public abstract class AttachmentSyncUpForegroundService extends Service {
    public static final String TAG = AttachmentSyncUpForegroundService.class.getSimpleName();

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private AttachmentLocalModel attachmentModel;
    private DataRow currentUserRow;
    private Notification syncNotification;
    private Bundle extras;

    public AttachmentSyncUpForegroundService() {
        init();
    }

    public void init(){
        this.firebaseStorage = FirebaseStorageSingleton.get();
        this.storageReference = firebaseStorage.getReference(MConstants.FIREBASE_STORAGE_LINK);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "My foreground service onCreate().");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            extras = intent.getExtras();
            if(action!=null)
                switch (action) {
                    case ACTION_START_FOREGROUND_SERVICE:
                        startForegroundService();
                        break;
                    case ACTION_STOP_FOREGROUND_SERVICE:
                        stopForegroundService();
                        break;
                }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /* Used to build and start foreground service. */
    private void startForegroundService() {
        Log.d(TAG, "Start foreground service.");
        showNotification();
        syncUpAttachments();
    }

    private void showNotification() {

        syncNotification = getMyActivityNotification(getResources().getString(R.string.preparing_data));
        startForeground(NOTIFICATION_ID.FOREGROUND_SERVICE,
                syncNotification);

    }


    private Notification getMyActivityNotification(String text){
        // The PendingIntent to launch our activity if the user selects
        // this notification
        Intent notificationIntent = new Intent(this, MainLogInActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Intent intent = setNotificationIntent(notificationIntent, extras);
        notificationIntent = intent == null? notificationIntent : intent;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        CharSequence title = getResources().getString(R.string.uploading_attachments);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher_foreground);
        String channelId = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            channelId = createNotificationChannel(TAG, "My Background Service");
        }
        if(channelId == null) {
            return new NotificationCompat.Builder(this)
                    .setContentTitle(title)
                    .setTicker(title)
                    .setContentText(text)
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true).build();
        }else{
            return new NotificationCompat.Builder(this)
                    .setChannelId(channelId)
                    .setContentTitle(title)
                    .setTicker(title)
                    .setContentText(text)
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true).build();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    private void syncUpAttachments() {
        attachmentModel = new AttachmentLocalModel(getApplicationContext());
        currentUserRow = ((App) getApplicationContext()).getCurrentUser();
        final StorageReference userReference = storageReference.child(currentUserRow.getString(Col.SERVER_ID));
        String selection = " is_uploaded_to_server = ? ";
        String[] args = {"0"};
        final List<DataRow> rows = attachmentModel.select(setAttachmentsSelection(selection), setAttachmentsArgs(args));
        SyncHelper syncHelper = new SyncHelper(rows) {
            @Override
            public void sync(DataRow row, int index) {
                String path = row.getString("path");
                Uri uri = Uri.fromFile(new File(path));
                StorageReference fireStorageRef = userReference.child(path);
                fireStorageRef.putFile(uri).addOnCompleteListener(this);
                String text = getResources().getString(R.string.upload_title) + (index + 1) + "/" + rows.size();
                syncNotification = getMyActivityNotification(text);
                startForeground(NOTIFICATION_ID.FOREGROUND_SERVICE,
                        syncNotification);
            }

            @Override
            public void onSyncFinished() {
                UploadFinished(rows);
                stopForegroundService();
                stopForeground(true);
                stopSelf();
            }
        };
        syncHelper.nextSync();
    }

    protected abstract String setAttachmentsSelection(String selection);

    protected abstract String[] setAttachmentsArgs(String[] args);

    protected abstract void UploadFinished(List<DataRow> rows);

    protected abstract Intent setNotificationIntent(Intent notificationIntent, Bundle extras);


    private void stopForegroundService() {
        Log.d(TAG, "Stop foreground service.");

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }

    private abstract class SyncHelper implements OnCompleteListener {
        private List<DataRow> rows;
        private int index;

        public SyncHelper(List<DataRow> rows){
            this.rows = rows;
        }

        public void nextSync(){
            if(index < rows.size()){
                sync(rows.get(index), index);
            }else{
                onSyncFinished();
            }
        }

        @Override
        public void onComplete(@NonNull Task task) {
            if(task.isSuccessful()){
                DataRow syncedRow = rows.get(index);
                Values values = new Values();
                String serverId = syncedRow.getString(Col.SERVER_ID);
                values.put("is_uploaded_to_server", 1);
                rows.get(index).put("is_uploaded_to_server", 1);
                attachmentModel.update(serverId, values);
            }
            index++;
            nextSync();
        }

        public abstract void sync(DataRow row, int index);
        public abstract void onSyncFinished();
    }
    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
