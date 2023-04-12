package com.devlomi.fireapp.utils;

import android.util.Log;

import com.devlomi.fireapp.model.realms.PhoneNumber;
import com.devlomi.fireapp.model.realms.RealmLocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Devlomi on 22/03/2018.
 */

//this is used to convert the received JSONs location or contact
//and convert them to the correct object
public class JsonUtil {

    //convert json location to RealmLocation object
    public static RealmLocation getRealmLocationFromJson(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            double lat = jsonObject.getDouble("lat");
            double lng = jsonObject.getDouble("lng");
            String address = jsonObject.getString("address");
            String name = jsonObject.getString("name");
            return new RealmLocation(lat, lng, address, name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    //convert json contact numbers to RealmContact object
    public static ArrayList<PhoneNumber> getPhoneNumbersList(String jsonString) {
        ArrayList<PhoneNumber> numberList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            Iterator<String> iter = jsonObject.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                PhoneNumber phoneNumber = new PhoneNumber(key);
                numberList.add(phoneNumber);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return numberList;

    }

}
