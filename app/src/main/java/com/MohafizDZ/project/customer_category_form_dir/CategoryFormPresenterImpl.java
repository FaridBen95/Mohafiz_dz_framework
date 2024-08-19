package com.MohafizDZ.project.customer_category_form_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.models.CustomerCategoryModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.TourModel;

public class CategoryFormPresenterImpl implements ICategoryFormPresenter.Presenter{
    private static final String TAG = CategoryFormPresenterImpl.class.getSimpleName();

    private final ICategoryFormPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;

    public CategoryFormPresenterImpl(ICategoryFormPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
    }

    @Override
    public void onViewCreated() {

    }

    @Override
    public void onRefresh() {

    }

    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final CustomerCategoryModel customerCategoryModel;

        private Models(Context context){
            tourModel = new TourModel(context);
            distributorModel = new DistributorModel(context);
            customerCategoryModel = new CustomerCategoryModel(context);
        }
    }
}
