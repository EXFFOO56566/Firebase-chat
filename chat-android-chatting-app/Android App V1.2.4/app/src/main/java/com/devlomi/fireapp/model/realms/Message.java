package com.devlomi.fireapp.model.realms;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.devlomi.fireapp.model.constants.DBConstants;
import com.devlomi.fireapp.model.constants.DownloadUploadStat;
import com.devlomi.fireapp.model.constants.MessageType;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.utils.SharedPreferencesManager;
import com.devlomi.fireapp.utils.TimeHelper;
import com.google.firebase.database.ServerValue;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmObject;
import io.realm.annotations.Index;


public class Message extends RealmObject implements Parcelable, Comparable {
    @Index
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
    //message timestamp
    private String timestamp;
    @Index
    private String chatId;
    //messageState if it's pending,sent,read or received
    private int messageStat;
    //media path in device storage
    private String localPath;
    //download upload state (loading,cancelled,success,finished)
    private int downloadUploadStat;
    //metadata could be (fileSize,videoSize or fileName)
    private String metadata;
    //is voice message listened by the receiver
    private boolean voiceMessageSeen;
    //media total duration (audio,voice or video length)
    private String mediaDuration;
    //blurred thumb decoded as BASE64
    //this is used when a user sends an image or video to another user
    //and that user did not download the image or video so it can show what content it is before downloading
    private String thumb;
    private boolean isForwarded;
    //video thumb (not blurred) used to show thumb for a video in recyclerView
    //it is also decoded as BASE64
    private String videoThumb;
    //file size for (file,video,audio,image) types
    private String fileSize;
    //when sending or receiving a contact
    private RealmContact contact;
    //when sending or receiving a location
    private RealmLocation location;
    //when sending a broadcast message we will copy
    //the same message to every user that sent the message to him
    //and to keep track of the sent message we will store the unique id of the original message
    //and then update the message state depending on broadcastedMessageId
    private boolean isGroup;
    private boolean isBroadcast;
    //this is used to indicate if the message was seen by the uesr
    //currentFontIndex'ts used to last 7 notification on APIs below API24
    private boolean isSeen;

    //used when replying to other messages
    private QuotedMessage quotedMessage;


    //this is used to convert the object to a Map to send it to Firebase Database
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put(DBConstants.FROM_ID, fromId);
        result.put(DBConstants.TYPE, type);
        result.put(DBConstants.CONTENT, content);
        result.put(DBConstants.TIMESTAMP, ServerValue.TIMESTAMP);

        //if it's a group we don't need to add this since the groupId will be stored in Firebase Database
        if (isGroup)
            result.put("fromPhone", SharedPreferencesManager.getPhoneNumber());
        else if (!isBroadcast)
            result.put(DBConstants.TOID, toId);

        //if there is a mediaDuration in this message add it to the map
        if (mediaDuration != null)
            result.put(DBConstants.MEDIADURATION, mediaDuration);

        //if there is a thumb in this message add it to the map
        if (thumb != null)
            result.put(DBConstants.THUMB, thumb);

        //if there is a metadata in this message add it to the map
        if (metadata != null)
            result.put(DBConstants.METADATA, metadata);

        //if there is a fileSize in this message add it to the map
        if (fileSize != null)
            result.put(DBConstants.FILESIZE, fileSize);

        //if there is a contact in this message add it to the map
        if (contact != null) {
            //adding phone Numbers
            //NOTE: The PhoneContact name will be in "Content Field"
            result.put(DBConstants.CONTACT, contact.toMap());
        }

        //if there is a location in this message add it to the map
        if (location != null) {
            result.put(DBConstants.LOCATION, location.toMap());
        }

        //Quoted Message
        if (quotedMessage != null)
            result.put("quotedMessageId", quotedMessage.getMessageId());

        //return final map
        return result;
    }


    public Message() {
    }

    public void setVoiceMessageSeen(boolean voiceMessageSeen) {
        this.voiceMessageSeen = voiceMessageSeen;
    }

    public boolean isVoiceMessageSeen() {
        return voiceMessageSeen;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getVideoThumb() {
        return videoThumb;
    }

    public void setVideoThumb(String videoThumb) {
        this.videoThumb = videoThumb;
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

    //get formatted time
    public String getTime() {
        return TimeHelper.getMessageTime(timestamp);

    }


    public String getChatPartnerId() {
        return fromId.equals(FireManager.getUid()) ? toId : fromId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public int getMessageStat() {
        return messageStat;
    }

    public void setMessageStat(int messageStat) {
        this.messageStat = messageStat;
    }

    public String getFromPhone() {
        return fromPhone;
    }

    public void setFromPhone(String fromPhone) {
        this.fromPhone = fromPhone;
    }

    public boolean isBroadcast() {
        return isBroadcast;
    }

    public void setBroadcast(boolean broadcast) {
        isBroadcast = broadcast;
    }



    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", fromId='" + fromId + '\'' +
                ", fromPhone='" + fromPhone + '\'' +
                ", toId='" + toId + '\'' +
                ", type=" + type +
                ", content='" + content + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", chatId='" + chatId + '\'' +
                ", messageStat=" + messageStat +
                ", localPath='" + localPath + '\'' +
                ", downloadUploadStat=" + downloadUploadStat +
                ", metadata='" + metadata + '\'' +
                ", voiceMessageSeen=" + voiceMessageSeen +
                ", mediaDuration='" + mediaDuration + '\'' +
                ", thumb='" + "a thumb here " + '\'' +
                ", isForwarded=" + isForwarded +
                ", videoThumb='" + "Video thumb here" + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", contact=" + contact +
                ", location=" + location +
                ", isGroup=" + isGroup +
                '}';
    }

    //to use list.contains or list.indexOf
    @Override
    public boolean equals(Object o) {
        if (o instanceof Message) {
            Message temp = (Message) o;
            if (this.messageId.equals(temp.getMessageId()))
                return true;
        }
        return false;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }


    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }


    public int getDownloadUploadStat() {
        return downloadUploadStat;
    }

    public void setDownloadUploadStat(int downloadUploadStat) {
        this.downloadUploadStat = downloadUploadStat;
    }


    public void setMediaDuration(String mediaDuration) {
        this.mediaDuration = mediaDuration;
    }

    public String getMediaDuration() {
        return mediaDuration;
    }


    public boolean isForwarded() {
        return isForwarded;
    }

    public void setForwarded(boolean forwarded) {
        isForwarded = forwarded;
    }


    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }


    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public boolean isExists() {
        return RealmHelper.getInstance().isExists(messageId);
    }


    public boolean isVideo() {
        return type == MessageType.SENT_VIDEO || type == MessageType.RECEIVED_VIDEO;
    }


    public boolean isTextMessage() {
        return type == MessageType.SENT_TEXT || type == MessageType.RECEIVED_TEXT;
    }


    public boolean isImage() {
        return type == MessageType.SENT_IMAGE || type == MessageType.RECEIVED_IMAGE;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }


    public QuotedMessage getQuotedMessage() {
        return quotedMessage;
    }

    public void setQuotedMessage(QuotedMessage quotedMessage) {
        this.quotedMessage = quotedMessage;
    }

    //clone message to another message to edit it and forward it
    public Message getClonedMessage() {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setFromId(fromId);
        message.setToId(toId);
        message.setType(type);
        message.setContent(content);
        message.setTimestamp(timestamp);
        message.setChatId(chatId);
        message.setMessageStat(messageStat);
        message.setDownloadUploadStat(downloadUploadStat);
        message.setLocalPath(localPath);
        message.setMetadata(metadata);
        message.setVoiceMessageSeen(voiceMessageSeen);
        message.setThumb(thumb);
        message.setVideoThumb(videoThumb);
        message.setMediaDuration(mediaDuration);
        message.setFileSize(fileSize);
        message.setForwarded(isForwarded);
        message.setLocation(location);
        message.setContact(contact);
        message.setGroup(isGroup);
        return message;
    }

    //clone message EXACTLY WITH ALL PROPERTIES
    public Message cloneExactly() {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setFromId(fromId);
        message.setToId(toId);
        message.setType(type);
        message.setContent(content);
        message.setTimestamp(timestamp);
        message.setChatId(chatId);
        message.setMessageStat(messageStat);
        message.setDownloadUploadStat(downloadUploadStat);
        message.setLocalPath(localPath);
        message.setMetadata(metadata);
        message.setVoiceMessageSeen(voiceMessageSeen);
        message.setThumb(thumb);
        message.setVideoThumb(videoThumb);
        message.setMediaDuration(mediaDuration);
        message.setFileSize(fileSize);
        message.setForwarded(isForwarded);
        message.setLocation(location);
        message.setContact(contact);
        message.setGroup(isGroup);
        message.setQuotedMessage(quotedMessage);
        message.setBroadcast(isBroadcast);
        message.setGroup(isGroup);
        return message;
    }


    public static int getPosFromId(String messageId, List<Message> messages) {
        Message message = new Message();
        message.setMessageId(messageId);
        return messages.indexOf(message);
    }

    public boolean isVoiceMessage() {
        return type == MessageType.SENT_VOICE_MESSAGE || type == MessageType.RECEIVED_VOICE_MESSAGE;
    }

    public boolean isContactMessage() {
        return type == MessageType.SENT_CONTACT || type == MessageType.RECEIVED_CONTACT;
    }

    public boolean isMediaType() {
        return
                type == MessageType.SENT_IMAGE ||
                        type == MessageType.RECEIVED_IMAGE ||
                        type == MessageType.SENT_VIDEO ||
                        type == MessageType.RECEIVED_VIDEO ||
                        type == MessageType.SENT_AUDIO ||
                        type == MessageType.RECEIVED_AUDIO ||
                        type == MessageType.SENT_VOICE_MESSAGE ||
                        type == MessageType.RECEIVED_VOICE_MESSAGE ||
                        type == MessageType.RECEIVED_FILE ||
                        type == MessageType.SENT_FILE;

    }

    public boolean isLocation() {
        return type == MessageType.SENT_LOCATION || type == MessageType.RECEIVED_LOCATION;
    }

    //Parcelable to pass message object over activities
    @Override
    public int describeContents() {
        return 0;
    }

    //Parcelable
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.messageId);
        dest.writeString(this.fromId);
        dest.writeString(this.toId);
        dest.writeInt(this.type);
        dest.writeString(this.content);
        dest.writeString(this.timestamp);
        dest.writeString(this.chatId);
        dest.writeInt(this.messageStat);
        dest.writeString(this.localPath);
        dest.writeInt(this.downloadUploadStat);
        dest.writeString(this.metadata);
        dest.writeByte(voiceMessageSeen ? (byte) 1 : (byte) 0);
        dest.writeString(this.mediaDuration);
        dest.writeString(this.thumb);
        dest.writeByte(isForwarded ? (byte) 1 : (byte) 0);
        dest.writeString(this.videoThumb);
        dest.writeString(this.fileSize);
        dest.writeParcelable(this.contact, 0);
        dest.writeParcelable(this.location, 0);
        dest.writeByte(isGroup ? (byte) 1 : (byte) 0);
        dest.writeString(fromPhone);

    }

    //Parcelable
    protected Message(Parcel in) {
        this.messageId = in.readString();
        this.fromId = in.readString();
        this.toId = in.readString();
        this.type = in.readInt();
        this.content = in.readString();
        this.timestamp = in.readString();
        this.chatId = in.readString();
        this.messageStat = in.readInt();
        this.localPath = in.readString();
        this.downloadUploadStat = in.readInt();
        this.metadata = in.readString();
        this.voiceMessageSeen = in.readByte() != 0;
        this.mediaDuration = in.readString();
        this.thumb = in.readString();
        this.isForwarded = in.readByte() != 0;
        this.videoThumb = in.readString();
        this.fileSize = in.readString();
        this.contact = in.readParcelable(RealmContact.class.getClassLoader());
        this.location = in.readParcelable(RealmLocation.class.getClassLoader());
        this.isGroup = in.readByte() != 0;
        this.fromPhone = in.readString();
    }

    //Parcelable
    public static final Creator<Message> CREATOR = new Creator<Message>() {
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    //used to sort messages by timestamp when user selects messages and want to copy them
    @Override
    public int compareTo(@NonNull Object o) {
        if (o instanceof Message) {
            Message message = (Message) o;
            Date d1 = new Date(Long.parseLong(timestamp));
            Date d2 = new Date(Long.parseLong(message.getTimestamp()));
            return d1.compareTo(d2);
        }
        return 0;
    }

    public boolean completeAfterDownload() {
        return this != null && getDownloadUploadStat() != DownloadUploadStat.CANCELLED && getType() != MessageType.SENT_DELETED_MESSAGE && getType() != MessageType.RECEIVED_DELETED_MESSAGE;
    }


    public boolean isMessageFromMe() {
        return fromId.equals(FireManager.getUid());
    }
}
