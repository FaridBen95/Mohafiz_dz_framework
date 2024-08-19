package com.MohafizDZ.framework_repository.Utils;

public interface BasePresenter {
    interface Presenter{
        void onViewCreated();
        void onRefresh();
    }

    interface View{
        void showToast(String msg);
        void showSimpleDialog(String title, String msg);
        boolean inNetwork();

        void toggleLoading(boolean isRefreshing);

        void setToolbarTitle(String title);
    }
}
