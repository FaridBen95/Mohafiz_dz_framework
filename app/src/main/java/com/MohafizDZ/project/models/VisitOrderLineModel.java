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
import com.MohafizDZ.project.providers.PreClosingSalesProvider;

import java.util.List;

public class VisitOrderLineModel extends Model{
    private static final String TAG = VisitOrderLineModel.class.getSimpleName();
    public static String PRE_CLOSING_SALES_AUTHORITY = BuildConfig.APPLICATION_ID + ".pre_closing_sales_authority";

    public Col order_id = new Col(Col.ColumnType.many2one).setRelationalModel(VisitOrderModel.class);
    public Col qty = new Col(Col.ColumnType.real).setDefaultValue(0);
    public Col price = new Col(Col.ColumnType.real);
    public Col total_price = new Col(Col.ColumnType.real);
    public Col product_id = new Col(Col.ColumnType.many2one).setRelationalModel(CompanyProductModel.class);
    public Col product_name = new Col(Col.ColumnType.varchar);
    public VisitOrderLineModel(Context mContext) {
        super(mContext, "visit_order_line");
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    public int createOrderLine(DataRow orderRow, String productId, String productName, float qty, float unitPrice, float totalPrice) {
        Values values = new Values();
        values.put("order_id", orderRow.getString(Col.SERVER_ID));
        values.put("qty", qty);
        values.put("price", unitPrice);
        values.put("total_price", totalPrice);
        values.put("product_id", productId);
        values.put("product_name", productName);
        int rowId = insert(values);
        return rowId;
    }

    public int deleteOrderLines(String orderId, boolean permanently) {
        String selection = " order_id = ? ";
        String[] args = {orderId};
        return delete(selection, args, permanently);
    }

    private Uri getSalesUri(String tourId){
        return PreClosingSalesProvider.generateUri(buildUri(PRE_CLOSING_SALES_AUTHORITY), tourId);
    }

    public List<DataRow> getPreClosingSales(String tourId){
        Uri uri = getSalesUri(tourId);
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        return CursorUtils.cursorToList(cursor);
    }
}
