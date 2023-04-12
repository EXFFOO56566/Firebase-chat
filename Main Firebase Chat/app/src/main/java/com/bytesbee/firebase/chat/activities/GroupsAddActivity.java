package com.bytesbee.firebase.chat.activities;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_GROUPS_IN_BOTH;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_GROUP_ID;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_GROUP_IMG;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_OBJ_GROUP;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.FALSE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.IMG_DEFAULTS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUPS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUPS_S;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUP_MEMBERS_S;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUP_UPLOAD;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.THREE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TRUE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_TEXT;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.ZERO;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytesbee.firebase.chat.activities.adapters.GroupsUserAdapters;
import com.bytesbee.firebase.chat.activities.constants.IGroupListener;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.models.Groups;
import com.bytesbee.firebase.chat.activities.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GroupsAddActivity extends BaseActivity implements IGroupListener, View.OnClickListener {

    private RecyclerView mRecyclerView;
    private RelativeLayout imgNoUsers;
    private ArrayList<User> mUsers;
    private ArrayList<User> mSelectedUsers;
    private List<String> mSelectedMembersId;
    private Set<String> mDeletedMembersId;
    private EditText txtGroupName;
    private boolean isEditGroup = false;
    private Groups groups;
    private String groupId;
    private String groupImg = "";
    private String lastMsg = "";
    private String msgType = "";

    private Uri imageUri = null;
    private StorageReference storageReference;
    private ImageView imgAvatar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        final FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference(REF_GROUP_UPLOAD);

        imgAvatar = findViewById(R.id.imgAvatar);
        imgNoUsers = findViewById(R.id.imgNoUsers);
        imgNoUsers.setVisibility(View.GONE);

        txtGroupName = findViewById(R.id.txtGroupName);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        final Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        Intent myIntent = getIntent();
        if (myIntent.getStringExtra(EXTRA_GROUP_ID) != null) {
            isEditGroup = TRUE;
            groups = (Groups) myIntent.getSerializableExtra(EXTRA_OBJ_GROUP);
            groupId = groups.getId();
            String groupName = groups.getGroupName();
            groupImg = groups.getGroupImg();
            msgType = groups.getType();
            lastMsg = groups.getLastMsg();
            txtGroupName.setText(groupName);
            Utils.setGroupImage(mActivity, groups.getGroupImg(), imgAvatar);
            getSupportActionBar().setTitle(R.string.strEditGroup);
        } else {
            getSupportActionBar().setTitle(R.string.strCreateNewGroup);
            isEditGroup = FALSE;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            screens.showClearTopScreen(LoginActivity.class);
            finish();
        }

        mUsers = new ArrayList<>();
        mSelectedUsers = new ArrayList<>();
        mSelectedMembersId = new ArrayList<>();
        mDeletedMembersId = new HashSet<>();
        mSelectedMembersId.add(firebaseUser.getUid());

        readUsers();

        imgAvatar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imgAvatar) {
            openImageCropper();
        }
    }

    private void readUsers() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Query query = Utils.getQuerySortBySearch();

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);

                        assert firebaseUser != null;
                        assert user != null;

                        if (!user.getId().equalsIgnoreCase(firebaseUser.getUid()) && user.isActive()) {
                            mUsers.add(user);
                        }

                    }

                    showUsers();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void showUsers() {
        if (mUsers.size() > 0) {
            imgNoUsers.setVisibility(View.GONE);
            final GroupsUserAdapters groupUserAdapters = new GroupsUserAdapters(mActivity, mUsers, mSelectedUsers, mSelectedMembersId, mDeletedMembersId, isEditGroup, groups, this);
            mRecyclerView.setAdapter(groupUserAdapters);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            imgNoUsers.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setSubTitle() {
        try {
            final int selectedCount = mSelectedUsers.size();
            getSupportActionBar().setSubtitle(getString(R.string.strSelected) + " " + selectedCount);
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.itemGroupSave) {
            final String strGroupName = txtGroupName.getText().toString().trim();
            if (Utils.isEmpty(strGroupName)) {
                screens.showToast(R.string.msgEnterGroupName);
                return true;
            }
            if (mSelectedMembersId.size() < THREE) {
                screens.showToast(R.string.msgGroupMoreThanOne);
                return true;
            }

            if (isEditGroup) {
                groupId = groups.getId();
            } else {
                groupId = Utils.getGroupUniqueId();
            }

            showProgress();
            groups = new Groups();

            final String currentDate = Utils.getDateTime();
            groups.setId(groupId);
            groups.setGroupName(strGroupName);
            groups.setAdmin(firebaseUser.getUid());
            groups.setMembers(mSelectedMembersId);
            groups.setGroupImg(Utils.isEmpty(groupImg) ? IMG_DEFAULTS : groupImg);
            groups.setLastMsgTime(currentDate);
            groups.setCreatedAt(currentDate);
            groups.setLastMsg(Utils.isEmpty(lastMsg) ? "" : lastMsg);
            groups.setType(Utils.isEmpty(msgType) ? TYPE_TEXT : msgType);
            groups.setActive(TRUE);
            FirebaseDatabase.getInstance().getReference().child(REF_GROUPS_S + groupId).setValue(groups).addOnCompleteListener(task -> addedGroupInMembers(groupId, ZERO)).addOnFailureListener(e -> hideProgress());

            return true;
        } else if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addedGroupInMembers(final String groupId, final int index) {
        if (index == mSelectedMembersId.size()) {
            if (isEditGroup) {
                deleteMembersFromGroups(groupId, ZERO);
            } else {
                groupAddedAndFinishScreen();
            }
        } else {
            FirebaseDatabase.getInstance().getReference()
                    .child(REF_GROUP_MEMBERS_S + mSelectedMembersId.toArray()[index] + EXTRA_GROUPS_IN_BOTH + groupId)
                    .setValue(groupId)
                    .addOnCompleteListener(task -> addedGroupInMembers(groupId, index + 1));
        }
    }

    private void deleteMembersFromGroups(final String groupId, final int userIndex) {
        if (userIndex == mDeletedMembersId.size()) {
            Intent data = new Intent();
            data.putExtra(EXTRA_OBJ_GROUP, groups);
            setResult(RESULT_OK, data);
            groupAddedAndFinishScreen();
        } else {
            FirebaseDatabase.getInstance().getReference()
                    .child(REF_GROUP_MEMBERS_S + mDeletedMembersId.toArray()[userIndex] + EXTRA_GROUPS_IN_BOTH + groupId).removeValue()
                    .addOnCompleteListener(task -> deleteMembersFromGroups(groupId, userIndex + 1));
        }
    }

    private void groupAddedAndFinishScreen() {
        hideProgress();
        if (Utils.isEmpty(imageUri)) {
            finish();
        } else {
            uploadImage();
        }
    }

    private void openImageCropper() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setFixAspectRatio(true)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this);
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(mActivity);
        pd.setMessage(getString(R.string.msg_image_upload));
        pd.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + Utils.getExtension(mActivity, imageUri));
            StorageTask uploadTask = fileReference.putFile(imageUri);

            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return fileReference.getDownloadUrl();
                    })
                    .addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            String mUrl = downloadUri.toString();
                            if (!Utils.isEmpty(groupId)) {
                                reference = FirebaseDatabase.getInstance().getReference(REF_GROUPS).child(groupId);
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put(EXTRA_GROUP_IMG, mUrl);
                                reference.updateChildren(hashMap);
                                groups.setGroupImg(mUrl);

                                Intent data = new Intent();
                                data.putExtra(EXTRA_OBJ_GROUP, groups);
                                setResult(RESULT_OK, data);
                            }
                            finish();
                        } else {
                            screens.showToast(R.string.msgFailedToUpload);
                        }
                        pd.dismiss();
                    }).addOnFailureListener(e -> {
                        Utils.getErrors(e);
                        screens.showToast(e.getMessage());
                        pd.dismiss();
                    });
        } else {
            screens.showToast(R.string.msgNoImageSelected);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
            }
        }

        if (!Utils.isEmpty(imageUri)) {
            imgAvatar.setImageURI(imageUri);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}

