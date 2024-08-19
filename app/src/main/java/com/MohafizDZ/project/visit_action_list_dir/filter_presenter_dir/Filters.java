package com.MohafizDZ.project.visit_action_list_dir.filter_presenter_dir;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Filters {
    public String selectedAction;
    public OrderBy orderBy = OrderBy.date;
    public boolean reverse;
    public Long dateStart = null;
    public Long dateEnd = null;
    public String customerId;
    public String customerName;
    public String regionId;
    public String regionName;
    public String tourId;
    public String tourName;
    public Float distance;
    public boolean showAllActions = true;
    private List<String> actions = new ArrayList<>();
    public Filters(String selectedAction) {
        this.selectedAction = selectedAction;
    }


    public boolean add(String s) {
        return actions.add(s);
    }

    public boolean remove(@Nullable Object o) {
        return actions.remove(o);
    }

    public List<String> getActions() {
        return actions;
    }

    public void clearActions() {
        actions.clear();
    }

    public void addOrRemove(String categoryId) {
        if(actions.contains(categoryId)){
            actions.remove(categoryId);
        }else{
            actions.add(categoryId);
        }
        if(actions.size() > 0){
            showAllActions = false;
        }else{
            showAllActions = true;
        }
    }
    public enum OrderBy{
        date, customer, distance
    }
}
