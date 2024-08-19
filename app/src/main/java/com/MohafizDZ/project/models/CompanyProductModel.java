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
import com.MohafizDZ.project.providers.ProductDetailsProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyProductModel extends Model {
    private static final String TAG = CompanyProductModel.class.getSimpleName();
    public static final String PRODUCT_DETAILS_AUTHORITY = BuildConfig.APPLICATION_ID +  ".product_details_provider";

    public Col name = new Col(Col.ColumnType.varchar);
    public Col code = new Col(Col.ColumnType.varchar);
    public Col description = new Col(Col.ColumnType.varchar);
    public Col category_id = new Col(Col.ColumnType.many2one).setRelationalModel(CompanyProductCategoryModel.class);
    public Col price = new Col(Col.ColumnType.real).setDefaultValue(0);
    public Col picture_low = new Col(Col.ColumnType.low_quality_image).setDefaultValue("");
    public Col creator_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class);
    public Col company_create_date = new Col(Col.ColumnType.varchar);
    public CompanyProductModel(Context mContext) {
        super(mContext, "company_product");
    }

    public String createProduct(Values values) {
        int id = insert(values);
        if(id > 0) {
            return browse(id).getString(Col.SERVER_ID);
        }else{
            return null;
        }
    }

    public String updateProduct(String id, Values values) {
        if(update(id, values) > 0){
            return id;
        }
        return null;
    }

    public List<DataRow> getProducts(String tourId, String selection, String[] selectionArgs, String sort) {
        List<DataRow> allRows = new ArrayList<>();
        Cursor cr = null;
        try{
            cr = mContext.getContentResolver().
                    query(getProductDetailsUri(tourId), null, selection, selectionArgs, sort);
        }catch (IllegalStateException e){
            e.printStackTrace();
            return getProducts(tourId, selection, selectionArgs, sort);
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

    public Map<String, DataRow> getProductsMap(String tourId, String selection, String[] selectionArgs, String sort) {
        Map<String, DataRow> allRows = new HashMap<>();
        Cursor cr = null;
        try{
            cr = mContext.getContentResolver().
                    query(getProductDetailsUri(tourId), null, selection, selectionArgs, sort);
        }catch (IllegalStateException e){
            e.printStackTrace();
            return getProductsMap(tourId, selection, selectionArgs, sort);
        }
        try{
            if (cr != null && cr.moveToFirst()) {
                do {
                    DataRow row = CursorUtils.toDatarow(cr);
                    allRows.put(row.getString(Col.SERVER_ID), row);
                } while (cr.moveToNext());
            }
        }finally {
            if(cr != null){
                cr.close();
            }
        }
        return allRows;
    }

    public Uri getProductDetailsUri(String tourId){
        return ProductDetailsProvider.getUri(buildUri(PRODUCT_DETAILS_AUTHORITY), tourId);
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
