package jp.shts.android.storiesprogressview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/*
this library is made by @shts https://github.com/shts/StoriesProgressView
i only modified it to fit my needs like pausing and starting stories
 */

public class StoriesProgressView extends LinearLayout {


    private final LayoutParams PROGRESS_BAR_LAYOUT_PARAM = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
    private final LayoutParams SPACE_LAYOUT_PARAM = new LayoutParams(5, LayoutParams.WRAP_CONTENT);

    private final List<PausableProgressBar> progressBars = new ArrayList<>();

    private int storiesCount = -1;
    /**
     * pointer of running animation
     */
    private int current = -1;
    private StoriesListener storiesListener;

    public interface StoriesListener {
        void onNext();

        void onPrev();

        void onComplete();

    }

    public StoriesProgressView(Context context) {
        this(context, null);
    }

    public StoriesProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public StoriesProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StoriesProgressView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        setOrientation(LinearLayout.HORIZONTAL);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.StoriesProgressView);
        storiesCount = typedArray.getInt(R.styleable.StoriesProgressView_progressCount, 0);
        typedArray.recycle();
        bindViews();
    }

    private void bindViews() {
        progressBars.clear();
        removeAllViews();

        for (int i = 0; i < storiesCount; i++) {
            final PausableProgressBar p = createProgressBar();
            progressBars.add(p);
            addView(p);
            if ((i + 1) < storiesCount) {
                addView(createSpace());
            }
        }
    }

    private PausableProgressBar createProgressBar() {
        PausableProgressBar p = new PausableProgressBar(getContext());
        p.setLayoutParams(PROGRESS_BAR_LAYOUT_PARAM);
        return p;
    }

    private View createSpace() {
        View v = new View(getContext());
        v.setLayoutParams(SPACE_LAYOUT_PARAM);
        return v;
    }

    /**
     * Set story count and create views
     *
     * @param storiesCount story count
     */
    public void setStoriesCount(int storiesCount) {
        this.storiesCount = storiesCount;
        bindViews();
    }

    /**
     * Set storiesListener
     *
     * @param storiesListener StoriesListener
     */
    public void setStoriesListener(StoriesListener storiesListener) {
        this.storiesListener = storiesListener;
    }

    /**
     * Skip current story
     */

    /**
     * Set a story's duration
     *
     * @param duration millisecond
     */
    public void setStoryDuration(long duration) {
        for (int i = 0; i < progressBars.size(); i++) {
            progressBars.get(i).setDuration(duration);
            progressBars.get(i).setCallback(callback(i));
        }
    }

    /**
     * Set stories count and each story duration
     *
     * @param durations milli
     */
    public void setStoriesCountWithDurations(@NonNull long[] durations) {
        storiesCount = durations.length;
        bindViews();
        for (int i = 0; i < progressBars.size(); i++) {
            progressBars.get(i).setDuration(durations[i]);
            progressBars.get(i).setCallback(callback(i));
        }
    }

    private PausableProgressBar.Callback callback(final int index) {
        return new PausableProgressBar.Callback() {
            @Override
            public void onStartProgress() {
            }

            @Override
            public void onFinishProgress() {


                if (current == storiesCount - 1) {
                    if (storiesListener != null)
                        storiesListener.onComplete();
                } else if (storiesListener != null)
                    storiesListener.onNext();

            }
        };
    }


    /**
     * Start progress animation
     */
    public void startStories() {
        progressBars.get(0).startProgress();
    }

    /**
     * Start progress animation from specific progress
     */
    public void startStories(int from) {
        for (int i = 0; i < from; i++) {
            progressBars.get(i).setMaxWithoutCallback();
        }
    }

    /**
     * Need to call when Activity or Fragment destroy
     */
    public void destroy() {
        for (PausableProgressBar p : progressBars) {
            p.clear();
        }
    }

    /**
     * Pause story
     */
    public void pause() {
        if (current < 0) {

            return;
        }
        progressBars.get(current).pauseProgress();
    }

    /**
     * Resume story
     */
    public void resume() {
        if (current < 0) return;
        progressBars.get(current).resumeProgress();
    }

    public void start(int index) {

        progressBars.get(index).startProgress();
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public void skip(int current) {
        progressBars.get(current).setMaxWithoutCallback();
        if (storiesListener != null)
            storiesListener.onNext();

        if (current == storiesCount - 1) {
            storiesListener.onComplete();
        }

    }

    public void reverse(int current) {
        if (current - 1 < 0) {
            return;
        }
        //if this is the first item then replay it

        progressBars.get(current).setMinWithoutCallback();
        progressBars.get(current).hideProgress(false);



        if (storiesListener != null)
            storiesListener.onPrev();
    }
}
