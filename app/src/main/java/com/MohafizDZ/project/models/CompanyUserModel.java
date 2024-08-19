package com.MohafizDZ.project.models;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.MohafizDZ.App;
import com.MohafizDZ.framework_repository.Utils.MySharedPreferences;
import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.framework_repository.service.firebase.IFirestoreSync;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.bullyboo.encoder.Encoder;
import ru.bullyboo.encoder.methods.AES;

public class CompanyUserModel extends Model {
    private static final String TAG = CompanyUserModel.class.getSimpleName();
    public static final String ADMIN_ROLE = "admin";
    public static final String DISTRIBUTOR_ROLE = "distributor";

    public Col company_id = new Col(Col.ColumnType.many2one).setRelationalModel(CompanyModel.class);
    public Col name = new Col(Col.ColumnType.varchar);
    public Col user_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class);
    public Col join_date = new Col(Col.ColumnType.varchar).setDefaultValue("");
    public Col encrypted_role = new Col(Col.ColumnType.varchar).setDefaultValue("");

    public CompanyUserModel(Context mContext) {
        super(mContext, "company_user");
    }

    public static String getRole(DataRow companyUserRow){
        String encryptedRole = companyUserRow.getString("encrypted_role");
        return App.TEST_MODE? encryptedRole : Encoder.BuilderAES()
                .message(encryptedRole)
                .method(AES.Method.AES_CBC_PKCS5PADDING)
                .key("JWYXelshE9saPgnpcRVuJWYXelshE9sa")
                .keySize(AES.Key.SIZE_256)
                .decrypt();
    }

    public static String getEncryptedRole(String role){
        return App.TEST_MODE? role : Encoder.BuilderAES()
                .message(role)
                .method(AES.Method.AES_CBC_PKCS5PADDING)
                .key("JWYXelshE9saPgnpcRVuJWYXelshE9sa")
                .keySize(AES.Key.SIZE_256)
                .encrypt();
    }

    public void syncUser(String userID, IFirestoreSync.SyncDownListener syncDownListener) {
        Query query = CompanyModel.getMainDocumentReference(mContext).collection(getModelName());
        query.where(Filter.equalTo("user_id", userID)).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(TAG, " from cache " + task.getResult().getMetadata().isFromCache());
                if(task.getResult().getDocuments().size() != 0) {
                    List<Map<String, Object>> data = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                        data.add(documentSnapshot.getData());
                    }
                    syncDownListener.onResult(data);
                }else if (task.getResult().getMetadata().isFromCache()){
                    syncDownListener.onException(new Exception("Slow Internet connection"));
                }else{
                    syncDownListener.onResult(new ArrayList<>());
                }
            }else{
                syncDownListener.onException(task.getException());
            }
        });
    }

    public void insertOrUpdateCompanyUser(Map<String, Object> recordLineMap, DataRow currentUserRow) {
        String userId = currentUserRow.getString(Col.SERVER_ID);
        Values values = new Values();
        values.put("user_id", userId);
        values.put("id", userId);
        values.put("company_id", CompanyModel.getCurrentCompanyId(mContext));
        values.put("name", currentUserRow.getString("name"));
        DataRow currentRow = browse( "user_id = ? ", new String[]{userId});
        if(recordLineMap != null) {
            values.put("join_date", recordLineMap.get("join_date"));
            values.put("encrypted_role", recordLineMap.get("encrypted_role"));
        }else if(currentRow == null){
            values.put("join_date", MyUtil.getCurrentDate());
            values.put("encrypted_role", getEncryptedRole(CompanyModel.getCompanyUserRole(mContext)));
        }
        if(currentRow == null){
            insert(values);
        }else{
            long localUpdateTimeInMillis = MyUtil.dateToMilliSec(currentRow.getString("_write_date"));
            long serverUpdateTimeInMillis = recordLineMap != null && recordLineMap.containsKey("write_date")?
                    Long.valueOf("" + recordLineMap.get("write_date")) : 0;
            if(serverUpdateTimeInMillis > localUpdateTimeInMillis) {
                update(currentRow.getString(Col.SERVER_ID), values);
            }
        }

    }

    public DataRow getCurrentUser(DataRow currentUserRow) {
        return browse(" user_id = ? ", new String[]{currentUserRow.getString(Col.SERVER_ID)});
    }

    public boolean isAdmin(DataRow currentUserRow) {
        DataRow companyUserRow = getCurrentUser(currentUserRow);
        return companyUserRow != null && getRole(companyUserRow).equals(ADMIN_ROLE);
    }

    public static boolean isAdmin(Context context, DataRow currentUserRow){
        return new CompanyUserModel(context).isAdmin(currentUserRow);
    }

    @Override
    public boolean canSyncDownRelations() {
        return false;
    }

    @Override
    public boolean canSyncRelations() {
        return false;
    }

    @Override
    public boolean canSyncUpRelations() {
        return false;
    }

    @Override
    public boolean allowDeleteRecordsOnServer() {
        return false;
    }

    @Override
    public boolean allowSyncDown() {
        return false;
    }

    @Override
    public boolean allowSyncUp() {
        return true;
    }

    @Override
    public boolean allowRemoveRecordsOutOfDomain() {
        return false;
    }

    @Override
    public boolean allowDeleteInLocal() {
        return false;
    }
}
