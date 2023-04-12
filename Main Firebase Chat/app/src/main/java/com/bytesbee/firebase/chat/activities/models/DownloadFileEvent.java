package com.bytesbee.firebase.chat.activities.models;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;


public class DownloadFileEvent implements Serializable {
    private Chat attachment;
    private int position;

    public DownloadFileEvent(Chat attachment, int adapterPosition) {
        this.attachment = attachment;
        this.position = adapterPosition;
    }

    public Chat getAttachment() {
        return attachment;
    }

    public void setAttachment(Chat attachment) {
        this.attachment = attachment;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @NotNull
    @Override
    public String toString() {
        return "DownloadFileEvent{" +
                "attachment=" + attachment +
                ", position=" + position +
                '}';
    }
}

