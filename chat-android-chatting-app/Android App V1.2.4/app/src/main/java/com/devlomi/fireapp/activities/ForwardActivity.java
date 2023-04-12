package com.devlomi.fireapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.activities.main.MainActivity;
import com.devlomi.fireapp.adapters.ForwardAdapter;
import com.devlomi.fireapp.model.ExpandableContact;
import com.devlomi.fireapp.model.constants.MessageType;
import com.devlomi.fireapp.model.realms.Message;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.ContactUtils;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.MessageCreator;
import com.devlomi.fireapp.utils.MimeTypes;
import com.devlomi.fireapp.utils.RealPathUtil;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.utils.ServiceHelper;
import com.devlomi.fireapp.utils.StringUtils;
import com.devlomi.fireapp.utils.Util;
import com.devlomi.fireapp.views.DevlomiSnackbar;

import java.util.ArrayList;
import java.util.List;

import ezvcard.VCard;
import io.realm.RealmResults;

public class ForwardActivity extends AppCompatActivity {
    public static final int PICK_NUMBERS_REQUEST = 1478;
    private Toolbar toolbarForward;
    private RecyclerView rvForward;
    RealmResults<User> usersList;
    ForwardAdapter adapter;
    private TextView tvSelectedContact;
    private FloatingActionButton fabSend;
    List<User> selectedForwardedUsers;


    public static final String SEPARATOR = " , ";
    CoordinatorLayout rootView;
    private DevlomiSnackbar mSnackbar;


    interface SearchCallback {
        void onQuery(String newText);

        void onSearchClose();
    }

    public void setSearchCallback(SearchCallback searchCallback) {
        this.searchCallback = searchCallback;
    }

    SearchCallback searchCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward);
        init();


        fabSend.hide();
        setSupportActionBar(toolbarForward);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initAdapter();


        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isHasIncomingShare()) {
                    if (adapter.getSelectedForwardedUsers().isEmpty())
                        return;

                    // in case only one user is selected
                    handleIncomingShare(adapter.getSelectedForwardedUsers());

                } else {

                    if (adapter.getSelectedForwardedUsers().isEmpty())
                        setResult(RESULT_CANCELED);
                    Intent intent = new Intent();
                    intent.putParcelableArrayListExtra(IntentUtils.EXTRA_DATA_RESULT, (ArrayList<? extends Parcelable>) adapter.getSelectedForwardedUsers());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });


    }

    private boolean isHasIncomingShare() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null || Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            return true;
        }

        return false;
    }

    private void handleIncomingShare(List<User> selectedUsers) {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {


            if (type.equals(MimeTypes.TEXT_PLAIN)) {
                handleTextShare(selectedUsers);

            }
            //Handle one ITEM
            else if (type.startsWith(MimeTypes.IMAGE)) {
                handleImageShare(selectedUsers);

            } else if (type.startsWith(MimeTypes.VIDEO)) {
                handleVideoShare();

            } else if (type.startsWith(MimeTypes.CONTACT)) {
                Uri uri = (Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM);

                List<VCard> vcards = ContactUtils.getContactAsVcard(this, uri);


                List<ExpandableContact> contactNameList = ContactUtils.getContactNamesFromVcard(vcards);

                Intent mIntent = new Intent(this, SelectContactNumbersActivity.class);
                mIntent.putParcelableArrayListExtra(IntentUtils.EXTRA_CONTACT_LIST, (ArrayList<? extends Parcelable>) contactNameList);
                startActivityForResult(mIntent, PICK_NUMBERS_REQUEST);


            } else if (type.startsWith(MimeTypes.AUDIO)) {
                handleIncomingAudio();

            }

            //Multiple Items
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleImageShare(adapter.getSelectedForwardedUsers());
            }

        }
    }


    private void handleIncomingAudio() {
        Uri uri = (Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        String filePath = RealPathUtil.getRealPath(this, uri);
        if (filePath == null) {
            showFileNotFoundToast();
            return;
        }
        String length = Util.getVideoLength(this, filePath);

        if (adapter.getSelectedForwardedUsers().size() > 1) {
            for (User user : adapter.getSelectedForwardedUsers()) {
                Message message = new MessageCreator.Builder(user, MessageType.SENT_AUDIO).path(filePath).duration(length).build();
                ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());

            }
            finish();

        } else {
            User user = adapter.getSelectedForwardedUsers().get(0);
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(IntentUtils.EXTRA_REAL_PATH, filePath);
            intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.AUDIO);
            intent.putExtra(IntentUtils.UID, user.getUid());
            startTheActivityWithFlags(intent);
            finish();
        }


    }

    private void handleVideoShare() {
        Uri videoUri = (Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        String filePath = RealPathUtil.getRealPath(this, videoUri);
        if (filePath == null) {
            showFileNotFoundToast();
            return;
        }
        if (adapter.getSelectedForwardedUsers().size() > 1) {

            for (User user : adapter.getSelectedForwardedUsers()) {
                Message message = new MessageCreator.Builder(user, MessageType.SENT_VIDEO).context(this).path(filePath).build();
                ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());
                showSendingToast();
                finish();
            }

        } else {
            Intent intent = new Intent(this, ChatActivity.class);
            User user = adapter.getSelectedForwardedUsers().get(0);
            intent.putExtra(IntentUtils.EXTRA_REAL_PATH, filePath);
            intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.VIDEO);
            intent.putExtra(IntentUtils.UID, user.getUid());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startTheActivityWithFlags(intent);
            finish();
        }

    }

    private void showSendingToast() {
        Toast.makeText(this, R.string.sending_messages, Toast.LENGTH_SHORT).show();
    }

    private void showFileNotFoundToast() {
        Toast.makeText(this, R.string.could_not_get_this_file, Toast.LENGTH_SHORT).show();
    }


    private void handleImageShare(List<User> selectedUsers) {
        ArrayList<Uri> imageUris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);


        //Multiple Images
        if (imageUris != null) {

            if (selectedUsers.size() > 1) {
                for (User user : selectedUsers) {

                    for (Uri uri : imageUris) {
                        String filePath = RealPathUtil.getRealPath(this, uri);
                        if (filePath != null) {
                            Message message = new MessageCreator.Builder(user, MessageType.SENT_IMAGE).path(filePath).fromCamera(false).build();
                            ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());
                        } else {
                            showFileNotFoundToast();
                        }
                    }
                }
                finish();
            } else {
                ArrayList<String> realPathList = (ArrayList<String>) getRealPathList(imageUris);
                User user = selectedUsers.get(0);
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(IntentUtils.EXTRA_REAL_PATH_LIST, realPathList);
                intent.putExtra(IntentUtils.UID, user.getUid());
                intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.IMAGE);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startTheActivityWithFlags(intent);
                finish();
            }

        } else {
            //One Image
            Uri imageUri = (Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            String filePath = RealPathUtil.getRealPath(this, imageUri);
            if (filePath == null) {
                showFileNotFoundToast();
                return;
            }

            if (selectedUsers.size() > 1) {
                for (User user : selectedUsers) {
                    Message message = new MessageCreator.Builder(user, MessageType.SENT_IMAGE).path(filePath).fromCamera(false).build();
                    ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());
                }
                finish();
            } else {
                User user = selectedUsers.get(0);
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(IntentUtils.EXTRA_REAL_PATH, filePath);
                intent.putExtra(IntentUtils.UID, user.getUid());
                intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.IMAGE);
                startTheActivityWithFlags(intent);
                finish();
            }


        }


    }

    private List<String> getRealPathList(ArrayList<Uri> imageUris) {
        List<String> realPathList = new ArrayList<>();
        for (Uri uri : imageUris) {
            realPathList.add(RealPathUtil.getRealPath(this, uri));
        }
        return realPathList;
    }

    private void startTheActivityWithFlags(Intent intent) {
        TaskStackBuilder sBuilder = TaskStackBuilder.create(this);
        sBuilder.addNextIntentWithParentStack(new Intent(this, MainActivity.class));
        sBuilder.addNextIntent(intent);
        sBuilder.startActivities();
    }


    private void handleTextShare(List<User> selectedUsers) {

        String sharedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText == null)
            return;

        if (selectedUsers.size() > 1) {
            for (User selectedUser : selectedUsers) {
                Message message = new MessageCreator.Builder(selectedUser, MessageType.SENT_TEXT).text(sharedText).build();
                ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());
            }
            showSendingToast();
            finish();
        } else {
            User user = selectedUsers.get(0);
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(IntentUtils.EXTRA_SHARED_TEXT, sharedText);
            intent.putExtra(IntentUtils.UID, user.getUid());
            intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.TEXT_PLAIN);
            startTheActivityWithFlags(intent);
            finish();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();

        return super.onOptionsItemSelected(item);
    }


    private void init() {
        toolbarForward = findViewById(R.id.toolbar_forward);
        rvForward = findViewById(R.id.rv_forward);
        rootView = findViewById(R.id.root_view);
        tvSelectedContact = findViewById(R.id.tv_selected_contact);
        fabSend = findViewById(R.id.fab_send);

        mSnackbar = new DevlomiSnackbar(rootView, getResources().getColor(R.color.blue));

        getListOfUsers();

        selectedForwardedUsers = new ArrayList<>();


    }

    private void getListOfUsers() {
        usersList = RealmHelper.getInstance().getForwardList();
    }

    private void initAdapter() {
        adapter = new ForwardAdapter(usersList, selectedForwardedUsers, true, this, null);
        rvForward.setLayoutManager(new LinearLayoutManager(this));
        rvForward.setAdapter(adapter);
    }


    public void updateSelectedUsers() {

        String userName = "";
        for (User user1 : adapter.getSelectedForwardedUsers()) {
            userName += user1.getUserName() + SEPARATOR;
        }


        tvSelectedContact.setText(StringUtils.removeExtraSeparators(userName, SEPARATOR));
        mSnackbar.getSnackbarTextView().setText(StringUtils.removeExtraSeparators(userName, SEPARATOR));


    }


    public void showSnackbar() {
        if (!mSnackbar.isShowing()) {
            mSnackbar.showSnackBar();
            fabSend.show();
        }
    }

    public void hideSnackbar() {
        tvSelectedContact.setText("");
        fabSend.hide();
        mSnackbar.dismissSnackbar();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_NUMBERS_REQUEST) {
            if (resultCode == RESULT_OK) {
                List<ExpandableContact> selectedContacts = data.getParcelableArrayListExtra(IntentUtils.EXTRA_CONTACT_LIST);
                handleContacts(selectedContacts);

            } else {
                Toast.makeText(this, R.string.not_contact_selected, Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    private void handleContacts(List<ExpandableContact> selectedContacts) {
        if (adapter.getSelectedForwardedUsers().size() > 1) {

            for (User user : adapter.getSelectedForwardedUsers()) {
                List<Message> messages = new MessageCreator.Builder(user, MessageType.SENT_CONTACT).contacts(selectedContacts).buildContacts();
                for (Message message : messages) {
                    ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());

                }
            }

            showSendingToast();

        } else {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putParcelableArrayListExtra(IntentUtils.EXTRA_CONTACT_LIST, (ArrayList<? extends Parcelable>) selectedContacts);
            User user = adapter.getSelectedForwardedUsers().get(0);
            intent.putExtra(IntentUtils.UID, user.getUid());
            intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.CONTACT);
            startTheActivityWithFlags(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_CANCELED);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_forward, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //notify new group activity if it's not null
                if (searchCallback != null)
                    searchCallback.onQuery(newText);

                if (!newText.trim().isEmpty()) {
                    RealmResults<User> users = RealmHelper.getInstance().searchForUser(newText, true);
                    adapter = new ForwardAdapter(users, selectedForwardedUsers, true, ForwardActivity.this, null);
                    rvForward.setAdapter(adapter);
                } else {
                    adapter = new ForwardAdapter(usersList, selectedForwardedUsers, true, ForwardActivity.this, null);
                    rvForward.setAdapter(adapter);
                }
                return false;
            }

        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchCallback.onSearchClose();
                adapter = new ForwardAdapter(usersList, selectedForwardedUsers, true, ForwardActivity.this, null);
                rvForward.setAdapter(adapter);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


}
