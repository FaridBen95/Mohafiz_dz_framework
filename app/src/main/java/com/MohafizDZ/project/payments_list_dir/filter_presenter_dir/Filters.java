package com.MohafizDZ.project.payments_list_dir.filter_presenter_dir;

public class Filters {
    public boolean isPayments;
    public boolean isRefund;
    public boolean isOrder;
    public boolean isFreePayments;
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

    public Filters(boolean isPayments, boolean isRefund, boolean isFreePaymentsOnly) {
        this.isPayments = isPayments;
        this.isRefund = isRefund;
        this.isFreePayments = isFreePaymentsOnly;
    }

    public enum OrderBy{
        reference, date, customer, amount
    }
}
