package com.MohafizDZ.project.visit_action_list_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.visit_action_list_dir.filter_presenter_dir.Filters;

import java.util.List;

public interface IActionListPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onItemClick(int position);

        void onSearch(String searchFilter);

        void setFilters(Filters filters);
    }

    interface View extends BasePresenter.View{

        void initAdapter(List<DataRow> rows);

        void onLoadFinished(List<DataRow> rows);

        void requestOpenDetails(String actionId);

        void setToolbarTitle(String title);
    }
}
