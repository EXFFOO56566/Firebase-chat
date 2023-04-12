package com.bytesbee.firebase.chat.activities.fragments;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_ABOUT;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_ADMIN;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_GROUPS_IN;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_GROUPS_IN_BOTH;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_GROUP_MEMBERS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_IMAGEURL;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_USER_ID;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.GEN_MALE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.GEN_UNSPECIFIED;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_CHATS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUPS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUPS_MESSAGES;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUP_MEMBERS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_OTHERS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_TOKENS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_UPLOAD;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_USERS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.SLASH;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_EMAIL;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_GOOGLE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bytesbee.firebase.chat.activities.LoginActivity;
import com.bytesbee.firebase.chat.activities.R;
import com.bytesbee.firebase.chat.activities.managers.SessionManager;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.models.Chat;
import com.bytesbee.firebase.chat.activities.models.Groups;
import com.bytesbee.firebase.chat.activities.models.User;
import com.bytesbee.firebase.chat.activities.views.SingleClickListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.shobhitpuri.custombuttons.GoogleSignInButton;
import com.skydoves.powermenu.CircularEffect;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends BaseFragment implements View.OnClickListener {

    private CircleImageView imgAvatar;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private StorageReference storageReference;
    private ImageView imgEditAbout;
    private ImageView imgEditGender;
    private Button btnDeleteAccount;
    private Uri imageUri;
    private StorageTask uploadTask;
    private String strDescription = "";
    private int strGender = GEN_UNSPECIFIED;
    private String strReEmail = "", strRePassword = "", strAvatarImg, strUsername = "", strSocialToken = "";
    private String currentId, strSignUpType = TYPE_EMAIL;

    private final List<String> userList = new ArrayList<>();
    private final List<String> groupAdminList = new ArrayList<>();
    private final List<String> groupAdminMemberList = new ArrayList<>();
    private final List<String> groupOthersList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        final View layoutCameraGallery = view.findViewById(R.id.layoutCameraGallery);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        final TextView txtUsername = view.findViewById(R.id.txtUsername);
        final TextView txtEmail = view.findViewById(R.id.txtEmail);
        final TextView txtAbout = view.findViewById(R.id.txtAbout);
        final TextView txtGender = view.findViewById(R.id.txtGender);
        imgEditAbout = view.findViewById(R.id.imgEdit);
        imgEditGender = view.findViewById(R.id.imgEditGender);
        final RelativeLayout layoutAbout = view.findViewById(R.id.layoutAbout);
        final RelativeLayout layoutGender = view.findViewById(R.id.layoutGender);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);

        final FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference(REF_UPLOAD);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        currentId = firebaseUser.getUid();

        final Intent i = getActivity().getIntent();
        final String userId = i.getStringExtra(EXTRA_USER_ID);

        String viewUserId;
        if (Utils.isEmpty(userId)) {//
            viewUserId = firebaseUser.getUid();
            showHideViews(View.VISIBLE);
            layoutCameraGallery.setOnClickListener(this);
            imgAvatar.setOnClickListener(this);
            layoutAbout.setOnClickListener(this);
            layoutGender.setOnClickListener(this);
            btnDeleteAccount.setOnClickListener(this);
        } else {
            viewUserId = userId;
            showHideViews(View.GONE);
        }

        final String lblStatus = mActivity.getString(R.string.strAboutStatus);
        final String lblUnSpecified = getString(R.string.strUnspecified);
        final String lblMale = getString(R.string.strMale);
        final String lblFemale = getString(R.string.strFemale);

        reference = FirebaseDatabase.getInstance().getReference(REF_USERS).child(viewUserId);
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    strUsername = user.getUsername();
                    strSignUpType = user.getSignup_type();
                    try {
                        strSocialToken = Utils.isEmpty(user.getSocial_token()) ? "" : user.getSocial_token();
                    } catch (Exception ignored) {
                    }
                    strAvatarImg = user.getMyImg();
                    txtUsername.setText(strUsername);
                    strReEmail = user.getEmail();
                    strRePassword = user.getPassword();
                    txtEmail.setText(strReEmail);
                    strGender = user.getGenders();
                    strDescription = user.getAbout();
                    txtAbout.setText(Utils.isEmpty(strDescription) ? lblStatus : strDescription);
                    txtGender.setText(strGender == GEN_UNSPECIFIED ? lblUnSpecified : (strGender == GEN_MALE ? lblMale : lblFemale));

                    Utils.setProfileImage(getContext(), strAvatarImg, imgAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void showHideViews(int isShow) {
        imgEditAbout.setVisibility(isShow);
        imgEditGender.setVisibility(isShow);
        btnDeleteAccount.setVisibility(isShow);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.layoutCameraGallery) {
            if (Utils.isTypeEmail(strSignUpType)) {
                openImageCropper();
            } else {
                screens.openFullImageViewActivity(v, strAvatarImg, strUsername);
            }
        } else if (id == R.id.imgAvatar) {
            screens.openFullImageViewActivity(v, strAvatarImg, strUsername);
        } else if (id == R.id.layoutAbout) {
            popupForAbout();
        } else if (id == R.id.layoutGender) {
            Utils.selectGenderPopup(mActivity, firebaseUser.getUid(), strGender);
        } else if (id == R.id.btnDeleteAccount) {
            openAuthenticatePopup();
        }
    }

    private void openAuthenticatePopup() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        CardView view = (CardView) mActivity.getLayoutInflater().inflate(R.layout.dialog_reauthenticate, null);

        if (SessionManager.get().isRTLOn()) {
            view.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            view.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }

        builder.setView(view);

        final LinearLayout layoutEmail = view.findViewById(R.id.layoutEmail);
        final TextView txtEmail = view.findViewById(R.id.txtEmail);
        final TextView txtPassword = view.findViewById(R.id.txtPassword);
        final Button btnSignup = view.findViewById(R.id.btnSignUp);
        final Button btnCancel = view.findViewById(R.id.btnCancel);
        final GoogleSignInButton btnGoogleSignIn = view.findViewById(R.id.btnGoogleSignIn);

        layoutEmail.setVisibility(View.GONE);
        btnGoogleSignIn.setVisibility(View.GONE);

        if (Utils.isTypeEmail(strSignUpType)) {
            layoutEmail.setVisibility(View.VISIBLE);
        } else if (strSignUpType.equalsIgnoreCase(TYPE_GOOGLE)) {
            btnGoogleSignIn.setVisibility(View.VISIBLE);
        }

        final AlertDialog alert = builder.create();

        btnCancel.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                alert.dismiss();
            }
        });

        btnSignup.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                final String strEmail = txtEmail.getText().toString().trim();
                final String strPassword = txtPassword.getText().toString().trim();

                if (TextUtils.isEmpty(strEmail) || TextUtils.isEmpty(strPassword)) {
                    screens.showToast(R.string.strAllFieldsRequired);
                } else if (!strEmail.equalsIgnoreCase(strReEmail) || !strPassword.equalsIgnoreCase(strRePassword)) {
                    screens.showToast(R.string.strInvalidEmailPassword);
                } else {
                    alert.dismiss();
                    deleteChatsAndOtherData();
                }
            }
        });

        btnGoogleSignIn.setOnClickListener(v -> {
            final String strEmail = txtEmail.getText().toString().trim();
            if (TextUtils.isEmpty(strEmail)) {
                screens.showToast(mActivity.getString(R.string.strEmailFieldsRequired));
            } else if (!strEmail.equalsIgnoreCase(strReEmail)) {
                screens.showToast(mActivity.getString(R.string.strInvalidEmail));
            } else {
                alert.dismiss();
                deleteChatsAndOtherData();
            }
        });

        alert.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        alert.setCanceledOnTouchOutside(false);
        alert.setCancelable(!Utils.isTypeEmail(strSignUpType));
        alert.show();
    }

    private void deleteInsideUsersChat() {
        for (int i = 0; i < userList.size(); i++) {
            final String key = userList.get(i);
            Query query = FirebaseDatabase.getInstance().getReference(REF_CHATS).child(key);
            query.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.getKey().equalsIgnoreCase(currentId)) {
                                    snapshot.getRef().removeValue();
                                    break;
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void deleteChatsAndOtherData() {
        showProgress();
        userList.clear();
        groupAdminList.clear();
        groupAdminMemberList.clear();
        groupOthersList.clear();

        final Query chats = FirebaseDatabase.getInstance().getReference(REF_CHATS).child(currentId);
        chats.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            userList.add(snapshot.getKey());

                            snapshot.getRef().removeValue();

                        }

                        deleteInsideUsersChat();
                    }
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        deleteGroupData();

    }

    //***************************************************************************************************************************
    //************************************************** GROUPS ADMIN DELETE - START **************************************************
    //***************************************************************************************************************************
    private void deleteGroupData() {

        //Delete first our groups, where we are admin
        final Query groupsAdmin = FirebaseDatabase.getInstance().getReference(REF_GROUPS).orderByChild(EXTRA_ADMIN).equalTo(currentId);
        groupsAdmin.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        try {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Groups grp = snapshot.getValue(Groups.class);

                                assert grp != null;
                                groupAdminList.add(grp.getId());

                                groupAdminMemberList.addAll(grp.getMembers());

                                snapshot.getRef().removeValue();// Delete our admin group
                            }
                        } catch (Exception ignored) {
                        }

                        //Remove group messages where we are Group Admin, Whole Group can be deleted
                        for (int i = 0; i < groupAdminList.size(); i++) {
                            String keyGroupId = null;
                            try {
                                keyGroupId = groupAdminList.get(i);

                                final Query groupsAdminMessages = FirebaseDatabase.getInstance().getReference(REF_GROUPS_MESSAGES).child(keyGroupId);

                                groupsAdminMessages.getRef().removeValue();// Delete our created Group All Messages

                            } catch (Exception ignored) {
                            }

                            try {
                                for (int j = 0; j < groupAdminMemberList.size(); j++) {
                                    String keyMem = groupAdminMemberList.get(j);

                                    final Query groupsAdminGroupsIn = FirebaseDatabase.getInstance().getReference(REF_GROUP_MEMBERS).child(keyMem + EXTRA_GROUPS_IN_BOTH + keyGroupId);

                                    groupsAdminGroupsIn.getRef().removeValue();//Remove Group Id from Other member's groupIn, So they are not part of our group because we're deleted

                                }
                            } catch (Exception ignored) {
                            }
                        }
                        deleteOtherGroupData();
                    } else {
                        deleteOtherGroupData();
                    }

                } catch (Exception ignored) {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

//        Utils.updateUserActive(firebaseUser.getUid());
    }

    /**
     * ===================================================================================================
     * =========================== START OTHER GROUPS DATA AND DELETE IF FOUND ===========================
     * ===================================================================================================
     */
    private void deleteOtherGroupData() {

        //Delete myself from Other groups where added by thier other users.
        final Query groupsAdminGroupsIn = FirebaseDatabase.getInstance().getReference(REF_GROUP_MEMBERS).child(currentId + SLASH + EXTRA_GROUPS_IN);
        groupsAdminGroupsIn.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            groupOthersList.add(snapshot.getKey());
                            snapshot.getRef().removeValue();//Remove from other Group created by other user.
                        }
                    }
                } catch (Exception ignored) {
                }

                try {
                    //Delete other groups from where I did chat with other guys, So I only deleted my message from that groups.
                    for (int i = 0; i < groupOthersList.size(); i++) {
                        String grpOtherMsg = groupOthersList.get(i);

                        final Query groupsOtherMsg = FirebaseDatabase.getInstance().getReference(REF_GROUPS_MESSAGES).child(grpOtherMsg);

                        groupsOtherMsg.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                try {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                            Chat chat = snapshot.getValue(Chat.class);

                                            assert chat != null;
                                            if (chat.getSender().equalsIgnoreCase(currentId)) {

                                                snapshot.getRef().removeValue();//Delete mine message only from Other groups

                                            }
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                        //Delete from Other Groups info where I added inside the 'members' attribute. So delete myself from their.
                        final Query groupsOther = FirebaseDatabase.getInstance().getReference(REF_GROUPS).child(grpOtherMsg);
                        groupsOther.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                try {
                                    if (dataSnapshot.exists()) {
                                        Groups groups = dataSnapshot.getValue(Groups.class);
                                        assert groups != null;
                                        List list = groups.getMembers();

                                        list.remove(currentId);//Delete our Id to remove from that group and update it list

                                        HashMap<String, Object> hashMap = new HashMap<>();

                                        hashMap.put(EXTRA_GROUP_MEMBERS, list);

                                        dataSnapshot.getRef().updateChildren(hashMap);// Delete/Update myself from members in other groups.
                                    }
                                } catch (Exception ignored) {
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                    }
                } catch (Exception ignored) {
                }

                deleteTokenOtherData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //***************************************************************************************************************************
    //*************************************************** TOKENS DELETE - END ***************************************************
    //***************************************************************************************************************************
    private void deleteTokenOtherData() {

        final Query tokens = FirebaseDatabase.getInstance().getReference(REF_TOKENS).orderByKey().equalTo(firebaseUser.getUid());
        tokens.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            snapshot.getRef().removeValue();
                        }
                    }
                } catch (Exception ignored) {
                }

                final Query others = FirebaseDatabase.getInstance().getReference(REF_OTHERS).orderByKey().equalTo(firebaseUser.getUid());
                others.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                    snapshot.getRef().removeValue();
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        if (Utils.isTypeEmail(strSignUpType)) {
                            deActivateAccount();
                        } else {
                            deActivateAccount(strSignUpType);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void deActivateAccount() {
        //Getting the user instance.
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            //You need to get here the token you saved at logging-in time.
            String token = null;

            AuthCredential credential;

            //This means you didn't have the token because user used like Facebook Sign-in method.
            if (token == null) {
                credential = EmailAuthProvider.getCredential(strReEmail, strRePassword);
            } else {
                //Doesn't matter if it was Facebook Sign-in or others. It will always work using GoogleAuthProvider for whatever the provider.
                //credential = GoogleAuthProvider.getCredential(token, null);
                return;
            }


            //We have to reauthenticate user because we don't know how long
            //it was the sign-in. Calling reauthenticate, will update the
            //user login and prevent FirebaseException (CREDENTIAL_TOO_OLD_LOGIN_AGAIN) on user.delete()
            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        //Calling delete to remove the user and wait for a result.

                        user.delete().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                //Ok, user remove
                                try {
                                    hideProgress();
                                    final Query reference3 = FirebaseDatabase.getInstance().getReference(REF_USERS).orderByKey().equalTo(user.getUid());
                                    reference3.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    snapshot.getRef().removeValue();
                                                    screens.showClearTopScreen(LoginActivity.class);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                } catch (Exception e) {
                                    Utils.getErrors(e);
                                }
                            } else {
                                //Handle the exception
                                Utils.getErrors(task1.getException());
                            }
                        });
                    });
        }
    }

    public void deActivateAccount(final String strSignUpType) {
        //Getting the user instance.
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            try {
                hideProgress();
                AuthCredential credential;
                if (strSignUpType.equalsIgnoreCase(TYPE_GOOGLE)) {
                    credential = GoogleAuthProvider.getCredential(strSocialToken, null);
                } else {
                    user.delete();
                    openLoginScreen(user, strSignUpType);
                    return;
                }
                user.reauthenticate(credential)
                        .addOnCompleteListener(task -> {
                            //Calling delete to remove the user and wait for a result.
                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //Ok, user remove
                                        openLoginScreen(user, strSignUpType);
                                    } else {
                                        //Handle the exception
                                        openLoginScreen(user, strSignUpType);
                                        Utils.getErrors(task.getException());
                                    }
                                }
                            });
                        });
            } catch (Exception e) {
                openLoginScreen(user, strSignUpType);
                Utils.getErrors(e);
            }
        } else {
            //Handle the exception
        }
    }

    private void openLoginScreen(FirebaseUser user, final String strSignUpType) {
        try {
            final Query reference3 = FirebaseDatabase.getInstance().getReference(REF_USERS).orderByKey().equalTo(user.getUid());
            reference3.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        FirebaseAuth.getInstance().signOut();
                        if (strSignUpType.equalsIgnoreCase(TYPE_GOOGLE)) {
                            revokeGoogle(mContext);
                        }
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            snapshot.getRef().removeValue();
                            screens.showClearTopScreen(LoginActivity.class);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

    private static void revokeGoogle(Context context) {
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
            mGoogleSignInClient.revokeAccess();
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

    public void popupForAbout() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getText(R.string.strEnterAbout));

        CardView view = (CardView) getLayoutInflater().inflate(R.layout.dialog_description, null);

        if (SessionManager.get().isRTLOn()) {
            view.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            view.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }

        final AppCompatButton btnCancel = view.findViewById(R.id.btnCancel);
        final AppCompatButton btnDone = view.findViewById(R.id.btnDone);

        builder.setView(view);

        final EditText txtAbout = view.findViewById(R.id.txtAbout);
        txtAbout.setText(strDescription);

        final AlertDialog alert = builder.create();

        btnCancel.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                alert.dismiss();
            }
        });

        btnDone.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                try {
                    final String strAbout = txtAbout.getText().toString().trim();

                    if (Utils.isEmpty(strAbout)) {
                        screens.showToast(R.string.msgErrorEnterDesc);
                        return;
                    }

                    try {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(REF_USERS).child(firebaseUser.getUid());
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put(EXTRA_ABOUT, strAbout);
                        reference.updateChildren(hashMap);
                    } catch (Exception e) {
                        Utils.getErrors(e);
                    }

                } catch (Exception e) {
                    Utils.getErrors(e);
                }
                alert.dismiss();
            }
        });

        alert.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        alert.setCanceledOnTouchOutside(false);
        alert.setCancelable(false);
        alert.show();
    }

    private File fileUri = null;
    private Uri imgUri;

    private void openImageCropper() {
        try {
            fileUri = null;
            imgUri = null;

            List<PowerMenuItem> list = new ArrayList<>();
            list.add(new PowerMenuItem(getString(R.string.strGallery), R.drawable.ic_popup_gallery));
            list.add(new PowerMenuItem(getString(R.string.strCamera), R.drawable.ic_popup_camera));
            PowerMenu powerMenu = new PowerMenu.Builder(mActivity)
                    .addItemList(list)
                    .setAnimation(MenuAnimation.ELASTIC_CENTER)
                    .setCircularEffect(CircularEffect.BODY)
                    .setTextGravity(Gravity.NO_GRAVITY)
                    .setMenuRadius(10f) // sets the corner radius.
                    .setMenuShadow(10f) // sets the shadow.
                    .setTextTypeface(Utils.getRegularFont(mActivity))
                    .setTextSize(15)
                    .setSelectedTextColor(Color.WHITE)
                    .setMenuColor(Color.WHITE)
                    .setSelectedEffect(true)
                    .setTextColor(ContextCompat.getColor(mActivity, R.color.grey_800))
                    .setSelectedMenuColor(ContextCompat.getColor(mActivity, R.color.colorAccent))
                    .setDismissIfShowAgain(true)
                    .setAutoDismiss(true)
                    .setOnMenuItemClickListener((position, item) -> {
                        if (item.getTitle().toString().equalsIgnoreCase(getString(R.string.strGallery))) {
                            openImage();
                        } else {
                            openCamera();
                        }
                    })
                    .build();

            powerMenu.showAsAnchorCenter(getView());

        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

    private void openCamera() {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            fileUri = Utils.createImageFile(mActivity);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Utils.getUriForFileProvider(mActivity, fileUri));
        } catch (Exception ignored) {

        }
        intentLauncher.launch(intent);
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intentLauncher.launch(intent);
    }

    private void cropImage() {
        try {
            Intent intent = CropImage.activity(imgUri)
                    .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                    .setFixAspectRatio(true)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .getIntent(mActivity);
            cropLauncher.launch(intent);
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

    /*
     * Intent launcher to get Image Uri from storage
     * */
    final ActivityResultLauncher<Intent> intentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (fileUri != null) { // Image Capture
                    imgUri = Uri.fromFile(fileUri);
                } else { // Pick from Gallery
                    Intent data = result.getData();
                    assert data != null;
                    imgUri = data.getData();
                }

                Utils.sout("ImageURI:::  " + imgUri);
                cropImage();

            }
        }
    });

    /*
     * Intent launcher to get Image Uri from storage
     * */
    final ActivityResultLauncher<Intent> cropLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                CropImage.ActivityResult re = CropImage.getActivityResult(data);
                assert re != null;
                imageUri = re.getUri();
                if (uploadTask != null && uploadTask.isInProgress()) {
                    screens.showToast(R.string.msgUploadInProgress);
                } else {
                    uploadImage();
                }
            }
        }
    });

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage(getString(R.string.msg_image_upload));
        pd.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + Utils.getExtension(getContext(), imageUri));
            uploadTask = fileReference.putFile(imageUri);

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
                            imgAvatar.setImageURI(imageUri);

                            reference = FirebaseDatabase.getInstance().getReference(REF_USERS).child(firebaseUser.getUid());
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put(EXTRA_IMAGEURL, mUrl);
                            reference.updateChildren(hashMap);
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

}
