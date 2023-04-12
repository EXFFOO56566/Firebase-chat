package com.bytesbee.firebase.chat.activities.models;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

public class Groups implements Serializable {
    private String id;
    private String groupName;
    private String admin;
    private String groupImg;
    private String lastMsg;
    private String lastMsgTime;
    private String type;
    private List<String> members;
    private String createdAt;
    private boolean active;
    private int sendMessageSetting; // 0 = All Participants, 1 = Only Admin

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getGroupImg() {
        return groupImg;
    }

    public void setGroupImg(String groupImg) {
        this.groupImg = groupImg;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public String getLastMsgTime() {
        return lastMsgTime;
    }

    public void setLastMsgTime(String lastMsgTime) {
        this.lastMsgTime = lastMsgTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSendMessageSetting() {
        return sendMessageSetting;
    }

    public void setSendMessageSetting(int sendMessageSetting) {
        this.sendMessageSetting = sendMessageSetting;
    }

    @NotNull
    @Override
    public String toString() {
        return "Groups{" +
                "id='" + id + '\'' +
                ", groupName='" + groupName + '\'' +
                ", admin='" + admin + '\'' +
                ", groupImg='" + groupImg + '\'' +
                ", lastMsg='" + lastMsg + '\'' +
                ", lastMsgTime='" + lastMsgTime + '\'' +
                ", members=" + members +
                ", createdAt='" + createdAt + '\'' +
                ", active='" + active + '\'' +
                ", sendMessageSetting='" + sendMessageSetting + '\'' +
                '}';
    }
}
