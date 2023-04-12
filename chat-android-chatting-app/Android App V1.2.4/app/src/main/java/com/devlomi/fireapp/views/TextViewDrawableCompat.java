package com.devlomi.fireapp.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;

import com.devlomi.fireapp.R;

//Compat Class to make Vector Drawables work on Older APIs when using DrawabeLeft,Right,Top,Bottom
public class TextViewDrawableCompat extends android.support.v7.widget.AppCompatTextView {
    public TextViewDrawableCompat(Context context) {
        super(context);
        initAttrs(context, null);
    }

    public TextViewDrawableCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public TextViewDrawableCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributeArray = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.TextViewDrawableCompat);

            Drawable drawableStart = null;
            Drawable drawableEnd = null;
            Drawable drawableBottom = null;
            Drawable drawableTop = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawableStart = attributeArray.getDrawable(R.styleable.TextViewDrawableCompat_drawableStartCompat);
                drawableEnd = attributeArray.getDrawable(R.styleable.TextViewDrawableCompat_drawableEndCompat);
                drawableBottom = attributeArray.getDrawable(R.styleable.TextViewDrawableCompat_drawableBottomCompat);
                drawableTop = attributeArray.getDrawable(R.styleable.TextViewDrawableCompat_drawableTopCompat);
            } else {
                final int drawableStartId = attributeArray.getResourceId(R.styleable.TextViewDrawableCompat_drawableStartCompat, -1);
                final int drawableEndId = attributeArray.getResourceId(R.styleable.TextViewDrawableCompat_drawableEndCompat, -1);
                final int drawableBottomId = attributeArray.getResourceId(R.styleable.TextViewDrawableCompat_drawableBottomCompat, -1);
                final int drawableTopId = attributeArray.getResourceId(R.styleable.TextViewDrawableCompat_drawableTopCompat, -1);

                if (drawableStartId != -1)
                    drawableStart = AppCompatResources.getDrawable(context, drawableStartId);
                if (drawableEndId != -1)
                    drawableEnd = AppCompatResources.getDrawable(context, drawableEndId);
                if (drawableBottomId != -1)
                    drawableBottom = AppCompatResources.getDrawable(context, drawableBottomId);
                if (drawableTopId != -1)
                    drawableTop = AppCompatResources.getDrawable(context, drawableTopId);
            }

            int tintColor = attributeArray.getColor(R.styleable.TextViewDrawableCompat_drawableTintCompat, -1);
            if (tintColor != -1) {
                if (drawableStart != null)
                    drawableStart.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
                if (drawableEnd != null)
                    drawableEnd.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
                if (drawableTop != null)
                    drawableTop.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
                if (drawableBottom != null)
                    drawableBottom.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
            }

            // to support rtl
            setCompoundDrawablesRelativeWithIntrinsicBounds(drawableStart, drawableTop, drawableEnd, drawableBottom);
            attributeArray.recycle();
        }
    }
}


