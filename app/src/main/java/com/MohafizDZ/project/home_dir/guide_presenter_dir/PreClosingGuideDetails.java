package com.MohafizDZ.project.home_dir.guide_presenter_dir;

import android.content.Context;

import com.MohafizDZ.own_distributor.R;

public class PreClosingGuideDetails extends ConcreteGuideDetails{
    private static final String TAG = PreClosingGuideDetails.class.getSimpleName();
    protected PreClosingGuideDetails(IGuidePresenter.Presenter presenter, Context context, Models models) {
        super(presenter, context, models);
    }

    @Override
    public String setDescription() {
        return getString(R.string.pre_closing_guide_description);
    }

    @Override
    public boolean setButtonVisibility() {
        return presenter.canEndTour() || presenter.canReopenTour();
    }

    @Override
    public String setButtonTitle() {
        return presenter.canEndTour()? getString(R.string.end_tour_label) :
                presenter.canReopenTour()? getString(R.string.reopen_label) : null;
    }

    @Override
    public boolean setStepVisibility() {
        return true;
    }

    @Override
    public String setStepTitle() {
        return getString(R.string.pre_closing_title);
    }

    @Override
    public int setImageDrawable() {
        return R.drawable.pre_closing_guide_image;
    }

    @Override
    public void onClickOnAction() {
        if(presenter.canEndTour()){
            presenter.closeTour();
        }else if(presenter.canReopenTour()){
            presenter.reopenTour();
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
