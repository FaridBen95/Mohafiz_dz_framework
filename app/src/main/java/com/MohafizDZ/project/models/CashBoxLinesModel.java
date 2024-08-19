package com.MohafizDZ.project.models;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;

public class CashBoxLinesModel extends Model {
    private static final String TAG = CashBoxLinesModel.class.getSimpleName();

    public Col tour_id = new Col(Col.ColumnType.many2one).setRelationalModel(CashBoxModel.class);
    public Col cash_box_id = new Col(Col.ColumnType.many2one).setRelationalModel(CashBoxModel.class);
    public Col denomination = new Col(Col.ColumnType.varchar);
    public Col denomination_value = new Col(Col.ColumnType.integer);
    public Col count = new Col(Col.ColumnType.integer);
    public Col creator_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class);

    public CashBoxLinesModel(Context mContext) {
        super(mContext, "tour_cash_box_lines");
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    public void createLine(boolean updating, String denomination, int count, DataRow cashBoxRow, DataRow currentUserRow){
        Values values = new Values();
        values.put("count", count);
        String selection = " cash_box_id = ? and denomination = ? ";
        String[] args = {cashBoxRow.getString(Col.SERVER_ID), denomination};
        DataRow similarRow = browse(selection, args);
        if(similarRow != null){
            int newCount = updating? count : similarRow.getInteger("count") + count;
            values.put("count", newCount);
            update(similarRow.getString(Col.SERVER_ID), values);
        }else {
            values.put("tour_id", cashBoxRow.getString("tour_id"));
            values.put("cash_box_id", cashBoxRow.getString(Col.SERVER_ID));
            values.put("denomination", denomination);
            values.put("denomination_value", Integer.valueOf(denomination));
            values.put("creator_id", currentUserRow.getString(Col.SERVER_ID));
            insert(values);
        }
    }
}
