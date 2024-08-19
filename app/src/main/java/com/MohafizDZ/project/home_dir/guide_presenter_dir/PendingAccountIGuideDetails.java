package com.MohafizDZ.project.home_dir.guide_presenter_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.Utils.IntentUtils;
import com.MohafizDZ.own_distributor.R;

public class PendingAccountIGuideDetails extends ConcreteGuideDetails {
    private static final String TAG = PendingAccountIGuideDetails.class.getSimpleName();
    protected PendingAccountIGuideDetails(IGuidePresenter.Presenter presenter, Context context, Models models) {
        super(presenter, context, models);
    }

    @Override
    public String setDescription() {
        return getString(R.string.account_not_configured_msg);
    }

    @Override
    public boolean setButtonVisibility() {
        return true;
    }

    @Override
    public String setButtonTitle() {
        return getString(R.string.call_label);
    }

    @Override
    public boolean setStepVisibility() {
        return false;
    }

    @Override
    public String setStepTitle() {
        return getString(R.string.pending_account_title);
    }

    @Override
    public int setImageDrawable() {
        return R.drawable.pending_account_guide_image;
    }

    @Override
    public void onClickOnAction() {
        if(presenter.hasPhoneCallPermission()){
            String phoneNumber = presenter.getSupportPhoneNumber();
            IntentUtils.requestCall(context, phoneNumber);
        }else{
            presenter.requestPhoneCallPermission();
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
