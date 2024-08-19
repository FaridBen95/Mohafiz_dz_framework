package com.MohafizDZ.project.customers_dir.filters_presenter_dir;

import androidx.core.util.Pair;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.project.customers_dir.Filters;

import java.util.LinkedHashMap;

public interface IFiltersPresenter {
    interface Presenter extends BasePresenter.Presenter{

        void setTourId(String tourId);
        void setSelectCustomerMode(boolean selectCustomersMode);

        void orderByName();
        void orderByVisitDate();
        void orderByProximity();

        void reverseSortBy(boolean checked);
        void showHasBalanceLimit(boolean checked);

        void onProximitySelected(String key, String value);
        void onVisitStateSelected(String key, String value);
        void onCategorySelected(String key, String value);
        void onRegionSelected(String key, String value);
        void onTourSelected(String key, String value);

        void onResetClicked();
        void requestSelectStartDate();
        void requestSelectEndDate();

        void onSelectDateRange(Pair<Long, Long> dateRange);

        void onBalanceStartChanged(String text);

        void onBalanceEndChanged(String text);

        void showPlannedCustomers(boolean checked);

        void setFilters(Filters filters);
    }

    interface View extends BasePresenter.View{

        void initRegionsFilter(LinkedHashMap<String, String> hashMap, boolean enabled, String regionName);
        void initCategoriesFilter(LinkedHashMap<String, String> hashMap);
        void initProximityFilter(LinkedHashMap<String, String> hashMap);
        void initVisitStateFilter(LinkedHashMap<String, String> hashMap);
        void initToursFilter(LinkedHashMap<String, String> hashMap, boolean enabled, String selectedTour);

        void setFilters(Filters filters);

        void orderByName();
        void orderByProximity();
        void orderByVisitDate();

        void setHasBalanceChecked(boolean checked);
        void setPlannedCustomersChecked(boolean checked);
        void setReverseChecked(Boolean checked);

        void setDateEndFilter(String text);
        void setDateStartFilter(String text);
        void setBalanceStart(String text);
        void setBalanceEnd(String text);
        void setProximityFilter(String text);
        void setVisitStateFilter(String text);
        void setCategoryFilter(String text);
        void setRegionFilter(String text);
        void setTourFilter(String text);

        void requestSelectDateRange(Pair<Long, Long> dateRange);

        void enableVisitStateFilter(boolean enabled);
        void enableVisitDatesFilter(boolean enabled);
        void enableVisitDateOrder(boolean enabled);

        void setFiltersControls();

        void requestCurrentLocation(LocationListener locationListener);
    }

    interface LocationListener{
        void onLocationChanged(double latitude, double longitude);

        void onStart();

        void onFailed();
    }

}
