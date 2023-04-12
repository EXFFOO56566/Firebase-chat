package com.devlomi.fireapp.utils;

import android.app.Activity;

import com.droidninja.imageeditengine.ImageEditor;

public class ImageEditorRequest {
    public static void open(Activity activity, String path) {
        new ImageEditor.Builder(activity, path)
                .setStickerAssets("stickers")
                .open();
    }
}
