package com.devlomi.fireapp.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FontUtil {
    public static boolean isFontExists(String fontName) {
        try {
            return Arrays.asList(MyApp.context().getResources().getAssets().list("fonts")).contains(fontName);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getFonts(){
        try {
            return Arrays.asList(MyApp.context().getResources().getAssets().list("fonts"));
        } catch (IOException e) {
            e.printStackTrace();
        }

            return null;
    }
}
