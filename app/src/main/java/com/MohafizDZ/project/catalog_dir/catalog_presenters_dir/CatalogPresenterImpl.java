package com.MohafizDZ.project.catalog_dir.catalog_presenters_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.models.Filters;
import com.MohafizDZ.project.catalog_dir.models.Models;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.catalog_dir.strategies_dir.ConcreteCatalogStrategy;
import com.MohafizDZ.project.catalog_dir.strategies_dir.ICatalogStrategy;
import com.MohafizDZ.project.models.TourConfigurationModel;

import java.util.ArrayList;
import java.util.List;

public class CatalogPresenterImpl implements ICatalogPresenter.Presenter{
    private static final String TAG = CatalogPresenterImpl.class.getSimpleName();

    private final ICatalogPresenter.View view;
    private final Context context;
    private final Models models;
    private final ICatalogStrategy catalogStrategy;
    private final List<ProductRow> rows;
    private Filters filters;
    private String searchFilter = "";
    private DataRow currentTourRow;
    private final DataRow currentUserRow;

    public CatalogPresenterImpl(ICatalogPresenter.View view, Context context, DataRow currentUserRow, ConcreteCatalogStrategy catalogStrategy) {
        this.view = view;
        this.context = context;
        this.models = new Models(context);
        catalogStrategy.setModels(models);
        this.catalogStrategy = catalogStrategy;
        this.currentUserRow = currentUserRow;
        rows = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        initData();
        view.toggleAddProduct(canEditProducts());
        view.toggleValidateDetailsContainer(catalogStrategy.canShowCustomerDetails());
        view.initAdapter(rows, !useUnlimitedStock() && catalogStrategy.canShowAvailability());
        view.setToolbarTitle(catalogStrategy.getTitle());
        catalogStrategy.onViewCreated();
    }

    private void initData(){
        DataRow distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        currentTourRow = models.tourModel.getCurrentTour(distributorRow);
    }

    private boolean useUnlimitedStock(){
        return currentTourRow != null && TourConfigurationModel.useUnlimitedStock(currentTourRow.getRelArray(models.tourModel, "configurations"));
    }

    private boolean canEditProducts(){
        return catalogStrategy.canEdit();
    }

    @Override
    public void setFilters(Filters filters) {
        this.filters = filters;
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
    public void onProductScan(String code) {
        String selection = " code = ? ";
        String[] args = {code};
        DataRow productRow = models.companyProductModel.browse(selection, args);
        if(productRow != null){
            view.setSearchFilter(productRow.getString("name"));
        }else{
            view.showToast(getString(R.string.product_not_found_msg));
        }
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    @Override
    public void onRefresh() {
        loadProducts();
        view.onLoadFinished(rows);
        refreshValidateButton();
    }

    private void refreshValidateButton(){
        view.toggleValidateContainer(catalogStrategy.canShowValidateButton());
    }
    @Override
    public void onItemClick(int position) {
        ProductRow row = rows.get(position);
        catalogStrategy.onItemClick(position, row);
        refreshValidateButton();
    }

    @Override
    public void refreshLine(int position) {
        view.onLineUpdated(position);
        refreshValidateButton();
    }

    @Override
    public void onItemLongClick(int position) {
        ProductRow row = rows.get(position);
        catalogStrategy.onItemLongClick(position, row);
        refreshValidateButton();
    }

    @Override
    public void onValidate() {
        catalogStrategy.onValidate();
    }

    @Override
    public void onCreateOptionsMenu() {
        view.toggleEmptyMenuItem(catalogStrategy.canEmpty());
    }

    @Override
    public void onEmptyClicked() {
        catalogStrategy.onEmptyClicked();
    }

    private void loadProducts(){
        rows.clear();
        Selection selection = prepareSelection();
        String sortBy = prepareSortBy();
        if(sortBy != null){
            sortBy += filters.reverse? " desc " : " asc ";
        }
        for(DataRow row : catalogStrategy.getProductRows(selection.selection, selection.args, sortBy)){
            ProductRow productRow = new ProductRow();
            productRow.putAll(row);
            rows.add(productRow);
        }
    }

    private String prepareSortBy() {
        switch (filters.orderBy){
            case price:
                return "price";
            case name:
                return "name";
            case category:
                return "category_name";
            case availability:
                return "stock_qty";
        }
        return null;
    }

    private Selection prepareSelection() {
        Selection selection = new Selection();
        if(!searchFilter.equals("")){
            selection.addSelection(" name like ? ", "%" + searchFilter + "%");
        }
        if(!filters.showAllCategories){
            List<String> categories = filters.getCategories();
            selection.addSelection(" category_id in (" + MyUtil.repeat("?, ", categories.size() - 1) + " ?)");
            selection.addArgs(categories);
        }
        if(filters.availableOnly){
            selection.addSelection("stock_qty > 0 ", null);
        }
        return selection;
    }

    private static class Selection{
        String selection = "";
        String[] args = {};

        private void addSelection(String selection, String arg){
            addArg(arg);
            addSelection(selection);
        }

        private void addSelection(String selection){
            this.selection = this.selection.length() > 0? this.selection + " and " + selection : selection;
        }

        private void addArg(String arg){
            if(arg != null){
                args = MyUtil.addArgs(args, arg);
            }
        }

        private void addArgs(List<String> args){
            for(String arg : args){
                addArg(arg);
            }
        }
    }
}
