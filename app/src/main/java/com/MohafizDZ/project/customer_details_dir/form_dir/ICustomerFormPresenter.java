package com.MohafizDZ.project.customer_details_dir.form_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

import java.util.LinkedHashMap;

public interface ICustomerFormPresenter {

    interface Presenter extends BasePresenter.Presenter{
        void setEditable(boolean isEditable);

        void requestUpdateImageView(String s);

        void onGpsLocationRecovered(Double latitude, Double longitude);

        void onBackPressed();

        void onSelectCategory(String categoryKey);
        void onSelectRegion(String regionKey);

        void createCategory(String name);

        void onRegionCreated();

        void requestCurrentLocation();

        void onValidate(String name, String phoneNum, String code, String balanceLimit, String categoryId, String regionId, double latitude, double longitude, String geoHash, String address, String note);

        void onCreateOptionsMenu();
    }

    interface View extends BasePresenter.View{

        void setName(String text);
        void setCodeText(String text);
        void setCategory(String text);
        void setRegion(String text);
        void setGpsLocation(String text, Double latitude, Double longitude);
        void setAddress(String text);
        void setNote(String text);
        void setImage(String text);

        void showIgnoreChangesDialog();

        void initRegionsFilter(LinkedHashMap<String, String> regions);
        void initCategoriesFilter(LinkedHashMap<String, String> categories);

        void requestCreateCustomerCategory();

        void openRegionMap();

        void setCodeEnabled(boolean enabled);
        void setBalanceLimitEnabled(boolean enabled);

        void setEditable(boolean isEditable);

        void toggleValidateButton(boolean visible);

        void requestCurrentLocation();

        void setValidateTitle(String title);

        void setPhoneNum(String text);

        void loadDetails(String id);

        void goBack();

        void toggleEditItem(boolean visible);

        void setBalanceLimit(String text);
    }
}
