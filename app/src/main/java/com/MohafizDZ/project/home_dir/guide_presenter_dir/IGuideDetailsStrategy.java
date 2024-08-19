package com.MohafizDZ.project.home_dir.guide_presenter_dir;

public interface IGuideDetailsStrategy {
    String setDescription();
    boolean setButtonVisibility();
    String setButtonTitle();
    boolean setStepVisibility();
    String setStepTitle();
    int setImageDrawable();
    void onClickOnAction();

    String getTag();

    void onViewCreated(boolean guideDetailsVisible);
}
