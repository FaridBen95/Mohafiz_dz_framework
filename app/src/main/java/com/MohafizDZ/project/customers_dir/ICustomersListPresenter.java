package com.MohafizDZ.project.customers_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.framework_repository.core.BitmapDataRow;

import java.util.List;

public interface ICustomersListPresenter {

    interface Presenter extends BasePresenter.Presenter{
        void setSelectCustomerMode(boolean active);

        void onItemLongClick(int position);

        void onItemClick(int position);

        void onCustomerChanged(int selectedPosition);

        void onSearch(String searchFilter);

        void setFilters(Filters filters);

        void requestOpenMap();
    }

    interface View extends BasePresenter.View{

        void toggleAddCustomer(boolean visible);

        void initAdapter(List<BitmapDataRow> rows);

        void onLoadFinished(List<BitmapDataRow> rows);

        void requestOpenDetails(int position, String customerId, String tourId);

        void onSelectCustomer(String customerId);

        void onCustomerUpdated(int selectedPosition);

        void requestOpenCustomersMap(Filters filters, boolean selectCustomerMode);
    }
}
