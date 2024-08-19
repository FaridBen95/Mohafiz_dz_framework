package com.MohafizDZ.project.sales_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.framework_repository.core.DataRow;

import java.util.List;

public interface ISalesPresenter {

    interface Presenter extends BasePresenter.Presenter {

        void onItemClick(int position);

        void onValidate();
        void setTourId(String tourId);
        void setEditable(boolean isEditable);
    }

    interface View extends BasePresenter.View{

        void toggleValidateButton(boolean visible);

        void initAdapter(List<DataRow> rows);

        void onLoadFinished(List<DataRow> rows);

        void requestOpenProductDetails(String productId, boolean editable);

        void goBack();
    }
}
