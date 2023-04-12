package com.devlomi.fireapp.model.realms;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

//this realm will contain the Deleted Message Id
//if the message was deleted by the sender and it's not received by recipient yet
//since fcm does not guarantee the deliver order
//therefore if the 'delete message event' received before the message itself
//it will be saved here and then we will check if the received message is deleted
//it will not saved to Message Realm
public class DeletedMessage extends RealmObject {

    public DeletedMessage() {
    }

    @PrimaryKey
    private String messageId;

    public DeletedMessage(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

}
