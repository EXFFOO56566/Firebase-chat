package com.devlomi.fireapp.utils;

import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;

import com.devlomi.fireapp.model.realms.GroupEvent;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PBundleUtil {
    private final String action;
    private final String messageId;
    private final String myUid;
    private final int stat;
    private final String groupId;
    private final GroupEvent groupEvent;
    private final String id;
    private final String chatId;

    private PBundleUtil(Builder builder) {
        this.action = builder.action;
        this.messageId = builder.messageId;
        this.myUid = builder.myUid;
        this.stat = builder.stat;
        this.groupId = builder.groupId;
        this.groupEvent = builder.groupEvent;
        this.id = builder.id;
        this.chatId = builder.chatId;
    }

    public PersistableBundle getBundle() {
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(IntentUtils.ACTION_TYPE, action);
        bundle.putString(IntentUtils.ID, id);
        if (messageId != null)
            bundle.putString(IntentUtils.EXTRA_MESSAGE_ID, messageId);
        if (myUid != null)
            bundle.putString(IntentUtils.EXTRA_MY_UID, myUid);
        if (stat != 0)
            bundle.putInt(IntentUtils.EXTRA_STAT, stat);
        if (groupId != null)
            bundle.putString(IntentUtils.EXTRA_GROUP_ID, groupId);
        if (groupEvent != null)
            bundle.putPersistableBundle(IntentUtils.EXTRA_GROUP_EVENT, groupEventToPersistableBundle(groupEvent));
        if (chatId != null)
            bundle.putString(IntentUtils.EXTRA_CHAT_ID, chatId);
        return bundle;
    }


    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String getAction() {
        return action;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public String getMyUid() {
        return myUid;
    }

    public int getStat() {
        return stat;
    }

    public String getGroupId() {
        return groupId;
    }

    public GroupEvent getGroupEvent() {
        return groupEvent;
    }


    public static class Builder {
        private String action;
        private String messageId;
        private String myUid;
        private int stat;
        private String id;
        private String groupId;
        private GroupEvent groupEvent;
        private String chatId;


        public Builder(String id) {
            this.id = id;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder myUid(String myUid) {
            this.myUid = myUid;
            return this;
        }

        public Builder stat(int stat) {
            this.stat = stat;
            return this;
        }

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder groupEvent(GroupEvent groupEvent) {
            this.groupEvent = groupEvent;
            return this;
        }

        public Builder chatId(String chatId) {
            this.chatId = chatId;
            return this;
        }

        public PBundleUtil build() {
            return new PBundleUtil(this);
        }
    }


    //convert group event to PersistableBundle
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private PersistableBundle groupEventToPersistableBundle(GroupEvent groupEvent) {
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(IntentUtils.EXTRA_CONTEXT_START, groupEvent.getContextStart());
        bundle.putInt(IntentUtils.EXTRA_EVENT_TYPE, groupEvent.getEventType());
        bundle.putString(IntentUtils.EXTRA_CONTEXT_END, groupEvent.getContextEnd());
        return bundle;
    }
}
