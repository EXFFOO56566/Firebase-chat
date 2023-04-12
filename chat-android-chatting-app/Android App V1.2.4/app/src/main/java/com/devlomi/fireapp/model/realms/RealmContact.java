package com.devlomi.fireapp.model.realms;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Devlomi on 17/01/2018.
 */


//Contact to send or receive
public class RealmContact extends RealmObject implements Parcelable {
    //contact name
    private String name;
    //list of phoneNumber of the contact
    private RealmList<PhoneNumber> realmList;


    public RealmContact() {
    }

    public RealmContact(String name, ArrayList<PhoneNumber> numbers) {
        this.name = name;

        //this hack is to make realmList Parcelable
        //more info at:
        // https://stackoverflow.com/questions/43619845/how-to-make-a-realmlist-parcelable
        realmList = new RealmList<>();
        realmList.addAll(numbers);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<PhoneNumber> getRealmList() {
        return realmList;
    }

    public HashMap<String, Boolean> toMap() {
        HashMap<String, Boolean> numbers = new HashMap<>();
        for (PhoneNumber phoneNumber : realmList) {
            numbers.put(phoneNumber.getNumber(), true);
        }
        return numbers;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeTypedList(realmList);
    }

    protected RealmContact(Parcel in) {
        this.name = in.readString();
        this.realmList = new RealmList<>();
        this.realmList.addAll(in.createTypedArrayList(PhoneNumber.CREATOR));
    }

    public static final Creator<RealmContact> CREATOR = new Creator<RealmContact>() {
        public RealmContact createFromParcel(Parcel source) {
            return new RealmContact(source);
        }

        public RealmContact[] newArray(int size) {
            return new RealmContact[size];
        }
    };
}
