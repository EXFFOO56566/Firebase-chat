package com.devlomi.fireapp.events;

public class UpdateGroupEvent {

    private String groupId;

    public UpdateGroupEvent( String groupId) {
        this.groupId = groupId;
    }


    public String getGroupId() {
        return groupId;
    }
}
