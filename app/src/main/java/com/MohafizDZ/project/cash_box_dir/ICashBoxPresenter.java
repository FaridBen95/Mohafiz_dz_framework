package com.MohafizDZ.project.cash_box_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.framework_repository.core.DataRow;

import java.util.LinkedHashMap;
import java.util.List;

public interface ICashBoxPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onAddClicked();

        void onValidate();

        void requestToggleDetails();

        void createOrUpdateLine(boolean updating, String denomination, String count);

        void onItemClick(int position);

        void deleteLine(Integer denominationValue);

        void setTourId(String tourId);

        void setEditable(boolean isEditable);
    }

    interface View extends BasePresenter.View{

        void onLoadFinished(List<DataRow> rows, boolean validated);

        void initAdapter(List<DataRow> rows, boolean validated);

        void toggleAddButton(boolean visible);

        void toggleCashBoxDetails(boolean visible);

        void setCashBoxTotal(String txt);

        void setName(String txt);
        void setTotalPayments(String txt);
        void setTotalRefunds(String txt);
        void setTotalExpenses(String txt);
        void setTotal(String txt);

        void updateToggleButtonIcon(int drawableResId);

        void toggleCashBoxDetailsContainer(boolean visible);

        void showLineCreationDialog(LinkedHashMap<String, String> denominations, Integer denominationValue, Integer count, boolean editable);

        void showValidateDate(String validateDate);

        void toggleValidateButton(boolean visible);

        void goBack();
    }
}
