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

import java.util.HashMap;
import java.util.Map;

public class DistributorStockModel extends Model {
    private static final String TAG = DistributorStockModel.class.getSimpleName();
    public static final String STATE_DRAFT = "draft";
    public static final String STATE_DONE = "done";

    public Col name = new Col(Col.ColumnType.varchar);
    public Col tour_id = new Col(Col.ColumnType.many2one).setRelationalModel(TourModel.class);
    public Col distributor_id = new Col(Col.ColumnType.many2one).setRelationalModel(DistributorStockModel.class);
    public Col creator_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class);
    public Col user_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class);
    public Col state = new Col(Col.ColumnType.varchar).setDefaultValue(STATE_DRAFT);
    public Col charging_date = new Col(Col.ColumnType.varchar);
    public Col initial_stock_date = new Col(Col.ColumnType.varchar);
    public Col validate_date = new Col(Col.ColumnType.varchar);
    public Col products = new Col(Col.ColumnType.array).setRelationalModel(CompanyProductModel.class);
    //this contains all lines
    public Col lines = new Col(Col.ColumnType.one2many).setRelationalModel(DistributorStockLineModel.class).setRelatedColumn("order_id");
    public Col loading_lines = new Col(Col.ColumnType.one2many).setRelationalModel(DistributorStockLineModel.class).setRelatedColumn("order_id");
    public Col initial_stock_lines = new Col(Col.ColumnType.one2many).setRelationalModel(DistributorStockLineModel.class).setRelatedColumn("order_id");

    public DistributorStockModel(Context mContext) {
        super(mContext, "distributor_stock");
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    public DataRow getCurrentStock(DataRow tourRow) {
        if(tourRow != null) {
            String selection = " tour_id = ? ";
            String[] args = {tourRow.getString(Col.SERVER_ID)};
            DataRow row = browse(selection, args);
            if(row == null){
                return initStockRow(tourRow);
            }
            return row;
        }
        return null;
    }

    private DataRow initStockRow(DataRow tourRow) {
        Values values = new Values();
        values.put("name", generateName(tourRow));
        values.put("tour_id", tourRow.getString(Col.SERVER_ID));
        values.put("distributor_id", tourRow.getString("distributor_id"));
        values.put("creator_id", tourRow.getString("user_id"));
        values.put("user_id", tourRow.getString("user_id"));
        //todo i should prepare init_lines from the previous stock
        int rowId = insert(values);
        return browse(rowId);
    }

    private String generateName(DataRow tourRow){
        return tourRow.getString("name") + " stock";
    }

}
