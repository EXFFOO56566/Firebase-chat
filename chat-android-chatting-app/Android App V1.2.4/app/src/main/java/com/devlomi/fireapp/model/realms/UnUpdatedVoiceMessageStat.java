package com.devlomi.fireapp.model.realms;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Devlomi on 18/03/2018.
 */

//this will contains the messages with "not yet" updated state
//when listened to  a message we want to update the message state to READ
//when there is no internet connection
public class UnUpdatedVoiceMessageStat extends RealmObject {
    @PrimaryKey
    private String messageId;
    private String myUid;
    private boolean isVoiceMessageSeen;

    public UnUpdatedVoiceMessageStat() {
    }

    public UnUpdatedVoiceMessageStat(String messageId, String myUid, boolean isVoiceMessageSeen) {
        this.messageId = messageId;
        this.myUid = myUid;
        this.isVoiceMessageSeen = isVoiceMessageSeen;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMyUid() {
        return myUid;
    }

    public void setMyUid(String myUid) {
        this.myUid = myUid;
    }

    public boolean isVoiceMessageSeen() {
        return isVoiceMessageSeen;
    }

    public void setVoiceMessageSeen(boolean voiceMessageSeen) {
        isVoiceMessageSeen = voiceMessageSeen;
    }
}
