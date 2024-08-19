package com.MohafizDZ.project.models;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.Model;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;

public class RegionModel extends Model {
    //todo add state, country also make sure to sync based on state
    private static final String TAG = RegionModel.class.getSimpleName();

    public Col name = new Col(Col.ColumnType.varchar);
    public Col creator_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class);
    public Col state_id = new Col(Col.ColumnType.varchar).setDefaultValue(1101);
    public Col country_id = new Col(Col.ColumnType.varchar).setDefaultValue(4);
    public Col state = new Col(Col.ColumnType.varchar).setDefaultValue("Oran");
    public Col country = new Col(Col.ColumnType.varchar).setDefaultValue("Algeria");
    public Col latitude = new Col(Col.ColumnType.real);
    public Col longitude = new Col(Col.ColumnType.real);
    public Col geo_hash = new Col(Col.ColumnType.varchar);
    public Col radius = new Col(Col.ColumnType.real);

    public RegionModel(Context mContext) {
        super(mContext, "region");
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
