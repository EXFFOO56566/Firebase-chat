package com.devlomi.fireapp.utils;

import android.webkit.MimeTypeMap;

/**
 * Created by Devlomi on 31/01/2018.
 */

public class MimeTypes {

    //supported MIME types when user share an item to our app
    public static final String TEXT_PLAIN = "text/plain";
    public static final String IMAGE = "image/";
    public static final String VIDEO = "video/";
    public static final String AUDIO = "audio/";
    public static final String CONTACT = "text/x-vcard";


    public static String getMimeType(String url) {
        String type = null;
        //get mime type from file path
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        return type;
    }
}
