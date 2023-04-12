package com.bytesbee.firebase.chat.activities.fragments;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_USER_ID;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.GEN_MALE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.GEN_UNSPECIFIED;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.IMG_PREVIEW;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_USERS;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bytesbee.firebase.chat.activities.R;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.models.User;
import com.bytesbee.firebase.chat.activities.views.SingleClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


public class ViewUserProfileFragment extends BaseFragment {

    private CircleImageView imgAvatar;

    private ImageView imgBlurImage;
    private String strDescription = "", strAvatarImg, strUsername = "";
    private String viewUserId = "";
    private int strGender = GEN_UNSPECIFIED;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_profile_new, container, false);

        final FloatingActionButton fabChat = view.findViewById(R.id.fabChat);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        imgBlurImage = view.findViewById(R.id.imgRelativeBlue);
        final TextView txtUsername = view.findViewById(R.id.txtUsername);
        final TextView txtEmail = view.findViewById(R.id.txtEmail);
        final TextView txtAbout = view.findViewById(R.id.txtAbout);
        final TextView txtGender = view.findViewById(R.id.txtGender);

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final Intent i = getActivity().getIntent();
        final String userId = i.getStringExtra(EXTRA_USER_ID);

        if (Utils.isEmpty(userId)) {
            assert firebaseUser != null;
            viewUserId = firebaseUser.getUid();
        } else {
            viewUserId = userId;
        }
        assert firebaseUser != null;
        if (viewUserId.equalsIgnoreCase(firebaseUser.getUid())) {
            fabChat.hide();
        } else {
            fabChat.show();
        }

        imgAvatar.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                screens.openFullImageViewActivity(v, strAvatarImg, strUsername);
            }
        });

        final String lblStatus = mActivity.getString(R.string.strAboutStatus);
        final String lblUnSpecified = getString(R.string.strUnspecified);
        final String lblMale = getString(R.string.strMale);
        final String lblFemale = getString(R.string.strFemale);

        final String msgPrivateEmail = mActivity.getString(R.string.msgPrivateEmail);
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference(REF_USERS).child(viewUserId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    strUsername = user.getUsername();
                    txtUsername.setText(strUsername);
                    String email = user.getEmail();
                    strAvatarImg = user.getImageURL();
                    if (!viewUserId.equalsIgnoreCase(firebaseUser.getUid())) {
                        if (user.isHideEmail()) {
                            email = msgPrivateEmail;
                        }
                        if (user.isHideProfilePhoto()) {
                            strAvatarImg = IMG_PREVIEW;
                        }
                    }
                    strGender = user.getGenders();
                    strDescription = user.getAbout();

                    txtEmail.setText(email);

                    txtAbout.setText(Utils.isEmpty(strDescription) ? lblStatus : strDescription);
                    txtGender.setText(strGender == GEN_UNSPECIFIED ? lblUnSpecified : (strGender == GEN_MALE ? lblMale : lblFemale));

                    Utils.setProfileImage(getContext(), strAvatarImg, imgAvatar);
                    Utils.setProfileBlurImage(getContext(), strAvatarImg, imgBlurImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fabChat.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                screens.openUserMessageActivity(viewUserId);
            }
        });


        return view;
    }

}
