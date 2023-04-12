package com.devlomi.fireapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Devlomi on 28/09/2017.
 */

public class BitmapUtils {
    private static final int radius = 6;
    public static final int THUMB_IMAGE_BLURRED_QUALITY = 40;
    private static int IMAGE_COMPRESS_QUALITY = 70;
    private static int VIDEO_THUMB_WIDTH = 200;
    private static int VIDEO_THUMB_QUALITY = 50;
    private static int VIDEO_THUMB_HEIGHT = 200;


    //extract image from a Video
    public static Bitmap getThumbnailFromVideo(String videoPath) {
        return ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND);
    }


    //generate a small thumb image it's 200x200 now
    public static String generateVideoThumbAsBase64(String videoPath) {
        //extarct thumbnail from video
        Bitmap bitmap = getThumbnailFromVideo(videoPath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //create small version of the thumbnail
        bitmap = Bitmap.createScaledBitmap(bitmap, VIDEO_THUMB_WIDTH, VIDEO_THUMB_HEIGHT, true);
        //compress it
        bitmap.compress(Bitmap.CompressFormat.JPEG, VIDEO_THUMB_QUALITY, baos);
        //encode it to save it database or send it
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public static String generateVideoThumbAsBase64(Bitmap bitmapThumb) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapThumb = Bitmap.createScaledBitmap(bitmapThumb, VIDEO_THUMB_WIDTH, VIDEO_THUMB_HEIGHT, true);
        bitmapThumb.compress(Bitmap.CompressFormat.JPEG, VIDEO_THUMB_QUALITY, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    //compress image and write it to output file
    public static void compressImage(String inputPath, File outputFile) {
        Bitmap bitmap = decodeFile(inputPath);
        convertBitmapToJpeg(bitmap, outputFile);
    }

    @NonNull
    private static BitmapFactory.Options getOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;
        return options;
    }

    //compress image and write it to output file with the provided quality
    public static void compressImage(String inputPath, File outputFile, int quality) {
        Bitmap bitmap = decodeFile(inputPath);
        convertBitmapToJpeg(bitmap, outputFile, quality);
    }

    //convert bitmap to a JPG file
    public static void convertBitmapToJpeg(Bitmap bmp, File f) {
        try {
            FileOutputStream fout = new FileOutputStream(f.getPath());
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            bmp.compress(Bitmap.CompressFormat.JPEG, IMAGE_COMPRESS_QUALITY, bos);
            bos.flush();
            bos.close();

        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    public static void convertBitmapToJpeg(Bitmap bmp, File f, int quality) {

        try {
            FileOutputStream fout = new FileOutputStream(f.getPath());
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            bos.flush();
            bos.close();

        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    //decode image as base64 String to save it in database or send it to firebase database
    //it's used for the thumbnail for other user to see a tiny thumbnail before download the full image
    public static String decodeImage(String path, boolean blur) {

        Bitmap bitmap = decodeFile(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (blur) {
            //blur this thumb
            Bitmap blurredBitmap = blurImage(bitmap, radius);
            //compress thumb

            blurredBitmap.compress(Bitmap.CompressFormat.JPEG, THUMB_IMAGE_BLURRED_QUALITY, baos);
        } else {
            bitmap.compress(Bitmap.CompressFormat.JPEG, THUMB_IMAGE_BLURRED_QUALITY, baos);
        }
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private static Bitmap decodeFile(String path) {
        return BitmapFactory.decodeFile(path, getOptions());
    }


    public static String decodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap blurredBitmap = blurImage(bitmap, radius);
        blurredBitmap.compress(Bitmap.CompressFormat.JPEG, THUMB_IMAGE_BLURRED_QUALITY, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }


    //decode image as PNG because we need a Transparent background
    //this is used to generate the user circle thumb
    public static String decodeImageAsPng(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 40, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    //convert decoded String image from database to a Bitmap
    //this is used to show the user image in notification
    public static Bitmap encodeImage(String decodedImage) {
        byte[] decodedString = Base64.decode(decodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    //used in Glide
    public static byte[] encodeImageAsBytes(String decodedImage) {
        byte[] decodedString = Base64.decode(decodedImage, Base64.DEFAULT);
        return decodedString;
    }

    public static Bitmap simpleBlur(Context context, Bitmap source) {
        RenderScript rs = RenderScript.create(context);

        Bitmap blurredBitmap = source.copy(Bitmap.Config.ARGB_8888, true);

        // Allocate memory for Renderscript to work with
        Allocation input = Allocation.createFromBitmap(
                rs,
                blurredBitmap,
                Allocation.MipmapControl.MIPMAP_FULL,
                Allocation.USAGE_SHARED
        );
        Allocation output = Allocation.createTyped(rs, input.getType());

        // Load up an instance of the specific script that we want to use.
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setInput(input);

        // Set the blur radius
        script.setRadius(10);

        // Start the ScriptIntrinisicBlur
        script.forEach(output);

        // Copy the output to the blurred bitmap
        output.copyTo(blurredBitmap);

        source.recycle();

        return blurredBitmap;

    }

    public static Bitmap blurImage(Bitmap sentBitmap, int radius) {
        int width = 100;
        int height = 100;
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }


    public static Bitmap convertFileImageToBitmap(String filePath) {
        return decodeFile(filePath);
    }

    //used to generate a thumb Circled user image from Full Image
    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        final int scaledWidth = 100;
        final int scaledHeight = 100;

        bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
        Bitmap output;
        Rect srcRect, dstRect;

        float r;
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        if (width > height) {
            output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
            int left = (width - height) / 2;
            int right = left + height;
            srcRect = new Rect(left, 0, right, height);
            dstRect = new Rect(0, 0, height, height);
            r = height / 2;
        } else {
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            int top = (height - width) / 2;
            int bottom = top + width;
            srcRect = new Rect(0, top, width, bottom);
            dstRect = new Rect(0, 0, width, width);
            r = width / 2;
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);


        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

        bitmap.recycle();

        return output;

    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = AppCompatResources.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
