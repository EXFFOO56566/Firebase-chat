package com.bytesbee.firebase.chat.activities.managers;

import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_GROUP_NAME;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_IMGPATH;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_LINK;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_OBJ_GROUP;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_STATUS;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_USERNAME;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.EXTRA_USER_ID;
import static com.bytesbee.firebase.chat.activities.constants.IConstants.REQUEST_PARTICIPATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityOptionsCompat;

import com.bytesbee.firebase.chat.activities.GroupsMessagesActivity;
import com.bytesbee.firebase.chat.activities.GroupsParticipantsActivity;
import com.bytesbee.firebase.chat.activities.ImageViewerActivity;
import com.bytesbee.firebase.chat.activities.MessageActivity;
import com.bytesbee.firebase.chat.activities.OnBoardingActivity;
import com.bytesbee.firebase.chat.activities.ProfileDialogActivity;
import com.bytesbee.firebase.chat.activities.R;
import com.bytesbee.firebase.chat.activities.SettingsActivity;
import com.bytesbee.firebase.chat.activities.ViewUserProfileActivity;
import com.bytesbee.firebase.chat.activities.WebViewBrowserActivity;
import com.bytesbee.firebase.chat.activities.constants.IConstants;
import com.bytesbee.firebase.chat.activities.models.Groups;

import java.io.Serializable;

/**
 * @author : Prashant Adesara
 * @url https://www.bytesbee.com
 * Navigation to new activity
 */
public class Screens {

    private final Context context;

    public Screens(Context con) {
        context = con;
    }

    public void showClearTopScreen(final Class<?> cls) {
        final Intent intent = new Intent(context, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public void showCustomScreen(final Class<?> cls) {
        final Intent intent = new Intent(context, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void startHomeScreen() {
        final Intent startHome = new Intent(Intent.ACTION_MAIN);
        startHome.addCategory(Intent.CATEGORY_HOME);
        startHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startHome);
    }

    private Toast toastMessage;

    public void showToast(final String strMsg) {
        try {
            if (toastMessage != null) {
                toastMessage.cancel();
            }
            if (!Utils.isEmpty(strMsg)) {
                toastMessage = Toast.makeText(context, strMsg, Toast.LENGTH_LONG);
                try {
                    @SuppressWarnings("deprecation") final LinearLayout toastLayout = (LinearLayout) toastMessage.getView();
                    final TextView txtToast = (TextView) toastLayout.getChildAt(0);
                    txtToast.setTypeface(Utils.getRegularFont(context));
                } catch (Exception e) {
                    Utils.getErrors(e);
                }
                toastMessage.show();
            }
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }

    public void showToast(final int strMsg) {
        showToast(context.getString(strMsg));
    }

    public void openOnBoardingScreen(final boolean isTakeTour) {
        final Intent intent = new Intent(context, OnBoardingActivity.class);
        intent.putExtra(EXTRA_STATUS, isTakeTour);
        context.startActivity(intent);
    }

    public void openProfilePictureActivity(final Object object) {
        final Intent intent = new Intent(context, ProfileDialogActivity.class);
        intent.putExtra(EXTRA_OBJ_GROUP, (Serializable) object);
        context.startActivity(intent);
    }

    public void openUserMessageActivity(final String userId) {
        final Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        context.startActivity(intent);
    }

    public void openViewProfileActivity(final String userId) {
        final Intent intent = new Intent(context, ViewUserProfileActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        context.startActivity(intent);
    }

    public void openGroupMessageActivity(final Groups object) {
        final Intent intent = new Intent(new Intent(context, GroupsMessagesActivity.class));
        intent.putExtra(EXTRA_OBJ_GROUP, object);
        context.startActivity(intent);
    }

    public void openGroupParticipantActivity(final Groups groups) {
        final Intent intent = new Intent(context, GroupsParticipantsActivity.class);
        intent.putExtra(EXTRA_OBJ_GROUP, groups);
        ((Activity) context).startActivityForResult(intent, REQUEST_PARTICIPATE);
    }

    public void openFullImageViewActivity(final View view, final String imgPath, final String username) {
        openFullImageViewActivity(view, imgPath, "", username);
    }

    public void openFullImageViewActivity(final View view, final String imgPath, final String groupName, final String username) {
        final Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(EXTRA_IMGPATH, imgPath);
        intent.putExtra(EXTRA_GROUP_NAME, groupName);
        intent.putExtra(EXTRA_USERNAME, username);
        try {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, view, context.getString(R.string.app_name));
            context.startActivity(intent, options.toBundle());
        } catch (Exception e) {
            context.startActivity(intent);
        }
    }

    public void openSettingsActivity() {
        final Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    public void openWebViewActivity(final String title, final String path) {
        final Intent intent = new Intent(context, WebViewBrowserActivity.class);
        intent.putExtra(EXTRA_USERNAME, title);
        intent.putExtra(EXTRA_LINK, path);
        context.startActivity(intent);
    }

    public void openGPSSettingScreen() {
        showToast(context.getString(R.string.msgGPSTurnOn));
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        }, IConstants.CLICK_DELAY_TIME);

    }
}
