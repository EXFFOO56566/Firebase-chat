package com.devlomi.fireapp.utils;

import com.devlomi.fireapp.model.constants.DBConstants;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

//this will called when migrating from old version
public class MyMigration implements RealmMigration {
    public static final int SCHEMA_VERSION = 5;

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();


        if (oldVersion == 0) {

            schema.get("Chat")
                    .addField("notificationId", int.class)
                    .addRealmListField("unreadMessages", schema.get("Message"));


            schema.create("DeletedMessage")
                    .addField(DBConstants.MESSAGE_ID, String.class, FieldAttribute.PRIMARY_KEY);


            schema.create("Group")
                    .addField("groupId", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("isActive", boolean.class)
                    .addField("createdByNumber", String.class)
                    .addField("timestamp", long.class)
                    .addRealmListField("users", schema.get("User"))
                    .addRealmListField("adminsUids", String.class)
                    .addField("onlyAdminsCanPost", boolean.class);


            schema.get("Message")
                    .addField("isGroup", boolean.class)
                    .addField(DBConstants.IS_SEEN, boolean.class)
                    .addField("fromPhone", String.class);


            schema.get("User")
                    .addField("appVer", String.class)
                    .addField("isGroupBool", boolean.class)
                    .addRealmObjectField("group", schema.get("Group"))
                    .addField("isStoredInContacts", boolean.class);

            schema.create("GroupEvent")
                    .addField("contextStart", String.class)
                    .addField("eventType", int.class)
                    .addField("contextEnd", String.class)
                    .addField("timestamp", String.class)
                    .addField("eventId", String.class);


            schema.create("PendingGroupJob")
                    .addField("groupId", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("type", int.class)
                    .addRealmObjectField("groupEvent", schema.get("GroupEvent"));

            schema.create("JobId")
                    .addField("id", String.class)
                    .addField(DBConstants.JOB_ID, int.class)
                    .addField("isVoiceMessage", boolean.class);

            //make contacts re-sync again after adding 'isStoredInContacts' field
            SharedPreferencesManager.setContactSynced(false);
            SharedPreferencesManager.setAppVersionSaved(false);

            oldVersion++;
        }

        if (oldVersion == 1) {
            schema.create("FireCall")
                    .addField("callId", String.class, FieldAttribute.PRIMARY_KEY)
                    .addRealmObjectField("user", schema.get("User"))
                    .addField("type", int.class)
                    .addField("timestamp", long.class)
                    .addField("duration", int.class)
                    .addField("phoneNumber", String.class)
                    .addField("isVideo", boolean.class);

            schema.create("Status")
                    .addField("statusId", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("userId", String.class, FieldAttribute.INDEXED)
                    .addField("timestamp", long.class)
                    .addField("thumbImg", String.class)
                    .addField("content", String.class)
                    .addField("localPath", String.class)
                    .addField("type", int.class)
                    .addField("duration", long.class)
                    .addField("seenCount", int.class)
                    .addField("seenCountSent", boolean.class)
                    .addField("isSeen", boolean.class);


            schema.create("UserStatuses")
                    .addField("userId", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("lastStatusTimestamp", long.class)
                    .addRealmObjectField("user", schema.get("User"))
                    .addRealmListField("statuses", schema.get("Status"))
                    .addField("areAllSeen", boolean.class);


            SharedPreferencesManager.setAppVersionSaved(false);


            oldVersion++;
        }
        if (oldVersion == 2) {


            schema.create("QuotedMessage")
                    .addField(DBConstants.MESSAGE_ID, String.class)
                    .addField(DBConstants.FROM_ID, String.class)
                    .addField("fromPhone", String.class)
                    .addField(DBConstants.TOID, String.class)
                    .addField(DBConstants.TYPE, int.class)
                    .addField(DBConstants.CONTENT, String.class)
                    .addField(DBConstants.METADATA, String.class)
                    .addField(DBConstants.MEDIADURATION, String.class)
                    .addField(DBConstants.THUMB, String.class)
                    .addField(DBConstants.FILESIZE, String.class)
                    .addRealmObjectField("contact", schema.get("RealmContact"))
                    .addRealmObjectField("location", schema.get("RealmLocation"));

            schema.get("Message")
                    .addRealmObjectField("quotedMessage", schema.get("QuotedMessage"));

            oldVersion++;
        }

        if (oldVersion == 3) {
            schema.create("CurrentUserInfo")
                    .addField(DBConstants.UID, String.class, FieldAttribute.PRIMARY_KEY)
                    .addField(DBConstants.PHONE, String.class);

            schema.create("Broadcast")
                    .addField("broadcastId", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("createdByNumber", String.class)
                    .addField(DBConstants.TIMESTAMP, long.class)
                    .addRealmListField("users", schema.get("User"));

            schema.get("Group")
                    .addField("currentGroupLink", String.class);

            schema.get("User")
                    .addField("isBroadcastBool", boolean.class)
                    .addRealmObjectField("broadcast", schema.get("Broadcast"));

            schema.get("Message")
                    .removePrimaryKey()
                    .addIndex("messageId")
                    .addField("isBroadcast", boolean.class);

            schema.get("QuotedMessage")
                    .addField("isBroadcast", boolean.class);

            oldVersion++;
        }
        if (oldVersion == 4) {
            schema.create("TextStatus")
                    .addField("statusId", String.class, FieldAttribute.PRIMARY_KEY,FieldAttribute.REQUIRED)
                    .addField("text", String.class, FieldAttribute.REQUIRED)
                    .addField("fontName", String.class, FieldAttribute.REQUIRED)
                    .addField("backgroundColor", String.class, FieldAttribute.REQUIRED);

            schema.get("Status")
                    .addRealmObjectField("textStatus", schema.get("TextStatus"));

            oldVersion++;

        }

    }
}

