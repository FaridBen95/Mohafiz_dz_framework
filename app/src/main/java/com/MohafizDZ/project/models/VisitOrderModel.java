package com.MohafizDZ.project.models;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.MohafizDZ.framework_repository.Utils.CursorUtils;
import com.MohafizDZ.framework_repository.Utils.RandomString;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.BuildConfig;
import com.MohafizDZ.project.catalog_dir.models.CartItemSingleton;

import java.util.ArrayList;
import java.util.List;

public class VisitOrderModel extends Model {
    private static final String TAG = VisitOrderModel.class.getSimpleName();
    public static final String ORDER_DETAILS_AUTHORITY = BuildConfig.APPLICATION_ID + ".order_details_authority";
    public static final String ORDER_STATE_DONE = "done";
    public static final String ORDER_STATE_CANCEL = "cancel";
    //in minutes
    public static final Integer DELETE_ORDER_DELAY = 5;

    public Col name = new Col(Col.ColumnType.varchar);
    public Col customer_id = new Col(Col.ColumnType.many2one).setRelationalModel(CompanyCustomerModel.class);
    public Col visit_id = new Col(Col.ColumnType.many2one).setRelationalModel(TourVisitModel.class);
    public Col tour_id = new Col(Col.ColumnType.many2one).setRelationalModel(TourModel.class);
    public Col state = new Col(Col.ColumnType.varchar).setDefaultValue(ORDER_STATE_DONE);
    public Col distributor_id = new Col(Col.ColumnType.many2one).setRelationalModel(DistributorModel.class);
    public Col seller_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class);
    public Col action_details_id = new Col(Col.ColumnType.many2one).setRelationalModel(TourVisitActionModel.class);
    public Col done_date = new Col(Col.ColumnType.varchar);
    public Col latitude = new Col(Col.ColumnType.real);
    public Col longitude = new Col(Col.ColumnType.real);
    public Col total_amount = new Col(Col.ColumnType.real);
    public Col payment_amount = new Col(Col.ColumnType.real);
    public Col remaining_amount = new Col(Col.ColumnType.real).setDefaultValue(0);
    public Col products = new Col(Col.ColumnType.array);
    public Col lines = new Col(Col.ColumnType.one2many).setRelationalModel(VisitOrderLineModel.class).setRelatedColumn("order_id");
    public Col payment_line = new Col(Col.ColumnType.many2one).setRelationalModel(PaymentModel.class);
    public VisitOrderModel(Context mContext) {
        super(mContext, "visit_order");
    }

    @Override
    public boolean isOnline() {
        return false;
    }


    public DataRow createOrder(String prefix, CartItemSingleton cartItemSingleton, double latitude, double longitude, DataRow visitRow, DataRow distributorRow, String date, float orderAmount, float paymentAmount, float remainingAmount) {
        Values values = new Values();
        values.put("name", generateName(prefix));
        values.put("customer_id", visitRow.getString("customer_id"));
        values.put("visit_id", visitRow.getString(Col.SERVER_ID));
        values.put("tour_id", visitRow.getString("tour_id"));
        values.put("state", ORDER_STATE_DONE);
        values.put("distributor_id", distributorRow.getString(Col.SERVER_ID));
        values.put("seller_id", distributorRow.getString("user_id"));
        values.put("done_date", date);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("total_amount", orderAmount);
        values.put("payment_amount", paymentAmount);
        values.put("remaining_amount", remainingAmount);
        int rowId = insert(values);
        return browse(rowId);
    }

    private String generateName(String prefix){
        String reference = prefix+
                "/"  + new RandomString(6 ,true).nextString() +
                "/"  + String.format("%05d", this.getCreateId() + 1);
        return reference;
    }

    public List<DataRow> getVisitDetails( String selection, String[] selectionArgs, String sort) {
        List<DataRow> allRows = new ArrayList<>();
        Cursor cr = null;
        try{
            cr = mContext.getContentResolver().
                    query(visitOrderDetailsUri(), null, selection, selectionArgs, sort);
        }catch (IllegalStateException e){
            e.printStackTrace();
            return getVisitDetails(selection, selectionArgs, sort);
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

    private Uri visitOrderDetailsUri(){
        return buildUri(ORDER_DETAILS_AUTHORITY);
    }
}
