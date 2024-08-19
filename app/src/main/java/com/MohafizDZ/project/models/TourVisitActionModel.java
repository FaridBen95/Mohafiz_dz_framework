package com.MohafizDZ.project.models;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.MohafizDZ.framework_repository.Utils.CursorUtils;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.BuildConfig;
import com.MohafizDZ.own_distributor.R;

import java.util.ArrayList;
import java.util.List;

public class TourVisitActionModel extends Model {
    //todo based on the firebase consumption i think this model is going to consume so much
    private static final String TAG = TourVisitActionModel.class.getSimpleName();
    public static final String ACTIONS_DETAILS_AUTHORITY = BuildConfig.APPLICATION_ID + ".actions_details_authority";
    public static final String ACTION_VISIT_START = "visit_start";
    public static final String ACTION_VISIT_STOP = "visit_stop";
    public static final String ACTION_VISIT_RESTART = "visit_restart";
    public static final String ACTION_SALE = "action_sale";
    public static final String ACTION_DELETE_ORDER = "action_delete_order";
    public static final String ACTION_CANCEL_ORDER = "action_cancel_order";
    public static final String ACTION_BACK_ORDER = "action_back_order";
    public static final String ACTION_NO_ACTION = "no_action";
    public static final String ACTION_OTHER = "action_other";
    public static final String ACTION_PAYMENT = "action_payment";
    public static final String ACTION_REFUND = "action_refund";

    public Col visit_id = new Col(Col.ColumnType.many2one).setRelationalModel(TourVisitModel.class);
    public Col tour_id = new Col(Col.ColumnType.many2one).setRelationalModel(TourModel.class);
    public Col customer_id = new Col(Col.ColumnType.many2one).setRelationalModel(CompanyCustomerModel.class);
    public Col action = new Col(Col.ColumnType.varchar);
    public Col action_date = new Col(Col.ColumnType.varchar);
    public Col latitude = new Col(Col.ColumnType.real);
    public Col longitude = new Col(Col.ColumnType.real);
    public Col geo_hash = new Col(Col.ColumnType.varchar);
    public Col distance_from_customer = new Col(Col.ColumnType.real);
    public Col res_id = new Col(Col.ColumnType.varchar).setDefaultValue("false");
    public Col rel_model_name = new Col(Col.ColumnType.varchar).setDefaultValue("false");
    public Col note = new Col(Col.ColumnType.text).setDefaultValue("");
    public Col no_action_category_id = new Col(Col.ColumnType.many2one).setRelationalModel(VisitNoActionCategoryModel.class);
    public TourVisitActionModel(Context mContext) {
        super(mContext, "tour_visit_action");
    }

    public boolean hasVisitAction(String visitId) {
        String selection = " visit_id = ? and action not in (?, ?, ?) ";
        String[] args = {visitId, ACTION_VISIT_RESTART, ACTION_VISIT_START, ACTION_VISIT_STOP};
        return getRows(selection, args).size() != 0;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    public void startVisit(DataRow visitRow, double latitude, double longitude) {
        Values values = prepareDefaultValues(MyUtil.getCurrentDate(), latitude, longitude, visitRow);
        values.put("action",ACTION_VISIT_START);
        insert(values);
    }

    public void restartVisit(DataRow visitRow, double latitude, double longitude) {
        Values values = prepareDefaultValues(MyUtil.getCurrentDate(), latitude, longitude, visitRow);
        values.put("action",ACTION_VISIT_RESTART);
        insert(values);
    }

    public void stopVisit(DataRow visitRow, double latitude, double longitude) {
        Values values = prepareDefaultValues(MyUtil.getCurrentDate(), latitude, longitude, visitRow);
        values.put("action",ACTION_VISIT_STOP);
        insert(values);
    }

    private Values prepareDefaultValues(String date, double latitude, double longitude, DataRow visitRow){
        Values values = new Values();
        values.put("visit_id",visitRow.getString(Col.SERVER_ID));
        values.put("tour_id",visitRow.getString("tour_id"));
        String customerId = visitRow.getString("customer_id");
        values.put("customer_id",customerId);
        DataRow customerRow = new CompanyCustomerModel(mContext).browse(customerId);
        values.put("action_date", date);
        values.put("latitude",latitude);
        values.put("longitude",longitude);
        values.put("geo_hash",MyUtil.getGeoHash(latitude, longitude));
        double customerLatitude = Double.valueOf(customerRow.getString("latitude"));
        double customerLongitude = Double.valueOf(customerRow.getString("longitude"));
        values.put("distance_from_customer",MyUtil.distance(latitude, longitude, customerLatitude, customerLongitude));
        return values;

    }

    public DataRow createOrderAction(boolean isBackOrder, DataRow visitRow, double latitude, double longitude, String date) {
        Values values = prepareDefaultValues(date, latitude, longitude, visitRow);
        values.put("action", isBackOrder? ACTION_BACK_ORDER : ACTION_SALE);
        int rowId = insert(values);
        return browse(rowId);
    }

    public DataRow createPaymentAction(boolean isRefund, DataRow visitRow, double latitude, double longitude, String date) {
        Values values = prepareDefaultValues(date, latitude, longitude, visitRow);
        values.put("action", isRefund? ACTION_REFUND : ACTION_PAYMENT);
        int rowId = insert(values);
        return browse(rowId);
    }

    public DataRow createOrderDeletionAction(boolean deletable, DataRow visitRow, double latitude, double longitude, String date) {
        Values values = prepareDefaultValues(date, latitude, longitude, visitRow);
        values.put("action", deletable? ACTION_DELETE_ORDER : ACTION_CANCEL_ORDER);
        int rowId = insert(values);
        return browse(rowId);
    }

    public boolean hasAction(String visitId, String action) {
        String selection = " visit_id = ? and action = ? ";
        String[] args = {visitId, action};
        return browse(selection, args) != null;
    }

    public String getAction(String action) {
        switch (action){
            case ACTION_VISIT_START:
                return getString(R.string.visit_start_name);
            case ACTION_VISIT_RESTART:
                return getString(R.string.visit_restart_name);
            case ACTION_VISIT_STOP:
                return getString(R.string.visit_stop_name);
            case ACTION_SALE:
                return getString(R.string.sale_label);
            case ACTION_DELETE_ORDER:
                return getString(R.string.delete_order_name);
            case ACTION_CANCEL_ORDER:
                return getString(R.string.cancel_order_name);
            case ACTION_BACK_ORDER:
                return getString(R.string.back_order_label);
            case ACTION_NO_ACTION:
                return getString(R.string.no_action_label);
            case ACTION_OTHER:
                return getString(R.string.action_other_label);
            case ACTION_PAYMENT:
                return getString(R.string.payment_label);
            case ACTION_REFUND:
                return getString(R.string.refund_label);
        }
        return "-";
    }

    private String getString(int resId){
        return mContext.getString(resId);
    }

    public List<DataRow> getActionsList(String selection, String[] selectionArgs, String sort) {
        List<DataRow> allRows = new ArrayList<>();
        Cursor cr = null;
        try{
            cr = mContext.getContentResolver().
                    query(actionsDetailsUri(), null, selection, selectionArgs, sort);
        }catch (IllegalStateException e){
            e.printStackTrace();
            return getActionsList(selection, selectionArgs, sort);
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

    private Uri actionsDetailsUri(){
        return buildUri(ACTIONS_DETAILS_AUTHORITY);
    }
}
