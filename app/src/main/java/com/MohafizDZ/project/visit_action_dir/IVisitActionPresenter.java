package com.MohafizDZ.project.visit_action_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.project.home_dir.visit_presenter_dir.IVisitPresenter;

import java.util.LinkedHashMap;
import java.util.List;

public interface IVisitActionPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void setEditable(boolean isEditable);

        void setCustomerId(String customerId);

        void setAction(String action);

        void requestAddImage(Integer position, String imagePath);

        void onSelectCategory(String key);

        void createCategory(String name);

        void requestModifyImage(int position);

        void deleteImage(int position);

        void onValidate(String categoryId, String note);
    }

    interface View extends BasePresenter.View{
        void setCustomerName(String txt);
        void setTourName(String txt);
        void setActionType(String txt);
        void setDistance(String txt);
        void setDate(String txt);
        void setNote(String txt);
        void goBack();

        void initAdapter(List<String> attachments);

        void onLoadFinished(List<String> attachments);

        void toggleAttachmentsContainer(boolean visible);

        void toggleAddImageButton(boolean visible);
        void toggleValidateButton(boolean visible);

        void toggleNoteInput(boolean visible);
        void toggleNoteTextView(boolean visible);

        void toggleNoActionCategory(boolean visible);

        void initCategoriesFilter(LinkedHashMap<String, String> categories);

        void requestCreateCustomerCategory();

        void setCategory(String category);

        void showModifyImageDialog(int position);

        void requestCurrentLocation(LocationListener locationListener);

        void loadDetails(String actionId);

        void setCategoryEnabled(boolean enabled);
    }

    interface LocationListener{
        void onLocationChanged(double latitude, double longitude);

        void onStart();

        void onFailed();
    }

}
