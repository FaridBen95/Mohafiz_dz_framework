package com.MohafizDZ.project.models;

import android.content.Context;

import androidx.annotation.NonNull;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.framework_repository.service.firebase.IFirestoreSync;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DistributorModel extends Model {
    private static final String TAG = DistributorModel.class.getSimpleName();

    public Col planner_id = new Col(Col.ColumnType.many2one).setRelationalModel(PlannerModel.class);
    public Col user_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class);
    public Col company_id = new Col(Col.ColumnType.many2one).setRelationalModel(CompanyModel.class);
    public Col code = new Col(Col.ColumnType.varchar);
    public Col configurations = new Col(Col.ColumnType.array).setRelatedColumn(Col.SERVER_ID).setRelationalModel(DistributorConfigurationModel.class);
    public Col default_vehicle_name = new Col(Col.ColumnType.varchar).setDefaultValue("");
    public Col join_date = new Col(Col.ColumnType.varchar).setDefaultValue("");
    public Col expenses_limit = new Col(Col.ColumnType.real).setDefaultValue("0");
    public DistributorModel(Context mContext) {
        super(mContext, "distributor");
    }

    public void syncDistributor(String userID,@NonNull IFirestoreSync.SyncDownListener syncDownListener) {
        Query query = CompanyModel.getMainDocumentReference(mContext).collection(getModelName());
        query.where(Filter.equalTo("user_id", userID)).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(task.getResult().getDocuments().size() != 0) {
                    List<Map<String, Object>> data = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                        data.add(documentSnapshot.getData());
                    }
                    syncDownListener.onResult(data);
                }else if (task.getResult().getMetadata().isFromCache()){
                    syncDownListener.onException(new Exception("Slow Internet connection"));
                }else{
                    syncDownListener.onResult(new ArrayList<>());
                }
            }else{
                syncDownListener.onException(task.getException());
            }
        });
    }

    public void insertOrUpdateDistributor(Map<String, Object> recordLineMap, DataRow currentUserRow) {
        //todo make sure to implement update later
        String userId = currentUserRow.getString(Col.SERVER_ID);
        Values values = new Values();
        values.put("user_id", userId);
        values.put("id", userId);
        values.put("company_id", CompanyModel.getCurrentCompanyId(mContext));
        if(recordLineMap != null){
            values.put("join_date", recordLineMap.get("join_date"));
            values.put("code", recordLineMap.get("code"));
        }else{
            values.put("join_date", MyUtil.getCurrentDate());
            values.put("code", CompanyModel.getCode(mContext));
        }
        DataRow currentDistributor = browse( "user_id = ? ", new String[]{userId});
        if(currentDistributor == null){
            insert(values);
        }else{
            long localUpdateTimeInMillis = MyUtil.dateToMilliSec(currentDistributor.getString("_write_date"));
            long serverUpdateTimeInMillis = recordLineMap.containsKey("write_date")?
                    Long.valueOf("" + recordLineMap.get("write_date")) : 0;
            if(serverUpdateTimeInMillis > localUpdateTimeInMillis) {
                update(currentDistributor.getString(Col.SERVER_ID), values);
            }
        }
    }

    public DataRow getCurrentDistributor(DataRow currentUserRow) {
        return browse("user_id = ? ", new String[]{currentUserRow.getString(Col.SERVER_ID)});
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
