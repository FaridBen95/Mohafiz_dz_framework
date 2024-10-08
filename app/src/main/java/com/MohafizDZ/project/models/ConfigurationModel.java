package com.MohafizDZ.project.models;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.own_distributor.BuildConfig;

public class ConfigurationModel extends Model {
    public static final String TAG = ConfigurationModel.class.getSimpleName();
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".global_configuration_provider";
    public static final String APP_LINK_KEY = "app_link_key";
    public static final String WELCOME_KEY = "welcome_key";
    public static final String MARKET_PLACE_MIN_PRICE_RANGE_FILTER = "min_price_key";
    public static final String MARKET_PLACE_MAX_PRICE_RANGE_FILTER = "max_price_key";
    public static final String PROFILE_SHOW_SALES_FOR_ALL = "profile_show_sales_for_all";
    public static final String MIN_VERSION_TO_ALLOW = "min_version_to_allow";
    public static final String FORCE_QUIT_APP_OFFLINE_KEY = "force_quit_app_offline";
    public static final String FORCE_QUIT_APP_OFFLINE_EN_MSG_KEY = "force_quit_app_offline_en_msg_key";
    public static final String FORCE_QUIT_APP_OFFLINE_FR_MSG_KEY = "force_quit_app_offline_fr_msg_key";
    public static final String FORCE_QUIT_APP_OFFLINE_AR_MSG_KEY = "force_quit_app_offline_ar_msg_key";

    public Col key = new Col(Col.ColumnType.varchar);
    public Col value = new Col(Col.ColumnType.varchar);

    public ConfigurationModel(Context mContext) {
        super(mContext, "global_configuration");
    }

    @Override
    public boolean allowDeleteRecordsOnServer() {
        return false;
    }

    @Override
    public boolean allowSyncUp() {
        return false;
    }

    public DataRow getValue(String key){
        String selection = " key = ? ";
        String[] args = {key};
        return browse(selection, args);
    }
}
