package com.devlomi.fireapp.model.realms;

import com.devlomi.fireapp.model.TextStatus;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.internal.Keep;

@Keep
public class Status extends RealmObject {

    @PrimaryKey
    private String statusId;
    @Index
    private String userId;
    private long timestamp;
    private String thumbImg;
    private String content;
    private String localPath;
    private TextStatus textStatus;
    private int type;
    private long duration;
    //this is for the user when he uploads a status and wants to see how many people saw that status
    private int seenCount;
    //this is for other users when they saw a status we want to make a job to update it on Firebase
    private boolean seenCountSent;
    private boolean isSeen;


    public Status(String statusId, String userId, long timestamp, String thumbImg, String content, String localPath, int type, long duration) {
        this.statusId = statusId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.thumbImg = thumbImg;
        this.content = content;
        this.localPath = localPath;
        this.type = type;
        this.duration = duration;
    }

    public Status(String statusId, String userId, long timestamp, String thumbImg, String content, String localPath, int type) {
        this.statusId = statusId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.thumbImg = thumbImg;
        this.content = content;
        this.localPath = localPath;
        this.type = type;
    }

    public Status(String statusId, String userId, long timestamp, TextStatus textStatus, int type) {
        this.statusId = statusId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.textStatus = textStatus;
        this.type = type;
    }


    public TextStatus getTextStatus() {
        return textStatus;
    }

    public void setTextStatus(TextStatus textStatus) {
        this.textStatus = textStatus;
    }

    public Status() {
    }


    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getStatusId() {
        return statusId;
    }

    public String getUserId() {
        return userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getThumbImg() {
        return thumbImg;
    }

    public String getContent() {
        return content;
    }

    public String getLocalPath() {
        return localPath;
    }

    public int getType() {
        return type;
    }

    public long getDuration() {
        return duration;
    }

    public Map toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", ServerValue.TIMESTAMP);
        result.put("type", type);

        if (thumbImg != null)
            result.put("thumbImg", thumbImg);
        if (content != null)
            result.put("content", content);

            result.put("duration", duration);


        if (textStatus != null) {
            Map<String, Object> textStatusMap = textStatus.toMap();
            for (String key : textStatusMap.keySet()) {
                result.put(key, textStatusMap.get(key));
            }
        }

        return result;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setThumbImg(String thumbImg) {
        this.thumbImg = thumbImg;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getSeenCount() {
        return seenCount;
    }

    public void setSeenCount(int seenCount) {
        this.seenCount = seenCount;
    }

    public boolean isSeenCountSent() {
        return seenCountSent;
    }

    public void setSeenCountSent(boolean seenCountSent) {
        this.seenCountSent = seenCountSent;
    }

    @Override
    public String toString() {
        return "Status{" +
                "statusId='" + statusId + '\'' +
                ", userId='" + userId + '\'' +
                ", timestamp=" + timestamp +
                ", thumbImg='" + "thumbHere " + '\'' +
                ", content='" + content + '\'' +
                ", localPath='" + localPath + '\'' +
                ", type=" + type +
                ", duration=" + duration +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Status status = (Status) o;

        return statusId != null ? statusId.equals(status.statusId) : status.statusId == null;
    }


}
