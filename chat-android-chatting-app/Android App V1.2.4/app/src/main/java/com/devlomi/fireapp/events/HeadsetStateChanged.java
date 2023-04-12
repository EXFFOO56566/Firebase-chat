package com.devlomi.fireapp.events;

/**
 * Created by Devlomi on 05/10/2017.
 */

public class HeadsetStateChanged {
    private int state;

    public HeadsetStateChanged(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
