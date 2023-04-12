package com.devlomi.fireapp.views.backgroundtintlayouts;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class LinearLayoutWithBackgroundTint extends LinearLayout {
    public LinearLayoutWithBackgroundTint(Context context) {
        super(context);
        init(context,null);
    }

    public LinearLayoutWithBackgroundTint(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public LinearLayoutWithBackgroundTint(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        new TintHelper(context, this, attrs);
    }


}
