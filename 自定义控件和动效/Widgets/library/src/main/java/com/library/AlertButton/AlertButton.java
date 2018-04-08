package com.library.AlertButton;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.library.R;
import com.library.utils.Utils;

public class AlertButton extends View {
    private static final int STOP_MENU_GAP = 24;    // 关闭按钮与屏幕边缘距离 24

    private static final int[] ALARM_START_COLOR = new int[]{255, 255, 255, 255};

    private static final int[] ALARM_TO_COLOR = new int[]{102, 254, 56, 36};

    private static final int[] ALARM_EQUAL_CIRCLE_START_COLOR = new int[]{0, 255, 255, 255};

    private static final int[] ALARM_EQUAL_CIRCLE_TO_COLOR = new int[]{25, 254, 56, 36};

    private static final int[] SHADOW_CIRCLE_START_COLOR = new int[]{51, 255, 255, 255};

    private static final int MSG_REFRESH_ANIMATION = 0;

    private static final int ANIMATION_DURATION = 25;

    private static final int RESTORE_ANIMATION_DURATION = 300;

    private static final int ALARM_EQUALCIRCLE_RADIUS = 11;

    private static final int ALARM_SHADOW_CIRCLE_RADIUS = 33;

    private Drawable mAlarmCircle;

    private Drawable mAlarmEar;
    private float mAlarmEarAngle = 20;

    private Drawable mAlarmBody;
    private int mAlarmAlpha = 255;
    private float mAlarmScale = 1.0f;
    private int mAlarmColor;

    private Paint mAlarmEqualCirclePaint;
    private int mAlarmEqualCircleColor;
    private float mAlarmEqualCircleRadius;
    private float mAlarmEqualCircleScale = 0.0f;

    private Paint mShadowCirclePaint;
    private int mShadowCircleColor;
    private float mShadowRadius;
    private boolean mShadowCircleShow;

    private Drawable mCancel;
    private float mCancelScale = 0.0f;

    private float mMaxMoveDistance;
    private float mCircleScale;

    private ValueAnimator mAlarmAnimator;

    private ValueAnimator mRestoreAnimator;

    private Context mContext;

    public AlertButton(Context context) {
        super(context);
    }

    public AlertButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mAlarmEar = getResources().getDrawable(R.drawable.ic_alarm_ear, context.getTheme());
        mAlarmBody = getResources().getDrawable(R.drawable.ic_alarm_body, context.getTheme());
        mCancel = getResources().getDrawable(R.drawable.btn_cancel, context.getTheme());
        mAlarmCircle = getResources().getDrawable(R.drawable.ic_alarm_circle, context.getTheme());
        mAlarmColor = getResources().getColor(R.color.white, context.getTheme());

        mAlarmEqualCirclePaint = new Paint();
        mAlarmEqualCircleColor = getResources().getColor(R.color.white, context.getTheme());
        mAlarmEqualCirclePaint.setColor(mAlarmEqualCircleColor);
        mAlarmEqualCirclePaint.setAntiAlias(true);

        mShadowCirclePaint = new Paint();
        mShadowCircleColor = getResources().getColor(R.color.white_20, context.getTheme());
        mShadowCirclePaint.setColor(mShadowCircleColor);
        mShadowCirclePaint.setAntiAlias(true);

        mContext = context;

        this.post(() -> {
            mMaxMoveDistance = getResources().getDisplayMetrics().widthPixels / 2
                    - Utils.dp2px(context, STOP_MENU_GAP) - getWidth() / 2;
            mAlarmEqualCircleRadius = Utils.dp2px(context, ALARM_EQUALCIRCLE_RADIUS);
            mShadowRadius = Utils.dp2px(context, ALARM_SHADOW_CIRCLE_RADIUS);
            mCircleScale = mShadowRadius / mAlarmEqualCircleRadius;
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_REFRESH_ANIMATION:
                    startAlarmAnimation();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int availableWidth = getWidth();
        int availableHeight = getHeight();

        int x = availableWidth / 2;
        int y = availableHeight / 2;

        if (mShadowCircleShow) {
            canvas.save();
            mShadowCirclePaint.setColor(mShadowCircleColor);
            canvas.drawCircle(x, y, mShadowRadius, mShadowCirclePaint);
            canvas.restore();
        }

        final Drawable alarmCircle = mAlarmCircle;
        drawAlarmCircle(canvas, alarmCircle, x, y, mAlarmColor);

        final Drawable alarmEar = mAlarmEar;
        drawAlarm(canvas, alarmEar, x, y, mAlarmScale, mAlarmAlpha, mAlarmEarAngle);

        final Drawable alarmBody = mAlarmBody;
        drawAlarm(canvas, alarmBody, x, y, mAlarmScale, mAlarmAlpha, 0);

        canvas.save();
        canvas.scale(mAlarmEqualCircleScale, mAlarmEqualCircleScale, x, y);
        mAlarmEqualCirclePaint.setColor(mAlarmEqualCircleColor);
        canvas.drawCircle(x, y, mAlarmEqualCircleRadius, mAlarmEqualCirclePaint);
        canvas.restore();

        final Drawable cancel = mCancel;
        drawCancel(canvas, cancel, x, y, mCancelScale);
    }

    private void drawAlarm(Canvas canvas, Drawable drawable, int x, int y, float scale, int alpha, float angle) {
        canvas.save();
        canvas.rotate(angle, x, y);
        canvas.scale(scale, scale, x, y);
        drawable.setAlpha(alpha);
        final int w = drawable.getIntrinsicWidth();
        final int h = drawable.getIntrinsicHeight();
        drawable.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        drawable.draw(canvas);
        canvas.restore();
    }

    private void drawCancel(Canvas canvas, Drawable drawable, int x, int y, float scale) {
        canvas.save();
        canvas.scale(scale, scale, x, y);
        drawable.setTint(getResources().getColor(R.color.alarm_alert_btn_red));
        final int w = drawable.getIntrinsicWidth();
        final int h = drawable.getIntrinsicHeight();
        drawable.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        drawable.draw(canvas);
        canvas.restore();
    }

    private void drawAlarmCircle(Canvas canvas, Drawable drawable, int x, int y, int color) {
        canvas.save();
        drawable.setTint(color);
        final int w = drawable.getIntrinsicWidth();
        final int h = drawable.getIntrinsicHeight();
        drawable.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        drawable.draw(canvas);
        canvas.restore();
    }

    public void updateViewWithDistance(float x) {
        float d = Math.abs(x) / mMaxMoveDistance;

        mShadowCircleColor = d <= 0.25 ? getGradientColor(4 * d, SHADOW_CIRCLE_START_COLOR,
                ALARM_EQUAL_CIRCLE_START_COLOR) : Color.argb(0, 255, 255, 255);

        mAlarmAlpha = d >= 0.25 ? 0 : (int) (255 - 1020 * d);
        mAlarmScale = d >= 1 ? mCircleScale : (1.0f + (mCircleScale - 1.0f) * d);
        mAlarmColor = d >= 1 ? getResources().getColor(R.color.alarm_alert_btn_red_40, mContext.getTheme()) :
                getGradientColor(d, ALARM_START_COLOR, ALARM_TO_COLOR);

        mAlarmEqualCircleScale = mAlarmScale;
        if (d <= 0.25f) {
            mAlarmEqualCircleColor = getGradientColor(4 * d, ALARM_EQUAL_CIRCLE_START_COLOR,
                    ALARM_START_COLOR);
        } else if (d <= 1.0f && d > 0.25f) {
            mAlarmEqualCircleColor = getGradientColor((4 * d - 1) / 3, ALARM_START_COLOR,
                    ALARM_EQUAL_CIRCLE_TO_COLOR);
        } else if (d > 1.0f) {
            mAlarmEqualCircleColor = getResources().getColor(R.color.alarm_alert_btn_red_10,
                    mContext.getTheme());
        }

        mCancelScale = d >= 1 ? 1.0f : d;
        invalidate();

        setTranslationX(x);
    }

    private void resetState() {
        mAlarmAlpha = 255;
        mAlarmScale = 1.0f;
        mAlarmColor = getResources().getColor(R.color.white, mContext.getTheme());
        mAlarmEarAngle = 0;

        mAlarmEqualCircleColor = getResources().getColor(R.color.white, mContext.getTheme());
        mAlarmEqualCircleScale = 0.0f;

        mShadowCircleColor = getResources().getColor(R.color.white_20, mContext.getTheme());

        mCancelScale = 0.0f;

        invalidate();
    }

    // 图片颜色渐变
    private int getGradientColor(float diff, int[] startColor, int[] toColor) {

        return Color.argb(gradientColor(diff, startColor[0], toColor[0]),
                gradientColor(diff, startColor[1], toColor[1]),
                gradientColor(diff, startColor[2], toColor[2]),
                gradientColor(diff, startColor[3], toColor[3]));
    }

    // 渐变颜色计算
    private int gradientColor(float diff, int startColor, int toColor) {

        return (int) ((toColor - startColor) * diff + startColor);
    }

    private int flag = 1;

    public void startAlarmAnimation() {
        mAlarmAnimator = ValueAnimator.ofFloat(0, 20 * flag, 0);
        mAlarmAnimator.setDuration(ANIMATION_DURATION);
        mAlarmAnimator.setInterpolator(PathInterpolatorCompat.create(0.33f, 0,
                0.67f, 1));
        mAlarmAnimator.addUpdateListener(valueAnimator -> {
            mAlarmEarAngle = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        mAlarmAnimator.addListener(new Animator.AnimatorListener() {
            boolean cancel;

            @Override
            public void onAnimationStart(Animator animator) {
                cancel = false;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!cancel) {
                    flag *= -1;
                    mHandler.sendEmptyMessage(MSG_REFRESH_ANIMATION);
                } else {
                    resetState();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                cancel = true;
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        mAlarmAnimator.start();
    }

    public void stopAlarmAnimation() {
        if (mAlarmAnimator != null && mAlarmAnimator.isRunning()) {
            mAlarmAnimator.cancel();
        }

        if (mRestoreAnimator != null && mRestoreAnimator.isRunning()) {
            mRestoreAnimator.cancel();
        }

        mHandler.removeMessages(MSG_REFRESH_ANIMATION);
        this.clearAnimation();
        invalidate();
    }

    private void startRestoreAnimation() {
        mRestoreAnimator = ValueAnimator.ofFloat(getTranslationX(), 0);
        mRestoreAnimator.setDuration(RESTORE_ANIMATION_DURATION);
        mRestoreAnimator.addUpdateListener(valueAnimator -> {
            updateViewWithDistance((float) valueAnimator.getAnimatedValue());
        });
        mRestoreAnimator.addListener(new Animator.AnimatorListener() {
            boolean cancel;
            @Override
            public void onAnimationStart(Animator animator) {
                cancel = false;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!cancel) {
                    resetState();
                    mAlarmAnimator.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                cancel = true;
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        mRestoreAnimator.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAlarmAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAlarmAnimation();
    }

    private float mLastX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mShadowCircleShow = true;
                mLastX = event.getRawX();
                stopAlarmAnimation();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (Math.abs(event.getRawX() - mLastX) >= mMaxMoveDistance) {
                    //TODO
                }
                mLastX = 0;
                mShadowCircleShow = false;
                startRestoreAnimation();
                break;
            case MotionEvent.ACTION_MOVE:
                float x;
                if (Math.abs(event.getRawX() - mLastX) <= mMaxMoveDistance) {
                    x = event.getRawX() - mLastX;
                } else {
                    x = event.getRawX() - mLastX > 0 ? mMaxMoveDistance : -mMaxMoveDistance;
                }
                updateViewWithDistance(x);
                break;
        }
        return true;
    }
}
