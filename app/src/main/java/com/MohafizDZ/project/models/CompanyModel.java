package com.MohafizDZ.project.models;

import android.content.Context;

import com.MohafizDZ.App;
import com.MohafizDZ.framework_repository.Utils.MySharedPreferences;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.framework_repository.service.FirestoreSingleton;
import com.MohafizDZ.own_distributor.BuildConfig;
import com.google.firebase.firestore.DocumentReference;

public class CompanyModel extends Model {
    private static final String TAG = CompanyModel.class.getSimpleName();
    private static final String COMPANY_ID_KEY = "com_id_key";
    private static final String COMPANY_PHONE_NUMBER_KEY = "com_ph_key";
    private static final String COMPANY_NAME_KEY = "com_nm_key";
    private static final String COMPANY_USER_ROLE = "com_us_rl_key";
    private static final String COMPANY_DISTRIBUTOR_CODE = "com_dst_cd_key";
    public static final String QR_CODE_TYPE_COMPANY = "company";
    public static final String QR_CODE_TYPE_ADMIN_PERMISSION = "admin_permission";
    public static final String QR_CODE_TYPE_UNDEFINED = "undefined";

    public Col name = new Col(Col.ColumnType.varchar);
    public Col country_id = new Col(Col.ColumnType.varchar).setDefaultValue(4);
    public Col state_id = new Col(Col.ColumnType.varchar).setDefaultValue(1101);
    public Col currency_code = new Col(Col.ColumnType.varchar).setDefaultValue("DA");
    public Col support_phone_num = new Col(Col.ColumnType.varchar);
    public CompanyModel(Context mContext) {
        super(mContext, "company");
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    public static DocumentReference getMainDocumentReference(Context context){
        CompanyModel companyModel = new CompanyModel(context);
        String companyId = getCurrentCompanyId(context);
        return FirestoreSingleton.get().collection(companyModel.getModelName()).document(companyId);
    }

    public static String getCollectionPath(Context context){
        CompanyModel companyModel = new CompanyModel(context);
        String companyId = getCurrentCompanyId(context);
        return "/" + companyModel.getModelName() + "/" + companyId;
    }

    public static boolean isCompanyScanned(Context context){
        return new MySharedPreferences(context).getString(COMPANY_ID_KEY, null) != null;
    }

    public static String getCode(Context context){
        return new MySharedPreferences(context).getString(COMPANY_DISTRIBUTOR_CODE, "code_01");
    }

    public static void saveCompany(Context context, String id, String name, String supportPhoneNumber, String role, String code) {
        final MySharedPreferences mySharedPreferences = new MySharedPreferences(context);
        mySharedPreferences.putString(COMPANY_ID_KEY, id);
        mySharedPreferences.putString(COMPANY_NAME_KEY, name);
        mySharedPreferences.putString(COMPANY_USER_ROLE, role);
        mySharedPreferences.putString(COMPANY_DISTRIBUTOR_CODE, code);
        mySharedPreferences.putString(COMPANY_PHONE_NUMBER_KEY, supportPhoneNumber);
    }

    public static DataRow getCurrentCompany(Context context){
        CompanyModel companyModel = new CompanyModel(context);
        return companyModel.getCurrentCompany();
    }

    public DataRow getCurrentCompany(){
        DataRow companyRow = browse(new MySharedPreferences(mContext).getString(COMPANY_ID_KEY, ""));
        if(companyRow == null){
            Values values = new Values();
            MySharedPreferences mySharedPreferences = new MySharedPreferences(mContext);
            String name = mySharedPreferences.getString(COMPANY_NAME_KEY, null);
            String id = mySharedPreferences.getString(COMPANY_ID_KEY, null);
            String phoneNumber = mySharedPreferences.getString(COMPANY_PHONE_NUMBER_KEY, null);
            if(id == null){
                return null;
            }
            values.put("name", name);
            values.put("id", id);
            values.put("support_phone_num", phoneNumber);
            insert(values);
            companyRow = browse(id);
        }
        return companyRow;
    }

    public static String getCurrentCompanyId(Context context){
        DataRow currentUserRow = ((App)context.getApplicationContext()).getCurrentUser();
        if(currentUserRow != null){
            return currentUserRow.getString("company_id");
        }else {
            MySharedPreferences mySharedPreferences = new MySharedPreferences(context);
            return mySharedPreferences.getString(COMPANY_ID_KEY, null);
        }
    }

    public static String getCompanyCurrency(Context context) {
        String companyId = getCurrentCompanyId(context);
        return new CompanyModel(context).browse(companyId).getString("currency_code");
    }

    public static String getCompanyUserRole(Context context){
        return new MySharedPreferences(context).getString(COMPANY_USER_ROLE, CompanyUserModel.DISTRIBUTOR_ROLE);
    }

}
