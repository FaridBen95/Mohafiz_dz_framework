package com.MohafizDZ.project.payment_details_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.project.payment_dir.IPaymentPresenter;

import java.util.List;

public interface IPaymentDetailsPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onCreateOptionsMenu();

        void cancelOrder(IPaymentPresenter.ValidateView validateView);

        void requestCancelOrder();

        void onValidate();
        void setExpenseValidation(boolean isValidation);
    }

    interface View extends BasePresenter.View{

        void goBack();

        void setReference(String txt);
        void setCustomerName(String txt);
        void setDistanceToCustomer(String txt);
        void setTourName(String txt);
        void setSellerName(String txt);
        void setDate(String txt);
        void setPaymentAmount(String txt);
        void setRemainingAmount(String txt);
        void setExpensesLeft(String txt);

        void toggleDeleteMenuItem(boolean visible);

        void showCancelDialog(String title, String msg, String positiveTitle);

        void toggleOrderReference(boolean visible);

        void setOrderName(String txt);

        void togglePaymentContainer(boolean visible);
        void toggleExpenseContainer(boolean visible);

        void toggleCustomerContainer(boolean visible);

        void initAdapter(List<String> attachments);

        void toggleAttachmentsContainer(boolean visible);

        void onLoadFinished(List<String> attachments);

        void toggleValidateButton(boolean visible);

        void setExpenseSubject(String txt);
        void setExpenseNote(String txt);
    }
}
