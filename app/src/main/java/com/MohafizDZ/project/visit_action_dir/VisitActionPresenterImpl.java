package com.MohafizDZ.project.visit_action_dir;

import android.content.Context;

import androidx.databinding.ObservableArrayList;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.home_dir.visit_presenter_dir.IVisitPresenter;
import com.MohafizDZ.project.models.AttachmentLocalModel;
import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.DistributorConfigurationModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.TourConfigurationModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.TourVisitActionModel;
import com.MohafizDZ.project.models.TourVisitModel;
import com.MohafizDZ.project.models.VisitNoActionCategoryModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class VisitActionPresenterImpl implements IVisitActionPresenter.Presenter{
    private static final String TAG = VisitActionPresenterImpl.class.getSimpleName();
    private final IVisitActionPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final String actionId;
    private final Models models;
    private final List<String> attachments;
    private DataRow distributorRow, currentTourRow, actionRow, visitRow;
    private boolean isEditable;
    private String action = null, customerId = null;
    private List<String> configurations;

    public VisitActionPresenterImpl(IVisitActionPresenter.View view, Context context, DataRow currentUserRow, String actionId) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.actionId = actionId;
        this.models = new Models(context);
        this.attachments = new ArrayList<>();
        this.configurations = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        if(initData()){
            initCategories();
            view.setCustomerName(getCustomerName());
            view.setTourName(getTourName());
            view.setActionType(getActionType());
            view.setCategory(getCategory());
            view.setCategoryEnabled(isEditable);
            view.setDistance(getDistance());
            view.setDate(getActionDate());
            view.initAdapter(attachments);
            view.toggleNoActionCategory(isNoAction());
            onRefresh();
        }else{
            view.goBack();
        }
    }

    private boolean canCreateCategory(){
        return DistributorConfigurationModel.canCreateNoActionCategory(configurations);
    }

    private DataRow createCategoryRow(){
        DataRow row = new DataRow();
        row.put("name", getString(R.string.create_category_label));
        row.put("id", "-2");
        row.put("_id", -2);
        return row;
    }


    private void initCategories(){
        view.initCategoriesFilter(getCategories());
    }


    private LinkedHashMap<String, String> getCategories(){
        List<DataRow> categories = models.noActionCategoryModel.getRows();
        if(canCreateCategory()) {
            categories.add(createCategoryRow());
        }
        return getNamesFromRows(categories);
    }

    private LinkedHashMap<String, String> getNamesFromRows(List<DataRow> rows) {
        LinkedHashMap<String, String> list = new LinkedHashMap<>();
        for(DataRow row : rows){
            list.put(row.getString(Col.SERVER_ID), row.getString("name"));
        }
        return list;
    }

    private String getCategory(){
        try {
            return models.noActionCategoryModel.browse(actionRow.getString("no_action_category_id")).getString("name");
        }catch (Exception ignored){}
        return "";
    }

    private boolean isNoAction(){
        String action = actionRow != null? actionRow.getString("action") : this.action;
        return action.equals(TourVisitActionModel.ACTION_NO_ACTION);
    }
    private String getString(int resId){
        return context.getString(resId);
    }

    private String getCustomerName(){
        DataRow customerRow = actionRow != null? models.customerModel.browse(actionRow.getString("customer_id")):
                models.customerModel.browse(customerId);
        return customerRow != null? customerRow.getString("name") : "-";
    }

    private String getTourName() {
        DataRow tourRow = actionRow != null? models.tourModel.browse(actionRow.getString("tour_id")):
                this.currentTourRow;
        return tourRow != null? tourRow.getString("name") : "-";
    }

    private String getActionType() {
        String action = this.action != null? this.action : actionRow.getString("action");
        return models.tourVisitActionModel.getAction(action);
    }

    private String getDistance(){
        if(actionRow != null) {
            float distance = actionRow.getFloat("distance_from_customer");
            return Math.round(distance) + " " + getString(R.string.meters_label);
        }else{
            return "-";
        }
    }

    private String getActionDate(){
        String actionDate = actionRow != null? actionRow.getString("action_date") : MyUtil.getCurrentDate();
        return actionDate != null? actionDate : "-";
    }

    private boolean initData(){
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        currentTourRow = models.tourModel.getCurrentTour(distributorRow);
        actionRow = actionId != null? models.tourVisitActionModel.browse(actionId) : null;
        visitRow = actionRow != null?
                models.tourVisitModel.browse(actionRow.getString("tour_id")):
                models.tourVisitModel.getCurrentVisit(getTourId(), customerId);
        configurations.clear();
        configurations.addAll(currentTourRow.getRelArray(models.tourModel, "configurations"));
        configurations.addAll(distributorRow.getRelArray(models.distributorModel, "configurations"));
        return actionRow != null || action != null;
    }

    private String getTourId(){
        DataRow tourRow = actionRow != null? models.tourModel.browse(actionRow.getString("tour_id")):
                this.currentTourRow;
        return tourRow != null? tourRow.getString(Col.SERVER_ID) : "-";
    }
    @Override
    public void onRefresh() {
        if(actionRow != null) {
            String note = actionRow.getString("note");
            note = note.equals("false")? "" : note;
            view.setNote(note);
        }
        loadAttachments();
        refreshImagesAdapter();
        boolean isEditable = isEditable();
        view.toggleAddImageButton(isEditable);
        view.toggleValidateButton(isEditable);
        view.toggleNoteInput(isEditable);
        view.toggleNoteTextView(!isEditable);
    }

    private boolean isEditable(){
        return isEditable;
    }

    private void loadAttachments(){
        if(actionRow != null) {
            attachments.clear();
            String selection = " model_name = ? and rel_id = ? ";
            String[] args = {models.tourVisitActionModel.getModelName(), actionRow.getString(Col.SERVER_ID)};
            for (DataRow row : models.attachmentLocalModel.getRows(selection, args)) {
                attachments.add(row.getString("path"));
            }
        }
    }

    @Override
    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    @Override
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @Override
    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public void requestAddImage(Integer position, String imagePath) {
        if(position == null) {
            attachments.add(imagePath);
        }else{
            attachments.set(position, imagePath);
        }
        refreshImagesAdapter();
    }

    @Override
    public void requestModifyImage(int position) {
        if(isEditable()){
            view.showModifyImageDialog(position);
        }
    }

    @Override
    public void deleteImage(int position) {
        attachments.remove(position);
        refreshImagesAdapter();
    }

    @Override
    public void onValidate(String categoryId, String note) {
        view.requestCurrentLocation(new IVisitActionPresenter.LocationListener() {
            @Override
            public void onLocationChanged(double latitude, double longitude) {
                view.toggleLoading(false);
                Values values = new Values();
                values.put("visit_id", visitRow.getString(Col.SERVER_ID));
                values.put("tour_id", currentTourRow.getString(Col.SERVER_ID));
                values.put("customer_id", customerId);
                DataRow customerRow = models.customerModel.browse(customerId);
                values.put("action", action);
                values.put("action_date", MyUtil.getCurrentDate());
                values.put("rel_model_name", models.tourVisitActionModel.getModelName());
                double customerLatitude = Double.valueOf(customerRow.getString("latitude"));
                double customerLongitude = Double.valueOf(customerRow.getString("longitude"));
                values.put("distance_from_customer", MyUtil.distance(latitude, longitude, customerLatitude, customerLongitude));
                values.put("latitude", latitude);
                values.put("longitude", longitude);
                values.put("geo_hash", MyUtil.getGeoHash(latitude, longitude));
                values.put("note", note);
                values.put("no_action_category_id", categoryId);
                final int rowId = models.tourVisitActionModel.insert(values);
                if (rowId > 0) {
                    //todo test if using cache would work
                    String id = models.tourVisitActionModel.browse(rowId).getString(Col.SERVER_ID);
                    for(String image : attachments){
                        Values attachmentValues = new Values();
                        attachmentValues.put("path", image);
                        attachmentValues.put("col_name", "images_list");
                        attachmentValues.put("model_name", models.tourVisitActionModel.getModelName());
                        attachmentValues.put("rel_id", id);
                        attachmentValues.put("is_uploaded_to_server", 0);
                        models.attachmentLocalModel.insert(attachmentValues);
                    }
                    view.loadDetails(id);
                    view.goBack();
                } else {
                    view.showToast(getString(R.string.error_occurred));
                }
            }

            @Override
            public void onStart() {
                view.toggleLoading(true);
            }

            @Override
            public void onFailed() {
                view.toggleLoading(false);
            }
        });
    }

    @Override
    public void onSelectCategory(String key) {
        if(key.equals("-2")){
            view.requestCreateCustomerCategory();
        }
    }

    @Override
    public void createCategory(String name) {
        if(!name.equals("")) {
            Values values = new Values();
            values.put("name", name);
            values.put("creator_id", currentUserRow.getString(Col.SERVER_ID));
            models.noActionCategoryModel.insert(values);
            initCategories();
        }else{
            view.showToast(getString(R.string.name_required));
        }
    }

    private void refreshImagesAdapter(){
        view.toggleAttachmentsContainer(attachments.size() > 0);
        view.onLoadFinished(attachments);
    }

    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final TourVisitActionModel tourVisitActionModel;
        private final TourVisitModel tourVisitModel;
        private final CompanyCustomerModel customerModel;
        private final AttachmentLocalModel attachmentLocalModel;
        private final VisitNoActionCategoryModel noActionCategoryModel;

        public Models(Context context) {
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.tourVisitActionModel = new TourVisitActionModel(context);
            this.tourVisitModel = new TourVisitModel(context);
            this.customerModel = new CompanyCustomerModel(context);
            this.attachmentLocalModel = new AttachmentLocalModel(context);
            this.noActionCategoryModel = new VisitNoActionCategoryModel(context);
        }
    }
}
