package com.devlomi.fireapp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by Devlomi on 26/11/2017.
 */

//this class is responsible for Files managing (move,copy files etc..)
public class FileUtils {
    //delete file
    public static void deleteFile(String path) {
        try {
            if (path == null)
                return;

            new File(path).delete();
        } catch (Exception e) {
        }
    }


    public static File moveFile(File file, File dir) throws IOException {
        File newFile = new File(dir, file.getName());
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            file.delete();
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }

        return newFile;

    }

    public static File moveFile(String inputFilePath, File dir) {
        File file = new File(inputFilePath);
        File newFile = new File(dir, file.getName());
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            file.delete();


            if (inputChannel != null) try {
                inputChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (outputChannel != null) outputChannel.close();
        } catch (IOException e) {

        }

        return newFile;

    }

    //copy file from directory to another
    public static void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public static void copyFile(String inputPath, File dst) throws IOException {
        File src = new File(inputPath);
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }


    //check if file exists
    public static boolean isFileExists(String path) {
        if (path == null)
            return false;
        return new File(path).exists();
    }


    //check if user is picked a Video or an Image
    public static boolean isPickedVideo(String path) {
        String extension;
        int i = path.lastIndexOf('.');
        if (i > 0) {
            extension = path.substring(i + 1);
            if (extension.equalsIgnoreCase("MP4") || extension.equalsIgnoreCase("3GP"))
                return true;

        }
        return false;
    }
}
