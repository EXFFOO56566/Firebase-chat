package com.devlomi.fireapp.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.devlomi.fireapp.R;
import com.devlomi.fireapp.events.UserImageDownloadedEvent;
import com.devlomi.fireapp.model.constants.GroupEventTypes;
import com.devlomi.fireapp.model.realms.GroupEvent;
import com.devlomi.fireapp.model.realms.User;
import com.devlomi.fireapp.utils.BitmapUtils;
import com.devlomi.fireapp.utils.CropImageRequest;
import com.devlomi.fireapp.utils.DirManager;
import com.devlomi.fireapp.utils.FileUtils;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.GroupManager;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.RealmHelper;
import com.devlomi.fireapp.utils.SharedPreferencesManager;
import com.theartofdev.edmodo.cropper.CropImage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

public class ProfilePhotoActivity extends AppCompatActivity {

    private Toolbar toolbarProfile;
    private ImageView profileFullScreen;

    User user;
    FireManager.OnUpdateUserPhoto onUpdateUserPhoto;
    String profilePhotoPath;
    private int IMAGE_QUALITY_COMPRESS = 30;
    private boolean isGroup = false;
    private boolean isBroadcast = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_photo);
        toolbarProfile = findViewById(R.id.toolbar_profile);
        profileFullScreen = findViewById(R.id.profile_full_screen);


        setSupportActionBar(toolbarProfile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //if user viewing other user's photo
        if (getIntent().hasExtra(IntentUtils.UID)) {

            String uid = getIntent().getStringExtra(IntentUtils.UID);
            //getting the user from realm because the image may not be updated while fetching the user in the list
            user = RealmHelper.getInstance().getUser(uid);
            isBroadcast = user.isBroadcastBool();
            isGroup = user.isGroupBool();
            profilePhotoPath = user.getUserLocalPhoto();
            getSupportActionBar().setTitle(user.getUserName());

            //if user is viewing his photo
        } else {
            String imgPath = getIntent().getStringExtra(IntentUtils.EXTRA_PROFILE_PATH);
            getSupportActionBar().setTitle(R.string.profile_photo);
            Glide.with(this).load(imgPath)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(profileFullScreen);
        }
    }

    private void loadImage(final String profilePhotoPath) {
        if (user == null) return;
        if (isBroadcast) {
            Drawable drawable = AppCompatResources.getDrawable(this, R.drawable.ic_broadcast_with_bg);
            profileFullScreen.setImageDrawable(drawable);
            //if the profilePhotoPath in Database is not exists
        } else if (profilePhotoPath == null) {
            //show the thumgImg while getting full Image
            if (user.getThumbImg() != null) {
                Glide.with(this).load(BitmapUtils.encodeImageAsBytes(user.getThumbImg())).asBitmap().into(profileFullScreen);
            }
            //start getting full image
            FireManager.downloadUserPhoto(user.getUid(), user.getUserLocalPhoto(), isGroup, onUpdateUserPhoto);
        } else {
            //otherwise check if the image stored in device
            //if it's stored then show it
            if (FileUtils.isFileExists(profilePhotoPath)) {
                Glide.with(this).load(profilePhotoPath).into(profileFullScreen);
            } else {
                //otherwise download the image
                FireManager.downloadUserPhoto(user.getUid(), user.getUserLocalPhoto(), isGroup, onUpdateUserPhoto);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile_photo, menu);
        //show edit profile button if the user is viewing his photo or if group admin wants to update group profile photo

        if (isGroup && FireManager.isAdmin(user.getGroup().getAdminsUids()) || !getIntent().hasExtra(IntentUtils.UID)) {
            menu.findItem(R.id.edit_profile_item).setVisible(true);

        }


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.edit_profile_item) {
            editProfilePhoto();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        onUpdateUserPhoto = new FireManager.OnUpdateUserPhoto() {
            @Override
            public void onSuccess(String photoPath) {
                try {
                    //load the image once it's downloaded
                    Glide.with(ProfilePhotoActivity.this).load(photoPath)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(profileFullScreen);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        loadImage(profilePhotoPath);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //free up resources and avoid memory leaks
        onUpdateUserPhoto = null;
    }

    private void editProfilePhoto() {
        pickImages();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                final File file = DirManager.generateUserProfileImage();

                //it is not recommended to change IMAGE_QUALITY_COMPRESS as it may become
                //too big and this may cause the app to crash due to large thumbImg
                //therefore the thumb img may became un-parcelable through activities
                BitmapUtils.compressImage(resultUri.getPath(), file, IMAGE_QUALITY_COMPRESS);

                if (isGroup) {
                    GroupManager.changeGroupImage(file.getPath(), user.getUid(), new FireManager.OnComplete() {
                        @Override
                        public void onComplete(boolean isSuccessful) {
                            if (isSuccessful) {

                                try {
                                    Glide.with(ProfilePhotoActivity.this)
                                            .load(file)
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true)
                                            .into(profileFullScreen);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                new GroupEvent(SharedPreferencesManager.getPhoneNumber(), GroupEventTypes.GROUP_SETTINGS_CHANGED, null).createGroupEvent(user, null);

                                Toast.makeText(ProfilePhotoActivity.this, R.string.image_changed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    FireManager.updateMyPhoto(file.getPath(), new FireManager.OnComplete() {
                        @Override
                        public void onComplete(boolean isSuccessful) {
                            if (isSuccessful) {
                                //skip cache because the img name will still the same
                                //and glide will think this is same image,therefore it
                                //will still show the old image
                                try {
                                    Glide.with(ProfilePhotoActivity.this)
                                            .load(file)
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true)
                                            .into(profileFullScreen);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
//
                                Toast.makeText(ProfilePhotoActivity.this, R.string.image_changed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, R.string.could_not_get_this_image, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickImages() {
        CropImageRequest.getCropImageRequest().start(this);
    }

    //load the image if it's downloaded by previous activity or service
    @Subscribe
    public void userImageDownloaded(UserImageDownloadedEvent event) {
        String imagePath = event.getPath();
        Glide.with(this).load(imagePath).into(profileFullScreen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
}
