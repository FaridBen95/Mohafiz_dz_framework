package com.MohafizDZ.project.product_form_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyProductCategoryModel;
import com.MohafizDZ.project.models.CompanyProductModel;
import com.MohafizDZ.project.models.DistributorConfigurationModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.TourModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ProductPresenterImpl implements IProductPresenter.Presenter{
    private static final String TAG = ProductPresenterImpl.class.getSimpleName();

    private final IProductPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final String productId;
    private final List<String> configurations;
    private DataRow productRow;
    private String productImage;
    private boolean isEditable;
    private DataRow distributorRow, tourRow;

    public ProductPresenterImpl(IProductPresenter.View view, Context context, DataRow currentUserRow, String productId) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        this.productId = productId;
        configurations = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        initData();
        initCategories();
        String validateTitle;
        if(productRow != null){
            view.setName(productRow.getString("name"));
            view.setPrice(productRow.getString("price"));
            view.setCodeText(productRow.getString("code"));
            view.setCategory(getCategory());
            view.setNote(productRow.getString("description"));
            productImage = productRow.getString("picture_low");
            view.setImage(productImage);
            validateTitle = getString(R.string.update);
        }else{
            validateTitle = getString(R.string.create_label);
        }
        view.setValidateTitle(validateTitle);
        onRefresh();
        if(!canEditPrice() && productRow == null){
            view.showToast(getString(R.string.cant_create_product_price_msg));
            view.goBack();
        }
    }

    private String getCategory(){
        try {
            return models.companyProductCategoryModel.browse(productRow.getString("category_id")).getString("name");
        }catch (Exception ignored){}
        return "";
    }

    private void initData(){
        if(productId != null){
            this.productRow = models.companyProductModel.browse(productId);
        }
        distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        tourRow = models.tourModel.getCurrentTour(distributorRow);
        configurations.clear();
        configurations.addAll(distributorRow.getRelArray(models.distributorModel, "configurations"));
    }

    private void initCategories(){
        view.initCategoriesFilter(getCategories());
    }
    
    private String getString(int resId){
        return context.getString(resId);
    }
    
    private LinkedHashMap<String, String> getCategories(){
        List<DataRow> categories = models.companyProductCategoryModel.getRows();
        if(canEditProductCategories()) {
            categories.add(createRow());
        }
        return getNamesFromRows(categories);
    }
    private DataRow createRow(){
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

    @Override
    public void onRefresh() {
        boolean isEditable = isEditable();
        view.setEditable(isEditable);
        view.toggleValidateButton(isEditable);
        view.toggleEditItem(!isEditable && canEditProducts());
        if(isEditable){
            view.enablePrice(canEditPrice());
        }
    }

    public boolean canEditProductCategories() {
        String state = tourRow != null? tourRow.getString("state") : TourModel.STATE_DRAFT;
        return allowEditProductCategories() && (state.equals(TourModel.STATE_DRAFT) || state.equals(TourModel.STATE_CONFIRMED));
    }

    private boolean allowEditProductCategories(){
        return DistributorConfigurationModel.canEditProductCategories(configurations);
    }

    public boolean canEditProducts() {
        String state = tourRow != null? tourRow.getString("state") : TourModel.STATE_DRAFT;
        return allowEditProducts() && (state.equals(TourModel.STATE_DRAFT) || state.equals(TourModel.STATE_CONFIRMED));
    }

    private boolean allowEditProducts(){
        return DistributorConfigurationModel.canEditProducts(configurations);
    }

    private boolean canEditPrice(){
        return DistributorConfigurationModel.canSetPrice(configurations);
    }

    private boolean isEditable(){
        return productRow == null || isEditable;
    }

    @Override
    public void setEditable(boolean editable) {
        this.isEditable = editable;
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
    public void requestUpdateImageView(String base64) {
        productImage = base64;
        view.setImage(base64);
    }

    @Override
    public void onCreateOptionsMenu() {
        view.toggleEditItem(!isEditable() && canEditProducts());
    }

    @Override
    public void onSelectCategory(String categoryKey) {
        if(categoryKey.equals("-2")){
            view.requestCreateCustomerCategory();
        }
    }

    @Override
    public void createCategory(String name) {
        if(!name.equals("")) {
            Values values = new Values();
            values.put("name", name);
            values.put("creator_id", currentUserRow.getString(Col.SERVER_ID));
            models.companyProductCategoryModel.insert(values);
            initCategories();
        }else{
            view.showToast(getString(R.string.name_required));
        }
    }

    @Override
    public void onCodeScan(String productCode) {
        DataRow productRow = getProduct(productCode);
        if(allowedProduct(productRow)){
            view.setCodeText(productCode);
        }else{
            String msg = getString(R.string.scanned_product_is_msg) + productRow.getString("name");
            view.showSimpleDialog(getString(R.string.product_exists_title), msg);
        }
    }

    private boolean allowedProduct(DataRow scannedProduct){
        return scannedProduct == null || (productRow != null && scannedProduct.getString(Col.SERVER_ID).equals(this.productRow.getString(Col.SERVER_ID)));
    }

    private DataRow getProduct(String productCode){
        return models.companyProductModel.browse(" code = ? ", new String[] {productCode});
    }

    @Override
    public void onValidate(String name, String price, String code, String categoryId, String description) {
        DataRow scannedProduct = getProduct(code);
        if(allowedProduct(scannedProduct)){
            Values values = new Values();
            values.put("name", name);
            values.put("price", price);
            values.put("code", code);
            values.put("picture_low", productImage);
            values.put("description", description);
            values.put("category_id", categoryId);
            String id;
            if (productRow == null) {
                values.put("company_create_date", MyUtil.getCurrentDate());
                values.put("creator_id", currentUserRow.getString(Col.SERVER_ID));
                id = models.companyProductModel.createProduct(values);
            } else {
                id = models.companyProductModel.updateProduct(productRow.getString(Col.SERVER_ID), values);
            }
            if (id != null) {
                view.restartActivity(id, false);
            } else {
                view.showToast(getString(R.string.error_occurred));
            }
        }else{
            String msg = getString(R.string.scanned_product_is_msg) + productRow.getString("name");
            view.showSimpleDialog(getString(R.string.product_exists_title), msg);
        }
    }

    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final CompanyProductModel companyProductModel;
        private final CompanyProductCategoryModel companyProductCategoryModel;

        private Models(Context context){
            this.distributorModel = new DistributorModel(context);
            this.tourModel = new TourModel(context);
            this.companyProductModel = new CompanyProductModel(context);
            this.companyProductCategoryModel = new CompanyProductCategoryModel(context);
        }
    }
}
