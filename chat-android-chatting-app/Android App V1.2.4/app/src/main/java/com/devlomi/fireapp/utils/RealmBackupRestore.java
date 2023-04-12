package com.devlomi.fireapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.devlomi.fireapp.exceptions.BackupFileMismatchedException;
import com.devlomi.fireapp.model.realms.Broadcast;
import com.devlomi.fireapp.model.realms.Chat;
import com.devlomi.fireapp.model.realms.CurrentUserInfo;
import com.devlomi.fireapp.model.realms.FireCall;
import com.devlomi.fireapp.model.realms.Group;
import com.devlomi.fireapp.model.realms.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;

public class RealmBackupRestore {

    private static File EXPORT_REALM_PATH = DirManager.getDatabasesFolder();
    private static String EXPORT_REALM_FILE_NAME = "messages.fbup";
    private static String IMPORT_REALM_FILE_NAME = "temp.realm";

    private final static String TAG = RealmBackupRestore.class.getName();

    private Activity activity;
    private Realm realm;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public RealmBackupRestore(Activity activity) {
        this.realm = Realm.getDefaultInstance();
        this.activity = activity;
    }

    public void backup() throws io.realm.internal.IOException {
        // First check if we have storage permissions
        if (activity != null)
            checkStoragePermissions(activity);

        File exportRealmFile;


        // create a backup file
        exportRealmFile = new File(EXPORT_REALM_PATH, EXPORT_REALM_FILE_NAME);

        // if backup file already exists, delete it
        exportRealmFile.delete();

        // copy current realm to backup file
        realm.writeCopyTo(exportRealmFile);


        realm.close();
        SharedPreferencesManager.setLastBackup(new Date().getTime());

    }

    public void restore() throws IOException, BackupFileMismatchedException, RealmMigrationNeededException {
        checkStoragePermissions(activity);
        //Restore

        String restoreFilePath = EXPORT_REALM_PATH + "/" + EXPORT_REALM_FILE_NAME;


        copyBundledRealmFile(restoreFilePath, IMPORT_REALM_FILE_NAME);
        //backed up realm
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name(IMPORT_REALM_FILE_NAME)
                .schemaVersion(MyMigration.SCHEMA_VERSION)
                .migration(new MyMigration())
                .build();
        Realm realm = Realm.getInstance(configuration);
        CurrentUserInfo currentUserInfo = realm.where(CurrentUserInfo.class).findFirst();
        if (currentUserInfo != null && currentUserInfo.getUid().equals(FireManager.getUid())) {
            RealmResults<Message> messages = realm.where(Message.class).findAll();
            RealmResults<Chat> chats = realm.where(Chat.class).findAll();
            RealmResults<Group> groups = realm.where(Group.class).findAll();
            RealmResults<FireCall> calls = realm.where(FireCall.class).findAll();
            RealmResults<Broadcast> broadcasts = realm.where(Broadcast.class).findAll();


            RealmHelper instance = RealmHelper.getInstance();

            for (Message message : messages) {
                instance.saveObjectToRealm(message);
            }
            for (Group group : groups) {
                instance.saveObjectToRealm(group);
            }
            for (Chat chat : chats) {
                instance.migrateChat(chat);
            }
            for (FireCall call : calls) {
                instance.saveObjectToRealm(call);
            }
            for (Broadcast broadcast : broadcasts) {
                instance.saveObjectToRealm(broadcast);
            }

        } else {
            throw new BackupFileMismatchedException();
        }

        realm.close();
        Realm.deleteRealm(configuration);

    }

    private String copyBundledRealmFile(String oldFilePath, String outFileName) throws IOException {
        File file = new File(activity.getApplicationContext().getFilesDir(), outFileName);
        FileUtils.deleteFile(file.getPath());

        FileOutputStream outputStream = new FileOutputStream(file);

        FileInputStream inputStream = new FileInputStream(new File(oldFilePath));

        byte[] buf = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buf)) > 0) {
            outputStream.write(buf, 0, bytesRead);
        }
        outputStream.close();
        return file.getAbsolutePath();


    }

    private void checkStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public static boolean isBackupFileExists() {
        File exportRealmFile = new File(EXPORT_REALM_PATH, EXPORT_REALM_FILE_NAME);
        return exportRealmFile.exists() && exportRealmFile.length() > 0;
    }

    private String dbPath() {
        return realm.getPath();
    }
}
