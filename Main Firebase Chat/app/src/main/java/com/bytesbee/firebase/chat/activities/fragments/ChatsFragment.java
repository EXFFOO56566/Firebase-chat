package com.bytesbee.firebase.chat.activities.fragments;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.GEN_FEMALE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.GEN_MALE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.GEN_UNSPECIFIED;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.IMG_DEFAULTS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_CHATS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.SLASH;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.STATUS_OFFLINE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.STATUS_ONLINE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TRUE;
import static com.bytesbee.firebase.chat.activities.managers.Utils.female;
import static com.bytesbee.firebase.chat.activities.managers.Utils.male;
import static com.bytesbee.firebase.chat.activities.managers.Utils.notset;
import static com.bytesbee.firebase.chat.activities.managers.Utils.offline;
import static com.bytesbee.firebase.chat.activities.managers.Utils.online;
import static com.bytesbee.firebase.chat.activities.managers.Utils.withPicture;
import static com.bytesbee.firebase.chat.activities.managers.Utils.withoutPicture;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bytesbee.firebase.chat.activities.R;
import com.bytesbee.firebase.chat.activities.adapters.UserAdapters;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.models.Chat;
import com.bytesbee.firebase.chat.activities.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ChatsFragment extends BaseFragment {

    private ArrayList<String> userList;
    private RelativeLayout imgNoMessage;
    private String currentId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

//        final FloatingActionButton fabChat = view.findViewById(R.id.fabChat);
        imgNoMessage = view.findViewById(R.id.imgNoMessage);
        imgNoMessage.setVisibility(View.GONE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentId = Objects.requireNonNull(firebaseUser).getUid();

        userList = new ArrayList<>();

        Query query = FirebaseDatabase.getInstance().getReference(REF_CHATS).child(currentId);
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                uList.clear();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        userList.add(snapshot.getKey());
                    }
                }

                if (userList.size() > 0) {
                    sortChats();
                } else {
                    imgNoMessage.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(Utils::uploadToken);
        return view;
    }

    Map<String, Chat> uList = new HashMap<>();

    private void sortChats() {
        for (int i = 0; i < userList.size(); i++) {
            final String key = userList.get(i);

            Query query = FirebaseDatabase.getInstance().getReference(REF_CHATS).child(currentId + SLASH + key).limitToLast(1);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Chat chat = snapshot.getValue(Chat.class);
                            uList.put(key, chat);
                        }
                    }

                    if (uList.size() == userList.size()) {

                        if (uList.size() > 0) {
                            uList = Utils.sortByChatDateTime(uList, false);
                        }

                        userList = new ArrayList(uList.keySet());

                        readChats();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

    }

    private void readChats() {
        mUsers = new ArrayList<>();

        Query reference = Utils.getQuerySortBySearch();
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                if (dataSnapshot.hasChildren()) {
                    try {
                        for (String id : userList) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);
                                assert user != null;
                                if (user.getId().equalsIgnoreCase(id) && user.isActive()) {
                                    onlineOptionFilter(user);
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        //Utils.getErrors(e);
                    }
                }

                if (mUsers.size() > 0) {
                    imgNoMessage.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    userAdapters = new UserAdapters(getContext(), mUsers, TRUE);
                    mRecyclerView.setAdapter(userAdapters);
                } else {
                    imgNoMessage.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void onlineOptionFilter(final User user) {
        try {
            if (user.getIsOnline() == STATUS_ONLINE) {
                if (online)
                    profileOptionFilter(user);
            } else if (user.getIsOnline() == STATUS_OFFLINE) {
                if (offline)
                    profileOptionFilter(user);
            } else {
                profileOptionFilter(user);
            }
        } catch (Exception ignored) {
        }
    }


    private void profileOptionFilter(final User user) {
        try {
            if (!user.getImageURL().equalsIgnoreCase(IMG_DEFAULTS)) {
                if (withPicture)
                    levelOptionFilter(user);
            } else if (user.getImageURL().equalsIgnoreCase(IMG_DEFAULTS)) {
                if (withoutPicture)
                    levelOptionFilter(user);
            } else {
                levelOptionFilter(user);
            }
        } catch (Exception ignored) {
        }
    }

    private void levelOptionFilter(final User user) {
        try {
            if (user.getGenders() == GEN_UNSPECIFIED) {
                if (notset)
                    addNewUserDataToList(user);
            } else {
                if (user.getGenders() == GEN_MALE) {
                    if (male)
                        addNewUserDataToList(user);
                } else if (user.getGenders() == GEN_FEMALE) {
                    if (female)
                        addNewUserDataToList(user);
                }
            }
        } catch (Exception e) {
            addNewUserDataToList(user);
        }
    }

    private void addNewUserDataToList(User user) {
        mUsers.add(user);
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_filter, menu);

        MenuItem searchItem = menu.findItem(R.id.itemFilter);

        searchItem.setOnMenuItemClickListener(item -> {
            Utils.filterPopup(getActivity(), this::readChats);
            return true;
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

}
