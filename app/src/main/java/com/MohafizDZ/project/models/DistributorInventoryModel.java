package com.MohafizDZ.project.models;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;

public class DistributorInventoryModel extends Model {
    private static final String TAG = DistributorInventoryModel.class.getSimpleName();
    public static final String STATE_DRAFT = "draft";
    public static final String STATE_DONE = "done";

    public Col name = new Col(Col.ColumnType.varchar);
    public Col tour_id = new Col(Col.ColumnType.many2one).setRelationalModel(TourModel.class);
    public Col distributor_id = new Col(Col.ColumnType.many2one).setRelationalModel(DistributorModel.class);
    public Col user_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class);
    public Col state = new Col(Col.ColumnType.varchar).setDefaultValue(STATE_DRAFT);
    public Col lines = new Col(Col.ColumnType.many2one).setRelationalModel(DistributorInventoryLineModel.class).setRelatedColumn("inventory_id");
    public Col products = new Col(Col.ColumnType.array).setRelationalModel(CompanyProductModel.class);
    public Col confirm_date = new Col(Col.ColumnType.varchar);
    public Col validate_date = new Col(Col.ColumnType.varchar);
    public DistributorInventoryModel(Context mContext) {
        super(mContext, "distributor_inventory");
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    public DataRow getCurrentInventory(DataRow tourRow) {
        String selection = " tour_id = ? ";
        String[] args = {tourRow.getString(Col.SERVER_ID)};
        DataRow inventoryRow = browse(selection, args);
        if(inventoryRow == null){
            return initInventoryRow(tourRow);
        }else {
            return inventoryRow;
        }
    }

    private DataRow initInventoryRow(DataRow tourRow){
        Values values = new Values();
        values.put("name", generateName(tourRow));
        values.put("tour_id", tourRow.getString(Col.SERVER_ID));
        values.put("distributor_id", tourRow.getString("distributor_id"));
        values.put("user_id", tourRow.getString("user_id"));
        int rowId = insert(values);
        return browse(rowId);
    }

    private String generateName(DataRow tourRow){
        return tourRow.getString("name") + " inventory";
    }

}
