package com.devlomi.fireapp.utils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.view.ViewTreeObserver;

//this is a Dialog onClick listener
//this is used to prevent memory leaks
public final class DetachableClickListener implements DialogInterface.OnClickListener {

    public static DetachableClickListener wrap(DialogInterface.OnClickListener delegate) {
        return new DetachableClickListener(delegate);
    }

    private DialogInterface.OnClickListener delegateOrNull;

    private DetachableClickListener(DialogInterface.OnClickListener delegate) {
        this.delegateOrNull = delegate;
    }

    @Override public void onClick(DialogInterface dialog, int which) {
        if (delegateOrNull != null) {
            delegateOrNull.onClick(dialog, which);
        }
    }

    public void clearOnDetach(Dialog dialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            dialog.getWindow()
                    .getDecorView()
                    .getViewTreeObserver()
                    .addOnWindowAttachListener(new ViewTreeObserver.OnWindowAttachListener() {
                        @Override public void onWindowAttached() { }
                        @Override public void onWindowDetached() {
                            delegateOrNull = null;
                        }
                    });
        }
    }
}
