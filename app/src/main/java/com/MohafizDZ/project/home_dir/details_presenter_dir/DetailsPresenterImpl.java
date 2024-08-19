package com.MohafizDZ.project.home_dir.details_presenter_dir;

import android.content.Context;
import android.util.Log;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.framework_repository.service.ModelHelper;
import com.MohafizDZ.framework_repository.service.SyncModels;
import com.MohafizDZ.framework_repository.service.SyncingReport;
import com.MohafizDZ.framework_repository.service.firebase.FirestoreSyncDownBridge;
import com.MohafizDZ.framework_repository.service.firebase.IFirestoreSync;
import com.MohafizDZ.framework_repository.service.firebase.QueryClause;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.project.models.CashBoxLinesModel;
import com.MohafizDZ.project.models.CashBoxModel;
import com.MohafizDZ.project.models.CashDenominationModel;
import com.MohafizDZ.project.models.CompanyCustomerModel;
import com.MohafizDZ.project.models.CompanyProductCategoryModel;
import com.MohafizDZ.project.models.CompanyProductModel;
import com.MohafizDZ.project.models.CompanyUserModel;
import com.MohafizDZ.project.models.CustomerCategoryModel;
import com.MohafizDZ.project.models.DistributorInventoryLineModel;
import com.MohafizDZ.project.models.DistributorInventoryModel;
import com.MohafizDZ.project.models.DistributorModel;
import com.MohafizDZ.project.models.DistributorStockLineModel;
import com.MohafizDZ.project.models.DistributorStockModel;
import com.MohafizDZ.project.models.ExpenseSubjectModel;
import com.MohafizDZ.project.models.PaymentModel;
import com.MohafizDZ.project.models.PlannerModel;
import com.MohafizDZ.project.models.RegionModel;
import com.MohafizDZ.project.models.TourModel;
import com.MohafizDZ.project.models.TourVisitActionModel;
import com.MohafizDZ.project.models.TourVisitModel;
import com.MohafizDZ.project.models.VisitNoActionCategoryModel;
import com.MohafizDZ.project.models.VisitOrderLineModel;
import com.MohafizDZ.project.models.VisitOrderModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DetailsPresenterImpl implements IDetailsPresenter.Presenter, SyncModels.SyncModelsListener {
    private static final String TAG = DetailsPresenterImpl.class.getSimpleName();
    private static final String SYNC_KEY_SUFFIX = "__";
    private static final int DEFAULT_SYNC_DOWN_DELAY = 30;
    private static final int REGION_SYNC_DOWN_DELAY = DEFAULT_SYNC_DOWN_DELAY;

    private final IDetailsPresenter.View view;
    private final Context context;
    private final DataRow currentUserRow;
    private final Models models;
    private int refreshingCount;

    public DetailsPresenterImpl(IDetailsPresenter.View view, Context context, DataRow currentUserRow) {
        this.view = view;
        this.context = context;
        this.currentUserRow = currentUserRow;
        this.models = new Models(context);
    }

    @Override
    public void onViewCreated() {
        requestSyncCompanyUser();
        requestSyncDistributor();
        requestSyncPlannerModel();
        requestSyncRegions();
        requestSyncDown(models.customerModel);
        requestSyncDown(models.customerCategoryModel);
        requestSyncDown(models.productModel);
        requestSyncDown(models.productCategoryModel);
        requestSyncDown(models.expenseSubjectModel);
        requestSyncDown(models.denominationModel);
        requestSyncDown(models.visitNoActionCategoryModel);
        if(isAdmin()){
            DataRow plannerRow = models.plannerModel.getCurrentPlanner(currentUserRow);
            if(!plannerRow.getBoolean("synced")){
                view.requestSyncPlannerModel();
            }
        }
    }

    private boolean isAdmin(){
        return models.companyUserModel.isAdmin(currentUserRow);
    }

    @Override
    public void onRefresh() {
        requestSyncDefaultModels();
    }

    private void requestSyncDefaultModels(){
        List<ModelHelper> modelHelpers = new ArrayList<>();
        DataRow currentCompanyUserRow = models.companyUserModel.getCurrentUser(currentUserRow);
        if(!currentCompanyUserRow.getBoolean("synced")){
            modelHelpers.add(generateModelHelper(models.companyUserModel));
        }
        DataRow distributorRow = models.distributorModel.getCurrentDistributor(currentUserRow);
        if(!distributorRow.getBoolean("synced") || distributorRow.getBoolean("_is_updated")){
            modelHelpers.add(generateModelHelper(models.distributorModel));
        }
        if(models.regionModel.countUpdatedRows() > 0){
            modelHelpers.add(generateModelHelper(models.regionModel));
        }
        if(models.customerModel.countUpdatedRows() > 0){
            modelHelpers.add(generateModelHelper(models.customerModel));
        }
        if(models.customerCategoryModel.countUpdatedRows() > 0){
            modelHelpers.add(generateModelHelper(models.customerCategoryModel));
        }
        if(models.productModel.countUpdatedRows() > 0){
            modelHelpers.add(generateModelHelper(models.productModel));
        }
        if(models.productCategoryModel.countUpdatedRows() > 0){
            modelHelpers.add(generateModelHelper(models.productCategoryModel));
        }
        if(models.expenseSubjectModel.countUpdatedRows() > 0){
            modelHelpers.add(generateModelHelper(models.expenseSubjectModel));
        }
        if(models.denominationModel.countUpdatedRows() > 0){
            modelHelpers.add(generateModelHelper(models.denominationModel));
        }
        if(models.visitNoActionCategoryModel.countUpdatedRows() > 0){
            modelHelpers.add(generateModelHelper(models.visitNoActionCategoryModel));
        }
        if(modelHelpers.size() > 0) {
            SyncModels syncModels = new SyncModels(context, modelHelpers).setListener(this);
            syncModels.sync();
        }
        Log.d(TAG, "perform sync for " + modelHelpers.size());
    }

    private void requestSyncDistributor() {
        //todo switch to firebase functions
        onSyncStateChanged(isRefreshing(true));
        final String key = models.distributorModel.getModelName() + "_down_" + SYNC_KEY_SUFFIX;
        if(hasSynced(key)){
            return;
        }
        models.distributorModel.syncDistributor(currentUserRow.getString(Col.SERVER_ID),
                new IFirestoreSync.SyncDownListener() {
            @Override
            public void onException(Exception exception) {
                onSyncStateChanged(isRefreshing(false));
                exception.printStackTrace();
                view.showToast(getString(R.string.error_occurred));
            }

            @Override
            public void onResult(List<Map<String, Object>> resultList) {
                onSyncStateChanged(isRefreshing(false));
                Map<String, Object> recordLineMap = resultList.size() > 0? resultList.get(0) : null;
                models.distributorModel.insertOrUpdateDistributor(recordLineMap, currentUserRow);
                saveSyncingDate(key, MyUtil.getCurrentDate());
            }
        });
    }

    private void requestSyncCompanyUser() {
        final String key = models.companyUserModel.getModelName() + "_down_" + SYNC_KEY_SUFFIX;
        if(hasSynced(key)){
            return;
        }
        onSyncStateChanged(isRefreshing(true));
        models.companyUserModel.syncUser(currentUserRow.getString(Col.SERVER_ID),
                new IFirestoreSync.SyncDownListener() {
                    @Override
                    public void onException(Exception exception) {
                        onSyncStateChanged(isRefreshing(false));
                        exception.printStackTrace();
                        view.showToast(getString(R.string.error_occurred));
                    }

                    @Override
                    public void onResult(List<Map<String, Object>> resultList) {
                        onSyncStateChanged(isRefreshing(false));
                        Map<String, Object> recordLineMap = resultList.size() > 0? resultList.get(0) : null;
                        models.companyUserModel.insertOrUpdateCompanyUser(recordLineMap, currentUserRow);
                        saveSyncingDate(key, MyUtil.getCurrentDate());
                    }
                });
    }

    private void requestSyncPlannerModel(){
        final String key = models.plannerModel.getModelName() + "_down_" + SYNC_KEY_SUFFIX;
        if(hasSynced(key)){
            return;
        }
        final FirestoreSyncDownBridge firestoreSyncDownBridge = new FirestoreSyncDownBridge(models.plannerModel, null, new FirestoreSyncDownBridge.SyncListener() {
            @Override
            public void onSyncFinished(List<DataRow> records) {
                onSyncStateChanged(isRefreshing(false));
                models.plannerModel.insertRecords(records);
                models.plannerModel.getCurrentPlanner(currentUserRow);
                saveSyncingDate(key, MyUtil.getCurrentDate());
            }

            @Override
            public boolean isSyncable(Col col) {
                return false;
            }

            @Override
            public List<QueryClause> setQuery() {
                return new ArrayList<>();
            }

            @Override
            public void onSyncFailed(Exception exception) {
                onSyncStateChanged(isRefreshing(false));
                exception.printStackTrace();
            }

            @Override
            public boolean orderByWriteDate() {
                return false;
            }

            @Override
            public String orderByField() {
                return null;
            }
        });
        onSyncStateChanged(isRefreshing(true));
        firestoreSyncDownBridge.syncPaging(false);
    }

    private void requestSyncRegions(){
        final String key = models.regionModel.getModelName() + "_down_" + SYNC_KEY_SUFFIX;
        if(syncWithSuccess(key, REGION_SYNC_DOWN_DELAY)){
            return;
        }
        final FirestoreSyncDownBridge firestoreSyncDownBridge = new FirestoreSyncDownBridge(models.regionModel, null, new FirestoreSyncDownBridge.SyncListener() {
            @Override
            public void onSyncFinished(List<DataRow> records) {
                onSyncStateChanged(isRefreshing(false));
                models.regionModel.insertRecords(records);
                saveSyncingDate(key, MyUtil.getCurrentDate());
            }

            @Override
            public boolean isSyncable(Col col) {
                return false;
            }

            @Override
            public List<QueryClause> setQuery() {
                List<QueryClause> list = new ArrayList<>();
                list.add(new QueryClause("state_id", QueryClause.Operator.equalTo, currentUserRow.getString("state_id")));
                list.add(new QueryClause("write_date", QueryClause.Operator.greaterOrEqualThan, getLastSyncDate(key)));
                return list;
            }

            @Override
            public void onSyncFailed(Exception exception) {
                onSyncStateChanged(isRefreshing(false));
                exception.printStackTrace();
            }

            @Override
            public boolean orderByWriteDate() {
                return false;
            }

            @Override
            public String orderByField() {
                return null;
            }
        });
        onSyncStateChanged(isRefreshing(true));
        firestoreSyncDownBridge.syncPaging(false);
    }

    public void saveSyncingDate(String key, String currentDate) {
        Values values = new Values();
        values.put("model_name", key);
        values.put("last_sync_date", currentDate);
        DataRow row = models.syncingReport.browse("model_name = ? ",
                new String[]{key});
        if(row != null){
            models.syncingReport.update(row.getInteger(Col.ROWID), values);
        }else{
            models.syncingReport.insert(values);
        }
    }

    public boolean syncWithSuccess(String key, int beforeMinutes){
        return models.syncingReport.browse(" model_name = ? and last_sync_date > ? ",
                new String[]{key, MyUtil.getDateBeforeMins(beforeMinutes)}) != null;
    }

    public boolean hasSynced(String key){
        return models.syncingReport.browse(" model_name = ? ",
                new String[]{key}) != null;
    }

    public long getLastSyncDate(String key){
        DataRow syncingRow = models.syncingReport.browse(" model_name = ? ",
                new String[]{key});
        String lastSyncDate = syncingRow != null ? syncingRow.getString("last_sync_date") : "";
        return MyUtil.dateToMilliSec(lastSyncDate);
    }




    private void requestSyncDown(Model model){
        final String key = model.getModelName() + "_down_" + SYNC_KEY_SUFFIX;
        if(syncWithSuccess(key, DEFAULT_SYNC_DOWN_DELAY)){
            return;
        }
        final FirestoreSyncDownBridge firestoreSyncDownBridge = new FirestoreSyncDownBridge(model, null, new FirestoreSyncDownBridge.SyncListener() {
            @Override
            public void onSyncFinished(List<DataRow> records) {
                onSyncStateChanged(isRefreshing(false));
                model.insertRecords(records);
                saveSyncingDate(key, MyUtil.getCurrentDate());
            }

            @Override
            public boolean isSyncable(Col col) {
                return false;
            }

            @Override
            public List<QueryClause> setQuery() {
                List<QueryClause> list = new ArrayList<>();
                final long lastSyncDate = getLastSyncDate(key);
                list.add(new QueryClause("write_date", QueryClause.Operator.greaterThan, lastSyncDate));
                return list;
            }

            @Override
            public void onSyncFailed(Exception exception) {
                onSyncStateChanged(isRefreshing(false));
                exception.printStackTrace();
            }

            @Override
            public boolean orderByWriteDate() {
                return false;
            }

            @Override
            public String orderByField() {
                return null;
            }
        });
        onSyncStateChanged(isRefreshing(true));
        firestoreSyncDownBridge.syncPaging(false);
    }

    private void onSyncStateChanged(boolean isRefreshing){
        if(!isRefreshing){
            view.prepareView();
        }
        view.toggleLoading(isRefreshing);
    }

    private boolean isRefreshing(boolean isRefreshing){
        if(isRefreshing){
            refreshingCount++;
        }else{
            refreshingCount--;
        }
        return refreshingCount != 0;
    }

    private String getString(int msgId){
        return context.getString(msgId);
    }

    @Override
    public void requestSyncUp() {
        new SyncModels(context, getModelsList()).setListener(this).sync();
    }

    private List<ModelHelper> getModelsList(){
        List<ModelHelper> list = new ArrayList<>();
        if(models.inventoryModel.countUpdatedRows() > 0){
            list.add(generateModelHelper(models.inventoryModel));
        }
        if(models.inventoryLineModel.countUpdatedRows() > 0){
            list.add(generateModelHelper(models.inventoryLineModel));
        }
        if(models.stockModel.countUpdatedRows() > 0){
            list.add(generateModelHelper(models.stockModel));
        }
        if(models.stockLineModel.countUpdatedRows() > 0){
            list.add(generateModelHelper(models.stockLineModel));
        }
        if(models.paymentModel.countUpdatedRows() > 0){
            list.add(generateModelHelper(models.paymentModel));
        }
        if(models.tourModel.countUpdatedRows() > 0){
            list.add(generateModelHelper(models.tourModel));
        }
        if(models.cashBoxModel.countUpdatedRows() > 0){
            list.add(generateModelHelper(models.cashBoxModel));
        }
        if(models.cashBoxLinesModel.countUpdatedRows() > 0){
            list.add(generateModelHelper(models.cashBoxLinesModel));
        }
        if(models.visitModel.countUpdatedRows() > 0){
            list.add(generateModelHelper(models.visitModel));
        }
        if(models.actionModel.countUpdatedRows() > 0){
            list.add(generateModelHelper(models.actionModel));
        }
        if(models.orderModel.countUpdatedRows() > 0){
            list.add(generateModelHelper(models.orderModel));
        }
        if(models.orderLineModel.countUpdatedRows() > 0){
            list.add(generateModelHelper(models.orderLineModel));
        }
        return list;
    }

    private ModelHelper generateModelHelper(Model model){
        return new ModelHelper(model, null, false,
                false, true, false,
                false, false, false, null, null);
    }

    @Override
    public void onSyncStarted() {
        view.toggleSyncSnackBar(true);
    }

    @Override
    public void onSyncFinished() {
        view.toggleSyncSnackBar(false);
    }

    @Override
    public void onSyncStart(Model model) {
        Log.d(TAG, "sync started for " + model.getModelName());
        view.setSyncSnackBarTitle(model.getModelName());
    }

    @Override
    public void onSyncFinished(Model model) {
        Log.d(TAG, "sync finished for " + model.getModelName());
    }

    @Override
    public void onSyncFailed(Model model) {
        view.showToast(getString(R.string.error_occurred));
        Log.d(TAG, "sync failed for " + model.getModelName());
    }

    private static class Models{
        private final DistributorModel distributorModel;
        private final PlannerModel plannerModel;
        private final RegionModel regionModel;
        private final CompanyUserModel companyUserModel;
        private final CompanyCustomerModel customerModel;
        private final CustomerCategoryModel customerCategoryModel;
        private final CompanyProductModel productModel;
        private final CompanyProductCategoryModel productCategoryModel;
        private final ExpenseSubjectModel expenseSubjectModel;
        private final CashDenominationModel denominationModel;
        private final VisitNoActionCategoryModel visitNoActionCategoryModel;

        private final DistributorInventoryModel inventoryModel;
        private final DistributorInventoryLineModel inventoryLineModel;
        private final DistributorStockModel stockModel;
        private final DistributorStockLineModel stockLineModel;
        private final PaymentModel paymentModel;
        private final TourModel tourModel;
        private final CashBoxModel cashBoxModel;
        private final CashBoxLinesModel cashBoxLinesModel;
        private final TourVisitModel visitModel;
        private final TourVisitActionModel actionModel;
        private final VisitOrderModel orderModel;
        private final VisitOrderLineModel orderLineModel;
        private final SyncingReport syncingReport;

        private Models(Context context){
            this.distributorModel = new DistributorModel(context);
            this.plannerModel = new PlannerModel(context);
            this.regionModel = new RegionModel(context);
            this.companyUserModel = new CompanyUserModel(context);
            this.customerModel = new CompanyCustomerModel(context);
            this.customerCategoryModel = new CustomerCategoryModel(context);
            this.productModel = new CompanyProductModel(context);
            this.productCategoryModel = new CompanyProductCategoryModel(context);
            this.expenseSubjectModel = new ExpenseSubjectModel(context);
            this.denominationModel = new CashDenominationModel(context);
            this.visitNoActionCategoryModel = new VisitNoActionCategoryModel(context);

            this.inventoryModel = new DistributorInventoryModel(context);
            this.inventoryLineModel = new DistributorInventoryLineModel(context);
            this.stockModel = new DistributorStockModel(context);
            this.stockLineModel = new DistributorStockLineModel(context);
            this.paymentModel = new PaymentModel(context);
            this.tourModel = new TourModel(context);
            this.cashBoxModel = new CashBoxModel(context);
            this.cashBoxLinesModel = new CashBoxLinesModel(context);
            this.visitModel = new TourVisitModel(context);
            this.actionModel = new TourVisitActionModel(context);
            this.orderModel = new VisitOrderModel(context);
            this.orderLineModel = new VisitOrderLineModel(context);

            this.syncingReport = new SyncingReport(context);
        }
    }
}
