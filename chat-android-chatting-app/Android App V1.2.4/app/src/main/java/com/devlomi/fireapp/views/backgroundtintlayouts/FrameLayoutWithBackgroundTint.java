package com.devlomi.fireapp.views.backgroundtintlayouts;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

//all layouts in 'backgroundtintlayouts package' will support the background tint for older APIs
public class FrameLayoutWithBackgroundTint extends FrameLayout {
    public FrameLayoutWithBackgroundTint(Context context) {
        super(context);
        init(context,null);
    }

    public FrameLayoutWithBackgroundTint(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public FrameLayoutWithBackgroundTint(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        //change the background tint by the given color in xml
        new TintHelper(context, this, attrs);
    }


}
