package com.MohafizDZ.project.payments_list_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.Utils.Selection;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.PaymentModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.payments_list_dir.filter_presenter_dir.Filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaymentsListPresenterImpl implements IPaymentListPresenter.Presenter{
    private static final String TAG = PaymentsListPresenterImpl.class.getSimpleName();

    private final IPaymentListPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final List<DataRow> rows;
    private DataRow tourRow, distributorRow;
    private Filters filters;
    private String searchFilter = "";

    public PaymentsListPresenterImpl(IPaymentListPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        rows = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        initData();
        view.initAdapter(rows);
        view.setToolbarTitle(getString(R.string.payments_label));
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    private void initData(){
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        tourRow = models.tourModel.getCurrentTour(distributorRow);
    }

    @Override
    public void onRefresh() {
        loadPayments();
        view.onLoadFinished(rows);
    }

    @Override
    public void onItemClick(int position) {
        DataRow row = rows.get(position);
        view.requestOpenDetails(row.getString(Col.SERVER_ID));
    }

    @Override
    public void onItemLongClick(int position) {
        DataRow row = rows.get(position);
        if(!row.getString("state").equals(PaymentModel.STATE_EXPENSES_DONE)){
            view.showValidationDialog(position);
        }
    }

    @Override
    public void validateExpense(int position) {
        DataRow row = rows.get(position);
        Values values = new Values();
        values.put("state", PaymentModel.STATE_EXPENSES_DONE);
        models.paymentModel.update(row.getString(Col.SERVER_ID), values);
        onRefresh();
    }

    @Override
    public void onSearch(String searchFilter) {
        searchFilter = searchFilter == null? "" : searchFilter;
        if(!this.searchFilter.equals(searchFilter)){
            this.searchFilter = searchFilter;
            onRefresh();
        }
    }

    @Override
    public void setFiltesr(Filters filters) {
        this.filters = filters;
    }

    private void loadPayments(){
        rows.clear();
        Selection selection = prepareSelection();
        String sortBy = prepareSortBy();
        Map<String, DataRow> customersMap = models.companyCustomerModel.getMap(Col.SERVER_ID);
        for(DataRow paymentRow : models.paymentModel.getPaymentsList(selection.getSelection(), selection.getArgs(), sortBy)){
            DataRow customerRow = customersMap.getOrDefault(paymentRow.getString("customer_id"), null);
            paymentRow.putRel("customer", customerRow);
            rows.add(paymentRow);
        }
    }

    private Selection prepareSelection() {
        Selection selection = new Selection();
        selection.addSelection(" is_expenses <> 1 ");
        if(!searchFilter.equals("")){
            selection.addSelection(" (p.name like ? or customer_name like ?) ");
            selection.addArg("%" + searchFilter + "%");
            selection.addArg("%" + searchFilter + "%");
        }
        if(filters != null) {
            if(filters.isPayments){
                selection.addSelection(" amount > 0 ");
            }
            if(filters.isRefund){
                selection.addSelection(" amount < 0 ");
            }
            if (filters.customerId != null) {
                selection.addSelection(" customer_id = ? ", filters.customerId);
            }
            if (filters.regionId != null) {
                selection.addSelection("customer_region_id = ? ", filters.regionId);
            }
            if (filters.tourId != null) {
                selection.addSelection("tour_id = ? ", filters.tourId);
            }
            if (filters.dateStart != null && filters.dateEnd != null) {
                String dateStartStr = MyUtil.milliSecToDate(filters.dateStart, MyUtil.DEFAULT_DATE_FORMAT);
                String dateEndStr = MyUtil.milliSecToDate(filters.dateEnd, MyUtil.DEFAULT_DATE_FORMAT) + " 23:59:59";
                selection.addSelection(" payment_date >= ? ", dateStartStr);
                selection.addSelection(" payment_date <= ? ", dateEndStr);
            }
            if(filters.isOrder){
                selection.addSelection(" visit_order_id not null and visit_order_id not in (? , '') ", "false");
            }
            if(filters.isFreePayments){
                selection.addSelection(" (visit_order_id is null or visit_order_id in (? , '')) ", "false");
            }
        }
        return selection;
    }

    private String prepareSortBy(){
        if(filters != null && filters.orderBy != null) {
            String direction = filters.reverse? " desc" : " asc";
            switch (filters.orderBy){
                case date:
                    return " payment_date" + direction;
                case amount:
                    return " amount" + direction;
                case customer:
                    return " customer_name" + direction;
                case reference:
                    return " p.name"+ direction;
            }
        }
        return null;
    }

    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final PaymentModel paymentModel;
        private final CompanyCustomerModel companyCustomerModel;

        private Models(Context context){
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.paymentModel = new PaymentModel(context);
            this.companyCustomerModel = new CompanyCustomerModel(context);
        }
    }
}
