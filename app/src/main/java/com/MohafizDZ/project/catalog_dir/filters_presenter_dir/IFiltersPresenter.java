package com.MohafizDZ.project.catalog_dir.filters_presenter_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.project.catalog_dir.models.Filters;

public interface IFiltersPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void reverseSortBy(boolean checked);

        void filterAvailability(boolean checked);
        void filterByAllCategories(boolean checked);

        void orderByName();
        void orderByAvailability();
        void orderByPrice();
        void orderByCategory();

        void onCategoryClicked(int position);
    }

    interface View extends BasePresenter.View{

        void setReverseChecked(boolean checked);
        void filterByAvailability(boolean checked);
        void filterByAllCategories(boolean checked);
        void orderByAvailability(boolean checked);
        void orderByCategory(boolean checked);
        void orderByName(boolean checked);
        void orderByPrice(boolean checked);

        void clearCategoriesFilter();

        void createCategoryChip(String name, int position);

        void setFilters(Filters filters);
    }
}
