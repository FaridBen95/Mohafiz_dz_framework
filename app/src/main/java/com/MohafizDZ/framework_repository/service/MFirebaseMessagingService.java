package com.MohafizDZ.framework_repository.service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.MohafizDZ.App;
import com.MohafizDZ.framework_repository.Utils.CustomNotificationBuilder;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.util.Map;

public class MFirebaseMessagingService extends FirebaseMessagingService implements SyncUtilsInTheAppRun.SyncUtilsListener{
    public static final String TAG = MFirebaseMessagingService.class.getSimpleName();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public Context context;
    public App app;
    private boolean allowStopService;

    @Override
    public boolean stopService(Intent name) {
        if(allowStopService) {
            return super.stopService(name);
        }
        return false;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Map<String, String> data = remoteMessage.getData();

        // Check if message contains a data payload.
        String toOpenFragment = null;
        if (data.size() > 0) {
            Log.d(TAG, "Message data payload: " + data);
        }

        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() == null) {
        if (remoteMessage.getData().size() != 0) {
            //you can here build your notification
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void showNotification(String message, String imageUri) {
        CustomNotificationBuilder notificationBuilder = new CustomNotificationBuilder(context, 0);
//        notificationBuilder.setTitle()
    }

//    private void buildNotification(NotificationManagerCompat managerCompat, Map<String, String> data) {
//        NotificationModel notificationModel = new NotificationModel(context);
//        String type = data.get("type");
//        String additionalData = data.containsKey("data")? data.get("data") : null;
//        NotificationModel.NotificationType notificationType = notificationModel.getType(type);
//        String fromUserId = data.containsKey("from_user")? data.get("from_user") : "false";
//        String fromUserName = data.containsKey("from_user_name")? data.get("from_user_name") : "false";
//        String postId = data.containsKey("post_id")? data.get("post_id") : "false";
//        String postTitle = data.containsKey("post_title")? data.get("post_title") : "false";
//        String saleId = data.containsKey("sale_id")? data.get("sale_id") : "false";
//        String saleTitle = data.containsKey("sale_title")? data.get("sale_title") : "false";
//        Bitmap profile = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile_no_picture);
//        DataRow shareableUserRow = new ShareableUserModel(context).browse(fromUserId);
//        String profilePicBlob = shareableUserRow != null? shareableUserRow.getString("user_profile_pic") : " false";
//        if(!profilePicBlob.equals("false")) {
//            profile = BitmapUtils.getBitmapImage(context, profilePicBlob);
//        }
//        Bitmap bitmap = null;
//        if(notificationType == NotificationModel.NotificationType.react || notificationType == NotificationModel.NotificationType.comment
//                || notificationType == NotificationModel.NotificationType.review){
//            try {
//                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
//                StorageReference storageReference = firebaseStorage.getReference(MConstants.FIREBASE_STORAGE_LINK);
//                JSONObject jsonData = new JSONObject(additionalData);
//                String imagePath = jsonData.getString("image_path");
//                String myUserId = app.getCurrentUser().getString(Col.SERVER_ID);
//                if(myUserId != null) {
//                    storageReference = storageReference.child(myUserId);
//                    FutureTarget<Bitmap> futureBitmap = GlideApp.with(context).asBitmap().
//                            load(storageReference.child(imagePath))
//                            .submit();
//                    bitmap = futureBitmap.get();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        String message = fromUserName + " ";
//        switch (notificationType){
//            case follow:
//                message = message.concat(context.getResources().getString(R.string.following_you));
//                break;
//            case comment:
//                message = message.concat(context.getResources().getString(R.string.comment_on_your_post));
//                if(postTitle != null) {
//                    message = message.concat(" ").concat("\"").concat(postTitle).concat("\"");
//                }
//                break;
//            case review:
//                message = message.concat(context.getResources().getString(R.string.added_review_on_your_post));
//                if(saleTitle != null) {
//                    message = message.concat(" ").concat("\"").concat(saleTitle).concat("\"");
//                }
//                break;
//            case react:
//                message = message.concat(context.getResources().getString(R.string.reacted_on_your_post));
//                if(postTitle != null) {
//                    message = message.concat(" ").concat("\"").concat(postTitle).concat("\"");
//                }
//                break;
//        }
//
//        Intent main_intent = new Intent(context.getApplicationContext(), NotificationsActivity.class);
//        main_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(),
//                0,
//                main_intent, 0);
//
//        NotificationCompat.BigPictureStyle style = null;
//        if(bitmap != null){
//            style = new NotificationCompat.BigPictureStyle();
//            style.bigPicture(bitmap);
//            style.bigLargeIcon(null);
//            style.setSummaryText(message);
//        }
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context.getApplicationContext(), CATEGORY_1)
//                .setSmallIcon(R.mipmap.ic_launcher_foreground)
//                .setContentTitle(context.getResources().getString(R.string.app_name))
//                .setContentText(message)
//                .setLargeIcon(profile);
//        if(style != null) {
//            notificationBuilder = notificationBuilder.setStyle(style);
//        }
//        Notification notification = notificationBuilder.setCategory(NotificationCompat.CATEGORY_MESSAGE)
//                .setColor(ContextCompat.getColor(context.getApplicationContext(),R.color.main_theme_primary))
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true)
//                .setOnlyAlertOnce(true)
//                .build();
//
//        String selection = " type =? and from_user =? and (post_id =? or sale_id = ?) ";
//        String[] args = {type, fromUserId, postId, saleId};
//        NotificationDataModel notificationDataModel = new NotificationDataModel(context);
//        DataRow row = notificationDataModel.browse(selection, args);
//        int id = -1;
//        if(row == null){
//            if(postId != null && !postId.equals("false")) {
//                Values values = new Values();
//                values.put("type", type);
//                values.put("from_user", fromUserId);
//                values.put("post_id", postId);
//                id = notificationDataModel.insert(values);
//            }
//            if(saleId != null && !saleId.equals("false")) {
//                Values values = new Values();
//                values.put("type", type);
//                values.put("from_user", fromUserId);
//                values.put("sale_id", saleId);
//                id = notificationDataModel.insert(values);
//            }
//        }else{
//            id = row.getInteger(Col.ROWID);
//        }
//        managerCompat.notify(id, notification);
//    }



    @Override
    public void onNewToken(@NonNull String s) {
        Log.d(TAG, "Refreshed token: " + s);
        saveNewToken(s);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
    }


    private void saveNewToken(String token) {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        try {
            app = (App) context;
        }catch (Exception ignored){}
    }

    @Override
    public void onSyncFinished() {
        allowStopService = true;
        stopSelf();
    }

    @Override
    public void onSyncFailed() {
        try {
            Looper.prepare();
        }catch (Exception ignored){}
        new Handler().postDelayed(() -> {
            Bundle _data = new Bundle();
            _data.putString("from", TAG);
            SyncUtils.requestSync(context.getApplicationContext(), NotificationModel.class, NotificationModel.AUTHORITY, _data, this);
        }, 3000);
    }

    public static class SendFCMNotification extends AsyncTask<Void, Void, String>{
        private String body;
        private String title;
        private JSONObject data;
        private String topic;
        private boolean addNotificationBlock;

        public SendFCMNotification(String body, String title, JSONObject data, String topic){
            this.body = body;
            this.title = title;
            this.data = data;
            this.topic = topic;
            this.addNotificationBlock = true;
        }

        public SendFCMNotification(JSONObject data, String topic){
            this.data = data;
            this.topic = topic;
            this.addNotificationBlock = false;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject json = new JSONObject();
                if(addNotificationBlock) {
                    JSONObject notifJson = new JSONObject();
                    notifJson.put("text", body);
                    notifJson.put("title", title);
                    notifJson.put("priority", "high");
                    json.put("notification", notifJson);
                }
                if(data != null) {
                    json.put("data", data);
                }
                json.put("to", "/topics/"+ topic);
                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = new Request.Builder()
                        .header("Authorization", "key=" + MConstants.FCM_SERVER_KEY)
                        .url("https://fcm.googleapis.com/fcm/send")
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                Log.i("send_fcm_notification",e.getMessage());
            }
            return null;
        }

    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
