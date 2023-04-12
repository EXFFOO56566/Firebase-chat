package com.devlomi.fireapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.adapters.MediaGalleryAdapter;
import com.devlomi.fireapp.views.dialogs.DeleteDialog;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.ServiceHelper;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.MessageCreator;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.views.GridItemDecoration;

import java.util.List;

public class MediaGalleryActivity extends AppCompatActivity {
    private static int SPAN_COUNT = 3;
    private static int SPACING = 16;
    private static final int ITEMS_COUNT_PER_ROW = 3;
    private static final int REQUEST_FORWARD = 145;


    private Toolbar toolbar;
    private RecyclerView rvMediaGallery;
    public boolean isInActionMode = false;
    MediaGalleryAdapter adapter;
    private TextView tvSelectedImagesCount;
    User user;
    private List<Message> mediaInChat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_gallery);
        init();
        setSupportActionBar(toolbar);

        String uid = getIntent().getStringExtra(IntentUtils.UID);
        user = RealmHelper.getInstance().getUser(uid);

        getSupportActionBar().setTitle(user.getUserName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mediaInChat = RealmHelper.getInstance().getMediaInChat(user.getUid());

        adapter = new MediaGalleryAdapter(this, mediaInChat);


        rvMediaGallery.setLayoutManager(new GridLayoutManager(this, ITEMS_COUNT_PER_ROW));

        //add spacing between items
        rvMediaGallery.addItemDecoration(new GridItemDecoration(SPAN_COUNT, SPACING, false));

        rvMediaGallery.setAdapter(adapter);


    }

    private void init() {
        toolbar = findViewById(R.id.toolbar_gallery);
        rvMediaGallery = findViewById(R.id.rv_media_gallery);
        tvSelectedImagesCount = findViewById(R.id.tv_selected_images_count);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.menu_item_forward:
                forwardItemClicked();
                break;

            case R.id.menu_item_delete:
                deleteItemClicked();
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    private void deleteItemClicked() {
        DeleteDialog deleteDialog = new DeleteDialog(this, true);
        deleteDialog.setmListener(new DeleteDialog.OnFragmentInteractionListener() {
            @Override
            public void onPositiveClick(boolean isDeleteChecked) {
                adapter.deleteItems(isDeleteChecked);
                exitActionMode();
            }


        });
        deleteDialog.show();

    }

    private void forwardItemClicked() {
        Intent intent = new Intent(this, ForwardActivity.class);
        startActivityForResult(intent, REQUEST_FORWARD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FORWARD && resultCode == RESULT_OK) {
            //get selected users
            List<User> pickedUsers = (List<User>) data.getSerializableExtra(IntentUtils.EXTRA_DATA_RESULT);

            //if the user selects only one user to send the images to him
            //then send the images and the launch activity with that user
            if (pickedUsers.size() == 1) {
                for (Message message : adapter.getSelectedItems()) {
                    Message forwardedMessage = MessageCreator.createForwardedMessage(message, user, FireManager.getUid());
                    ServiceHelper.startNetworkRequest(this, forwardedMessage.getMessageId(), message.getChatId());
                }
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(IntentUtils.UID, user.getUid());
                startActivity(intent);
                finish();
                //otherwise send the images to the users and finish this activity
            } else {
                for (User pickedUser : pickedUsers) {
                    for (Message message : adapter.getSelectedItems()) {
                        Message forwardedMessage = MessageCreator.createForwardedMessage(message, pickedUser, FireManager.getUid());
                        ServiceHelper.startNetworkRequest(this, forwardedMessage.getMessageId(), message.getChatId());
                    }
                }

                Toast.makeText(this, R.string.sending_messages, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void addItemToActionMode(int itemsCount) {
        tvSelectedImagesCount.setText(itemsCount + "");
    }


    public void onActionModeStarted() {
        if (!isInActionMode) {
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.menu_gallery_action);
            setToolbarTitle(false);
        }

        isInActionMode = true;
        tvSelectedImagesCount.setVisibility(View.VISIBLE);

    }

    public void exitActionMode() {
        adapter.exitActionMode();
        isInActionMode = false;
        tvSelectedImagesCount.setVisibility(View.GONE);
        toolbar.getMenu().clear();
        setToolbarTitle(true);
    }

    public boolean isInActionMode() {
        return isInActionMode;
    }


    @Override
    public void onBackPressed() {
        if (isInActionMode)
            exitActionMode();
        else
            super.onBackPressed();
    }


    private void setToolbarTitle(boolean setVisible) {
        if (setVisible)
            getSupportActionBar().setTitle(user.getUserName());
        else
            getSupportActionBar().setTitle("");
    }


    public User getUser() {
        return user;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //update items if items deleted
        adapter.notifyDataSetChanged();
    }
}
