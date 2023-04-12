package com.bytesbee.firebase.chat.activities;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.BROADCAST_DOWNLOAD_EVENT;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.DELAY_ONE_SEC;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.DOWNLOAD_DATA;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EMPTY;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_ATTACH_DATA;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_ATTACH_DURATION;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_ATTACH_FILE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_ATTACH_NAME;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_ATTACH_PATH;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_ATTACH_SIZE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_ATTACH_TYPE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_DATETIME;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_GROUPS_IN_BOTH;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_GROUP_ID;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_IMGPATH;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_LAST_MSG;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_LAST_TIME;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_MESSAGE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_OBJ_GROUP;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_RECEIVER;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_SEEN;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_SENDER;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_TYPE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXT_MP3;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXT_VCF;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.FALSE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.FCM_URL;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.IMG_FOLDER;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.ONE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.PERMISSION_AUDIO;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.PERMISSION_CONTACT;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.PERMISSION_DOCUMENT;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.PERMISSION_VIDEO;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUPS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUPS_MESSAGES;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUPS_S;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUP_ATTACHMENT;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUP_MEMBERS_S;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_GROUP_PHOTO_UPLOAD;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_TOKENS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_USERS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REF_VIDEO_THUMBS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REQUEST_CODE_CONTACT;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REQUEST_CODE_PLAY_SERVICES;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REQUEST_PARTICIPATE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REQUEST_PERMISSION_RECORD;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.SETTING_ALL_PARTICIPANTS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.SLASH;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TRUE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TWO;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_CONTACT;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_IMAGE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_RECORDING;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_TEXT;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.VIBRATE_HUNDRED;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.ZERO;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytesbee.firebase.chat.activities.adapters.GroupsMessageAdapters;
import com.bytesbee.firebase.chat.activities.async.BaseTask;
import com.bytesbee.firebase.chat.activities.async.TaskRunner;
import com.bytesbee.firebase.chat.activities.fcm.APIService;
import com.bytesbee.firebase.chat.activities.fcm.RetroClient;
import com.bytesbee.firebase.chat.activities.fcmmodels.Data;
import com.bytesbee.firebase.chat.activities.fcmmodels.MyResponse;
import com.bytesbee.firebase.chat.activities.fcmmodels.Sender;
import com.bytesbee.firebase.chat.activities.fcmmodels.Token;
import com.bytesbee.firebase.chat.activities.managers.DownloadUtil;
import com.bytesbee.firebase.chat.activities.managers.FirebaseUploader;
import com.bytesbee.firebase.chat.activities.managers.SessionManager;
import com.bytesbee.firebase.chat.activities.managers.Utils;
import com.bytesbee.firebase.chat.activities.models.Attachment;
import com.bytesbee.firebase.chat.activities.models.AttachmentTypes;
import com.bytesbee.firebase.chat.activities.models.Chat;
import com.bytesbee.firebase.chat.activities.models.DownloadFileEvent;
import com.bytesbee.firebase.chat.activities.models.Groups;
import com.bytesbee.firebase.chat.activities.models.LocationAddress;
import com.bytesbee.firebase.chat.activities.models.User;
import com.bytesbee.firebase.chat.activities.views.SingleClickListener;
import com.bytesbee.firebase.chat.activities.views.files.FileUtils;
import com.bytesbee.firebase.chat.activities.views.files.MediaFile;
import com.bytesbee.firebase.chat.activities.views.files.PickerManager;
import com.bytesbee.firebase.chat.activities.views.files.PickerManagerCallbacks;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;
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
import com.google.gson.Gson;
import com.rtchagas.pingplacepicker.PingPlacePicker;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.wafflecopter.multicontactpicker.ContactResult;
import com.wafflecopter.multicontactpicker.LimitColumn;
import com.wafflecopter.multicontactpicker.MultiContactPicker;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupsMessagesActivity extends BaseActivity implements View.OnClickListener, PickerManagerCallbacks {

    private ImageView mImageView;
    private TextView txtGroupName, txtTyping;
    private RecyclerView mRecyclerView;
    private Groups groups;
    private String groupId, groupName = "";

    private Toolbar mToolbar;
    private ArrayList<Chat> chats;
    private GroupsMessageAdapters messageAdapters;

    private Map<String, User> userList;
    private List<String> memberList;

    private APIService apiService;

    private boolean notify = false;

    private String strUsername, strGroups;
    private Uri imageUri = null;
    private StorageTask uploadTask;
    private FirebaseStorage storage;
    private StorageReference storageReference, storageAttachment;
    private String currentUserId;

    //New Component
    private LinearLayout btnGoToBottom;
    private EmojiPopup emojiIcon;
    private CardView mainAttachmentLayout;
    private View attachmentBGView;
    private EmojiEditText newMessage;
    private ImageView imgAddAttachment, imgAttachmentEmoji, imgCamera;
    private RelativeLayout rootView;

    //Picker
    private PickerManager pickerManager;

    //Recording
    private Handler recordWaitHandler, recordTimerHandler;
    private Runnable recordRunnable, recordTimerRunnable;
    private MediaRecorder mRecorder = null;
    private String recordFilePath;
    private RecordView recordView;
    private RecordButton recordButton;
    private boolean isStart = false;
    private int firstVisible = -1;

    private String vCardData, displayName, phoneNumber;
    private File fileUri = null;
    private Uri imgUri;

    //Admin Message
    private RelativeLayout rlChatView, rlAdminMsgView;
    private TextView lblOnlyAdminMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_for_group);

        mActivity = this;

        apiService = RetroClient.getClient(FCM_URL).create(APIService.class);
        initUI();

        txtTyping.setText(getString(R.string.strGroupInfo));

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbar.setNavigationOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                onBackPressed();
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        currentUserId = firebaseUser.getUid();

        final Intent intent = getIntent();
        groups = (Groups) intent.getSerializableExtra(EXTRA_OBJ_GROUP);
        groupId = groups.getId();
        groupName = groups.getGroupName();
        strGroups = new Gson().toJson(groups);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference(REF_GROUP_PHOTO_UPLOAD + SLASH + groupId);
        storageAttachment = storage.getReference(REF_GROUP_ATTACHMENT + SLASH + groupId);

        reference = FirebaseDatabase.getInstance().getReference(REF_USERS).child(currentUserId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    strUsername = user.getUsername();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Admin Message
        Utils.setHTMLMessage(lblOnlyAdminMsg, getString(R.string.only_admin_can_send_messages));

        mRecyclerView.setHasFixedSize(false);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        layoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(layoutManager);
        btnGoToBottom.setVisibility(View.GONE);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (firstVisible == -1)
                    firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition();

                if (layoutManager.findLastVisibleItemPosition() < firstVisible) {
                    btnGoToBottom.setVisibility(View.VISIBLE);
                } else {
                    btnGoToBottom.setVisibility(View.GONE);
                }
            }
        });

        btnGoToBottom.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                try {
                    if (firstVisible != -1) {
                        mRecyclerView.smoothScrollToPosition(messageAdapters.getItemCount() - 1);
                    }
                } catch (Exception ignored) {

                }
            }
        });

        rlChatView.setVisibility(View.GONE);
        recordButton.setVisibility(View.GONE);
        rlAdminMsgView.setVisibility(View.GONE);

        final Query reference = FirebaseDatabase.getInstance().getReference(REF_GROUPS).child(groupId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    try {
                        groups = dataSnapshot.getValue(Groups.class);
                        assert groups != null;
                        if (groups.getId().equalsIgnoreCase(groupId) && groups.isActive()) {

                            if (groups.getAdmin().equalsIgnoreCase(currentUserId)) {
                                rlChatView.setVisibility(View.VISIBLE);
                                recordButton.setVisibility(View.VISIBLE);
                                rlAdminMsgView.setVisibility(View.GONE);

                            } else {
                                if (groups.getSendMessageSetting() == SETTING_ALL_PARTICIPANTS) {
                                    rlChatView.setVisibility(View.VISIBLE);
                                    recordButton.setVisibility(View.VISIBLE);
                                    rlAdminMsgView.setVisibility(View.GONE);
                                } else {
                                    rlChatView.setVisibility(View.GONE);
                                    recordButton.setVisibility(View.GONE);
                                    rlAdminMsgView.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Utils.getErrors(e);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        readGroupTitle();

        readMessages(groupId);

        final LinearLayout viewProfile = findViewById(R.id.viewProfile);
        viewProfile.setOnClickListener(new SingleClickListener() {
            @Override
            public void onClickView(View v) {
                screens.openGroupParticipantActivity(groups);
            }
        });

        emojiIcon = EmojiPopup.Builder.fromRootView(rootView).setOnEmojiPopupShownListener(() -> {
            hideAttachmentView();
            imgAttachmentEmoji.setImageResource(R.drawable.ic_keyboard_24dp);
        }).setOnEmojiPopupDismissListener(() -> imgAttachmentEmoji.setImageResource(R.drawable.ic_insert_emoticon_gray)).setKeyboardAnimationStyle(R.style.emoji_fade_animation_style).build(newMessage);

        newMessage.setOnTouchListener((v, event) -> {
            hideAttachmentView();
            return false;
        });

        registerUserUpdates();

        final Handler handler = new Handler(Looper.getMainLooper());
        //This permission required because when you playing the recorded your voice, at that time audio wave effect shown.
        handler.postDelayed(this::permissionRecording, 800);

    }

    private void initUI() {
        mImageView = findViewById(R.id.imageView);
        txtTyping = findViewById(R.id.txtTyping);
        txtGroupName = findViewById(R.id.txtGroupName);
        mToolbar = findViewById(R.id.toolbar);
        mRecyclerView = findViewById(R.id.recyclerView);

        //Admin Message
        rlChatView = findViewById(R.id.rlChatView);
        rlAdminMsgView = findViewById(R.id.rlAdminMsgView);
        lblOnlyAdminMsg = findViewById(R.id.lblOnlyAdminMsg);

        //New
        rootView = findViewById(R.id.rootView);
        btnGoToBottom = findViewById(R.id.btnBottom);
        newMessage = findViewById(R.id.newMessage);
        imgAddAttachment = findViewById(R.id.imgAddAttachment);
        imgCamera = findViewById(R.id.imgCamera);
        mainAttachmentLayout = findViewById(R.id.mainAttachmentLayout);
        mainAttachmentLayout.setVisibility(View.GONE);
        attachmentBGView = findViewById(R.id.attachmentBGView);
        attachmentBGView.setVisibility(View.GONE);
        attachmentBGView.setOnClickListener(this);

        imgAttachmentEmoji = findViewById(R.id.imgAttachmentEmoji);

        imgAddAttachment.setOnClickListener(this);
        imgCamera.setOnClickListener(this);
        imgAttachmentEmoji.setOnClickListener(this);
        findViewById(R.id.btnAttachmentVideo).setOnClickListener(this);
        findViewById(R.id.btnAttachmentContact).setOnClickListener(this);
        findViewById(R.id.btnAttachmentGallery).setOnClickListener(this);
        findViewById(R.id.btnAttachmentAudio).setOnClickListener(this);
        findViewById(R.id.btnAttachmentLocation).setOnClickListener(this);
        findViewById(R.id.btnAttachmentDocument).setOnClickListener(this);

        recordView = findViewById(R.id.recordView);
        recordButton = findViewById(R.id.recordButton);
        recordButton.setRecordView(recordView);//IMPORTANT

        initListener();

        pickerManager = new PickerManager(this, this, this);
    }

    private void initListener() {
        //ListenForRecord must be false ,otherwise onClick will not be called
        recordButton.setOnRecordClickListener(v -> clickToSend());

        //Cancel Bounds is when the Slide To Cancel text gets before the timer . default is 8
        final boolean isRTLOn = SessionManager.get().isRTLOn();
        recordView.setRTLDirection(isRTLOn);
        recordView.setSlideMarginRight(recordView.getSlideMargin());
        recordView.setCancelBounds(8);
        recordView.setSlideFont(Utils.getRegularFont(mActivity));
        recordView.setCounterTimerFont(Utils.getBoldFont(mActivity));
        //prevent recording under one Second
        recordView.setLessThanSecondAllowed(false);
        recordView.setSoundEnabled(true);
//        recordView.setTimeLimit(60000);//1000 = 1 second
        recordView.setTrashIconColor(getResources().getColor(R.color.red_500));

        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                hideAttachmentView();
                if (newMessage.getText().toString().trim().isEmpty()) {
                    if (recordWaitHandler == null)
                        recordWaitHandler = new Handler(Looper.getMainLooper());
                    recordRunnable = () -> recordingStart();
                    recordWaitHandler.postDelayed(recordRunnable, ONE);
                }
                hideEditTextLayout();
            }

            @Override
            public void onCancel() {
                if (mRecorder != null && Utils.isEmpty(newMessage.getText().toString().trim())) {
                    recordingStop(FALSE);
                    screens.showToast(R.string.recording_cancelled);
                }
            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
//                final String time = Utils.getHumanTimeText(recordTime);
                try {
                    Utils.sout("Recording Finish: " + recordTime + " >> " + limitReached);
                    if (recordWaitHandler != null && newMessage.getText().toString().trim().isEmpty())
                        recordWaitHandler.removeCallbacks(recordRunnable);
                    if (mRecorder != null && newMessage.getText().toString().trim().isEmpty()) {
                        recordingStop(TRUE);
                    }
                } catch (Exception e) {
                    Utils.getErrors(e);
                }
                showEditTextLayout();
            }

            @Override
            public void onLessThanSecond() {
                showEditTextLayout();
            }
        });

        recordView.setRecordPermissionHandler(() -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return true;
            }
            if (recordPermissionsAvailable()) {
                return true;
            } else {
                permissionRecording();
            }
            return false;

        });

        recordView.setOnBasketAnimationEndListener(this::showEditTextLayout);
    }

    private void showEditTextLayout() {
        if (isStart) {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                imgAttachmentEmoji.setVisibility(View.VISIBLE);
                newMessage.setVisibility(View.VISIBLE);
                imgAddAttachment.setVisibility(View.VISIBLE);
                imgCamera.setVisibility(View.VISIBLE);
            }, 10);
        }
        isStart = false;
    }

    private void hideEditTextLayout() {
        isStart = true;
        imgAttachmentEmoji.setVisibility(View.GONE);
        newMessage.setVisibility(View.INVISIBLE);
        imgAddAttachment.setVisibility(View.GONE);
        imgCamera.setVisibility(View.GONE);
    }

    private void registerUserUpdates() {
        //Publish logged in user's typing status
        newMessage.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == ZERO) {
                    recordButton.setListenForRecord(true);
                    recordButton.setImageResource(R.drawable.recv_ic_mic_white);
                } else {
                    recordButton.setListenForRecord(false);
                    recordButton.setImageResource(R.drawable.ic_send);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void clickToSend() {
        final String txtMessage = newMessage.getText().toString().trim();
        if (TextUtils.isEmpty(txtMessage)) {
            screens.showToast(R.string.strEmptyMsg);
        } else {
            sendMessage(TYPE_TEXT, txtMessage, null);
        }
        newMessage.setText("");
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        if (id == R.id.recordButton) {
            hideAttachmentView();
            clickToSend();
        } else if (id == R.id.imgAttachmentEmoji) {
            emojiIcon.toggle();
        } else if (id == R.id.imgAddAttachment) {
            fileUri = null;
            imgUri = null;
            Utils.closeKeyboard(mActivity, view);
            if (mainAttachmentLayout.getVisibility() == View.VISIBLE) {
                hideAttachmentView();
            } else {
                showAttachmentView();
            }
        } else if (id == R.id.imgCamera) {
            fileUri = null;
            imgUri = null;
            hideAttachmentView();
            openCamera();
        } else if (id == R.id.btnAttachmentGallery) {
            hideAttachmentView();
            openImage();
        } else if (id == R.id.btnAttachmentAudio) {
            hideAttachmentView();
            openAudioPicker();
        } else if (id == R.id.btnAttachmentLocation) {
            hideAttachmentView();
            openPlacePicker();
        } else if (id == R.id.btnAttachmentVideo) {
            hideAttachmentView();
            openVideoPicker();
        } else if (id == R.id.btnAttachmentDocument) {
            hideAttachmentView();
            openDocumentPicker();
        } else if (id == R.id.btnAttachmentContact) {
            hideAttachmentView();
            openContactPicker();
        } else if (id == R.id.attachmentBGView) {
            hideAttachmentView();
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

    private void openAudioPicker() {
        if (permissionsAvailable(permissionsStorage)) {
            Intent target = FileUtils.getAudioIntent();
            Intent intent = Intent.createChooser(target, getString(R.string.choose_file));
            try {
                pickerLauncher.launch(intent);
            } catch (Exception ignored) {
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, PERMISSION_AUDIO);
        }
    }

    private void openVideoPicker() {
        if (permissionsAvailable(permissionsStorage)) {
            Intent target = FileUtils.getVideoIntent();
            Intent intent = Intent.createChooser(target, getString(R.string.choose_file));
            try {
                pickerLauncher.launch(intent);
            } catch (Exception ignored) {
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, PERMISSION_VIDEO);
        }
    }

    public void openDocumentPicker() {
        if (permissionsAvailable(permissionsStorage)) {
            final Intent target = FileUtils.getDocumentIntent();
            final Intent intent = Intent.createChooser(target, getString(R.string.choose_file));
            try {
                pickerLauncher.launch(intent);
            } catch (ActivityNotFoundException ignored) {
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, PERMISSION_DOCUMENT);
        }
    }

    private void openContactPicker() {
        if (permissionsAvailable(permissionsContact)) {
            new MultiContactPicker.Builder(mActivity) //Activity/fragment context
                    .setTitleText(getString(R.string.choose_contact))
                    .setChoiceMode(MultiContactPicker.CHOICE_MODE_SINGLE) //Optional - default: CHOICE_MODE_MULTIPLE
                    .handleColor(ContextCompat.getColor(mActivity, R.color.colorPrimary)) //Optional - default: Azure Blue
                    .bubbleColor(ContextCompat.getColor(mActivity, R.color.colorPrimary)) //Optional - default: Azure Blue
                    .setLoadingType(MultiContactPicker.LOAD_ASYNC) //Optional - default LOAD_ASYNC (wait till all loaded vs stream results)
                    .limitToColumn(LimitColumn.PHONE) //Optional - default NONE (Include phone + email, limiting to one can improve loading time)
                    .setActivityAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out) //Optional - default: No animation overrides
                    .showPickerForResult(REQUEST_CODE_CONTACT);
        } else {
            ActivityCompat.requestPermissions(this, permissionsContact, PERMISSION_CONTACT);
        }
    }

    private void openPlacePicker() {
        if (!Utils.isGPSEnabled(mActivity)) {
            screens.openGPSSettingScreen();
        } else {
            PingPlacePicker.IntentBuilder builder = new PingPlacePicker.IntentBuilder();
            builder.setAndroidApiKey(getString(R.string.key_android))
                    .setMapsApiKey(getString(R.string.key_maps));
            try {
                Intent placeIntent = builder.build(mActivity);
                placeLauncher.launch(placeIntent);
            } catch (Exception ex) {
                // Google Play services is not available...
                Utils.getErrors(ex);
                try {
                    GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
                    googleApiAvailability.showErrorDialogFragment(this, googleApiAvailability.isGooglePlayServicesAvailable(this), REQUEST_CODE_PLAY_SERVICES);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CONTACT:
                if (permissionsAvailable(permissions))
                    openContactPicker();
                break;
            case PERMISSION_AUDIO:
                if (permissionsAvailable(permissions))
                    openAudioPicker();
                break;
            case PERMISSION_DOCUMENT:
                if (permissionsAvailable(permissions))
                    openDocumentPicker();
                break;
            case PERMISSION_VIDEO:
                if (permissionsAvailable(permissions))
                    openVideoPicker();
                break;
            case REQUEST_PERMISSION_RECORD:
                if (permissionsAvailable(permissions)) {
                    try {
                        if (messageAdapters != null)
                            messageAdapters.notifyDataSetChanged();
                    } catch (Exception ignored) {

                    }
                }
                break;
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

                try {
                    CropImage.activity(imgUri)
                            .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                            .setCropShape(CropImageView.CropShape.RECTANGLE)
                            .setFixAspectRatio(true)
                            .start(mActivity);
                } catch (Exception e) {
                    Utils.getErrors(e);
                }
            }
        }
    });

    final ActivityResultLauncher<Intent> pickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                // There are no request codes
                final Intent data = result.getData();
                assert data != null;
                final Uri uriData = data.getData();
                Utils.sout("PickerManager uri: " + uriData.toString());
                pickerManager.getPath(uriData, Build.VERSION.SDK_INT); /* {@link PickerManagerOnCompleteListener }*/
            }
        }
    });

    final ActivityResultLauncher<Intent> placeLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            try {
                final Intent data = result.getData();
                assert data != null;
                final Place place = PingPlacePicker.getPlace(data);
                assert place != null;
                final String name = Utils.isEmpty(place.getName()) ? "" : place.getName();
                final LocationAddress locationAddress = new LocationAddress(name, place.getAddress(), place.getLatLng().latitude, place.getLatLng().longitude);
                Attachment attachment = new Attachment();
                attachment.setData(new Gson().toJson(locationAddress));
                attachment.setFileName(name);
                attachment.setName(name);
                sendMessage(AttachmentTypes.getTypeName(AttachmentTypes.LOCATION), place.getAddress(), attachment);
            } catch (Exception e) {
                Utils.getErrors(e);
            }
        }
    });

    final ActivityResultLauncher<Intent> editGroupLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    final Intent data = result.getData();
                    assert data != null;
                    groups = (Groups) data.getSerializableExtra(EXTRA_OBJ_GROUP);
                    refreshGroupsData();
                } catch (Exception e) {
                    Utils.getErrors(e);
                }
            }
        }
    });

    private void readGroupTitle() {
        groupName = groups.getGroupName();
        txtGroupName.setText(groupName);
        Utils.setGroupImage(mActivity, groups.getGroupImg(), mImageView);
    }

    private void sendMessage(String type, String message, Attachment attachment) {
        notify = true;
        String defaultMsg = message;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        final String date = Utils.getDateTime();
        hashMap.put(EXTRA_SENDER, currentUserId);
        hashMap.put(EXTRA_RECEIVER, groupId);
        hashMap.put(EXTRA_MESSAGE, message);
        hashMap.put(EXTRA_ATTACH_TYPE, type);
        hashMap.put(EXTRA_TYPE, TYPE_TEXT);//This is for older version users(Default TEXT, all other set as IMAGE)

        try {
            if (!type.equalsIgnoreCase(TYPE_TEXT) && !type.equalsIgnoreCase(TYPE_IMAGE)) {
                defaultMsg = Utils.getDefaultMessage();
                hashMap.put(EXTRA_MESSAGE, defaultMsg);
            }
        } catch (Exception ignored) {
        }

        try {
            if (type.equalsIgnoreCase(TYPE_TEXT)) {
                Utils.sout(EMPTY);
                //No need to do anything here.
            } else if (type.equalsIgnoreCase(TYPE_IMAGE)) {
                hashMap.put(EXTRA_IMGPATH, message);
            } else {
                hashMap.put(EXTRA_ATTACH_PATH, message);
                try {
                    if (attachment != null) {
                        hashMap.put(EXTRA_ATTACH_NAME, attachment.getName());
                        hashMap.put(EXTRA_ATTACH_FILE, attachment.getFileName());
                        hashMap.put(EXTRA_ATTACH_SIZE, attachment.getBytesCount());
                        if (attachment.getData() != null) {
                            hashMap.put(EXTRA_ATTACH_DATA, attachment.getData());
                        }
                        if (attachment.getDuration() != null) {
                            hashMap.put(EXTRA_ATTACH_DURATION, attachment.getDuration());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }

        hashMap.put(EXTRA_SEEN, FALSE);
        hashMap.put(EXTRA_DATETIME, date);

        reference.child(REF_GROUPS_MESSAGES + SLASH + groupId).push().setValue(hashMap);

        HashMap<String, Object> mapGroupLastMsg = new HashMap<>();
        mapGroupLastMsg.put(EXTRA_LAST_MSG, defaultMsg);
        mapGroupLastMsg.put(EXTRA_TYPE, type);
        mapGroupLastMsg.put(EXTRA_LAST_TIME, date);

        reference.child(REF_GROUPS + SLASH + groupId).updateChildren(mapGroupLastMsg);

        Utils.chatSendSound(getApplicationContext());

        try {
            String msg = message;
            if (!type.equalsIgnoreCase(TYPE_TEXT) && !type.equalsIgnoreCase(TYPE_IMAGE)) {
                try {
                    String firstCapital = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
                    if (attachment != null) {
                        msg = "New " + firstCapital + "(" + attachment.getName() + ")";
                    } else {
                        msg = firstCapital;
                    }
                } catch (Exception e) {
                    msg = message;
                }
            }

//            final List<String> memList = groups.getMembers();
            final List<String> memList = new ArrayList<>(groups.getMembers());

            memList.remove(currentUserId);//Remove current Logged in ID from list, cause no need to send notification to us.
            if (notify) {
                for (int i = 0; i < memList.size(); i++) {

                    final String memReceiver = memList.get(i);

                    sendNotification(memReceiver, strUsername, msg, type);
                }
                notify = false;
            }
        } catch (Exception ignored) {
        }

    }

    private void sendNotification(final String receiver, final String username, final String message, final String type) {
        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference(REF_TOKENS);
        Query query = tokenRef.orderByKey().equalTo(receiver);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Token token = snapshot.getValue(Token.class);

                        final Data data = new Data(currentUserId, R.drawable.ic_stat_ic_notification, username, message, getString(R.string.strNewGroupMessage), receiver, strGroups, type);

                        assert token != null;
                        final Sender sender = new Sender(data, token.getToken());

                        apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(@NotNull Call<MyResponse> call, @NotNull Response<MyResponse> response) {
                                assert response.code() != 200 || response.body() != null;
                            }

                            @Override
                            public void onFailure(@NotNull Call<MyResponse> call, @NotNull Throwable t) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void readMessages(final String groupId) {
        chats = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference(REF_GROUPS_MESSAGES + SLASH + groupId);
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chats.clear();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            Chat chat = snapshot.getValue(Chat.class);
                            assert chat != null;
                            if (!Utils.isEmpty(chat.getMessage())) {
                                chats.add(chat);
                            }

                        } catch (Exception ignored) {
                        }

                    }
                }
                readUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readUsers() {
        userList = new HashMap<>();
        memberList = groups.getMembers();

        Query query = FirebaseDatabase.getInstance().getReference(REF_USERS);
        query.keepSynced(true);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                if (dataSnapshot.hasChildren()) {
                    for (int i = 0; i < memberList.size(); i++) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            final User user = snapshot.getValue(User.class);
                            assert user != null;
                            if (memberList.get(i).equalsIgnoreCase(user.getId())) {
                                userList.put(user.getId(), user);
                                break;
                            }
                        }
                    }
                }
                try {
                    messageAdapters = new GroupsMessageAdapters(mActivity, chats, userList);
                    mRecyclerView.setAdapter(messageAdapters);
                } catch (Exception e) {
                    Utils.getErrors(e);
                }
                readGroupMembers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void readGroupMembers() {
        userList = Utils.sortByUser(userList, FALSE);
        StringBuilder u = new StringBuilder();
        for (User user : userList.values()) {
            if (!user.getId().equalsIgnoreCase(currentUserId)) {
                u.append(user.getUsername()).append(", ");
            }
        }
        txtTyping.setText(u.append(getString(R.string.strYou)));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_groups, menu);
        MenuItem item = menu.findItem(R.id.itemAddGroup);
        item.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.itemGroupInfo) {
            screens.openGroupParticipantActivity(groups);
        } else if (itemId == R.id.itemClearMyChats) {
            Utils.showYesNoDialog(mActivity, R.string.strDelete, R.string.strDeleteOwnChats, () -> {
                showProgress();
                deleteOwnChats(FALSE);//False means don't close current screen, just delete my own chats
            });
        } else if (itemId == R.id.itemEditGroup) {
            if (isAdmin()) {
                final Intent intent = new Intent(mActivity, GroupsAddActivity.class);
                intent.putExtra(EXTRA_GROUP_ID, groupId);
                intent.putExtra(EXTRA_OBJ_GROUP, groups);
                editGroupLauncher.launch(intent);
            } else {
                screens.showToast(R.string.msgOnlyAdminEdit);
            }
        } else if (itemId == R.id.itemLeaveGroup) {
            Utils.showYesNoDialog(mActivity, R.string.strLeave, R.string.strLeaveFromGroup, () -> {
                showProgress();
                if (isAdmin()) {

                    if (groups.getMembers().size() >= TWO) {//Make other Person to Admin for this group cause more than 2 person available.

                        String newAdminId = groups.getMembers().get(1);//Make Admin to next USER.

                        groups.setAdmin(newAdminId);

                        groups.getMembers().remove(currentUserId);

                        leaveFromGroup(TRUE);//True means close current screen, cause first we leave from group and than delete own chats

                    } else {//You are alone in this Groups. So Delete group and its DATA.

                        deleteWholeGroupsData(); // In this case only groups have Single User and can delete whole groups data.

                    }

                } else {

                    List<String> removeId = groups.getMembers();
                    removeId.remove(currentUserId);
                    groups.setMembers(removeId);

                    leaveFromGroup(TRUE);//True means close current screen, cause first we leave from group and than delete own chats

                }
            });
        } else if (itemId == R.id.itemDeleteGroup) {
            if (isAdmin()) {
                Utils.showYesNoDialog(mActivity, R.string.strDelete, R.string.strDeleteWholeGroup, () -> {
                    showProgress();
                    deleteWholeGroupsData();// Delete whole /groups, /messagesGroups and /membersJoined for that groups
                });
            } else {
                screens.showToast(R.string.msgOnlyAdminDelete);
            }
        }
        return true;
    }

    private void leaveFromGroup(final boolean isFinishActivity) {
        //Remove from Main Group info /groupId/members/<removeId>
        FirebaseDatabase.getInstance().getReference().child(REF_GROUPS_S + groupId).setValue(groups).addOnCompleteListener(task -> {

            //Remove from MembersGroup/groupsIn/<groupId>
            FirebaseDatabase.getInstance().getReference().child(REF_GROUP_MEMBERS_S + currentUserId + EXTRA_GROUPS_IN_BOTH + groupId).removeValue()
                    .addOnCompleteListener(task1 -> {
                        hideProgress();
                        deleteOwnChats(isFinishActivity);//True means close current screen, cause first we leave from group and than delete own chats
                    });
        }).addOnFailureListener(e -> hideProgress());
    }

    private void deleteWholeGroupsData() {
        final int members = groups.getMembers().size();
        FirebaseDatabase.getInstance().getReference().child(REF_GROUPS_S + groupId).removeValue().addOnCompleteListener(task -> {
            for (int i = 0; i < members; i++) {
                FirebaseDatabase.getInstance().getReference().child(REF_GROUP_MEMBERS_S + groups.getMembers().get(i) + EXTRA_GROUPS_IN_BOTH + groupId).removeValue().addOnCompleteListener(task12 -> {

                });
                if (i == (members - 1)) {
                    hideProgress();
                    finish();
                }
            }
            FirebaseDatabase.getInstance().getReference().child(REF_GROUPS_MESSAGES + SLASH + groupId).removeValue().addOnCompleteListener(task1 -> {

            });
        });
    }

    /**
     * False means don't close current screen, just delete my own chats
     * True  means close current screen, cause first we leave from group and than delete own chats
     */
    private void deleteOwnChats(final boolean isFinishActivity) {
        firstVisible = -1;
        FirebaseDatabase.getInstance().getReference().child(REF_GROUPS_MESSAGES + SLASH + groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Chat chat = snapshot.getValue(Chat.class);
                            assert chat != null;
                            if (chat.getSender().equalsIgnoreCase(currentUserId)) {
                                Utils.deleteUploadedFilesFromCloud(storage, chat);
                                snapshot.getRef().removeValue();
                            }
                        }
                    }

                } catch (Exception ignored) {
                }
                hideProgress();
                if (isFinishActivity) {
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private File myImageFile = null;

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(mActivity);
        pd.setMessage(getString(R.string.msg_image_upload));
        pd.show();

        if (imageUri != null) {

            if (Utils.isAboveQ()) {
                myImageFile = getExternalFilesDir(null);
            } else {
                myImageFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + IMG_FOLDER);
            }

            final StorageReference fileReference = storageReference.child(currentUserId + "_" + System.currentTimeMillis() + "." + Utils.getExtension(mActivity, imageUri));
            uploadTask = fileReference.putFile(imageUri);

            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return fileReference.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()) {
                    final Uri downloadUri = task.getResult();
                    final String mUrl = downloadUri.toString();
                    if (!Utils.isEmpty(groupId)) {
                        sendMessage(TYPE_IMAGE, mUrl, null);
                    }
                    Utils.deleteRecursive(myImageFile);
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

    private void uploadThumbnail(final String filePath) {
        if (mainAttachmentLayout.getVisibility() == View.VISIBLE) {
            mainAttachmentLayout.setVisibility(View.GONE);
            attachmentBGView.setVisibility(View.GONE);
            imgAddAttachment.animate().setDuration(400).rotationBy(-45).start();
        }

        final ProgressDialog pd = new ProgressDialog(mActivity);
        pd.setMessage(getString(R.string.msg_image_upload));
        pd.show();

        final File file = new File(filePath);
        final StorageReference storageReference = storageAttachment.child(AttachmentTypes.getTypeName(AttachmentTypes.VIDEO) + SLASH + REF_VIDEO_THUMBS).child(currentUserId + "_" + file.getName() + ".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            //If thumbnail exists
            pd.dismiss();
            final Attachment attachment = new Attachment();
            attachment.setData(uri.toString());
            myFileUploadTask(filePath, AttachmentTypes.VIDEO, attachment);
            Utils.deleteRecursive(Utils.getCacheFolder(mActivity));
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                final BaseTask baseTask = new BaseTask() {
                    @Override
                    public void setUiForLoading() {
                        super.setUiForLoading();
                    }

                    @Override
                    public Object call() {
                        return ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
                    }

                    @Override
                    public void setDataAfterLoading(Object result) {
                        final Bitmap bitmap = (Bitmap) result;
                        if (bitmap != null) {
                            //Upload thumbnail and then upload video
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();
                            UploadTask uploadTask = storageReference.putBytes(data);
                            uploadTask.continueWithTask(task -> {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                // Continue with the task to get the download URL
                                return storageReference.getDownloadUrl();
                            }).addOnCompleteListener(task -> {
                                pd.dismiss();
                                if (task.isSuccessful()) {
                                    final Uri downloadUri = task.getResult();
                                    final Attachment attachment = new Attachment();
                                    attachment.setData(downloadUri.toString());
                                    myFileUploadTask(filePath, AttachmentTypes.VIDEO, attachment);
                                } else {
                                    myFileUploadTask(filePath, AttachmentTypes.VIDEO, null);
                                }
                                Utils.deleteRecursive(Utils.getCacheFolder(mActivity));
                            }).addOnFailureListener(e1 -> {
                                pd.dismiss();
                                myFileUploadTask(filePath, AttachmentTypes.VIDEO, null);
                                Utils.deleteRecursive(Utils.getCacheFolder(mActivity));
                            });
                        } else {
                            pd.dismiss();
                            myFileUploadTask(filePath, AttachmentTypes.VIDEO, null);
                            Utils.deleteRecursive(Utils.getCacheFolder(mActivity));
                        }
                    }

                };

                final TaskRunner thumbnailTask = new TaskRunner();
                thumbnailTask.executeAsync(baseTask);
            }
        });
    }

    private void myFileUploadTask(String filePath, @AttachmentTypes.AttachmentType final int attachmentType, final Attachment attachment) {
        Utils.sout("MYFile Upload " + attachmentType + " >> " + filePath);
        hideAttachmentView();

        final ProgressDialog pd = new ProgressDialog(mActivity);
        pd.setMessage(getString(R.string.msg_image_upload));
        pd.setCancelable(false);
        pd.show();

        final File mFileUpload = new File(filePath);
        final String fileName = Utils.getUniqueFileName(mFileUpload, attachmentType);
        final File fileToUpload = new File(Utils.moveFileToFolder(mActivity, true, fileName, mFileUpload, attachmentType).toString(), fileName);

        final StorageReference storageReference = storageAttachment.child(AttachmentTypes.getTypeName(attachmentType)).child(currentUserId + "_" + fileName);
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            //If file is already uploaded
            Attachment myAttachment = null;
            try {
                myAttachment = attachment;
                if (myAttachment == null) myAttachment = new Attachment();
                if (attachmentType == AttachmentTypes.CONTACT) {
                    Utils.sout(EMPTY);
                } else {
                    myAttachment.setName(fileToUpload.getName());
                    myAttachment.setFileName(fileName);
                    myAttachment.setDuration(Utils.getVideoDuration(mActivity, fileToUpload));
                }
                myAttachment.setUrl(uri.toString());
                myAttachment.setBytesCount(fileToUpload.length());
            } catch (Exception ignored) {
            }
            sendMessage(AttachmentTypes.getTypeName(attachmentType), uri.toString(), myAttachment);
            pd.dismiss();
            //Utils.deleteRecursive(new File(dir));
            Utils.deleteRecursive(Utils.getCacheFolder(mActivity));
        }).addOnFailureListener(exception -> {
            //Else upload and then send message
            FirebaseUploader firebaseUploader = new FirebaseUploader(storageReference, new FirebaseUploader.UploadListener() {
                @Override
                public void onUploadFail(String message) {
                    Utils.sout("onUploadFail::: " + message);
                    pd.dismiss();
                }

                @Override
                public void onUploadSuccess(String downloadUrl) {
                    Attachment myAttachment = null;
                    try {
                        myAttachment = attachment;
                        if (myAttachment == null) myAttachment = new Attachment();
                        if (attachmentType == AttachmentTypes.CONTACT) {
                            Utils.sout(EMPTY);
                        } else {
                            myAttachment.setName(mFileUpload.getName());
                            myAttachment.setFileName(fileName); // fileToUpload.getName()
                            try {
                                myAttachment.setDuration(Utils.getVideoDuration(mActivity, fileToUpload));
                            } catch (Exception e) {
                                Utils.getErrors(e);
                            }
                        }
                        myAttachment.setUrl(downloadUrl);
                        myAttachment.setBytesCount(fileToUpload.length());
                    } catch (Exception e) {
                        Utils.getErrors(e);
                    }
                    sendMessage(AttachmentTypes.getTypeName(attachmentType), downloadUrl, myAttachment);
                    pd.dismiss();
                    try {
                        Utils.deleteRecursive(Utils.getCacheFolder(mActivity));
                    } catch (Exception e) {
                        Utils.getErrors(e);
                    }
                }

                @Override
                public void onUploadProgress(int progress) {
                    try {
                        pd.setMessage("Uploading " + progress + "%...");
                    } catch (Exception ignored) {
                    }
                }

                @Override
                public void onUploadCancelled() {
                    pd.dismiss();
                }
            });
            firebaseUploader.uploadFile(fileToUpload);
        });
    }

    private void getSendVCard(final List<ContactResult> results) {
        try {
            displayName = results.get(0).getDisplayName();
            phoneNumber = results.get(0).getPhoneNumbers().get(0).getNumber();
        } catch (Exception e) {
            Utils.getErrors(e);
        }

        final BaseTask baseTask = new BaseTask() {
            @Override
            public void setUiForLoading() {
                super.setUiForLoading();
            }

            @Override
            public Object call() {
                Cursor cursor = Utils.contactsCursor(mActivity, phoneNumber);
                File toSend = Utils.getSentDirectory(mActivity, TYPE_CONTACT);//Looks like this : AppName/Contact/.sent/
                if (cursor != null && !cursor.isClosed()) {
                    cursor.getCount();
                    if (cursor.moveToFirst()) {
                        @SuppressLint("Range") String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
                        try {
                            AssetFileDescriptor assetFileDescriptor = getContentResolver().openAssetFileDescriptor(uri, "r");
                            if (assetFileDescriptor != null) {
                                FileInputStream inputStream = assetFileDescriptor.createInputStream();
                                boolean dirExists = toSend.exists();
                                if (!dirExists)
                                    dirExists = toSend.mkdirs();
                                if (dirExists) {
                                    try {
                                        toSend = Utils.getSentFile(toSend, EXT_VCF);
                                        boolean fileExists = toSend.exists();
                                        if (!fileExists)
                                            fileExists = toSend.createNewFile();
                                        if (fileExists) {
                                            OutputStream stream = new BufferedOutputStream(new FileOutputStream(toSend, false));
                                            byte[] buffer = Utils.readAsByteArray(inputStream);
                                            vCardData = new String(buffer);
                                            stream.write(buffer);
                                            stream.close();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Utils.getErrors(e);
                        } finally {
                            cursor.close();
                        }
                    }
                }
                return toSend;
            }

            @Override
            public void setDataAfterLoading(Object result) {
                final File f = (File) result;
                if (f != null && !TextUtils.isEmpty(vCardData)) {
                    Attachment attachment = new Attachment();
                    attachment.setData(vCardData);
                    try {
                        attachment.setName(displayName);
                        attachment.setFileName(displayName);
                        attachment.setDuration(phoneNumber);
                    } catch (Exception ignored) {
                    }
                    myFileUploadTask(f.getAbsolutePath(), AttachmentTypes.CONTACT, attachment);
                }
            }
        };

        TaskRunner taskRunner = new TaskRunner();
        taskRunner.executeAsync(baseTask);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PARTICIPATE && resultCode == RESULT_FIRST_USER && data != null) {
            finish();
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                imageUri = result.getUri();
                if (uploadTask != null && uploadTask.isInProgress()) {
                    screens.showToast(R.string.msgUploadInProgress);
                } else {
                    uploadImage();
                }
            } else
                assert resultCode != CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE || result != null;
        }

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CONTACT:
                    try {
                        assert data != null;
                        List<ContactResult> results = MultiContactPicker.obtainResult(data);
                        getSendVCard(results);
                    } catch (Exception e) {
                        Utils.getErrors(e);
                    }
                    break;

                case REQUEST_CODE_PLAY_SERVICES:
                    openPlacePicker();
            }
        }
    }

    private void refreshGroupsData() {
        readGroupTitle();
        readUsers();
    }

    private boolean isAdmin() {
        if (groups.getAdmin().equalsIgnoreCase(currentUserId)) {
            return TRUE;
        }
        return FALSE;
    }

    private void recordingStop(boolean send) {
        try {
            Utils.sout("Recording Stop");
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        } catch (Exception ex) {
            mRecorder = null;
            Utils.getErrors(ex);
        }
        recordTimerStop();
        if (send) {
            myFileUploadTask(recordFilePath, AttachmentTypes.RECORDING, null);
        } else {
            try {
                new File(recordFilePath).delete();
            } catch (Exception e) {
                Utils.getErrors(e);
            }
        }
    }

    private void permissionRecording() {
        if (!recordPermissionsAvailable()) {
            ActivityCompat.requestPermissions(mActivity, permissionsRecord, REQUEST_PERMISSION_RECORD);
        }
    }

    private void recordingStart() {
        if (recordPermissionsAvailable()) {
            File recordFile = Utils.getSentDirectory(mActivity, TYPE_RECORDING);//Looks like this : AppName/RECORDING/Sent/
            boolean dirExists = recordFile.exists();
            if (!dirExists)
                dirExists = recordFile.mkdirs();

            if (dirExists) {
                try {
                    recordFile = Utils.getSentFile(getCacheDir(), EXT_MP3);
                    if (!recordFile.exists())
                        recordFile.createNewFile();
                    try {
                        final FileOutputStream fos = new FileOutputStream(recordFile);
                        fos.write(new byte[1024]);
                        fos.close();
                    } catch (Exception e) {
                        Utils.getErrors(e);
                    }
                    recordFilePath = recordFile.getAbsolutePath();
                    Utils.sout("RecordingStart Path: " + recordFile.exists() + ">>" + recordFilePath);
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mRecorder.setOutputFile(recordFilePath);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    mRecorder.prepare();
                    mRecorder.start();
                    recordTimerStart();
                } catch (Exception ex) {
                    Utils.getErrors(ex);
                    mRecorder = null;
                }
            }
        } else {
            permissionRecording();
        }
    }

    private void recordTimerStart() {
        screens.showToast(R.string.recording);
        try {
            recordTimerRunnable = new Runnable() {
                public void run() {
                    recordTimerHandler.postDelayed(this, DELAY_ONE_SEC);
                }
            };
            if (recordTimerHandler == null)
                recordTimerHandler = new Handler(Looper.getMainLooper());

            recordTimerHandler.post(recordTimerRunnable);
        } catch (Exception ignored) {
        }
        Utils.setVibrate(mActivity, VIBRATE_HUNDRED);
    }

    private void recordTimerStop() {
        try {
            recordTimerHandler.removeCallbacks(recordTimerRunnable);
            Utils.setVibrate(mActivity, VIBRATE_HUNDRED);
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

    private boolean recordPermissionsAvailable() {
        boolean available = true;
        for (String permission : permissionsRecord) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PERMISSION_GRANTED) {
                available = false;
                break;
            }
        }
        return available;
    }

    private final ArrayList<Integer> positionList = new ArrayList<>();

    //Download complete listener
    private final BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null)
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                    if (positionList.size() > ZERO && messageAdapters != null) {
                        for (int pos : positionList) {
                            if (pos != -1) {
//                                Uncomment to play recording directly once download completed
//                                But before that please stop the current playing audio if playing
//                                try {
//                                      chats.get(pos).setDownloadProgress(COMPLETED);
//                                } catch (Exception ignored) {
//                                }
                                messageAdapters.notifyItemChanged(pos);
                            }
                        }
                    }
                    positionList.clear();
                }
        }
    };

    public void downloadFile(DownloadFileEvent downloadFileEvent) {
        if (permissionsAvailable(permissionsStorage)) {
            new DownloadUtil().loading(this, downloadFileEvent);
            positionList.add(downloadFileEvent.getPosition());
        } else {
            ActivityCompat.requestPermissions(this, permissionsStorage, 47);
        }
    }

    //Download event listener
    private final BroadcastReceiver downloadEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadFileEvent downloadFileEvent = (DownloadFileEvent) intent.getSerializableExtra(DOWNLOAD_DATA);
            try {
                if (downloadFileEvent != null) {
                    downloadFile(downloadFileEvent);
                }
            } catch (Exception ignored) {
            }
        }
    };

    private void hideAttachmentView() {
        if (mainAttachmentLayout.getVisibility() == View.VISIBLE) {
            mainAttachmentLayout.setVisibility(View.GONE);
            attachmentBGView.setVisibility(View.GONE);
            imgAddAttachment.animate().setDuration(400).rotationBy(-45).start();
        }
    }

    private void showAttachmentView() {
        mainAttachmentLayout.setVisibility(View.VISIBLE);
        attachmentBGView.setVisibility(View.VISIBLE);
        imgAddAttachment.animate().setDuration(400).rotationBy(45).start();
        emojiIcon.dismiss();
    }

    //
    //  PickerManager Listeners
    //
    //  The listeners can be used to display a Dialog when a file is selected from Dropbox/Google Drive or OnDrive.
    //  The listeners are callbacks from an AsyncTask that creates a new File of the original in /storage/emulated/0/Android/data/your.package.name/files/Temp/
    //
    //  PickerManagerOnUriReturned()
    //  When selecting a file from Google Drive, for example, the Uri will be returned before the file is available(if it has not yet been cached/downloaded).
    //  Google Drive will first have to download the file before we have access to it.
    //  This can be used to let the user know that we(the application), are waiting for the file to be returned.
    //
    //  PickerManagerOnStartListener()
    //  This will be call once the file creations starts and will only be called if the selected file is not local
    //
    //  PickerManagerOnProgressUpdate(int progress)
    //  This will return the progress of the file creation (in percentage) and will only be called if the selected file is not local
    //
    //  PickerManagerOnCompleteListener(String path, boolean wasDriveFile)
    //  If the selected file was from Dropbox/Google Drive or OnDrive, then this will be called after the file was created.
    //  If the selected file was a local file then this will be called directly, returning the path as a String
    //  Additionally, a boolean will be returned letting you know if the file selected was from Dropbox/Google Drive or OnDrive.

    private TextView percentText;
    private ProgressBar mProgressBar;
    private AlertDialog mdialog;
    private ProgressDialog progressBar;

    @Override
    public void PickerManagerOnUriReturned() {
        progressBar = new ProgressDialog(this);
        progressBar.setMessage(getString(R.string.msgWaitingForFile));
        progressBar.setCancelable(false);
        progressBar.show();
    }

    @Override
    public void PickerManagerOnStartListener() {
        final Handler mPickHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread. Your worker tells you in the message what to do.
                if (progressBar.isShowing()) {
                    progressBar.cancel();
                }
                final AlertDialog.Builder mPro = new AlertDialog.Builder(new ContextThemeWrapper(mActivity, R.style.myDialog));
                @SuppressLint("InflateParams") final View mPView = LayoutInflater.from(mActivity).inflate(R.layout.dailog_layout, null);
                percentText = mPView.findViewById(R.id.percentText);

                percentText.setOnClickListener(new SingleClickListener() {
                    @Override
                    public void onClickView(View view) {
                        pickerManager.cancelTask();
                        if (mdialog != null && mdialog.isShowing()) {
                            mdialog.cancel();
                        }
                    }
                });

                mProgressBar = mPView.findViewById(R.id.mProgressBar);
                mProgressBar.setMax(100);
                mPro.setView(mPView);
                mdialog = mPro.create();
                mdialog.show();
            }
        };
        mPickHandler.sendEmptyMessage(ZERO);
    }

    @Override
    public void PickerManagerOnProgressUpdate(int progress) {
        try {
            Handler mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message message) {
                    final String progressPlusPercent = progress + "%";
                    percentText.setText(progressPlusPercent);
                    mProgressBar.setProgress(progress);
                }
            };
            mHandler.sendEmptyMessage(ZERO);
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

    //REQUEST_PICK_AUDIO, REQUEST_PICK_VIDEO, REQUEST_PICK_DOCUMENT
    @Override
    public void PickerManagerOnCompleteListener(String path, boolean wasDriveFile, boolean wasUnknownProvider, boolean wasSuccessful, String reason) {
        if (mdialog != null && mdialog.isShowing()) {
            mdialog.cancel();
        }
        Utils.sout("Picker Path :: " + new File(path).exists() + " >> " + path + " :drive: " + wasDriveFile + " :<Success>: " + wasSuccessful);

        int fileType = 0;
        try {
            fileType = MediaFile.getFileType(path).fileType;
        } catch (Exception e) {
            //Utils.getErrors(e);
        }

        if (wasSuccessful) {
            final int file_size = Integer.parseInt(String.valueOf(new File(path).length() / 1024));

            if (MediaFile.isAudioFileType(fileType)) {
                if (file_size > Utils.getAudioSizeLimit()) {
                    screens.showToast(String.format(getString(R.string.msgFileTooBig), Utils.MAX_SIZE_AUDIO));
                } else {
                    myFileUploadTask(path, AttachmentTypes.AUDIO, null);
                }
            } else if (MediaFile.isVideoFileType(fileType)) {
                if (file_size > Utils.getVideoSizeLimit()) {
                    screens.showToast(String.format(getString(R.string.msgFileTooBig), Utils.MAX_SIZE_VIDEO));
                } else {
                    uploadThumbnail(Uri.parse(path).getPath());
                }
            } else {
                if (file_size > Utils.getDocumentSizeLimit()) {
                    screens.showToast(String.format(getString(R.string.msgFileTooBig), Utils.MAX_SIZE_DOCUMENT));
                } else {
                    myFileUploadTask(path, AttachmentTypes.DOCUMENT, null);
                }
            }

        } else {
            screens.showToast(R.string.msgChooseFileFromOtherLocation);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            registerReceiver(downloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            LocalBroadcastManager.getInstance(this).registerReceiver(downloadEventReceiver, new IntentFilter(BROADCAST_DOWNLOAD_EVENT));
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(downloadCompleteReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadEventReceiver);
        } catch (Exception ignored) {
        }
        try {
            if (messageAdapters != null) {
                messageAdapters.stopAudioFile();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onBackPressed() {
        try {
            pickerManager.deleteTemporaryFile(this);
        } catch (Exception ignored) {
        }
        if (mainAttachmentLayout.getVisibility() == View.VISIBLE) {
            hideAttachmentView();
        } else {
            finish();
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (!isChangingConfigurations()) {
                pickerManager.deleteTemporaryFile(this);
            }
        } catch (Exception ignored) {
        }
    }

}
