package com.devlomi.fireapp.model.realms;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class PendingGroupJob extends RealmObject {

    public PendingGroupJob() {
    }

    @PrimaryKey
    private String groupId;
    private int type;
    private GroupEvent groupEvent;

    public PendingGroupJob(String groupId, int type ,GroupEvent groupEvent) {
        this.groupId = groupId;
        this.type = type;
        this.groupEvent = groupEvent;
    }

    public GroupEvent getGroupEvent() {
        return groupEvent;
    }

    public String getGroupId() {
        return groupId;
    }

    public int getType() {
        return type;
    }
}

