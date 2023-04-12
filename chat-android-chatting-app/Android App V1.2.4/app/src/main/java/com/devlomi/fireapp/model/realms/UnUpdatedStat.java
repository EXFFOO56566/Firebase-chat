package com.devlomi.fireapp.model.realms;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Devlomi on 18/03/2018.
 */

//this will contains the messages with "not yet" updated state
//when received a message we want to update the message state to received
//when there is no internet connection
public class UnUpdatedStat extends RealmObject {
    @PrimaryKey
    private String messageId;
    private String myUid;
    //state to update (received,read)
    private int statToBeUpdated;

    public UnUpdatedStat() {
    }

    public UnUpdatedStat(String messageId, String myUid, int statToBeUpdated) {
        this.messageId = messageId;
        this.myUid = myUid;
        this.statToBeUpdated = statToBeUpdated;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getStatToBeUpdated() {
        return statToBeUpdated;
    }

    public void setStatToBeUpdated(int statToBeUpdated) {
        this.statToBeUpdated = statToBeUpdated;
    }

    public String getMyUid() {
        return myUid;
    }

    public void setMyUid(String myUid) {
        this.myUid = myUid;
    }
}
