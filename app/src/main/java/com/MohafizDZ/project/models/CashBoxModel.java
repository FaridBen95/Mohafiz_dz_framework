package com.MohafizDZ.project.models;

import static io.grpc.inprocess.InProcessServerBuilder.generateName;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;

public class CashBoxModel extends Model {
    private static final String TAG = CashBoxModel.class.getSimpleName();
    public static final String STATE_DRAFT = "draft";
    public static final String STATE_DONE = "done";

    public Col state = new Col(Col.ColumnType.varchar).setDefaultValue(STATE_DRAFT);
    public Col name = new Col(Col.ColumnType.varchar).setDefaultValue("");
    public Col tour_id = new Col(Col.ColumnType.many2one).setRelationalModel(TourModel.class);
    public Col distributor_id = new Col(Col.ColumnType.many2one).setRelationalModel(DistributorModel.class);
    public Col creator_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class);
    public Col user_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class);
    public Col lines = new Col(Col.ColumnType.one2many).setRelationalModel(CashBoxLinesModel.class).setRelatedColumn("cash_box_id");
    public Col validate_date = new Col(Col.ColumnType.varchar);

    public CashBoxModel(Context mContext) {
        super(mContext, "tour_cash_box");
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    public DataRow getCurrentCashBox(DataRow tourRow) {
        String selection = " tour_id = ? ";
        String[] args = {tourRow.getString(Col.SERVER_ID)};
        DataRow row = browse(selection, args);
        if(row == null){
            row = createCashBox(tourRow);
        }
        return row;
    }

    private DataRow createCashBox(DataRow tourRow){
        Values values = new Values();
        values.put("state", STATE_DRAFT);
        values.put("name", generateName(tourRow));
        values.put("tour_id", tourRow.getString(Col.SERVER_ID));
        values.put("distributor_id", tourRow.getString("distributor_id"));
        values.put("creator_id", tourRow.getString("user_id"));
        values.put("user_id", tourRow.getString("user_id"));
        int rowId = insert(values);
        return browse(rowId);
    }

    private String generateName(DataRow tourRow) {
        return tourRow.getString("name")+ " " + mContext.getString(R.string.cash_box_label);
    }
}
