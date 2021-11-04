package com.MohafizDZ.framework_repository.controls;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.PagerAdapter;

import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.adapter.AbstractStepAdapter;
import com.stepstone.stepper.adapter.StepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;
import java.util.List;

public abstract class BottomSheetStepperAdapter extends AbstractStepAdapter {
    public static final String TAG = BottomSheetStepperAdapter.class.getSimpleName();
    private static final String CURRENT_STEP_POSITION_KEY = "current_step";
    List<BottomSheetPage> pages;
    private Context mContext;
    BlockingStep currentStep;

    public BottomSheetStepperAdapter(@NonNull Context context,
                                     List<BottomSheetPage> pages) {
        super(context);
//        super(fragmentManager, context);
        this.mContext = context;
        this.pages = pages;
    }

    @Override
    public BottomSheetPage createStep(int position) {
        if(position < pages.size()) {
            BottomSheetPage step = pages.get(position);
            step.prepareView();
            Bundle b = new Bundle();
            b.putInt(CURRENT_STEP_POSITION_KEY, position);
            return step;
        }
        return null;
    }

    @Override
    public Step findStep(int position) {
        if(position < pages.size()) {
            return pages.get(position);
        }
        return null;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    protected String getCurrentName(int position){
        return "Page "+ position;
    }

    @NonNull
    @Override
    public StepViewModel getViewModel(int position) {
        if(position < pages.size()) {
            StepViewModel stepViewModel = onPageViewBind(pages.get(position));
            if (stepViewModel == null) {
                return new StepViewModel.Builder(mContext)
                        .setTitle(getCurrentName(position)) //can be a CharSequence instead
                        .create();
            }
            return stepViewModel;
        }
        return new StepViewModel.Builder(mContext)
                .setTitle(getCurrentName(position)) //can be a CharSequence instead
                .create();
    }

    protected abstract StepViewModel onPageViewBind(BottomSheetPage bottomSheetPage);

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        BottomSheetPage step = pages.get(position);
        container.addView(step);
        return step;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
