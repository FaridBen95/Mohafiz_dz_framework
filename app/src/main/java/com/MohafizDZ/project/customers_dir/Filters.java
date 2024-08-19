package com.MohafizDZ.project.customers_dir;

import java.io.Serializable;

public class Filters implements Serializable {
    public String customerId;
    public OrderBy orderBy = OrderBy.name;
    public boolean reverse;
    public String categoryId;
    public String categoryName;
    public Float proximity;
    public VisitState visitState;
    public Float balanceStart;
    public Float balanceEnd;
    public boolean hasBalanceLimit;
    public String regionId;
    public String regionName;
    public String tourId;
    public String tourName;
    public Long visitDateStart;
    public Long visitDateEnd;
    public Double currentLatitude, currentLongitude;
    public boolean plannedCustomers;

    public Filters(String tourId) {
        this.tourId = tourId;
    }


    public enum OrderBy{
        name, visitDate, proximity
    }

    public enum VisitState{
        visited, not_visited
    }
}
