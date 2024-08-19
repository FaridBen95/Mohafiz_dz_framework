package com.MohafizDZ.project.customers_dir.customers_map_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.project.customers_dir.Filters;

public interface ICustomersMapPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onSearch(String searchFilter);

        void setFilters(Filters filters);

        void onCustomerClick(Integer position);

        void setSelectCustomerMode(boolean selectCustomerMode);

        void setEditable(boolean editable);

        void onMarkerDragged(Integer position, double latitude, double longitude);
    }

    interface View extends BasePresenter.View{

        void addMarker(String name, double latitude, double longitude, boolean draggable, int position);

        void clearMarkers();

        void getCurrentLocation();

        void openCustomerDetails(String customerId, String tourId);

        void goBack();

        void onSelectCustomer(String customerId);

        void onCustomerUpdated();
    }
}
