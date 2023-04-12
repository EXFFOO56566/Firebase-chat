package com.bytesbee.firebase.chat.activities.views.files;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.bytesbee.firebase.chat.activities.R;
import com.bytesbee.firebase.chat.activities.async.BaseTask;
import com.bytesbee.firebase.chat.activities.async.TaskRunner;
import com.bytesbee.firebase.chat.activities.managers.Utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.Comparator;

/**
 * Created by BytesBee
 */
public class FileUtils {
    private FileUtils() {
    } //private constructor to enforce Singleton pattern


    public static final String MIME_TYPE_AUDIO = "audio/*";
    public static final String MIME_TYPE_TEXT = "text/*";
    public static final String MIME_TYPE_IMAGE = "image/*";
    public static final String MIME_TYPE_VIDEO = "video/*";
    public static final String MIME_TYPE_APP = "application/*";

    public static final String HIDDEN_PREFIX = ".";
    /**
     * TAG for log messages.
     */
    static final String TAG = "FileUtils";
    private static final boolean DEBUG = false; // Set to true to enable logging
    /**
     * File and folder comparator. TODO Expose sorting option method
     */
    public static Comparator<File> sComparator = new Comparator<File>() {
        @Override
        public int compare(File f1, File f2) {
            // Sort alphabetically by lower case, which is much cleaner
            return f1.getName().toLowerCase().compareTo(
                    f2.getName().toLowerCase());
        }
    };
    /**
     * File (not directories) filter.
     */
    public static FileFilter sFileFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            final String fileName = file.getName();
            // Return files only (not directories) and skip hidden files
            return file.isFile() && !fileName.startsWith(HIDDEN_PREFIX);
        }
    };
    /**
     * Folder (directories) filter.
     */
    public static FileFilter sDirFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            final String fileName = file.getName();
            // Return directories only and skip hidden directories
            return file.isDirectory() && !fileName.startsWith(HIDDEN_PREFIX);
        }
    };

    /**
     * Gets the extension of a file name, like ".png" or ".jpg" or ".mp3".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    public static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    /**
     * Returns the path only (without file name).
     *
     * @param file
     * @return
     */
    public static File getPathWithoutFilename(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                // no file to be split off. Return everything
                return file;
            } else {
                String filename = file.getName();
                String filepath = file.getAbsolutePath();

                // Construct path without file name.
                String pathwithoutname = filepath.substring(0,
                        filepath.length() - filename.length());
                if (pathwithoutname.endsWith("/")) {
                    pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length() - 1);
                }
                return new File(pathwithoutname);
            }
        }
        return null;
    }

    /**
     * @return The MIME type for the given file.
     */
    public static String getMimeType(File file) {

        String extension = getExtension(file.getName());

        if (extension.length() > 0)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));

        return "application/octet-stream";
    }

    public static final String AUTHORITY = "com.bytesbee.filechoser.documents";

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority.
     */
    public static boolean isLocalStorageDocument(Context context, Uri uri) {
        final String authority = context.getString(R.string.authority);
        return authority.equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                if (DEBUG)
                    DatabaseUtils.dumpCursor(cursor);

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * Get the file size in a human-readable string.
     *
     * @param size
     * @return
     */
    public static String getReadableFileSize(int size) {
        final int BYTES_IN_KILOBYTES = 1024;
        final DecimalFormat dec = new DecimalFormat("###.#");
        final String KILOBYTES = " KB";
        final String MEGABYTES = " MB";
        final String GIGABYTES = " GB";
        float fileSize = 0;
        String suffix = KILOBYTES;

        if (size > BYTES_IN_KILOBYTES) {
            fileSize = size / BYTES_IN_KILOBYTES;
            if (fileSize > BYTES_IN_KILOBYTES) {
                fileSize = fileSize / BYTES_IN_KILOBYTES;
                if (fileSize > BYTES_IN_KILOBYTES) {
                    fileSize = fileSize / BYTES_IN_KILOBYTES;
                    suffix = GIGABYTES;
                } else {
                    suffix = MEGABYTES;
                }
            }
        }
        return dec.format(fileSize) + suffix;
    }

    public static void copyFile(File src, File dst) {
        final BaseTask baseTask = new BaseTask() {
            @Override
            public void setUiForLoading() {
                super.setUiForLoading();
            }

            @Override
            public Object call() throws Exception {
                try {
                    FileInputStream inStream = new FileInputStream(src);
                    FileOutputStream outStream = new FileOutputStream(dst);
                    FileChannel inChannel = inStream.getChannel();
                    FileChannel outChannel = outStream.getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                    inStream.close();
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void setDataAfterLoading(Object result) {
            }

        };
        TaskRunner taskRunner = new TaskRunner();
        taskRunner.executeAsync(baseTask);

//        AsyncTask<File, Void, Void> task = new AsyncTask<File, Void, Void>() {
//            @Override
//            protected Void doInBackground(File... params) {
//                try {
//                    FileInputStream inStream = new FileInputStream(params[0]);
//                    FileOutputStream outStream = new FileOutputStream(params[1]);
//                    FileChannel inChannel = inStream.getChannel();
//                    FileChannel outChannel = outStream.getChannel();
//                    inChannel.transferTo(0, inChannel.size(), outChannel);
//                    inStream.close();
//                    outStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//            }
//        };
//        File[] arr = {src, dst};
//        task.execute(arr);
    }

    public static Intent getImageIntent() {
        String[] mimeTypes = {"image/jpeg", "image/jpeg", "image/png"};
        return pickIntentFromMimeType(mimeTypes);
    }

    /**
     * Supported Formats: avi, mov, mp4, webm, 3gp, mkv, mpeg
     */
    public static Intent getVideoIntent() {
        String[] mimeTypes = {"video/avi", "video/mov", "video/quicktime", "video/mp4", "video/webm", "video/3gpp", "video/x-matroska", "video/mpeg"};
        return pickIntentFromMimeType(mimeTypes);
    }

    /**
     * Supported formats: aac, amr, awb, mp3, mp4, ogg, opus, wav
     */
    public static Intent getAudioIntent() {
        String[] mimeTypes = {"audio/aac", "audio/aac-adts", "audio/amr", "audio/amr-wb", "audio/mpeg", "audio/mp4", "audio/ogg", "audio/x-wav"};
        return pickIntentFromMimeType(mimeTypes);
    }

    /**
     * Get the Intent for selecting content to be used in an Intent Chooser.
     * Supported Formats: doc, docx, xls, xlsx, ppt, pdf, epub, txt, zip
     *
     * @return The intent for opening a file with Intent.createChooser()
     */
    public static Intent getDocumentIntent() {
        String[] mimeTypes = {
                "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                "application/pdf", //pdf
                "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                "text/plain",//txt
                "application/zip",//zip
                "application/epub+zip" //epub
        };
        return pickIntentFromMimeType(mimeTypes);
    }

    /**
     * Get the Intent for selecting content to be used in an Intent Chooser.
     *
     * @return The intent for opening a file with Intent.createChooser()
     */
    public static Intent pickIntentFromMimeType(String[] mimeTypes) {
        // Implicitly allow the user to select a particular kind of data
//        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // The MIME data type filter
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        /* Commented by Prashant Adesara - START API 30 */
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
//            if (mimeTypes.length > 0) {
//                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
//            }
//        } else {
//            String mimeTypesStr = "";
//            for (String mimeType : mimeTypes) {
//                mimeTypesStr += mimeType + "|";
//            }
//            intent.setType(mimeTypesStr.substring(0, mimeTypesStr.length() - 1));
//        }
        /* Commented by Prashant Adesara - END API 30*/

        //intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "IMAGE_NAME.pdf")));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }

    public static String fileCopyFromCache(Context context, Uri uri) {
        final File file = new File(context.getCacheDir(), uri.getLastPathSegment());
        try (final InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream output = new FileOutputStream(file)) {
            final byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            return file.getPath();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void copyFileToDest(File src, File dst) {
        try {
            Utils.sout("copyFileNew:: Source File to copy:: " + src.toString() + " >> " + dst.toString());
            FileInputStream input = new FileInputStream(src);
            FileOutputStream output = new FileOutputStream(dst);
            byte[] buffer = new byte[1024];

            int read;
            while ((read = input.read(buffer)) > 0) {
                output.write(buffer, 0, read);
            }
            //Utils.sout("copyFileNew Dest:: " + dst.exists());
            input.close();
            output.close();
        } catch (Exception e) {
            Utils.getErrors(e);
        }
    }
}

