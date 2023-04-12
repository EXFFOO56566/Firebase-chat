package com.devlomi.fireapp.utils;

import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

public class FabRotationAnimation {
    private RotateAnimation rotate;
    private int drawable;
    private Animation.AnimationListener animationListener;

    public FabRotationAnimation (final RotateAnimationListener listener) {
        rotate = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(200);
        rotate.setInterpolator(new FastOutLinearInInterpolator());

        animationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (listener != null)
                    listener.onRotationAnimationEnd(drawable);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

    }




    public RotateAnimation start(int drawable) {
        this.drawable = drawable;
        rotate.setAnimationListener(animationListener);
        return rotate;
    }



    public interface RotateAnimationListener {
        void onRotationAnimationEnd(int drawable);
    }
}
