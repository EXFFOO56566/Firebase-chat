package com.bytesbee.firebase.chat.activities.views.audiowave;

import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.bytesbee.firebase.chat.activities.managers.Utils;

public class MyDrawableCompat {
    public static void setColorFilter(@NonNull Drawable drawable, @ColorInt int color) {
        if (Utils.isAboveQ()) {
            drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_IN));
        } else {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }
}