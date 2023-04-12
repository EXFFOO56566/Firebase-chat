package com.devlomi.fireapp.model.constants;

/**
 * Created by Devlomi.
 */
//indicates the message type
public class MessageType {
    public static final int SENT_TEXT = 1;
    public static final int SENT_IMAGE = 2;
    public static final int RECEIVED_TEXT = 3;
    public static final int RECEIVED_IMAGE = 4;
    public static final int SENT_VIDEO = 5;
    public static final int RECEIVED_VIDEO = 6;
    public static final int SENT_AUDIO = 9;
    public static final int RECEIVED_AUDIO = 10;
    public static final int SENT_VOICE_MESSAGE = 11;
    public static final int RECEIVED_VOICE_MESSAGE = 12;
    public static final int SENT_FILE = 13;
    public static final int RECEIVED_FILE = 14;
    public static final int DAY_ROW = 15;
    public static final int SENT_CONTACT = 16;
    public static final int RECEIVED_CONTACT = 17;
    public static final int SENT_LOCATION = 18;
    public static final int RECEIVED_LOCATION = 19;

    public static final int SENT_DELETED_MESSAGE = 30;
    public static final int RECEIVED_DELETED_MESSAGE = 31;

    public static final int GROUP_EVENT = 9999;

    public static final int[] SUPPORTED_MESSAGES_TYPES = {
            SENT_TEXT,
            SENT_IMAGE,
            RECEIVED_TEXT,
            RECEIVED_IMAGE,
            SENT_VIDEO,
            RECEIVED_VIDEO,
            SENT_AUDIO,
            RECEIVED_AUDIO,
            SENT_VOICE_MESSAGE,
            RECEIVED_VOICE_MESSAGE,
            SENT_FILE,
            RECEIVED_FILE,
            DAY_ROW,
            SENT_CONTACT,
            RECEIVED_CONTACT,
            SENT_LOCATION,
            RECEIVED_LOCATION,
            SENT_DELETED_MESSAGE,
            RECEIVED_DELETED_MESSAGE,
            GROUP_EVENT
    };

    public static boolean isMessageSupported(int type) {
        for (int i = 0; i < SUPPORTED_MESSAGES_TYPES.length; i++) {
            int mType = SUPPORTED_MESSAGES_TYPES[i];
            if (mType == type)
                return true;
        }
        return false;
    }

    public static boolean isMediaItem(int type) {
        return type == SENT_IMAGE || type == RECEIVED_IMAGE || type == SENT_VIDEO || type == RECEIVED_VIDEO
                || type == SENT_AUDIO || type == RECEIVED_AUDIO || type == SENT_VOICE_MESSAGE || type == RECEIVED_VOICE_MESSAGE;
    }


    public static boolean isSentType(int type) {
        return type == SENT_TEXT || type == SENT_IMAGE || type == SENT_VIDEO || type == SENT_AUDIO
                || type == SENT_FILE || type == SENT_VOICE_MESSAGE
                || type == SENT_CONTACT || type == SENT_LOCATION;
    }

    public static boolean isSentText(int type) {
        return type == SENT_TEXT || type == RECEIVED_TEXT;
    }

    public static boolean isDeletedMessage(int type) {
        return type == SENT_DELETED_MESSAGE || type == RECEIVED_DELETED_MESSAGE;
    }

    public static boolean isLocation(int type) {
        return type == SENT_LOCATION || type == RECEIVED_LOCATION;
    }

    public static boolean isContact(int type) {
        return type == SENT_CONTACT || type == RECEIVED_CONTACT;
    }

    public static boolean isVideo(int type) {
        return type == SENT_VIDEO || type == RECEIVED_VIDEO;
    }

    //convert sent type to received when receiving a message from other user
    //because by default it's sent when the other user sent it to user
    public static int convertSentToReceived(int type) {
        int convertedType = type;
        switch (type) {
            case SENT_TEXT:
                convertedType = RECEIVED_TEXT;
                break;

            case SENT_AUDIO:
                convertedType = RECEIVED_AUDIO;
                break;


            case SENT_FILE:
                convertedType = RECEIVED_FILE;
                break;

            case SENT_IMAGE:
                convertedType = RECEIVED_IMAGE;
                break;

            case SENT_VIDEO:
                convertedType = RECEIVED_VIDEO;
                break;

            case SENT_VOICE_MESSAGE:
                convertedType = RECEIVED_VOICE_MESSAGE;
                break;

            case SENT_CONTACT:
                convertedType = RECEIVED_CONTACT;
                break;

            case SENT_LOCATION:
                convertedType = RECEIVED_LOCATION;
                break;
        }
        return convertedType;
    }

    public static int convertReceivedToSent(int type) {
        int convertedType = type;
        switch (type) {
            case RECEIVED_TEXT:
                convertedType = SENT_TEXT;
                break;

            case RECEIVED_AUDIO:
                convertedType = SENT_AUDIO;
                break;


            case RECEIVED_FILE:
                convertedType = SENT_FILE;
                break;

            case RECEIVED_IMAGE:
                convertedType = SENT_IMAGE;
                break;

            case RECEIVED_VIDEO:
                convertedType = SENT_VIDEO;
                break;

            case RECEIVED_VOICE_MESSAGE:
                convertedType = SENT_VOICE_MESSAGE;
                break;

            case RECEIVED_CONTACT:
                convertedType = SENT_CONTACT;
                break;

            case RECEIVED_LOCATION:
                convertedType = SENT_LOCATION;
                break;
        }
        return convertedType;
    }
}
