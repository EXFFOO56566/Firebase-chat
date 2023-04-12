package com.devlomi.fireapp.utils;

import com.theartofdev.edmodo.cropper.CropImage;

public class CropImageRequest {
    public static final int IMAGE_WIDTH = 2000;
    public static final int IMAGE_HEIGHT = 2000;


    //get crop image request when user wants to change his photo
    public static CropImage.ActivityBuilder getCropImageRequest() {
        return CropImage.activity()
                .setMaxCropResultSize(IMAGE_WIDTH, IMAGE_HEIGHT)
                .setAutoZoomEnabled(false);
    }
}
