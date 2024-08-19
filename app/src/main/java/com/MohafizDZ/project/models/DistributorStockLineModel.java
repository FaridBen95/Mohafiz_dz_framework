package com.MohafizDZ.project.models;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.Model;

public class DistributorStockLineModel extends Model {
    private static final String TAG = DistributorStockLineModel.class.getSimpleName();
    public static final String LINE_TYPE_INITIAL = "initial";
    public static final String LINE_TYPE_LOADING = "loading";

    public Col name = new Col(Col.ColumnType.varchar);
    public Col product_id = new Col(Col.ColumnType.many2one).setRelationalModel(CompanyProductModel.class);
    public Col order_id = new Col(Col.ColumnType.many2one).setRelationalModel(DistributorStockModel.class);
    public Col qty = new Col(Col.ColumnType.real);
    public Col line_type = new Col(Col.ColumnType.varchar);

    public DistributorStockLineModel(Context mContext) {
        super(mContext, "distributor_stock_line");
    }

    @Override
    public boolean isOnline() {
        return false;
    }
}
