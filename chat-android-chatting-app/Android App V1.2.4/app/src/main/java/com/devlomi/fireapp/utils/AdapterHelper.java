package com.devlomi.fireapp.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.constants.DownloadUploadStat;
import com.devlomi.fireapp.model.constants.MessageStat;
import com.devlomi.fireapp.model.constants.MessageType;
import com.devlomi.fireapp.model.realms.Message;

import java.util.Date;
import java.util.List;

public class AdapterHelper {


    //check if it's only one item and there it's a media item and it's downloaded also
    public static boolean shouldEnableShareButton(List<Message> selectedItems) {
        if (selectedItems.size() == 1 && isHasMediaItem(selectedItems) && isMediaDownloaded(selectedItems))
            return true;

        return false;

    }


    //check if the list has a media item
    public static boolean isHasMediaItem(List<Message> selectedItems) {
        boolean returnVal = false;
        for (Message message : selectedItems) {

            if (MessageType.isMediaItem(message.getType()))
                returnVal = true;
            else
                return false;

        }
        return returnVal;
    }

    //check if media is downloaded
    public static boolean isMediaDownloaded(List<Message> selectedItems) {
        boolean returnVal = false;

        for (Message message : selectedItems) {

            if (message.getDownloadUploadStat() == DownloadUploadStat.SUCCESS
                    /* second param is for non media messages and default is 0 */
                    || message.getDownloadUploadStat() == DownloadUploadStat.DEFAULT)
                returnVal = true;

            else return false;


        }

        return returnVal;


    }

    public static boolean hasDeletedMessage(List<Message> messages) {
        for (Message message : messages) {
            if (MessageType.isDeletedMessage(message.getType()))
                return true;
        }
        return false;
    }

    //check if media is downloaded to enable forward button
    public static boolean shouldEnableForwardButton(List<Message> selectedItems) {
        if (isMediaDownloaded(selectedItems) && !hasDeletedMessage(selectedItems))
            return true;
        return false;
    }

    //check if all messages are ONLY text
    public static boolean shouldEnableCopyItem(List<Message> selectedItems) {
        boolean returnVal = false;
        for (Message message : selectedItems) {
            if (message.isExists() && message.isTextMessage())
                returnVal = true;
            else return false;
        }
        return returnVal;
    }


    public static int getVoiceMessageIcon(boolean isVoiceMessageSeen) {
        if (isVoiceMessageSeen)
            return R.drawable.ic_mic_read_with_stroke;
        else
            return R.drawable.ic_mic_sent_with_stroke;
    }

    //check if this message is selected or not
    public static boolean isSelectedForActionMode(Message message, List<Message> selcetedItems) {
        return (!selcetedItems.isEmpty() && message.isExists() && selcetedItems.contains(message));
    }

    public static int getPlayIcon(boolean isPlaying) {
        if (isPlaying)
            return R.drawable.ic_pause;

        return R.drawable.ic_play_arrow;
    }

    public static int getMessageStatDrawable(int messageStat) {
        switch (messageStat) {
            case MessageStat.PENDING:
                return R.drawable.ic_watch_later_green;

            case MessageStat.SENT:
                return R.drawable.ic_check;

            case MessageStat.RECEIVED:
                return R.drawable.ic_done_all;

            case MessageStat.READ:
                return R.drawable.ic_check_read;

            default:
                return R.drawable.ic_check;

        }
    }


    //change the checkmark color to grey
    public static Drawable getColoredStatDrawable(Context context, int messageStat) {
        Resources resources = context.getResources();
        Drawable drawable = resources.getDrawable(getMessageStatDrawable(messageStat));
        drawable.mutate();

        //if it's not read we will keep the blue color
        if (messageStat != MessageStat.READ) {
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
            DrawableCompat.setTint(drawable, context.getResources().getColor(R.color.colorTextDesc));
        }
        return drawable;
    }

    public static boolean canDeleteForEveryOne(List<Message> selectedItems) {
        for (Message selectedItem : selectedItems) {

            long messageTime = Long.parseLong(selectedItem.getTimestamp());
            long now = new Date().getTime();
            if (selectedItem.getMessageStat() == MessageStat.PENDING
                    || MessageType.isDeletedMessage(selectedItem.getType())//check if it's already deleted
                    || !MessageType.isSentType(selectedItem.getType())//check if it's sent by the user
                    || TimeHelper.isMessageTimePassed(now, messageTime)) //check if time is valid
                return false;
        }
        return true;
    }


    //check if the list has a media item
    public static boolean shouldHideAllItems(List<Message> selectedItems) {
        boolean returnVal = false;
        for (Message message : selectedItems) {

            if (!MessageType.isMessageSupported(message.getType()))
                return true;
            else
                returnVal = false;

        }
        return returnVal;
    }
}
