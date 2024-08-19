package com.MohafizDZ.project.order_details_dir;

import static com.MohafizDZ.project.models.VisitOrderModel.DELETE_ORDER_DELAY;

import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.models.CompanyProductModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.PaymentModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.TourVisitActionModel;
import com.MohafizDZ.project.models.UserModel;
import com.MohafizDZ.project.models.VisitOrderLineModel;
import com.MohafizDZ.project.models.VisitOrderModel;
import com.MohafizDZ.project.order_details_dir.strategies.CancelOrderPaymentStrategy;
import com.MohafizDZ.project.order_details_dir.strategies.DeleteOrderPaymentStrategy;
import com.MohafizDZ.project.payment_dir.IPaymentPresenter;
import com.MohafizDZ.project.payment_dir.strategies.ConcretePaymentStrategy;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailsPresenterImpl implements IOrderDetailsPresenter.Presenter{
    private static final String TAG = OrderDetailsPresenterImpl.class.getSimpleName();

    private final IOrderDetailsPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final String orderId;
    private final List<DataRow> lines;
    private DataRow currentDistributorRow, currentTourRow, customerRow, orderRow, orderTour, orderDistributor, sellerRow, actionDetailsRow;
    private String currencyCode;

    public OrderDetailsPresenterImpl(IOrderDetailsPresenter.View view, Context context, DataRow currentUserRow, String orderId) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        this.orderId = orderId;
        lines = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        if(initData()){
            view.setOrderName(orderRow.getString("name"));
            view.setCustomerName(customerRow.getString("name"));
            view.setDistanceToCustomer(calculateDistance());
            String tourName = orderTour != null? orderTour.getString("name") : "-";
            view.setTourName(tourName);
            String sellerName = sellerRow != null? sellerRow.getString("name") : "-";
            view.setSellerName(sellerName);
            view.setDate(orderRow.getString("done_date"));
            view.setOrderAmount(getPrice(orderRow.getFloat("total_amount")));
            view.setPaymentAmount(getPrice(orderRow.getFloat("payment_amount")));
            view.setRemainingAmount(getPrice(orderRow.getFloat("remaining_amount")));
            view.initAdapter(lines);
            onRefresh();
        }else{
            view.showToast(getString(R.string.error_occurred));
            view.goBack();
        }
    }

    private String getPrice(float price){
        return price + " " + currencyCode;
    }

    private String calculateDistance(){
        if(actionDetailsRow != null) {
            float distance = actionDetailsRow.getFloat("distance_from_customer");
            return Math.round(distance) + " " + getString(R.string.meters_label);
        }else{
            return "-";
        }
    }

    private String getString(int resId){
        return context.getString(resId);
    }

    private boolean initData(){
        orderRow = models.visitOrderModel.browse(orderId);
        if(orderRow != null){
            currentDistributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
            currentTourRow = models.tourModel.getCurrentTour(currentDistributorRow);
            customerRow = models.companyCustomerModel.browse(orderRow.getString("customer_id"));
            orderDistributor = models.distributorModel.browse(orderRow.getString("distributor_id"));
            sellerRow = orderDistributor != null? models.userModel.browse(orderDistributor.getString("user_id")) : null;
            orderTour = models.tourModel.browse(orderRow.getString("tour_id"));
            actionDetailsRow = models.tourVisitActionModel.browse(orderRow.getString("action_details_id"));
            currencyCode = CompanyModel.getCompanyCurrency(context);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onRefresh() {
        loadLines();
        view.onLoadFinished(lines);
    }

    private void loadLines(){
        lines.clear();
        String selection = " order_id = ? ";
        String[] args = {orderId + ""};
        List<DataRow> orderLines = models.visitOrderLineModel.select(selection, args);
        for(DataRow row : orderLines){
            DataRow productRow = models.companyProductModel.browse(row.getString("product_id"));
            if(productRow != null){
                row.put("name", productRow.getString("name"));
                row.put("picture_low", productRow.getString("picture_low"));
                lines.add(row);
            }else{
                view.showToast(getString(R.string.error_occurred));
                view.goBack();
                break;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu() {
        view.toggleDeleteMenuItem(isCancelable());
    }

    private boolean isDeletable(){
        String validatedDate = orderRow.getString("done_date");
        final String dateBeforeMins = MyUtil.getDateBeforeMins(DELETE_ORDER_DELAY);
        boolean deletable = MyUtil.getDateFromStringDate(validatedDate).getTime() > MyUtil.getDateFromStringDate(dateBeforeMins).getTime();
        return isCancelable() && deletable;
    }

    private boolean isCancelable(){
        return customerRow != null && currentTourRow.getString(Col.SERVER_ID).equals(orderRow.getString("tour_id")) &&
                orderRow.getFloat("total_amount") > 0;
    }

    @Override
    public void requestCancelOrder() {
        if(isCancelable()) {
            if (isDeletable()) {
                view.showCancelDialog(getString(R.string.delete_label), getString(R.string.delete_order_msg), getString(R.string.delete_label));
            } else {
                view.showCancelDialog(getString(R.string.cancel_label), getString(R.string.cancel_order_msg), getString(R.string.cancel_label));
            }
        }else{
            view.showToast(getString(R.string.cant_cancel_msg));
        }
    }

    @Override
    public void cancelOrder(IPaymentPresenter.ValidateView validateView) {
        if(isDeletable()) {
            DeleteOrderPaymentStrategy deleteOrderPaymentStrategy = new DeleteOrderPaymentStrategy(context, validateView, currentUserRow, orderRow);
            final ConcretePaymentStrategy.ValidateHelper validateHelper =
                    ConcretePaymentStrategy.ValidateHelper.getInstance(deleteOrderPaymentStrategy,
                            new com.MohafizDZ.project.payment_dir.models.Models(context), customerRow.getString(Col.SERVER_ID));
            validateView.requestCurrentLocation(new IPaymentPresenter.LocationListener() {
                @Override
                public void onLocationChanged(double latitude, double longitude) {
                    view.toggleLoading(false);
                    validateHelper.validate(latitude, longitude);
                }

                @Override
                public void onStart() {
                    view.toggleLoading(true);
                }

                @Override
                public void onFailed() {
                    view.toggleLoading(false);
                }
            });
        }else{
            //todo implement cancel order
            //todo if i'll sync then i should recheck about the deletable condition
            CancelOrderPaymentStrategy deleteOrderPaymentStrategy = new CancelOrderPaymentStrategy(context, validateView, currentUserRow,
                    orderRow, lines);
            final ConcretePaymentStrategy.ValidateHelper validateHelper =
                    ConcretePaymentStrategy.ValidateHelper.getInstance(deleteOrderPaymentStrategy,
                            new com.MohafizDZ.project.payment_dir.models.Models(context), customerRow.getString(Col.SERVER_ID));
            validateView.requestCurrentLocation(new IPaymentPresenter.LocationListener() {
                @Override
                public void onLocationChanged(double latitude, double longitude) {
                    view.toggleLoading(false);
                    validateHelper.validate(latitude, longitude);
                }

                @Override
                public void onStart() {
                    view.toggleLoading(true);
                }

                @Override
                public void onFailed() {
                    view.toggleLoading(false);
                }
            });
        }
    }

    private static class Models{
        private final UserModel userModel;
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final VisitOrderModel visitOrderModel;
        private final VisitOrderLineModel visitOrderLineModel;
        private final PaymentModel paymentModel;
        private final CompanyCustomerModel companyCustomerModel;
        private final TourVisitActionModel tourVisitActionModel;
        private CompanyProductModel companyProductModel;

        private Models(Context context){
            this.userModel = new UserModel(context);
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.visitOrderModel = new VisitOrderModel(context);
            this.visitOrderLineModel = new VisitOrderLineModel(context);
            this.paymentModel = new PaymentModel(context);
            this.companyCustomerModel = new CompanyCustomerModel(context);
            this.tourVisitActionModel = new TourVisitActionModel(context);
            this.companyProductModel = new CompanyProductModel(context);
        }
    }
}
