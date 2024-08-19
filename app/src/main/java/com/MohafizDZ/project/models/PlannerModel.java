package com.MohafizDZ.project.models;

import android.content.Context;

import com.MohafizDZ.App;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;

public class PlannerModel extends Model {
    private static final String TAG = PlannerModel.class.getSimpleName();

    public Col name = new Col(Col.ColumnType.varchar);
    public Col company_id = new Col(Col.ColumnType.many2one).setRelationalModel(CompanyModel.class);
    public Col phone_number = new Col(Col.ColumnType.varchar);
    public Col user_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class);
    public PlannerModel(Context mContext) {
        super(mContext, "planner");
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    public DataRow getCurrentPlanner(DataRow currentUserRow) {
        DataRow currentPlanner = browse(" user_id = ? ", new String[] {currentUserRow.getString(Col.SERVER_ID)});
        if(currentPlanner == null){
            String role = CompanyModel.getCompanyUserRole(mContext);
            if(role != null && role.equals(CompanyUserModel.ADMIN_ROLE)){
                Values values = new Values();
                values.put("name", currentUserRow.getString("name"));
                values.put("company_id", CompanyModel.getCurrentCompanyId(mContext));
                values.put("phone_number", currentUserRow.getString("phone_number"));
                values.put("user_id", currentUserRow.getString(Col.SERVER_ID));
                values.put("id", currentUserRow.getString(Col.SERVER_ID));
                int id = insert(values);
                return browse(id);
            }
        }
        return currentPlanner;
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
        DataRow currentUserRow = ((App)mContext.getApplicationContext()).getCurrentUser();
        return currentUserRow != null && CompanyUserModel.isAdmin(mContext, currentUserRow);
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
