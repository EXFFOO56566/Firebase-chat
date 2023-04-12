package com.devlomi.fireapp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devlomi.fireapp.R;

//this will show the dividers among the statuses (recent updates,viewed statuses)
public class HeaderViewDecoration extends RecyclerView.ItemDecoration {
    private TextView customView;
    private int position1, position2 = -1;
    private String header1Title, header2Title = "";

    public HeaderViewDecoration(Context context) {
        customView = (TextView) LayoutInflater.from(context).inflate(R.layout.row_status_header, null);
    }


    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (parent.getChildAdapterPosition(view) == position1 || parent.getChildAdapterPosition(view) == position2) {
                if (parent.getChildAdapterPosition(view) == position1)
                    customView.setText(header1Title);
                if (parent.getChildAdapterPosition(view) == position2)
                    customView.setText(header2Title);
                c.save();
                int height = customView.getMeasuredHeight();
                int top = view.getTop() - height;
                c.translate(0f, top);
                customView.draw(c);

                c.restore();
            }
        }

    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) == position1 || parent.getChildAdapterPosition(view) == position2) {
            measureHeaderView(customView, parent);
            outRect.set(0, customView.getMeasuredHeight(), 0, 0);
        } else {
            outRect.setEmpty();
        }
    }


    protected void measureHeaderView(View view, ViewGroup parent) {
        if (view.getLayoutParams() == null) {
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        final DisplayMetrics displayMetrics = parent.getContext().getResources().getDisplayMetrics();

        int widthSpec = View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, View.MeasureSpec.EXACTLY);

        int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                parent.getPaddingLeft() + parent.getPaddingRight(), view.getLayoutParams().width);
        int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                parent.getPaddingTop() + parent.getPaddingBottom(), view.getLayoutParams().height);

        view.measure(childWidth, childHeight);

        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
    }


    public void updateHeaders(int header1pos, int header2pos, String header1Title, String header2Title) {
        this.position1 = header1pos;
        this.position2 = header2pos;
        this.header1Title = header1Title;
        this.header2Title = header2Title;
    }
}

