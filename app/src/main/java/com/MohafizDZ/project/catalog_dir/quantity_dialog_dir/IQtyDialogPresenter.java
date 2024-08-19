package com.MohafizDZ.project.catalog_dir.quantity_dialog_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;

public interface IQtyDialogPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void setAvailability(Float availability);
        void setUnitPrice(Float unitPrice);
    }

    interface View extends BasePresenter.View{

        void show();

        void setProductName(String name);

        void toggleAvailability(boolean visible);
        void togglePrice(boolean visible);
        void toggleTotalPrice(boolean visible);

        void setQuantity(String qtyText);

        void setUnitPrice(String text);
        void setAvailability(String text);

        void setTotalPrice(String text);

        void setProductImage(String base64);
    }

    interface Dialog {
        void setAvailability(float availability);
        void setUnitPrice(float unitPrice);
        void showDialog(MyAppCompatActivity activity);
    }

    interface DialogListener{
        void onPositiveClicked(ProductRow productRow, float qty);
        void onNeutralClicked(ProductRow productRow, float qty);
        void onNegativeClicked(ProductRow productRow, float qty);
    }

    enum PresenterType{
        selectQty, saleQty, backOrderQty
    }
}
