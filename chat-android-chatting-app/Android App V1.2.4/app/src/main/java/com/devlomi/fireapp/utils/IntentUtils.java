package com.devlomi.fireapp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.support.v4.content.FileProvider;

import com.devlomi.fireapp.BuildConfig;
import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.realms.PhoneNumber;
import com.devlomi.fireapp.model.realms.RealmContact;
import com.devlomi.fireapp.model.realms.RealmLocation;

import java.io.File;
import java.util.ArrayList;

import io.realm.RealmList;

/**
 * Created by Devlomi on 31/01/2018.
 */

//this class to identify extras and actions
//and for getting some custom intents
public class IntentUtils {

    public static final String EXTRA_PATH = "path";
    public static final String EXTRA_REAL_PATH = "real-path";
    public static final String EXTRA_REAL_PATH_LIST = "real-path-list";
    public static final String EXTRA_STAT = "stat";
    public static final String EXTRA_MY_UID = "my_uid";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_GROUP_ID = "extra-group-id";


    public static final String EXTRA_PATH_RESULT = "path_result";
    public static final String EXTRA_MESSAGE_ID = "messageId";
    public static final String EXTRA_DATA_RESULT = "data";
    public static final String EXTRA_CONTACT_LIST = "contactList";

    public static final String EXTRA_SHARED_TEXT = "shared_text";
    public static final String EXTRA_URI_LIST = "uri_list";
    public static final String EXTRA_URI = "uri";
    public static final String EXTRA_MIME_TYPE = "mime_type";


    public static final String EXTRA_FORWARDED = "forwarded";


    public static final String EXTRA_PROFILE_PATH = "extra_profile_path";


    public static final String EXTRA_FIRST_VISIBLE_ITEM_POSITION = "extra_first_visible_item_position";
    public static final String EXTRA_LAST_VISIBLE_ITEM_POSITION = "extra_last_visible_item_position";
    public static final String EXTRA_CURRENT_MESSAGE_ID = "current_message_id";
    public static final String EXTRA_STARTING_POSITION = "extra_starting_item_position";
    public static final String EXTRA_CURRENT_ALBUM_POSITION = "extra_current_item_position";

    public static final String INTENT_ACTION_SYNC_CONTACTS = "intent-action-sync-contacts";
    public static final String INTENT_ACTION_HANDLE_REPLY = "intent-action-handle-reply";
    public static final String INTENT_ACTION_MARK_AS_READ = "intent-action-mark-as-read";
    public static final String INTENT_ACTION_UPDATE_MESSAGE_STATE = "intent-action-update-message-state";
    public static final String INTENT_ACTION_UPDATE_VOICE_MESSAGE_STATE = "intent-action-update-voice-message-state";
    public static final String INTENT_ACTION_NETWORK_REQUEST = "intent-action-network-request";
    public static final String INTENT_ACTION_FETCH_AND_CREATE_GROUP = "intent-action-fetch-and-create-group";
    public static final String INTENT_ACTION_FETCH_GROUP_INFO = "intent-action-fetch-group-info";

    public static final String INTENT_ACTION_FETCH_USER_GROUPS_AND_BROADCASTS = "intent-action-fetch-user-groups";
    public static final String INTENT_ACTION_MESSAGE_DELETED = "intent-action-message-deleted";


    public static final String PHONE = "phone";
    public static final String INTENT_ACTION_DISMISS_NOTIFICATION = "dismiss-handleNewMessage";
    public static final String UID = "uid";
    public static final String ID = "id";
    public static final String ACTION_START_PLAY = "start_play";
    public static final String ACTION_SEEK_TO = "seek_to";
    public static final String URL = "url";
    public static final String POS = "pos";
    public static final String PROGRESS = "progress";
    public static final String ACTION_STOP_AUDIO = "stop_audio";
    public static final String EXTRA_HEADSETSTATE_CHANGED = "headsetstate_changed";
    public static final String EXTRA_GROUP_COUNT = "extra-group-count";
    public static final String EXTRA_SELECTED_USERS = "extra-selected-users";
    public static final String INTENT_ACTION_UPDATE_GROUP = "intent-action-update-group";
    public static final String EXTRA_EVENT_ID = "extra-event-id";
    public static final String EXTRA_CONTEXT_START = "extra-context-start";
    public static final String EXTRA_EVENT_TYPE = "extra-event-type";
    public static final String EXTRA_CONTEXT_END = "extra-context-end";
    public static final String EXTRA_GROUP_EVENT = "extra-group-event";
    public static final String EXTRA_STATUS_ID = "extra-status-id";
    public static final String EXTRA_TEXT_STATUS = "extra-text-status";


    public static final String EXTRA_CHAT_ID = "extra-chat-id";
    public static final String ACTION_TYPE = "action-type";
    public static final String FCM_TOKEN = "fcm-token";
    public static final String PHONE_CALL_TYPE = "phone-call-type";
    public static final String ISVIDEO = "is_video";
    public static final String CALL_ID = "call-id";
    public static final String CALL_ACTION_TYPE = "call-action-type";
    public static final String IS_COMING_FROM_NOTIFICATION = "is-coming-from-notification";
    public static final String CAMERA_VIEW_SHOW_PICK_IMAGE_BUTTON = "camera-view-show-pick-image";
    public static final String IS_STATUS = "isStatus";
    public static final String START_SINCH = "start-sinch";
    public static final String IS_GROUP = "isGroup";
    public static final String IS_BROADCAST = "isBroadcast";

    public static String EXTRA_FROMID = "fromId";

    public static int NOTIFICATION_ACTION_ANSWER = 1;
    public static int NOTIFICATION_ACTION_DECLINE = 2;
    public static int NOTIFICATION_ACTION_CLICK = 3;


    //used to open a maps app with the given location
    public static Intent getOpenMapIntent(RealmLocation location) {
        double latitude = location.getLat();
        double longitude = location.getLng();
        String label = location.getName();
        String uriBegin = "geo:" + latitude + "," + longitude;
        String query = latitude + "," + longitude + "(" + label + ")";
        String encodedQuery = Uri.encode(query);
        String uriString = uriBegin + "?q=" + encodedQuery + "&z=17";
        Uri uri = Uri.parse(uriString);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
        return intent;
    }


    public static Intent getShareImageIntent(String filePath) {

        Intent shareIntent = new Intent();

        shareIntent.setAction(Intent.ACTION_SEND);

        Uri uri = Uri.fromFile(new File(filePath));
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

        if (Build.VERSION.SDK_INT >= 24) {
            //this solves android.os.FileUriExposedException
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        shareIntent.setType(MimeTypes.getMimeType(filePath));

        return Intent.createChooser(shareIntent, "Share Using");

    }

    //used to open the file by system
    public static Intent getOpenFileIntent(Context context, String path) {

        String fileExtension = Util.getFileExtensionFromPath(path);

        File toInstall = new File(path);


        //if it's apk make the system open apk installer
        if (fileExtension.equalsIgnoreCase("apk")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", toInstall);
                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(apkUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                return intent;
            } else {
                Uri apkUri = Uri.fromFile(toInstall);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                return intent;
            }


            //else make the system open an app that can handle given type
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri uriForFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", toInstall);
                Intent newIntent = new Intent(Intent.ACTION_VIEW);
                newIntent.setDataAndType(uriForFile, "application/*");
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                return newIntent;
            } else {
                Uri uriForFile = Uri.fromFile(toInstall);
                Intent newIntent = new Intent(Intent.ACTION_VIEW);
                newIntent.setDataAndType(uriForFile, "application/*");
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                return newIntent;
            }
        }
    }

    //add contact to the device using realm contact
    public static Intent getAddContactIntent(RealmContact contact) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        RealmList<PhoneNumber> numbersList = contact.getRealmList();
        ArrayList<ContentValues> data = new ArrayList<ContentValues>();

        //add phone numbers to ContentValue
        for (int i = 0; i < numbersList.size(); i++) {
            ContentValues row = new ContentValues();
            row.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            row.put(ContactsContract.CommonDataKinds.Phone.NUMBER, numbersList.get(i).getNumber());
            row.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
            data.add(row);
        }

        //add contact name
        intent.putExtra(ContactsContract.Intents.Insert.NAME, contact.getName());
        //set the contact numbers
        intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);

        return intent;
    }

    //add contact to the device using realm phone number
    public static Intent getAddContactIntent(String phone) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone);
        return intent;
    }

    public static Intent getSendEmailIntent(Context context) {
        String email = context.getString(R.string.email);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name) + " " + context.getString(R.string.feedback));
        return Intent.createChooser(emailIntent, context.getString(R.string.choose_email_app));
    }

    public static Intent getOpenTwitterIntent() {
        String twitterId = MyApp.context().getString(R.string.twitter_account);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(twitterId));
        return intent;
    }

    public static Intent getOpenWebsiteIntent() {
        String website = MyApp.context().getString(R.string.website);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
        return intent;
    }

    public static Intent getShareAppIntent(Context context) {
        final String appPackageName = context.getPackageName();
        String link = "https://play.google.com/store/apps/details?id=" + appPackageName;//app link is auto generated by using package name
        Resources res = context.getResources();
        String text = String.format(res.getString(R.string.share_app_text),
                res.getString(R.string.app_name));

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text + "\n" + link);
        sendIntent.setType("text/plain");
        return sendIntent;
    }


    public static Intent getShareTextIntent(String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        return sendIntent;
    }
}
