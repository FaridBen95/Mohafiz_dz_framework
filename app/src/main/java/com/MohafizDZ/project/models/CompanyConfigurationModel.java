package com.MohafizDZ.project.models;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;

public class CompanyConfigurationModel extends Model {
    private static final String TAG = CompanyConfigurationModel.class.getSimpleName();
    public Col key = new Col(Col.ColumnType.varchar);
    public Col value = new Col(Col.ColumnType.varchar);
    public CompanyConfigurationModel(Context mContext) {
        super(mContext, "company_config");
    }

    @Override
    public boolean allowDeleteRecordsOnServer() {
        return false;
    }

    @Override
    public boolean allowSyncUp() {
        return false;
    }

    public DataRow getValue(String key){
        String selection = " key = ? ";
        String[] args = {key};
        return browse(selection, args);
    }
}
