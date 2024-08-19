/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p>
 * Created on 9/1/15 6:51 PM
 */
package com.MohafizDZ.framework_repository.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.MohafizDZ.own_distributor.R;

import java.util.ArrayList;
import java.util.List;


public class CustomNotificationBuilder {
    public static final String TAG = CustomNotificationBuilder.class.getSimpleName();
    public static final String KEY_NOTIFICATION_RING_TONE = "notification_ringtone";
    private Context mContext;
    private Notification.Builder mNotificationBuilder = null;
    private PendingIntent mNotificationResultIntent = null;
    private NotificationManager mNotificationManager = null;
    private String title, text, bigText;
    private boolean mOnGoing = false, mAutoCancel = true;
    private Intent resultIntent = null;
    private int icon = R.mipmap.ic_launcher_foreground;
    private int small_icon = R.mipmap.ic_launcher_foreground;
    private List<NotificationAction> mActions = new ArrayList<>();
    private int notification_id = 0;
    private Boolean withVibrate = true;
    private Boolean withLargeIcon = true;
    private Boolean withRingTone = true;
    private int notification_color = R.color.android_white;
    private int maxProgress = -1;
    private int currentProgress = -1;
    private boolean indeterminate = false;
    private Bitmap bigPictureStyle = null;

    public CustomNotificationBuilder(Context context, int notification_id) {
        mContext = context;
        this.notification_id = notification_id;
    }

    public CustomNotificationBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public CustomNotificationBuilder setText(String text) {
        this.text = text;
        return this;
    }

    public CustomNotificationBuilder setIcon(int res_id) {
        icon = res_id;
        return this;
    }

    public CustomNotificationBuilder withLargeIcon(boolean largeIcon) {
        withLargeIcon = largeIcon;
        return this;
    }

    public boolean withLargeIcon() {
        return withLargeIcon;
    }

    public CustomNotificationBuilder withRingTone(Boolean ringTone) {
        withRingTone = ringTone;
        return this;
    }

    public boolean withRingTone() {
        return withRingTone;
    }

    public CustomNotificationBuilder setBigPicture(Bitmap bitmap) {
        bigPictureStyle = bitmap;
        return this;
    }

    public CustomNotificationBuilder setBigText(String bigText) {
        this.bigText = bigText;
        return this;
    }

    public CustomNotificationBuilder setOngoing(boolean onGoing) {
        mOnGoing = onGoing;
        return this;
    }

    public CustomNotificationBuilder setAutoCancel(boolean autoCancel) {
        mAutoCancel = autoCancel;
        return this;
    }

    public CustomNotificationBuilder addAction(NotificationAction action) {
        mActions.add(action);
        return this;
    }

    public CustomNotificationBuilder allowVibrate(Boolean vibrate) {
        withVibrate = vibrate;
        return this;
    }

    public CustomNotificationBuilder setColor(int res_id) {
        notification_color = res_id;
        return this;
    }

    public CustomNotificationBuilder setProgress(int max, int progress, boolean indeterminate) {
        maxProgress = max;
        currentProgress = progress;
        this.indeterminate = indeterminate;
        return this;
    }

    private void init() {
        mNotificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationBuilder = new Notification.Builder(mContext);
        mNotificationBuilder.setContentTitle(title);
        mNotificationBuilder.setContentText(text);
        if (bigText == null)
            mNotificationBuilder.setContentInfo(text);
        if (withLargeIcon()) {
            mNotificationBuilder.setSmallIcon(small_icon);
            Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(), this.icon);
            Bitmap newIcon = Bitmap.createBitmap(icon.getWidth(), icon.getHeight(), icon.getConfig());
            Canvas canvas = new Canvas(newIcon);
            canvas.drawColor(mContext.getResources().getColor( R.color.android_white));
            canvas.drawBitmap(icon, 0, 0, null);
            mNotificationBuilder.setLargeIcon(newIcon);
        } else {
            mNotificationBuilder.setSmallIcon(icon);
        }
        mNotificationBuilder.setAutoCancel(mAutoCancel);
        mNotificationBuilder.setOngoing(mOnGoing);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mNotificationBuilder.setColor(mContext.getResources().getColor( notification_color));
        }
        if (bigText != null) {
            Notification.Style notiStyle = new Notification.BigPictureStyle();
            mNotificationBuilder.setStyle(notiStyle);
        }
        if (bigPictureStyle != null) {
            mNotificationBuilder.setStyle(new Notification.BigPictureStyle());
        }

        if (maxProgress != -1) {
            mNotificationBuilder.setProgress(maxProgress, currentProgress, indeterminate);
        }
    }

    private void setSoundForNotification() {
        Uri uri = getNotificationRingTone(mContext);
        mNotificationBuilder.setSound(uri);
    }

    public static Uri getNotificationRingTone(Context context) {
        MySharedPreferences mPref = new MySharedPreferences(context);
        String defaultUri = context.getResources().getString( R.string.notification_default_ring_tone);
        return Uri.parse(mPref.getString(KEY_NOTIFICATION_RING_TONE, defaultUri));
    }

    private void setVibrateForNotification() {
        mNotificationBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000,
                1000});
    }

    public CustomNotificationBuilder setResultIntent(Intent intent) {
        resultIntent = intent;
        return this;
    }

    public CustomNotificationBuilder build() {
        init();
        if (withVibrate) {
            setVibrateForNotification();
        }
        if (withRingTone())
            setSoundForNotification();
        if (resultIntent != null) {
            _setResultIntent();
        }
        if (mActions.size() > 0) {
            _addActions();
        }
        return this;
    }

    private void _addActions() {
        for (NotificationAction action : mActions) {
            Intent intent = new Intent(mContext, action.getIntent());
            intent.setAction(action.getAction());
            intent.putExtras(action.getExtras());
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationBuilder.addAction(action.getIcon(),
                    action.getTitle(), pendingIntent);
        }
    }

    private void _setResultIntent() {
        mNotificationResultIntent = PendingIntent.getActivity(mContext, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        mNotificationBuilder.setContentIntent(mNotificationResultIntent);
    }

    public void show() {
// === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            mNotificationBuilder.setChannelId(channelId);
        }
        if (mNotificationBuilder == null) {
            build();
        }
        mNotificationManager.notify(notification_id, mNotificationBuilder.build());
    }

    public static void cancelNotification(Context context) {
        NotificationManager nMgr = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE
        );
        nMgr.cancelAll();
        ;
    }

    public static void cancelNotification(Context context, int id) {
        NotificationManager nMgr = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE
        );
        nMgr.cancel(id);
    }


    public static class NotificationAction {
        private int icon;
        private int requestCode;
        private String title;
        private String action;
        private Bundle extras;
        private Class<?> intent;

        public NotificationAction(int icon, String title, int requestCode,
                                  String action, Class<?> intent, Bundle extras) {
            super();
            this.icon = icon;
            this.title = title;
            this.requestCode = requestCode;
            this.action = action;
            this.intent = intent;
            this.extras = extras;
        }

        public Class<?> getIntent() {
            return intent;
        }

        public void setIntent(Class<?> intent) {
            this.intent = intent;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public int getRequestCode() {
            return requestCode;
        }

        public void setRequestCode(int requestCode) {
            this.requestCode = requestCode;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public Bundle getExtras() {
            return extras;
        }

        public void setExtras(Bundle extras) {
            this.extras = extras;
        }

    }
}
