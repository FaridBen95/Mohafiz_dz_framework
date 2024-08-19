package com.MohafizDZ.project.models;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.Model;

public class VisitNoActionCategoryModel extends Model {
    private static final String TAG = VisitNoActionCategoryModel.class.getSimpleName();

    public Col name = new Col(Col.ColumnType.varchar);
    public Col creator_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class);
    public VisitNoActionCategoryModel(Context mContext) {
        super(mContext, "visit_no_action_category");
    }

    @Override
    public boolean canSyncDownRelations() {
        return false;
    }

    @Override
    public boolean canSyncRelations() {
        return false;
    }

    @Override
    public boolean canSyncUpRelations() {
        return false;
    }

    @Override
    public boolean allowDeleteRecordsOnServer() {
        return false;
    }

    @Override
    public boolean allowSyncDown() {
        return false;
    }

    @Override
    public boolean allowSyncUp() {
        return true;
    }

    @Override
    public boolean allowRemoveRecordsOutOfDomain() {
        return false;
    }

    @Override
    public boolean allowDeleteInLocal() {
        return false;
    }
}
