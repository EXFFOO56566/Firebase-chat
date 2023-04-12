package com.devlomi.fireapp.model;

/**
 * Created by Devlomi on 02/12/2017.
 */

// save/change network progress  state in recyclerView
public class ProgressData {

    private int progress;
    private String receiverId;
    private String messageId;

    public ProgressData( int progress, String receiverId, String messageId) {

        this.progress = progress;
        this.receiverId = receiverId;
        this.messageId = messageId;
    }


    public int getProgress() {
        return progress;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getMessageId() {
        return messageId;
    }
}
