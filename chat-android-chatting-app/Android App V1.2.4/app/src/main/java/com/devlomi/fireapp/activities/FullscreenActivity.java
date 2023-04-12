package com.devlomi.fireapp.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.adapters.FullScreenAdapter;
import com.devlomi.fireapp.interfaces.ToolbarStateChange;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.ListUtil;
import com.devlomi.fireapp.utils.MessageCreator;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.utils.ServiceHelper;
import com.devlomi.fireapp.utils.TimeHelper;
import com.devlomi.fireapp.views.dialogs.DeleteDialog;

import java.util.List;
import java.util.Map;

import io.realm.RealmList;
import me.zhanghai.android.systemuihelper.SystemUiHelper;

import static com.devlomi.fireapp.utils.IntentUtils.EXTRA_CURRENT_ALBUM_POSITION;
import static com.devlomi.fireapp.utils.IntentUtils.EXTRA_CURRENT_MESSAGE_ID;
import static com.devlomi.fireapp.utils.IntentUtils.EXTRA_FIRST_VISIBLE_ITEM_POSITION;
import static com.devlomi.fireapp.utils.IntentUtils.EXTRA_LAST_VISIBLE_ITEM_POSITION;
import static com.devlomi.fireapp.utils.IntentUtils.EXTRA_STARTING_POSITION;


public class FullscreenActivity extends AppCompatActivity implements ToolbarStateChange {

    private static final int REQUEST_CODE_FORWARD = 145;
    public static final int TOOLBAR_ANIMATION_DURATION = 400;

    Toolbar toolbar;
    private ViewPager viewPager;
    private TextView toolbarName, toolbarTime;
    private LinearLayout appbarWrapper;
    private FrameLayout appbar;

    //only media from this chat
    List<Message> images;
    //get whole chat list
    List<Message> messages;

    FullScreenAdapter adapter;
    SystemUiHelper helper;
    User user;

    int mCurrentPosition = 0;
    private int mStartingPosition;
    private int mFirstVisibleItem;
    private int mLastVisibleItem;

    private boolean mIsReturning;


    //transition
    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            if (mIsReturning) {
                ImageView sharedElement = (ImageView) adapter.getImage();
                if (sharedElement == null) {
                    // If shared element is null, then it has been scrolled off screen and
                    // no longer visible. In this case we cancel the shared element transition by
                    // removing the shared element from the shared elements map.
                    names.clear();
                    sharedElements.clear();
                } else if (mStartingPosition != mCurrentPosition) {
                    // If the user has swiped to a different ViewPager page, then we need to
                    // remove the old shared element and replace it with the new shared element
                    // that should be transitioned instead.


                    int realListPosition = Message.getPosFromId(images.get(mCurrentPosition).getMessageId(), messages);

                    names.clear();
                    sharedElements.clear();


                    //getting only elements in bounds
                    if (realListPosition <= mFirstVisibleItem && realListPosition <= mLastVisibleItem) {
                        names.add(ViewCompat.getTransitionName(sharedElement));
                        sharedElements.put(ViewCompat.getTransitionName(sharedElement), sharedElement);
                    }


                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        init();

        //transitions
        supportPostponeEnterTransition();
        setEnterSharedElementCallback(mCallback);

        mFirstVisibleItem = getIntent().getIntExtra(EXTRA_FIRST_VISIBLE_ITEM_POSITION, 0);
        mLastVisibleItem = getIntent().getIntExtra(EXTRA_LAST_VISIBLE_ITEM_POSITION, 0);


        setSupportActionBar(toolbar);


        helper = new SystemUiHelper(this, SystemUiHelper.LEVEL_IMMERSIVE, SystemUiHelper.FLAG_IMMERSIVE_STICKY, new SystemUiHelper.OnVisibilityChangeListener() {
            @Override
            public void onVisibilityChange(boolean visible) {
                //hide or show toolbar when the bar state is changed
                animateToolbar(visible);
            }
        });

        helper.show();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        String selectedImgMessageId = getIntent().getStringExtra(IntentUtils.EXTRA_MESSAGE_ID);


        String uid = getIntent().getStringExtra(IntentUtils.UID);
        user = RealmHelper.getInstance().getUser(uid);
        String chatId = user.getUid();


        images = RealmHelper.getInstance().getMediaInChat(chatId);
        messages = RealmHelper.getInstance().getMessagesInChat(chatId);

        mCurrentPosition = getPosFromId(selectedImgMessageId);

        mStartingPosition = mCurrentPosition;


        adapter = new FullScreenAdapter(getSupportFragmentManager(), this, images, mCurrentPosition);

        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(mCurrentPosition);
        setToolbarData(images.get(mCurrentPosition));


        //onSwipe
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                setToolbarData(images.get(position));
            }

        });


    }

    private void animateToolbar(boolean visible) {
        if (visible) {
            appbarWrapper.animate()
                    .alpha(1)
                    .translationY(0)
                    .setDuration(TOOLBAR_ANIMATION_DURATION)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
        } else {
            appbarWrapper.animate()
                    .alpha(0)
                    .translationY(-appbar.getBottom())
                    .setDuration(TOOLBAR_ANIMATION_DURATION)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
        }
    }


    private void init() {
        toolbar = findViewById(R.id.toolbar);
        toolbarName = findViewById(R.id.toolbar_name);
        toolbarTime = findViewById(R.id.toolbar_time);
        appbarWrapper = findViewById(R.id.appbar_wrapper);
        appbar = findViewById(R.id.appbar);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(1);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_img_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void finishAfterTransition() {
        mIsReturning = true;
        Intent data = new Intent();
        data.putExtra(EXTRA_STARTING_POSITION, mStartingPosition);

        data.putExtra(EXTRA_CURRENT_ALBUM_POSITION, mCurrentPosition);
        String messageId = images.get(mCurrentPosition).getMessageId();
        data.putExtra(EXTRA_CURRENT_MESSAGE_ID, messageId);

        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.forward_item:
                forwardItemClicked();
                break;

            case R.id.delete_item:
                deleteItemClicked();
                break;

            case R.id.menu_item_share:
                shareItemClicked();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareItemClicked() {
        String path = images.get(mCurrentPosition).getLocalPath();
        Intent shareImageIntent = IntentUtils.getShareImageIntent(path);
        shareImageIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        shareImageIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(shareImageIntent);
    }

    private void deleteItemClicked() {
        DeleteDialog deleteDialog = new DeleteDialog(this, true);

        deleteDialog.setmListener(new DeleteDialog.OnFragmentInteractionListener() {
            @Override
            public void onPositiveClick(boolean isDeleteChecked) {
                String messageId = images.get(mCurrentPosition).getMessageId();
                String chatId = images.get(mCurrentPosition).getChatId();

                RealmHelper.getInstance().deleteMessageFromRealm(chatId, messageId, isDeleteChecked);
                adapter.notifyDataSetChanged();
                if (images.isEmpty())
                    finish();
            }
        });

        deleteDialog.show();


    }


    private void forwardItemClicked() {
        Intent intent = new Intent(this, ForwardActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FORWARD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FORWARD && resultCode == RESULT_OK) {
            List<User> selectedList = data.getParcelableArrayListExtra(IntentUtils.EXTRA_DATA_RESULT);
            if (selectedList.isEmpty())
                return;

            Message message = images.get(mCurrentPosition);

            if (selectedList.size() == 1) {
                Intent intent = new Intent(this, ChatActivity.class);
                User user = selectedList.get(0);
                Message forwardedMessage = MessageCreator.createForwardedMessage(message, user, FireManager.getUid());
                intent.putExtra(IntentUtils.EXTRA_FORWARDED, true);
                intent.putExtra(IntentUtils.UID, user.getUid());
                intent.putExtra(IntentUtils.EXTRA_MESSAGE, forwardedMessage);

                //prevent duplicate
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();


            } else {
                for (User user : selectedList) {
                    Message forwardedMessage = MessageCreator.createForwardedMessage(message, user, FireManager.getUid());
                    ServiceHelper.startNetworkRequest(this, forwardedMessage.getMessageId(), forwardedMessage.getChatId());
                }
                Toast.makeText(this, R.string.sending_messages, Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void setToolbarData(Message message) {
        toolbarName.setText(getSenderName(message.getFromId()));
        toolbarTime.setText(TimeHelper.getMediaTime(Long.parseLong(message.getTimestamp())));
    }

    private String getSenderName(String fromId) {
        if (fromId.equals(FireManager.getUid())) {
            return getString(R.string.you);
        }

        if (user.isGroupBool()) {
            RealmList<User> users = user.getGroup().getUsers();
            User userById = ListUtil.getUserById(fromId, users);
            if (userById != null)
                return userById.getUserName();
        }

        return user.getUserName();

    }

    private int getPosFromId(String messageId) {
        for (int i = 0; i < images.size(); i++) {
            Message message = images.get(i);
            if (message.getMessageId().equals(messageId)) {
                return i;
            }
        }
        return 0;
    }


    @Override
    public void hideToolbar() {
        helper.hide();
    }

    @Override
    public void showToolbar() {
        helper.show();
    }

    @Override
    public void toggle() {
        helper.toggle();
    }


}
