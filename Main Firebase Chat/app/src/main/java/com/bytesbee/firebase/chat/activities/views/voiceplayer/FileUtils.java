package com.bytesbee.firebase.chat.activities.views.voiceplayer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class FileUtils {
    public static byte[] fileToBytes(File file) {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
