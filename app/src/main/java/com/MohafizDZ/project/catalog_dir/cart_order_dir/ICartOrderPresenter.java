package com.MohafizDZ.project.catalog_dir.cart_order_dir;

import androidx.annotation.OptIn;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.google.android.material.badge.ExperimentalBadgeUtils;

import java.util.List;

public interface ICartOrderPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onValidate();
    }

    interface View extends BasePresenter.View{

        void initAdapter(List<ProductRow> rows);

        void onLoadFinished(List<ProductRow> rows);

        @OptIn(markerClass = ExperimentalBadgeUtils.class)
        void refreshValidateButtonBadge(int count);

        void toggleValidateContainer(boolean visible);

        void setTotalAmount(String text);

        void setCustomerName(String text);

        void openPaymentActivity(String strategyClassName);

        void setToolbarTitle(String title);
    }
}
