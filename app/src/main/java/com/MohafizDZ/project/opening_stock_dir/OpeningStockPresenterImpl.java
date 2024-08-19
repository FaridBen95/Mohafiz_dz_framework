package com.MohafizDZ.project.opening_stock_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.catalog_dir.models.CartItemSingleton;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;
import com.MohafizDZ.project.catalog_dir.quantity_dialog_dir.IQtyDialogPresenter;
import com.MohafizDZ.project.catalog_dir.quantity_dialog_dir.QtyDialog;
import com.MohafizDZ.project.home_dir.guide_presenter_dir.Models;
import com.MohafizDZ.project.models.DistributorStockLineModel;
import com.MohafizDZ.project.models.DistributorStockModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpeningStockPresenterImpl implements IOpeningStockPresenter.Presenter{
    private static final String TAG = OpeningStockPresenterImpl.class.getSimpleName();

    private final IOpeningStockPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final List<ProductRow> rows;
    private DataRow stockRow;
    private final CartItemSingleton cartItemSingleton;
    private boolean isEditable = true;
    private DataRow tourRow;
    private String tourId;

    public OpeningStockPresenterImpl(IOpeningStockPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        this.rows = new ArrayList<>();
        this.cartItemSingleton = CartItemSingleton.getInstance();
    }

    @Override
    public void setTourId(String tourId) {
        this.tourId = tourId;
    }

    @Override
    public void onViewCreated() {
        view.setToolbarTitle(getString(R.string.initial_stock_title));
        initData();
        isEditable = stockRow.getString("state").equals(DistributorStockModel.STATE_DRAFT);
        view.initAdapter(rows);
        onRefresh();
    }

    private void initData(){
        DataRow distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        tourRow = tourId != null? models.tourModel.browse(tourId) : models.tourModel.getCurrentTour(distributorRow);
        this.stockRow = models.stockModel.getCurrentStock(tourRow);
    }

    @Override
    public void onRefresh() {
        if(!isEditable){
            prepareLines();
        }
        view.onLoadFinished(rows);
        view.toggleDeleteMenuItem(isEditable && cartItemSingleton.hasCartItems());
        view.toggleValidateButton(isEditable);
        view.toggleAddButton(isEditable);
        view.refreshValidateButtonBadge(cartItemSingleton.cartItems.size());
    }

    private void prepareLines() {
        rows.clear();
        String selection = " order_id = ? ";
        String[] args = {stockRow.getString(Col.SERVER_ID)};
        Map<String, DataRow> productsMap = models.productModel.getMap(Col.SERVER_ID);
        for(DataRow row : models.stockLineModel.getRows(selection, args)){
            String productId = row.getString("product_id");
            ProductRow productRow = new ProductRow();
            DataRow prodRow = productsMap.containsKey(productId)? productsMap.get(productId) : null;
            if(prodRow != null) {
                productRow.putAll(prodRow);
                productRow.setQty(row.getFloat("qty"));
                rows.add(productRow);
            }else{
                view.showToast(getString(R.string.error_occurred));
                view.goBack();
                return;
            }
        }
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
    public void onItemClick(int position) {
        if(!isEditable){
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
    public void onCreateOptionsMenu() {
        view.toggleDeleteMenuItem(isEditable && cartItemSingleton.hasCartItems());
    }

    @Override
    public void emptyList() {
        cartItemSingleton.clearItems();
        onListUpdated();
    }

    @Override
    public void onBackPressed() {
        onBackPressed(false);
    }

    @Override
    public void onBackPressed(boolean force) {
        if(force) {
            cartItemSingleton.clearItems();
            view.goBack();
        }else{
            if(isEditable) {
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
            for(ProductRow row : rows){
                Values values = new Values();
                values.put("name", row.getString("name"));
                values.put("product_id", row.getString(Col.SERVER_ID));
                values.put("order_id", stockRow.getString(Col.SERVER_ID));
                values.put("qty", row.getQty());
                //todo make a concept on how to change to initial type
                values.put("line_type", DistributorStockLineModel.LINE_TYPE_LOADING);
                if(models.stockLineModel.insert(values) <= 0){
                    return false;
                }
            }
            Values values = new Values();
            String validateDate = MyUtil.getCurrentDate();
            values.put("charging_date", validateDate);
            values.put("initial_stock_date", validateDate);
            values.put("validate_date", validateDate);
            values.put("state", DistributorStockModel.STATE_DONE);
            Values tourValues = new Values();
            tourValues.put("initial_stock_validated", 1);
            if(models.tourModel.update(tourRow.getString(Col.SERVER_ID), tourValues) <= 0){
                return false;
            }
            return models.stockModel.update(stockRow.getString(Col.SERVER_ID), values) > 0;
        });
        if(result){
            isEditable = false;
            onBackPressed(true);
        }else{
            view.showToast(getString(R.string.error_occurred));
        }
    }

    private String getString(int resId){
        return context.getString(resId);
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
}
