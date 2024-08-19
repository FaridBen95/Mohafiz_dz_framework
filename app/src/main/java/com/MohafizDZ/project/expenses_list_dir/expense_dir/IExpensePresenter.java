package com.MohafizDZ.project.expenses_list_dir.expense_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

import java.util.LinkedHashMap;
import java.util.List;

public interface IExpensePresenter {

    interface Presenter extends BasePresenter.Presenter{

        void requestAddPayment();

        void requestEditPayment();

        void onAddPayment(String value);

        void requestAddImage(Integer position, String path);

        void deleteImage(int position);

        void requestModifyImage(int position);

        void onValidate(String subject, String note);

        void onBackPressed();
    }

    interface View extends BasePresenter.View{

        void setExpensesLeft(String txt);
        void setExpense(String txt);
        void setName(String txt);
        void setAllowedLimit(String txt);
        void setExpenses(String txt);
        void setAllowedExpenses(String txt);

        void toggleAmountContainer(boolean visible);
        void toggleAddButton(boolean visible);
        void toggleEditButton(boolean visible);

        void initAdapter(List<String> attachments);

        void openPaymentDialog(float totalToPay, Float paymentAmount);

        void toggleExpenseLimit(boolean visible);
        void toggleAllowedExpenses(boolean visible);
        void toggleExpensesLeft(boolean visible);
        void toggleAttachmentsContainer(boolean visible);

        void showModifyImageDialog(int position);

        void onLoadFinished(List<String> attachments);

        void initSubjectFilter(LinkedHashMap<String, String> subjectList);

        void requestCurrentLocation(LocationListener locationListener);

        void loadDetails(String id);

        void goBack();

        void showIgnoreChangesDialog();
    }

    interface LocationListener{
        void onLocationChanged(double latitude, double longitude);

        void onStart();

        void onFailed();
    }
}
