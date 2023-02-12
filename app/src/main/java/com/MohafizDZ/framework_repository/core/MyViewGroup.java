package com.MohafizDZ.framework_repository.core;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.MohafizDZ.framework_repository.local_sentry.GlobalTouchListener;
import com.MohafizDZ.framework_repository.local_sentry.Logger;

public class MyViewGroup extends FrameLayout {
    private Context context;
    private static int semaphore = 0;
    private static boolean enabled = true;

    private static GlobalTouchListener globalTouchListener;

    public GlobalTouchListener getGlobalTouchListener() {
        return globalTouchListener;
    }

    public void setGlobalTouchListener(GlobalTouchListener globalTouchListener) {
        this.globalTouchListener = globalTouchListener;
    }

    public MyViewGroup(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        try {
            super.onLayout(changed, l, t, r, b);
        }catch (Exception ignored){}
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN && isTouchEnabled()){
            enableTouch(false);
            int x = (int) event.getX();
            int y = (int) event.getY();
            String log = null;
            if(globalTouchListener != null && globalTouchListener.startTrack()) {
                log = globalTouchListener.ClickedOn(x, y);
                Logger logger = new Logger(context);
                logger.startWriting(log);
            }
            log = log == null ? "Click X:" + event.getX() + " Y:" + event.getY() : log;
            Log.d("Fragment_touch", log);
            this.postDelayed(new Runnable() {
                @Override
                public void run() {
                    enableTouch(true);

                }
            }, 500);
        }
        return super.onInterceptTouchEvent(event);
    }

    private void enableTouch(boolean enabled){
        this.enabled = enabled;
    }

    private boolean isTouchEnabled(){
        return enabled;
    }

    public int getRelativeLeft(View myView) {
        if (myView.getParent() == this)
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft((View) myView.getParent());
    }

    public int getRelativeTop(View myView) {
        int top = myView.getTop();
        if(myView instanceof ScrollView){
            top -= myView.getScrollY();
        }
        if (myView.getParent() == this)
            return top;
        else
            return top + getRelativeTop((View) myView.getParent());
    }
}
