package com.MohafizDZ.project.catalog_dir.cart_order_dir;

import android.content.Context;

import com.MohafizDZ.project.catalog_dir.cart_order_dir.strategies_dir.ConcreteCartOrderStrategy;
import com.MohafizDZ.project.catalog_dir.cart_order_dir.strategies_dir.ICartOrderStrategy;
import com.MohafizDZ.project.catalog_dir.models.Models;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;

import java.util.ArrayList;
import java.util.List;

public class CartOrderPresenterImpl implements ICartOrderPresenter.Presenter {
    private static final String TAG = CartOrderPresenterImpl.class.getSimpleName();

    private final ICartOrderPresenter.View view;
    private final Context context;
    private final ICartOrderStrategy cartOrderStrategy;
    private final Models models;
    private final List<ProductRow> rows;

    public CartOrderPresenterImpl(ICartOrderPresenter.View view, Context context, ConcreteCartOrderStrategy cartOrderStrategy) {
        this.view = view;
        this.context = context;
        this.models = new Models(context);
        cartOrderStrategy.setModels(models);
        this.cartOrderStrategy = cartOrderStrategy;
        this.rows = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        view.initAdapter(rows);
        view.setToolbarTitle(cartOrderStrategy.getTitle());
        cartOrderStrategy.onViewCreated();
        onRefresh();
    }

    @Override
    public void onRefresh() {
        loadProducts();
        view.onLoadFinished(rows);
        refreshValidateButton();
    }

    private void refreshValidateButton() {
        view.toggleValidateContainer(cartOrderStrategy.canShowValidateButton());
    }

    private void loadProducts(){
        rows.clear();
        rows.addAll(cartOrderStrategy.setProductRows());
    }


    @Override
    public void onValidate() {
        cartOrderStrategy.onValidate();
    }
}
