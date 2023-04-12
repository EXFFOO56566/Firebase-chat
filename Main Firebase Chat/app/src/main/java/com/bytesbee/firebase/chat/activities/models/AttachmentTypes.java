package com.bytesbee.firebase.chat.activities.models;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXT_MP3;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_AUDIO;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_CONTACT;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_DOCUMENT;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_IMAGE;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_LOCATION;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_RECORDING;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.TYPE_VIDEO;

import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.IntDef;
import androidx.annotation.RequiresApi;

import com.bytesbee.firebase.chat.activities.managers.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by BytesBee
 */

public class AttachmentTypes {

    public static final int IMAGE = 0;
    public static final int VIDEO = 1;
    public static final int CONTACT = 2;
    public static final int AUDIO = 3;
    public static final int LOCATION = 4;
    public static final int DOCUMENT = 5;
    public static final int RECORDING = 6;
    public static final int NONE_TEXT = 7;
    public static final int NONE_TYPING = 8;

    @IntDef({IMAGE, VIDEO, CONTACT, AUDIO, LOCATION, DOCUMENT, NONE_TEXT, NONE_TYPING, RECORDING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AttachmentType {
    }

    public static String getTypeName(@AttachmentType int attachmentType) {
        switch (attachmentType) {
            case IMAGE:
                return TYPE_IMAGE;
            case AUDIO:
                return TYPE_AUDIO;
            case VIDEO:
                return TYPE_VIDEO;
            case CONTACT:
                return TYPE_CONTACT;
            case DOCUMENT:
                return TYPE_DOCUMENT;
            case LOCATION:
                return TYPE_LOCATION;
            case RECORDING:
                return TYPE_RECORDING;
            case NONE_TEXT:
                return "none_text";
            case NONE_TYPING:
                return "none_typing";
            default:
                return "none";
        }
    }

    public static String getTypeName(String attachmentType) {
        switch (attachmentType) {
            case TYPE_IMAGE:
                return TYPE_IMAGE;
            case TYPE_AUDIO:
                return TYPE_AUDIO;
            case TYPE_VIDEO:
                return TYPE_VIDEO;
            case TYPE_CONTACT:
                return TYPE_CONTACT;
            case TYPE_DOCUMENT:
                return TYPE_DOCUMENT;
            case TYPE_LOCATION:
                return TYPE_LOCATION;
            case TYPE_RECORDING:
                return TYPE_RECORDING;
//            case NONE_TEXT:
//                return "none_text";
//            case NONE_TYPING:
//                return "none_typing";
            default:
                return "none";
        }
    }

    public static String getDirectoryByType(String type) {
        switch (type) {
            case TYPE_AUDIO:
            case TYPE_RECORDING:
                return Utils.getMusicFolder();
            case TYPE_VIDEO:
                return Utils.getMoviesFolder();
            case TYPE_DOCUMENT:
            case TYPE_CONTACT:
                return Utils.getDownloadFolder();
        }
        return Utils.getDownloadFolder();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Uri getTargetUri(String type) {
        switch (type) {
            case TYPE_AUDIO:
            case TYPE_RECORDING:
                return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            case TYPE_VIDEO:
                return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            case TYPE_DOCUMENT:
            case TYPE_CONTACT:
                return MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        }
        return MediaStore.Downloads.EXTERNAL_CONTENT_URI;
    }

    public static String getExtension(final String fileExtension, final int attachmentType) {

        switch (attachmentType) {
            case AUDIO:
            case RECORDING:
                if (!fileExtension.endsWith(EXT_MP3))
                    return EXT_MP3;
        }
        return fileExtension;
    }
}
