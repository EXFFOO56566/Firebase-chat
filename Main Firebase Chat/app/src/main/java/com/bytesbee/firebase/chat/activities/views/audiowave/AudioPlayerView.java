package com.bytesbee.firebase.chat.activities.views.audiowave;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.ZERO;
import static com.bytesbee.firebase.chat.activities.managers.Utils.convertSecondsToHMmSs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bytesbee.firebase.chat.activities.R;
import com.bytesbee.firebase.chat.activities.managers.Screens;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.views.voiceplayer.FileUtils;
import com.bytesbee.firebase.chat.activities.views.voiceplayer.PlayerVisualizerSeekbar;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;


public class AudioPlayerView extends LinearLayout {

    private int headsetBackgroundColor, playBackgroundColor, playIconColor, pauseBackgroundColor, pauseIconColor,
            downloadBackgroundColor, downloadIconColor, shareBackgroundColor, viewBackgroundColor,
            seekBarProgressColor, seekBarThumbColor, progressTimeColor, timingBackgroundColor,
            visualizationPlayedColor, visualizationNotPlayedColor, playProgressbarColor;

    private int headsetDirection;

    private float viewCornerRadius, headsetCornerRadius, playCornerRadius, pauseCornerRadius, downloadCornerRadius, shareCornerRadius;
    private boolean showShareButton, showTiming, enableVirtualizer;
    private GradientDrawable playShape, pauseShape, downloadShape, shareShape, viewShape, headsetShape;
    private Context context;
    private String path;
    private String shareTitle = "Share Voice";

    private LinearLayout main_layout, padded_layout;
    private ImageView imgPlay, imgPause, imgShare, imgDownload, imgHeadset;
    private RelativeLayout audioHeadsetLayout, container_layout;
    private AudioWave audioWave;
    private Visualizer mVisualizer;
    private SeekBar seekBar;
    private ProgressBar progressBar;
    private TextView txtProcess, txtAudioFileName;
    private MediaPlayer mediaPlayer;
    private ProgressBar pb_play;

    private PlayerVisualizerSeekbar seekbarV;

    public AudioPlayerView(Context context) {
        super(context);
        this.context = context;
    }

    public AudioPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context, attrs);
        this.context = context;
    }

    public AudioPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context, attrs);
        this.context = context;
    }

    private void initViews(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.VoicePlayerView, 0, 0);

        viewShape = new GradientDrawable();
        playShape = new GradientDrawable();
        pauseShape = new GradientDrawable();
        downloadShape = new GradientDrawable();
        shareShape = new GradientDrawable();
        headsetShape = new GradientDrawable();

        try {
            showShareButton = typedArray.getBoolean(R.styleable.VoicePlayerView_showShareButton, false);
            showTiming = typedArray.getBoolean(R.styleable.VoicePlayerView_showTiming, true);
            viewCornerRadius = typedArray.getFloat(R.styleable.VoicePlayerView_viewCornerRadius, 0);
            playCornerRadius = typedArray.getFloat(R.styleable.VoicePlayerView_playPauseCornerRadius, 0);
            pauseCornerRadius = typedArray.getFloat(R.styleable.VoicePlayerView_playPauseCornerRadius, 0);
            downloadCornerRadius = typedArray.getFloat(R.styleable.VoicePlayerView_playPauseCornerRadius, 0);
            shareCornerRadius = typedArray.getFloat(R.styleable.VoicePlayerView_shareCornerRadius, 0);
            playBackgroundColor = typedArray.getColor(R.styleable.VoicePlayerView_playPauseBackgroundColor, getResources().getColor(R.color.colorAccent));
            playIconColor = typedArray.getColor(R.styleable.VoicePlayerView_playPauseIconColor, getResources().getColor(R.color.colorAccent));
            pauseBackgroundColor = typedArray.getColor(R.styleable.VoicePlayerView_playPauseBackgroundColor, getResources().getColor(R.color.colorAccent));
            pauseIconColor = typedArray.getColor(R.styleable.VoicePlayerView_playPauseIconColor, getResources().getColor(R.color.colorAccent));
            downloadBackgroundColor = typedArray.getColor(R.styleable.VoicePlayerView_playPauseBackgroundColor, getResources().getColor(R.color.colorAccent));
            downloadIconColor = typedArray.getColor(R.styleable.VoicePlayerView_playPauseIconColor, getResources().getColor(R.color.colorAccent));
            shareBackgroundColor = typedArray.getColor(R.styleable.VoicePlayerView_shareBackgroundColor, getResources().getColor(R.color.colorAccent));
            viewBackgroundColor = typedArray.getColor(R.styleable.VoicePlayerView_viewBackground, getResources().getColor(R.color.white));
            seekBarProgressColor = typedArray.getColor(R.styleable.VoicePlayerView_seekBarProgressColor, getResources().getColor(R.color.colorAccent));
            seekBarThumbColor = typedArray.getColor(R.styleable.VoicePlayerView_seekBarThumbColor, getResources().getColor(R.color.colorAccent));
            progressTimeColor = typedArray.getColor(R.styleable.VoicePlayerView_progressTimeColor, Color.GRAY);
            shareTitle = typedArray.getString(R.styleable.VoicePlayerView_shareText);
            enableVirtualizer = typedArray.getBoolean(R.styleable.VoicePlayerView_enableVisualizer, false);
            timingBackgroundColor = typedArray.getColor(R.styleable.VoicePlayerView_timingBackgroundColor, getResources().getColor(android.R.color.transparent));
            visualizationNotPlayedColor = typedArray.getColor(R.styleable.VoicePlayerView_visualizationNotPlayedColor, getResources().getColor(R.color.gray));
            visualizationPlayedColor = typedArray.getColor(R.styleable.VoicePlayerView_visualizationPlayedColor, getResources().getColor(R.color.colorAccent));
            playProgressbarColor = typedArray.getColor(R.styleable.VoicePlayerView_playProgressbarColor, getResources().getColor(R.color.colorAccent));
            headsetBackgroundColor = typedArray.getColor(R.styleable.VoicePlayerView_headsetBackgroundColor, getResources().getColor(R.color.colorAccent));
            headsetCornerRadius = typedArray.getFloat(R.styleable.VoicePlayerView_headsetCornerRadius, 0);

            headsetDirection = typedArray.getInt(R.styleable.VoicePlayerView_headsetDirection, ZERO);

        } finally {
            typedArray.recycle();
        }

        //0 = Right, 1 = Left
        if (headsetDirection == ZERO) {//RIGHT
            LayoutInflater.from(context).inflate(R.layout.vp_audio_view_right, this);
        } else {
            LayoutInflater.from(context).inflate(R.layout.vp_audio_view_left, this);
        }

        main_layout = this.findViewById(R.id.collectorLinearLayout);
        padded_layout = this.findViewById(R.id.paddedLinearLayout);
        container_layout = this.findViewById(R.id.containerLinearLayout);
        imgPlay = this.findViewById(R.id.imgPlay);
        imgPause = this.findViewById(R.id.imgPause);
        imgShare = this.findViewById(R.id.imgShare);
        seekBar = this.findViewById(R.id.seekBar);
        txtAudioFileName = this.findViewById(R.id.txtAudioFileName);
        progressBar = this.findViewById(R.id.progressBar);
        txtProcess = this.findViewById(R.id.txtTime);
        seekbarV = this.findViewById(R.id.seekBarV);
        pb_play = this.findViewById(R.id.pb_play);
        imgDownload = this.findViewById(R.id.imgDownload);

        audioHeadsetLayout = this.findViewById(R.id.audioHeadsetLayout);
        audioWave = this.findViewById(R.id.audioWave);
        imgHeadset = this.findViewById(R.id.imgHeadset);

        pb_play.setVisibility(GONE);
        imgDownload.setVisibility(GONE);
        audioWave.setVisibility(GONE);
        txtAudioFileName.setVisibility(GONE);
        imgHeadset.setVisibility(VISIBLE);

        viewShape.setColor(viewBackgroundColor);
        viewShape.setCornerRadius(viewCornerRadius);
        playShape.setColor(playBackgroundColor);
        playShape.setCornerRadius(playCornerRadius);
        pauseShape.setColor(pauseBackgroundColor);
        pauseShape.setCornerRadius(pauseCornerRadius);
        downloadShape.setColor(downloadBackgroundColor);
        downloadShape.setCornerRadius(downloadCornerRadius);
        shareShape.setColor(shareBackgroundColor);
        shareShape.setCornerRadius(shareCornerRadius);
        headsetShape.setColor(headsetBackgroundColor);
        headsetShape.setCornerRadius(headsetCornerRadius);

        imgPause.setBackground(playShape);
        imgPause.setColorFilter(playIconColor, PorterDuff.Mode.SRC_IN);
        imgPlay.setBackground(pauseShape);
        imgPlay.setColorFilter(pauseIconColor, PorterDuff.Mode.SRC_IN);
        imgDownload.setBackground(downloadShape);
        imgDownload.setColorFilter(downloadIconColor, PorterDuff.Mode.SRC_IN);
        audioHeadsetLayout.setBackground(headsetShape);
        imgHeadset.setBackground(playShape);
        imgHeadset.setColorFilter(playIconColor, PorterDuff.Mode.SRC_IN);
//        imgHeadset.setColorFilter(ContextCompat.getColor(context, playPauseIconColor), PorterDuff.Mode.SRC_IN);
        audioWave.getConfig().setColor(playIconColor);

        imgShare.setBackground(shareShape);
        main_layout.setBackground(viewShape);

        MyDrawableCompat.setColorFilter(seekBar.getProgressDrawable(), seekBarProgressColor);
        MyDrawableCompat.setColorFilter(seekBar.getThumb(), seekBarThumbColor);
//        seekBar.getProgressDrawable().setColorFilter(seekBarProgressColor, PorterDuff.Mode.SRC_IN);
//        seekBar.getThumb().setColorFilter(seekBarThumbColor, PorterDuff.Mode.SRC_IN);

        GradientDrawable timingBackground = new GradientDrawable();
        timingBackground.setColor(timingBackgroundColor);
        timingBackground.setCornerRadius(25);
        txtProcess.setBackground(timingBackground);
        txtProcess.setPadding(6, 0, 6, 0);
        txtProcess.setTextColor(progressTimeColor);

        MyDrawableCompat.setColorFilter(pb_play.getIndeterminateDrawable(), playProgressbarColor);
//        pb_play.getIndeterminateDrawable().setColorFilter(playProgressbarColor, PorterDuff.Mode.SRC_IN);


        if (!showShareButton)
            imgShare.setVisibility(GONE);
        if (!showTiming)
            txtProcess.setVisibility(INVISIBLE);

        if (enableVirtualizer) {
            seekbarV.setVisibility(VISIBLE);
            seekBar.setVisibility(GONE);
            MyDrawableCompat.setColorFilter(seekbarV.getProgressDrawable(), getResources().getColor(android.R.color.transparent));
            MyDrawableCompat.setColorFilter(seekbarV.getThumb(), getResources().getColor(android.R.color.transparent));
//            seekbarV.getProgressDrawable().setColorFilter(getResources().getColor(android.R.color.transparent), PorterDuff.Mode.SRC_IN);
//            seekbarV.getThumb().setColorFilter(getResources().getColor(android.R.color.transparent), PorterDuff.Mode.SRC_IN);
            seekbarV.setColors(visualizationPlayedColor, visualizationNotPlayedColor);
        }

    }

    public void setFileName(String str) {
        if (!Utils.isEmpty(str)) {
            txtAudioFileName.setVisibility(VISIBLE);
            txtAudioFileName.setText(str);
        } else {
            txtAudioFileName.setVisibility(GONE);
        }
    }

    //Set the audio source and prepare mediaPlayer
    public void setAudio(String audioPath) {
        path = audioPath;
        mediaPlayer = new MediaPlayer();
        if (path != null) {
            try {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .build());
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                prepareVisualizer();
                mediaPlayer.setVolume(10, 10);
                //START and PAUSE are in other listeners
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        seekBar.setMax(mp.getDuration());
                        if (seekbarV.getVisibility() == VISIBLE) {
                            seekbarV.setMax(mp.getDuration());
                        }
//                        txtProcess.setText(DEFAULT_ZERO + " / " + convertSecondsToHMmSs(mp.getDuration()));
                        txtProcess.setText(convertSecondsToHMmSs(mp.getDuration()));
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        try {
                            mVisualizer.setEnabled(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        imgPause.setVisibility(View.GONE);
                        audioWave.setVisibility(GONE);
                        imgPlay.setVisibility(View.VISIBLE);
                        imgHeadset.setVisibility(VISIBLE);
                        imgDownload.setVisibility(GONE);
                        pb_play.setVisibility(GONE);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

//            seekBar.setOnSeekBarChangeListener(seekBarListener);
//            imgPlay.setOnClickListener(imgPlayClickListener);
//            imgPause.setOnClickListener(imgPauseClickListener);
            imgShare.setOnClickListener(imgShareClickListener);
            if (seekbarV.getVisibility() == VISIBLE) {
                seekbarV.updateVisualizer(FileUtils.fileToBytes(new File(path)));
            }
            seekbarV.setOnSeekBarChangeListener(seekBarListener);
            seekbarV.updateVisualizer(FileUtils.fileToBytes(new File(path)));
        } else {
            imgPlay.setOnClickListener(imgPlayNoFileClickListener);
        }
    }

    //Components' listeners

    OnClickListener imgPlayClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            imgPause.setVisibility(View.VISIBLE);
            audioWave.setVisibility(VISIBLE);
            imgPlay.setVisibility(View.GONE);
            imgHeadset.setVisibility(INVISIBLE);
            imgDownload.setVisibility(GONE);
            pb_play.setVisibility(GONE);

            try {
                mVisualizer.setEnabled(true);
            } catch (Exception e) {
//                    Utils.getErrors(e);
                prepareVisualizer();
            }
            mediaPlayer.start();
            try {
                update(mediaPlayer, txtProcess, seekBar, context);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    OnClickListener imgPauseClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            imgPlay.setVisibility(View.VISIBLE);
            imgPause.setVisibility(View.GONE);
            audioWave.setVisibility(GONE);
            imgHeadset.setVisibility(VISIBLE);
            imgDownload.setVisibility(GONE);
            pb_play.setVisibility(GONE);

            mediaPlayer.pause();
        }
    };

    final OnClickListener imgPlayNoFileClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final Screens screens = new Screens(context);
            screens.showToast(R.string.msgFileNotFound);
        }
    };


    private void prepareVisualizer() {
        try {
            mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            mVisualizer.setDataCaptureListener(
                    new Visualizer.OnDataCaptureListener() {
                        public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                            audioWave.updateVisualizer(bytes);
                        }

                        public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                        }
                    }, Visualizer.getMaxCaptureRate() / 2, true, false);
            mVisualizer.setEnabled(true);
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

    SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mediaPlayer.seekTo(progress);
                update(mediaPlayer, txtProcess, seekBar, context);
                if (seekbarV.getVisibility() == VISIBLE) {
                    seekbarV.updatePlayerPercent((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration());
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Utils.sout("onSeek touch");
            imgPause.setVisibility(View.GONE);
            audioWave.setVisibility(GONE);
            imgPlay.setVisibility(View.VISIBLE);
            imgHeadset.setVisibility(VISIBLE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            imgPlay.setVisibility(View.GONE);
            imgHeadset.setVisibility(INVISIBLE);
            imgPause.setVisibility(View.VISIBLE);
            audioWave.setVisibility(VISIBLE);
            mediaPlayer.start();

        }
    };

    public void stopPlayer() {
        try {
            Utils.sout("Stop Player from Audio Player");
            imgPause.callOnClick();
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

    OnClickListener imgShareClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imgShare.setVisibility(GONE);
                    progressBar.setVisibility(VISIBLE);
                }
            });
            File file = new File(path);
            if (file.exists()) {
                Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                intentShareFile.setType(URLConnection.guessContentTypeFromName(file.getName()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                }
                intentShareFile.putExtra(Intent.EXTRA_STREAM,
                        Uri.parse("file://" + file.getAbsolutePath()));

                context.startActivity(Intent.createChooser(intentShareFile, shareTitle));
            }
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(GONE);
                            imgShare.setVisibility(VISIBLE);
                        }
                    });

                }
            }, 500);

        }
    };

    //Updating seekBar in realtime
    private void update(final MediaPlayer mediaPlayer, final TextView time, final SeekBar seekBar, final Context context) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                if (seekbarV.getVisibility() == VISIBLE) {
                    seekbarV.setProgress(mediaPlayer.getCurrentPosition());
                    seekbarV.updatePlayerPercent((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration());
                }

                if (mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() > 100) {
//                    time.setText(convertSecondsToHMmSs(mediaPlayer.getCurrentPosition()) + " / " + convertSecondsToHMmSs(mediaPlayer.getDuration() ));
                    time.setText(convertSecondsToHMmSs(mediaPlayer.getCurrentPosition()));
                } else {
//                    time.setText(DEFAULT_ZERO + " / " + convertSecondsToHMmSs(mediaPlayer.getDuration() / 1000));
                    time.setText(convertSecondsToHMmSs(mediaPlayer.getDuration()));
                    seekBar.setProgress(0);
                    if (seekbarV.getVisibility() == VISIBLE) {
                        seekbarV.updatePlayerPercent(0);
                        seekbarV.setProgress(0);
                    }
                }
                Handler handler = new Handler(Looper.getMainLooper());
                try {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (mediaPlayer.getCurrentPosition() > -1) {
                                    try {
                                        update(mediaPlayer, time, seekBar, context);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    handler.postDelayed(runnable, 2);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    //Convert long milli seconds to a formatted String to display it
//    private static String convertSecondsToHMmSs(long seconds) {
//        long s = seconds % 60;
//        long m = (seconds / 60) % 60;
//        long h = (seconds / (60 * 60)) % 24;
//        return String.format("%02d:%02d", m, s);
//    }

    //These both functions to avoid mediaplayer errors

    public void onStop() {
        try {
            try {
                mVisualizer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPause() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mVisualizer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        imgPause.setVisibility(View.GONE);
//        audioWave.setVisibility(GONE);
//        imgPlay.setVisibility(View.VISIBLE);
//        imgHeadset.setVisibility(VISIBLE);
//        imgDownload.setVisibility(GONE);
//        pb_play.setVisibility(GONE);
    }


    // Programmatically functions

    public void setViewBackgroundShape(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(getResources().getColor(color));
        shape.setCornerRadius(radius);
        main_layout.setBackground(shape);
    }

    public void setShareBackgroundShape(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(getResources().getColor(color));
        shape.setCornerRadius(radius);
        imgShare.setBackground(shape);
    }

    public void setPlayPauseBackgroundShape(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(getResources().getColor(color));
        shape.setCornerRadius(radius);
        imgPause.setBackground(shape);
        imgPlay.setBackground(shape);
    }

    public void setSeekBarStyle(int progressColor, int thumbColor) {
        MyDrawableCompat.setColorFilter(seekBar.getProgressDrawable(), getResources().getColor(progressColor));
        MyDrawableCompat.setColorFilter(seekBar.getThumb(), getResources().getColor(thumbColor));
//        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(progressColor), PorterDuff.Mode.SRC_IN);
//        seekBar.getThumb().setColorFilter(getResources().getColor(thumbColor), PorterDuff.Mode.SRC_IN);
    }

    public void setTimingVisibility(boolean visibility) {
        if (!visibility)
            txtProcess.setVisibility(INVISIBLE);
        else
            txtProcess.setVisibility(VISIBLE);
    }

    public void setShareButtonVisibility(boolean visibility) {
        if (!visibility)
            imgShare.setVisibility(GONE);
        else
            imgShare.setVisibility(VISIBLE);
    }

    public void setShareText(String shareText) {
        shareTitle = shareText;
    }

    public void showDownloadButton() {
        imgPlay.setVisibility(GONE);
        imgHeadset.setVisibility(VISIBLE);
        imgPause.setVisibility(GONE);
        audioWave.setVisibility(GONE);
        imgDownload.setVisibility(VISIBLE);
        pb_play.setVisibility(GONE);
    }

    public void showPlayProgressbar() {
        imgPlay.setVisibility(GONE);
        imgHeadset.setVisibility(VISIBLE);
        imgPause.setVisibility(GONE);
        audioWave.setVisibility(GONE);
        imgDownload.setVisibility(GONE);
        pb_play.setVisibility(VISIBLE);
    }

    public void hidePlayProgressbar() {
        imgDownload.setVisibility(GONE);
        pb_play.setVisibility(GONE);
        imgPlay.setVisibility(VISIBLE);
        imgHeadset.setVisibility(VISIBLE);
    }

    public void hidePlayProgressAndPlay() {
        imgDownload.setVisibility(GONE);
        pb_play.setVisibility(GONE);
        imgPlay.setVisibility(VISIBLE);
        imgHeadset.setVisibility(VISIBLE);
        imgPlay.callOnClick();
    }

    public void refreshVisualizer() {
        if (seekbarV.getVisibility() == VISIBLE) {
            seekbarV.updateVisualizer(FileUtils.fileToBytes(new File(path)));
        }
    }

    public ProgressBar getPlayProgressbar() {
        return pb_play;
    }

    public int getPlayPauseBackgroundColor() {
        return playBackgroundColor;
    }

    public void setPlayPauseBackgroundColor(int playPauseBackgroundColor) {
        this.playBackgroundColor = playPauseBackgroundColor;
    }

    public int getPlayPauseIconColor() {
        return playIconColor;
    }

    public void setPlayPauseIconColor(int playPauseIconColor) {
        this.playIconColor = playPauseIconColor;
    }

    public int getShareBackgroundColor() {
        return shareBackgroundColor;
    }

    public void setShareBackgroundColor(int shareBackgroundColor) {
        this.shareBackgroundColor = shareBackgroundColor;
    }

    public int getViewBackgroundColor() {
        return viewBackgroundColor;
    }

    public void setViewBackgroundColor(int viewBackgroundColor) {
        this.viewBackgroundColor = viewBackgroundColor;
    }

    public int getSeekBarProgressColor() {
        return seekBarProgressColor;
    }

    public void setSeekBarProgressColor(int seekBarProgressColor) {
        this.seekBarProgressColor = seekBarProgressColor;
    }

    public int getSeekBarThumbColor() {
        return seekBarThumbColor;
    }

    public void setSeekBarThumbColor(int seekBarThumbColor) {
        this.seekBarThumbColor = seekBarThumbColor;
    }

    public int getProgressTimeColor() {
        return progressTimeColor;
    }

    public void setProgressTimeColor(int progressTimeColor) {
        this.progressTimeColor = progressTimeColor;
    }

    public int getTimingBackgroundColor() {
        return timingBackgroundColor;
    }

    public void setTimingBackgroundColor(int timingBackgroundColor) {
        this.timingBackgroundColor = timingBackgroundColor;
    }

    public int getVisualizationPlayedColor() {
        return visualizationPlayedColor;
    }

    public void setVisualizationPlayedColor(int visualizationPlayedColor) {
        this.visualizationPlayedColor = visualizationPlayedColor;
    }

    public int getVisualizationNotPlayedColor() {
        return visualizationNotPlayedColor;
    }

    public void setVisualizationNotPlayedColor(int visualizationNotPlayedColor) {
        this.visualizationNotPlayedColor = visualizationNotPlayedColor;
    }

    public int getPlayProgressbarColor() {
        return playProgressbarColor;
    }

    public void setPlayProgressbarColor(int playProgressbarColor) {
        this.playProgressbarColor = playProgressbarColor;
    }

    public float getViewCornerRadius() {
        return viewCornerRadius;
    }

    public void setViewCornerRadius(float viewCornerRadius) {
        this.viewCornerRadius = viewCornerRadius;
    }

    public float getPlayCornerRadius() {
        return playCornerRadius;
    }

    public void setPlayCornerRadius(float playCornerRadius) {
        this.playCornerRadius = playCornerRadius;
    }

    public float getShareCornerRadius() {
        return shareCornerRadius;
    }

    public void setShareCornerRadius(float shareCornerRadius) {
        this.shareCornerRadius = shareCornerRadius;
    }

    public boolean isShowShareButton() {
        return showShareButton;
    }

    public void setShowShareButton(boolean showShareButton) {
        this.showShareButton = showShareButton;
    }

    public boolean isShowTiming() {
        return showTiming;
    }

    public void setShowTiming(boolean showTiming) {
        this.showTiming = showTiming;
    }

    public boolean isEnableVirtualizer() {
        return enableVirtualizer;
    }

    public void setEnableVirtualizer(boolean enableVirtualizer) {
        this.enableVirtualizer = enableVirtualizer;
    }

    public GradientDrawable getPlayShape() {
        return playShape;
    }

    public void setPlayShape(GradientDrawable playShape) {
        this.playShape = playShape;
    }

    public GradientDrawable getShareShape() {
        return shareShape;
    }

    public void setShareShape(GradientDrawable shareShape) {
        this.shareShape = shareShape;
    }

    public GradientDrawable getViewShape() {
        return viewShape;
    }

    public void setViewShape(GradientDrawable viewShape) {
        this.viewShape = viewShape;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getShareTitle() {
        return shareTitle;
    }

    public void setShareTitle(String shareTitle) {
        this.shareTitle = shareTitle;
    }

    public LinearLayout getMain_layout() {
        return main_layout;
    }

    public void setMain_layout(LinearLayout main_layout) {
        this.main_layout = main_layout;
    }

    public LinearLayout getPadded_layout() {
        return padded_layout;
    }

    public void setPadded_layout(LinearLayout padded_layout) {
        this.padded_layout = padded_layout;
    }

    public RelativeLayout getContainer_layout() {
        return container_layout;
    }

    public void setContainer_layout(RelativeLayout container_layout) {
        this.container_layout = container_layout;
    }

    public ImageView getImgPlay() {
        return imgPlay;
    }

    public void setImgPlay(ImageView imgPlay) {
        this.imgPlay = imgPlay;
    }

    public ImageView getImgPause() {
        return imgPause;
    }

    public void setImgPause(ImageView imgPause) {
        this.imgPause = imgPause;
    }

    public ImageView getImgDownload() {
        return imgDownload;
    }

    public void setImgDownload(ImageView imgDownload) {
        this.imgDownload = imgDownload;
    }

    public ImageView getImgShare() {
        return imgShare;
    }

    public void setImgShare(ImageView imgShare) {
        this.imgShare = imgShare;
    }

    public SeekBar getSeekBar() {
        return seekBar;
    }

    public void setSeekBar(SeekBar seekBar) {
        this.seekBar = seekBar;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public TextView getTxtProcess() {
        return txtProcess;
    }

    public void setTxtProcess(TextView txtProcess) {
        this.txtProcess = txtProcess;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public ProgressBar getPb_play() {
        return pb_play;
    }

    public void setPb_play(ProgressBar pb_play) {
        this.pb_play = pb_play;
    }

    public PlayerVisualizerSeekbar getSeekbarV() {
        return seekbarV;
    }

    public void setSeekbarV(PlayerVisualizerSeekbar seekbarV) {
        this.seekbarV = seekbarV;
    }

    public OnClickListener getImgPlayClickListener() {
        return imgPlayClickListener;
    }

    public void setImgPlayClickListener(OnClickListener imgPlayClickListener) {
        this.imgPlayClickListener = imgPlayClickListener;
    }

    public OnClickListener getImgPlayNoFileClickListener() {
        return imgPlayNoFileClickListener;
    }

    public SeekBar.OnSeekBarChangeListener getSeekBarListener() {
        return seekBarListener;
    }

    public void setSeekBarListener(SeekBar.OnSeekBarChangeListener seekBarListener) {
        this.seekBarListener = seekBarListener;
    }

    public OnClickListener getImgPauseClickListener() {
        return imgPauseClickListener;
    }

    public void setImgPauseClickListener(OnClickListener imgPauseClickListener) {
        this.imgPauseClickListener = imgPauseClickListener;
    }

    public OnClickListener getImgShareClickListener() {
        return imgShareClickListener;
    }

    public void setImgShareClickListener(OnClickListener imgShareClickListener) {
        this.imgShareClickListener = imgShareClickListener;
    }
}
