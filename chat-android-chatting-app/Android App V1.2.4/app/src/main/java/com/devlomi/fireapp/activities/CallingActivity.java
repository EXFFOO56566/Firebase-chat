package com.devlomi.fireapp.activities;

import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.constants.FireCallType;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.services.CallingService;
import com.devlomi.fireapp.utils.BitmapUtils;
import com.devlomi.fireapp.utils.DpUtil;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.MyApp;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.utils.Util;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CallingActivity extends AppCompatActivity implements CallingService.StartFailedListener, ServiceConnection {

    public static final int LOCAL_VIEW_WIDTH = 100;
    public static final int LOCAL_VIEW_HEIGHT = 150;
    private ImageView imgUser;
    private TextView tvUsername;
    private TextView tvStatus;
    private TextView tvCallType;
    private FloatingActionButton btnAnswer;
    private FloatingActionButton btnReject;
    private FloatingActionButton btnHangup;
    private ImageButton btnSpeaker;
    private ImageButton btnMic;
    private ImageButton btnVideo;
    private ConstraintLayout constraint;
    private ImageButton btnFlipCamera;
    private ImageView bottomHolder;


    static final String ADDED_LISTENER = "addedListener";


    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;

    private boolean mAddedListener = false;
    private boolean mLocalVideoViewAdded = false;
    private boolean mRemoteVideoViewAdded = false;
    private boolean isSpeakerEnabled = false;
    private boolean isMicMuted = false;
    private boolean isLocalVideoEnabled = true;
    private boolean isRemoteVideoEnabled = true;
    private boolean isTimerAdded = false;
    private boolean isVideo;

    private int type;
    private View localView;


    private FireManager.OnGetUserPhoto onGetUserPhoto;
    private User user;
    private String uid;
    private String phoneNumber;
    private String mCallId;
    private CallingService.SinchServiceInterface mSinchServiceInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenOnFlags();
        setContentView(R.layout.activity_phone_call);


        initViews();
        bindService();


        uid = getIntent().getStringExtra(IntentUtils.UID);
        phoneNumber = getIntent().getStringExtra(IntentUtils.PHONE);
        type = getIntent().getIntExtra(IntentUtils.PHONE_CALL_TYPE, -1);
        isVideo = getIntent().getBooleanExtra(IntentUtils.ISVIDEO, false);
        mCallId = getIntent().getStringExtra(IntentUtils.CALL_ID);


        //set initial type text
        String callTypeText = String.format(getString(R.string.call_type), getString(R.string.app_name));
        tvCallType.setText(callTypeText);
        user = RealmHelper.getInstance().getUser(uid);


        if (user != null) {
            tvUsername.setText(user.getUserName());
            tvStatus.setText(R.string.connecting);
            //load the full user image if it's exists
            if (user.getUserLocalPhoto() != null) {
                Glide.with(this).load(user.getUserLocalPhoto()).into(imgUser);
                //otherwise load the thumbImg
            } else {
                Glide.with(this).load(BitmapUtils.encodeImageAsBytes(user.getThumbImg())).asBitmap().into(imgUser);
            }


        } else {
            //if the user is not exists in local database we will set the name as phoneNumber
            if (phoneNumber != null)
                tvUsername.setText(phoneNumber);

            //fetch the user info and save it
            FireManager.fetchUserByUid(this, uid, new FireManager.IsHasAppListener() {
                @Override
                public void onFound(User user) {
                    //update call object when fetching user
                    RealmHelper.getInstance().updateUserObjectForCall(uid, mCallId);

                    tvUsername.setText(user.getUserName());
                }

                @Override
                public void onNotFound() {

                }
            });

        }


        hideOrShowButtons(type == FireCallType.OUTGOING);


        btnAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answerCall();
            }
        });


        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endCall();
            }
        });


        btnHangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endCall();
            }
        });

        btnSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSinchServiceInterface != null) {
                    if (isSpeakerEnabled) {
                        disableSpeaker();
                    } else {
                        enableSpeaker();
                    }
                }
            }
        });

        btnMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSinchServiceInterface != null) {
                    if (isMicMuted) {
                        setIconBg(view, false);
                        mSinchServiceInterface.getAudioController().unmute();
                        isMicMuted = false;
                    } else {
                        setIconBg(view, true);
                        mSinchServiceInterface.getAudioController().mute();
                        isMicMuted = true;
                    }
                }
            }
        });


        btnFlipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSinchServiceInterface != null) {
                    mSinchServiceInterface.getVideoController().toggleCaptureDevicePosition();
                }
            }
        });

        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSinchServiceInterface == null) return;

                if (isLocalVideoEnabled) {
                    pauseVideo(view);
                } else {
                    enableVideo(view);
                    enableSpeaker();
                }
            }
        });


        //hide or show bottom view with the buttons
        constraint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSinchServiceInterface == null) return;
                Call call = mSinchServiceInterface.getCall(mCallId);
                if (call == null) return;
                if (call.getState() != CallState.ESTABLISHED) return;
                if (call.getDetails().isVideoOffered()) {
                    if (bottomHolder.getVisibility() == View.VISIBLE) {
                        btnVideo.setVisibility(View.GONE);
                        btnMic.setVisibility(View.GONE);
                        bottomHolder.setVisibility(View.GONE);
                        btnHangup.setVisibility(View.GONE);
                        btnFlipCamera.setVisibility(View.GONE);
                        btnSpeaker.setVisibility(View.GONE);
                    } else {

                        btnVideo.setVisibility(View.VISIBLE);
                        btnMic.setVisibility(View.VISIBLE);
                        bottomHolder.setVisibility(View.VISIBLE);
                        btnHangup.setVisibility(View.VISIBLE);
                        btnFlipCamera.setVisibility(View.VISIBLE);
                        btnSpeaker.setVisibility(View.VISIBLE);

                    }
                }
            }
        });


        onGetUserPhoto = new FireManager.OnGetUserPhoto() {
            @Override
            public void onGetPhoto(String photoPath) {
                Glide.with(CallingActivity.this).load(photoPath).into(imgUser);
            }

            @Override
            public void onGetThumb(String thumbImg) {

            }
        };

        //fetch the remote user's photo
        if (user != null)
            FireManager.checkAndDownloadUserPhoto(user, onGetUserPhoto);

    }


    private void disableSpeaker() {
        setIconBg(btnSpeaker, false);
        mSinchServiceInterface.getAudioController().disableSpeaker();
        isSpeakerEnabled = false;
        mSinchServiceInterface.setSpeakerEnabled(isSpeakerEnabled);
    }

    private void enableSpeaker() {
        setIconBg(btnSpeaker, true);
        mSinchServiceInterface.getAudioController().enableSpeaker();
        isSpeakerEnabled = true;
        mSinchServiceInterface.setSpeakerEnabled(isSpeakerEnabled);
    }

    private void pauseVideo(View view) {
        mSinchServiceInterface.getCall(mCallId).pauseVideo();
        setIconBg(view, false);
        isLocalVideoEnabled = false;
        removeLocalView();
        btnFlipCamera.setVisibility(View.GONE);
        btnSpeaker.setVisibility(View.VISIBLE);
    }

    private void enableVideo(View view) {
        mSinchServiceInterface.getCall(mCallId).resumeVideo();
        setIconBg(view, true);
        isLocalVideoEnabled = true;
        addLocalView();
        btnSpeaker.setVisibility(View.INVISIBLE);
        btnFlipCamera.setVisibility(View.VISIBLE);
    }


    private void setVideoStuff() {
        if (mSinchServiceInterface == null) {
            return;
        }

        if (mSinchServiceInterface.getCall(mCallId) == null) return;


        boolean videoOffered = mSinchServiceInterface.getCall(mCallId).getDetails().isVideoOffered();

        if (!videoOffered) {
            btnVideo.setVisibility(View.GONE);
            btnFlipCamera.setVisibility(View.GONE);
            String fireAppVoiceCall = String.format(getString(R.string.fireapp_voice_call), getString(R.string.app_name));
            tvCallType.setText(fireAppVoiceCall);

        } else {
            enableSpeaker();
            btnSpeaker.setVisibility(View.INVISIBLE);
            String fireAppVideoCall = String.format(getString(R.string.fireapp_video_call), getString(R.string.app_name));
            tvCallType.setText(fireAppVideoCall);
            setIconBg(btnVideo, true);
        }
    }


    //hide or show buttons depending on call direction (incoming,outgoing)
    private void hideOrShowButtons(boolean showHangup) {
        if (showHangup) {
            btnReject.setVisibility(View.GONE);
            btnAnswer.setVisibility(View.GONE);
            btnHangup.setVisibility(View.VISIBLE);
        } else {
            btnReject.setVisibility(View.VISIBLE);
            btnAnswer.setVisibility(View.VISIBLE);
            btnHangup.setVisibility(View.GONE);
        }

    }


    private void answerCall() {
        Call call = mSinchServiceInterface.getCall(mCallId);
        if (call != null) {
            call.answer();
        }
    }


    private void endCall() {
        Call call = mSinchServiceInterface.getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private void initViews() {
        imgUser = findViewById(R.id.img_user);
        tvUsername = findViewById(R.id.tv_username);
        tvStatus = findViewById(R.id.tv_status);
        btnHangup = findViewById(R.id.btn_hangup_in_call);
        btnSpeaker = findViewById(R.id.btn_speaker);
        btnMic = findViewById(R.id.btn_mic);
        btnVideo = findViewById(R.id.btn_video);
        btnAnswer = findViewById(R.id.btn_answer);
        btnReject = findViewById(R.id.btn_reject);
        constraint = findViewById(R.id.constraint);
        tvCallType = findViewById(R.id.tv_call_type);
        btnFlipCamera = findViewById(R.id.btn_flip_camera);
        bottomHolder = findViewById(R.id.bottom_holder);
    }

    @Override
    public void onStartFailed(SinchError error) {
    }

    @Override
    public void onStarted() {

        if (mSinchServiceInterface.isCallActive()) return;

        Call call;
        if (isVideo)
            call = mSinchServiceInterface.callUserVideo(uid);
        else
            call = mSinchServiceInterface.callUserVoice(uid);

        mCallId = call.getCallId();


        if (call != null) {
            if (!mAddedListener) {
                call.addCallListener(new SinchCallListener());
                mAddedListener = true;
            }
        } else {
            finish();
        }
        setVideoStuff();
        updateUI();
    }


    private class SinchCallListener implements VideoCallListener {
        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            tvStatus.setText(getCallEndCauseString(cause));

            //revert sound control back to normal
            setVolumeControlStream(AudioManager.STREAM_SYSTEM);

            //finish activity after 2 seconds
            // to make the user see what the reason of call termination
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    CallingActivity.this.finish();
                }
            }, 2000);

        }

        @Override
        public void onCallEstablished(Call call) {
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            hideOrShowButtons(true);
            scheduleTimer();
            updateUI();
        }

        @Override
        public void onCallProgressing(Call call) {
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
        }

        //this will called when remote user opens camera
        @Override
        public void onVideoTrackAdded(Call call) {
            isRemoteVideoEnabled = true;
        }

        //this will called when remote user resumes the camera after pause
        @Override
        public void onVideoTrackResumed(Call call) {
            if (call.getDetails().isVideoOffered() && call.getState() == CallState.ESTABLISHED)
                addRemoteView();

            isRemoteVideoEnabled = true;

        }

        //this will called when remote user pauses camera
        @Override
        public void onVideoTrackPaused(Call call) {
            if (call.getState() == CallState.ESTABLISHED)
                removeRemoteView();

            isRemoteVideoEnabled = false;

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(ADDED_LISTENER, mAddedListener);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mAddedListener = savedInstanceState.getBoolean(ADDED_LISTENER);
    }

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            CallingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }


    //we are using onWindowFocusChanged because on Samsung Devices the onPause will called when we will acquire the PowerLock
    //which will cause a Flicker
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mSinchServiceInterface == null) return;
        if (hasFocus)
            mSinchServiceInterface.setActivityVisible(true);
        else
            mSinchServiceInterface.setActivityVisible(false);


    }

    @Override
    public void onStop() {
        super.onStop();
        MyApp.phoneCallActivityPaused();
        mDurationTask.cancel();
        mTimer.cancel();
        isTimerAdded = false;
        removeVideoViews();

    }

    @Override
    public void onStart() {
        super.onStart();
        MyApp.phoneCallActivityResumed();

        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        if (mSinchServiceInterface != null) {
            Call call = mSinchServiceInterface.getCall(mCallId);
            if (call != null && call.getState() == CallState.ESTABLISHED && !isTimerAdded)
                scheduleTimer();
        }
        updateUI();


    }

    private void scheduleTimer() {
        try {
            mTimer.schedule(mDurationTask, 0, 500);
            isTimerAdded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updateCallDuration() {
        Call call = mSinchServiceInterface.getCall(mCallId);
        if (call != null) {
            tvStatus.setText(Util.formatCallTime(call.getDetails().getDuration()));
        }
    }

    private void addLocalView() {
        if (mLocalVideoViewAdded || mSinchServiceInterface == null) {
            return; //early
        }


        final VideoController vc = mSinchServiceInterface.getVideoController();
        if (vc != null) {
            ConstraintSet set = new ConstraintSet();
            localView = vc.getLocalView();
            int lvId = localView.generateViewId();
            localView.setId(lvId);


            //if the local view has no parent(not added to the layout yet) then add it
            if (localView.getParent() == null) {
                constraint.addView(localView);

                localView.bringToFront();
                int localViewWidth = (int) DpUtil.toPixel(LOCAL_VIEW_WIDTH, this);
                int localViewHeight = (int) DpUtil.toPixel(LOCAL_VIEW_HEIGHT, this);
                localView.getLayoutParams().width = localViewWidth;
                localView.getLayoutParams().height = localViewHeight;
                set.clone(constraint);

                //update video views positions
                if (isLocalVideoEnabled || !mRemoteVideoViewAdded) {
                    setConstraintToBelowImageHolder(set);
                } else {
                    setLocalViewConstraintsToTop(set);
                }

                localView.bringToFront();

                set.applyTo(constraint);
            }


            mLocalVideoViewAdded = true;

        }

    }

    //set localView to below top image (the image that contains the username,call duration etc..)
    private void setConstraintToBelowImageHolder(ConstraintSet set) {
        if (set == null) {
            set = new ConstraintSet();
            set.clone(constraint);
            set.connect(localView.getId(), ConstraintSet.TOP, R.id.imageView3, ConstraintSet.BOTTOM);
            set.connect(localView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            set.applyTo(constraint);
        } else {
            set.connect(localView.getId(), ConstraintSet.TOP, R.id.imageView3, ConstraintSet.BOTTOM);
            set.connect(localView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        }
    }

    //set localView to the top
    private void setLocalViewConstraintsToTop(ConstraintSet set) {
        set.connect(localView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(localView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
    }

    private void addRemoteView() {
        if (mRemoteVideoViewAdded || mSinchServiceInterface == null) {
            return; //early
        }
        final VideoController vc = mSinchServiceInterface.getVideoController();

        if (vc != null) {

            ConstraintSet set = new ConstraintSet();
            View remoteView = vc.getRemoteView();


            int lvId = remoteView.generateViewId();
            remoteView.setId(lvId);


            if (remoteView.getParent() == null) {
                constraint.addView(remoteView);
                set.clone(constraint);
                set.constrainWidth(remoteView.getId(), ConstraintSet.MATCH_CONSTRAINT);
                set.constrainHeight(remoteView.getId(), ConstraintSet.MATCH_CONSTRAINT);

                set.connect(remoteView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                set.connect(remoteView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                set.connect(remoteView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                set.connect(remoteView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            }
            //move the local view to top right
            if (localView != null) {
                setLocalViewConstraintsToTop(set);
                localView.bringToFront();
            }
            set.applyTo(constraint);


            bottomHolder.bringToFront();
            btnMic.bringToFront();
            btnVideo.bringToFront();
            btnFlipCamera.bringToFront();
            btnSpeaker.bringToFront();
            btnVideo.setVisibility(View.VISIBLE);
            btnFlipCamera.setVisibility(View.VISIBLE);
            mRemoteVideoViewAdded = true;
        }
    }


    private void removeVideoViews() {
        if (mSinchServiceInterface == null) {
            return; // early
        }

        VideoController vc = mSinchServiceInterface.getVideoController();
        if (vc != null) {

            if (vc.getRemoteView().getParent() != null)
                constraint.removeView(vc.getRemoteView());
            if (vc.getLocalView().getParent() != null)
                constraint.removeView(vc.getLocalView());

            mLocalVideoViewAdded = false;
            mRemoteVideoViewAdded = false;
        }
    }

    private void removeLocalView() {
        if (mSinchServiceInterface == null) {
            return; // early
        }
        VideoController vc = mSinchServiceInterface.getVideoController();
        if (vc != null) {
            if (vc.getLocalView().getParent() != null)
                constraint.removeView(vc.getLocalView());
        }
        mLocalVideoViewAdded = false;

    }

    private void removeRemoteView() {
        if (mSinchServiceInterface == null) {
            return; // early
        }

        VideoController vc = mSinchServiceInterface.getVideoController();
        if (vc != null) {
            if (vc.getRemoteView().getParent() != null)
                constraint.removeView(vc.getRemoteView());
            mRemoteVideoViewAdded = false;

            setConstraintToBelowImageHolder(null);


        }
    }


    private void updateUI() {
        if (mSinchServiceInterface == null) {
            return; // early
        }

        Call call = mSinchServiceInterface.getCall(mCallId);

        if (call != null) {
            tvStatus.setText(getCallStateString(call.getState()));
            if (call.getDetails().isVideoOffered()) {
                btnSpeaker.setVisibility(View.INVISIBLE);
                btnFlipCamera.setVisibility(View.VISIBLE);
                if (isLocalVideoEnabled)
                    addLocalView();
                if (call.getState() == CallState.ESTABLISHED && isRemoteVideoEnabled) {
                    addRemoteView();
                }
            } else {
                btnSpeaker.setVisibility(View.VISIBLE);
                btnFlipCamera.setVisibility(View.INVISIBLE);
            }
        }
    }

    //this will change button background when it's active
    private void setIconBg(View view, boolean show) {
        if (show)
            view.setBackground(ContextCompat.getDrawable(this, R.drawable.active_icon_bg));
        else
            view.setBackground(null);
    }

    private String getCallStateString(CallState callState) {
        Resources resources = getResources();

        switch (callState) {
            case INITIATING:
            case PROGRESSING:
                return resources.getString(R.string.connecting);

            case ENDED:
                return resources.getString(R.string.call_ended);


            default:
                return "";
        }
    }

    private String getCallEndCauseString(CallEndCause cause) {
        Resources resources = getResources();


        switch (cause) {
            case DENIED:
                return resources.getString(R.string.call_denied);

            case FAILURE:
                return resources.getString(R.string.call_failure);

            case CANCELED:
                return resources.getString(R.string.call_cancelled);

            case HUNG_UP:
                return resources.getString(R.string.user_hungup);

            case NO_ANSWER:
                return resources.getString(R.string.call_no_answer);


            default:
                return "";
        }
    }


    //these flags will make the screen turns on whenever a call has come
    //also it will prevent the screen from auto turn off when the user the is making a call
    private void setScreenOnFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (CallingService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = (CallingService.SinchServiceInterface) iBinder;

            if (type == FireCallType.OUTGOING) {
                if (mSinchServiceInterface.isCallActive()) {
                    if (!mAddedListener) {
                        Call call = mSinchServiceInterface.getCall(mCallId);
                        if (call != null) {
                            call.addCallListener(new SinchCallListener());
                        }
                        mAddedListener = true;
                    }
                } else {
                    if (mSinchServiceInterface.isStarted())
                        onStarted();
                    else
                        mSinchServiceInterface.startClient(this);
                }
            } else {
                Call call = mSinchServiceInterface.getCall(mCallId);
                if (call != null) {

                    if (!mAddedListener) {
                        call.addCallListener(new SinchCallListener());
                        mAddedListener = true;
                    }
                } else {
                    finish();
                }
                setVideoStuff();
                updateUI();
            }

        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        if (CallingService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = null;

        }
    }


    private void bindService() {
        Intent serviceIntent = new Intent(this, CallingService.class);

        getApplicationContext().bindService(serviceIntent, this, BIND_AUTO_CREATE);
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean(ADDED_LISTENER, mAddedListener);
        outState.putString(IntentUtils.CALL_ID, mCallId);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        mAddedListener = savedInstanceState.getBoolean(ADDED_LISTENER);
        mCallId = savedInstanceState.getString(IntentUtils.CALL_ID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onGetUserPhoto = null;
    }


}
