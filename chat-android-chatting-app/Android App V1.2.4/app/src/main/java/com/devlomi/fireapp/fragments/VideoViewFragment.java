package com.devlomi.fireapp.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.interfaces.ToolbarStateChange;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.utils.BitmapUtils;
import com.devlomi.fireapp.utils.DpUtil;
import com.devlomi.fireapp.utils.Util;

/**
 * Created by Devlomi on 26/09/2017.
 */

public class VideoViewFragment extends Fragment {
    //hide toolbar and system bars after two seconds
    private static final long HIDE_DELAY_TIME = 2000;

    //indicates toolbar and systembars state
    private boolean isHidden = false;
    //initial current video duration
    private static int currentVideoDuration = -1;

    //hide toolbar and system bars after two seconds
    Handler hideHandler = new Handler();
    //update progress
    Handler videoProgressHandler = new Handler();

    ImageView thumbImage;
    VideoView videoView;
    ImageButton btnPlay;
    SeekBar seekBar;
    TextView tvSeekbarCurrent;
    TextView tvSeekbarTotal;
    LinearLayout seekbarContainer;

    Message message;
    String oldPath = "";

    //change make activity changes the toolbar state
    ToolbarStateChange toolbarStateChange;

    float x = 0;


    public void setMessage(Message message) {
        this.message = message;
    }

    public void setContext(Context context) {
        this.toolbarStateChange = (ToolbarStateChange) context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_view, container, false);

        videoView = view.findViewById(R.id.video_view);
        seekBar = view.findViewById(R.id.seekbar_video);
        btnPlay = view.findViewById(R.id.btn_play_video);
        thumbImage = view.findViewById(R.id.image_thumb);
        tvSeekbarCurrent = view.findViewById(R.id.tv_seekbar_current);
        tvSeekbarTotal = view.findViewById(R.id.tv_seekbar_total);
        seekbarContainer = view.findViewById(R.id.seekbar_container);

        //set margins for navigation bar since we are using fitsSystemWindows
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) seekbarContainer.getLayoutParams();
        params.bottomMargin = getNavigationBarHeight();
        seekbarContainer.setLayoutParams(params);


        final String path = message.getLocalPath();

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //pause player
                if (videoView.isPlaying() && path.equals(oldPath)) {
                    pausePlayer();
                    //resume
                } else if (path.equals(oldPath)) {
                    resumePlayer();
                } else {
                    //start
                    playVideo(message.getLocalPath());

                }


            }
        });


        //we are using onTouchListener because the onClickListener is not working
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    x = event.getX();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (x == event.getX()) {
                        //start hide handler
                        runHideHandler();

                        if (videoView.isPlaying()) {
                            if (isHidden) {
                                showBtnAndSeekbar();
                                runHideHandler();
                            } else {
                                hideBtnAndSeekbar();
                                hideHandler.removeCallbacks(hideRunnableTask);
                            }
                        }
                    }
                }
                return true;
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                videoProgressHandler.removeCallbacks(updateProgressTask);

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //seek
                videoProgressHandler.removeCallbacks(updateProgressTask);
                videoView.seekTo(seekBar.getProgress());
                updateProgressBar();
            }
        });


        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                showBtnAndSeekbar();
                btnPlay.setImageResource(R.drawable.ic_play_arrow);
            }
        });


        Bitmap bitmap = BitmapUtils.getThumbnailFromVideo(path);
        thumbImage.setImageBitmap(bitmap);

        tvSeekbarTotal.setText(message.getMediaDuration());


        return view;
    }

    private int getNavigationBarHeight() {
        Resources resources = getActivity().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        //if there is no navigation bar set margin t 8DP
        return (int) DpUtil.toPixel(8, getActivity());
    }



    private void resumePlayer() {
        videoView.start();
        btnPlay.setImageResource(R.drawable.ic_pause);
        thumbImage.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        runHideHandler();


    }

    private void pausePlayer() {
        videoView.pause();
        btnPlay.setImageResource(R.drawable.ic_play_arrow);
    }

    private void hideBtnAndSeekbar() {

        //animate hiding btnPlay
        btnPlay.animate().alpha(0).setDuration(300).start();
        seekBar.setVisibility(View.GONE);
        tvSeekbarCurrent.setVisibility(View.GONE);
        tvSeekbarTotal.setVisibility(View.GONE);
        //hide toolbar and system bars
        toolbarStateChange.hideToolbar();
        isHidden = true;

    }

    private void showBtnAndSeekbar() {
        //animate showing btnPlay
        btnPlay.animate().alpha(1).setDuration(300).start();
        seekBar.setVisibility(View.VISIBLE);
        tvSeekbarCurrent.setVisibility(View.VISIBLE);
        tvSeekbarTotal.setVisibility(View.VISIBLE);
        //show toolbar and system bars
        toolbarStateChange.showToolbar();
        isHidden = false;
    }


    private void playVideo(String path) {

        videoView.requestFocus();
        videoView.setVideoURI(Uri.parse(path));
        thumbImage.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int videoDuration = videoView.getDuration();
                seekBar.setMax(videoDuration);
                tvSeekbarTotal.setText(Util.milliSecondsToTimer(videoDuration) + "");
            }
        });
        videoView.start();

        btnPlay.setImageResource(R.drawable.ic_pause);
        updateProgressBar();
        runHideHandler();


        oldPath = path;


    }


    @Override
    public void onPause() {
        super.onPause();
        //pause the video

        pausePlayer();
        showBtnAndSeekbar();

        if (videoView != null && videoView.getCurrentPosition() != 0)
            currentVideoDuration = videoView.getCurrentPosition();

        videoProgressHandler.removeCallbacks(updateProgressTask);

        System.out.println("ONPause ");
    }


    //this is called when navigating between videos
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        //stop video

        if (!isVisibleToUser)
            if (videoView != null) {
                stopPlayer();

                currentVideoDuration = videoView.getCurrentPosition();
                showBtnAndSeekbar();
            }

        if (videoProgressHandler != null && updateProgressTask != null)
            videoProgressHandler.removeCallbacks(updateProgressTask);

    }

    private void stopPlayer() {
        btnPlay.setImageResource(R.drawable.ic_play_arrow);
        oldPath = "";
        if (videoView != null)
            videoView.suspend();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (videoView != null && currentVideoDuration != -1)
            videoView.seekTo(currentVideoDuration);

    }


    //create instance of VideoViewFragment
    public static VideoViewFragment create(Context context, Message message) {
        VideoViewFragment fragment = new VideoViewFragment();
        fragment.setContext(context);
        fragment.setMessage(message);
        return fragment;
    }

    private Runnable updateProgressTask = new Runnable() {
        public void run() {
            seekBar.setProgress(videoView.getCurrentPosition());
            tvSeekbarCurrent.setText(Util.milliSecondsToTimer(videoView.getCurrentPosition()) + "");
            videoProgressHandler.postDelayed(this, 100);
        }
    };

    private void updateProgressBar() {
        videoProgressHandler.postDelayed(updateProgressTask, 100);
    }


    private void runHideHandler() {
        hideHandler.postDelayed(hideRunnableTask, HIDE_DELAY_TIME);
    }


    private Runnable hideRunnableTask = new Runnable() {
        @Override
        public void run() {
            if (videoView.isPlaying() && !isHidden) {
                hideBtnAndSeekbar();
            }
            hideHandler.removeCallbacks(this);
        }
    };
}
