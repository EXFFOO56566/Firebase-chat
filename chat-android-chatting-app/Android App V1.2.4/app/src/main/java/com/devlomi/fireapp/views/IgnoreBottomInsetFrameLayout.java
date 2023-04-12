package com.devlomi.fireapp.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

//this will set the bottom insets to 0 on older APIs
public class IgnoreBottomInsetFrameLayout extends FrameLayout {

    public IgnoreBottomInsetFrameLayout(Context context) {
        super(context);
    }

    public IgnoreBottomInsetFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IgnoreBottomInsetFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected boolean fitSystemWindows(Rect insets) {
        insets.bottom = 0;
        return super.fitSystemWindows(insets);
    }
}
