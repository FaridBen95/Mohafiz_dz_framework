package com.MohafizDZ.project.payments_list_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.payments_list_dir.filter_presenter_dir.Filters;

import java.util.List;

public interface IPaymentListPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onItemClick(int position);

        void onItemLongClick(int position);

        void validateExpense(int position);

        void onSearch(String searchFilter);

        void setFiltesr(Filters filters);
    }

    interface View extends BasePresenter.View{

        void initAdapter(List<DataRow> rows);

        void onLoadFinished(List<DataRow> rows);

        void requestOpenDetails(String orderId);

        void setToolbarTitle(String title);

        void showValidationDialog(int position);
    }
}
