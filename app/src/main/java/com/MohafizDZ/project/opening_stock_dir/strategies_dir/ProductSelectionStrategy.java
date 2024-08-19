package com.MohafizDZ.project.opening_stock_dir.strategies_dir;

import android.app.Activity;
import android.content.Context;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.catalog_dir.catalog_presenters_dir.ICatalogPresenter;
import com.MohafizDZ.project.catalog_dir.strategies_dir.MainCatalogStrategy;
import com.MohafizDZ.project.catalog_strategies.OrderCatalogStrategy;
import com.MohafizDZ.project.models.TourModel;

public class ProductSelectionStrategy extends OrderCatalogStrategy {
    public ProductSelectionStrategy(Context context, ICatalogPresenter.View view, DataRow currentUserRow) {
        super(context, view, currentUserRow);
    }

    @Override
    public boolean canShowAvailability() {
        return false;
    }

    @Override
    public boolean canShowValidateButton() {
        return true;
    }

    @Override
    public boolean canEdit() {
        String state = tourRow != null? tourRow.getString("state") : TourModel.STATE_DRAFT;
        return allowEditProducts() && (state.equals(TourModel.STATE_DRAFT) || state.equals(TourModel.STATE_CONFIRMED));
    }

    @Override
    public void onValidate() {
        view.goBack(Activity.RESULT_OK);
    }
}
