package com.devlomi.fireapp.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.GroupLinkUtil;
import com.devlomi.fireapp.utils.GroupManager;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.utils.Util;
import com.devlomi.fireapp.views.AcceptInviteBottomSheet;

public class AcceptInviteLink extends AppCompatActivity implements AcceptInviteBottomSheet.BottomSheetCallbacks {
    String groupId;
    private AcceptInviteBottomSheet bottomSheet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!Util.isOreoOrAbove()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();

        bottomSheet = new AcceptInviteBottomSheet();
        bottomSheet.show(getSupportFragmentManager(), "");


        if (intent.getData() == null || intent.getData().getLastPathSegment() == null) {
            onInvalidLink();
        } else {

            String groupLink = intent.getData().getLastPathSegment();
            //check if link is valid
            GroupLinkUtil.isGroupLinkValid(groupLink, new GroupLinkUtil.GetGroupByLinkCallback() {
                @Override
                public void onFound(final String groupId) {
                    AcceptInviteLink.this.groupId = groupId;
                    //if chat user already in group do nothing
                    User user = RealmHelper.getInstance().getUser(groupId);
                    if (user != null && user.getGroup() != null && user.getGroup().isActive()) {
                        alreadyInGroup();
                        return;
                }

                    //check if user is banned from group
                    GroupManager.isUserBannedFromGroup(groupId, FireManager.getUid(), new GroupManager.IsUserBannedCallback() {
                        @Override
                        public void onComplete(boolean isBanned) {
                            if (isBanned) {
                                Toast.makeText(AcceptInviteLink.this, R.string.banned_from_group, Toast.LENGTH_SHORT).show();
                                finish();

                            } else {

                                GroupManager.fetchGroupPartialInfo(AcceptInviteLink.this, groupId, new GroupManager.FetchPartialGroupInfoCallback() {
                                    @Override
                                    public void onComplete(User user, int usersCount) {
                                        if (bottomSheet != null)
                                            bottomSheet.showData(user, usersCount);
                                    }

                                    @Override
                                    public void onFailed() {

                                    }
                                });

                            }
                        }

                        @Override
                        public void onFailed() {
                            Toast.makeText(AcceptInviteLink.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }

                @Override
                public void onError() {
                    onInvalidLink();
                }
            });


        }


    }

    private void alreadyInGroup() {
        Toast.makeText(this, R.string.you_are_already_joined_the_group, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void onInvalidLink() {
        Toast.makeText(this, getString(R.string.invalid_group_link), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onDismiss() {
        finish();
    }

    @Override
    public void onJoinBtnClick() {
        if (groupId == null) return;
        if (bottomSheet != null) {
            bottomSheet.showLoadingOnJoin();
        }
        GroupManager.fetchAndCreateGroupFromLink(AcceptInviteLink.this, groupId, new GroupManager.OnFetchGroupsComplete() {
            @Override
            public void onComplete(String groupId) {
                Intent mIntent = new Intent(AcceptInviteLink.this, ChatActivity.class);
                mIntent.putExtra(IntentUtils.UID, groupId);
                startActivity(mIntent);
                finish();
            }
        });
    }
}
