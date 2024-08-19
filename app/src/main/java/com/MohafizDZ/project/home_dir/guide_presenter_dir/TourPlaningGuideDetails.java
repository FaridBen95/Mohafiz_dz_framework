package com.MohafizDZ.project.home_dir.guide_presenter_dir;

import android.content.Context;

import com.MohafizDZ.own_distributor.R;

public class TourPlaningGuideDetails extends ConcreteGuideDetails{
    private static final String TAG = TourPlaningGuideDetails.class.getSimpleName();
    protected TourPlaningGuideDetails(IGuidePresenter.Presenter presenter, Context context, Models models) {
        super(presenter, context, models);
    }

    @Override
    public String setDescription() {
        return getString(R.string.tour_plan_label);
    }

    @Override
    public boolean setButtonVisibility() {
        return true;
    }

    @Override
    public String setButtonTitle() {
        return getString(R.string.plan_label);
    }

    @Override
    public boolean setStepVisibility() {
        return true;
    }

    @Override
    public String setStepTitle() {
        return getString(R.string.tour_plan_label);
    }

    @Override
    public int setImageDrawable() {
        return R.drawable.tour_plan_guide_image;
    }

    @Override
    public void onClickOnAction() {
        if(presenter.blockTourPlaning()){
            presenter.requestScanForPlan();
        }else{
            presenter.requestOpenTourForm();
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
