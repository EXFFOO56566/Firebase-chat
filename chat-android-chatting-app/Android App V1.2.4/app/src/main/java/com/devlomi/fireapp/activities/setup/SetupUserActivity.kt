package com.devlomi.fireapp.activities.setup

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.devlomi.fireapp.R
import com.devlomi.fireapp.activities.main.MainActivity
import com.devlomi.fireapp.common.ScopedActivity
import com.devlomi.fireapp.common.extensions.toDeffered
import com.devlomi.fireapp.common.extensions.toDefferedWithTask
import com.devlomi.fireapp.events.FetchingUserGroupsAndBroadcastsFinished
import com.devlomi.fireapp.exceptions.BackupFileMismatchedException
import com.devlomi.fireapp.utils.*
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber
import io.realm.exceptions.RealmMigrationNeededException
import kotlinx.android.synthetic.main.activity_setup_user.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.io.IOException
import java.util.*

class SetupUserActivity : ScopedActivity() {


    internal var storedPhotoUrl: String? = null
    internal var choosenPhoto: String? = null
    private var thumbImg: String? = null
    private var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_user)


        fetchUserPhoto()


        fab_setup_user.setOnClickListener { completeSetup() }
        user_img_setup.setOnClickListener { pickImage() }

        //On Done Keyboard Button Click
        et_username_setup!!.setOnEditorActionListener(TextView.OnEditorActionListener
        { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                completeSetup()
                return@OnEditorActionListener true
            }
            false
        })


        if (RealmBackupRestore.isBackupFileExists()) {
            check_text_view_number.visibility = View.VISIBLE
        } else {
            check_text_view_number.visibility = View.GONE
        }
    }


    private fun fetchUserPhoto() {
        launch {
            try {


                val dataSnapshot = FireConstants.usersRef.child(FireManager.getUid())
                        .child("photo").toDeffered().await()

                if (dataSnapshot.value == null) {
                    storedPhotoUrl = ""
                    progress_bar_setup_user_img.visibility = View.GONE
                } else {
                    //otherwise get the stored user image url
                    storedPhotoUrl = dataSnapshot.getValue(String::class.java)


                    //load the image
                    //we are using listener to determine when the image loading is finished
                    //so we can hide the progressBar
                    Glide.with(this@SetupUserActivity).load(storedPhotoUrl)
                            .listener(object : RequestListener<String, GlideDrawable> {
                                override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                                    progress_bar_setup_user_img.visibility = View.GONE
                                    return false; }

                                override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                                    progress_bar_setup_user_img.visibility = View.GONE
                                    return false
                                }

                            }).into(user_img_setup)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    private fun completeSetup() {
        //check if user not entered his username
        if (TextUtils.isEmpty(et_username_setup!!.text.toString())) {
            et_username_setup!!.error = getString(R.string.username_is_empty)
        } else {


            val dialog = ProgressDialog(this@SetupUserActivity)
            dialog.setMessage(getString(R.string.loading))
            dialog.setCancelable(false)
            dialog.show()

            if (check_text_view_number.visibility == View.VISIBLE && check_text_view_number.isChecked) {
                try {
                    RealmBackupRestore(this).restore()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, R.string.error_restoring_backup, Toast.LENGTH_SHORT).show()
                } catch (e: RealmMigrationNeededException) {
                    e.printStackTrace()
                    Toast.makeText(this, R.string.error_restoring_backup, Toast.LENGTH_SHORT).show()
                } catch (e: BackupFileMismatchedException) {
                    e.printStackTrace()
                    Toast.makeText(this, R.string.backup_file_mismatched, Toast.LENGTH_SHORT).show()
                }

            }

            try {

                // if there user does not choose a new photo
                if (choosenPhoto == null) {
                    //if stored photo on database not exists
                    //then get the defaultUserProfilePhoto from database and download it
                    if (storedPhotoUrl != null && storedPhotoUrl == "") {
                        getDefaultUserProfilePhoto(dialog)
                        //if datasnapshot is not ready yet or there is a connection issue
                    } else if (storedPhotoUrl == null) {
                        dialog.dismiss()
                        showSnackbar()
                    } else {
                        //otherwise get the stored user image from database
                        //download it and save it and save user info then finish setup
                        getUserImage(dialog)

                    }
                    //user picked an image
                    //upload it  then save user info and finish setup
                } else {
                    uploadUserPhoto(dialog)
                }
            } catch (e: Exception) {

            }

        }
    }

    private fun uploadUserPhoto(dialog: ProgressDialog) {
        launch {
            val task = FireConstants.imageProfileRef.child(UUID.randomUUID().toString() + ".jpg")
                    .putFile(Uri.fromFile(File(choosenPhoto)))
                    .toDefferedWithTask()
                    .await()
            if (task.isSuccessful) {
                val imageUrl = task.result!!.downloadUrl.toString()
                val userInfoHashmap = getUserInfoHashmap(et_username_setup!!.text.toString(), imageUrl, choosenPhoto)

                val isSuccessful = FireConstants.usersRef.child(FireManager.getUid()!!)
                        .updateChildren(userInfoHashmap)
                        .toDeffered().await()

                dialog.dismiss()
                if (isSuccessful) {
                    saveUserInfo(File(choosenPhoto))
                } else
                    showSnackbar()

            } else
                showSnackbar()
        }
    }


    private fun getUserImage(dialog: ProgressDialog) {
        launch {

            val file = DirManager.getMyPhotoPath()
            val isSuccessful = FirebaseStorage.getInstance().getReferenceFromUrl(storedPhotoUrl!!).getFile(file).toDeffered().await()
            if (isSuccessful) {
                val userInfoHashmap = getUserInfoHashmap(et_username_setup!!.text.toString(), storedPhotoUrl)

                val isSuccessful = FireConstants.usersRef.child(FireManager.getUid()!!)
                        .updateChildren(userInfoHashmap)
                        .toDeffered()
                        .await()

                dialog.dismiss()
                if (isSuccessful) {
                    saveUserInfo(file)
                } else
                    showSnackbar()

            } else
                showSnackbar()
        }
    }


    private fun getDefaultUserProfilePhoto(dialog: ProgressDialog) {
        launch {
            try {
                val photoFile = DirManager.getMyPhotoPath()
                val dataSnapshot = FireConstants.mainRef.child("defaultUserProfilePhoto").toDeffered().await()
                if (dataSnapshot.value == null) {
                    dialog.dismiss()
                    showSnackbar()
                } else {
                    val photoUrl = dataSnapshot.getValue(String::class.java)
                    //download image
                    val isSuccessful = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl!!)
                            .getFile(photoFile)
                            .toDeffered()
                            .await()

                    if (isSuccessful) {
                        //save user info and finish setup
                        val userName = et_username_setup!!.text.toString()
                        val map = getUserInfoHashmap(userName, photoUrl, photoFile.path)
                        val isSuccess = FireConstants.usersRef.child(FireManager.getUid()!!).updateChildren(map).toDeffered().await()
                        dialog.dismiss()
                        if (isSuccess) {
                            saveUserInfo(photoFile)
                        } else {
                            showSnackbar()
                        }


                    } else
                        showSnackbar()

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun showSnackbar() {
        Snackbar.make(findViewById(android.R.id.content), R.string.no_internet_connection, Snackbar.LENGTH_SHORT).show()
    }

    private fun getUserInfoHashmap(userName: String, photoUrl: String?, filePath: String? = null): HashMap<String, Any> {
        val map = hashMapOf<String, Any>()
        map["photo"] = photoUrl!!
        map["name"] = userName
        map["phone"] = FireManager.getPhoneNumber()
        val defaultStatus = String.format(getString(R.string.default_status), getString(R.string.app_name))
        map["status"] = defaultStatus
        val appVersion = AppVerUtil.getAppVersion(this)
        if (appVersion != "")
            map["ver"] = appVersion

        //create thumbImg and original image and compress them if the user chosen a new photo
        if (filePath != null) {
            val circleBitmap = BitmapUtils.getCircleBitmap(BitmapUtils.convertFileImageToBitmap(filePath))
            thumbImg = BitmapUtils.decodeImageAsPng(circleBitmap)
            map["thumbImg"] = thumbImg!!
        }

        return map
    }


    //save user info locally
    private fun saveUserInfo(photoFile: File) {
        SharedPreferencesManager.saveMyPhoto(photoFile.path)
        if (thumbImg == null) {
            val circleBitmap = BitmapUtils.getCircleBitmap(BitmapUtils.convertFileImageToBitmap(photoFile.path))
            thumbImg = BitmapUtils.decodeImageAsPng(circleBitmap)
        }
        SharedPreferencesManager.saveMyThumbImg(thumbImg)
        SharedPreferencesManager.saveMyUsername(et_username_setup!!.text.toString())
        SharedPreferencesManager.savePhoneNumber(FireManager.getPhoneNumber())
        SharedPreferencesManager.saveMyStatus(getString(R.string.default_status))
        SharedPreferencesManager.setAppVersionSaved(true)
        saveCountryCode()

        //show progress while getting user groups
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage(resources.getString(R.string.loading))
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()

        ServiceHelper.fetchUserGroupsAndBroadcasts(this)

    }


    @Subscribe
    fun fetchingGroupsAndBroadcastsComplete(event: FetchingUserGroupsAndBroadcastsFinished) {
        SharedPreferencesManager.setUserInfoSaved(true)
        progressDialog!!.dismiss()
        startMainActivity()
    }


    //save country code to shared preferences (see ContactUtils class for more info)
    private fun saveCountryCode() {
        val phoneUtil = PhoneNumberUtil.createInstance(this)
        val numberProto: Phonenumber.PhoneNumber
        try {
            //get the countryName code Like "+1 or +44 etc.." from the user number
            //so if the user number is like +1 444-444-44 we will save only "+1"
            numberProto = phoneUtil.parse(FireManager.getPhoneNumber(), "")
            val countryCode = phoneUtil.getRegionCodeForNumber(numberProto)
            SharedPreferencesManager.saveCountryCode(countryCode)
        } catch (e: NumberParseException) {
            e.printStackTrace()
        }

    }

    private fun pickImage() {
        CropImageRequest.getCropImageRequest().start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri


                val file = DirManager.getMyPhotoPath()
                try {
                    //copy image to the App Folder
                    FileUtils.copyFile(resultUri.path, file)

                    Glide.with(this).load(file).into(user_img_setup!!)
                    choosenPhoto = file.path
                    progress_bar_setup_user_img!!.visibility = View.GONE
                } catch (e: IOException) {
                    e.printStackTrace()
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, R.string.could_not_get_this_image, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        EventBus.getDefault().register(this)
        super.onResume()
        if (SharedPreferencesManager.isFetchedUserGroups()) {
            if (progressDialog != null)
                progressDialog!!.dismiss()
            SharedPreferencesManager.setUserInfoSaved(true)
            startMainActivity()
        }
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }
}

