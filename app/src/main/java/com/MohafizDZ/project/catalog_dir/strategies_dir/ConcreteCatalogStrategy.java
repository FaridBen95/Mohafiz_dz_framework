package com.MohafizDZ.project.catalog_dir.strategies_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.catalog_dir.catalog_presenters_dir.ICatalogPresenter;
import com.MohafizDZ.project.catalog_dir.models.Models;
import com.MohafizDZ.project.models.DistributorConfigurationModel;
import com.MohafizDZ.project.models.TourConfigurationModel;

import java.util.List;

public abstract class ConcreteCatalogStrategy implements ICatalogStrategy{
    protected final Context context;
    protected final ICatalogPresenter.View view;
    protected ICatalogPresenter.Presenter presenter;
    protected Models models;
    protected final DataRow currentUserRow;
    protected String customerId;
    protected DataRow customerRow;
    protected DataRow distributorRow, tourRow, stockRow;
    public ConcreteCatalogStrategy(Context context, ICatalogPresenter.View view, DataRow currentUserRow) {
        this.context = context;
        this.view = view;
        this.currentUserRow = currentUserRow;
    }

    public void setModels(Models models) {
        this.models = models;
        initData();
    }

    public abstract List<DataRow> getProductRows(String selection, String[] args, String sortBy);

    private void initData(){
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        tourRow = models.tourModel.getCurrentTour(distributorRow);
        stockRow = models.stockModel.getCurrentStock(tourRow);
    }

    protected boolean useAvailability(){
        return tourRow != null && TourConfigurationModel.useUnlimitedStock(tourRow.getRelArray(models.tourModel, "configurations"));
    }


    protected String getString(int resId){
        return context.getString(resId);
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
        if(customerId != null) {
            this.customerRow = models.companyCustomerModel.browse(customerId);
        }
    }

    @Override
    public void onValidate() {

    }

    @Override
    public void onEmptyClicked() {

    }

    @Override
    public void onViewCreated() {

    }

    @Override
    public String getTitle() {
        return null;
    }

    protected boolean allowEditProducts(){
        return DistributorConfigurationModel.canEditProducts(distributorRow.getRelArray(models.distributorModel, "configurations"));
    }

    public void setPresenter(ICatalogPresenter.Presenter presenter) {
        this.presenter = presenter;
    }


    @Override
    public boolean canShowCustomerDetails() {
        return customerRow != null;
    }
}
