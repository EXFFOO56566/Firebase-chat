package com.devlomi.fireapp.model.realms;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.events.UpdateGroupEvent;
import com.devlomi.fireapp.model.constants.GroupEventTypes;
import com.devlomi.fireapp.model.constants.MessageType;
import com.devlomi.fireapp.utils.ListUtil;
import com.devlomi.fireapp.utils.MyApp;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.utils.SharedPreferencesManager;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;

@Keep
//group event will contains what happened in a group
public class GroupEvent extends RealmObject implements Parcelable {
    //context start: is like who is started the event
    private String contextStart;
    //event type to identify what event is it like ADMIN_CHANGED,USER_ADDED etc..
    private int eventType;
    //context start: is like who is affected with this  event
    private String contextEnd;
    private String timestamp;
    private String eventId;


    public String getEventId() {
        return eventId;
    }

    public GroupEvent() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public GroupEvent(String contextStart, int eventType, String contextEnd) {
        this.contextStart = contextStart;
        this.eventType = eventType;
        this.contextEnd = contextEnd;
    }

    public GroupEvent(String contextStart, int eventType, String contextEnd, String eventId) {
        this.contextStart = contextStart;
        this.eventType = eventType;
        this.contextEnd = contextEnd;
        this.eventId = eventId;
    }

    public String getContextStart() {
        return contextStart;
    }

    public void setContextStart(String contextStart) {
        this.contextStart = contextStart;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getContextEnd() {
        return contextEnd;
    }

    public void setContextEnd(String contextEnd) {
        this.contextEnd = contextEnd;
    }


    //this will create the group event and save it as a message inside 'Content'
    //it will be save like: contextStart:eventType:contextEnd
    public void createGroupEvent(User group, String eventId) {
        Message message = new Message();
        message.setGroup(true);
        message.setChatId(group.getUid());
        message.setToId(group.getUid());

        message.setMessageId(eventId == null ? UUID.randomUUID().toString() : eventId);
        String content = "";

        if (contextEnd != null) {
            content = contextStart + ":" + eventType + ":" + contextEnd;
        } else {
            content = contextStart + ":" + eventType;
        }

        message.setContent(content);
        message.setType(MessageType.GROUP_EVENT);
        message.setTimestamp(String.valueOf(new Date().getTime()));
        RealmHelper.getInstance().saveObjectToRealm(message);
        RealmHelper.getInstance().saveChatIfNotExists(message, group);
        EventBus.getDefault().post(new UpdateGroupEvent(group.getUid()));

    }


    //this will extract the string from 'Content' and set as readable-human text
    public static String extractString(String messageContent, RealmList<User> users) {
        try {

            String[] content = messageContent.split(":");

            String contextStart = content[0];
            int eventType = Integer.parseInt(content[1]);

            String finalText = "";
            Resources resources = MyApp.context().getResources();

            String currentUserPhoneNumber = SharedPreferencesManager.getPhoneNumber();

            switch (eventType) {
                case GroupEventTypes.ADMIN_ADDED:
                    String contextEnd = content[2];

                    if (contextEnd.equals(currentUserPhoneNumber)) {
                        finalText = (resources.getString(R.string.you)) + " " + (resources.getString(R.string.are_now_an_admin));
                    } else {
                        finalText = getUserNameFromGroupEvent(contextEnd, users) + " " + (resources.getString(R.string.is_now_an_admin));
                    }

                    break;

                case GroupEventTypes.ADMIN_REMOVED:

                    contextEnd = content[2];

                    if (contextEnd.equals(currentUserPhoneNumber)) {
                        finalText = resources.getString(R.string.you) + " " + resources.getString(R.string.are_no_longer_an_admin);
                    } else {
                        finalText = getUserNameFromGroupEvent(contextEnd, users) + " " + resources.getString(R.string.is_no_longer_an_admin);
                    }
                    break;

                case GroupEventTypes.USER_ADDED:
                    if (contextStart.equals(currentUserPhoneNumber)) {
                        finalText = (resources.getString(R.string.you_added)) + " ";
                    } else {
                        finalText = getUserNameFromGroupEvent(contextStart, users) + " " + (resources.getString(R.string.added)) + " ";
                    }

                    contextEnd = content[2];

                    if (contextEnd.equals(currentUserPhoneNumber)) {
                        finalText += resources.getString(R.string.you);
                    } else {
                        finalText += getUserNameFromGroupEvent(contextEnd, users);
                    }
                    break;

                case GroupEventTypes.USER_LEFT_GROUP:
                    if (contextStart.equals(currentUserPhoneNumber)) {
                        finalText = (resources.getString(R.string.you_left)) + " ";
                    } else {
                        finalText = getUserNameFromGroupEvent(contextStart, users) + " " + (resources.getString(R.string.left_group)) + " ";
                    }
                    break;

                case GroupEventTypes.USER_REMOVED_BY_ADMIN:
                    if (contextStart.equals(currentUserPhoneNumber)) {
                        finalText = (resources.getString(R.string.you_removed)) + " ";
                    } else {
                        finalText = getUserNameFromGroupEvent(contextStart, users) + " " + (resources.getString(R.string.removed)) + " ";
                    }

                    contextEnd = content[2];

                    if (contextEnd.equals(currentUserPhoneNumber)) {
                        finalText += resources.getString(R.string.you);
                    } else {
                        finalText += getUserNameFromGroupEvent(contextEnd, users);
                    }
                    break;

                case GroupEventTypes.GROUP_CREATION:
                    if (contextStart.equals(currentUserPhoneNumber)) {
                        finalText = (resources.getString(R.string.you_created_this_group)) + " ";
                    } else {
                        finalText = getUserNameFromGroupEvent(contextStart, users) + " " + (resources.getString(R.string.created_this_group));
                    }
                    break;


                case GroupEventTypes.GROUP_SETTINGS_CHANGED:
                    if (contextStart.equals(currentUserPhoneNumber)) {
                        finalText = (resources.getString(R.string.you_changed_group_preferences)) + " ";
                    } else {
                        finalText = getUserNameFromGroupEvent(contextStart, users) + " " + (resources.getString(R.string.changed_group_preferences));
                    }
                    break;

                case GroupEventTypes.JOINED_VIA_LINK:
                    if (contextStart.equals(currentUserPhoneNumber)) {
                        finalText = resources.getString(R.string.you) + " " + resources.getString(R.string.joined_via_invite_link);
                    } else {
                        finalText = getUserNameFromGroupEvent(contextStart, users) + " " + (resources.getString(R.string.joined_via_invite_link));
                    }

                    break;
            }
            return finalText;
        } catch (Exception e) {

        }
        return "";
    }


    //this will get the user name from the number
    private static String getUserNameFromGroupEvent(String number, RealmList<User> users) {
        User user = ListUtil.getUserByNumber(number, users);
        if (user != null)
            return user.getUserName();

        return number;
    }

    @Override
    public String toString() {
        return "GroupEvent{" +
                "contextStart='" + contextStart + '\'' +
                ", eventType=" + eventType +
                ", contextEnd='" + contextEnd + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.contextStart);
        dest.writeInt(this.eventType);
        dest.writeString(this.contextEnd);
        dest.writeString(this.timestamp);
        dest.writeString(this.eventId);
    }

    protected GroupEvent(Parcel in) {
        this.contextStart = in.readString();
        this.eventType = in.readInt();
        this.contextEnd = in.readString();
        this.timestamp = in.readString();
        this.eventId = in.readString();
    }

    public static final Parcelable.Creator<GroupEvent> CREATOR = new Parcelable.Creator<GroupEvent>() {
        public GroupEvent createFromParcel(Parcel source) {
            return new GroupEvent(source);
        }

        public GroupEvent[] newArray(int size) {
            return new GroupEvent[size];
        }
    };


}
