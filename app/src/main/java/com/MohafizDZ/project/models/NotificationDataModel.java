package com.MohafizDZ.project.models;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.Model;

public class NotificationDataModel extends Model {
    public static final String TAG = NotificationDataModel.class.getSimpleName();

    public Col type = new Col(Col.ColumnType.varchar);
    public Col from_user = new Col(Col.ColumnType.varchar);
    public Col post_id = new Col(Col.ColumnType.varchar);
    public Col sale_id = new Col(Col.ColumnType.varchar);

    public NotificationDataModel(Context mContext) {
        super(mContext, "fcm_notification");
    }
}
