package com.MohafizDZ.project.customer_details_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.project.customers_dir.Filters;

public interface ICustomerDetailsPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onCreateOptionsMenu();

        void requestOpenMap();
    }

    interface View extends BasePresenter.View{

        void setName(String txt);
        void setCode(String txt);
        void setRegion(String txt);
        void setGpsPosition(String txt);
        void setPhoneNum(String txt);
        void setAddress(String txt);
        void setNote(String txt);
        void setCategory(String txt);
        void setBalance(String txt);
        void setBalanceLimit(String txt);

        void setImage(String base64, String name);

        void toggleEditItem(boolean visible);

        void toggleVisitContainer(boolean visible);

        void openMap(Filters filters);
    }
}
