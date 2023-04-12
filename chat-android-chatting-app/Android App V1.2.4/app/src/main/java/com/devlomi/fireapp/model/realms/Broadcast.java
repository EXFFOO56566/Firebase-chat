package com.devlomi.fireapp.model.realms;

import com.devlomi.fireapp.utils.TimeHelper;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Broadcast extends RealmObject {
    @PrimaryKey
    private String broadcastId;
    private String createdByNumber;
    private long timestamp;
    private RealmList<User> users;

    public String getBroadcastId() {
        return broadcastId;
    }

    public void setBroadcastId(String broadcastId) {
        this.broadcastId = broadcastId;
    }

    public String getCreatedByNumber() {
        return createdByNumber;
    }

    public void setCreatedByNumber(String createdByNumber) {
        this.createdByNumber = createdByNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public RealmList<User> getUsers() {
        return users;
    }

    public void setUsers(RealmList<User> users) {
        this.users = users;
    }

    public String getTime() {
        return TimeHelper.getDate(timestamp);
    }
}
