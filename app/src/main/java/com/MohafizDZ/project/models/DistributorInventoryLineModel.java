package com.MohafizDZ.project.models;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.Model;

public class DistributorInventoryLineModel extends Model {
    private static final String TAG = DistributorInventoryLineModel.class.getSimpleName();
    public static final String STATE_DRAFT = "draft";
    public static final String STATE_CONFIRM = "confirm";
    public static final String STATE_DONE = "done";


    public Col name = new Col(Col.ColumnType.varchar);
    public Col product_id = new Col(Col.ColumnType.many2one).setRelationalModel(CompanyProductModel.class);
    public Col qty = new Col(Col.ColumnType.real);
    public Col theo_qty = new Col(Col.ColumnType.real);
    public Col tour_id = new Col(Col.ColumnType.many2one).setRelationalModel(TourModel.class);
    public Col distributor_id = new Col(Col.ColumnType.many2one).setRelationalModel(DistributorModel.class);
    public Col inventory_id = new Col(Col.ColumnType.many2one).setRelationalModel(DistributorInventoryModel.class);
    public Col state = new Col(Col.ColumnType.varchar).setDefaultValue(STATE_DRAFT);
    public DistributorInventoryLineModel(Context mContext) {
        super(mContext, "distributor_inventory_line");
    }

    @Override
    public boolean isOnline() {
        return false;
    }
}
