package com.MohafizDZ.project.home_dir.guide_presenter_dir;

import android.content.Context;

public abstract class ConcreteGuideDetails implements IGuideDetailsStrategy{
    protected final IGuidePresenter.Presenter presenter;
    protected final Context context;
    protected final Models models;


    protected ConcreteGuideDetails(IGuidePresenter.Presenter presenter, Context context, Models models) {
        this.presenter = presenter;
        this.context = context;
        this.models = models;
    }

    protected String getString(int resId){
        return context.getString(resId);
    }

    @Override
    public void onViewCreated(boolean guideDetailsVisible) {

    }
}
