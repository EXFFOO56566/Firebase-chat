package com.devlomi.fireapp.model.realms;

import com.devlomi.fireapp.utils.TimeHelper;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class FireCall extends RealmObject {
    @PrimaryKey
    private String callId;
    private User user;
    private int type;
    private long timestamp;
    private int duration;
    private String phoneNumber;
    private boolean isVideo;

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        //convert it to milliseconds if needed
        if (!TimeHelper.isTimestampInMillis(timestamp))
            this.timestamp = timestamp * 1000;
        else
            this.timestamp = timestamp;
    }

    public int getDuration() {
        return duration;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public FireCall() {
    }



    public FireCall(String callId, User user, int type, long timestamp, String phoneNumber, boolean isVideo) {
        this.callId = callId;
        this.user = user;
        this.type = type;
        this.phoneNumber = phoneNumber;
        this.isVideo = isVideo;

        //convert it to milliseconds if needed
        if (!TimeHelper.isTimestampInMillis(timestamp))
            this.timestamp = timestamp * 1000;
        else
            this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "FireCall{" +
                "callId='" + callId + '\'' +
                ", user=" + user +
                ", type=" + type +
                ", timestamp=" + timestamp +
                ", duration=" + duration +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", isVideo=" + isVideo +
                '}';
    }
}
