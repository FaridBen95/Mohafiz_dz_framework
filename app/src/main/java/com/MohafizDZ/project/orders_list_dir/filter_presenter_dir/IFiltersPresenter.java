package com.MohafizDZ.project.orders_list_dir.filter_presenter_dir;

import androidx.core.util.Pair;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

import java.util.LinkedHashMap;

public interface IFiltersPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void orderByRef();
        void orderByDate();
        void orderByCustomer();
        void orderByAmount();

        void onCustomerSelected(String id, String name);
        void onRegionSelected(String id, String name);
        void onProductSelected(String id, String name);
        void onTourSelected(String id, String name);

        void onResetClicked();

        void reverseSortBy(boolean b);

        void requestSelectStartDate();

        void requestSelectEndDate();

        void onSelectDateRange(Pair<Long, Long> selection);

        void setTourId(String tourId);

        void setSalesFilter(boolean isSales);

        void setBackOrdersFilter(boolean isBackOrders);

        void setCustomerId(String customerId);
    }

    interface View extends BasePresenter.View{

        void setReverseChecked(boolean checked);

        void setDateStartFilter(String dateStr);

        void setDateEndFilter(String dateStr);

        void setCustomerFilter(String customerName);

        void setRegionFilter(String regionName);

        void setTourFilter(String tourName);
        void setProductFilter(String productName);

        void orderByRef();
        void orderByDate();
        void orderByCustomer();
        void orderByAmount();

        void setFilters(Filters filters);

        void initCustomersFilter(LinkedHashMap<String, String> hashMap, String customerName);
        void initRegionsFilter(LinkedHashMap<String, String> hashMap);
        void initToursFilter(LinkedHashMap<String, String> hashMap, String selectedTourName);
        void initProductsFilter(LinkedHashMap<String, String> hashMap);

        void requestSelectDateRange(Pair<Long, Long> dateRange);
    }
}
