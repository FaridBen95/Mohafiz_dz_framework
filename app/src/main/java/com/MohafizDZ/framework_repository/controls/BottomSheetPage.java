package com.MohafizDZ.framework_repository.controls;

import android.content.Context;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.MohafizDZ.framework_repository.core.BaseFragment;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

public abstract class BottomSheetPage extends LinearLayout implements BlockingStep {
    public static final String TAG = BottomSheetPage.class.getSimpleName();
    private String pageTitle;

    public BottomSheetPage(final Context context) {
        super(context);
        setOnTouchListener(new OnSwipeTouchListener(context) {
            public void onSwipeTop() {
            }
            public void onSwipeRight() {
                StepperLayout.OnBackClickedCallback onBackClickedCallback =
                        setStepperLayout(). new OnBackClickedCallback();
                onBackClicked(onBackClickedCallback);
            }
            public void onSwipeLeft() {
                StepperLayout.OnNextClickedCallback onNextClickedCallback =
                        setStepperLayout(). new OnNextClickedCallback();
                onNextClicked(onNextClickedCallback);
            }
            public void onSwipeBottom() {
            }

        });
    }

    protected abstract StepperLayout setStepperLayout();

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }

    public BottomSheetPage setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
        return this;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    Step createStep(){
        return this;
    }

    @Override
    public void onSelected() {
    }


    @Override
    public void onNextClicked(final StepperLayout.OnNextClickedCallback callback) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.goToNextStep();
                callback.getStepperLayout().hideProgress();
            }
        }, 100);
    }

    @Override
    public void onCompleteClicked(final StepperLayout.OnCompleteClickedCallback callback) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.complete();
            }
        }, 100);

    }

    @Override
    public void onBackClicked(final StepperLayout.OnBackClickedCallback callback) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.goToPrevStep();
                callback.getStepperLayout().showProgress("");
            }
        }, 100);

    }

    public abstract void prepareView();
}
