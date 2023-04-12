package com.devlomi.fireapp.utils;

import android.os.Environment;

import com.devlomi.fireapp.R;
import com.devlomi.fireapp.model.constants.MessageType;
import com.devlomi.fireapp.model.constants.StatusType;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Devlomi on 12/08/2017.
 */

//this class will manage all create file name
//and it contains all folder paths for all types (image,video etc..)
public class DirManager {
    private static final String EXTENSION_JPG = ".jpg";
    private static final String EXTENSION_MP4 = ".mp4";
    private static final String EXTENSION_WAV = ".wav";
    private static final String APP_FOLDER_NAME = MyApp.context().getString(R.string.app_folder_name);


    //Main App Folder: /sdcard/FireApp/
    public static String mainAppFolder() {
        File file = new File(Environment.getExternalStorageDirectory() + "/" + APP_FOLDER_NAME + "/");
        //if the directory is not exists create it
        if (!file.exists())
            file.mkdir();


        return file.getAbsolutePath();
    }

    //the sent images : /sdcard/FireApp/FireAppImages/Sent
    public static String getSentImagesFolder() {
        File file = new File(mainAppFolder() + "/" + APP_FOLDER_NAME + " " + "Images/Sent");
        if (!file.exists()) {
            file.mkdirs();
        }
        createNoMediaFile(file);


        return file.getAbsolutePath();
    }

    public static String receivedImagesDir() {
        File file = new File(mainAppFolder() + "/" + APP_FOLDER_NAME + " " + "Images/");
        if (!file.exists())
            file.mkdirs();


        return file.getAbsolutePath();
    }


    //get stored profile photos directory : /sdcard/FireApp/ProfilePhotos
    public static String getUserProfilePhotoFolder() {
        File file = new File(mainAppFolder() + "/" + APP_FOLDER_NAME + " " + "Profile Photos");
        if (!file.exists()) {
            file.mkdirs();
        }
        createNoMediaFile(file);

        return file.getAbsolutePath();
    }


    // /sdcard/FireApp/FireApp Files/
    public static String getReceivedFilesFolder() {
        File file = new File(mainAppFolder() + "/" + APP_FOLDER_NAME + " " + "Files");
        if (!file.exists())
            file.mkdirs();

        return file.getAbsolutePath();
    }


    // /sdcard/FireApp/FireApp Files/Sent
    public static String getSentFilesFolder() {
        File file = new File(mainAppFolder() + "/" + APP_FOLDER_NAME + " " + "Files/Sent");
        if (!file.exists())
            file.mkdirs();

        return file.getAbsolutePath();
    }


    // /sdcard/FireApp/FireApp Audio/Sent
    public static String getSentAudioFolder() {
        File file = new File(mainAppFolder() + "/" + APP_FOLDER_NAME + " " + "Audio/Sent");
        if (!file.exists()) {
            file.mkdirs();
        }
        createNoMediaFile(file);

        return file.getAbsolutePath();
    }

    // /sdcard/FireApp/FireApp Audio/
    public static String getReceivedAudioFolder() {
        File file = new File(mainAppFolder() + "/" + APP_FOLDER_NAME + " " + "Audio");
        if (!file.exists())
            file.mkdirs();

        return file.getAbsolutePath();
    }


    public static String sentVoiceMessageDir() {
        File file = new File(mainAppFolder() + "/" + APP_FOLDER_NAME + " " + "VoiceMessage/Sent/");
        if (!file.exists()) {
            file.mkdirs();
        }
        createNoMediaFile(file);

        return file.getAbsolutePath();
    }

    public static String voiceMessagesReceived() {
        File file = new File(mainAppFolder() + "/" + APP_FOLDER_NAME + " " + "VoiceMessage");
        if (!file.exists())
            file.mkdirs();


        createNoMediaFile(file);

        return file.getPath();
    }


    public static String receivedVideoDir() {
        File file = new File(mainAppFolder() + "/" + APP_FOLDER_NAME + " " + "Video/");
        if (!file.exists())
            file.mkdirs();

        return file.getAbsolutePath();
    }

    public static String sentVideoDir() {
        File file = new File(mainAppFolder() + "/" + APP_FOLDER_NAME + " " + "Video/Sent");
        if (!file.exists()) {
            file.mkdirs();
        }
        createNoMediaFile(file);

        return file.getPath();
    }

    //the user's image file when he wants to change his photo
    public static File getMyPhotoPath() {
        File file = new File(mainAppFolder(), "user-img.jpg");
        //delete old file
        file.delete();
        return file;
    }

    public static String getReceivedStatusFolder() {
        File file = new File(mainAppFolder() + "/" + ".Statuses");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getPath();
    }

    private static String getWallpapaerFolder() {
        File file = new File(mainAppFolder() + "/" + "Wallpaper");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    public static File genereateWallpaperFile() {
        return new File(getWallpapaerFolder() + "/" + UUID.randomUUID().toString() + EXTENSION_JPG);
    }

    //this will generate a new file name it will generate the date as year month day Millisecond
    //more info https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
    // the final output would be like this IMG-201804301230.jpg
    public static String generateNewName(int type) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddSSSS", Locale.US); //the Locale us is to use english numbers
        return getFileTypeString(type) + "-" + sdf.format(date);
    }


    //create the file to save the user profile into it :/sdcard/FireApp/ProfilePhotos/IMG-"DATE".jpg
    public static File generateUserProfileImage() {
        return new File(getUserProfilePhotoFolder() + "/" + generateNewName(MessageType.SENT_IMAGE) + EXTENSION_JPG);
    }

    //this will generate new file with the needed directory
    public static File generateFile(int type) {
        File file;
        switch (type) {
            case MessageType.SENT_IMAGE:
                file = new File(getSentImagesFolder() + "/" + generateNewName(type) + EXTENSION_JPG);
                break;

            case MessageType.RECEIVED_IMAGE:
                file = new File(receivedImagesDir() + "/" + generateNewName(type) + EXTENSION_JPG);
                break;


            case MessageType.SENT_VIDEO:
                file = new File(sentVideoDir() + "/" + generateNewName(type) + EXTENSION_MP4);
                break;

            case MessageType.RECEIVED_VIDEO:
                file = new File(receivedVideoDir() + "/" + generateNewName(type) + EXTENSION_MP4);
                break;

            case MessageType.SENT_VOICE_MESSAGE:
                file = new File(sentVoiceMessageDir() + "/" + generateNewName(type) + EXTENSION_WAV);
                break;

            case MessageType.RECEIVED_VOICE_MESSAGE:
                file = new File(voiceMessagesReceived() + "/" + generateNewName(type) + EXTENSION_WAV);
                break;

            default:
                file = new File(getSentImagesFolder() + "/" + generateNewName(type) + ".jpg");
                break;


        }
        //create dirs if not exists
        if (!file.exists())
            file.getParentFile().mkdirs();

        return file;
    }


    //this used because we need the extension of the file so we can save the file with its extension
    //whether it's MP3,WAV,etc..
    public static File generateAudioFile(int type, String fileExtension) {
        File file;

        if (type == MessageType.SENT_AUDIO)
            file = new File(getSentAudioFolder() + "/" + generateNewName(type) + "." + fileExtension);

        else
            file = new File(getReceivedAudioFolder() + "/" + generateNewName(type) + "." + fileExtension);


        if (!file.exists())
            file.getParentFile().mkdirs();

        return file;
    }


    //here we don't want to generate a new name,instead the name will be formatted from sender
    public static File generateFileForFilesType(int type, String fileName) {
        if (type == MessageType.SENT_FILE)
            return new File(getSentFilesFolder() + "/" + fileName);

        return new File(getReceivedFilesFolder() + "/" + fileName);
    }


    //get file type for the file name
    private static String getFileTypeString(int type) {
        switch (type) {
            case MessageType.SENT_IMAGE:
            case MessageType.RECEIVED_IMAGE:
                return "IMG";


            case MessageType.SENT_VIDEO:
            case MessageType.RECEIVED_VIDEO:
                return "VID";


            case MessageType.SENT_AUDIO:
            case MessageType.RECEIVED_AUDIO:
                return "AUD";


            case MessageType.SENT_VOICE_MESSAGE:
            case MessageType.RECEIVED_VOICE_MESSAGE:
                //push to talk (voice message)
                return "PTT";


            default:
                return "FILE";


        }

    }

    //this will prevent the gallery or audio player app in the device from showing "sent" files (images,videos,audio,voice messages) by our app
    //basically just hide them
    public static void createNoMediaFile(File folderPath) {
        File file = new File(folderPath + "/" + ".nomedia");
        try {
            if (!file.exists())
                file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getReceivedStatusFile(String statusId, int type) {
        //if the status was downloaded from other users we will save it in the hidden folder ".Statuses"
        //otherwise we will save as a normal received image or video
        if (type == StatusType.VIDEO)
            return new File(DirManager.getReceivedStatusFolder() + "/" + statusId + ".mp4");
        else
            return new File(DirManager.getReceivedStatusFolder() + "/" + statusId + ".jpg");


    }

    public static File getDatabasesFolder() {
        //if the status was downloaded from other users we will save it in the hidden folder ".Statuses"
        //otherwise we will save as a normal received image or video
        File file = new File(mainAppFolder() + "/" + "Databases");

        if (!file.exists())
            file.mkdirs();

        return file;


    }



}
