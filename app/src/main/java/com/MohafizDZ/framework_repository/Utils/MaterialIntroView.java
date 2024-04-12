package com.MohafizDZ.framework_repository.Utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.MohafizDZ.empty_project.R;

import co.mobiwise.materialintro.MaterialIntroConfiguration;
import co.mobiwise.materialintro.animation.AnimationFactory;
import co.mobiwise.materialintro.animation.AnimationListener;
import co.mobiwise.materialintro.prefs.PreferencesManager;
import co.mobiwise.materialintro.shape.Circle;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.shape.Rect;
import co.mobiwise.materialintro.shape.Shape;
import co.mobiwise.materialintro.shape.ShapeType;
import co.mobiwise.materialintro.target.Target;
import co.mobiwise.materialintro.target.ViewTarget;
import co.mobiwise.materialintro.utils.Constants;
import co.mobiwise.materialintro.utils.Utils;

public class MaterialIntroView extends RelativeLayout {

    /**
     * Mask color
     */
    private int maskColor;

    /**
     * MaterialIntroView will start
     * showing after delayMillis seconds
     * passed
     */
    private long delayMillis;

    /**
     * We don't draw MaterialIntroView
     * until isReady field set to true
     */
    private boolean isReady;

    /**
     * Show/Dismiss MaterialIntroView
     * with fade in/out animation if
     * this is enabled.
     */
    private boolean upDownAnimation;

    /**
     * Show/Dismiss MaterialIntroView
     * with fade in/out animation if
     * this is enabled.
     */
    private boolean isFadeAnimationEnabled;

    /**
     * Animation duration
     */
    private long fadeAnimationDuration;

    /**
     * targetShape focus on target
     * and clear circle to focus
     */
    private Shape targetShape;

    /**
     * Focus Type
     */
    private Focus focusType;

    /**
     * FocusGravity type
     */
    private FocusGravity focusGravity;

    /**
     * Target View
     */
    private Target targetView;

    /**
     * Eraser
     */
    private Paint eraser;

    /**
     * Handler will be used to
     * delay MaterialIntroView
     */
    private Handler handler;

    /**
     * All views will be drawn to
     * this bitmap and canvas then
     * bitmap will be drawn to canvas
     */
    private Bitmap bitmap;
    private Canvas canvas;

    /**
     * Circle padding
     */
    private int padding;

    /**
     * Layout width/height
     */
    private int width;
    private int height;

    /**
     * Dismiss on touch any position
     */
    private boolean dismissOnTouch;

    /**
     * Info dialog view
     */
    private View infoView;

    /**
     * Info Dialog Text
     */
    private TextView textViewInfo;

    /**
     * Info dialog text color
     */
    private int colorTextViewInfo;

    /**
     * Info dialog will be shown
     * If this value true
     */
    private boolean isInfoEnabled;

    /**
     * Dot view will appear center of
     * cleared target area
     */
    private View dotView;

    /**
     * Dot View will be shown if
     * this is true
     */
    private boolean isDotViewEnabled;

    /**
     * Info Dialog Icon
     */
    private ImageView imageViewIcon;

    /**
     * Image View will be shown if
     * this is true
     */
    private boolean isImageViewEnabled;

    /**
     * Save/Retrieve status of MaterialIntroView
     * If Intro is already learnt then don't show
     * it again.
     */
    private PreferencesManager preferencesManager;

    /**
     * Check using this Id whether user learned
     * or not.
     */
    private String materialIntroViewId;

    /**
     * When layout completed, we set this true
     * Otherwise onGlobalLayoutListener stuck on loop.
     */
    private boolean isLayoutCompleted;

    /**
     * Notify user when MaterialIntroView is dismissed
     */
    private MaterialIntroListener materialIntroListener;

    /**
     * Perform click operation to target
     * if this is true
     */
    private boolean isPerformClick;

    /**
     * Disallow this MaterialIntroView from showing up more than once at a time
     */
    private boolean isIdempotent;

    /**
     * Shape of target
     */
    private ShapeType shapeType;

    /**
     * Use custom shape
     */
    private boolean usesCustomShape = false;

    private boolean alwaysShow = false;
    private Context mContext;

    private int customCursorShapeLayout;
    private View skipView;
    private boolean forceShow;
    protected boolean skippable = true;

    private void setAlwaysShow(boolean alwaysShow) {
        this.alwaysShow = alwaysShow;
    }

    private void forceShow(boolean forceShow) {
        this.forceShow = forceShow;
    }

    private void setSkippable(boolean skippable) {
        this.skippable = skippable;
    }

    public MaterialIntroView(Context context) {
        this(context, 0);
    }

    public MaterialIntroView(Context context, int customCursorShapeLayout) {
        super(context);
        this.mContext = context;
        if(customCursorShapeLayout != 0) {
            this.customCursorShapeLayout = customCursorShapeLayout;
        }
        init(context);
    }

    public MaterialIntroView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MaterialIntroView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialIntroView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        setVisibility(INVISIBLE);

        /**
         * set default values
         */
        maskColor = Constants.DEFAULT_MASK_COLOR;
        delayMillis = Constants.DEFAULT_DELAY_MILLIS;
        fadeAnimationDuration = Constants.DEFAULT_FADE_DURATION;
        padding = Constants.DEFAULT_TARGET_PADDING;
        colorTextViewInfo = Constants.DEFAULT_COLOR_TEXTVIEW_INFO;
        focusType = Focus.ALL;
        focusGravity = FocusGravity.CENTER;
        shapeType = ShapeType.CIRCLE;
        isReady = false;
        isFadeAnimationEnabled = true;
        dismissOnTouch = false;
        isLayoutCompleted = false;
        isInfoEnabled = false;
        isDotViewEnabled = false;
        isPerformClick = false;
        isImageViewEnabled = true;
        isIdempotent = false;

        /**
         * initialize objects
         */
        handler = new Handler();

        preferencesManager = new PreferencesManager(context);

        eraser = new Paint();
        eraser.setColor(0xFFFFFFFF);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        eraser.setFlags(Paint.ANTI_ALIAS_FLAG);

        View layoutInfo = LayoutInflater.from(getContext()).inflate(R.layout.material_intro_card, null);

        infoView = layoutInfo.findViewById(R.id.info_layout);
        textViewInfo = (TextView) layoutInfo.findViewById(R.id.textview_info);
        textViewInfo.setTextColor(colorTextViewInfo);
        imageViewIcon = (ImageView) layoutInfo.findViewById(R.id.imageview_icon);
        if(customCursorShapeLayout != 0) {
            dotView = LayoutInflater.from(getContext()).inflate(customCursorShapeLayout, null);
        }else{
            dotView = LayoutInflater.from(getContext()).inflate(R.layout.dotview, null);
        }
        dotView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                targetShape.reCalculateAll();
                if (targetShape != null && targetShape.getPoint().y != 0 && !isLayoutCompleted) {
                    if (isInfoEnabled)
                        setInfoLayout();
                    if(isDotViewEnabled)
                        setDotViewLayout();
                    removeOnGlobalLayoutListener(MaterialIntroView.this, this);
                }
            }
        });
        skipView = LayoutInflater.from(getContext()).inflate(R.layout.skip_view, null);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // Set alignment to bottom right
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.bottomMargin = MyUtil.dpToPx(20);
        params.addRule(RelativeLayout.ALIGN_PARENT_END); // or ALIGN_PARENT_RIGHT
        addView(skipView, params);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
        v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isReady) return;

        if (bitmap == null || canvas == null) {
            if (bitmap != null) bitmap.recycle();

            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            this.canvas = new Canvas(bitmap);
        }

        /**
         * Draw mask
         */
        this.canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        this.canvas.drawColor(maskColor);

        /**
         * Clear focus area
         */
        targetShape.draw(this.canvas, eraser, padding);

        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    /**
     * Perform click operation when user
     * touches on target circle.
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float xT = event.getX();
        float yT = event.getY();

        boolean isTouchOnFocus = targetShape.isTouchOnFocus(xT, yT);
        boolean isTouchOnSkipFocus = skippable && isTouchOnSkipFocus(xT, yT);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if (isTouchOnFocus && isPerformClick) {
                    targetView.getView().setPressed(true);
                    targetView.getView().invalidate();
                }

                return true;
            case MotionEvent.ACTION_UP:

                if (isTouchOnFocus || isTouchOnSkipFocus || dismissOnTouch)
                    dismiss();

                if (isTouchOnFocus && isPerformClick) {
                    targetView.getView().performClick();
                    targetView.getView().setPressed(true);
                    targetView.getView().invalidate();
                    targetView.getView().setPressed(false);
                    targetView.getView().invalidate();
                }
                if(isTouchOnSkipFocus && isPerformClick){
                    if(materialIntroListener != null){
                        materialIntroListener.onSkip();
                    }
                }

                return true;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    public boolean isTouchOnSkipFocus(double x, double y) {
        int[] location = new int[2];
        View view = skipView.findViewById(R.id.skipContainer);
        view.getLocationInWindow(location);
        android.graphics.Rect rect = new android.graphics.Rect(
                location[0],
                location[1],
                location[0] + view.getWidth(),
                location[1] + view.getHeight()
        );
        RectF rectF = new RectF();
        rectF.set(rect);

        rectF.left -= padding;
        rectF.top -= padding;
        rectF.right += padding;
        rectF.bottom += padding;
        return rectF.contains((float) x, (float) y);
    }


    /**
     * Shows material view with fade in
     * animation
     *
     * @param activity
     */
    private void show(Activity activity) {

        if (preferencesManager.isDisplayed(materialIntroViewId) && !forceShow)
            return;

        ((ViewGroup) activity.getWindow().getDecorView()).addView(this);

        setReady(true);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFadeAnimationEnabled)
                    AnimationFactory.animateFadeIn(MaterialIntroView.this, fadeAnimationDuration, new AnimationListener.OnAnimationStartListener() {
                        @Override
                        public void onAnimationStart() {
                            setVisibility(VISIBLE);
                        }
                    });
                else
                    setVisibility(VISIBLE);
            }
        }, delayMillis);

        if(!alwaysShow && isIdempotent) {
            preferencesManager.setDisplayed(materialIntroViewId);
        }
    }

    /**
     * Dismiss Material Intro View
     */
    public void dismiss() {
        if(!alwaysShow && !isIdempotent) {
            preferencesManager.setDisplayed(materialIntroViewId);
        }

        AnimationFactory.animateFadeOut(this, fadeAnimationDuration, new AnimationListener.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                setVisibility(GONE);
                removeMaterialView();

                if (materialIntroListener != null)
                    materialIntroListener.onUserClicked(materialIntroViewId);
            }
        });
    }

    private void removeMaterialView(){
        if(getParent() != null )
            ((ViewGroup) getParent()).removeView(this);
    }

    /**
     * locate info card view above/below the
     * circle. If circle's Y coordiante is bigger than
     * Y coordinate of root view, then locate cardview
     * above the circle. Otherwise locate below.
     */
    private void setInfoLayout() {

        handler.post(new Runnable() {
            @Override
            public void run() {
                isLayoutCompleted = true;

                if (infoView.getParent() != null)
                    ((ViewGroup) infoView.getParent()).removeView(infoView);

                RelativeLayout.LayoutParams infoDialogParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT);

                if (targetShape.getPoint().y < height / 2) {
                    ((RelativeLayout) infoView).setGravity(Gravity.TOP);
                    infoDialogParams.setMargins(
                            0,
                            targetShape.getPoint().y + targetShape.getHeight() / 2,
                            0,
                            0);
                } else {
                    ((RelativeLayout) infoView).setGravity(Gravity.BOTTOM);
                    infoDialogParams.setMargins(
                            0,
                            0,
                            0,
                            height - (targetShape.getPoint().y + targetShape.getHeight() / 2) + 2 * targetShape.getHeight() / 2);
                }

                infoView.setLayoutParams(infoDialogParams);
                infoView.postInvalidate();

                addView(infoView);

                if (!isImageViewEnabled){
                    imageViewIcon.setVisibility(GONE);
                }

                infoView.setVisibility(VISIBLE);
            }
        });
    }

    private void setDotViewLayout() {

        handler.post(new Runnable() {
            @Override
            public void run() {

                if (dotView.getParent() != null)
                    ((ViewGroup) dotView.getParent()).removeView(dotView);

                RelativeLayout.LayoutParams dotViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                dotViewLayoutParams.height = Utils.dpToPx(Constants.DEFAULT_DOT_SIZE);
                dotViewLayoutParams.width = Utils.dpToPx(Constants.DEFAULT_DOT_SIZE);
                dotViewLayoutParams.setMargins(
                        targetShape.getPoint().x - (dotViewLayoutParams.width / 2),
                        targetShape.getPoint().y - (dotViewLayoutParams.height / 2),
                        0,
                        0);
                dotView.setLayoutParams(dotViewLayoutParams);
                dotView.postInvalidate();
                addView(dotView);

                dotView.setVisibility(VISIBLE);
                com.MohafizDZ.framework_repository.Utils.AnimationFactory.performAnimation(dotView);
            }
        });
    }

    /**
     * SETTERS
     */

    private void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
    }

    private void setDelay(int delayMillis) {
        this.delayMillis = delayMillis;
    }

    private void enableFadeAnimation(boolean isFadeAnimationEnabled) {
        this.isFadeAnimationEnabled = isFadeAnimationEnabled;
    }

    private void enableUpDownAnimation(boolean upDownAnimation) {
        this.upDownAnimation = upDownAnimation;
    }

    private void setShapeType(ShapeType shape) {
        this.shapeType = shape;
    }

    private void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    private void setTarget(Target target) {
        targetView = target;
    }

    private void setFocusType(Focus focusType) {
        this.focusType = focusType;
    }

    private void setShape(Shape shape) {
        this.targetShape = shape;
    }

    private void setPadding(int padding) {
        this.padding = padding;
    }

    private void setDismissOnTouch(boolean dismissOnTouch) {
        this.dismissOnTouch = dismissOnTouch;
    }

    private void setFocusGravity(FocusGravity focusGravity) {
        this.focusGravity = focusGravity;
    }

    private void setColorTextViewInfo(int colorTextViewInfo) {
        this.colorTextViewInfo = colorTextViewInfo;
        textViewInfo.setTextColor(this.colorTextViewInfo);
    }

    private void setTextViewInfo(String textViewInfo) {
        this.textViewInfo.setText(textViewInfo);
    }

    private void setTextViewInfoSize(int textViewInfoSize) {
        this.textViewInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, textViewInfoSize);
    }

    private void enableInfoDialog(boolean isInfoEnabled) {
        this.isInfoEnabled = isInfoEnabled;
    }

    private void enableImageViewIcon(boolean isImageViewEnabled){
        this.isImageViewEnabled = isImageViewEnabled;
    }

    private void setIdempotent(boolean idempotent){
        this.isIdempotent = idempotent;
    }

    private void enableDotView(boolean isDotViewEnabled){
        this.isDotViewEnabled = isDotViewEnabled;
    }

    public void setConfiguration(MaterialIntroConfiguration configuration) {

        if (configuration != null) {
            this.maskColor = configuration.getMaskColor();
            this.delayMillis = configuration.getDelayMillis();
            this.isFadeAnimationEnabled = configuration.isFadeAnimationEnabled();
            this.colorTextViewInfo = configuration.getColorTextViewInfo();
            this.isDotViewEnabled = configuration.isDotViewEnabled();
            this.dismissOnTouch = configuration.isDismissOnTouch();
            this.colorTextViewInfo = configuration.getColorTextViewInfo();
            this.focusType = configuration.getFocusType();
            this.focusGravity = configuration.getFocusGravity();
        }
    }

    private void setUsageId(String materialIntroViewId) {
        this.materialIntroViewId = materialIntroViewId;
    }

    private void setListener(MaterialIntroListener materialIntroListener) {
        this.materialIntroListener = materialIntroListener;
    }

    private void setPerformClick(boolean isPerformClick){
        this.isPerformClick = isPerformClick;
    }

    /**
     * Builder Class
     */
    public static class Builder {

        private MaterialIntroView materialIntroView;

        private Activity activity;

        private Focus focusType = Focus.MINIMUM;

        public Builder(Activity activity) {
            this.activity = activity;
            materialIntroView = new MaterialIntroView(activity);
        }

        public Builder(Activity activity, int cursorLayoutId) {
            this.activity = activity;
            materialIntroView = new MaterialIntroView(activity, cursorLayoutId);
        }

        public MaterialIntroView.Builder setMaskColor(int maskColor) {
            materialIntroView.setMaskColor(maskColor);
            return this;
        }

        public MaterialIntroView.Builder setDelayMillis(int delayMillis) {
            materialIntroView.setDelay(delayMillis);
            return this;
        }

        public MaterialIntroView.Builder enableFadeAnimation(boolean isFadeAnimationEnabled) {
            materialIntroView.enableFadeAnimation(isFadeAnimationEnabled);
            return this;
        }

        public MaterialIntroView.Builder setShape(ShapeType shape) {
            materialIntroView.setShapeType(shape);
            return this;
        }

        public MaterialIntroView.Builder setFocusType(Focus focusType) {
            materialIntroView.setFocusType(focusType);
            return this;
        }

        public MaterialIntroView.Builder setFocusGravity(FocusGravity focusGravity) {
            materialIntroView.setFocusGravity(focusGravity);
            return this;
        }

        public MaterialIntroView.Builder setTarget(View view) {
            materialIntroView.setTarget(new ViewTarget(view));
            return this;
        }

        public MaterialIntroView.Builder setTargetPadding(int padding) {
            materialIntroView.setPadding(padding);
            return this;
        }

        public MaterialIntroView.Builder setTextColor(int textColor) {
            materialIntroView.setColorTextViewInfo(textColor);
            return this;
        }

        public MaterialIntroView.Builder setInfoText(String infoText) {
            materialIntroView.enableInfoDialog(true);
            materialIntroView.setTextViewInfo(infoText);
            return this;
        }

        public MaterialIntroView.Builder setInfoTextSize(int textSize) {
            materialIntroView.setTextViewInfoSize(textSize);
            return this;
        }

        public MaterialIntroView.Builder dismissOnTouch(boolean dismissOnTouch) {
            materialIntroView.setDismissOnTouch(dismissOnTouch);
            return this;
        }

        public MaterialIntroView.Builder setSkippable(boolean skippable) {
            materialIntroView.setSkippable(skippable);
            return this;
        }

        public MaterialIntroView.Builder setUsageId(String materialIntroViewId) {
            materialIntroView.setUsageId(materialIntroViewId);
            return this;
        }

        public MaterialIntroView.Builder enableDotAnimation(boolean isDotAnimationEnabled) {
            materialIntroView.enableDotView(isDotAnimationEnabled);
            return this;
        }

        public MaterialIntroView.Builder enableIcon(boolean isImageViewIconEnabled) {
            materialIntroView.enableImageViewIcon(isImageViewIconEnabled);
            return this;
        }

        public MaterialIntroView.Builder setIdempotent(boolean idempotent) {
            materialIntroView.setIdempotent(idempotent);
            return this;
        }

        public MaterialIntroView.Builder setConfiguration(MaterialIntroConfiguration configuration) {
            materialIntroView.setConfiguration(configuration);
            return this;
        }

        public MaterialIntroView.Builder setListener(MaterialIntroListener materialIntroListener) {
            materialIntroView.setListener(materialIntroListener);
            return this;
        }

        public MaterialIntroView.Builder setCustomShape(Shape shape) {
            materialIntroView.usesCustomShape = true;
            materialIntroView.setShape(shape);
            return this;
        }

        public MaterialIntroView.Builder performClick(boolean isPerformClick){
            materialIntroView.setPerformClick(isPerformClick);
            return this;
        }

        public MaterialIntroView.Builder alwaysShow(boolean alwaysShow){
            materialIntroView.setAlwaysShow(alwaysShow);
            return this;
        }

        public MaterialIntroView.Builder forceShow(boolean forceShow){
            materialIntroView.forceShow(forceShow);
            return this;
        }

        public MaterialIntroView build() {
            if(materialIntroView.usesCustomShape) {
                return materialIntroView;
            }

            // no custom shape supplied, build our own
            Shape shape;

            if(materialIntroView.shapeType == ShapeType.CIRCLE) {
                shape = new Circle(
                        materialIntroView.targetView,
                        materialIntroView.focusType,
                        materialIntroView.focusGravity,
                        materialIntroView.padding);
            } else {
                shape = new Rect(
                        materialIntroView.targetView,
                        materialIntroView.focusType,
                        materialIntroView.focusGravity,
                        materialIntroView.padding);
            }

            materialIntroView.setShape(shape);
            return materialIntroView;
        }

        public MaterialIntroView show() {
            build().show(activity);
            if(materialIntroView.skippable) {
                materialIntroView.skipView.findViewById(R.id.skipContainer).setVisibility(VISIBLE);
            }else{
                materialIntroView.skipView.findViewById(R.id.skipContainer).setVisibility(GONE);
            }
            return materialIntroView;
        }

    }

}
