package com.bytesbee.firebase.chat.activities.models;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.FALSE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.GEN_UNSPECIFIED;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.IMG_PREVIEW;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.STATUS_OFFLINE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_EMAIL;

import com.bytesbee.firebase.chat.activities.managers.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.net.URLDecoder;

public class User implements Serializable {
    private String id;
    private String username;
    private String email;
    private String imageURL;
    private String status;
    private int isOnline = STATUS_OFFLINE;
    private String search;
    private String password;
    private boolean active;
    private boolean typing;
    private String typingwith;
    private String about;
    private String gender;
    private int genders = GEN_UNSPECIFIED;
    private String lastSeen;
    private boolean isChecked;
    private boolean isAdmin;
    private boolean hideEmail = FALSE;
    private boolean hideProfilePhoto = FALSE;
    private String signup_type = TYPE_EMAIL;
    private String social_token = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageURL() {
        if (isHideProfilePhoto()) {
            return IMG_PREVIEW;
        }
        return imageURL;
    }

    public String getMyImg() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        if (imageURL.startsWith("https%3A%2F%2") || imageURL.startsWith("http%3A%2F%2")) {
            try {
                imageURL = URLDecoder.decode(imageURL, "UTF-8");
            } catch (Exception e) {
                Utils.getErrors(e);
            }
        }
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }

    public String getTypingwith() {
        return typingwith;
    }

    public void setTypingwith(String typingwith) {
        this.typingwith = typingwith;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isHideEmail() {
        return hideEmail;
    }

    public void setHideEmail(boolean hideEmail) {
        this.hideEmail = hideEmail;
    }

    public boolean isHideProfilePhoto() {
        return hideProfilePhoto;
    }

    public void setHideProfilePhoto(boolean hideProfilePhoto) {
        this.hideProfilePhoto = hideProfilePhoto;
    }

    public int getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }

    public int getGenders() {
        return genders;
    }

    public void setGenders(int genders) {
        this.genders = genders;
    }

    public String getSignup_type() {
        return signup_type;
    }

    public void setSignup_type(String signup_type) {
        this.signup_type = signup_type;
    }

    public String getSocial_token() {
        return social_token;
    }

    public void setSocial_token(String social_token) {
        this.social_token = social_token;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", status='" + status + '\'' +
                ", isOnline=" + isOnline +
                ", search='" + search + '\'' +
                ", password='" + password + '\'' +
                ", active=" + active +
                ", typing=" + typing +
                ", typingwith='" + typingwith + '\'' +
                ", about='" + about + '\'' +
                ", gender='" + gender + '\'' +
                ", genders=" + genders +
                ", lastSeen='" + lastSeen + '\'' +
                ", isChecked=" + isChecked +
                ", isAdmin=" + isAdmin +
                ", hideEmail=" + hideEmail +
                ", hideProfilePhoto=" + hideProfilePhoto +
                ", signup_type='" + signup_type + '\'' +
                ", social_token='" + social_token + '\'' +
                '}';
    }
}
