package com.devlomi.fireapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.constants.MessageType;
import com.devlomi.fireapp.model.realms.Group;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.ClipboardUtil;
import com.devlomi.fireapp.utils.GroupLinkUtil;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.MessageCreator;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.utils.ServiceHelper;
import com.devlomi.fireapp.views.TextViewDrawableCompat;

import java.util.List;

public class ShareGroupLinkActivity extends AppCompatActivity {

    private static final int REQUEST_SHARE_VIA_FIREAPP = 23;
    private LinearLayout shareLinkLayout;
    private TextView tvGroupLink;
    private TextViewDrawableCompat tvSendLinkViaFireapp;
    private TextViewDrawableCompat tvCopyLink;
    private TextViewDrawableCompat tvShareLink;
    private TextViewDrawableCompat tvRevokeLink;
    private ProgressBar progressBar;


    private Group group;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_group_link);
        shareLinkLayout = findViewById(R.id.share_link_layout);
        tvGroupLink = findViewById(R.id.tv_group_link);
        tvSendLinkViaFireapp = findViewById(R.id.tv_send_link_via_fireapp);
        tvCopyLink = findViewById(R.id.tv_copy_link);
        tvShareLink = findViewById(R.id.tv_share_link);
        tvRevokeLink = findViewById(R.id.tv_revoke_link);
        progressBar = findViewById(R.id.progress_bar);

        String sendLinkText = String.format(getString(R.string.send_link_via_fireapp), getString(R.string.app_name));
        tvSendLinkViaFireapp.setText(sendLinkText);

        final String groupId = getIntent().getStringExtra(IntentUtils.EXTRA_GROUP_ID);

        final User user = RealmHelper.getInstance().getUser(groupId);

        //if there is no group link exists in Realm disable clicks
        //then start to fetch the link, if the link was not created before,
        //create a new one and save it to realm
        disableClicks();
        if (user != null && user.getGroup() != null) {
            group = user.getGroup();
            if (group.getCurrentGroupLink() != null) {
                enableClicks();
                setLinkText(group.getCurrentGroupLink());
            } else {
                tvGroupLink.setText(R.string.no_link_gnerated);
                GroupLinkUtil.getLinkAndFetchNewOneIfNotExists(groupId, new GroupLinkUtil.FetchCurrentLinkCallback() {
                    @Override
                    public void onFetch(String groupLink) {
                        enableClicks();
                        setLinkText(groupLink);
                    }

                    @Override
                    public void onFailed() {
                        disableClicks();
                    }
                });
            }
        }

        tvSendLinkViaFireapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShareGroupLinkActivity.this, ForwardActivity.class);
                startActivityForResult(intent, REQUEST_SHARE_VIA_FIREAPP);
            }
        });


        shareLinkLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (group.getCurrentGroupLink() != null) {
                    startActivity(IntentUtils.getShareTextIntent(getLink()));
                }
            }
        });

        tvCopyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardUtil.copyTextToClipboard(ShareGroupLinkActivity.this, getLink());
                Toast.makeText(ShareGroupLinkActivity.this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
            }
        });


        tvShareLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(IntentUtils.getShareTextIntent(getLink()));
            }
        });

        tvRevokeLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideOrShowProgress(true);
                GroupLinkUtil.generateLink(groupId, new GroupLinkUtil.GenerateLinkCallback() {
                    @Override
                    public void onGenerate(String groupLink) {
                        setLinkText(groupLink);
                        hideOrShowProgress(false);

                    }

                    @Override
                    public void onFailed() {

                    }
                });
            }
        });

    }

    private void setLinkText(String groupLink) {
        tvGroupLink.setText(GroupLinkUtil.getFinalLink(groupLink));
    }


    @NonNull
    private String getLink() {
        return tvGroupLink.getText().toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SHARE_VIA_FIREAPP && resultCode == RESULT_OK) {
            List<User> selectedUsers = data.getParcelableArrayListExtra(IntentUtils.EXTRA_DATA_RESULT);
            String link = getLink();
            for (User selectedUser : selectedUsers) {
                Message message = new MessageCreator.Builder(selectedUser, MessageType.SENT_TEXT).text(link).build();
                ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());
            }

            Toast.makeText(this, R.string.sending_messages, Toast.LENGTH_SHORT).show();
        }
    }

    private void disableClicks() {
        tvShareLink.setEnabled(false);
        tvSendLinkViaFireapp.setEnabled(false);
        tvRevokeLink.setEnabled(false);
        tvCopyLink.setEnabled(false);
        shareLinkLayout.setEnabled(false);
        hideOrShowProgress(true);


    }

    private void hideOrShowProgress(boolean showProgress) {
        progressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        shareLinkLayout.setVisibility(showProgress ? View.GONE : View.VISIBLE);
    }

    private void enableClicks() {
        tvShareLink.setEnabled(true);
        tvSendLinkViaFireapp.setEnabled(true);
        tvRevokeLink.setEnabled(true);
        tvCopyLink.setEnabled(true);
        shareLinkLayout.setEnabled(true);
        hideOrShowProgress(false);
    }
}
