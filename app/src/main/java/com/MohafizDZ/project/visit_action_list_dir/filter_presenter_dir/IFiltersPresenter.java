package com.MohafizDZ.project.visit_action_list_dir.filter_presenter_dir;

import androidx.core.util.Pair;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

import java.util.LinkedHashMap;

public interface IFiltersPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void orderByDate();
        void orderByCustomer();
        void orderByDistance();

        void onCustomerSelected(String id, String name);
        void onRegionSelected(String id, String name);
        void onTourSelected(String id, String name);

        void onResetClicked();

        void reverseSortBy(boolean b);

        void requestSelectStartDate();

        void requestSelectEndDate();

        void onSelectDateRange(Pair<Long, Long> selection);

        void setTourId(String tourId);
        void setCustomerId(String customerId);

        void setSelectedAction(String action);

        void filterByAllCategories(boolean checked);

        void onActionClicked(int position);
    }

    interface View extends BasePresenter.View{

        void setReverseChecked(boolean checked);

        void setDateStartFilter(String dateStr);

        void setDateEndFilter(String dateStr);

        void setCustomerFilter(String customerName);

        void setRegionFilter(String regionName);

        void setTourFilter(String tourName);

        void orderByDate();
        void orderByCustomer();
        void orderByDistance();

        void setFilters(Filters filters);

        void initCustomersFilter(LinkedHashMap<String, String> hashMap, String customerName);
        void initRegionsFilter(LinkedHashMap<String, String> hashMap);
        void initToursFilter(LinkedHashMap<String, String> hashMap, String selectedTourName);

        void requestSelectDateRange(Pair<Long, Long> dateRange);

        void initProximityFilter(LinkedHashMap<String, String> proximityMap);

        void setProximityFilter(String text);

        void clearActionsFilter();

        void createActionChip(String name, int position, boolean checked);

        void filterByAllActions(boolean showAllActions);
    }
}
