package com.devlomi.fireapp.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.devlomi.fireapp.R;

import java.io.FileInputStream;
import java.io.IOException;

//this class is responsible for playing ringtone & progress tone when initiating a new call or when receiving a new call
public class RingtonePlayer {

    Ringtone defaultRingtone;
    AudioTrack mProgressTone;
    Context context;
    private final static int SAMPLE_RATE = 16000;

    public RingtonePlayer(Context context) {
        this.context = context.getApplicationContext();
        Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        defaultRingtone = RingtoneManager.getRingtone(context, defaultRingtoneUri);
    }


    public void playIncomingRingtone() {
        if (defaultRingtone != null)
            defaultRingtone.play();
    }


    public void stopRingtone() {
        if (defaultRingtone != null)
            defaultRingtone.stop();

        if (mProgressTone != null)
            stopProgressTone();

    }

    public void playProgressTone() {
        stopProgressTone();
        try {
            mProgressTone = createProgressTone(context);
            mProgressTone.play();
        } catch (Exception e) {
        }
    }

    public void stopProgressTone() {
        if (mProgressTone != null) {
            mProgressTone.stop();
            mProgressTone.release();
            mProgressTone = null;
        }
    }

    private static AudioTrack createProgressTone(Context context) throws IOException {
        AssetFileDescriptor fd = context.getResources().openRawResourceFd(R.raw.progress_tone);
        int length = (int) fd.getLength();

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, length, AudioTrack.MODE_STATIC);

        byte[] data = new byte[length];
        readFileToBytes(fd, data);

        audioTrack.write(data, 0, data.length);
        audioTrack.setLoopPoints(0, data.length / 2, 30);

        return audioTrack;
    }

    private static void readFileToBytes(AssetFileDescriptor fd, byte[] data) throws IOException {
        FileInputStream inputStream = fd.createInputStream();

        int bytesRead = 0;
        while (bytesRead < data.length) {
            int res = inputStream.read(data, bytesRead, (data.length - bytesRead));
            if (res == -1) {
                break;
            }
            bytesRead += res;
        }
    }
}
