package com.devlomi.fireapp.utils;

import android.app.Activity;
import android.support.design.widget.Snackbar;

import com.devlomi.fireapp.R;

public class SnackbarUtil {
    public static void showDoesNotFireAppSnackbar(Activity context) {
        String text = String.format(context.getString(R.string.does_not_have_fireapp), context.getString(R.string.app_name));
        Snackbar.make(context.findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show();
    }
}
