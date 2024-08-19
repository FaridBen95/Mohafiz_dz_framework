package com.MohafizDZ.project.settings_dir;

import com.MohafizDZ.framework_repository.Utils.BasePresenter;

public interface ISettingsPresenter {

    interface Presenter extends BasePresenter.Presenter{

        void generateDistInviteQrCode(String code);
        void generateAdminInviteQrCode(String code);
        void generateAdminQrCode();
    }

    interface View extends BasePresenter.View{

        void setCompanyName(String text);
        void setRole(String text);
        void setUserName(String text);
        void setJoinDate(String text);

        void toggleQrCodeContainer(boolean visible);

        void showQrCode(String qrCodeContent);
    }
}
