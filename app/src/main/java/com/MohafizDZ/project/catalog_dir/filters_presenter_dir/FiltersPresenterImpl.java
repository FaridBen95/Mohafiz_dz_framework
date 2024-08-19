package com.MohafizDZ.project.catalog_dir.filters_presenter_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.project.catalog_dir.models.Filters;
import com.MohafizDZ.project.models.CompanyProductCategoryModel;

import java.util.ArrayList;
import java.util.List;

public class FiltersPresenterImpl implements IFiltersPresenter.Presenter{
    private static final String TAG = FiltersPresenterImpl.class.getSimpleName();

    private final IFiltersPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private Filters filters;
    private final List<DataRow> categories;

    public FiltersPresenterImpl(IFiltersPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        categories = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        prepareCategoriesFilter();
        prepareDefaultFilters();
        onRefresh();
    }

    private void prepareCategoriesFilter(){
        categories.clear();
        categories.addAll(models.categoryModel.getRows());
        view.clearCategoriesFilter();
        for(int i = 0; i < categories.size(); i++){
            DataRow categoryRow = categories.get(i);
            view.createCategoryChip(categoryRow.getString("name"), i);
        }
    }

    private void prepareDefaultFilters(){
        filters = new Filters();
        view.setReverseChecked(filters.reverse);
        view.filterByAvailability(filters.availableOnly);
        view.filterByAllCategories(filters.showAllCategories);
        view.orderByAvailability(false);
        view.orderByName(false);
        view.orderByCategory(false);
        view.orderByPrice(false);
        switch (filters.orderBy){
            case availability:
                view.orderByAvailability(true);
                break;
            case name:
                view.orderByName(true);
                break;
            case category:
                view.orderByCategory(true);
                break;
            case price:
                view.orderByPrice(true);
                break;
        }
    }

    @Override
    public void onRefresh() {
        view.setFilters(filters);
    }

    @Override
    public void reverseSortBy(boolean checked) {
        filters.reverse = checked;
        onRefresh();
    }

    @Override
    public void filterAvailability(boolean checked) {
        filters.availableOnly = checked;
        onRefresh();
    }

    @Override
    public void filterByAllCategories(boolean checked) {
        filters.showAllCategories = checked;
        if(checked){
            unCheckCategories();
        }
        onRefresh();
    }

    private void unCheckCategories(){
        filters.clearCategories();
        prepareCategoriesFilter();
        view.filterByAllCategories(filters.showAllCategories);
    }

    @Override
    public void orderByName() {
        filters.orderBy = Filters.OrderBy.name;
        onRefresh();
    }

    @Override
    public void orderByAvailability() {
        filters.orderBy = Filters.OrderBy.availability;
        onRefresh();
    }

    @Override
    public void orderByPrice() {
        filters.orderBy = Filters.OrderBy.price;
        onRefresh();
    }

    @Override
    public void orderByCategory() {
        filters.orderBy = Filters.OrderBy.category;
        onRefresh();
    }

    @Override
    public void onCategoryClicked(int position) {
        DataRow category = categories.get(position);
        boolean showAllCategories = filters.showAllCategories;
        filters.addOrRemove(category.getString(Col.SERVER_ID));
        if(showAllCategories != filters.showAllCategories) {
            view.filterByAllCategories(filters.showAllCategories);
        }else{
            onRefresh();
        }
    }

    private static class Models{
        private final CompanyProductCategoryModel categoryModel;
        private Models(Context context){
            this.categoryModel = new CompanyProductCategoryModel(context);
        }
    }


}
