package com.bytesbee.firebase.chat.activities.fragments;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.FALSE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.GEN_FEMALE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.GEN_MALE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.GEN_UNSPECIFIED;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.IMG_DEFAULTS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.STATUS_OFFLINE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.STATUS_ONLINE;
import static com.bytesbee.firebase.chat.activities.managers.Utils.female;
import static com.bytesbee.firebase.chat.activities.managers.Utils.male;
import static com.bytesbee.firebase.chat.activities.managers.Utils.notset;
import static com.bytesbee.firebase.chat.activities.managers.Utils.offline;
import static com.bytesbee.firebase.chat.activities.managers.Utils.online;
import static com.bytesbee.firebase.chat.activities.managers.Utils.withPicture;
import static com.bytesbee.firebase.chat.activities.managers.Utils.withoutPicture;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bytesbee.firebase.chat.activities.R;
import com.bytesbee.firebase.chat.activities.adapters.UserAdapters;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.models.User;
import com.bytesbee.firebase.chat.activities.views.SingleClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class UsersFragment extends BaseFragment {

    private AppCompatEditText txtSearch;
    private ImageView imgClear;
    private RelativeLayout imgNoUsers;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        imgNoUsers = view.findViewById(R.id.imgNoUsers);
        imgNoUsers.setVisibility(View.GONE);

        imgClear = view.findViewById(R.id.imgClear);
        txtSearch = view.findViewById(R.id.txtSearch);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mUsers = new ArrayList<>();

        readUsers();

        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
                if (count > 0) {
                    imgClear.setVisibility(View.VISIBLE);
                } else {
                    imgClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        imgClear.setVisibility(View.GONE);
        imgClear.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                txtSearch.setText("");
                txtSearch.requestFocus();
            }
        });

        return view;
    }

    private void showUsers() {
        if (mUsers.size() > 0) {
            imgNoUsers.setVisibility(View.GONE);
            userAdapters = new UserAdapters(getContext(), mUsers, FALSE);
            mRecyclerView.setAdapter(userAdapters);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            imgNoUsers.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }


    private void searchUsers(final String search) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Query query = Utils.getQuerySortBySearch();
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        assert user != null;
                        assert firebaseUser != null;
                        if (!user.getId().equalsIgnoreCase(firebaseUser.getUid()) && user.isActive()) {
                            if (user.getSearch().contains(search)) {
                                onlineOptionFilter(user);
                            }
                        }
                    }
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readUsers() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        Query query = FirebaseDatabase.getInstance().getReference(REF_USERS).orderByChild(EXTRA_SEARCH).startAt(search).endAt(search + "\uf8ff");
        final Query query = Utils.getQuerySortBySearch();
        query.keepSynced(true);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                if (dataSnapshot.hasChildren()) {
                    if (txtSearch.getText().toString().trim().equalsIgnoreCase("")) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);

                            assert firebaseUser != null;
                            assert user != null;

                            if (!user.getId().equalsIgnoreCase(firebaseUser.getUid()) && user.isActive()) {
                                onlineOptionFilter(user);
                            }

                        }
                    }
                }
                showUsers();

                try {
                    final String searchHint = String.format(getString(R.string.strSearchWithCount), mUsers.size());
                    txtSearch.setHint(searchHint);
                } catch (Exception ignored) {
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
            Utils.filterPopup(getActivity(), this::readUsers);
            return true;
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

}
