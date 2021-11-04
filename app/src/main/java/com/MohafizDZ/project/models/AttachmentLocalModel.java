package com.MohafizDZ.project.models;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.Model;

public class AttachmentLocalModel extends Model {
    public static final String TAG = AttachmentLocalModel.class.getSimpleName();

    Col path = new Col(Col.ColumnType.varchar);
    Col col_name = new Col(Col.ColumnType.varchar);
    Col model_name = new Col(Col.ColumnType.varchar);
    Col rel_id = new Col(Col.ColumnType.varchar);
    Col is_uploaded_to_server = new Col(Col.ColumnType.bool).setDefaultValue(0);

    public AttachmentLocalModel(Context mContext) {
        super(mContext, "attachment_local_model");
    }

    @Override
    public boolean isOnline() {
        return false;
    }
}
