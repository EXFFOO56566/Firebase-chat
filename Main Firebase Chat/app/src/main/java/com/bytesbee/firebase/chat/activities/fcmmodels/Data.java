package com.bytesbee.firebase.chat.activities.fcmmodels;

public class Data {
    private String user;
    private int icon;
    private final String username;
    private String body;
    private String title;
    private String sent;
    private String groups;
    private String type;

    public Data(String user, int icon, String username, String body, String title, String sent, String type) {
        this.user = user;
        this.icon = icon;
        this.username = username;
        this.body = body;
        this.title = title;
        this.sent = sent;
        this.type = type;
    }

    public Data(String user, int icon, String username, String body, String title, String sent, String groups, String type) {
        this.user = user;
        this.icon = icon;
        this.username = username;
        this.body = body;
        this.title = title;
        this.sent = sent;
        this.groups = groups;
        this.type = type;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
