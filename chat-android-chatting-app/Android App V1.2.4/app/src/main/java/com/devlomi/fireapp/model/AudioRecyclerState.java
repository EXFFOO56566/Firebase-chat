package com.devlomi.fireapp.model;

/**
 * Created by Devlomi on 23/08/2017.
 */

// save/change audio state in recyclerView
public class AudioRecyclerState {
    private boolean isPlaying;
    private String currentDuration;
    private int progress;
    private int max = -1;


    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public String getCurrentDuration() {
        return currentDuration;
    }

    public void setCurrentDuration(String currentDuration) {
        this.currentDuration = currentDuration;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public AudioRecyclerState(boolean isPlaying, String currentDuration, int progress) {
        this.isPlaying = isPlaying;
        this.currentDuration = currentDuration;
        this.progress = progress;
    }

    public AudioRecyclerState(boolean isPlaying, String currentDuration, int progress, int max) {
        this.isPlaying = isPlaying;
        this.currentDuration = currentDuration;
        this.progress = progress;
        this.max = max;
    }
}
