package com.MohafizDZ.framework_repository.Utils;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;


import com.MohafizDZ.own_distributor.R;

import co.mobiwise.materialintro.animation.AnimationListener;

public class AnimationFactory {

    /**
     * MaterialIntroView will appear on screen with
     * fade in animation. Notifies onAnimationStartListener
     * when fade in animation is about to start.
     *
     * @param view
//     * @param duration
     * @param onAnimationStartListener
     */
    public static void animateUpDown(Context context, View view, final AnimationListener.OnAnimationStartListener onAnimationStartListener) {
        ObjectAnimator animatorSet = (ObjectAnimator) AnimatorInflater.loadAnimator(context, R.animator.up_down_anim);
        animatorSet.setTarget(view);
        animatorSet.start();
    }

    public static void animateFadeIn(View view, long duration, final AnimationListener.OnAnimationStartListener onAnimationStartListener) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        objectAnimator.setDuration(duration);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (onAnimationStartListener != null)
                    onAnimationStartListener.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.start();
    }

    /**
     * MaterialIntroView will disappear from screen with
     * fade out animation. Notifies onAnimationEndListener
     * when fade out animation is ended.
     *
     * @param view
     * @param duration
     * @param onAnimationEndListener
     */
    public static void animateFadeOut(View view, long duration, final AnimationListener.OnAnimationEndListener onAnimationEndListener) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 1, 0);
        objectAnimator.setDuration(duration);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationEndListener != null)
                    onAnimationEndListener.onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.start();
    }

    public static void performAnimation(View view) {
        ValueAnimator moveUp = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, 50f);
        moveUp.setDuration(1000);
        moveUp.setRepeatCount(ValueAnimator.INFINITE);
        moveUp.setRepeatMode(ValueAnimator.REVERSE);
        moveUp.setInterpolator(new DecelerateInterpolator());

        ValueAnimator moveDown = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 50f, 0f);
        moveDown.setDuration(1000);
        moveDown.setRepeatCount(ValueAnimator.INFINITE);
        moveDown.setRepeatMode(ValueAnimator.REVERSE);
        moveDown.setInterpolator(new AccelerateInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(moveDown, moveUp);

// Set up an infinite loop for the animation
//        animatorSet.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                try {
//                    super.onAnimationEnd(animation);
//                    animatorSet.start(); // Restart the animation when it finishes
//                }catch (Exception ignored){}
//            }
//        });
        animatorSet.start();
    }

}
