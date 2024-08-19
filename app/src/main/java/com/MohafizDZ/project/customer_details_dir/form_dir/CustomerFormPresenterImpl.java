package com.MohafizDZ.project.customer_details_dir.form_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.CustomerCategoryModel;
import com.MohafizDZ.project.models.DistributorConfigurationModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.RegionModel;
import com.MohafizDZ.project.models.TourConfigurationModel;
import com.MohafizDZ.project.models.TourModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CustomerFormPresenterImpl implements ICustomerFormPresenter.Presenter{
    private static final String TAG = CustomerFormPresenterImpl.class.getSimpleName();

    private final ICustomerFormPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final String customerId;
    private DataRow tourRow, distributorRow, customerRow;
    private final List<String> configurations;
    private boolean isEditable;
    private String customerImage;

    public CustomerFormPresenterImpl(ICustomerFormPresenter.View view, Context context, DataRow currentUserRow, String customerId) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        this.customerId = customerId;
        configurations = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        initData();
        initRegions();
        initCategories();
        String validateTitle;
        if(customerRow != null){
            view.setName(customerRow.getString("name"));
            view.setCodeText(customerRow.getString("customer_code"));
            view.setPhoneNum(customerRow.getString("phone_num"));
            view.setBalanceLimit(customerRow.getString("balance_limit"));
            view.setCategory(getCategory());
            view.setRegion(getRegion());
            Double latitude = 0.0d;
            Double longitude = 0.0d;
            try {
                latitude = Double.valueOf(customerRow.getString("latitude"));
                longitude = Double.valueOf(customerRow.getString("longitude"));
            }catch (Exception ignored){}
            view.setGpsLocation(getGeoHash(), latitude, longitude);
            view.setAddress(customerRow.getString("address"));
            view.setNote(customerRow.getString("note"));
            customerImage = customerRow.getString("picture_low");
            view.setImage(customerImage);
            validateTitle = getString(R.string.update);
        }else{
            validateTitle = getString(R.string.create_label);
            view.setRegion(getTourRegion());
        }
        view.setValidateTitle(validateTitle);
        onRefresh();
    }

    @Override
    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    private String getCategory(){
        try {
            return models.customerCategoryModel.browse(customerRow.getString("category_id")).getString("name");
        }catch (Exception ignored){}
        return "";
    }

    private String getRegion(){
        try {
            return models.regionModel.browse(customerRow.getString("region_id")).getString("name");
        }catch (Exception ignored){}
        return "";
    }

    private String getTourRegion(){
        try{
            return models.regionModel.browse(tourRow.getString("region_id")).getString("name");
        }catch (Exception ignored){}
        return "";
    }

    private String getGeoHash(){
        try{
            double latitude = Double.valueOf(customerRow.getString("latitude"));
            double longitude = Double.valueOf(customerRow.getString("longitude"));
            return MyUtil.getGeoHash(latitude, longitude);
        }catch (Exception ignored){}
        return "";
    }

    private void initData(){
        this.distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        this.tourRow = models.tourModel.getCurrentTour(distributorRow);
        if(customerId != null){
            this.customerRow = models.companyCustomerModel.browse(customerId);
        }
        configurations.clear();
        configurations.addAll(tourRow.getRelArray(models.tourModel, "configurations"));
        configurations.addAll(distributorRow.getRelArray(models.distributorModel, "configurations"));
    }

    private void initRegions(){
        view.initRegionsFilter(getRegions());
    }

    private void initCategories(){
        view.initCategoriesFilter(getCategories());
    }

    private LinkedHashMap<String, String> getRegions(){
        List<DataRow> regions = models.regionModel.getRows();
        if(canCreateRegion()) {
            regions.add(createRegionRow());
        }
        return getNamesFromRows(regions);
    }

    private LinkedHashMap<String, String> getCategories(){
        List<DataRow> categories = models.customerCategoryModel.getRows();
        if(canCreateCategory()) {
            categories.add(createCategoryRow());
        }
        return getNamesFromRows(categories);
    }

    private boolean canCreateCustomerCode(){
        return TourConfigurationModel.canPrintCustomerCode(configurations);
    }

    private boolean canEditBalanceLimit(){
        return DistributorConfigurationModel.canEditCustomerBalance(configurations);
    }

    private boolean canCreateCategory(){
        return DistributorConfigurationModel.canEditCustomerCategory(configurations);
    }

    private boolean canCreateRegion(){
        return DistributorConfigurationModel.canEditRegions(configurations);
    }

    private DataRow createRegionRow(){
        DataRow row = new DataRow();
        row.put("name", getString(R.string.create_region_label));
        row.put("id", "-2");
        row.put("_id", -2);
        return row;
    }

    private DataRow createCategoryRow(){
        DataRow row = new DataRow();
        row.put("name", getString(R.string.create_category_label));
        row.put("id", "-2");
        row.put("_id", -2);
        return row;
    }

    private LinkedHashMap<String, String> getNamesFromRows(List<DataRow> rows) {
        LinkedHashMap<String, String> list = new LinkedHashMap<>();
        for(DataRow row : rows){
            list.put(row.getString(Col.SERVER_ID), row.getString("name"));
        }
        return list;
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    @Override
    public void onRefresh() {
        boolean isEditable = isEditable();
        view.setEditable(isEditable);
        view.setCodeEnabled(isEditable && canCreateCustomerCode());
        view.setBalanceLimitEnabled(isEditable && canEditBalanceLimit());
        view.toggleValidateButton(isEditable);
        view.toggleEditItem(!isEditable && canEditCustomers());
    }

    @Override
    public void onCreateOptionsMenu() {
        view.toggleEditItem(!isEditable() && canEditCustomers());
    }

    private boolean canEditCustomers(){
        List<String> configurations = tourRow.getRelArray(models.tourModel, "configurations");
        return TourConfigurationModel.canEditCustomers(configurations);
    }

    private boolean isEditable(){
        return customerRow == null || isEditable;
    }

    @Override
    public void requestUpdateImageView(String base64Image) {
        customerImage = base64Image;
        view.setImage(base64Image);
    }

    @Override
    public void onGpsLocationRecovered(Double latitude, Double longitude) {
        view.setGpsLocation(MyUtil.getGeoHash(latitude, longitude), latitude, longitude);
    }

    @Override
    public void onBackPressed() {
        if(isEditable) {
            view.showIgnoreChangesDialog();
        }else{
            view.goBack();
        }
    }

    @Override
    public void onSelectCategory(String categoryKey) {
        if(categoryKey.equals("-2")){
            view.requestCreateCustomerCategory();
        }
    }

    @Override
    public void onSelectRegion(String regionKey) {
        if(regionKey.equals("-2")){
            view.openRegionMap();
        }
    }

    @Override
    public void createCategory(String name) {
        if(!name.equals("")) {
            Values values = new Values();
            values.put("name", name);
            values.put("creator_id", currentUserRow.getString(Col.SERVER_ID));
            models.customerCategoryModel.insert(values);
            initCategories();
        }else{
            view.showToast(getString(R.string.name_required));
        }
    }

    @Override
    public void onRegionCreated() {
        initRegions();
    }

    @Override
    public void requestCurrentLocation() {
        if(isEditable){
            view.requestCurrentLocation();
        }
    }

    private DataRow getCustomer(String customerCode){
        return customerCode.equals("") || customerCode.equals("false")? null :
                models.companyCustomerModel.browse(" customer_code = ? ", new String[] {customerCode});
    }

    private boolean allowCustomer(DataRow scannedCustomer){
        return scannedCustomer == null || (customerRow != null && scannedCustomer.getString(Col.SERVER_ID).equals(this.customerRow.getString(Col.SERVER_ID)));
    }

    @Override
    public void onValidate(String name, String phoneNum, String code, String balanceLimit, String categoryId, String regionId, double latitude, double longitude, String geoHash, String address, String note) {
        DataRow scannedCustomer = getCustomer(code);
        if(allowCustomer(scannedCustomer)) {
            Values values = new Values();
            values.put("name", name);
            values.put("customer_code", code);
            values.put("balance_limit", balanceLimit);
            values.put("region_id", regionId);
            DataRow regionRow = models.regionModel.browse(regionId);
            values.put("state_id", regionRow.getString("state_id"));
            values.put("state", regionRow.getString("state_id"));
            values.put("country_id", regionRow.getString("country_id"));
            values.put("country", regionRow.getString("country"));
            values.put("picture_low", customerImage);
            values.put("latitude", latitude);
            values.put("longitude", longitude);
            values.put("geo_hash", geoHash);
            values.put("phone_num", phoneNum);
            values.put("address", address);
            values.put("note", note);
            values.put("category_id", categoryId);
            String id;
            if (customerRow == null) {
                values.put("company_create_date", MyUtil.getCurrentDate());
                values.put("creator_id", currentUserRow.getString(Col.SERVER_ID));
                id = models.companyCustomerModel.createCustomer(values);
            } else {
                id = models.companyCustomerModel.updateCustomer(customerRow.getString(Col.SERVER_ID), values);
            }
            if (id != null) {
                view.loadDetails(id);
            } else {
                view.showToast(getString(R.string.error_occurred));
            }
        }else{
            String msg = getString(R.string.scanned_customer_is_msg) + scannedCustomer.getString("name");
            view.showSimpleDialog(getString(R.string.customer_exists_title), msg);
        }
    }

    private static class Models{
        private final CompanyCustomerModel companyCustomerModel;
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final CustomerCategoryModel customerCategoryModel;
        public final RegionModel regionModel;

        private Models(Context context){
            this.companyCustomerModel = new CompanyCustomerModel(context);
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.customerCategoryModel = new CustomerCategoryModel(context);
            this.regionModel = new RegionModel(context);
        }
    }
}
