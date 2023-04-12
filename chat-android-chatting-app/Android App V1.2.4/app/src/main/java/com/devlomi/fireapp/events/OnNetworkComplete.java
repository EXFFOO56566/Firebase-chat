package com.devlomi.fireapp.events;

/**
 * Created by Devlomi on 06/01/2018.
 */

public class OnNetworkComplete {
    private String id;

    public OnNetworkComplete(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
