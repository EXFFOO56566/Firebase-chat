package com.devlomi.fireapp.views;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class TextViewWithShapeBackground extends AppCompatTextView {
    public TextViewWithShapeBackground(Context context) {
        super(context);
    }

    public TextViewWithShapeBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewWithShapeBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setShapeColor(int color) {
        Drawable background = getBackground();
        if (background instanceof ShapeDrawable) {
            ((ShapeDrawable) background).getPaint().setColor(color);
        } else if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(color);
        } else if (background instanceof ColorDrawable) {
            ((ColorDrawable) background).setColor(color);
        }
    }
}
