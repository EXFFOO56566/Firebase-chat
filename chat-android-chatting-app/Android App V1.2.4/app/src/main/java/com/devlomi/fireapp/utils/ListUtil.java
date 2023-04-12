package com.devlomi.fireapp.utils;

import com.devlomi.fireapp.model.realms.User;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;


public class ListUtil {




    public static int getPosFromIdUser(String id, List<User> list) {
        User user = new User();
        user.setUid(id);
        return list.indexOf(user);
    }


    public static User getUserById(String id, RealmList<User> list) {
        User user = new User();
        user.setUid(id);
        if (list.indexOf(user) == -1) {
            return RealmHelper.getInstance().getUser(id);
        }
        return list.get(list.indexOf(user));
    }



    public static User getUserByNumber(String phone, RealmList<User> users) {

        for (User user : users) {
            if (user != null && phone != null && user.getPhone() != null && user.getPhone().equals(phone))
                return user;
        }

        return RealmHelper.getInstance().getUserByNumber(phone);
    }


    public static List<String> distinct(List<String> list1, List<String> list2) {
        // Make the two lists

// Prepare a union
        List<String> union = new ArrayList<String>(list1);
        union.addAll(list2);
// Prepare an intersection
        List<String> intersection = new ArrayList<String>(list1);
        intersection.retainAll(list2);
// Subtract the intersection from the union
        union.removeAll(intersection);

        return union;
    }

}
