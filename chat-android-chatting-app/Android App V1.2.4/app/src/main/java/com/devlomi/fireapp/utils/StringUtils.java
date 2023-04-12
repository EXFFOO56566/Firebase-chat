package com.devlomi.fireapp.utils;

public class StringUtils {

    //this will remove the separators if it's exists at the end of text
    public static String removeExtraSeparators(String text, String separator) {
        try {
            return text.substring(0, text.lastIndexOf(separator));
        } catch (Exception e) {
            return text;
        }
    }


}
