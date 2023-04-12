package com.devlomi.fireapp.events;

public class UserImageDownloadedEvent {
    String path;

    public String getPath() {
        return path;
    }

    public UserImageDownloadedEvent(String path) {
        this.path = path;
    }
}
