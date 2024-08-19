package com.MohafizDZ.project.sales_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.VisitOrderLineModel;

import java.util.ArrayList;
import java.util.List;

public class SalesPresenterImpl implements ISalesPresenter.Presenter{
    private static final String TAG = SalesPresenterImpl.class.getSimpleName();

    private final ISalesPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final List<DataRow> rows;
    private DataRow distributorRow, tourRow;
    private String tourId;
    private boolean isEditable;

    public SalesPresenterImpl(ISalesPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        this.rows = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        view.setToolbarTitle(getString(R.string.sales_label));
        initData();
        view.initAdapter(rows);
        onRefresh();
    }

    private void initData(){
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        tourRow = tourId != null? models.tourModel.browse(tourId): models.tourModel.getCurrentTour(distributorRow);
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    @Override
    public void onRefresh() {
        view.toggleValidateButton(isEditable && unvalidated());
        loadLines();
        view.onLoadFinished(rows);
    }

    private void loadLines(){
        rows.clear();
        rows.addAll(models.orderLineModel.getPreClosingSales(tourRow.getString(Col.SERVER_ID)));
    }

    private boolean unvalidated(){
        return !tourRow.getBoolean("sales_validated");
    }

    @Override
    public void onItemClick(int position) {
        DataRow productRow = rows.get(position);
        view.requestOpenProductDetails(productRow.getString(Col.SERVER_ID), false);
    }

    @Override
    public void onValidate() {
        Values values = new Values();
        values.put("sales_validated", 1);
        models.tourModel.update(tourRow.getString(Col.SERVER_ID), values);
        view.goBack();
    }

    @Override
    public void setTourId(String tourId) {
        this.tourId = tourId;
    }

    @Override
    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final VisitOrderLineModel orderLineModel;

        private Models(Context context){
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.orderLineModel = new VisitOrderLineModel(context);
        }
    }
}
