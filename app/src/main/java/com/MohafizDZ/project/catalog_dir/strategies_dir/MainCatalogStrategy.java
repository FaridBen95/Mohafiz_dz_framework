package com.MohafizDZ.project.catalog_dir.strategies_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.catalog_dir.catalog_presenters_dir.ICatalogPresenter;
import com.MohafizDZ.project.catalog_dir.models.CartItemSingleton;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.models.TourModel;

import java.util.List;

public class MainCatalogStrategy extends ConcreteCatalogStrategy{
    @Override
    public boolean canShowAvailability() {
        return stockRow != null && useAvailability();
    }

    public MainCatalogStrategy(Context context, ICatalogPresenter.View view, DataRow currentUserRow) {
        super(context, view, currentUserRow);
        CartItemSingleton.getInstance().setCanShowQty(false);
    }

    @Override
    public boolean canEdit() {
        String state = tourRow != null? tourRow.getString("state") : TourModel.STATE_DRAFT;
        return allowEditProducts() && (state.equals(TourModel.STATE_DRAFT) || state.equals(TourModel.STATE_CONFIRMED));
    }

    @Override
    public boolean canShowValidateButton() {
        return false;
    }

    @Override
    public List<DataRow> getProductRows(String selection, String[] args, String sortBy) {
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        return models.companyProductModel.getProducts(tourId, selection, args, sortBy);
    }

    @Override
    public void onItemClick(int position, ProductRow productRow) {
        view.requestOpenProductDetails(productRow.getString(Col.SERVER_ID), false);
    }

    @Override
    public void onItemLongClick(int position, ProductRow productRow) {

    }

    @Override
    public boolean canEmpty() {
        return false;
    }
}
