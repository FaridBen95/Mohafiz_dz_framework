package com.MohafizDZ.project.opening_stock_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.catalog_dir.quantity_dialog_dir.IQtyDialogPresenter;
import com.MohafizDZ.project.catalog_dir.quantity_dialog_dir.QtyDialog;

import java.util.List;

public interface IOpeningStockPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onListUpdated();

        void onItemClick(int position);

        void onCreateOptionsMenu();

        void emptyList();

        void onBackPressed();

        void onBackPressed(boolean force);

        void onValidate();

        void setTourId(String tourId);
    }

    interface View extends BasePresenter.View{

        void initAdapter(List<ProductRow> rows);

        void onLoadFinished(List<ProductRow> rows);

        void showQtyDialog(IQtyDialogPresenter.Dialog qtyDialog);

        void onLineUpdated(int position);

        void onLineDeleted(int position);

        void toggleDeleteMenuItem(boolean visible);

        void goBack();

        void showIgnoreChangesDialog();

        void toggleValidateButton(boolean visible);

        void refreshValidateButtonBadge(int count);

        void toggleAddButton(boolean visible);
    }
}
