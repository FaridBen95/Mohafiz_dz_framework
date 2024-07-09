package com.MohafizDZ.framework_repository.core.Account.login_helper_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;
import com.MohafizDZ.framework_repository.core.Values;

public interface ILogIn {

    interface Presenter {
        void init();

        void googleLogIn();

        boolean isUserConnected();

        void FirebaseGoogleAuth(String token);

        void loginAsGuest();
    }

    interface View extends BasePresenter.View {

        void toggleLoading(boolean isRefreshing);

        void onDataRecovered(Values userValues);

        void onAuthSuccess();

        void onGoogleAuthSuccess(String token);
    }

    public enum LogInType{
        google, anonymous, email
    }
}
