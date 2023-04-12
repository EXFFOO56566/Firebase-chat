package com.devlomi.fireapp.model.realms;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

/**
 * Created by Devlomi on 15/01/2018.
 */

//phone number inside realm contact
public class PhoneNumber extends RealmObject implements Parcelable {

    private String number;

    protected PhoneNumber(Parcel in) {
        number = in.readString();
    }

    public PhoneNumber(String number) {
        this.number = number;
    }

    public PhoneNumber() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(number);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PhoneNumber> CREATOR = new Creator<PhoneNumber>() {
        @Override
        public PhoneNumber createFromParcel(Parcel in) {
            return new PhoneNumber(in);
        }

        @Override
        public PhoneNumber[] newArray(int size) {
            return new PhoneNumber[size];
        }
    };

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    //to use list.contains or list.indexOf
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PhoneNumber) {
            PhoneNumber temp = (PhoneNumber) obj;

            if (this.number.equals(temp.getNumber()))
                return true;

        }
        return false;
    }
}
