package com.bytesbee.firebase.chat.activities.fragments;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_GROUPS_IN_BOTH;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUPS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUP_MEMBERS_S;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bytesbee.firebase.chat.activities.R;
import com.bytesbee.firebase.chat.activities.adapters.GroupsAdapters;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.models.Groups;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GroupsFragment extends BaseFragment {

    private ArrayList<String> groupList;
    private RelativeLayout imgNoMessage;
    private GroupsAdapters groupsAdapters;
    private ArrayList<Groups> mGroups;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        imgNoMessage = view.findViewById(R.id.imgNoMessage);
        imgNoMessage.setVisibility(View.GONE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        groupList = new ArrayList<>();

        assert firebaseUser != null;
        Query query = FirebaseDatabase.getInstance().getReference(REF_GROUP_MEMBERS_S + firebaseUser.getUid() + EXTRA_GROUPS_IN_BOTH);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupList.clear();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String strGroupId = snapshot.getValue(String.class);
                        groupList.add(strGroupId);
                    }
                }

                if (groupList.size() > 0) {
                    imgNoMessage.setVisibility(View.GONE);
                    readGroups();
                    mRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    imgNoMessage.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void readGroups() {
        mGroups = new ArrayList<>();

        Query reference = FirebaseDatabase.getInstance().getReference(REF_GROUPS);
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mGroups.clear();
                if (dataSnapshot.hasChildren()) {
                    Map<String, Groups> uList = new HashMap<>();
                    try {

                        for (String id : groupList) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Groups groups = snapshot.getValue(Groups.class);
                                assert groups != null;
                                if (!Utils.isEmpty(groups.getId())) {
                                    if (groups.getId().equalsIgnoreCase(id) && groups.isActive()) {
                                        uList.put(groups.getId(), groups);
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Utils.getErrors(e);
                    }

                    if (uList.size() > 0) {
                        uList = Utils.sortByGroupDateTime(uList, false);

                        mGroups.addAll(uList.values());
                    }
                    groupsAdapters = new GroupsAdapters(getContext(), mGroups);
                    mRecyclerView.setAdapter(groupsAdapters);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

}