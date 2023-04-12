package com.devlomi.fireapp.utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Devlomi on 31/10/2017.
 */

//this class will update Presence (online or last seen)
public class PresenceUtil {

    public PresenceUtil() {
        onConnect();
    }

    DatabaseReference connectedRef;
    DatabaseReference presenceRef;
    ValueEventListener connectedListener;

    private void onConnect() {
        presenceRef = FireConstants.presenceRef.child(FireManager.getUid());
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected) {
                    FireManager.setOnlineStatus();
                } else {
                    FireManager.setLastSeen();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        presenceRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

    }

    public void onPause() {
        connectedRef.removeEventListener(connectedListener);
        FireManager.setLastSeen();


    }

    public void onResume() {
        FireManager.setOnlineStatus();
        connectedRef.addValueEventListener(connectedListener);
    }

}
