package com.devlomi.fireapp.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.devlomi.fireapp.activities.CallingActivity;
import com.devlomi.fireapp.model.constants.FireCallType;
import com.devlomi.fireapp.model.realms.FireCall;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.AudioHelper;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.MyApp;
import com.devlomi.fireapp.utils.NotificationHelper;
import com.devlomi.fireapp.utils.ProximitySensor;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.utils.RingtonePlayer;
import com.devlomi.fireapp.utils.SharedPreferencesManager;
import com.devlomi.fireapp.utils.SinchConfig;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.NotificationResult;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallDetails;
import com.sinch.android.rtc.calling.CallDirection;
import com.sinch.android.rtc.calling.CallNotificationResult;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;
import com.sinch.android.rtc.video.VideoScalingType;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallingService extends Service implements VideoCallListener, ProximitySensor.Delegate, AudioManager.OnAudioFocusChangeListener {


    private boolean isCallActivityVisible = false;
    private boolean isSpeakerEnabled = false;
    private boolean isCallActive = false;


    static final String TAG = CallingService.class.getSimpleName();

    private SinchServiceInterface mSinchServiceInterface = new SinchServiceInterface();
    private SinchClient mSinchClient;
    private SinchCallClientListener callClientListener;

    private StartFailedListener mListener;

    ProximitySensor proximitySensor;
    AudioManager audioManager;
    RingtonePlayer ringtonePlayer;
    private int notificationId = -1;


    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        proximitySensor = new ProximitySensor(this, this);
        ringtonePlayer = new RingtonePlayer(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.hasExtra(IntentUtils.START_SINCH)){
                start();
            }
            else if (intent.hasExtra(IntentUtils.CALL_ACTION_TYPE) && mSinchClient != null){
                int action = intent.getIntExtra(IntentUtils.CALL_ACTION_TYPE, -1);
                String callId = intent.getStringExtra(IntentUtils.CALL_ID);

                Call call = mSinchClient.getCallClient().getCall(callId);

                if (call != null) {
                    if (action == IntentUtils.NOTIFICATION_ACTION_ANSWER || action == IntentUtils.NOTIFICATION_ACTION_CLICK) {
                        Intent mIntent = new Intent(CallingService.this, CallingActivity.class);
                        int type = call.getDirection() == CallDirection.OUTGOING ? FireCallType.OUTGOING : FireCallType.INCOMING;
                        mIntent.putExtra(IntentUtils.PHONE_CALL_TYPE, type);
                        mIntent.putExtra(IntentUtils.CALL_ID, callId);
                        mIntent.putExtra(IntentUtils.UID, call.getRemoteUserId());
                        mIntent.putExtra(IntentUtils.PHONE, call.getHeaders().get("phoneNumber"));
                        mIntent.putExtra(IntentUtils.ISVIDEO, call.getDetails().isVideoOffered());
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        CallingService.this.startActivity(mIntent);

                        if (action == IntentUtils.NOTIFICATION_ACTION_ANSWER)
                            call.answer();
                    } else if (action == IntentUtils.NOTIFICATION_ACTION_DECLINE)
                        call.hangup();
                }
            }
        }
        return START_STICKY;
    }


    private void createClient() {
        mSinchClient = SinchConfig.getSinchClient(this);

        mSinchClient.setSupportCalling(true);
        mSinchClient.setSupportManagedPush(true);
        mSinchClient.getVideoController().setResizeBehaviour(VideoScalingType.ASPECT_FILL);


        mSinchClient.addSinchClientListener(new MySinchClientListener());
        mSinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());

    }

    @Override
    public void onDestroy() {
        stop();
        super.onDestroy();
    }

    private void start() {

        if (mSinchClient == null) {
            createClient();
        }

        if (!mSinchClient.isStarted()) {
            mSinchClient.start();

        }

    }

    private void stop() {
        setCallActive(false);

        if (ringtonePlayer != null)
            ringtonePlayer.stopRingtone();

        if (audioManager != null) {
            setBluetoothHeadset(false);
            audioManager.abandonAudioFocus(this);
        }
        if (mSinchClient != null) {
            mSinchClient.terminateGracefully();
            mSinchClient = null;
        }
    }

    private void setCallActive(boolean mCallActive) {
        isCallActive = mCallActive;
        MyApp.setCallActive(mCallActive);

    }

    private boolean isStarted() {
        return (mSinchClient != null && mSinchClient.isStarted());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mSinchServiceInterface;
    }

    @Override
    public void onVideoTrackAdded(Call call) {

    }

    @Override
    public void onVideoTrackPaused(Call call) {

    }

    @Override
    public void onVideoTrackResumed(Call call) {

    }

    @Override
    public void onCallProgressing(Call call) {


        if (call.getDirection() == CallDirection.OUTGOING) {
            setCallActive(true);

            int streamType = AudioManager.STREAM_VOICE_CALL;

            String uid = call.getRemoteUserId();
            User user = RealmHelper.getInstance().getUser(uid);
            String phoneNumber = call.getHeaders().get("phoneNumber");
            Log.d("PhoneNumber", "onCallProgressing: " + phoneNumber);

            long startedTime = call.getDetails().getStartedTime();
            FireCall fireCall = new FireCall(call.getCallId(), user, FireCallType.OUTGOING, startedTime, phoneNumber, call.getDetails().isVideoOffered());

            Notification activeCallNotification = new NotificationHelper(this).createActiveCallNotification(user, phoneNumber, call.getCallId(), call.getDetails().isVideoOffered(), getNotificationId());
            startForeground(getNotificationId(), activeCallNotification);
            RealmHelper.getInstance().saveObjectToRealm(fireCall);

            requestAudioFocus(streamType);
            if (ringtonePlayer != null)
                ringtonePlayer.playProgressTone();

        }
    }

    private void requestAudioFocus(int streamType) {
        audioManager.requestAudioFocus(CallingService.this, streamType, AudioManager.AUDIOFOCUS_GAIN);
    }


    @Override
    public void onCallEstablished(Call call) {
        if (ringtonePlayer != null)
            ringtonePlayer.stopRingtone();


        //divert sound to bluetooh headset if available
        if (AudioHelper.isBluetoothHeadsetOn(audioManager))
            setBluetoothHeadset(true);

        setCallActive(true);


        if (call.getDirection() == CallDirection.INCOMING) {
            RealmHelper.getInstance().setCallAsAnswered(call.getCallId());
            String uid = call.getRemoteUserId();
            User user = RealmHelper.getInstance().getUser(uid);
            boolean videoOffered = call.getDetails().isVideoOffered();
            String phoneNumber = call.getHeaders().get("phoneNumber");
            Log.d("PhoneNumber", "onCallEstablished: " + phoneNumber);
            NotificationHelper notificationHelper = new NotificationHelper(this);
            Notification activeCallNotification = notificationHelper.createActiveCallNotification(user, phoneNumber, call.getCallId(), videoOffered, getNotificationId());
            notificationHelper.notifyNotification(getNotificationId(), activeCallNotification);
        }


    }

    private void setBluetoothHeadset(boolean enable) {
        if (enable) {
            audioManager.setMode(0);
            audioManager.setBluetoothScoOn(true);
            audioManager.startBluetoothSco();
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        } else {
            audioManager.setBluetoothScoOn(false);
            audioManager.stopBluetoothSco();
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
    }

    @Override
    public void onCallEnded(Call call) {
        if (ringtonePlayer != null)
            ringtonePlayer.stopRingtone();

        setBluetoothHeadset(false);
        stopListenForSensor();
        CallDetails details = call.getDetails();
        RealmHelper.getInstance().updateCallInfoOnCallEnded(call.getCallId(), details.getDuration());
        new NotificationHelper(CallingService.this).cancelIncomingCallNotification();
        notificationId = -1;
        stopForeground(true);

        //if the call was cancelled save it to firebase database , so the other user does not notified about it
        if (call.getDirection() == CallDirection.OUTGOING && call.getDetails().getDuration() == 0) {
            FireManager.setCallCancelled(call.getRemoteUserId(), call.getCallId());
        }

        setCallActive(false);
        audioManager.abandonAudioFocus(this);

    }


    @Override
    public void onShouldSendPushNotification(Call call, List<PushPair> list) {

    }

    @Override
    public void onProximitySensorNear() {
        if (isCallActivityVisible)
            proximitySensor.acquire();

    }

    @Override
    public void onProximitySensorFar() {
        proximitySensor.release();
    }

    @Override
    public void onAudioFocusChange(int i) {
    }

    public class SinchServiceInterface extends Binder {

        public Call callUserVideo(String userId) {
            String currentUserPhoneNumber = SharedPreferencesManager.getPhoneNumber();
            HashMap<String, String> callMap = new HashMap<>();
            callMap.put("phoneNumber", currentUserPhoneNumber);
            callMap.put("timestamp", String.valueOf(new Date().getTime()));
            Call call = mSinchClient.getCallClient().callUserVideo(userId, callMap);
            call.addCallListener(CallingService.this);
            return call;
        }

        public Call callUserVoice(String userId) {
            String currentUserPhoneNumber = SharedPreferencesManager.getPhoneNumber();
            HashMap<String, String> callMap = new HashMap<>();
            callMap.put("phoneNumber", currentUserPhoneNumber);
            callMap.put("timestamp", String.valueOf(new Date().getTime()));
            Call call = mSinchClient.getCallClient().callUser(userId, callMap);
            call.addCallListener(CallingService.this);
            return call;
        }


        public void setActivityVisible(boolean visible) {
            CallingService.this.isCallActivityVisible = visible;
            if (visible) {
                startListenForSensor();
            } else {
                stopListenForSensor();
            }
        }

        public void setSpeakerEnabled(boolean isEnabled) {
            CallingService.this.isSpeakerEnabled = isEnabled;
            if (isEnabled) {
                stopListenForSensor();
            } else {
                startListenForSensor();
            }
        }


        public boolean isStarted() {
            return CallingService.this.isStarted();
        }

        public boolean isCallActive() {
            return CallingService.this.isCallActive;
        }

        public Call getCall(String callId) {
            if (mSinchClient != null)
                return mSinchClient.getCallClient().getCall(callId);
            return null;
        }

        public VideoController getVideoController() {
            if (!isStarted()) {
                return null;
            }
            return mSinchClient.getVideoController();
        }

        public AudioController getAudioController() {
            if (!isStarted()) {
                return null;
            }
            return mSinchClient.getAudioController();
        }

        public NotificationResult relayRemotePushNotificationPayload(final Map payload) {

            if (mSinchClient == null) {
                createClient();
            } else if (mSinchClient == null) {
                Log.e(TAG, "Can't start a SinchClient as no username is available, unable to relay push.");
                return null;
            }
            NotificationResult notificationResult = mSinchClient.relayRemotePushNotificationPayload(payload);
            if (notificationResult.isCall() && notificationResult.isValid()) {
                CallNotificationResult callResult = notificationResult.getCallResult();

                if (!callResult.isCallCanceled()) {
                    if (callClientListener == null) {
                        callClientListener = new SinchCallClientListener();
                        mSinchClient.getCallClient().addCallClientListener(callClientListener);
                    }
                } else {
                    if (callClientListener != null) {
                        mSinchClient.getCallClient().removeCallClientListener(callClientListener);
                        callClientListener = null;
                    }

                }


            }
            return notificationResult;
        }

        public void startClient(StartFailedListener startFailedListener) {
            mListener = startFailedListener;
            start();
        }
    }


    private void stopListenForSensor() {
        proximitySensor.stopListenForSensor();
        proximitySensor.release();
    }

    private void startListenForSensor() {
        if (!AudioHelper.isHeadsetOn(audioManager) && !isSpeakerEnabled)
            proximitySensor.listenForSensor();
    }

    public interface StartFailedListener {

        void onStartFailed(SinchError error);

        void onStarted();
    }

    private class MySinchClientListener implements SinchClientListener {

        @Override
        public void onClientFailed(SinchClient client, SinchError error) {
            if (mListener != null) {
                mListener.onStartFailed(error);
            }
            mSinchClient.terminate();
            mSinchClient = null;
        }

        @Override
        public void onClientStarted(SinchClient client) {
            if (mListener != null) {
                mListener.onStarted();

                if (!SharedPreferencesManager.isSinchConfigured())
                    SharedPreferencesManager.setSinchConfigured(true);
            }
        }

        @Override
        public void onClientStopped(SinchClient client) {

        }

        @Override
        public void onLogMessage(int level, String area, String message) {
            switch (level) {
                case Log.DEBUG:
                    Log.d(area, message);
                    break;
                case Log.ERROR:
                    Log.e(area, message);
                    break;
                case Log.INFO:
                    Log.i(area, message);
                    break;
                case Log.VERBOSE:
                    Log.v(area, message);
                    break;
                case Log.WARN:
                    Log.w(area, message);
                    break;
            }
        }

        @Override
        public void onRegistrationCredentialsRequired(SinchClient client,
                                                      ClientRegistration clientRegistration) {
        }
    }

    private class SinchCallClientListener implements CallClientListener {

        @Override
        public void onIncomingCall(CallClient callClient, final Call call) {
            //check if call was cancelled before starting the calling activity
            FireManager.isCallCancelled(call.getRemoteUserId(), call.getCallId(), new FireManager.IsCallCancelled() {
                @Override
                public void isCancelled(boolean isCancelled) {
                    if (isCancelled) return;
                    if (call == null) return;
                    if (RealmHelper.getInstance().isCallCancelled(call.getCallId())) return;
                    if (MyApp.isPhoneCallActivityVisible()) return;

                    setCallActive(true);

                    FireCall fireCall = RealmHelper.getInstance().getFireCall(call.getCallId());
                    String phoneNumber = fireCall == null ? "" : fireCall.getPhoneNumber();
                    Log.d("PhoneNumber", "onIncomingCall: " + phoneNumber);
                    CallDetails details = call.getDetails();

                    String uid = call.getRemoteUserId();
                    User user = RealmHelper.getInstance().getUser(uid);
                    boolean videoOffered = details.isVideoOffered();
                    String callId = call.getCallId();
                    Intent intent = new Intent(CallingService.this, CallingActivity.class);
                    intent.putExtra(IntentUtils.PHONE_CALL_TYPE, FireCallType.INCOMING);
                    intent.putExtra(IntentUtils.CALL_ID, callId);
                    intent.putExtra(IntentUtils.UID, uid);
                    intent.putExtra(IntentUtils.PHONE, phoneNumber);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    CallingService.this.startActivity(intent);
                    call.addCallListener(CallingService.this);
                    Notification incomingCallNotification = new NotificationHelper(CallingService.this).createIncomingCallNotification(user, phoneNumber, callId, videoOffered, getNotificationId());
                    startForeground(getNotificationId(), incomingCallNotification);
                    if (ringtonePlayer != null && audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                        requestAudioFocus(AudioManager.STREAM_VOICE_CALL);
                        ringtonePlayer.playIncomingRingtone();
                    }
                }
            });


        }


    }

    private int getNotificationId() {
        if (notificationId == -1)
            notificationId = NotificationHelper.generateId();

        return notificationId;
    }


}
