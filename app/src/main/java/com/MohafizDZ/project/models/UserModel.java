package com.MohafizDZ.project.models;

import android.content.Context;

import com.MohafizDZ.App;
import com.MohafizDZ.own_distributor.BuildConfig;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.service.SyncingDomain;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

public class UserModel extends Model {
    public static final String TAG = UserModel.class.getSimpleName();
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".users_provider";

    public static final int FIELD_CHANGE_INTERVAL_IN_HOURS = 24 * 30;

    public Col firebase_user_id = new Col(Col.ColumnType.varchar);
    public Col name = new Col(Col.ColumnType.varchar);
    public Col profile_picture = new Col(Col.ColumnType.low_quality_image);
    public Col phone_number = new Col(Col.ColumnType.varchar);
    public Col phone_code = new Col(Col.ColumnType.varchar);
    public Col is_phone_authenticated = new Col(Col.ColumnType.bool).setDefaultValue(0);
    public Col email = new Col(Col.ColumnType.varchar);
    public Col state = new Col(Col.ColumnType.varchar);
    public Col state_id = new Col(Col.ColumnType.integer).setDefaultValue(1101);
    public Col country = new Col(Col.ColumnType.varchar).setDefaultValue("Algeria");
    public Col country_id = new Col(Col.ColumnType.varchar).setDefaultValue(4);
    public Col country_code = new Col(Col.ColumnType.varchar);
    public Col subscribed_to_fcm = new Col(Col.ColumnType.bool).setLocalColumn().setDefaultValue(0);
    public Col company_id = new Col(Col.ColumnType.many2one).setRelationalModel(CompanyModel.class);
    public Col vehicle_name = new Col(Col.ColumnType.varchar);
    public Col is_anonymous = new Col(Col.ColumnType.bool).setDefaultValue(0);
    //todo this field should be assigned from the scanned qr code
    public Col can_edit_catalog = new Col(Col.ColumnType.bool).setDefaultValue(1);
    private boolean canOverwriteOnLocal;

    public UserModel(Context mContext) {
        super(mContext, "user");
    }

    @Override
    public SyncingDomain setDefaultDomain(SyncingDomain domain) {

        try {
            FirebaseUser currentUser = ((App)mContext.getApplicationContext()).firebaseAuth.getCurrentUser();
            domain.addOperation("firebase_user_id", SyncingDomain.Operation.equalTo,
                    currentUser.getUid(), false);
            return domain;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean allowRemoveRecordsOutOfDomain() {
        return false;
    }

    @Override
    public boolean canSyncRelations() {
        return true;
    }

    @Override
    public boolean canSyncDownRelations() {
        return true;
    }

    public void setCanOverwriteOnLocal(boolean canOverwriteOnLocal) {
        this.canOverwriteOnLocal = canOverwriteOnLocal;
    }

    @Override
    public boolean forceOverwriteOnLocal() {
        return canOverwriteOnLocal;
    }

    @Override
    public DocumentReference getDocumentReference() {
        return null;
    }

    @Override
    public String getCollectionPath() {
        return null;
    }
}
