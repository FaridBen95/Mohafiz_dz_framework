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

public class TourConfigurationModel extends Model {
    //this model is only offline for configuration role
    //inorder to add a configuration i need to update the constants for database upgrade
    private static final String TAG = TourConfigurationModel.class.getSimpleName();
    private static final String CUSTOMER_EDIT_KEY = "customer_edit";
    private static final String CUSTOMER_QR_CODE_PRINT_KEY = "customer_qr_code_print";
    private static final String FORCE_VISIT_ACTION_KEY = "force_visit_action";
    private static final String FORCE_CURRENT_REGION_KEY = "force_current_region";
    private static final String SHOW_CASH_BOX_DETAILS_KEY = "show_cash_box_details";
    private static final String UNLIMITED_STOCK_KEY = "unlimited_stock";
    private static final String SHOW_THEO_INVENTORY_KEY = "show_theo_inventory";

    public Col key = new Col(Col.ColumnType.varchar);
    //resId inside the value to generate text based on the current language
    public Col value = new Col(Col.ColumnType.varchar);
    //checked means the default checked for this parameter
    public Col checked = new Col(Col.ColumnType.bool).setDefaultValue(0);
    public TourConfigurationModel(Context mContext) {
        super(mContext, "tour_configuration");
    }

    public static boolean canEditCustomers(List<String> configurations) {
        return configurations.contains(CUSTOMER_EDIT_KEY);
    }

    public static boolean forceCurrentRegion(List<String> configurations) {
        return configurations.contains(FORCE_CURRENT_REGION_KEY);
    }

    public static boolean canPrintCustomerCode(List<String> configurations){
        return configurations.contains(CUSTOMER_QR_CODE_PRINT_KEY);
    }

    public static boolean forceVisitAction(List<String> configurations) {
        return configurations.contains(FORCE_VISIT_ACTION_KEY);
    }

    public static boolean showCashBoxDetails(List<String> configurations){
        return App.TEST_MODE || configurations.contains(SHOW_CASH_BOX_DETAILS_KEY);
    }

    public static boolean useUnlimitedStock(List<String> configurations){
        return configurations.contains(UNLIMITED_STOCK_KEY);
    }

    public static boolean showTheoInventory(List<String> configurations) {
        return App.TEST_MODE || configurations.contains(SHOW_THEO_INVENTORY_KEY);
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
        insertConfig(db, CUSTOMER_EDIT_KEY, R.string.customer_edit_label);
        insertConfig(db, CUSTOMER_QR_CODE_PRINT_KEY, R.string.print_customer_qr_code_label);
        insertConfig(db, FORCE_VISIT_ACTION_KEY, R.string.force_visit_action_label);
        insertConfig(db, FORCE_CURRENT_REGION_KEY, R.string.force_current_region_labe);
        insertConfig(db, SHOW_CASH_BOX_DETAILS_KEY, R.string.show_cash_box_details);
        insertConfig(db, UNLIMITED_STOCK_KEY, R.string.unlimited_stock_text);
        insertConfig(db, SHOW_THEO_INVENTORY_KEY, R.string.show_theo_inventory_text);
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

    @Override
    public boolean isOnline() {
        return false;
    }
}
