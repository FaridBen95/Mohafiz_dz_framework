package com.MohafizDZ.project.visit_action_list_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.Utils.Selection;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.TourVisitActionModel;
import com.MohafizDZ.project.models.VisitOrderModel;
import com.MohafizDZ.project.visit_action_list_dir.filter_presenter_dir.Filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActionsListPresenterImpl implements IActionListPresenter.Presenter{
    private static final String TAG = ActionsListPresenterImpl.class.getSimpleName();

    private final IActionListPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final List<DataRow> rows;
    private DataRow tourRow, distributorRow;
    private String searchFilter = "";
    private Filters filters;

    public ActionsListPresenterImpl(IActionListPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        rows = new ArrayList<>();
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
    public void setFilters(Filters filters) {
        this.filters = filters;
    }

    @Override
    public void onViewCreated() {
        initData();
        view.initAdapter(rows);
        view.setToolbarTitle(getString(R.string.actions_label));
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
        loadActions();
        view.onLoadFinished(rows);
    }

    private void loadActions(){
        rows.clear();
        Selection selection = prepareSelection();
        String sortBy = prepareSortBy();
        Map<String, DataRow> customersMap = models.companyCustomerModel.getMap(Col.SERVER_ID);
        for(DataRow row : models.actionModel.getActionsList(selection.getSelection(), selection.getArgs(), sortBy)){
            String actionName = row.getString("action");
            DataRow customerRow = customersMap.getOrDefault(row.getString("customer_id"), null);
            row.putRel("customer", customerRow);
            row.put("action_name", models.actionModel.getAction(actionName));
            rows.add(row);
        }
    }

    private Selection prepareSelection() {
        Selection selection = new Selection();
        if(!searchFilter.equals("")){
            selection.addSelection(" (cc.name like ? or action like ?) ");
            selection.addArg("%" + searchFilter + "%");
            selection.addArg("%" + searchFilter + "%");
        }
        if(filters != null) {
            if (filters.customerId != null) {
                selection.addSelection(" customer_id = ? ", filters.customerId);
            }
            if (filters.regionId != null) {
                selection.addSelection("customer_region_id = ? ", filters.regionId);
            }
            if (filters.tourId != null) {
                selection.addSelection("tour_id = ? ", filters.tourId);
            }
            if(filters.distance != null){
                selection.addSelection(" distance_from_customer >= ? ", filters.distance + "");
            }
            if(!filters.showAllActions){
                List<String> actions = filters.getActions();
                selection.addSelection(" action in (" + MyUtil.repeat("?, ", actions.size() - 1) + " ?)");
                selection.addArgs(actions);
            }
            if (filters.dateStart != null && filters.dateEnd != null) {
                String dateStartStr = MyUtil.milliSecToDate(filters.dateStart, MyUtil.DEFAULT_DATE_FORMAT);
                String dateEndStr = MyUtil.milliSecToDate(filters.dateEnd, MyUtil.DEFAULT_DATE_FORMAT) + " 23:59:59";
                selection.addSelection(" action_date >= ? ", dateStartStr);
                selection.addSelection(" action_date <= ? ", dateEndStr);
            }
        }
        return selection;
    }

    private String prepareSortBy(){
        if(filters != null && filters.orderBy != null) {
            String direction = filters.reverse? " desc" : " asc";
            switch (filters.orderBy){
                case date:
                    return " action_date" + direction;
                case distance:
                    return " distance_from_customer" + direction;
                case customer:
                    return " lower(customer_name)" + direction;
            }
        }
        return null;
    }

    @Override
    public void onItemClick(int position) {
        DataRow row = rows.get(position);
        view.requestOpenDetails(row.getString(Col.SERVER_ID));
    }

    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final CompanyCustomerModel companyCustomerModel;
        private final TourVisitActionModel actionModel;

        private Models(Context context){
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.companyCustomerModel = new CompanyCustomerModel(context);
            this.actionModel = new TourVisitActionModel(context);
        }
    }
}
