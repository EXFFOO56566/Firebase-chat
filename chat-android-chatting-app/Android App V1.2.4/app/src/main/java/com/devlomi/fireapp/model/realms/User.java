package com.devlomi.fireapp.model.realms;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import com.devlomi.fireapp.utils.FireManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Devlomi on 03/08/2017.
 */

@Keep
public class User extends RealmObject implements Parcelable {
    @PrimaryKey
    @Index
    //user id
    private String uid;
    //user photo url in server
    private String photo;
    //user status
    private String status;
    private String phone;
    //user photo path in the device
    private String userLocalPhoto;
    //userName saved using query from phonebook
    private String userName;
    private boolean isBlocked;

    //default app version
    private String appVer;
    //user's  thumb img (small image) decodes as BASE64
    private String thumbImg;
    private boolean isGroupBool;
    private Group group;
    private Broadcast broadcast;
    private boolean isBroadcastBool;
    private boolean isStoredInContacts;


    public String getThumbImg() {
        return thumbImg;
    }

    public void setThumbImg(String thumbImg) {
        this.thumbImg = thumbImg;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUserLocalPhoto(String userLocalPhoto) {
        this.userLocalPhoto = userLocalPhoto;
    }

    public boolean isGroupBool() {
        return isGroupBool;
    }

    public void setGroupBool(boolean groupBool) {
        isGroupBool = groupBool;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof User) {
            User temp = (User) o;
            if (this.uid.equals(temp.getUid()))
                return true;
        }
        return false;
    }


    public String getUserLocalPhoto() {
        return userLocalPhoto;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public User() {
        appVer = "1.0";
    }


    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }


    public String getAppVer() {
        return appVer;
    }

    public void setAppVer(String appVer) {
        this.appVer = appVer;
    }

    public boolean isStoredInContacts() {
        return isStoredInContacts;
    }

    public void setStoredInContacts(boolean storedInContacts) {
        isStoredInContacts = storedInContacts;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", photo='" + photo + '\'' +
                ", status='" + status + '\'' +
                ", phone='" + phone + '\'' +
                ", userLocalPhoto='" + userLocalPhoto + '\'' +
                ", appVer='" + appVer + '\'' +
                ", isStored in contacts='" + isStoredInContacts + '\'' +
                ", userName='" + userName + '\'' +
                ", isBlocked=" + isBlocked +
                ", thumbImg='" + "a thumb here" + '\'' +
                ", isGroupBool=" + isGroupBool +
                ", group=" + group +
                '}';
    }

    public Broadcast getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(Broadcast broadcast) {
        this.broadcast = broadcast;
    }

    public boolean isBroadcastBool() {
        return isBroadcastBool;
    }

    public void setBroadcastBool(boolean broadcastBool) {
        isBroadcastBool = broadcastBool;
    }



    public static Map<String, Object> toMap(List<User> userList, boolean addCurrentUser) {
        Map<String, Object> usersMap = new HashMap<>();


        for (User user : userList) {
            usersMap.put(user.getUid(), false);//false indicating if a user is admin
        }

        //adding group creator to the group as an Admin
        if (addCurrentUser)
            usersMap.put(FireManager.getUid(), true);

        return usersMap;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uid);
        dest.writeString(this.photo);
        dest.writeString(this.status);
        dest.writeString(this.phone);
        dest.writeString(this.userLocalPhoto);
        dest.writeString(this.userName);
        dest.writeByte(isBlocked ? (byte) 1 : (byte) 0);
        dest.writeString(this.appVer);
        dest.writeString(this.thumbImg);
        dest.writeByte(isGroupBool ? (byte) 1 : (byte) 0);
        dest.writeByte(isBroadcastBool ? (byte) 1 : (byte) 0);
        dest.writeByte(isStoredInContacts ? (byte) 1 : (byte) 0);
    }

    protected User(Parcel in) {
        this.uid = in.readString();
        this.photo = in.readString();
        this.status = in.readString();
        this.phone = in.readString();
        this.userLocalPhoto = in.readString();
        this.userName = in.readString();
        this.isBlocked = in.readByte() != 0;
        this.appVer = in.readString();
        this.thumbImg = in.readString();
        this.isGroupBool = in.readByte() != 0;
        this.isBroadcastBool = in.readByte() != 0;
        this.isStoredInContacts = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
