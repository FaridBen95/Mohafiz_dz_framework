package com.MohafizDZ.project.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.MohafizDZ.App;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;

import java.util.List;

public class DistributorConfigurationModel extends Model {
    private static final String TAG = DistributorConfigurationModel.class.getSimpleName();
    private static final String BLOCK_TOUR_PLANING_KEY = "block_tour_planing";
    private static final String CAN_EDIT_BALANCE_LIMIT = "can_edit_balance_limit";
    private static final String CAN_EDIT_REGION = "can_edit_region";
    private static final String CAN_EDIT_CUSTOMER_CATEGORY = "can_edit_customer_category";
    private static final String CAN_EDIT_PRODUCT = "can_edit_product";
    private static final String CAN_EDIT_PRODUCT_CATEGORY = "can_edit_product_category";
    private static final String CAN_EDIT_PRODUCT_PRICE = "can_edit_product_price";
    private static final String CAN_EDIT_NO_ACTION_CATEGORY = "can_edit_action";
    private static final String CAN_EDIT_EXPENSE_SUBJECT = "can_edit_expense_subject";
    private static final String CAN_EDIT_DENOMINATION = "can_edit_denomination";

    public Col key = new Col(Col.ColumnType.varchar);
    //resId inside the value to generate text based on the current language
    public Col value = new Col(Col.ColumnType.varchar);
    public DistributorConfigurationModel(Context mContext) {
        super(mContext, "distribution_configurations");
    }

    public static boolean canEditRegions(List<String> configurations) {
        return configurations.contains(CAN_EDIT_REGION);
    }

    public static boolean canEditCustomerCategory(List<String> configurations) {
        return configurations.contains(CAN_EDIT_CUSTOMER_CATEGORY);
    }

    public static boolean canEditCustomerBalance(List<String> configurations) {
        return configurations.contains(CAN_EDIT_BALANCE_LIMIT);
    }

    public static boolean blockTourPlaning(List<String> configurations){
        return configurations.contains(BLOCK_TOUR_PLANING_KEY);
    }

    public static boolean canCreateNoActionCategory(List<String> configurations) {
        return configurations.contains(CAN_EDIT_NO_ACTION_CATEGORY);
    }

    public static boolean canEditExpenseSubject(List<String> configurations) {
        return configurations.contains(CAN_EDIT_EXPENSE_SUBJECT);
    }

    public static boolean canEditProducts(List<String> configurations) {
        return configurations.contains(CAN_EDIT_PRODUCT);
    }

    public static boolean canSetPrice(List<String> configurations) {
        return configurations.contains(CAN_EDIT_PRODUCT_PRICE);
    }

    public static boolean canEditProductCategories(List<String> configurations) {
        return configurations.contains(CAN_EDIT_PRODUCT_CATEGORY);
    }

    public static boolean canCreateDenomination(List<String> configurations) {
        return App.TEST_MODE || configurations.contains(CAN_EDIT_DENOMINATION);
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public void onModelCreated(SQLiteDatabase db) {
        initConfiguration(db);
    }

    @Override
    public void onModelUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onModelUpgrade(db, oldVersion, newVersion);
        resetConfigurations(db);
        initConfiguration(db);
    }

    private void resetConfigurations(SQLiteDatabase db) {
        db.execSQL("DELETE FROM "+ getModelName() + " WHERE _id > 0 ");
    }

    private void initConfiguration(SQLiteDatabase db){
        insertConfig(db, BLOCK_TOUR_PLANING_KEY, R.string.block_tour_planing_txt);
        insertConfig(db, CAN_EDIT_BALANCE_LIMIT, R.string.edit_customer_balance_txt);
        insertConfig(db, CAN_EDIT_REGION, R.string.edit_region_txt);
        insertConfig(db, CAN_EDIT_CUSTOMER_CATEGORY, R.string.edit_customer_category_txt);
        insertConfig(db, CAN_EDIT_PRODUCT, R.string.edit_product_txt);
        insertConfig(db, CAN_EDIT_PRODUCT_CATEGORY, R.string.edit_product_categories_txt);
        insertConfig(db, CAN_EDIT_PRODUCT_PRICE, R.string.edit_product_price_txt);
        insertConfig(db, CAN_EDIT_NO_ACTION_CATEGORY, R.string.edit_action_categories_txt);
        insertConfig(db, CAN_EDIT_EXPENSE_SUBJECT, R.string.edit_expense_subjects_txt);
    }

    private void insertConfig(SQLiteDatabase db, String key, int resId){
        Values values = new Values();
        values.put("key", key);
        values.put(Col.SERVER_ID, key);
        values.put("value", resId);
        prepareValues(values);
        db.insert(getModelName(), null, values.toContentValues());
    }

    private void prepareValues(Values values){
        values.put("_write_date", MyUtil.getCurrentDate());
        values.put("_create_date", MyUtil.getCurrentDate());
        if (!values.containsKey("_is_active"))
            values.put("_is_active", 1);
        if (!values.containsKey("removed"))
            values.put("removed", 0);
        if (!values.containsKey("synced"))
            values.put("synced", 0);
        if (!values.containsKey(Col.SERVER_ID)){
            values.put(Col.SERVER_ID, getCreateXId());
        }
    }
}
