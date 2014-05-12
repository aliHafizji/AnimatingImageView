package com.ali.animatingimageview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.Random;

/**
 * Created by kauserali on 06/05/14.
 */
public class AnimatingImageView extends ImageView {

    private static final int DEFAULT_START_DELAY = 0;
    private static final int DEFAULT_ANIMATION_PER_PIXEL_TRANSLATE_DURATION = 18;
    private static final int DEFAULT_START_INDEX = 0;
    private static final int DEFAULT_FADE_IN_DURATION = 250;
    private static final int DEFAULT_FADE_OUT_DURATION = 300;

    private int mPrevTranslateX, mPrevTranslateY, mStartDelay, mTranslateDurationPerPixel, mStartIndex, mCurrentIndex, mFadeInDuration, mFadeOutDuration;
    private int[] mDrawablesResourceIds;
    private boolean mIsAnimationRunning, mStartAnimation;

    private TimeInterpolator mInterpolator;
    private AnimatorSet mAnimation;
    private Random mRandom;

    public AnimatingImageView(Context context) {
        super(context);
        init();
    }

    public AnimatingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatingImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public boolean isAnimating() {
        return mIsAnimationRunning;
    }

    public void startAnimation() {
        mStartAnimation = true;
        configureBounds();
    }

    public void stopAnimation() {
        if (mAnimation != null) {
            mAnimation.cancel();
            mStartAnimation = false;
            mAnimation = null;
        }
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        if (interpolator != null) {
            mInterpolator = interpolator;
        } else {
            throw new IllegalArgumentException("Interpolator cannot be null");
        }
    }

    public void setStartDelay(int startDelay) {
        mStartDelay = startDelay;
    }

    public void setFadeInDuration(int fadeInDuration) {
        mFadeOutDuration = fadeInDuration;
    }

    public void setFadeOutDuration(int fadeOutDuration) {
        mFadeOutDuration = fadeOutDuration;
    }

    public int getPerPixelTranslateAnimationDuration() {
        return mTranslateDurationPerPixel;
    }

    public void setPerPixelTranslateAnimationDuration(int animationDuration) {
        mTranslateDurationPerPixel = animationDuration;
    }

    public void setDrawableResourceIds(int[] resourceIds) {
        if (resourceIds.length > 0) {
            mDrawablesResourceIds = resourceIds;

            Resources r = getResources();
            if (r == null) {
                return;
            }
            setImageDrawable(r.getDrawable(mDrawablesResourceIds[mStartIndex]));
        }
    }

    public void resetAnimation() {
        stopAnimation();
        mCurrentIndex = 0;
        setAlpha(1f);
        Resources r = getResources();
        if (r == null) {
            return;
        }
        setImageDrawable(r.getDrawable(mDrawablesResourceIds[mStartIndex]));
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean isChanged = super.setFrame(l, t, r, b);
        if (isChanged && mStartAnimation) {
            configureBounds();
        }
        return isChanged;
    }

    private void init() {
        setImageDrawable(null);
        setScaleType(ScaleType.MATRIX);
        mStartDelay = DEFAULT_START_DELAY;
        mCurrentIndex = DEFAULT_START_INDEX;
        mTranslateDurationPerPixel = DEFAULT_ANIMATION_PER_PIXEL_TRANSLATE_DURATION;
        mFadeInDuration = DEFAULT_FADE_IN_DURATION;
        mFadeOutDuration = DEFAULT_FADE_OUT_DURATION;
        mStartIndex = DEFAULT_START_INDEX;
        mInterpolator = new LinearInterpolator();
    }

    private void configureBounds() {
        if (mDrawablesResourceIds.length > 0 && getScaleType() == ScaleType.MATRIX && getWidth() > 0 && getHeight() > 0 && getResources() != null && !isAnimating()) {

            final int viewWidth = getWidth();
            final int viewHeight = getHeight();

            final Drawable drawable = getResources().getDrawable(mDrawablesResourceIds[mCurrentIndex]);
            if (drawable == null) {
                return;
            }

            AnimatorSet fadeOutInAnimatorSet = null;
            if (getDrawable() != null && mAnimation != null) {

                final PropertyValuesHolder alphaFadeOut = PropertyValuesHolder.ofFloat("alpha", 1, 0.2f);
                final PropertyValuesHolder alphaFadeIn = PropertyValuesHolder.ofFloat("alpha", 0.2f, 1);

                ObjectAnimator alphaFadeOutAnimator = ObjectAnimator.ofPropertyValuesHolder(this, alphaFadeOut);
                alphaFadeOutAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        setImageDrawable(drawable);
                    }
                });
                alphaFadeOutAnimator.setDuration(mFadeOutDuration);

                ObjectAnimator alphaFadeInAnimator = ObjectAnimator.ofPropertyValuesHolder(AnimatingImageView.this, alphaFadeIn).setDuration(mFadeInDuration);

                fadeOutInAnimatorSet = new AnimatorSet();
                fadeOutInAnimatorSet.playSequentially(alphaFadeOutAnimator, alphaFadeInAnimator);
            } else {
                setImageDrawable(drawable);
            }

            final int drawableWidth = drawable.getIntrinsicWidth();
            final int drawableHeight = drawable.getIntrinsicHeight();

            if (mRandom == null) {
                mRandom = new Random();
            }

            int translateX = mRandom.nextInt(drawableWidth - viewWidth);
            final int finalTranslateX = ((translateX + mPrevTranslateX) > drawableWidth ? -translateX : translateX) * -1;

            ValueAnimator xValueAnimator = ValueAnimator.ofFloat(mPrevTranslateX, finalTranslateX);
            xValueAnimator.addUpdateListener(new UpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedValue() != null) {
                        getImageMatrix().getValues(mDrawableMatrixValues);

                        Matrix matrix = new Matrix();
                        matrix.setTranslate((Float) animation.getAnimatedValue(), mDrawableMatrixValues[Matrix.MTRANS_Y]);
                        setImageMatrix(matrix);
                        invalidate();
                    }
                }

            });

            int translateY = mRandom.nextInt(drawableHeight - viewHeight);
            final int finalTranslateY = ((translateY + mPrevTranslateY) > drawableHeight ? -translateY : translateY) * -1;

            ValueAnimator yValueAnimator = ValueAnimator.ofFloat(mPrevTranslateY, finalTranslateY);
            yValueAnimator.addUpdateListener(new UpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedValue() != null) {
                        getImageMatrix().getValues(mDrawableMatrixValues);

                        Matrix matrix = new Matrix();
                        matrix.setTranslate(mDrawableMatrixValues[Matrix.MTRANS_X], (Float) animation.getAnimatedValue());
                        setImageMatrix(matrix);
                        invalidate();
                    }
                }
            });

            final double translateDistance = Math.sqrt( Math.pow(Math.abs(mPrevTranslateX - finalTranslateX), 2) + Math.pow(Math.abs(mPrevTranslateY - finalTranslateY), 2));

            AnimatorSet translateAnimation = new AnimatorSet();
            translateAnimation.playTogether(xValueAnimator, yValueAnimator);
            translateAnimation.setDuration((long)translateDistance * mTranslateDurationPerPixel);

            mAnimation = new AnimatorSet();
            if (fadeOutInAnimatorSet != null) {
                translateAnimation.setStartDelay(Math.min(mFadeInDuration, mFadeOutDuration));
                mAnimation.playTogether(fadeOutInAnimatorSet, translateAnimation);
            } else {
                mAnimation.playTogether(xValueAnimator, yValueAnimator);
                mAnimation.setDuration((long)translateDistance * mTranslateDurationPerPixel);
            }
            mAnimation.setStartDelay(mStartDelay);
            mAnimation.setInterpolator(mInterpolator);
            mAnimation.addListener(new AnimatorListenerAdapter() {

                private boolean mCanceled;

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mIsAnimationRunning = true;
                    mCanceled = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    mCanceled = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mIsAnimationRunning = false;
                    if (mCanceled) {
                        /**
                         * We need to store the translate values at this point in time
                         *
                         */
                        float[] values = new float[9];
                        getImageMatrix().getValues(values);
                        mPrevTranslateX = (int)values[Matrix.MTRANS_X];
                        mPrevTranslateY  = (int)values[Matrix.MTRANS_Y];
                    } else {
                        mPrevTranslateX = finalTranslateX;
                        mPrevTranslateY = finalTranslateY;
                        mCurrentIndex += ((mCurrentIndex + 1) == mDrawablesResourceIds.length) ? -mCurrentIndex : 1;
                        configureBounds();
                    }
                }
            });
            mAnimation.start();
        }
    }

    private static abstract class UpdateListener implements ValueAnimator.AnimatorUpdateListener {
        protected float[] mDrawableMatrixValues;

        public UpdateListener() {
            mDrawableMatrixValues = new float[9];
        }
    }
}