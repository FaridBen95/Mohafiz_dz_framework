package com.MohafizDZ.project.home_dir.guide_presenter_dir;

import android.content.Context;

import com.MohafizDZ.project.models.CompanyProductModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.DistributorStockLineModel;
import com.MohafizDZ.project.models.DistributorStockModel;
import com.MohafizDZ.project.models.PlannerModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.TourVisitModel;

public class Models {
    public final CompanyProductModel productModel;
    public final TourModel tourModel;
    public final DistributorModel distributorModel;
    public final PlannerModel plannerModel;
    public final TourVisitModel tourVisitModel;
    public final DistributorStockModel stockModel;
    public final DistributorStockLineModel stockLineModel;

    public Models(Context context){
        this.productModel = new CompanyProductModel(context);
        this.tourModel = new TourModel(context);
        this.distributorModel = new DistributorModel(context);
        this.plannerModel = new PlannerModel(context);
        this.tourVisitModel = new TourVisitModel(context);
        this.stockModel = new DistributorStockModel(context);
        this.stockLineModel = new DistributorStockLineModel(context);
    }
}
