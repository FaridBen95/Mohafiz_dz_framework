package com.MohafizDZ.framework_repository.core.Account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.MohafizDZ.App;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class MUser {
    public static final String TAG = MUser.class.getSimpleName();
    public static final String AUTH_TOKEN_KEY = "auth_access_token";
    public static final String AUTH_TOKEN_WRITE_DATE_KEY = "auth_access_token_write_date";

    private String uid;
    private String androidAccountName = MConstants.DEFAULT_ANDROID_NAME;
    private String email;
    private String phoneNumber;
    private String imgPath;
    private String name;
    private String lastName;
    private String databaseName;
    private Account account;

    MUser(){

    }

    public static MUser getCurrentMUser(Context context){
        return getCurrentMUser(context, null);
    }

    public static MUser getCurrentMUser(Context context,@Nullable FirebaseUser firebaseUser){
        String uid = null;
        if(firebaseUser == null){
            firebaseUser = ((App)context.getApplicationContext()).firebaseAuth.getCurrentUser();
        }
        if(firebaseUser != null){
            uid = firebaseUser.getUid();
        }
        for(MUser user : getAllMUsers(context)){
            if((uid == null && user.getAndroidAccountName().equals(MConstants.DEFAULT_ANDROID_NAME)
                    || user.getUid().equals(uid))){
                return user;
            }
        }
        return null;
    }

    public static List<MUser> getAllMUsers(Context context){
        List<MUser> users = new ArrayList<>();
        AccountManager aManager = AccountManager.get(context);
        for (Account account : aManager.getAccountsByType(MConstants.KEY_ACCOUNT_TYPE)) {
            MUser user = new MUser();
            user.fillFromAccount(aManager, account);
            users.add(user);
        }
        return users;
    }

    public static MUser getDetails(Context context, String username) {
        for (MUser user : getAllMUsers(context))
            if (user.getAndroidAccountName().equals(username)) {
                return user;
            }
        return null;
    }

    protected void fillFromAccount(AccountManager accountManager, Account account) {
        String uid = accountManager.getUserData(account, "uid");
        uid = uid != null? uid : "";
        setUid(uid);
        setAndroidAccountName(accountManager.getUserData(account, "android_account_name"));
        String databaseName = uid.equals("")? MConstants.DATABASE_NAME : uid + "database.db";
        setDatabaseName(databaseName);
        setEmail(accountManager.getUserData(account, "email"));
        setImgPath(accountManager.getUserData(account, "image_path"));
        setName(accountManager.getUserData(account, "name"));
        setLastName(accountManager.getUserData(account, "last_name"));
        setPhoneNumber(accountManager.getUserData(account, "phone_number"));
        this.account = account;
    }

    public Bundle toBundle(){
        Bundle data = new Bundle();
        data.putString("uid", uid);
        data.putString("android_account_name", androidAccountName);
        data.putString("phone_number", phoneNumber);
        data.putString("email", email);
        data.putString("image_path", imgPath);
        data.putString("name", name);
        data.putString("last_name", lastName);
        return data;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAndroidAccountName() {
        return androidAccountName;
    }

    public void setAndroidAccountName(String androidAccountName) {
        this.androidAccountName = androidAccountName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
