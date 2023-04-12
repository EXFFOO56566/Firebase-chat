package com.devlomi.fireapp.model.realms;

import io.realm.RealmObject;

public class QuotedMessage extends RealmObject {
    private String messageId;
    //sender id
    private String fromId;
    //this field is used for groups
    //if a user is not exists in group and he sent a message previously
    //we will show his phone only
    private String fromPhone;
    //receiver id
    private String toId;
    //message type (text,image,video etc..)
    private int type;
    //message content (text content,media item path in database)
    private String content;
    //metadata could be (fileSize,videoSize or fileName)
    private String metadata;
    //media total duration (audio,voice or video length)
    private String mediaDuration;
    //blurred thumb decoded as BASE64
    //this is used when a user sends an image or video to another user
    //and that user did not download the image or video so it can show what content it is before downloading
    private String thumb;
    //file size for (file,video,audio,image) types
    private String fileSize;
    //when sending or receiving a contact
    private RealmContact contact;
    //when sending or receiving a location
    private RealmLocation location;
    private boolean isBroadcast;


    public static Message quotedMessageToMessage(QuotedMessage quotedMessage) {
        Message message = new Message();
        message.setContact(quotedMessage.getContact());
        message.setContent(quotedMessage.getContent());
        message.setFileSize(quotedMessage.getFileSize());
        message.setFromId(quotedMessage.getFromId());
        message.setFromPhone(quotedMessage.getFromPhone());
        message.setLocation(quotedMessage.getLocation());
        message.setMediaDuration(quotedMessage.getMediaDuration());
        message.setMessageId(quotedMessage.getMessageId());
        message.setThumb(quotedMessage.getThumb());
        message.setType(quotedMessage.getType());
        message.setToId(quotedMessage.getToId());
        message.setMetadata(quotedMessage.getMetadata());
        message.setBroadcast(quotedMessage.isBroadcast);
        return message;
    }


    public static QuotedMessage messageToQuotedMessage(Message message) {
        QuotedMessage quotedMessage = new QuotedMessage();
        quotedMessage.setContact(message.getContact());
        quotedMessage.setContent(message.getContent());
        quotedMessage.setFileSize(message.getFileSize());
        quotedMessage.setFromId(message.getFromId());
        quotedMessage.setFromPhone(message.getFromPhone());
        quotedMessage.setLocation(message.getLocation());
        quotedMessage.setMediaDuration(message.getMediaDuration());
        quotedMessage.setMessageId(message.getMessageId());
        quotedMessage.setThumb(message.getThumb());
        quotedMessage.setType(message.getType());
        quotedMessage.setToId(message.getToId());
        quotedMessage.setMetadata(message.getMetadata());
        quotedMessage.setBroadcast(message.isBroadcast());
        return quotedMessage;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getFromPhone() {
        return fromPhone;
    }

    public void setFromPhone(String fromPhone) {
        this.fromPhone = fromPhone;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getMediaDuration() {
        return mediaDuration;
    }

    public void setMediaDuration(String mediaDuration) {
        this.mediaDuration = mediaDuration;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }


    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public RealmContact getContact() {
        return contact;
    }

    public void setContact(RealmContact contact) {
        this.contact = contact;
    }

    public RealmLocation getLocation() {
        return location;
    }

    public void setLocation(RealmLocation location) {
        this.location = location;
    }

    public boolean isBroadcast() {
        return isBroadcast;
    }

    public void setBroadcast(boolean broadcast) {
        isBroadcast = broadcast;
    }


}
