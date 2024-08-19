package com.MohafizDZ.project.models;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Pair;

import com.MohafizDZ.framework_repository.Utils.CursorUtils;
import com.MohafizDZ.framework_repository.Utils.RandomString;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class PaymentModel extends Model {
    public static final String PAYMENT_DETAILS_AUTHORITY = BuildConfig.APPLICATION_ID + ".payments_details_authority";
    private static final String TAG = PaymentModel.class.getSimpleName();
    public static final String STATE_DONE = "state_done";
    public static final String STATE_EXPENSES_DONE = "state_expenses_done";
    public static final String STATE_EXPENSES_DRAFT = "state_expenses_draft";
    public static final String STATE_CANCEL = "cancel";
    private static final String PREFIX_PAYMENT = "payment";
    private static final String PREFIX_REFUND = "refund";
    private static final String PREFIX_EXPENSE = "expense";

    public Col reference = new Col(Col.ColumnType.varchar);
    public Col name = new Col(Col.ColumnType.varchar);
    public Col amount = new Col(Col.ColumnType.real).setDefaultValue(0);
    public Col remaining_amount = new Col(Col.ColumnType.real).setDefaultValue(0);
    public Col visit_order_id = new Col(Col.ColumnType.many2one).setRelationalModel(VisitOrderModel.class);
    public Col customer_id = new Col(Col.ColumnType.many2one).setDefaultValue("false").setRelationalModel(CompanyCustomerModel.class);
    public Col tour_id = new Col(Col.ColumnType.many2one).setRelationalModel(TourModel.class);
    public Col visit_id = new Col(Col.ColumnType.many2one).setRelationalModel(TourVisitModel.class).setDefaultValue("false");
    public Col distributor_id = new Col(Col.ColumnType.many2one).setRelationalModel(DistributorModel.class);
    public Col action_details_id = new Col(Col.ColumnType.many2one).setRelationalModel(TourVisitActionModel.class);
    public Col payment_date = new Col(Col.ColumnType.varchar);
    public Col latitude = new Col(Col.ColumnType.real);
    public Col longitude = new Col(Col.ColumnType.real);
    public Col state = new Col(Col.ColumnType.varchar).setDefaultValue(STATE_DONE);
    public Col is_expenses = new Col(Col.ColumnType.bool).setDefaultValue(0);
    public Col expense_subject = new Col(Col.ColumnType.varchar).setDefaultValue("");
    public Col expense_note = new Col(Col.ColumnType.text).setDefaultValue("");
    public PaymentModel(Context mContext) {
        super(mContext, "payment");
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    public DataRow createOrderPayment(String orderName, float paymentAmount, float actualBalance, DataRow visitRow, DataRow distributorRow, String date){
        Values values = prepareDefaultValues(paymentAmount, actualBalance, visitRow, distributorRow, date);
        values.put("name", orderName);
        values.put("reference", orderName);
        int rowId = insert(values);
        return browse(rowId);
    }

    public DataRow createPayment(boolean isRefund, float paymentAmount, float actualBalance, DataRow visitRow, DataRow distributorRow, String date){
        Values values = prepareDefaultValues(paymentAmount, actualBalance, visitRow, distributorRow, date);
        String prefix = !isRefund? PREFIX_PAYMENT : PREFIX_REFUND;
        values.put("name", generateName(prefix));
        values.put("reference", generateName(prefix));
        int rowId = insert(values);
        return browse(rowId);
    }

    public DataRow createExpense(String subject, String note, float paymentAmount, float actualBalance, DataRow tourRow, DataRow distributorRow, String date){
        Values values = new Values();
        values.put("expense_subject", subject);
        values.put("expense_note", note);
        values.put("amount", paymentAmount);
        values.put("remaining_amount", actualBalance);
        values.put("customer_id", "false");
        values.put("tour_id", tourRow.getString(Col.SERVER_ID));
        values.put("visit_id", "false");
        values.put("distributor_id", distributorRow.getString(Col.SERVER_ID));
        values.put("payment_date", date);
        values.put("is_expenses", 1);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        String prefix = PREFIX_EXPENSE;
        values.put("name", generateName(prefix));
        values.put("state", STATE_EXPENSES_DRAFT);
        values.put("reference", generateName(prefix));
        int rowId = insert(values);
        return browse(rowId);

    }

    public Values prepareDefaultValues(float paymentAmount, float actualBalance, DataRow visitRow, DataRow distributorRow, String date){
        Values values = new Values();
        values.put("amount", paymentAmount);
        values.put("remaining_amount", actualBalance);
        values.put("customer_id", visitRow.getString("customer_id"));
        values.put("tour_id", visitRow.getString("tour_id"));
        values.put("visit_id", visitRow.getString(Col.SERVER_ID));
        values.put("distributor_id", distributorRow.getString(Col.SERVER_ID));
        values.put("payment_date", date);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        return values;
    }

    private String generateName(String prefix){
        String reference = prefix+
                "/"  + new RandomString(6 ,true).nextString() +
                "/"  + String.format("%05d", this.getCreateId() + 1);
        return reference;
    }

    public List<DataRow> getPaymentsList(String selection, String[] selectionArgs, String sort) {
        List<DataRow> allRows = new ArrayList<>();
        Cursor cr = null;
        try{
            cr = mContext.getContentResolver().
                    query(paymentsDetailsUri(), null, selection, selectionArgs, sort);
        }catch (IllegalStateException e){
            e.printStackTrace();
            return getPaymentsList(selection, selectionArgs, sort);
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

    private Uri paymentsDetailsUri(){
        return buildUri(PAYMENT_DETAILS_AUTHORITY);
    }

    public Pair<Float, Float> getTotalExpenses(String tourId) {
        String selection = " tour_id = ? and (state in (?, ?) or customer_id = 'false' )";
        String[] args = {tourId, PaymentModel.STATE_EXPENSES_DONE, PaymentModel.STATE_EXPENSES_DRAFT};
        float total = 0;
        float validatedTotal = 0;
        for(DataRow row : getRows(selection, args)){
            String state = row.getString("state");
            if(state.equals(PaymentModel.STATE_CANCEL)){
                continue;
            }
            float amount = row.getFloat("amount");
            total += amount;
            if(state.equals(PaymentModel.STATE_EXPENSES_DONE)){
                validatedTotal += amount;
            }
        }
        return new Pair<>(total, validatedTotal);
    }

}
