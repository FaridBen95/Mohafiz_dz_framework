package com.MohafizDZ.project.payment_details_dir;

import static com.MohafizDZ.project.models.VisitOrderModel.DELETE_ORDER_DELAY;

import android.app.Activity;
import android.content.Context;
import android.util.Pair;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.expenses_list_dir.IExpensesPresenter;
import com.MohafizDZ.project.models.AttachmentLocalModel;
import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.models.CompanyProductModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.PaymentModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.TourVisitActionModel;
import com.MohafizDZ.project.models.UserModel;
import com.MohafizDZ.project.models.VisitOrderModel;
import com.MohafizDZ.project.payment_dir.IPaymentPresenter;
import com.MohafizDZ.project.payment_dir.strategies.ConcretePaymentStrategy;
import com.MohafizDZ.project.payment_dir.strategies.PaymentStrategy;
import com.MohafizDZ.project.payment_dir.strategies.RefundStrategy;

import java.util.ArrayList;
import java.util.List;

public class PaymentDetailsPresenterImpl implements IPaymentDetailsPresenter.Presenter{
    private static final String TAG = PaymentDetailsPresenterImpl.class.getSimpleName();

    private final IPaymentDetailsPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final String paymentId;
    private DataRow currentDistributorRow, currentTourRow, customerRow, paymentRow,
            paymentTour, paymentDistributor, sellerRow, actionDetailsRow;
    private String currencyCode;
    private final List<String> attachments;
    private boolean isValidation;

    public PaymentDetailsPresenterImpl(IPaymentDetailsPresenter.View view, Context context, DataRow currentUserRow, String paymentId) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        this.paymentId = paymentId;
        this.attachments = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        if(initData()){
            view.setReference(paymentRow.getString("name"));
            String orderName = getOrderName();
            view.toggleOrderReference(orderName != null);
            view.setOrderName(orderName);
            boolean isExpense = isExpense();
            view.toggleCustomerContainer(!isExpense);
            view.toggleValidateButton(isExpense && isValidation());
            if(!isExpense) {
                view.setCustomerName(customerRow.getString("name"));
            }else{
                view.setExpenseSubject(paymentRow.getString("expense_subject"));
                view.setExpenseNote(paymentRow.getString("expense_note"));
            }
            view.setDistanceToCustomer(calculateDistance());
            String tourName = paymentTour != null? paymentTour.getString("name") : "-";
            view.setTourName(tourName);
            String sellerName = sellerRow != null? sellerRow.getString("name") : "-";
            view.setSellerName(sellerName);
            view.setDate(paymentRow.getString("payment_date"));
            view.togglePaymentContainer(!isExpense);
            view.toggleExpenseContainer(isExpense);
            view.setPaymentAmount(getPrice(paymentRow.getFloat("amount")));
            view.setRemainingAmount(getPrice(paymentRow.getFloat("remaining_amount")));
            view.setExpensesLeft(getPrice(Math.abs(paymentRow.getFloat("remaining_amount"))));
            prepareAttachments();
            onRefresh();
        }else{
            view.showToast(getString(R.string.error_occurred));
            view.goBack();
        }
    }

    private boolean isValidation() {
        return isValidation;
    }

    private void prepareAttachments(){
        view.initAdapter(attachments);
        loadAttachments();
        refreshImagesAdapter();
    }

    private void loadAttachments(){
        if(isExpense()) {
            attachments.clear();
            String selection = " model_name = ? and rel_id = ? ";
            String[] args = {models.paymentModel.getModelName(), paymentRow.getString(Col.SERVER_ID)};
            for (DataRow row : models.attachmentLocalModel.getRows(selection, args)) {
                attachments.add(row.getString("path"));
            }
        }
    }

    private void refreshImagesAdapter() {
        view.toggleAttachmentsContainer(attachments.size() > 0);
        view.onLoadFinished(attachments);
    }


    private boolean isExpense() {
        return paymentRow.getBoolean("is_expenses") || paymentRow.getString("customer_id").equals("false");
    }

    private String getOrderName() {
        DataRow orderRow = models.orderModel.browse(paymentRow.getString("visit_order_id"));
        if(orderRow != null){
            return orderRow.getString("name");
        }
        return null;
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
        paymentRow = models.paymentModel.browse(paymentId);
        if(paymentRow != null){
            currentDistributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
            currentTourRow = models.tourModel.getCurrentTour(currentDistributorRow);
            customerRow = models.companyCustomerModel.browse(paymentRow.getString("customer_id"));
            paymentDistributor = models.distributorModel.browse(paymentRow.getString("distributor_id"));
            sellerRow = paymentDistributor != null? models.userModel.browse(paymentDistributor.getString("user_id")) : null;
            paymentTour = models.tourModel.browse(paymentRow.getString("tour_id"));
            actionDetailsRow = models.tourVisitActionModel.browse(paymentRow.getString("action_details_id"));
            currencyCode = CompanyModel.getCompanyCurrency(context);
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onRefresh() {
    }

    @Override
    public void onCreateOptionsMenu() {
        view.toggleDeleteMenuItem(isCancelable());
    }

    private boolean isDeletable(){
        String validatedDate = paymentRow.getString("payment_date");
        final String dateBeforeMins = MyUtil.getDateBeforeMins(DELETE_ORDER_DELAY);
        boolean deletable = isExpense() || MyUtil.getDateFromStringDate(validatedDate).getTime() > MyUtil.getDateFromStringDate(dateBeforeMins).getTime();
        return isCancelable() && deletable;
    }

    private boolean isCancelable(){
        if(isExpense()){
            return paymentRow.getString("state").equals(PaymentModel.STATE_EXPENSES_DRAFT);
        }else {
            return customerRow != null && currentTourRow.getString(Col.SERVER_ID).equals(paymentRow.getString("tour_id")) &&
                    paymentRow.getFloat("amount") > 0;
        }
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
    public void setExpenseValidation(boolean isValidation) {
        this.isValidation = isValidation;
    }

    @Override
    public void onValidate() {
        if(paymentRow.getBoolean("is_expenses")) {
            final Pair<Float, Float> totalExpenses = getTotalExpenses();
            float total = totalExpenses.second + paymentRow.getFloat("amount");
            float limit = getExpensesLimit();
            if(total > limit){
                view.showSimpleDialog(getString(R.string.limit_exceeded_title), getString(R.string.expenses_limit_exceeded_msg));
            }else {
                Values values = new Values();
                values.put("state", PaymentModel.STATE_EXPENSES_DONE);
                models.paymentModel.update(paymentRow.getString(Col.SERVER_ID), values);
                view.goBack();
            }
        }else {
            view.showToast(getString(R.string.error_occurred));
        }
    }

    private float getExpensesLimit(){
        return currentTourRow != null? currentTourRow.getFloat("expenses_limit") : 0.0f;
    }


    private Pair<Float, Float> getTotalExpenses() {
        String tourId = currentTourRow != null? currentTourRow.getString(Col.SERVER_ID) : null;
        return models.paymentModel.getTotalExpenses(tourId);
    }

    @Override
    public void cancelOrder(IPaymentPresenter.ValidateView validateView) {
        if(isExpense()){
            Values values = new Values();
            values.put("state", PaymentModel.STATE_CANCEL);
            models.paymentModel.update(paymentRow.getString(Col.SERVER_ID), values);
            validateView.goBack(Activity.RESULT_OK);
        }else{
            cancelCurrentPayment( validateView);
        }
    }

    private void cancelCurrentPayment(IPaymentPresenter.ValidateView validateView){
        float amount = paymentRow.getFloat("amount");
        if(amount > 0){
            validateCancel(new RefundStrategy(context, validateView, currentUserRow), validateView);
        }else{
            validateCancel(new PaymentStrategy(context, validateView, currentUserRow), validateView);
        }
    }

    private void validateCancel(ConcretePaymentStrategy paymentStrategy, IPaymentPresenter.ValidateView validateView){
        float amount = paymentRow.getFloat("amount");
        ConcretePaymentStrategy.ValidateHelper validateHelper = ConcretePaymentStrategy.ValidateHelper.
                getInstance(paymentStrategy, new com.MohafizDZ.project.payment_dir.models.Models(context), customerRow.getString(Col.SERVER_ID));
        validateHelper.setPayment(amount);
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
                view.toggleLoading(true);
            }
        });
    }

    private static class Models{
        private final UserModel userModel;
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final PaymentModel paymentModel;
        private final CompanyCustomerModel companyCustomerModel;
        private final TourVisitActionModel tourVisitActionModel;
        private final CompanyProductModel companyProductModel;
        private final VisitOrderModel orderModel;
        private final AttachmentLocalModel attachmentLocalModel;


        private Models(Context context){
            this.userModel = new UserModel(context);
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.paymentModel = new PaymentModel(context);
            this.companyCustomerModel = new CompanyCustomerModel(context);
            this.tourVisitActionModel = new TourVisitActionModel(context);
            this.companyProductModel = new CompanyProductModel(context);
            this.orderModel = new VisitOrderModel(context);
            this.attachmentLocalModel = new AttachmentLocalModel(context);
        }
    }
}
