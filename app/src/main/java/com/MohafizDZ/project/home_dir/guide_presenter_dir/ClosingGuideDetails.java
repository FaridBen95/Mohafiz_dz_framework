package com.MohafizDZ.project.home_dir.guide_presenter_dir;



import android.content.Context;

import com.MohafizDZ.own_distributor.R;

public class ClosingGuideDetails extends ConcreteGuideDetails{
    private static final String TAG = ClosingGuideDetails.class.getSimpleName();

    protected ClosingGuideDetails(IGuidePresenter.Presenter presenter, Context context, Models models) {
        super(presenter, context, models);
    }

    @Override
    public String setDescription() {
        return getString(R.string.closing_guide_descriptions);
    }

    @Override
    public boolean setButtonVisibility() {
        return true;
    }

    @Override
    public String setButtonTitle() {
        return getString(R.string.terminate_label);
    }

    @Override
    public boolean setStepVisibility() {
        return true;
    }

    @Override
    public String setStepTitle() {
        return getString(R.string.closing_guide_title);
    }

    @Override
    public int setImageDrawable() {
        return R.drawable.closed_guide_image;
    }

    @Override
    public void onClickOnAction() {
        presenter.validateClosingTour();
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
