package com.devlomi.fireapp.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.devlomi.fireapp.R;
import com.github.lzyzsd.circleprogress.DonutProgress;

/**
 * Created by Devlomi on 06/12/2017.
 */

//this class contains a ProgressBar with a Cancel Button inside it
public class ProgressWithCancelView extends FrameLayout {
    DonutProgress donutProgress;
    ImageView cancelBtn;
    int color;

    public ProgressWithCancelView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public ProgressWithCancelView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public ProgressWithCancelView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        //create new progress bar
        donutProgress = new DonutProgress(context);
        donutProgress.setShowText(false);

        //get progress color from xml if provided
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ProgressWithCancelView, 0, 0);
            if (array != null) {

                color = array.getColor(R.styleable.ProgressWithCancelView_progressBarColor, -1);


                array.recycle();
            } else
                color = getResources().getColor(R.color.colorPrimary);
        } else color = getResources().getColor(R.color.colorPrimary);


        //set progress color
        donutProgress.setFinishedStrokeColor(color);


        donutProgress.setUnfinishedStrokeWidth(10);
        donutProgress.setFinishedStrokeWidth(10);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        donutProgress.setLayoutParams(layoutParams);
        //center the progress and cancel btn
        layoutParams.gravity = Gravity.CENTER;
        //create the cancel button
        cancelBtn = new ImageView(context);
        //add some padding
        cancelBtn.setPadding(5, 5, 5, 5);
        cancelBtn.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //set the icon
        cancelBtn.setImageResource(R.drawable.ic_clear);
        cancelBtn.setLayoutParams(layoutParams);


        //add progressBar
        addView(donutProgress);
        //add the cancel button
        addView(cancelBtn);


    }

    //update the progress bar with given progress
    public void setProgress(int progress) {
        donutProgress.setProgress(progress);
    }


}
