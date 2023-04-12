package com.devlomi.fireapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;

import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.NotificationHelper;
import com.devlomi.fireapp.utils.ServiceHelper;

import static com.devlomi.fireapp.utils.NotificationHelper.KEY_TEXT_REPLY;


//this will handle the reply from notification on API24+
public class HandleReplyReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(IntentUtils.INTENT_ACTION_HANDLE_REPLY)) {
                //get user to send the message
                String uid = intent.getStringExtra(IntentUtils.UID);
                String chatId = intent.getStringExtra(IntentUtils.EXTRA_CHAT_ID);
                //get the replied text
                String text = getMessageText(intent);
                ServiceHelper.handleReply(context, uid, chatId, text);


                new NotificationHelper(context).dismissNotification(uid, true);
            }
        }
    }

    //get the text from textField in notification
    private String getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(KEY_TEXT_REPLY).toString();
        }
        return null;
    }

}
