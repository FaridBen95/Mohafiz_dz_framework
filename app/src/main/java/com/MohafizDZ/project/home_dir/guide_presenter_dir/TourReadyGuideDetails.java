package com.MohafizDZ.project.home_dir.guide_presenter_dir;

import android.content.Context;

import com.MohafizDZ.framework_repository.core.Values;
import com.MohafizDZ.own_distributor.R;

public class TourReadyGuideDetails extends ConcreteGuideDetails{
    private static final String TAG = TourReadyGuideDetails.class.getSimpleName();

    protected TourReadyGuideDetails(IGuidePresenter.Presenter presenter, Context context, Models models) {
        super(presenter, context, models);
    }

    @Override
    public String setDescription() {
        return getString(R.string.get_ready_to_start_msg);
    }

    @Override
    public boolean setButtonVisibility() {
        return true;
    }

    @Override
    public String setButtonTitle() {
        return presenter.canStartTour()? getString(R.string.start_tour_label) : getString(R.string.prepare_stock_label);
    }

    @Override
    public boolean setStepVisibility() {
        return true;
    }

    @Override
    public String setStepTitle() {
        return getString(R.string.get_ready_to_start_label);
    }

    @Override
    public int setImageDrawable() {
        return R.drawable.ready_guide_image;
    }

    @Override
    public void onClickOnAction() {
        if(presenter.canStartTour()) {
            presenter.startTour();
            presenter.onRefresh();
        }else{
            presenter.requestOpenInitialStock();
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
