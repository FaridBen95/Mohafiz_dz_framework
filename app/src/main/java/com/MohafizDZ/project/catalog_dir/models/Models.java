package com.MohafizDZ.project.catalog_dir.models;

import android.content.Context;

import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.CompanyProductModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.DistributorStockLineModel;
import com.MohafizDZ.project.models.DistributorStockModel;
import com.MohafizDZ.project.models.TourModel;

public class Models {
    public final CompanyProductModel companyProductModel;
    public final DistributorStockModel stockModel;
    public final DistributorStockLineModel stockLineModel;
    public final CompanyCustomerModel companyCustomerModel;
    public final DistributorModel distributorModel;
    public final TourModel tourModel;
    public Models(Context context) {
        companyProductModel = new CompanyProductModel(context);
        companyCustomerModel = new CompanyCustomerModel(context);
        this.tourModel = new TourModel(context);
        this.distributorModel = new DistributorModel(context);
        stockModel = new DistributorStockModel(context);
        stockLineModel = new DistributorStockLineModel(context);
    }
}
