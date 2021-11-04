package com.MohafizDZ.project.providers;

import com.MohafizDZ.framework_repository.core.MyBaseProvider;
import com.MohafizDZ.project.models.ConfigurationModel;

public class ConfigurationSyncProvider extends MyBaseProvider {

    @Override
    public String setAuthority() {
        return ConfigurationModel.AUTHORITY;
    }
}
