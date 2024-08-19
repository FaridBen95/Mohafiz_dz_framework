package com.MohafizDZ.project.expenses_list_dir.expense_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.AttachmentLocalModel;
import com.MohafizDZ.project.models.CompanyModel;
import com.MohafizDZ.project.models.DistributorConfigurationModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.ExpenseSubjectModel;
import com.MohafizDZ.project.models.PaymentModel;
import com.MohafizDZ.project.models.TourModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ExpensePresenterImpl implements IExpensePresenter.Presenter{
    private static final String TAG = ExpensePresenterImpl.class.getSimpleName();

    private final IExpensePresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private final String currencyCode;
    private final List<String> attachments;
    private DataRow tourRow, distributorRow;
    private float expensesLimit;
    private float expenses;
    private float allowedExpenses;
    private Float amount = null;

    public ExpensePresenterImpl(IExpensePresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
        currencyCode = CompanyModel.getCompanyCurrency(context);
        attachments = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        view.setToolbarTitle(getString(R.string.expense_label));
        initData();
        initSubjectList();
        view.setName(currentUserRow.getString("name"));
        view.setAllowedLimit(getPrice(expensesLimit));
        view.setExpenses(getPrice(expenses));
        view.setAllowedExpenses(getPrice(allowedExpenses));
        view.initAdapter(attachments);
        toggleExpenseLimitViews();
        onRefresh();
    }

    private void initSubjectList(){
        view.initSubjectFilter(getSubjectList());
    }


    private LinkedHashMap<String, String> getSubjectList(){
        List<DataRow> categories = models.expenseSubjectModel.getRows();
        return getNamesFromRows(categories);
    }

    private LinkedHashMap<String, String> getNamesFromRows(List<DataRow> rows) {
        LinkedHashMap<String, String> list = new LinkedHashMap<>();
        for(DataRow row : rows){
            list.put(row.getString(Col.SERVER_ID), row.getString("name"));
        }
        return list;
    }

    private String getString(int resId){
        return context.getString(resId);
    }
    private void toggleExpenseLimitViews(){
        boolean allowExpense = tourRow.getBoolean("control_expenses_amount") || tourRow.getFloat("expenses_limit") > 0;
        view.toggleExpenseLimit(allowExpense);
        view.toggleAllowedExpenses(allowExpense);
        view.toggleExpensesLeft(allowExpense);
    }

    @Override
    public void requestAddPayment() {
        view.openPaymentDialog(Math.abs(0.0f), null);
    }

    @Override
    public void requestEditPayment() {
        view.openPaymentDialog(Math.abs(0.0f), Math.abs(amount));
    }

    @Override
    public void onAddPayment(String value) {
        try{
            this.amount = Float.valueOf(value);
        }catch (Exception ignored){
            this.amount = 0.0f;
        }
        onRefresh();
    }

    @Override
    public void requestAddImage(Integer position, String imagePath) {
        if(position == null) {
            attachments.add(imagePath);
        }else{
            attachments.set(position, imagePath);
        }
        refreshImagesAdapter();
    }

    @Override
    public void requestModifyImage(int position) {
        view.showModifyImageDialog(position);
    }

    @Override
    public void onValidate(String subject, String note) {
        if(attachments.size() == 0){
            view.showToast(getString(R.string.images_required_msg));
        }else{
            if(!allowedSubject(subject)){
                view.showToast(getString(R.string.cant_create_expense_subject));
                return;
            }
            view.requestCurrentLocation(new IExpensePresenter.LocationListener() {
                @Override
                public void onLocationChanged(double latitude, double longitude) {
                    view.toggleLoading(false);
                    saveSubject(subject);
                    DataRow expenseRow = models.paymentModel.createExpense(subject, note, amount, allowedExpenses - amount, tourRow, distributorRow, MyUtil.getCurrentDate());
                    if (expenseRow != null) {
                        String id = expenseRow.getString(Col.SERVER_ID);
                        for(String image : attachments){
                            Values attachmentValues = new Values();
                            attachmentValues.put("path", image);
                            attachmentValues.put("col_name", "images_list");
                            attachmentValues.put("model_name", models.paymentModel.getModelName());
                            attachmentValues.put("rel_id", id);
                            attachmentValues.put("is_uploaded_to_server", 0);
                            models.attachmentLocalModel.insert(attachmentValues);
                        }
                        view.loadDetails(id);
                        view.goBack();
                    } else {
                        view.showToast(getString(R.string.error_occurred));
                    }
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

    private boolean allowedSubject(String subject) {
        return hasSubject(subject) || canCreateSubject();
    }

    private boolean canCreateSubject() {
        return DistributorConfigurationModel.canEditExpenseSubject(distributorRow.getRelArray(models.distributorModel, "configurations"));
    }

    @Override
    public void onBackPressed() {
        view.showIgnoreChangesDialog();
    }

    private void saveSubject(String subject) {
        if(hasSubject(subject)){
            return;
        }
        Values values = new Values();
        values.put("name", subject);
        models.expenseSubjectModel.insert(values);
    }

    private boolean hasSubject(String subject){
        String selection = " name like ? ";
        String[] args = {"%" + subject + "%"};
        return models.expenseSubjectModel.browse(selection, args) != null;
    }

    @Override
    public void deleteImage(int position) {
        attachments.remove(position);
        refreshImagesAdapter();
    }

    private void refreshImagesAdapter(){
        view.toggleAttachmentsContainer(attachments.size() > 0);
        view.onLoadFinished(attachments);
    }

    private float calculateExpenses(){
        float total = 0.0f;
        String selection = " customer_id = ? and tour_id = ? and state <> ? ";
        String[] args = { "false", tourRow.getString(Col.SERVER_ID), PaymentModel.STATE_CANCEL};
        for(DataRow row : models.paymentModel.getRows(selection, args)){
            total += Math.abs(row.getFloat("amount"));
        }
        return total;
    }
    protected String getPrice(Float price){
        if(price != null) {
            return price + " " + currencyCode;
        }else{
            return "-";
        }
    }


    private void initData(){
        this.distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        this.tourRow = models.tourModel.getCurrentTour(distributorRow);
        expensesLimit = tourRow.getFloat("expenses_limit");
        expenses = calculateExpenses();
        allowedExpenses = expensesLimit - expenses;
    }

    @Override
    public void onRefresh() {
        view.toggleAmountContainer(amount != null);
        view.toggleAddButton(amount == null);
        view.toggleEditButton(amount != null);
        view.setExpense(getPrice(amount));
        if(amount != null) {
            view.setExpensesLeft(getPrice(allowedExpenses - amount));
        }else{
            view.setExpensesLeft(getPrice(allowedExpenses));
        }
        refreshImagesAdapter();
    }

    private static class Models{
        private final TourModel tourModel;
        private final DistributorModel distributorModel;
        private final PaymentModel paymentModel;
        private final ExpenseSubjectModel expenseSubjectModel;
        private final AttachmentLocalModel attachmentLocalModel;

        private Models(Context context){
            this.tourModel = new TourModel(context);
            this.distributorModel = new DistributorModel(context);
            this.paymentModel = new PaymentModel(context);
            this.expenseSubjectModel = new ExpenseSubjectModel(context);
            this.attachmentLocalModel = new AttachmentLocalModel(context);
        }
    }
}
