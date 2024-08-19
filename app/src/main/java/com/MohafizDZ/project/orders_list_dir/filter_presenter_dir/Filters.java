package com.MohafizDZ.project.orders_list_dir.filter_presenter_dir;

public class Filters {
    public boolean isSales;
    public boolean isBackOrders;
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
    public String productId;
    public String productName;

    public Filters(boolean isSales, boolean isBackOrders) {
        this.isSales = isSales;
        this.isBackOrders = isBackOrders;
    }

    public enum OrderBy{
        reference, date, customer, amount
    }
}
