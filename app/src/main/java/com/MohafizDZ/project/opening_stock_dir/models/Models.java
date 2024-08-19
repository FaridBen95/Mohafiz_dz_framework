package com.MohafizDZ.project.opening_stock_dir.models;

import android.content.Context;

import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.DistributorStockLineModel;
import com.MohafizDZ.project.models.DistributorStockModel;
import com.MohafizDZ.project.models.TourModel;

public class Models {
    public final TourModel tourModel;
    public final DistributorStockModel stockModel;
    public final DistributorModel distributorModel;
    public final DistributorStockLineModel stockLineModel;

    private Models(Context context){
        this.tourModel = new TourModel(context);
        this.stockModel = new DistributorStockModel(context);
        this.distributorModel = new DistributorModel(context);
        this.stockLineModel = new DistributorStockLineModel(context);
    }
}
