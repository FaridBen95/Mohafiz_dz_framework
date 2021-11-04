package com.MohafizDZ.project.providers;

import com.MohafizDZ.framework_repository.core.MyBaseProvider;
import com.MohafizDZ.project.models.UserModel;

public class UsersSyncProvider extends MyBaseProvider {
    @Override
    public String setAuthority() {
        return UserModel.AUTHORITY;
    }
}
