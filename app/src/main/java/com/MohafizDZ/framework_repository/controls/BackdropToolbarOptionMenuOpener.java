package com.MohafizDZ.framework_repository.controls;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;

import com.MohafizDZ.empty_project.R;
import com.google.android.material.appbar.AppBarLayout;

public class BackdropToolbarOptionMenuOpener {

    private final AnimatorSet animatorSet = new AnimatorSet();
    private MenuItem menuItem;
    private Toolbar toolbar;
    private Context context;
    private View sheet;
    private Interpolator interpolator;
    private int height;
    private boolean backdropShown = false;
    private Drawable openIcon;
    private Drawable closeIcon;
    private int h;
    private OnBackDropMenuChangeListener onBackDropMenuChangeListener;


    //When you want to use full screen backdrop, use this , because the header will be seen at the bottom even when the backdrop contents are scrolled.
    public BackdropToolbarOptionMenuOpener(Context context, View sheet, Interpolator interpolator, Drawable openIcon, Drawable closeIcon, Toolbar toolbar) {
        this.context = context;
        this.sheet = sheet;
        this.interpolator = interpolator;
        this.openIcon = openIcon;
        this.closeIcon = closeIcon;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        //((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        this.toolbar = toolbar;
    }

    //When you want to use defined height backdrop use this, as i have defined custom height in this case for "PaymentStepsBackdrop.java" class.
    public BackdropToolbarOptionMenuOpener(Context context, View sheet, Interpolator interpolator, Drawable openIcon, Drawable closeIcon, int h, Toolbar toolbar, MenuItem menuItem) {
        this.context = context;
        this.sheet = sheet;
        this.interpolator = interpolator;
        this.openIcon = openIcon;
        this.closeIcon = closeIcon;
        this.h = h;
        this.toolbar = toolbar;
        this.menuItem = menuItem;
    }

    public void clickOnFilter() {
        backdropShown = !backdropShown;
        if(backdropShown) {
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
            params.setScrollFlags(0);
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL);
            toolbar.setLayoutParams(params);
        }else{
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
            params.setScrollFlags(0);
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
            toolbar.setLayoutParams(params);
        }

        animatorSet.removeAllListeners();
        animatorSet.end();
        animatorSet.cancel();

        updateIcon();

        int translateY;

        if (height > 0) {
            translateY = height - context.getResources().getDimensionPixelSize(R.dimen.backdrop_height1);
        } else {
            translateY = h;
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(sheet, "translationY", backdropShown ? translateY : 0);
        animator.setDuration(500);
        if (interpolator != null) {
            animator.setInterpolator(interpolator);
        }
        animatorSet.play(animator);
        animator.start();
        if(onBackDropMenuChangeListener != null){
            onBackDropMenuChangeListener.onBackDropMenuChange(isOpened());
        }
    }

    private void updateIcon() {
        if (backdropShown) {
            menuItem.setIcon(closeIcon);
//                ((ImageView) view).setImageDrawable(closeIcon);
        } else {
            menuItem.setIcon(openIcon);
//                ((ImageView) view).setImageDrawable(openIcon);
            }
        }

    public boolean isOpened(){
        return backdropShown;
    }

    public interface OnBackDropMenuChangeListener{
        void onBackDropMenuChange(boolean opened);
    }

    public void setOnBackDropMenuChangeListener(OnBackDropMenuChangeListener onBackDropMenuChangeListener) {
        this.onBackDropMenuChangeListener = onBackDropMenuChangeListener;
    }
}
