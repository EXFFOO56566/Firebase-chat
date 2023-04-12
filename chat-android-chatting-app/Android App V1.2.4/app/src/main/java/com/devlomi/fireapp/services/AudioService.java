package com.devlomi.fireapp.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.events.AudioServiceCallbacksEvent;
import com.devlomi.fireapp.model.constants.HeadsetState;
import com.devlomi.fireapp.model.constants.SensorState;
import com.devlomi.fireapp.utils.AudioHelper;
import com.devlomi.fireapp.utils.FileUtils;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.NotificationHelper;
import com.devlomi.fireapp.utils.ProximitySensor;
import com.devlomi.fireapp.utils.Util;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

public class AudioService extends Service implements AudioManager.OnAudioFocusChangeListener, ProximitySensor.Delegate {

    //waves
    private Visualizer mVisualizer;
    MediaPlayer mediaPlayer;
    Handler updateProgressHandler = new Handler();
    //initial position
    int pos = -1;
    //initial old position
    int oldPos = -1;
    //url is the the file path in device
    String url;
    //messageId
    String id, oldId;
    int headsetState = HeadsetState.UNPLUGGED;
    int sensorState = SensorState.FAR;
    byte[] waves;
    AudioManager audioManager;
    ProximitySensor mProximitySensor;


    public AudioService() {
    }


    private int getStreamType(int mSensorState, int mHeadsetState) {
        //if the user wants to listen from earpiece and there is no headphone connected
        //play sound from earpiece
        if (mSensorState == SensorState.NEAR && mHeadsetState == HeadsetState.UNPLUGGED) {
            return AudioManager.STREAM_VOICE_CALL;
        } else {
            //play sound from speaker
            return AudioManager.STREAM_MUSIC;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mProximitySensor = new ProximitySensor(this, this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {

            String action = intent.getAction();
            //if it's play action
            if (action.equals(IntentUtils.ACTION_START_PLAY) && intent.getExtras() != null) {
                String id = intent.getStringExtra(IntentUtils.ID);
                String url = intent.getStringExtra(IntentUtils.URL);
                int pos = intent.getIntExtra(IntentUtils.POS, 0);
                int progress = intent.getIntExtra(IntentUtils.PROGRESS, 0);
                play(id, url, pos, progress, sensorState);

                //seek action
            } else if (action.equals(IntentUtils.ACTION_SEEK_TO) && intent.getExtras() != null) {
                String id = intent.getStringExtra(IntentUtils.ID);
                int progress = intent.getIntExtra(IntentUtils.PROGRESS, 0);
                seekTo(id, progress);
                //stop action
            } else if (action.equals(IntentUtils.ACTION_STOP_AUDIO)) {
                if (id != null && sensorState != SensorState.NEAR) {
                    onPause(id, pos);
                    stop();
                }

            }
        } else if (intent != null && intent.hasExtra(IntentUtils.EXTRA_HEADSETSTATE_CHANGED)) {
            int headsetState = intent.getIntExtra(IntentUtils.EXTRA_HEADSETSTATE_CHANGED, HeadsetState.UNPLUGGED);
            headsetStateChanged(headsetState);
        }

        return START_NOT_STICKY;
    }

    private void startForeground() {
        startForeground(NotificationHelper.ID_NOTIFICATION_AUDIO, new NotificationHelper(this).getAudioNotification());
    }


    public void play(final String id, String url, final int pos, int progress, int initialSensorState) {
        this.id = id;
        this.url = url;
        this.pos = pos;


        //pause the audio if the same message is clicked while it's playing
        if (isPlaying() && pos == oldPos) {
            pausePlayer();
        } else {
            //if it's playing stop old audio to run the new one
            if (isPlaying()) {
                stop();
                onPause(oldId, oldPos);
            }


            int streamType = getStreamType(initialSensorState, AudioHelper.isHeadsetOn(audioManager) ? HeadsetState.PLUGGED_IN : HeadsetState.UNPLUGGED);
            //request focus if there are other apps that play audio it will stop
            int audioFocusResult = audioManager.requestAudioFocus(this, streamType, AudioManager.AUDIOFOCUS_GAIN);


            if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                //check if file exists in device
                if (FileUtils.isFileExists(url)) {

                    try {

                        mediaPlayer = new MediaPlayer();

                        //set sound from speaker or earpiece
                        mediaPlayer.setAudioStreamType(streamType);
                        //set the path of audio file
                        mediaPlayer.setDataSource(url);
                        //load the file
                        mediaPlayer.prepare();

                        //if there is no headset connected start listen for proximity sensor
                        //to indicates when the user wants to play audio from earpiece
                        if (!AudioHelper.isHeadsetOn(audioManager))
                            startListenForSensor();


                        //set max seekbar on activity
                        setMax(this.id, pos, mediaPlayer.getDuration());


                        //resuming previous voice message
                        if (progress != -1 && progress != mediaPlayer.getDuration()) {
                            mediaPlayer.seekTo(progress);
                        }
                        //start playing
                        mediaPlayer.start();

                        if (Util.isOreoOrAbove()) {
                            startForeground();
                        }

                        //prepare waves to run
                        prepareVisualizer();

                        //start updating the seekbar every 100ms
                        updateProgressHandler.postDelayed(mUpdateTimeTask, 100);

                        //update UI on activity
                        onPlay(this.id, pos, streamType);

                        mVisualizer.setEnabled(true);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            //stop progress update
                            updateProgressHandler.removeCallbacks(mUpdateTimeTask);

                            //update UI
                            onComplete(AudioService.this.id, pos, mediaPlayer.getDuration());

                            //stop waves
                            mVisualizer.setEnabled(false);
                            stop();
                            //return sensor state to the normal state after finish play
                            sensorState = SensorState.FAR;


                        }
                    });

                    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {

                            //update UI
                            AudioService.this.onError(id, pos);

                            stop();
                            return false;
                        }
                    });

                } else {
                    onError(id, pos);
                }
            } else {
                Toast.makeText(this, R.string.media_player_error, Toast.LENGTH_SHORT).show();
            }
        }

        oldPos = pos;
        oldId = id;

    }

    private void startListenForSensor() {
        mProximitySensor.listenForSensor();
    }

    public void headsetStateChanged(int state) {

        headsetState = state;
        if (state == HeadsetState.UNPLUGGED && isPlaying()) {

            pausePlayer();
        }

    }

    public void sensorStateChanged(int state) {

        if (!isPlaying()) return;

        //decrement progress by 200ms (while the user grab the phone to his ear)
        int currentPos = mediaPlayer.getCurrentPosition() - 200;
        if (sensorState != state) {
            sensorState = state;

            if (state == SensorState.FAR) {

                //when user stops listening from earpiece ,stop audio
                stop();
                onPause(id, pos);


            } else {
                //else user start listening from earpiece
                stop();
                //play from earpiece with decremented progress
                play(id, url, pos, currentPos, sensorState);

            }
        }
    }


//

    private void stop() {
        try {
            //stop listening to sensor
            mProximitySensor.stopListenForSensor();
            //release screen lock
            mProximitySensor.release();
            //remove focus
            audioManager.abandonAudioFocus(this);
            //release waves
            releaseVisualizer();
            //stop audio if it's playing
            if (isPlaying())
                mediaPlayer.stop();
            //release media player
            mediaPlayer.reset();
            mediaPlayer.release();
            if (Util.isOreoOrAbove())
                stopForeground(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void pausePlayer() {
        mediaPlayer.pause();

        onPause(id, pos);

        mVisualizer.setEnabled(false);
        stop();

        updateProgressHandler.removeCallbacks(mUpdateTimeTask);


    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            try {
                int currentDuration = getCurrentPosition();

                //update progress UI
                onProgressUpdate(id, pos, currentDuration, waves);


                updateProgressHandler.postDelayed(this, 200);

            } catch (Exception ex) {
            }
        }
    };

    private int getCurrentPosition() {
        if (mediaPlayer != null)
            return mediaPlayer.getCurrentPosition();

        return 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {

            stop();
            mediaPlayer = null;
        }
        super.onDestroy();

    }


    public void seekTo(String id, int progress) {
        if (this.id != null && this.id.equals(id) && isPlaying())
            mediaPlayer.seekTo(progress);
    }

    private void prepareVisualizer() {
        mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate) {

                        waves = bytes;

                    }

                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] bytes, int samplingRate) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
        mVisualizer.setEnabled(true);
    }

    public boolean isPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public int getDuration() {
        try {
            return mediaPlayer.getDuration();
        } catch (Exception e) {
            return 0;
        }

    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }


    @Override
    public void onAudioFocusChange(int i) {
        //if another app wants to play audio stop our audio
        if (i == AudioManager.AUDIOFOCUS_LOSS || i == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            stop();


            onPause(id, pos);

            //if other app wants to play a Short sound (like notification ) lower the volume
        } else if (i == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            audioManager.setStreamVolume(getStreamType(sensorState, headsetState), 30, AudioManager.FLAG_PLAY_SOUND);
        }
    }


    //user wants to listen from earpiece
    @Override
    public void onProximitySensorNear() {

        if (isPlaying()) {
            sensorStateChanged(SensorState.NEAR);
            //turn screen to black to prevent input
            mProximitySensor.acquire();
        }

    }

    //user dont want to listen from earpiece
    @Override
    public void onProximitySensorFar() {
        if (isPlaying()) {
            sensorStateChanged(SensorState.FAR);
        }

    }

    private void setMax(String id, int pos, int duration) {
        EventBus.getDefault().post(new AudioServiceCallbacksEvent.setMax(id, pos, duration));
    }


    private void onPlay(String id, int pos, int streamType) {
        EventBus.getDefault().post(new AudioServiceCallbacksEvent.onPlay(id, pos, streamType));
    }


    private void onPause(String id, int pos) {
        EventBus.getDefault().post(new AudioServiceCallbacksEvent.onPause(id, pos));
    }

    private void onComplete(String id, int pos, int finalProgress) {
        EventBus.getDefault().post(new AudioServiceCallbacksEvent.onComplete(id, pos, finalProgress));
    }

    private void onError(String id, int pos) {
        EventBus.getDefault().post(new AudioServiceCallbacksEvent.onError(id, pos));
    }

    private void onProgressUpdate(String id, int pos, int progress, byte[] waves) {
        EventBus.getDefault().post(new AudioServiceCallbacksEvent.onProgressUpdate(id, pos, progress, waves));

    }

    private void releaseVisualizer() {
        if (mVisualizer != null) {
            mVisualizer = null;
        }
    }


}

