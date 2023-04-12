package com.devlomi.fireapp.utils;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.constants.MessageType;
import com.devlomi.fireapp.model.realms.Message;

/**
 * Created by Devlomi on 19/03/2018.
 */

public class MessageTypeHelper {
    public static String getTypeText(int type) {
        switch (type) {
            case MessageType.SENT_IMAGE:
            case MessageType.RECEIVED_IMAGE:
                return MyApp.context().getString(R.string.photo);

            case MessageType.SENT_VIDEO:
            case MessageType.RECEIVED_VIDEO:
                return MyApp.context().getString(R.string.video);


            case MessageType.SENT_VOICE_MESSAGE:
            case MessageType.RECEIVED_VOICE_MESSAGE:
                return MyApp.context().getString(R.string.voice_message);


            case MessageType.SENT_AUDIO:
            case MessageType.RECEIVED_AUDIO:
                return MyApp.context().getString(R.string.audio);


            case MessageType.SENT_FILE:
            case MessageType.RECEIVED_FILE:
                return MyApp.context().getString(R.string.file);


            case MessageType.SENT_LOCATION:
            case MessageType.RECEIVED_LOCATION:
                return MyApp.context().getString(R.string.location);


            default:
                return "";
        }

    }

    public static String extractMessageTypeMetadataText(Message message) {
        if (message.isVoiceMessage()) {
            //set the voice message duration
            return message.getMediaDuration();

        } else if (message.isContactMessage()) {
            //set contact name
            return message.getContact().getName();
        } else if (message.isLocation()) {
            //set location name or Address
            if (!Util.isNumeric(message.getLocation().getName())) {
                return message.getLocation().getName();
            } else {
                return message.getLocation().getAddress();
            }
        }
        return getTypeText(message.getType());
    }

    public static int getMessageTypeDrawable(int type) {
        switch (type) {
            case MessageType.SENT_IMAGE:
            case MessageType.RECEIVED_IMAGE:
                return R.drawable.ic_photo_camera;

            case MessageType.SENT_VIDEO:
            case MessageType.RECEIVED_VIDEO:
                return R.drawable.ic_videocam;


            case MessageType.SENT_VOICE_MESSAGE:
            case MessageType.RECEIVED_VOICE_MESSAGE:
                return R.drawable.mic_icon;


            case MessageType.SENT_AUDIO:
            case MessageType.RECEIVED_AUDIO:
                return R.drawable.ic_music_note;

            case MessageType.SENT_CONTACT:
            case MessageType.RECEIVED_CONTACT:
                return R.drawable.ic_person;

            case MessageType.SENT_LOCATION:
            case MessageType.RECEIVED_LOCATION:
                return R.drawable.ic_location_on;

            case MessageType.SENT_FILE:
            case MessageType.RECEIVED_FILE:
                return R.drawable.ic_insert_drive_file;

            default:
                return -1;
        }

    }

    //this is to show emoji icon at start of the notification
    public static String getEmojiIcon(int type) {

        switch (type) {

            case MessageType.RECEIVED_IMAGE:
                return "\uD83D\uDCF7";


            case MessageType.RECEIVED_VIDEO:
                return "\uD83D\uDCF9";


            case MessageType.RECEIVED_VOICE_MESSAGE:
                return "\uD83C\uDFA4";


            case MessageType.RECEIVED_AUDIO:
                return "\uD83C\uDFB5";

            case MessageType.RECEIVED_CONTACT:
                return "\uD83D\uDC65";

            case MessageType.RECEIVED_LOCATION:
                return "\uD83D\uDCCD";

            case MessageType.RECEIVED_FILE:
                return "\uD83D\uDCCD";

            default:
                return "";
        }
    }


    //get message content with emoji icon if needed
    public static String getMessageContent(Message message, boolean includeEmoji) {
        String contentText;
        //if it's a text message we don't need to show an Emoji
        if (message.isTextMessage())
            contentText = message.getContent();
        else {
            String emojiText = includeEmoji ? MessageTypeHelper.getEmojiIcon(message.getType()) + " " : "";

            //if it's a voice message add mic icon at the start along with voice message duration in ()
            if (message.isVoiceMessage()) {
                contentText = emojiText + MessageTypeHelper.getTypeText(message.getType()) + " (" + message.getMediaDuration() + ")";
                //if it's a contact message show contact icon + the contact name
            } else if (message.isContactMessage()) {
                contentText = emojiText + message.getContact().getName() + MessageTypeHelper.getTypeText(message.getType());
            } else
                //otherwise get the needed emoji(image,video,file location etc..)
                contentText = emojiText + MessageTypeHelper.getTypeText(message.getType());
        }
        return contentText;
    }


}
