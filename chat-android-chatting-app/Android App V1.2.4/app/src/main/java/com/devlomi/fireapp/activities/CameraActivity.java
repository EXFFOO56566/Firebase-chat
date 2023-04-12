package com.devlomi.fireapp.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Toast;

import com.cjt2325.cameralibrary.JCameraView;
import com.cjt2325.cameralibrary.ResultCodes;
import com.cjt2325.cameralibrary.listener.ClickListener;
import com.cjt2325.cameralibrary.listener.ErrorListener;
import com.cjt2325.cameralibrary.listener.JCameraListener;
import com.cjt2325.cameralibrary.listener.RecordStartListener;
import com.cjt2325.cameralibrary.util.DeviceUtil;
import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.constants.MessageType;
import com.devlomi.fireapp.utils.BitmapUtils;
import com.devlomi.fireapp.utils.DirManager;
import com.devlomi.fireapp.utils.IntentUtils;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;

import me.zhanghai.android.systemuihelper.SystemUiHelper;

public class CameraActivity extends AppCompatActivity {
    //max image selectable(when choosing from gallery)
    public static final int MAX_IMAGE_SELECTABLE = 5;
    //max video selectable(when choosing from gallery)
    public static final int MAX_VIDEO_SELECTABLE = 1;
    public static final int REQUEST_CODE_PICK_FROM_GALLERY = 2323;
    private JCameraView jCameraView;
    private Chronometer chronometer;
    SystemUiHelper uiHelper;
    //if the user opens the camera for adding new status we will save the image in the received images folder
    private boolean isStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        chronometer = findViewById(R.id.chronometer);
        uiHelper = new SystemUiHelper(this, SystemUiHelper.LEVEL_IMMERSIVE, SystemUiHelper.FLAG_IMMERSIVE_STICKY);


        //if the user opens the camera for adding new status we will save the image in the received images folder
        isStatus = getIntent().hasExtra(IntentUtils.IS_STATUS);


        jCameraView = findViewById(R.id.jcameraview);
        //if it's status we will save the video in received video folder,otherwise we will save it in sent video folder
        jCameraView.setSaveVideoPath(DirManager.generateFile(isStatus ? MessageType.RECEIVED_VIDEO : MessageType.SENT_VIDEO).getPath());
        jCameraView.setFeatures(JCameraView.BUTTON_STATE_BOTH);
        jCameraView.setTip(getString(R.string.camera_tip));

        //show pickImage from gallery button if needed
        if (getIntent().hasExtra(IntentUtils.CAMERA_VIEW_SHOW_PICK_IMAGE_BUTTON))
            jCameraView.showPickImageButton();

        //set media quality
        jCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);
        jCameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                Log.i("CJT", "camera error");
                Intent intent = new Intent();
                setResult(ResultCodes.CAMERA_ERROR_STATE, intent);
                finish();
            }

            @Override
            public void AudioPermissionError() {
                Toast.makeText(CameraActivity.this, R.string.audio_permission_error, Toast.LENGTH_SHORT).show();
            }
        });

        jCameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {

                //if the user opens the camera for adding new status we will save the image in the received images folder
                File outputFile = DirManager.generateFile(isStatus ? MessageType.RECEIVED_IMAGE : MessageType.SENT_IMAGE);

                BitmapUtils.convertBitmapToJpeg(bitmap, outputFile);

                String path = outputFile.getPath();
                Intent intent = new Intent();
                intent.putExtra(IntentUtils.EXTRA_PATH_RESULT, path);
                setResult(ResultCodes.IMAGE_CAPTURE_SUCCESS, intent);
                finish();
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {

                Intent intent = new Intent();
                intent.putExtra(IntentUtils.EXTRA_PATH_RESULT, url);
                setResult(ResultCodes.VIDEO_RECORD_SUCCESS, intent);
                finish();
            }

            @Override
            public void quit() {

            }
        });

        jCameraView.setRecordStartListener(new RecordStartListener() {
            @Override
            public void onStart() {
                chronometer.setBase(SystemClock.currentThreadTimeMillis());
                chronometer.start();
            }

            @Override
            public void onStop() {
                chronometer.stop();
            }
        });

        jCameraView.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                CameraActivity.this.finish();
            }
        });

        jCameraView.setPickImageListener(new ClickListener() {
            @Override
            public void onClick() {
                pickImages();
            }
        });

        Log.i("CJT", DeviceUtil.getDeviceModel());
    }

    private void pickImages() {
        Matisse.from(CameraActivity.this)
                .choose(MimeType.of(MimeType.MP4, MimeType.THREEGPP, MimeType.THREEGPP2
                        , MimeType.JPEG, MimeType.BMP, MimeType.PNG))
                .countable(true)
                .maxSelectablePerMediaType(MAX_IMAGE_SELECTABLE, MAX_VIDEO_SELECTABLE)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(REQUEST_CODE_PICK_FROM_GALLERY);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //hiding system bars
        uiHelper.hide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        jCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        jCameraView.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FROM_GALLERY && resultCode == RESULT_OK) {
            setResult(ResultCodes.PICK_IMAGE_FROM_CAMERA, data);
            finish();
        }
    }
}