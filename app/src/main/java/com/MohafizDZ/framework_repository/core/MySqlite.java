package com.MohafizDZ.framework_repository.core;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.MohafizDZ.App;
import com.MohafizDZ.framework_repository.core.Account.MUser;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.MohafizDZ.framework_repository.Utils.SQLUtil;

import java.util.HashMap;

public class MySqlite extends SQLiteOpenHelper{
    public static final String TAG = MySqlite.class.getSimpleName();
    private final MUser mUser;

    private String DATABASE_NAME = MConstants.DATABASE_NAME;
    private Context mContext;
    private int DATABASE_VERSION = MConstants.DATABASE_VERSION;

    public DatabaseObserver getDatabaseObserver() {
        return databaseObserver;
    }

    public void setDatabaseObserver(DatabaseObserver databaseObserver) {
        this.databaseObserver = databaseObserver;
    }

    private DatabaseObserver databaseObserver;

    public MySqlite(Context mContext, MUser mUser){
        super(mContext, mUser == null? MConstants.DATABASE_NAME : mUser.getDatabaseName(),
                null, MConstants.DATABASE_VERSION,
                null);
        this.DATABASE_NAME = mUser == null? MConstants.DATABASE_NAME : mUser.getDatabaseName();
        this.DATABASE_VERSION = MConstants.DATABASE_VERSION;
        this.mContext = mContext;
        this.mUser = mUser == null ? MUser.getCurrentMUser(mContext) : mUser;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        Log.i(TAG, "Creating database.");
        ModelRegistryUtils registryUtils = App.getModelRegistryUtils();
        HashMap<String, Class<? extends Model>> models = registryUtils.getModels();
        for (String key : models.keySet()){
            Model model = App.getModel(mContext, key, mUser.getAndroidAccountName());
            assert model != null;
            if(Model.class.equals(model.getClass().getSuperclass())) {
                SQLUtil.generateCreateStatement(model);
            }
        }
        HashMap<String, String> sqlCreateStatement = SQLUtil.getSqlCreateStatement();
        for(String modelName : sqlCreateStatement.keySet()){
            final String createQuery = sqlCreateStatement.get(modelName);
            db.execSQL(createQuery);
        }
        Log.i(TAG, "Tables Created ");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Creating database.");
        ModelRegistryUtils registryUtils = App.getModelRegistryUtils();
        HashMap<String, Class<? extends Model>> models = registryUtils.getModels();
        for (String key : models.keySet()){
            Model model = App.getModel(mContext, key, mUser.getAndroidAccountName());
            assert model != null;
            if(Model.class.equals(model.getClass().getSuperclass())) {
                model.onModelUpgrade(db, oldVersion, newVersion);
            }
        }
    }




}
