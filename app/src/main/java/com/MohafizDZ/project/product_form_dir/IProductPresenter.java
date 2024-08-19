package com.MohafizDZ.project.product_form_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

import java.util.LinkedHashMap;

public interface IProductPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void setEditable(boolean editable);

        void onBackPressed();

        void requestUpdateImageView(String base64);

        void onCreateOptionsMenu();

        void onSelectCategory(String key);

        void createCategory(String name);

        void onValidate(String name, String price, String code, String categoryId, String description);

        void onCodeScan(String productCode);
    }

    interface View extends BasePresenter.View{

        void toggleEditItem(boolean visible);

        void requestCreateCustomerCategory();

        void setEditable(boolean isEditable);

        void toggleValidateButton(boolean visible);

        void setValidateTitle(String title);

        void initCategoriesFilter(LinkedHashMap<String, String> categories);

        void setName(String text);

        void setPrice(String text);

        void setImage(String text);
        void setCodeText(String text);
        void setCategory(String text);
        void setNote(String text);

        void showIgnoreChangesDialog();

        void goBack();

        void restartActivity(String id, boolean editable);

        void enablePrice(boolean enabled);
    }
}
