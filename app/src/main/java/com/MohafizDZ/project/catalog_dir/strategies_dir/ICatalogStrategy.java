package com.MohafizDZ.project.catalog_dir.strategies_dir;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.catalog_dir.models.ProductRow;

import java.util.List;

public interface ICatalogStrategy {
    boolean canEdit();
    boolean canShowValidateButton();
    List<DataRow> getProductRows(String selection, String[] args, String sortBy);
    void onItemClick(int position, ProductRow productRow);
    void onItemLongClick(int position, ProductRow productRow);

    void onValidate();

    boolean canEmpty();

    void onEmptyClicked();

    void onViewCreated();

    String getTitle();

    boolean canShowAvailability();

    boolean canShowCustomerDetails();
}
