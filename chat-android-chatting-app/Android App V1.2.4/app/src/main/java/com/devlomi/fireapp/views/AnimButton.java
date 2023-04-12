package com.devlomi.fireapp.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.devlomi.fireapp.R;
import com.devlomi.record_view.RecordButton;

/**
 * Created by Devlomi on 01/11/2017.
 */
//this class will animate the state between the record icon and send icon
//when users starts and stops writing
//credits goes to @gunhansancar https://github.com/gunhansancar/AnimButton

public class AnimButton extends RecordButton {
    private static final Interpolator interpolator = new DecelerateInterpolator();

    public static final int RECORDING_STATE = 1;
    public static final int TYPING_STATE = 2;
    private static final float SCALE_TOTAL = 1f;
    private static final float ALPHA_TOTAL = 255;

    private Drawable firstDrawable;
    private Drawable secondDrawable;

    private int state = RECORDING_STATE;
    private int duration = 300;
    private boolean init = false;
    private int recordingResourceId;
    private int typingResourceId;


    public AnimButton(Context context) {
        super(context);
    }

    public AnimButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AnimButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AnimButton, 0, 0);
        if (array != null) {
            recordingResourceId = array.getResourceId(R.styleable.AnimButton_recording, -1);
            typingResourceId = array.getResourceId(R.styleable.AnimButton_typing, -1);
            duration = array.getInteger(R.styleable.AnimButton_duration, duration);

            array.recycle();
        }

        initCommon();
    }


    private void initCommon() {
        if (recordingResourceId > 0 && typingResourceId > 0) {
            init = true;

            Resources resources = getResources();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                firstDrawable = resources.getDrawable(recordingResourceId, null).mutate();
                secondDrawable = resources.getDrawable(typingResourceId, null).mutate();
            } else {
                firstDrawable = resources.getDrawable(recordingResourceId).mutate();
                secondDrawable = resources.getDrawable(typingResourceId).mutate();
            }

            setImageDrawable(firstDrawable);
        }
    }

    //change from record to typing state or vice-versa
    public void goToState(int state) {
        if (!init || this.state == state) return;

        switch (state) {
            case RECORDING_STATE:
                animate(firstDrawable, secondDrawable);
                break;
            case TYPING_STATE:
                animate(secondDrawable, firstDrawable);
                break;
        }

        this.state = state;
    }

    private Drawable makeDrawable(Drawable drawable, float scale, int alpha) {
        ScaleDrawable scaleDrawable = new ScaleDrawable(drawable, 0, scale, scale);
        scaleDrawable.setLevel(1);
        scaleDrawable.setAlpha(alpha);
        return scaleDrawable;
    }


    //start animating the icon
    private void animate(final Drawable from, final Drawable to) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, SCALE_TOTAL);
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                float left = SCALE_TOTAL - value;

                Drawable firstInset = makeDrawable(from, left, (int) (value * ALPHA_TOTAL));
                Drawable secondInset = makeDrawable(to, value, (int) (left * ALPHA_TOTAL));

                setImageDrawable(new LayerDrawable(new Drawable[]{firstInset, secondInset}));
            }
        });

        animator.start();
    }


    public int getState() {
        return state;
    }
}
