package com.devlomi.fireapp.model.realms;

import com.devlomi.fireapp.utils.TimeHelper;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Devlomi on 03/08/2017.
 */

public class Chat extends RealmObject {
    @PrimaryKey
    //index for faster querying
    @Index
    private String chatId;
    //last message for this chat
    private Message lastMessage;
    //last message time ,this is used when deleting last message
    // to save the last message time stamp and keep chatList ordered
    private String lastMessageTimestamp;
    //the user in this chat
    private User user;

    //if this chat is muted
    private boolean isMuted;
    //unread messages count
    private int unReadCount;
    //this is used when there is unread messages
    // to scroll to the first unReadMessage rather than scroll to last in recyclerView
    //and if it's empty there is no unread count
    private String firstUnreadMessageId = "";

    private int notificationId;
    private RealmList<Message> unreadMessages;





    public String getFirstUnreadMessageId() {
        return firstUnreadMessageId;
    }

    public void setFirstUnreadMessageId(String firstUnreadMessageId) {
        this.firstUnreadMessageId = firstUnreadMessageId;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message message) {
        this.lastMessage = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public int getUnReadCount() {
        return unReadCount;
    }

    public void setUnReadCount(int unReadCount) {
        this.unReadCount = unReadCount;
    }

    public RealmList<Message> getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(RealmList<Message> unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    //to use list.contains or list.indexOf
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Chat) {
            Chat chat = (Chat) obj;
            if (chat.getChatId().equals(this.chatId))
                return true;
        }
        return false;
    }

    //to print user.toString() properly (debugging purposes)
    @Override
    public String toString() {
        return "Chat{" +
                "chatId='" + chatId + '\'' +
                ", lastMessageTimestamp='" + lastMessageTimestamp + '\'' +
                ", isMuted=" + isMuted +
                ", unReadCount=" + unReadCount +
                ", firstUnreadMessageId='" + firstUnreadMessageId + '\'' +
                ", notificationId='" + notificationId + '\'' +
                ", user=" + user +
                '}';
    }

    public String getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public String getTime() {
        return TimeHelper.getMessageTime(lastMessageTimestamp);
    }


    public void setLastMessageTimestamp(String lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }
}
