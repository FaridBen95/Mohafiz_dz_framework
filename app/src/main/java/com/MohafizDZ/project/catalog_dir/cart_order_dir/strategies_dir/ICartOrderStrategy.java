package com.MohafizDZ.project.catalog_dir.cart_order_dir.strategies_dir;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;

import java.util.List;

public interface ICartOrderStrategy {
    List<ProductRow> setProductRows();

    boolean canShowValidateButton();

    void onViewCreated();

    void onValidate();

    String getTitle();
}
