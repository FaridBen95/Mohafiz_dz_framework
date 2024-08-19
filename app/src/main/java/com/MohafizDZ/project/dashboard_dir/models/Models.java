package com.MohafizDZ.project.dashboard_dir.models;

import android.content.Context;

import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.RegionModel;
import com.MohafizDZ.project.models.TourModel;

public class Models {
    public final TourModel tourModel;
    public final DistributorModel distributorModel;
    public final RegionModel regionModel;

    public Models(Context context){
        this.tourModel = new TourModel(context);
        this.distributorModel = new DistributorModel(context);
        this.regionModel = new RegionModel(context);
    }
}
