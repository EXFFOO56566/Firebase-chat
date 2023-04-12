package com.bytesbee.firebase.chat.activities;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_GROUPS_IN_BOTH;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_GROUP_ID;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_OBJ_GROUP;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.FALSE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUPS_MESSAGES;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUPS_S;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUP_MEMBERS_S;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.SLASH;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TRUE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TWO;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytesbee.firebase.chat.activities.adapters.GroupsParticipantsAdapters;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.models.Chat;
import com.bytesbee.firebase.chat.activities.models.Groups;
import com.bytesbee.firebase.chat.activities.models.User;
import com.bytesbee.firebase.chat.activities.views.SingleClickListener;
import com.bytesbee.firebase.chat.activities.views.profileview.HeaderView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class GroupsParticipantsActivity extends BaseActivity implements AppBarLayout.OnOffsetChangedListener {

    private ArrayList<User> mUsers;
    private GroupsParticipantsAdapters userAdapters;
    private RecyclerView mRecyclerView;
    private User mineUser = new User();
    private String groupId;
    private Groups groups;
    private String groupName = "";
    private TextView lblParticipants;
    private ImageView imgGroupBackground;

    private HeaderView toolbarHeaderView;
    private HeaderView floatHeaderView;
    private boolean isHideToolbarView = false;
    private FirebaseStorage storage;
    private int setting;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participants);
        toolbarHeaderView = findViewById(R.id.toolbar_header_view);
        floatHeaderView = findViewById(R.id.float_header_view);
        final AppBarLayout appBarLayout = findViewById(R.id.appbar);
        final Toolbar mToolbar = findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbar.setNavigationOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                onBackPressed();
            }
        });

        lblParticipants = findViewById(R.id.lblParticipants);
        final TextView txtExitGroup = findViewById(R.id.txtExitGroup);

        final Intent intent = getIntent();
        groups = (Groups) intent.getSerializableExtra(EXTRA_OBJ_GROUP);
        groupName = groups.getGroupName();
        groupId = groups.getId();

        storage = FirebaseStorage.getInstance();
        //final StorageReference storageReference = storage.getReference(REF_GROUP_PHOTO_UPLOAD + SLASH + groupId);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        imgGroupBackground = findViewById(R.id.imgGroupBackground);
        Utils.setGroupParticipateImage(mActivity, groups.getGroupImg(), imgGroupBackground);
        imgGroupBackground.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                //final Screens screens = new Screens(mActivity);
                screens.openFullImageViewActivity(v, groups.getGroupImg(), groupName, "");
            }
        });

        final RelativeLayout imgNoMessage = findViewById(R.id.imgNoMessage);
        imgNoMessage.setVisibility(View.GONE);
        mRecyclerView = findViewById(R.id.recyclerView);
        LinearLayout layoutGroupAdminSetting = findViewById(R.id.layoutGroupAdminSetting);
        final TextView lblSettingOption = findViewById(R.id.lblSettingOption);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), layoutManager.getOrientation());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        appBarLayout.addOnOffsetChangedListener(this);

        readGroupTitle();

        //Admin Message
        CardView cardView = findViewById(R.id.cardViewSendMessage);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (groups.getAdmin().equalsIgnoreCase(firebaseUser.getUid())) {
            cardView.setVisibility(View.VISIBLE);
        } else {
            cardView.setVisibility(View.GONE);
        }

        setting = groups.getSendMessageSetting();

        lblSettingOption.setText(Utils.getSettingString(mActivity, setting));

        layoutGroupAdminSetting.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                Utils.selectSendMessages(mActivity, groupId, setting, value -> {
                    lblSettingOption.setText(value);
                    setting = Utils.getSettingValue(mActivity, value);
                    groups.setSendMessageSetting(setting);
                });
            }
        });

        txtExitGroup.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {

                Utils.showYesNoDialog(mActivity, R.string.strLeave, R.string.strLeaveFromGroup, () -> {
                    showProgress();
                    if (isAdmin()) {

                        if (groups.getMembers().size() >= TWO) {//Make other Person to Admin for this group cause more than 2 person available.

                            String newAdminId = groups.getMembers().get(1);//Default set from 1st position to Make as Admin.
                            for (int i = 0; i < groups.getMembers().size(); i++) {
                                if (!groups.getMembers().get(i).equalsIgnoreCase(firebaseUser.getUid())) {
                                    newAdminId = groups.getMembers().get(i);//Assign Admin Role to next USER.
                                    break;
                                }
                            }

                            groups.setAdmin(newAdminId);

                            groups.getMembers().remove(firebaseUser.getUid());

                            leaveFromGroup(TRUE);//True means close current screen, cause first we leave from group and than delete own chats

                        } else {//You are alone in this Groups. So Delete group and its DATA.

                            deleteWholeGroupsData(); // In this case only groups have Single User and can delete whole groups data.

                        }

                    } else {

                        List<String> removeId = groups.getMembers();
                        removeId.remove(firebaseUser.getUid());
                        groups.setMembers(removeId);

                        leaveFromGroup(TRUE);//True means close current screen, cause first we leave from group and than delete own chats

                    }
                });

            }
        });
    }

    private void leaveFromGroup(final boolean isFinishActivity) {
        //Remove from Main Group info /groupId/members/<removeId>
        FirebaseDatabase.getInstance().getReference().child(REF_GROUPS_S + groupId).setValue(groups).addOnCompleteListener(task -> {

            //Remove from MembersGroup/groupsIn/<groupId>
            FirebaseDatabase.getInstance().getReference().child(REF_GROUP_MEMBERS_S + firebaseUser.getUid() + EXTRA_GROUPS_IN_BOTH + groupId).removeValue()
                    .addOnCompleteListener(task1 -> {
                        deleteOwnChats(isFinishActivity);//True means close current screen, cause first we leave from group and than delete own chats
                    });
        }).addOnFailureListener(e -> hideProgress());
    }

    /**
     * False means don't close current screen, just delete my own chats
     * True  means close current screen, cause first we leave from group and than delete own chats
     */
    private void deleteOwnChats(final boolean isFinishActivity) {
        FirebaseDatabase.getInstance().getReference().child(REF_GROUPS_MESSAGES + SLASH + groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Chat chat = snapshot.getValue(Chat.class);
                            assert chat != null;
                            if (chat.getSender().equalsIgnoreCase(firebaseUser.getUid())) {
                                Utils.deleteUploadedFilesFromCloud(storage, chat);
                                snapshot.getRef().removeValue();
                            }
                        }
                    }

                } catch (Exception ignored) {
                }
                hideProgress();
                if (isFinishActivity) {
                    goBack();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void goBack() {
        Intent data = new Intent();
        data.putExtra(EXTRA_OBJ_GROUP, groups);
        setResult(RESULT_FIRST_USER, data);
        finish();
    }

    private void deleteWholeGroupsData() {
        final int members = groups.getMembers().size();
        FirebaseDatabase.getInstance().getReference().child(REF_GROUPS_S + groupId).removeValue().addOnCompleteListener(task -> {
            for (int i = 0; i < members; i++) {
                FirebaseDatabase.getInstance().getReference().child(REF_GROUP_MEMBERS_S + groups.getMembers().get(i) + EXTRA_GROUPS_IN_BOTH + groupId).removeValue().addOnCompleteListener(task12 -> {

                });
                if (i == (members - 1)) {
                    hideProgress();
                    goBack();
                }
            }
            FirebaseDatabase.getInstance().getReference().child(REF_GROUPS_MESSAGES + SLASH + groupId).removeValue().addOnCompleteListener(task1 -> {

            });
        });
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        if (percentage == 1f && isHideToolbarView) {
            toolbarHeaderView.setVisibility(View.VISIBLE);
            isHideToolbarView = !isHideToolbarView;

        } else if (percentage < 1f && !isHideToolbarView) {
            toolbarHeaderView.setVisibility(View.GONE);
            isHideToolbarView = !isHideToolbarView;
        }
    }

    private void readUsers() {
        mUsers = new ArrayList<>();
        mineUser = new User();

        Query reference = Utils.getQuerySortBySearch();
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                if (dataSnapshot.hasChildren()) {
                    try {

                        for (String id : groups.getMembers()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);
                                assert user != null;
                                if (user.getId().equalsIgnoreCase(id) && user.isActive()) {

                                    if (groups.getAdmin().equalsIgnoreCase(user.getId())) {
                                        user.setAdmin(TRUE);
                                    } else {
                                        user.setAdmin(FALSE);
                                    }
                                    if (!user.getId().equalsIgnoreCase(firebaseUser.getUid())) {
                                        mUsers.add(user);
                                    } else {
                                        user.setUsername(getString(R.string.strYou));
                                        mineUser = user;
                                    }
                                    break;
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }

                    if (mUsers.size() > 0) {
                        mUsers = Utils.sortByUser(mUsers);
//                        if (mineUser.isAdmin()) {
//                            mUsers.add(0, mineUser);
//                        } else {
                        mUsers.add(mineUser);
//                        }
                    }

                    userAdapters = new GroupsParticipantsAdapters(mActivity, mUsers);
                    mRecyclerView.setAdapter(userAdapters);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_add, menu);
        MenuItem item = menu.findItem(R.id.itemGroupSave);
        item.setIcon(R.drawable.ic_group_add);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.itemGroupSave) {
            if (isAdmin()) {
                final Intent intent = new Intent(mActivity, GroupsAddActivity.class);
                intent.putExtra(EXTRA_GROUP_ID, groupId);
                intent.putExtra(EXTRA_OBJ_GROUP, groups);
                intentLauncher.launch(intent);
            } else {
                screens.showToast(R.string.msgOnlyAdminEdit);
            }
        }
        return true;
    }

    private void readGroupTitle() {
        final String lastSeen = String.format(getString(R.string.strCreatedOn), Utils.formatDateTime(mActivity, groups.getLastMsgTime()));
        groupName = groups.getGroupName();

        toolbarHeaderView.bindTo(groupName, lastSeen);
        floatHeaderView.bindTo(groupName, lastSeen);
        lblParticipants.setText(String.format(getString(R.string.strParticipants), groups.getMembers().size()));

        Utils.setGroupParticipateImage(mActivity, groups.getGroupImg(), imgGroupBackground);

        readUsers();
    }

    /*
     * Intent launcher to get Image Uri from storage
     * */
    final ActivityResultLauncher<Intent> intentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                final Intent data = result.getData();
                groups = (Groups) data.getSerializableExtra(EXTRA_OBJ_GROUP);
                readGroupTitle();
            }
        }
    });

    private boolean isAdmin() {
        if (groups.getAdmin().equalsIgnoreCase(firebaseUser.getUid())) {
            return TRUE;
        }
        return FALSE;
    }
}
