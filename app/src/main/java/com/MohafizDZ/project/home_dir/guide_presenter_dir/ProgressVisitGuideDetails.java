package com.MohafizDZ.project.home_dir.guide_presenter_dir;

import android.content.Context;
import android.os.Handler;

import com.MohafizDZ.own_distributor.R;

public class ProgressVisitGuideDetails extends ConcreteGuideDetails{
    private static final String TAG = ProgressVisitGuideDetails.class.getSimpleName();
    protected ProgressVisitGuideDetails(IGuidePresenter.Presenter presenter, Context context, Models models) {
        super(presenter, context, models);
    }

    @Override
    public String setDescription() {
        return getString(R.string.progress_visit_guide_description);
    }

    @Override
    public boolean setButtonVisibility() {
        return true;
    }

    @Override
    public String setButtonTitle() {
        return getString(R.string.stop_visit_label);
    }

    @Override
    public boolean setStepVisibility() {
        return true;
    }

    @Override
    public String setStepTitle() {
        return getString(R.string.progress_visit_guide_title);
    }

    @Override
    public int setImageDrawable() {
        return R.drawable.tour_progress_visit_guide_image;
    }

    @Override
    public void onClickOnAction() {
        presenter.requestStopVisit();
    }

    @Override
    public String getTag() {
        return TAG;
    }

//    @Override
//    public void onViewCreated(final boolean guideDetailsVisible) {
////        if(guideDetailsVisible && presenter.canToggleGuideDetails()) {
////            presenter.lockGuideDetailsVisibility(true);
////            new Handler().postDelayed(() -> {
////                presenter.lockGuideDetailsVisibility(false);
////                presenter.requestToggleGuideDetails();
////                }, 3000);
////        }
//    }
}
