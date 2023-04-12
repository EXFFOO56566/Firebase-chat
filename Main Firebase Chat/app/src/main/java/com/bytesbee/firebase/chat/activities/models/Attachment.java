package com.bytesbee.firebase.chat.activities.models;

import android.os.Parcel;
import android.os.Parcelable;


public class Attachment implements Parcelable {
    private String name, fileName, data, url, duration;
    private long bytesCount;

    public Attachment() {
    }

    protected Attachment(Parcel in) {
        name = in.readString();
        fileName = in.readString();
        data = in.readString();
        url = in.readString();
        duration = in.readString();
        bytesCount = in.readLong();
    }

    public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
        @Override
        public Attachment createFromParcel(Parcel in) {
            return new Attachment(in);
        }

        @Override
        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName != null ? fileName : "";
    }

    public void setFileName(String name) {
        this.fileName = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public long getBytesCount() {
        return bytesCount;
    }

    public void setBytesCount(long bytesCount) {
        this.bytesCount = bytesCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(fileName);
        dest.writeString(data);
        dest.writeString(url);
        dest.writeString(duration);
        dest.writeLong(bytesCount);
    }
}

