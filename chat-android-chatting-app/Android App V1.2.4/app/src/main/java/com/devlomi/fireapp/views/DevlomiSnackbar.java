package com.devlomi.fireapp.views;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.SwipeDismissBehavior;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

//this custom snackbar used to show the selected contacts in forward activity
//it has some properties like changing the color and get the TextView from snackbar
public class DevlomiSnackbar {

    private View rootView;
    private Snackbar.SnackbarLayout layout;
    private Snackbar snackbar;
    private TextView snackbarTextView;


    public DevlomiSnackbar(View rootView, int backgroundColor) {
        this.rootView = rootView;
        //make snackbar ,and prevent auto hide
        snackbar = Snackbar.make(this.rootView, "", Snackbar.LENGTH_INDEFINITE);

        //get snackbar layout
        layout = (Snackbar.SnackbarLayout) snackbar.getView();


        //used to prevent snackbar swipe
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.LayoutParams lp = layout.getLayoutParams();
                if (lp instanceof CoordinatorLayout.LayoutParams) {
                    ((CoordinatorLayout.LayoutParams) lp).setBehavior(new DisableSwipeBehavior());
                    layout.setLayoutParams(lp);
                }
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            }
        });

        //set background color
        layout.setBackgroundColor(backgroundColor);


        //get snackbar textview
        snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);


    }

    //get snackbar textview
    public TextView getSnackbarTextView() {
        return snackbarTextView;
    }


    public void showSnackBar() {
        snackbar.show();
    }

    public void dismissSnackbar() {
        snackbar.dismiss();
    }

    public boolean isShowing() {
        return snackbar.isShown();
    }
}


//used to prevent snackbar swipe
class DisableSwipeBehavior extends SwipeDismissBehavior<Snackbar.SnackbarLayout> {
    @Override
    public boolean canSwipeDismissView(View view) {
        return false;
    }
}

