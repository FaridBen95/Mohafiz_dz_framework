package com.MohafizDZ.project.scan_dir;

import android.os.Bundle;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

public interface IScanPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void onScan(String result);
    }

    interface View extends BasePresenter.View{

        void goBack();

        void setResult(int resultCode, Bundle data);
    }

    enum PresenterType{
        admin, productScan,
    }
}
