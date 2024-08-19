package com.MohafizDZ.project.inventory_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.catalog_dir.quantity_dialog_dir.IQtyDialogPresenter;

import java.util.List;

public interface IInventoryPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onListUpdated();

        void onBackPressed(boolean force);

        void onValidate();

        void onCreateOptionsMenu();

        void initLines();

        void emptyList();

        void onItemClick(int position);

        void requestAddLine();

        void onCatalogCanceled();

        void setTourId(String tourId);

        void setEditable(boolean isEditable);
    }

    interface View extends BasePresenter.View{

        void startCatalogActivity(String strategyClassName);

        void initAdapter(List<ProductRow> rows, boolean canShowTheoQty);

        void onLoadFinished(List<ProductRow> rows);

        void showQtyDialog(IQtyDialogPresenter.Dialog qtyDialog);

        void onLineUpdated(int position);

        void onLineDeleted(int position);

        void toggleDeleteMenuItem(boolean visible);

        void goBack();

        void showIgnoreChangesDialog();

        void toggleValidateButton(boolean visible);
        void toggleAddButton(boolean visible);

        void refreshValidateButtonBadge(int count);

        void toggleInitMenuItem(boolean visible);
    }
}
