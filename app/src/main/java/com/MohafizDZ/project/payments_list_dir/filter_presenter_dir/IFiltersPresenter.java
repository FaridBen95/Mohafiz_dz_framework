package com.MohafizDZ.project.payments_list_dir.filter_presenter_dir;

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
        void onTourSelected(String id, String name);

        void onResetClicked();

        void reverseSortBy(boolean b);
        void setOrdersOnly(boolean b);
        void setFreePaymentsOnly(boolean b);

        void requestSelectStartDate();

        void requestSelectEndDate();

        void onSelectDateRange(Pair<Long, Long> selection);

        void setTourId(String tourId);

        void setPayments(boolean isSales);

        void setRefunds(boolean isBackOrders);
        void checkFreePayments(boolean isFreePaymentsOnly);

        void setCustomerId(String customerId);
    }

    interface View extends BasePresenter.View{

        void setReverseChecked(boolean checked);
        void setFreePaymentsOnlyChecked(boolean checked);

        void setDateStartFilter(String dateStr);

        void setDateEndFilter(String dateStr);

        void setCustomerFilter(String customerName);

        void setRegionFilter(String regionName);

        void setTourFilter(String tourName);

        void orderByRef();
        void orderByDate();
        void orderByCustomer();
        void orderByAmount();

        void setFilters(Filters filters);

        void initCustomersFilter(LinkedHashMap<String, String> hashMap, String customerName);
        void initRegionsFilter(LinkedHashMap<String, String> hashMap);
        void initToursFilter(LinkedHashMap<String, String> hashMap, String selectedTourName);

        void requestSelectDateRange(Pair<Long, Long> dateRange);
    }
}
