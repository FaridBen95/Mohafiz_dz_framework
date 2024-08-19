package com.MohafizDZ.project.inventory_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.models.CartItem;
import com.MohafizDZ.project.catalog_dir.models.CartItemSingleton;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.catalog_dir.quantity_dialog_dir.IQtyDialogPresenter;
import com.MohafizDZ.project.catalog_dir.quantity_dialog_dir.QtyDialog;
import com.MohafizDZ.project.models.CompanyProductModel;
import com.MohafizDZ.project.models.DistributorInventoryLineModel;
import com.MohafizDZ.project.models.DistributorInventoryModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.DistributorStockModel;
import com.MohafizDZ.project.models.TourConfigurationModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.opening_stock_dir.strategies_dir.ProductSelectionStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryPresenterImpl implements IInventoryPresenter.Presenter{
    private static final String TAG = InventoryPresenterImpl.class.getSimpleName();

    private final IInventoryPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final List<ProductRow> rows;
    //this field will save lines before opening the catalog
    private final Map<String, CartItem> cartItemBackup;
    private final CartItemSingleton cartItemSingleton;
    private DataRow tourRow, inventoryRow;
    private final List<String> configurations;
    private boolean allowEdit, isEditable;
    private String tourId;


    public InventoryPresenterImpl(IInventoryPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        this.rows = new ArrayList<>();
        this.configurations = new ArrayList<>();
        this.cartItemSingleton = CartItemSingleton.getInstance();
        this.cartItemBackup = new HashMap<>();
    }

    private String getString(int resId){
        return context.getString(resId);
    }
    @Override
    public void onViewCreated() {
        view.setToolbarTitle(getString(R.string.inventory_label));
        initData();
        allowEdit = inventoryRow.getString("state").equals(DistributorInventoryModel.STATE_DRAFT);
        view.initAdapter(rows, !allowEdit);
        onRefresh();
    }

    private void initData(){
        DataRow distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        tourRow = tourId != null? models.tourModel.browse(tourId) : models.tourModel.getCurrentTour(distributorRow);
        inventoryRow = models.inventoryModel.getCurrentInventory(tourRow);
        configurations.clear();
        configurations.addAll(tourRow.getRelArray(models.tourModel, "configurations"));
        configurations.addAll(distributorRow.getRelArray(models.distributorModel, "configurations"));
    }

    private boolean showTheoInventory(){
        return TourConfigurationModel.showTheoInventory(configurations);
    }

    @Override
    public void onListUpdated() {
        rows.clear();
        Map<String, DataRow> productsMap = models.productModel.getMap(Col.SERVER_ID);
        for(String productId : cartItemSingleton.cartItems.keySet()){
            DataRow row = productsMap.get(productId);
            if(row != null) {
                ProductRow productRow = new ProductRow();
                productRow.putAll(row);
                rows.add(productRow);
            }
        }
        onRefresh();
    }

    @Override
    public void onRefresh() {
        String state = inventoryRow.getString("state");
        if(state.equals(DistributorInventoryModel.STATE_DONE)){
            initValidatedInventoryLines();
        }else {
            if (canInitLines()) {
                initInventoryLines();
            }
        }
        view.onLoadFinished(rows);
        view.toggleDeleteMenuItem(isEditable && allowEdit && cartItemSingleton.hasCartItems());
        view.toggleValidateButton(isEditable && allowEdit);
        view.toggleAddButton(isEditable && allowEdit);
        view.refreshValidateButtonBadge(cartItemSingleton.cartItems.size());
    }

    private boolean canInitLines(){
        return !allowEdit || (!isEditable && showTheoInventory());
    }

    private void initInventoryLines(){
        String selection = " inventory_id = ? ";
        String[] args = {inventoryRow.getString(Col.SERVER_ID)};
        rows.clear();
        Map<String, DataRow> productsMap = models.productModel.getMap(Col.SERVER_ID);
        for(DataRow row : models.inventoryLineModel.getRows(selection, args)){
            String productId = row.getString("product_id");
            DataRow prodRow = productsMap.get(productId);
            if(prodRow != null){
                ProductRow productRow = new ProductRow();
                productRow.addAll(prodRow);
                productRow.setQty(row.getFloat("qty"));
                productRow.put("theo_qty", row.getFloat("theo_qty"));
                rows.add(productRow);
            }
        }
    }

    private void initValidatedInventoryLines(){
        String selection = " stock_qty <> 0 or inventory_qty <> 0";
        rows.clear();
        String tourId = tourRow != null? tourRow.getString(Col.SERVER_ID) : null;
        List<DataRow> inventoryLines = models.productModel.getProducts(tourId, selection, null, null);
        for(DataRow row : inventoryLines){
            ProductRow productRow = new ProductRow();
            productRow.addAll(row);
            productRow.setQty(row.getFloat("inventory_qty"));
            productRow.put("theo_qty", row.getFloat("stock_qty"));
            rows.add(productRow);
        }
    }

    @Override
    public void onBackPressed(boolean force) {
        if(force || !isEditable) {
            cartItemSingleton.clearItems();
            view.goBack();
        }else{
            if(allowEdit) {
                view.showIgnoreChangesDialog();
            }else{
                cartItemSingleton.clearItems();
                view.goBack();
            }
        }
    }

    @Override
    public void onValidate() {
        boolean result = Model.startTransaction(context, () -> {
            String inventoryId = inventoryRow.getString(Col.SERVER_ID);
            Map<String, DataRow> availabilityMap = getAvailability();
            for(ProductRow row : rows){
                Values values = new Values();
                values.put("distributor_id", inventoryRow.getString("distributor_id"));
                values.put("tour_id", inventoryRow.getString("tour_id"));
                values.put("name", row.getString("name"));
                String productId = row.getString(Col.SERVER_ID);
                values.put("product_id", productId);
                values.put("inventory_id", inventoryId);
                values.put("qty", row.getQty());
                float theoQty = availabilityMap.containsKey(productId)? availabilityMap.get(productId).getFloat("stock_qty") : 0.0f;
                values.put("theo_qty", theoQty);
                //todo when implementing steps i should reconsider about the line state
                values.put("state", DistributorInventoryLineModel.STATE_DONE);
                if(models.inventoryLineModel.insert(values) <= 0){
                    return false;
                }
            }
            Values values = new Values();
            String validateDate = MyUtil.getCurrentDate();
            values.put("confirm_date", validateDate);
            values.put("validate_date", validateDate);
            //todo when implementing steps i should reconsider about the line state
            values.put("state", DistributorInventoryModel.STATE_DONE);
            Values tourValues = new Values();
            tourValues.put("inventory_validated", 1);
            if(models.tourModel.update(tourRow.getString(Col.SERVER_ID), tourValues) <= 0){
                return false;
            }
            return models.inventoryModel.update(inventoryId, values) > 0;
        });
        if(result){
            allowEdit = false;
            onBackPressed(true);
        }else{
            view.showToast(getString(R.string.error_occurred));
        }
    }

    @Override
    public void onCreateOptionsMenu() {
        view.toggleDeleteMenuItem(isEditable && allowEdit && cartItemSingleton.hasCartItems());
        view.toggleInitMenuItem(isEditable && allowEdit && showTheoInventory());
    }

    @Override
    public void initLines() {
        Map<String, DataRow> availabilityMap = getAvailability();
        rows.clear();
        for(DataRow row : availabilityMap.values()){
            float qty = row.getFloat("stock_qty");
            ProductRow productRow = new ProductRow();
            productRow.addAll(row);
            productRow.setQty(qty);
            rows.add(productRow);
        }
        onListUpdated();
    }

    private Map<String, DataRow> getAvailability(){
        String selection = " stock_qty > 0 ";
        String[] args = {};
        return models.productModel.
                getProductsMap(tourRow.getString(Col.SERVER_ID),
                        selection, args, null);
    }

    @Override
    public void emptyList() {
        cartItemSingleton.clearItems();
        onListUpdated();
    }

    @Override
    public void onItemClick(int position) {
        if(!allowEdit || !isEditable){
            return;
        }
        ProductRow productRow = rows.get(position);
        float lastQty = productRow.getQty();
        view.showQtyDialog(new QtyDialog(new IQtyDialogPresenter.DialogListener() {
            @Override
            public void onPositiveClicked(ProductRow productRow, float qty) {
                productRow.setQty(qty);
                refreshLine(position);
            }

            @Override
            public void onNeutralClicked(ProductRow productRow, float qty) {
                productRow.setQty(0);
                refreshLine(position);
            }

            @Override
            public void onNegativeClicked(ProductRow productRow, float qty) {
                productRow.setQty(lastQty);
            }
        }, productRow, lastQty));
    }

    @Override
    public void requestAddLine() {
        cartItemBackup.clear();
        for(ProductRow row : rows){
            String productId = row.getString(Col.SERVER_ID);
            cartItemBackup.put(productId, new CartItem(productId, row.getString("name"), 0, row.getFloat("price")).setQty(row.getQty()));
        }
        view.startCatalogActivity(ProductSelectionStrategy.class.getName());
    }

    @Override
    public void onCatalogCanceled() {
        cartItemSingleton.clearItems();
        cartItemSingleton.addCartItems(cartItemBackup);
    }

    @Override
    public void setTourId(String tourId) {
        this.tourId = tourId;
    }

    @Override
    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    private void refreshLine(int position){
        ProductRow productRow = rows.get(position);
        if(productRow.getQty() == 0.0f){
            rows.remove(position);
            view.onLineDeleted(position);
        }else {
            view.onLineUpdated(position);
        }
        view.refreshValidateButtonBadge(cartItemSingleton.cartItems.size());
    }


    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final DistributorStockModel stockModel;
        private final DistributorInventoryModel inventoryModel;
        private final DistributorInventoryLineModel inventoryLineModel;
        private final CompanyProductModel productModel;

        private Models(Context context){
            tourModel = new TourModel(context);
            distributorModel = new DistributorModel(context);
            stockModel = new DistributorStockModel(context);
            inventoryModel = new DistributorInventoryModel(context);
            inventoryLineModel = new DistributorInventoryLineModel(context);
            productModel = new CompanyProductModel(context);
        }

    }
}
