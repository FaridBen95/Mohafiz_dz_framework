package com.MohafizDZ.project.catalog_dir.models;

import androidx.annotation.Nullable;


import java.util.ArrayList;
import java.util.List;

public class Filters {
    public boolean reverse = false;
    public boolean availableOnly = false;
    public boolean showAllCategories = true;
    private List<String> categories = new ArrayList<>();
    public OrderBy orderBy = OrderBy.name;

    public boolean add(String s) {
        return categories.add(s);
    }

    public boolean remove(@Nullable Object o) {
        return categories.remove(o);
    }

    public List<String> getCategories() {
        return categories;
    }

    public void clearCategories() {
        categories.clear();
    }

    public void addOrRemove(String categoryId) {
        if(categories.contains(categoryId)){
            categories.remove(categoryId);
        }else{
            categories.add(categoryId);
        }
        if(categories.size() > 0){
            showAllCategories = false;
        }else{
            showAllCategories = true;
        }
    }

    public enum OrderBy{
        name, availability, price, category
    }
}
