package com.devlomi.fireapp.utils;

import com.devlomi.fireapp.model.constants.TypingStat;
import com.devlomi.fireapp.model.realms.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.HashMap;
import java.util.List;

public class GroupTyping {
    private List<User> users;
    private HashMap<String, TypingState> typingHashmap;
    GroupTypingListener groupTypingListener;
    ChildEventListener childEventListener;
    private String groupId;

    public GroupTyping(List<User> users, final String groupId, final GroupTypingListener groupTypingListener) {
        this.users = users;
        this.groupId = groupId;
        this.groupTypingListener = groupTypingListener;


        typingHashmap = new HashMap<>();

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                onChange(dataSnapshot, groupTypingListener, groupId);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                onChange(dataSnapshot, groupTypingListener, groupId);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        FireConstants.groupTypingStat.child(groupId).addChildEventListener(childEventListener);


    }

    private void onChange(DataSnapshot dataSnapshot, GroupTypingListener groupTypingListener, String groupId) {
        if (dataSnapshot.getValue() == null)
            return;

        if (dataSnapshot.getKey().equals(FireManager.getUid()))
            return;


        int stat = dataSnapshot.getValue(Integer.class);
        String uid = dataSnapshot.getKey();

        if (uid.equals(FireManager.getUid()))
            return;

        //if user stops typing,remove him from map
        // then check if there is another user is typing ,if so notify the callback
        //and if there is no other user is typing notify callback that there are no users  typing

        //and if a user is typing ,add him to map and notify callback

        if (stat == TypingStat.NOT_TYPING) {
            typingHashmap.remove(uid);
            if (typingHashmap.isEmpty()) {
                groupTypingListener.onAllNotTyping(groupId);
            } else {
                User lastUserTyping = getLastUserTyping();
                if (lastUserTyping!=null) {
                    //get last user typing state
                    TypingState typingState = typingHashmap.get(lastUserTyping.getUid());
                    if (typingState!=null) {
                        int mState = typingState.state;
                        //set last user typing state
                        groupTypingListener.onTyping(mState, groupId, lastUserTyping);
                    }
                }
            }

        } else {
            typingHashmap.put(uid, new TypingState(stat, getIndex()));
            groupTypingListener.onTyping(stat, groupId, getLastUserTyping());
        }
    }

    private User getLastUserTyping() {
        int index = 0;
        User user = null;
        for (String s : typingHashmap.keySet()) {
            TypingState typingState = typingHashmap.get(s);
            if (typingState.index > index || typingHashmap.size() == 1) {
                index = typingState.index;
                int posFromIdUser = ListUtil.getPosFromIdUser(s, users);
                if (posFromIdUser != -1) {
                    user = users.get(posFromIdUser);
                }
            }
        }
        return user;
    }

    private User getLastUserTypingState() {
        int index = 0;
        User user = null;
        for (String s : typingHashmap.keySet()) {
            TypingState typingState = typingHashmap.get(s);
            if (typingState.index > index || typingHashmap.size() == 1) {
                index = typingState.index;
                int posFromIdUser = ListUtil.getPosFromIdUser(s, users);
                if (posFromIdUser != -1) {
                    user = users.get(posFromIdUser);
                }
            }
        }
        return user;
    }

    public interface GroupTypingListener {
        void onTyping(int state, String groupId, User user);

        void onAllNotTyping(String groupId);
    }

    public void cleanUp() {
        FireConstants.groupTypingStat.child(groupId).removeEventListener(childEventListener);
    }

    private class TypingState {
        public int state;
        public int index;

        public TypingState(int state, int index) {
            this.state = state;
            this.index = index;
        }
    }


    private int getIndex() {
        int index = 0;

        if (typingHashmap.isEmpty())
            return index;


        for (String s : typingHashmap.keySet()) {
            TypingState typingState = typingHashmap.get(s);
            if (typingState.index > index) {
                index = typingState.index;
            }
        }

        return index + 1;
    }

}
