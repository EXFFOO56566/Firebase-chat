package com.devlomi.fireapp.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.devlomi.fireapp.R;
import com.devlomi.fireapp.activities.main.MainActivity;
import com.devlomi.fireapp.adapters.MediaHorizontalAdapter;
import com.devlomi.fireapp.adapters.ParticipantsAdapter;
import com.devlomi.fireapp.events.UpdateGroupEvent;
import com.devlomi.fireapp.model.constants.GroupEventTypes;
import com.devlomi.fireapp.model.realms.Broadcast;
import com.devlomi.fireapp.model.realms.Chat;
import com.devlomi.fireapp.model.realms.Group;
import com.devlomi.fireapp.model.realms.GroupEvent;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.BitmapUtils;
import com.devlomi.fireapp.utils.BroadcastManager;
import com.devlomi.fireapp.utils.ContactUtils;
import com.devlomi.fireapp.utils.FileUtils;
import com.devlomi.fireapp.utils.FireConstants;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.GroupManager;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.MyApp;
import com.devlomi.fireapp.utils.NetworkHelper;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.utils.SharedPreferencesManager;
import com.devlomi.fireapp.views.dialogs.SetGroupTitleDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsActivity extends AppCompatActivity implements ParticipantsAdapter.OnParticipantClick, AppBarLayout.OnOffsetChangedListener {
    public static final int REQUEST_CODE_ADD_GROUP_MEMBERS = 2043;
    private CardView cardViewMedia;
    private TextView tvCountMedia;
    private RecyclerView rvHorizontalMedia;
    private FrameLayout layoutMute;
    private SwitchCompat switchMute;
    private TextView tvStatusDetails;
    private TextView tvNumberDetails;
    private TextView tvParticipantsCount;
    private TextView tvCantSendMessages;
    private TextView tvCreatedBy;
    private TextView tvAddParticipants;


    private CardView aboutAndPhoneNumber;
    private CardView groupParticipants;
    private RecyclerView rvParticipants;
    private CardView cardViewBlock;
    private CardView cardViewExitGroup;
    private TextView tvExitGroup;
    private LinearLayout addParticipantAdmin;
    private View groupSeparator;
    private FrameLayout layoutOnlyAdminsCanPost;
    private SwitchCompat switchAdminsOnlyCanPost;

    private LinearLayout shareInviteLink;
    private View groupSeparatorTwo;


    ParticipantsAdapter participantsAdapter;


    private TextView tvBlock;
    private CollapsingToolbarLayout toolbarLayout;
    private ImageView userImageToolbar;
    Toolbar toolbar;
    private AppBarLayout appBar;


    User user;
    boolean isGroup;
    boolean isBroadcast;
    private MediaHorizontalAdapter adapter;
    private List<Message> mediaList;
    FireManager.OnGetUserPhoto onGetUserPhoto;
    List<User> userList;
    private boolean isAdmin = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        init();
        setSupportActionBar(toolbar);


        final String userId = getIntent().getStringExtra(IntentUtils.UID);
        //getting the user from realm because the thumb img may different from the parcelable
        user = RealmHelper.getInstance().getUser(userId);
        isGroup = user.isGroupBool();
        isBroadcast = user.isBroadcastBool();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get userName from Phonebook
        String name = user.getUserName();
        toolbarLayout.setTitle(name);
        toolbar.setTitle(name);

        mediaList = RealmHelper.getInstance().getMediaInChat(user.getUid());

        int mediaCount = mediaList.size();

        tvCountMedia.setText(mediaCount + "");
        if (mediaCount == 0) {
            cardViewMedia.setVisibility(View.GONE);
        }
        tvNumberDetails.setText(user.getPhone());
        tvStatusDetails.setText(user.getStatus());

        //if the chat is exists then show the muted button
        //otherwise hide it
        if (RealmHelper.getInstance().getChat(user.getUid()) != null) {
            Chat chat = RealmHelper.getInstance().getChat(user.getUid());
            switchMute.setChecked(chat.isMuted());
        } else {
            layoutMute.setVisibility(View.GONE);
            cardViewMedia.setVisibility(View.GONE);
        }


        if (isGroup) {
            Group group = user.getGroup();
            isAdmin = FireManager.isAdmin(group.getAdminsUids());
            userList = group.getUsers();
            showParticipantsLayout();
            tvParticipantsCount.setText(userList.size() + " " + getResources().getString(R.string.participants));
            String createdByName = group.getCreatedByNumber().equals(SharedPreferencesManager.getPhoneNumber()) ? getResources().getString(R.string.you) : ContactUtils.queryForNameByNumber(this, group.getCreatedByNumber());
            String createdByStr = getResources().getString(R.string.created_by)
                    + " "
                    + createdByName
                    + " " + getResources().getString(R.string.at)
                    + " " + group.getTime();
            tvCreatedBy.setText(createdByStr);
            if (isAdmin) {
                showAdminViews();
            }else {
                groupSeparatorTwo.setVisibility(View.GONE);
            }
        } else if (isBroadcast) {
            Broadcast broadcast = user.getBroadcast();
            userList = broadcast.getUsers();
            setRecipientsCount();
            tvAddParticipants.setText(R.string.edit_recipients);
            String createdByStr = getResources().getString(R.string.created) + " " + broadcast.getTime();
            tvCreatedBy.setText(createdByStr);
            showParticipantsLayout();
            addParticipantAdmin.setVisibility(View.VISIBLE);
            groupSeparator.setVisibility(View.VISIBLE);
            groupSeparatorTwo.setVisibility(View.GONE);
            layoutMute.setVisibility(View.GONE);
            tvExitGroup.setText(R.string.delete_broadcast_list);
        }

        setUesrBlockedText();

        loadUserImageLocally();


        setAdapter();


        //show user image in another activity
        userImageToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDetailsActivity.this, ProfilePhotoActivity.class);
                intent.putExtra(IntentUtils.UID, user.getUid());
                startActivity(intent);
            }
        });

        //show all media in this chat
        tvCountMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserDetailsActivity.this, MediaGalleryActivity.class);
                intent.putExtra(IntentUtils.UID, user.getUid());
                startActivity(intent);
            }
        });


        addParticipantAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addParticipants();
            }
        });

        // mute/unmute
        switchMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                RealmHelper.getInstance().setMuted(user.getUid(), isChecked);
            }
        });

        switchAdminsOnlyCanPost.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
                if (!NetworkHelper.isConnected(UserDetailsActivity.this)) {
                    Toast.makeText(UserDetailsActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    compoundButton.setChecked(!b);
                    return;
                }

                FireConstants.groupsRef.child(user.getUid()).child("info").child("onlyAdminsCanPost").setValue(b).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            RealmHelper.getInstance().setOnlyAdminsCanPost(user.getUid(), b);
                            new GroupEvent(SharedPreferencesManager.getPhoneNumber(), GroupEventTypes.GROUP_SETTINGS_CHANGED, null).createGroupEvent(user, null);
                        } else {
                            compoundButton.setChecked(false);
                            Toast.makeText(UserDetailsActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


        cardViewBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUserBlocked();
            }
        });

        cardViewExitGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBroadcast)
                    deleteBroadcast();
                else if (isGroup)
                    exitGroup();
            }
        });

        shareInviteLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserDetailsActivity.this, ShareGroupLinkActivity.class);
                intent.putExtra(IntentUtils.EXTRA_GROUP_ID, user.getGroup().getGroupId());
                startActivity(intent);
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        onGetUserPhoto = new FireManager.OnGetUserPhoto() {
            @Override
            public void onGetThumb(String thumbImg) {
                //load thumb img while the full image is downloading
                try {
                    Glide.with(UserDetailsActivity.this)
                            .load(BitmapUtils.encodeImageAsBytes(thumbImg))
                            .asBitmap()
                            .into(userImageToolbar);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onGetPhoto(String photoPath) {
                try {
                    Glide.with(UserDetailsActivity.this).load(photoPath).into(userImageToolbar);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        //check for new image and load it
        if (!isBroadcast)
            FireManager.checkAndDownloadUserPhoto(user, onGetUserPhoto);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        //update items if the user has deleted images while he is in "MediaGalleryActivity"
        //and he is getting back from there
        adapter.notifyDataSetChanged();
        //update count also
        if (mediaList != null)
            tvCountMedia.setText(mediaList.size() + "");
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //free up resources and avoid memory leaks
        onGetUserPhoto = null;
    }

    private void exitGroup() {
        if (!user.getGroup().isActive()) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.confirmation)
                    .setMessage(R.string.delete_group_confirmation)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            RealmHelper.getInstance().deleteChat(user.getUid());
                            startMainActivityAndFinish();
                        }
                    }).show();

        } else {
            if (!NetworkHelper.isConnected(this)) {
                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder dialog = new AlertDialog.Builder(UserDetailsActivity.this);
            dialog.setTitle(R.string.confirmation)
                    .setMessage(R.string.exit_group_confirmation_dialog)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final ProgressDialog progressDialog = new ProgressDialog(UserDetailsActivity.this);
                            progressDialog.setCancelable(false);
                            progressDialog.setMessage(getResources().getString(R.string.exiting_group));
                            progressDialog.show();
                            GroupManager.exitGroup(user.getUid(), FireManager.getUid(), new GroupManager.OnComplete() {
                                @Override
                                public void onComplete(boolean isSuccessful) {
                                    progressDialog.dismiss();
                                    if (isSuccessful) {
                                        GroupEvent groupEvent = new GroupEvent(SharedPreferencesManager.getPhoneNumber(), GroupEventTypes.USER_LEFT_GROUP, null);
                                        groupEvent.createGroupEvent(UserDetailsActivity.this.user, null);
                                        RealmHelper.getInstance().exitGroup(user.getUid());
                                        startMainActivityAndFinish();
                                    } else {
                                        Toast.makeText(UserDetailsActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }).show();


        }
    }

    private void startMainActivityAndFinish() {
        startActivity(new Intent(UserDetailsActivity.this, MainActivity.class));
        finish();
    }

    private void deleteBroadcast() {

        if (!NetworkHelper.isConnected(MyApp.context())) {
            Toast.makeText(UserDetailsActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.confirmation)
                .setMessage(R.string.delete_broadcast_confirmation)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        BroadcastManager.deleteBroadcast(user.getUid(), new BroadcastManager.OnComplete() {
                            @Override
                            public void onComplete(boolean isSuccessful) {
                                if (isSuccessful) {
                                    startMainActivityAndFinish();
                                } else {
                                    Toast.makeText(UserDetailsActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).show();

    }

    private void showAdminViews() {
        addParticipantAdmin.setVisibility(View.VISIBLE);
        groupSeparator.setVisibility(View.VISIBLE);
        groupSeparatorTwo.setVisibility(View.VISIBLE);
        shareInviteLink.setVisibility(View.VISIBLE);
        layoutOnlyAdminsCanPost.setVisibility(View.VISIBLE);
        switchAdminsOnlyCanPost.setChecked(user.getGroup().isOnlyAdminsCanPost());
    }

    private void setAdapter() {
        adapter = new MediaHorizontalAdapter(this, mediaList, user);
        //set horizontal media items
        rvHorizontalMedia.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHorizontalMedia.setAdapter(adapter);
        if (isGroup || isBroadcast) {
            if (isGroup)
                participantsAdapter = new ParticipantsAdapter(this, user.getGroup().getAdminsUids(), userList, this);
            else participantsAdapter = new ParticipantsAdapter(this, userList, this);
            rvParticipants.setLayoutManager(new LinearLayoutManager(this));
            rvParticipants.setAdapter(participantsAdapter);
            appBar.addOnOffsetChangedListener(this);
        }
    }


    private void showParticipantsLayout() {
        cardViewBlock.setVisibility(View.GONE);
        cardViewExitGroup.setVisibility(View.VISIBLE);
        aboutAndPhoneNumber.setVisibility(View.GONE);
        groupParticipants.setVisibility(View.VISIBLE);
        tvCreatedBy.setVisibility(View.VISIBLE);

        if (!isGroup) return;

        if (!user.getGroup().isActive()) {
            tvCantSendMessages.setVisibility(View.VISIBLE);
            layoutMute.setVisibility(View.GONE);
            Drawable drawable = AppCompatResources.getDrawable(this, R.drawable.basket_red);
            tvExitGroup.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            tvExitGroup.setText(R.string.delete_group);
        }

    }

    private void loadUserImageLocally() {

        RequestListener<Object, Bitmap> requestListener = new RequestListener<Object, Bitmap>() {
            @Override
            public boolean onException(Exception e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                createPaletteAsync(resource);
                return false;
            }


        };

        if (isBroadcast) {
            Drawable drawable = AppCompatResources.getDrawable(this, R.drawable.ic_broadcast_with_bg);
            userImageToolbar.setImageDrawable(drawable);
        } else if (user.getUserLocalPhoto() != null && FileUtils.isFileExists(user.getUserLocalPhoto())) {
            Glide.with(this).load(user.getUserLocalPhoto()).asBitmap().listener(requestListener).into(userImageToolbar);
        } else if (user.getThumbImg() != null) {
            byte[] bytes = BitmapUtils.encodeImageAsBytes(user.getThumbImg());
            Glide.with(this).load(bytes).asBitmap().listener(requestListener).into(userImageToolbar);
        }
    }


    private void setUesrBlockedText() {
        tvBlock.setText(user.isBlocked() ? R.string.unblock : R.string.block_contact);
    }


    private void init() {
        appBar = findViewById(R.id.app_bar);
        toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbar = findViewById(R.id.toolbar);
        userImageToolbar = findViewById(R.id.user_image_toolbar);
        cardViewMedia = findViewById(R.id.card_view_media);
        tvCountMedia = findViewById(R.id.tv_count_media);
        rvHorizontalMedia = findViewById(R.id.rv_horizontal_media);
        layoutMute = findViewById(R.id.layout_mute);
        switchMute = findViewById(R.id.switch_mute);
        tvStatusDetails = findViewById(R.id.tv_status_details);
        tvNumberDetails = findViewById(R.id.tv_number_details);
        cardViewBlock = findViewById(R.id.card_view_block);
        tvBlock = findViewById(R.id.tv_block);
        aboutAndPhoneNumber = findViewById(R.id.about_and_phone_number);
        groupParticipants = findViewById(R.id.group_participants);
        rvParticipants = findViewById(R.id.rv_participants);
        cardViewExitGroup = findViewById(R.id.card_view_exit_group);
        tvExitGroup = findViewById(R.id.tv_exit_group);
        tvParticipantsCount = findViewById(R.id.tv_participants_count);
        addParticipantAdmin = findViewById(R.id.share_link_layout);
        groupSeparator = findViewById(R.id.group_separator);
        tvCantSendMessages = findViewById(R.id.tv_cant_send_messages);
        tvCreatedBy = findViewById(R.id.tv_created_by);
        layoutOnlyAdminsCanPost = findViewById(R.id.layout_only_admins_can_post);
        switchAdminsOnlyCanPost = findViewById(R.id.switch_admins_only_can_post);
        shareInviteLink = findViewById(R.id.share_invite_link);
        groupSeparatorTwo = findViewById(R.id.group_separator_two);
        tvAddParticipants = findViewById(R.id.tv_add_participants);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.add_participants:
                addParticipants();
                break;

            case R.id.edit_group:
                editGroupClicked();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editGroupClicked() {
        if (!NetworkHelper.isConnected(this)) {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        SetGroupTitleDialog setGroupTitleDialog = new SetGroupTitleDialog(this, user.getUserName());
        if (isBroadcast)
            setGroupTitleDialog.setDialogTitle(getResources().getString(R.string.broadcast_name));
        setGroupTitleDialog.setmListener(new SetGroupTitleDialog.OnFragmentInteractionListener() {
            @Override
            public void onPositiveClick(final String newTitle) {

                //if it's the same name do nothing
                if (newTitle.equals(user.getUserName()))
                    return;

                final ProgressDialog progressDialog = new ProgressDialog(UserDetailsActivity.this);
                progressDialog.setMessage(getResources().getString(R.string.loading));
                progressDialog.setCancelable(false);
                progressDialog.show();
                if (isGroup) {
                    GroupManager.changeGroupName(newTitle, user.getUid(), new GroupManager.OnComplete() {
                        @Override
                        public void onComplete(boolean isSuccessful) {
                            progressDialog.dismiss();
                            if (isSuccessful) {
                                Toast.makeText(UserDetailsActivity.this, R.string.group_name_changed, Toast.LENGTH_SHORT).show();
                                new GroupEvent(SharedPreferencesManager.getPhoneNumber(), GroupEventTypes.GROUP_SETTINGS_CHANGED, null).createGroupEvent(user, null);
                                updateGroupEvent(new UpdateGroupEvent(user.getUid()));
                            } else {
                                Toast.makeText(UserDetailsActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else if (isBroadcast) {
                    BroadcastManager.changeBroadcastName(user.getUid(), newTitle, new BroadcastManager.OnComplete() {
                        @Override
                        public void onComplete(boolean isSuccessful) {
                            progressDialog.dismiss();
                            if (isSuccessful) {
                                toolbarLayout.setTitle(newTitle);
                                toolbar.setTitle(newTitle);
                            }
                        }
                    });
                }
            }
        });


        setGroupTitleDialog.show();
    }

    private void addParticipants() {
        Intent intent = new Intent(this, NewGroupActivity.class);
        intent.putExtra(IntentUtils.UID, user.getUid());
        intent.putExtra(IntentUtils.IS_BROADCAST, isBroadcast);
        startActivityForResult(intent, REQUEST_CODE_ADD_GROUP_MEMBERS);
    }

    @Subscribe
    public void updateGroupEvent(UpdateGroupEvent event) {
        String groupId = event.getGroupId();
        tvParticipantsCount.setText(userList.size() + " " + getResources().getString(R.string.participants));
        if (groupId.equals(user.getUid())) {
            String groupTitle = user.getUserName();
            toolbarLayout.setTitle(groupTitle);
            toolbar.setTitle(groupTitle);
            //check for new image and load it
            FireManager.checkAndDownloadUserPhoto(user, onGetUserPhoto);
            participantsAdapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_GROUP_MEMBERS && resultCode == RESULT_OK) {
            if (!NetworkHelper.isConnected(UserDetailsActivity.this)) {
                Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                return;
            }

            final ProgressDialog progressDialog = new ProgressDialog(UserDetailsActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getResources().getString(R.string.adding));
            progressDialog.show();
            final ArrayList<User> selectedUsers = data.getParcelableArrayListExtra(IntentUtils.EXTRA_SELECTED_USERS);
            if (isGroup) {
                GroupManager.addParticipant(user.getUid(), selectedUsers, new GroupManager.OnComplete() {
                    @Override
                    public void onComplete(boolean isSuccessful) {
                        progressDialog.dismiss();
                        if (isSuccessful) {
                            RealmHelper.getInstance().addUsersToGroup(user.getUid(), selectedUsers);
                            for (User selectedUser : selectedUsers) {
                                GroupEvent groupEvent = new GroupEvent(SharedPreferencesManager.getPhoneNumber(), GroupEventTypes.USER_ADDED, selectedUser.getPhone());
                                groupEvent.createGroupEvent(UserDetailsActivity.this.user, null);

                            }
                            participantsAdapter.notifyDataSetChanged();
                            Toast.makeText(UserDetailsActivity.this, R.string.added_successfully, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UserDetailsActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (isBroadcast) {
                BroadcastManager.addParticipant(user.getUid(), selectedUsers, new BroadcastManager.OnComplete() {
                    @Override
                    public void onComplete(boolean isSuccessful) {
                        if (isSuccessful)
                            participantsAdapter.notifyDataSetChanged();

                        setRecipientsCount();
                        progressDialog.dismiss();
                    }
                });
            }
        }
    }

    private void setRecipientsCount() {
        tvParticipantsCount.setText(userList.size() + " " + getResources().getString(R.string.recipients));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isGroup && isAdmin || isBroadcast) {
            getMenuInflater().inflate(R.menu.menu_user_details, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void setUserBlocked() {
        final String receiverUid = user.getUid();
        if (NetworkHelper.isConnected(this)) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(R.string.loading);
            progressDialog.setCancelable(false);
            progressDialog.show();


            //unblock user
            if (user.isBlocked()) {
                FireManager.setUserBlocked(FireManager.getUid(), receiverUid, false, new FireManager.OnComplete() {
                    @Override
                    public void onComplete(boolean isSuccessful) {
                        progressDialog.dismiss();

                        if (isSuccessful) {
                            //update it locally
                            RealmHelper.getInstance().setUserBlocked(user, false);
                            setUesrBlockedText();
                        }


                    }
                });
            }

            //block user
            else {
                FireManager.setUserBlocked(FireManager.getUid(), receiverUid, true, new FireManager.OnComplete() {
                    @Override
                    public void onComplete(boolean isSuccessful) {
                        progressDialog.dismiss();
                        if (isSuccessful) {
                            //update it locally
                            RealmHelper.getInstance().setUserBlocked(user, true);
                            setUesrBlockedText();
                        }
                    }
                });

            }
        } else {
            Snackbar.make(findViewById(android.R.id.content), R.string.no_internet_connection, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(User user, View view) {
        startChatActivityWithDifferentUser(user);
    }

    @Override
    public void onLongClick(final User user, View view) {
        if (isAdmin || isBroadcast) {


            PopupMenu popup = new PopupMenu(UserDetailsActivity.this, view);
            //Inflating the Popup using xml file
            popup.getMenuInflater()
                    .inflate(isBroadcast ? R.menu.popup_menu_broadcast_options : R.menu.popup_menu_admin_options, popup.getMenu());


            String userName = user.getUserName();
            boolean isSelectedUserIsAdmin = false;

            if (isAdmin) {
                String adminStr = isSelectedUserIsAdmin ? getResources().getString(R.string.dismiss_admin) : getResources().getString(R.string.make_group_admin);
                popup.getMenu().findItem(R.id.make_group_admin).setTitle(adminStr);
                isSelectedUserIsAdmin = FireManager.isAdmin(user.getUid(), UserDetailsActivity.this.user.getGroup().getAdminsUids());
            }

            popup.getMenu().findItem(R.id.message_member).setTitle(getResources().getString(R.string.message_member) + " " + userName);

            popup.getMenu().findItem(R.id.delete_group_member).setTitle(getResources().getString(R.string.remove_member) + " " + userName);

            final boolean finalIsSelectedUserIsAdmin = isSelectedUserIsAdmin;
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.message_member:
                            startChatActivityWithDifferentUser(user);
                            break;

                        case R.id.make_group_admin:
                            makeGroupAdmin(user, !finalIsSelectedUserIsAdmin);
                            break;

                        case R.id.delete_group_member:
                            AlertDialog.Builder deleteMemberDialog = new AlertDialog.Builder(UserDetailsActivity.this);
                            deleteMemberDialog.setTitle(R.string.delete_member)
                                    .setMessage(R.string.delete_member_message)
                                    .setNegativeButton(R.string.no, null)
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (isGroup)
                                                deleteGroupMember(user);
                                            else if (isBroadcast) {
                                                deleteBroadcastMember(user.getUid());
                                            }
                                        }
                                    }).show();
                            break;
                    }
                    return true;
                }
            });
            popup.show();


        }
    }

    private void deleteBroadcastMember(final String userToDeleteUid) {
        if (!NetworkHelper.isConnected(this)) {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.removing_member));
            progressDialog.setCancelable(false);
            progressDialog.show();
            BroadcastManager.removeBroadcastMember(user.getUid(), userToDeleteUid, new BroadcastManager.OnComplete() {
                @Override
                public void onComplete(boolean isSuccessful) {
                    progressDialog.dismiss();
                    setRecipientsCount();
                    if (isSuccessful) {
                        participantsAdapter.notifyDataSetChanged();
                        Toast.makeText(UserDetailsActivity.this, R.string.member_removed, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserDetailsActivity.this, R.string.error_removing_member, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void makeGroupAdmin(final User userToSet, final boolean setAdmin) {
        if (!NetworkHelper.isConnected(this))
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();

        FireConstants.groupsRef.child(user.getUid()).child("users").child(userToSet.getUid()).setValue(setAdmin).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                RealmHelper.getInstance().setGroupAdmin(user.getUid(), userToSet.getUid(), setAdmin);
                new GroupEvent(SharedPreferencesManager.getPhoneNumber()
                        , setAdmin ? GroupEventTypes.ADMIN_ADDED : GroupEventTypes.ADMIN_REMOVED
                        , userToSet.getPhone()).createGroupEvent(user, null);
                participantsAdapter.notifyDataSetChanged();
            }
        });
    }


    private void deleteGroupMember(final User userToDelete) {
        if (!NetworkHelper.isConnected(this)) {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.removing_member));
            progressDialog.setCancelable(false);
            progressDialog.show();
            GroupManager.removeGroupMember(user.getUid(), userToDelete.getUid(), new GroupManager.OnComplete() {
                @Override
                public void onComplete(boolean isSuccessful) {
                    progressDialog.dismiss();
                    if (isSuccessful) {
                        GroupEvent groupEvent = new GroupEvent(SharedPreferencesManager.getPhoneNumber(), GroupEventTypes.USER_REMOVED_BY_ADMIN, userToDelete.getPhone());
                        groupEvent.createGroupEvent(UserDetailsActivity.this.user, null);
                        RealmHelper.getInstance().deleteGroupMember(user.getUid(), userToDelete.getUid());
                        participantsAdapter.notifyDataSetChanged();
                        Toast.makeText(UserDetailsActivity.this, R.string.member_removed, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserDetailsActivity.this, R.string.error_removing_member, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void startChatActivityWithDifferentUser(User user) {
        Intent intent = new Intent(UserDetailsActivity.this, ChatActivity.class);
        intent.putExtra(IntentUtils.UID, user.getUid());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    //hide and show 'created by' textView  when toolbar collapsed/expanded
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        if (Math.abs(verticalOffset) >= (appBarLayout.getTotalScrollRange())) {
            tvCreatedBy.setVisibility(View.GONE);
        } else {
            tvCreatedBy.setVisibility(View.VISIBLE);
        }
    }

    //change statusBar and toolbar colors to the Dominant color of the image
    public void createPaletteAsync(Bitmap bitmap) {
        if (bitmap == null) return;
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
                int mutedColor = p.getDominantColor(R.attr.colorPrimary);
                setStatusBarColor(mutedColor);
                toolbarLayout.setBackgroundColor(mutedColor);
                toolbarLayout.setStatusBarScrimColor(mutedColor);
                toolbarLayout.setContentScrimColor(mutedColor);

            }
        });
    }

    private void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // finally change the color
            window.setStatusBarColor(color);
        }
    }


}
