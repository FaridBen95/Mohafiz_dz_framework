package com.MohafizDZ.project.home_dir.guide_presenter_dir;

import android.content.Context;
import android.os.Handler;

import com.MohafizDZ.own_distributor.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class TourProgressGuideDetails extends ConcreteGuideDetails{
    private static final String TAG = TourProgressGuideDetails.class.getSimpleName();

    protected TourProgressGuideDetails(IGuidePresenter.Presenter presenter, Context context, Models models) {
        super(presenter, context, models);
    }

    @Override
    public String setDescription() {
        return getString(R.string.tour_progress_guide_msg);
    }

    @Override
    public boolean setButtonVisibility() {
        return true;
    }

    @Override
    public String setButtonTitle() {
        return getString(R.string.end_tour_label);
    }

    @Override
    public boolean setStepVisibility() {
        return true;
    }

    @Override
    public String setStepTitle() {
        return getString(R.string.tour_progress_title);
    }

    @Override
    public int setImageDrawable() {
        return R.drawable.tour_progress_guide_image;
    }

    @Override
    public void onClickOnAction() {
        new MaterialAlertDialogBuilder(context).
                setTitle(getString(R.string.end_tour_label)).
                setMessage(getString(R.string.end_tour_msg)).
                setPositiveButton(getString(R.string.end_tour_label), (dialogInterface, i) -> presenter.endTour()).
                setNegativeButton(getString(R.string.cancel_label), null).
                create().show();
    }

    @Override
    public String getTag() {
        return TAG;
    }

//    @Override
//    public void onViewCreated(final boolean guideDetailsVisible) {
//        if(guideDetailsVisible && presenter.canToggleGuideDetails()) {
//            presenter.lockGuideDetailsVisibility(true);
//            new Handler().postDelayed(() -> {
//                presenter.lockGuideDetailsVisibility(false);
//                presenter.requestToggleGuideDetails();
//            }, 3000);
//        }
//    }
}
