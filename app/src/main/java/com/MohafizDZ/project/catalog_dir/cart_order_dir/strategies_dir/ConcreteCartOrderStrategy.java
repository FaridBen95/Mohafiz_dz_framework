package com.MohafizDZ.project.catalog_dir.cart_order_dir.strategies_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.catalog_dir.cart_order_dir.ICartOrderPresenter;
import com.MohafizDZ.project.catalog_dir.models.Models;

public abstract class ConcreteCartOrderStrategy implements ICartOrderStrategy{
    protected final Context context;
    protected final ICartOrderPresenter.View view;
    protected final DataRow currentUserRow;
    protected Models models;
    protected DataRow customerRow;
    protected String customerId;

    public ConcreteCartOrderStrategy(Context context, ICartOrderPresenter.View view, DataRow currentUserRow) {
        this.context = context;
        this.view = view;
        this.currentUserRow = currentUserRow;
    }

    public void setModels(Models models) {
        this.models = models;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
        if(customerId != null){
            customerRow = models.companyCustomerModel.browse(customerId);
        }
    }

    @Override
    public String getTitle() {
        return null;
    }

    protected String getString(int resId){
        return context.getString(resId);
    }
}
