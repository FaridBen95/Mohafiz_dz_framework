package com.MohafizDZ.project.models;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.MohafizDZ.framework_repository.Utils.CursorUtils;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.BuildConfig;
import com.MohafizDZ.project.providers.CustomersProvider;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class CompanyCustomerModel extends Model {
    private static final String TAG = CompanyCustomerModel.class.getSimpleName();
    public static final String CUSTOMERS_LIST_AUTHORITY = BuildConfig.APPLICATION_ID + ".customers_list_authority";
    public Col name = new Col(Col.ColumnType.varchar);
    public Col customer_code = new Col(Col.ColumnType.varchar);
    public Col creator_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class);
    public Col region_id = new Col(Col.ColumnType.many2one).setRelationalModel(RegionModel.class);
    public Col state_id = new Col(Col.ColumnType.varchar);
    public Col country_id = new Col(Col.ColumnType.varchar);
    //todo add states from cfeed project
    public Col state = new Col(Col.ColumnType.varchar).setDefaultValue("Oran");
    public Col country = new Col(Col.ColumnType.varchar).setDefaultValue("Algeria");
    public Col picture_low = new Col(Col.ColumnType.low_quality_image);
    public Col picture_path = new Col(Col.ColumnType.varchar);
    //this is the create date used in the current company (in future version companies can import customers so it will be import date)
    public Col company_create_date = new Col(Col.ColumnType.varchar);
    public Col latitude = new Col(Col.ColumnType.real);
    public Col longitude = new Col(Col.ColumnType.real);
    public Col geo_hash = new Col(Col.ColumnType.varchar);
    public Col phone_num = new Col(Col.ColumnType.varchar);
    public Col address = new Col(Col.ColumnType.varchar);
    public Col note = new Col(Col.ColumnType.text);
    public Col category_id = new Col(Col.ColumnType.many2one).setRelationalModel(CustomerCategoryModel.class);
    public Col balance = new Col(Col.ColumnType.real).setDefaultValue(0);
    public Col balance_limit = new Col(Col.ColumnType.real).setDefaultValue(0);


    public CompanyCustomerModel(Context mContext) {
        super(mContext, "company_customer");
    }

    public String createCustomer(Values values) {
        int id = insert(values);
        if(id > 0) {
            return browse(id).getString(Col.SERVER_ID);
        }else{
            return null;
        }
    }

    public String updateCustomer(String id, Values values) {
        if(update(id, values) > 0){
            return id;
        }
        return null;
    }

    public void updateBalance(String customerId, float remainingAmount) {
        Values values = new Values();
        values.put("balance", remainingAmount);
        update(customerId, values);
    }

    public List<DataRow> getCustomers(String tourId, @Nonnull String latitude,
                                      @Nonnull String longitude, String selection, String[] args, String sortBy){
        List<DataRow> allRows = new ArrayList<>();
        Cursor cr = null;
        try{
            cr = mContext.getContentResolver().
                    query(getCustomersListUri(tourId, latitude, longitude), null, selection, args, sortBy);
        }catch (IllegalStateException e){
            e.printStackTrace();
            return getCustomers(tourId, latitude, longitude, selection, args, sortBy);
        }
        try{
            if (cr != null && cr.moveToFirst()) {
                do {
                    DataRow row = CursorUtils.toDatarow(cr);
                    allRows.add(row);
                } while (cr.moveToNext());
            }
        }finally {
            if(cr != null){
                cr.close();
            }
        }
        return allRows;
    }

    private Uri getCustomersListUri(String tourId, String latitude, String longitude){
        return CustomersProvider.getCustomersListUri(buildUri(CUSTOMERS_LIST_AUTHORITY), tourId, latitude, longitude);
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
