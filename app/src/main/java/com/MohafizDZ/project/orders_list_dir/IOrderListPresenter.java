package com.MohafizDZ.project.orders_list_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.orders_list_dir.filter_presenter_dir.Filters;

import java.util.List;

public interface IOrderListPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onItemClick(int position);

        void setFilters(Filters filters);

        void onSearch(String searchFilter);
    }

    interface View extends BasePresenter.View{

        void initAdapter(List<DataRow> rows);

        void onLoadFinished(List<DataRow> rows);

        void requestOpenDetails(String orderId);

        void setToolbarTitle(String title);
    }
}
