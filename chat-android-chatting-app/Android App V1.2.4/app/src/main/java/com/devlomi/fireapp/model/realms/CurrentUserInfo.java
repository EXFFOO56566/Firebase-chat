package com.devlomi.fireapp.model.realms;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CurrentUserInfo extends RealmObject {
    @PrimaryKey
    private String uid;
    private String phone;

    public CurrentUserInfo(String uid, String phone) {
        this.uid = uid;
        this.phone = phone;
    }

    public CurrentUserInfo() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
