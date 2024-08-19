package com.MohafizDZ.project.expenses_list_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.framework_repository.core.DataRow;

import java.util.List;

public interface IExpensesPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void requestCreateExpense();

        void onItemClick(int position);
        void onItemLongClick(int position);

        void requestValidateExpenses();

        void validateExpenses();

        void setTourId(String tourId);

        void setEditable(boolean isEditable);
    }

    interface View extends BasePresenter.View{

        void initAdapter(List<DataRow> rows);

        void onLoadFinished(List<DataRow> rows);

        void requestOpenDetails(String paymentId, boolean canValidateExpense);

        void openExpenseActivity();

        void toggleBottomNavigation(boolean isVisible);

        void toggleTotalContainer(boolean isVisible);

        void setTotalExpenses(String txt);

        void setValidatedExpenses(String txt);

        void toggleValidateButton(boolean visible);

        void showConfirmationDialog(String title, String msg);

        void toggleCreateButton(boolean visible);

        void requestValidateItem(ValidateDialogListener dialogListener);

        void setExpensesLimit(boolean visible, String price);
    }

    interface ValidateDialogListener{
        void onValidate();
    }
}
