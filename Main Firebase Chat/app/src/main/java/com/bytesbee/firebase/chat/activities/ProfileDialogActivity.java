package com.bytesbee.firebase.chat.activities;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_OBJ_GROUP;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bytesbee.firebase.chat.activities.managers.Screens;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.models.Groups;
import com.bytesbee.firebase.chat.activities.models.User;
import com.bytesbee.firebase.chat.activities.views.SingleClickListener;

public class ProfileDialogActivity extends AppCompatActivity {

    private String username;
    private User user;
    private Groups groups;
    private boolean isGroupObj = false;
    private Screens screens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.profile_dialog);
        screens = new Screens(this);
        final Intent intent = getIntent();
        String imgPath;
        try {
            user = (User) intent.getSerializableExtra(EXTRA_OBJ_GROUP);
            username = user.getUsername();
            imgPath = user.getImageURL();
        } catch (Exception e) {
            isGroupObj = true;
            groups = (Groups) intent.getSerializableExtra(EXTRA_OBJ_GROUP);
            username = groups.getGroupName();
            imgPath = groups.getGroupImg();
        }

        try {
            TextView txtUsername = findViewById(R.id.txtUsername);
            ImageView profileImage = findViewById(R.id.profile_image);
            Glide.with(this)
                    .load(imgPath)
                    .apply(isGroupObj ? new RequestOptions().placeholder(R.drawable.img_group_default_orange) : new RequestOptions().placeholder(R.drawable.profile_avatar))
                    .into(profileImage);
            txtUsername.setText(username);

            ImageView imgChatView = findViewById(R.id.imgChatView);
            final ImageView imgInfoView = findViewById(R.id.imgInfoView);

            imgChatView.setOnClickListener(new SingleClickListener() {
                @Override
                public void onClickView(View v) {
                    if (isGroupObj) {
                        screens.openGroupMessageActivity(groups);
                    } else {
                        screens.openUserMessageActivity(user.getId());
                    }
                    finish();
                }
            });

            imgInfoView.setOnClickListener(new SingleClickListener() {
                @Override
                public void onClickView(View v) {
                    if (isGroupObj) {
                        screens.openGroupParticipantActivity(groups);
                    } else {
                        screens.openViewProfileActivity(user.getId());
                    }
                    finish();
                }
            });

            profileImage.setOnClickListener(new SingleClickListener() {
                @Override
                public void onClickView(View v) {
                    try {
                        String strAvatarImg;
                        if (isGroupObj) {
                            strAvatarImg = groups.getGroupImg();
                            screens.openFullImageViewActivity(v, strAvatarImg, groups.getGroupName(), "");
                        } else {
                            strAvatarImg = user.getImageURL();
                            screens.openFullImageViewActivity(v, strAvatarImg, username);
                        }
                    } catch (Exception e) {
                        Utils.getErrors(e);
                    }
                }
            });
        } catch (Exception ignored) {

        }

    }
}