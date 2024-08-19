package com.MohafizDZ.project.catalog_dir.quantity_dialog_dir;

import android.content.Context;

import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.models.CompanyProductModel;

public class SelectQtyDialogPresenterImpl implements IQtyDialogPresenter.Presenter{
    private static final String TAG = SelectQtyDialogPresenterImpl.class.getSimpleName();

    protected final IQtyDialogPresenter.View view;
    protected final Context context;
    protected final ProductRow productRow;
    protected final float qty;
    protected final Models models;
    protected final String currency;
    protected Float availability, unitPrice;

    public SelectQtyDialogPresenterImpl(IQtyDialogPresenter.View view, Context context, ProductRow productRow, float qty) {
        this.view = view;
        this.context = context;
        this.productRow = productRow;
        this.qty = qty;
        this.models = new Models(context);
        this.currency = CompanyModel.getCompanyCurrency(context);
    }

    @Override
    public void onViewCreated() {
        view.setProductName(productRow.getString("name"));
        view.setQuantity(qty + "");
        view.setProductImage(productRow.getString("picture_low"));
        if(unitPrice != null){
            view.setUnitPrice(getPrice(unitPrice));
            view.togglePrice(true);
        }else{
            view.togglePrice(false);
        }
        if(availability != null){
            view.setAvailability(availability + "");
            view.toggleAvailability(true);
        }else{
            view.toggleAvailability(false);
        }
        onRefresh();
    }

    private String getPrice(float price){
        return price + " " + currency;
    }

    @Override
    public void onRefresh() {
        if(unitPrice != null && qty > 0){
            view.setTotalPrice(getTotalPrice());
            view.toggleTotalPrice(true);
        }else{
            view.toggleTotalPrice(false);
        }
        view.show();
    }

    private String getTotalPrice(){
        float price = unitPrice * qty;
        return getPrice(price);
    }

    @Override
    public void setAvailability(Float availability) {
        this.availability = availability;
    }

    @Override
    public void setUnitPrice(Float unitPrice) {
        this.unitPrice = unitPrice;
    }


    private static class Models{
        private final CompanyProductModel productModel;

        private Models(Context context){
            this.productModel = new CompanyProductModel(context);
        }
    }
}
