package com.devlomi.fireapp.activities.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.devlomi.fireapp.R;
import com.devlomi.fireapp.activities.ProfilePhotoActivity;
import com.devlomi.fireapp.utils.BitmapUtils;
import com.devlomi.fireapp.utils.CropImageRequest;
import com.devlomi.fireapp.utils.DirManager;
import com.devlomi.fireapp.utils.FireManager;
import com.devlomi.fireapp.utils.IntentUtils;
import com.devlomi.fireapp.utils.NetworkHelper;
import com.devlomi.fireapp.utils.SharedPreferencesManager;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Devlomi on 25/03/2018.
 */

public class ProfilePreferenceFragment extends PreferenceFragment {
    public static final int PICK_IMAGE_REQUEST = 4951;
    private CircleImageView imageViewUserProfile;
    private ImageButton imageButtonChangeUserProfile;
    private ImageButton imageButtonEditUsername;
    private TextView tvUsername;
    private TextView tvStatus;
    private TextView tvPhoneNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void pickImages() {
        CropImageRequest.getCropImageRequest().start(getActivity(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fagment_profile_settings, container, false);

        imageViewUserProfile = view.findViewById(R.id.image_view_user_profile);
        imageButtonChangeUserProfile = view.findViewById(R.id.image_button_change_user_profile);
        tvUsername = view.findViewById(R.id.tv_username);
        imageButtonEditUsername = view.findViewById(R.id.image_button_edit_username);
        tvStatus = view.findViewById(R.id.tv_status);
        tvPhoneNumber = view.findViewById(R.id.tv_phone_number);


        String userName = SharedPreferencesManager.getUserName();
        String status = SharedPreferencesManager.getStatus();
        String phoneNumber = SharedPreferencesManager.getPhoneNumber();
        final String myPhoto = SharedPreferencesManager.getMyPhoto();
        tvStatus.setText(status);
        tvUsername.setText(userName);
        tvPhoneNumber.setText(phoneNumber);


        imageViewUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfilePhotoActivity.class);
                String transName = "profile_photo_trans";

                intent.putExtra(IntentUtils.EXTRA_PROFILE_PATH, myPhoto);
                startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), v, transName).toBundle());
            }
        });

        imageButtonChangeUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImages();
            }
        });

        imageButtonEditUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditTextDialog(getString(R.string.enter_your_name), new EditTextDialogListener() {
                    @Override
                    public void onOk(final String text) {
                        if (TextUtils.isEmpty(text)) {
                            Toast.makeText(getActivity(), R.string.username_is_empty, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (NetworkHelper.isConnected(getActivity())) {
                            FireManager.updateMyUserName(text, new FireManager.OnComplete() {
                                @Override
                                public void onComplete(boolean isSuccessful) {
                                    if (isSuccessful) {
                                        SharedPreferencesManager.saveMyUsername(text);
                                        tvUsername.setText(text);
                                    } else {
                                        Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                        } else {
                            Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        tvStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditTextDialog(getString(R.string.enter_your_status), new EditTextDialogListener() {
                    @Override
                    public void onOk(final String text) {
                        if (TextUtils.isEmpty(text)) {
                            Toast.makeText(getActivity(), R.string.status_is_empty, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (NetworkHelper.isConnected(getActivity())) {
                            FireManager.updateMyStatus(text, new FireManager.OnComplete() {
                                @Override
                                public void onComplete(boolean isSuccessful) {
                                    if (isSuccessful) {

                                        SharedPreferencesManager.saveMyStatus(text);
                                        tvStatus.setText(text);
                                    } else {
                                        Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                        } else {
                            Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        Glide.with(getActivity()).load(Uri.fromFile(new File(myPhoto)))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageViewUserProfile);

        return view;

    }

    private void showEditTextDialog(String message, final EditTextDialogListener listener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        final EditText edittext = new EditText(getActivity());
        alert.setMessage(message);


        alert.setView(edittext);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {


                if (listener != null)
                    listener.onOk(edittext.getText().toString());


            }
        });

        alert.setNegativeButton(R.string.cancel, null);

        alert.show();


    }


    private interface EditTextDialogListener {
        void onOk(String text);
    }

    public interface ImageSelectedListener {
        void onSelect(String imagePath);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();


                final File file = DirManager.getMyPhotoPath();
                BitmapUtils.compressImage(resultUri.getPath(), file, 30);


                FireManager.updateMyPhoto(file.getPath(), new FireManager.OnComplete() {
                    @Override
                    public void onComplete(boolean isSuccessful) {
                        if (isSuccessful) {
                            try {
                                Glide.with(getActivity())
                                        .load(file)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .into(imageViewUserProfile);
                                Toast.makeText(getActivity(), R.string.image_changed, Toast.LENGTH_SHORT).show();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


//

                        }
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}

