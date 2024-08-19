package com.MohafizDZ.project.catalog_dir.catalog_presenters_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.project.catalog_dir.models.Filters;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.catalog_dir.quantity_dialog_dir.IQtyDialogPresenter;

import java.util.List;

public interface ICatalogPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onItemClick(int position);

        void onItemLongClick(int position);

        void onValidate();

        void onCreateOptionsMenu();

        void onEmptyClicked();

        void refreshLine(int position);

        void setFilters(Filters filters);

        void onSearch(String searchFilter);

        void onProductScan(String code);
    }

    interface View extends BasePresenter.View{

        void toggleAddProduct(boolean visible);

        void initAdapter(List<ProductRow> rows, boolean showAvailability);

        void onLoadFinished(List<ProductRow> rows);

        void requestOpenProductDetails(String productId, boolean editable);

        void refreshItem(int position);

        void toggleValidateContainer(boolean visible);

        void refreshValidateButtonBadge(int count);

        void setTotalAmount(String text);

        void toggleEmptyMenuItem(boolean visible);

        void onListUpdated();

        void openCartOrder(String strategyClassName);

        void setCustomerName(String text);

        void setToolbarTitle(String title);

        void goBack(int resultCode);

        void toggleValidateDetailsContainer(boolean visible);

        void showQtyDialog(IQtyDialogPresenter.Dialog qtyDialog);

        void onLineDeleted(int position);

        void onLineUpdated(int position);

        void setSearchFilter(String name);
    }
}
