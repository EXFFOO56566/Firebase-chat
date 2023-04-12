package com.devlomi.fireapp.events;

public class GroupActiveStateChanged {
    private String groupId;
    private boolean isActive;

    public GroupActiveStateChanged(String groupId, boolean isActive) {
        this.groupId = groupId;
        this.isActive = isActive;
    }

    public String getGroupId() {
        return groupId;
    }

    public boolean isActive() {
        return isActive;
    }
}
