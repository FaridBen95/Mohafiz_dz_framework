package com.MohafizDZ.project.payment_dir.models;

import android.content.Context;

import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.CompanyProductModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.PaymentModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.TourVisitActionModel;
import com.MohafizDZ.project.models.TourVisitModel;
import com.MohafizDZ.project.models.VisitOrderLineModel;
import com.MohafizDZ.project.models.VisitOrderModel;

public class Models {
    public final PaymentModel paymentModel;
    public final VisitOrderModel visitOrderModel;
    public final VisitOrderLineModel visitOrderLineModel;
    public final CompanyCustomerModel companyCustomerModel;
    public final TourModel tourModel;
    public final DistributorModel distributorModel;
    public final TourVisitModel tourVisitModel;
    public final TourVisitActionModel tourVisitActionModel;
    public final CompanyProductModel companyProductModel;

    public Models(Context context){
        this.paymentModel = new PaymentModel(context);
        this.companyCustomerModel = new CompanyCustomerModel(context);
        this.tourModel = new TourModel(context);
        this.distributorModel = new DistributorModel(context);
        this.tourVisitModel = new TourVisitModel(context);
        this.visitOrderModel = new VisitOrderModel(context);
        this.visitOrderLineModel = new VisitOrderLineModel(context);
        this.tourVisitActionModel = new TourVisitActionModel(context);
        companyProductModel = new CompanyProductModel(context);
    }
}
