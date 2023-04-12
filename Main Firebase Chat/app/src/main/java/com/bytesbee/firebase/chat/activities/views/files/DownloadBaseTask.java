package com.bytesbee.firebase.chat.activities.views.files;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.bytesbee.firebase.chat.activities.async.BaseTask;
import com.bytesbee.firebase.chat.activities.managers.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class DownloadBaseTask extends BaseTask {
    private final Uri mUri;
    private final CallBackTask callback;
    private final WeakReference<Context> mContext;
    private final String myExtention;
    private String pathPlusName;
    private File folder;
    private Cursor returnCursor;
    private InputStream is = null;
    private String errorReason = "";

    public DownloadBaseTask(Uri uri, Context context, CallBackTask callback) {
        this.mUri = uri;
        final MimeTypeMap mime = MimeTypeMap.getSingleton();
        myExtention = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        Utils.sout("==DownloadBaseTask:: " + myExtention + " >>> " + mUri);
        mContext = new WeakReference<>(context);
        this.callback = callback;
    }


    @Override
    public Object call() throws Exception {
        File file = null;
        int size = -1;

        Context context = mContext.get();
        if (context != null) {
            folder = context.getExternalFilesDir("Temp");
            if (folder != null && !folder.exists()) {
                if (folder.mkdirs()) {
                    Utils.sout("==Temp folder created");
                }
            }
            returnCursor = context.getContentResolver().query(mUri, null, null, null, null);
            try {
                is = context.getContentResolver().openInputStream(mUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            // File is now available
            callback.PickerManagerOnPreExecute();
        } catch (Exception e) {
            Utils.getErrors(e);
        }

        try {
            try {
                if (returnCursor != null && returnCursor.moveToFirst()) {
                    long fileSize;
                    if (mUri.getScheme() != null)
                        if (mUri.getScheme().equals("content")) {
                            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                            fileSize = returnCursor.getLong(sizeIndex);
                            size = (int) fileSize;
                        } else if (mUri.getScheme().equals("file")) {
                            File ff = new File(mUri.getPath());
                            fileSize = ff.length();
                            size = (int) fileSize;
                        }
                }
            } finally {
                if (returnCursor != null)
                    returnCursor.close();
            }
//            getDisplayNameSize(mContext.get(), mUri);
            String fileName = getFileName(mUri, mContext.get());
            if (!TextUtils.isEmpty(myExtention)) {
                if (!fileName.endsWith(myExtention)) {
                    fileName = fileName + "." + myExtention;
                }
            }

            pathPlusName = folder + "/" + fileName;
            file = new File(folder + "/" + fileName);
            Utils.sout("==pathPlusName + File :: " + fileName + " >>> " + pathPlusName + " >> " + file);
            final BufferedInputStream bis = new BufferedInputStream(is);
            final FileOutputStream fos = new FileOutputStream(file);

            final byte[] data = new byte[1024];
            long total = 0;
            int count;
            while ((count = bis.read(data)) != -1) {
//                if (!isCancelled()) {
                total += count;
                if (size != -1) {
                    try {
                        int post = (int) ((total * 100) / size);
//                            Utils.sout("==Publish Progress:: " + post);
                        callback.PickerManagerOnProgressUpdate(post);
//                            publishProgress((int) ((total * 100) / size));

                    } catch (Exception e) {
//                            Utils.sout("==File size is less than 1 : Progress is 0");
                        callback.PickerManagerOnProgressUpdate(0);
//                            publishProgress(0);
                    }
                }
                fos.write(data, 0, count);
//                }
            }
            fos.flush();
            fos.close();

        } catch (Exception e) {
            Utils.sout("==Exception:: " + e.getMessage());
            Utils.getErrors(e);
            errorReason = e.getMessage();
        }

        assert file != null;
        return file.getAbsolutePath();

    }

    @Override
    public void setUiForLoading() {
        callback.PickerManagerOnUriReturned();
//        listener.showProgressBar(0);
    }

    @Override
    public void setDataAfterLoading(Object result) {
//        listener.setDataInPageWithResult(result);
//        listener.hideProgressBar();
        if (result == null) {
            callback.PickerManagerOnPostExecute(pathPlusName, true, false, errorReason);
        } else {
            callback.PickerManagerOnPostExecute(pathPlusName, true, true, "");
        }
    }

    private String getFileName(Uri uri, Context context) {
        String result = null;
        if (uri.getScheme() != null) {
            if (uri.getScheme().equals("content")) {
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            assert result != null;
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}
