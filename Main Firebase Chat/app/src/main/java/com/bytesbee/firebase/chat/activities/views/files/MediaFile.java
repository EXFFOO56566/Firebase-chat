package com.bytesbee.firebase.chat.activities.views.files;

import android.mtp.MtpConstants;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;

//http://androidxref.com/4.4.4_r1/xref/frameworks/base/media/java/android/media/MediaFile.java#174

/**
 * MediaScanner helper class.
 */
public class MediaFile {
    private static final HashMap<String, MediaFileType> sFileTypeMap = new HashMap<>();
    private static final HashMap<String, Integer> sMimeTypeMap = new HashMap<>();
    // maps file extension to MTP format code
    private static final HashMap<String, Integer> sFileTypeToFormatMap = new HashMap<>();
    // maps mime type to MTP format code
    private static final HashMap<String, Integer> sMimeTypeToFormatMap = new HashMap<>();
    // maps MTP format code to mime type
    private static final HashMap<Integer, String> sFormatToMimeTypeMap = new HashMap<>();

    // Audio file types -  aac, amr, awb, mp3, mp4, ogg, wav, opus,
    public static final int FILE_TYPE_AAC = 1;
    public static final int FILE_TYPE_AMR = 2;
    public static final int FILE_TYPE_AWB = 3;
    public static final int FILE_TYPE_MP3 = 4;
    public static final int FILE_TYPE_OGG = 5;
    public static final int FILE_TYPE_WAV = 6;
    public static final int FILE_TYPE_M4A = 7;
    public static final int FILE_TYPE_MKA = 8;
    private static final int FIRST_AUDIO_FILE_TYPE = FILE_TYPE_AAC;
    private static final int LAST_AUDIO_FILE_TYPE = FILE_TYPE_MKA;

    // MIDI file types
    public static final int FILE_TYPE_MID = 11;
    public static final int FILE_TYPE_SMF = 12;
    public static final int FILE_TYPE_IMY = 13;
    private static final int FIRST_MIDI_FILE_TYPE = FILE_TYPE_MID;
    private static final int LAST_MIDI_FILE_TYPE = FILE_TYPE_IMY;

    // Video file types -  avi, mov, mp4, webm, 3gp, mkv, mpeg
    public static final int FILE_TYPE_AVI = 21;
    public static final int FILE_TYPE_MOV = 22;
    public static final int FILE_TYPE_MOV_QUICK = 23;
    public static final int FILE_TYPE_MP4 = 24;
    public static final int FILE_TYPE_WEBM = 25;
    public static final int FILE_TYPE_3GPP = 26;
    public static final int FILE_TYPE_MKV = 27;
//    public static final int FILE_TYPE_M4V = 27;
//    public static final int FILE_TYPE_3GPP2 = 28;
//    public static final int FILE_TYPE_WMV = 29;
//    public static final int FILE_TYPE_ASF = 30;
//    public static final int FILE_TYPE_MP2TS = 31;

    private static final int FIRST_VIDEO_FILE_TYPE = FILE_TYPE_AVI;
    private static final int LAST_VIDEO_FILE_TYPE = FILE_TYPE_MKV;

    // More video file types
    public static final int FILE_TYPE_MP2PS = 200;
    private static final int FIRST_VIDEO_FILE_TYPE2 = FILE_TYPE_MP2PS;
    private static final int LAST_VIDEO_FILE_TYPE2 = FILE_TYPE_MP2PS;

    // Other popular file types
    public static final int FILE_TYPE_TEXT = 100;
    public static final int FILE_TYPE_PDF = 102;
    public static final int FILE_TYPE_MS_WORD = 104;
    public static final int FILE_TYPE_MS_EXCEL = 105;
    public static final int FILE_TYPE_MS_POWERPOINT = 106;
    public static final int FILE_TYPE_ZIP = 107;
    private static final int FIRST_DOC_FILE_TYPE = FILE_TYPE_TEXT;
    private static final int LAST_DOC_FILE_TYPE = FILE_TYPE_ZIP;

    public static class MediaFileType {
        public final int fileType;
        public final String mimeType;

        MediaFileType(int fileType, String mimeType) {
            this.fileType = fileType;
            this.mimeType = mimeType;
        }

        @NotNull
        @Override
        public String toString() {
            return "{" +
                    "fileType=" + fileType +
                    ", mimeType='" + mimeType + '\'' +
                    '}';
        }
    }

    static void addFileType(String extension, int fileType, String mimeType) {
        sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType));
        sMimeTypeMap.put(mimeType, fileType);
    }

    static void addFileType(String extension, int fileType, String mimeType, int mtpFormatCode) {
        addFileType(extension, fileType, mimeType);
        sFileTypeToFormatMap.put(extension, mtpFormatCode);
        sMimeTypeToFormatMap.put(mimeType, mtpFormatCode);
        sFormatToMimeTypeMap.put(mtpFormatCode, mimeType);
    }

    static {

        addFileType("AAC", FILE_TYPE_AAC, "audio/aac", MtpConstants.FORMAT_AAC);
        addFileType("AAC", FILE_TYPE_AAC, "audio/aac-adts", MtpConstants.FORMAT_AAC);
        addFileType("AMR", FILE_TYPE_AMR, "audio/amr");
        addFileType("AWB", FILE_TYPE_AWB, "audio/amr-wb");
        addFileType("MP3", FILE_TYPE_MP3, "audio/mpeg", MtpConstants.FORMAT_MP3);
        addFileType("MPGA", FILE_TYPE_MP3, "audio/mpeg", MtpConstants.FORMAT_MP3);
        addFileType("OGG", FILE_TYPE_OGG, "audio/ogg", MtpConstants.FORMAT_OGG);
        addFileType("OGG", FILE_TYPE_OGG, "application/ogg", MtpConstants.FORMAT_OGG);
        addFileType("OGA", FILE_TYPE_OGG, "application/ogg", MtpConstants.FORMAT_OGG);
        addFileType("WAV", FILE_TYPE_WAV, "audio/x-wav", MtpConstants.FORMAT_WAV);
        addFileType("M4A", FILE_TYPE_M4A, "audio/mp4", MtpConstants.FORMAT_MPEG);
        addFileType("MKA", FILE_TYPE_MKA, "audio/x-matroska");

        addFileType("MID", FILE_TYPE_MID, "audio/midi");
        addFileType("MIDI", FILE_TYPE_MID, "audio/midi");
        addFileType("XMF", FILE_TYPE_MID, "audio/midi");
        addFileType("RTTTL", FILE_TYPE_MID, "audio/midi");
        addFileType("SMF", FILE_TYPE_SMF, "audio/sp-midi");
        addFileType("IMY", FILE_TYPE_IMY, "audio/imelody");
        addFileType("RTX", FILE_TYPE_MID, "audio/midi");
        addFileType("OTA", FILE_TYPE_MID, "audio/midi");
        addFileType("MXMF", FILE_TYPE_MID, "audio/midi");

        addFileType("AVI", FILE_TYPE_AVI, "video/avi");
        addFileType("MOV", FILE_TYPE_MOV, "video/mov");
        addFileType("MOV", FILE_TYPE_MOV_QUICK, "video/quicktime");
        addFileType("MP4", FILE_TYPE_MP4, "video/mp4", MtpConstants.FORMAT_MPEG);
        addFileType("WEBM", FILE_TYPE_WEBM, "video/webm");
        addFileType("3GP", FILE_TYPE_3GPP, "video/3gpp", MtpConstants.FORMAT_3GP_CONTAINER);
        addFileType("3GPP", FILE_TYPE_3GPP, "video/3gpp");
        addFileType("MKV", FILE_TYPE_MKV, "video/x-matroska");
        addFileType("MPEG", FILE_TYPE_MP4, "video/mpeg", MtpConstants.FORMAT_MPEG);
        addFileType("MPG", FILE_TYPE_MP4, "video/mpeg", MtpConstants.FORMAT_MPEG);

        addFileType("PDF", FILE_TYPE_PDF, "application/pdf");
        addFileType("DOC", FILE_TYPE_MS_WORD, "application/msword", MtpConstants.FORMAT_MS_WORD_DOCUMENT);
        addFileType("XLS", FILE_TYPE_MS_EXCEL, "application/vnd.ms-excel", MtpConstants.FORMAT_MS_EXCEL_SPREADSHEET);
        addFileType("PPT", FILE_TYPE_MS_POWERPOINT, "application/mspowerpoint", MtpConstants.FORMAT_MS_POWERPOINT_PRESENTATION);
        addFileType("ZIP", FILE_TYPE_ZIP, "application/zip");

    }

    public static boolean isAudioFileType(int fileType) {
        return ((fileType >= FIRST_AUDIO_FILE_TYPE &&
                fileType <= LAST_AUDIO_FILE_TYPE) ||
                (fileType >= FIRST_MIDI_FILE_TYPE &&
                        fileType <= LAST_MIDI_FILE_TYPE));
    }

    public static boolean isVideoFileType(int fileType) {
        return (fileType >= FIRST_VIDEO_FILE_TYPE &&
                fileType <= LAST_VIDEO_FILE_TYPE)
                || (fileType >= FIRST_VIDEO_FILE_TYPE2 &&
                fileType <= LAST_VIDEO_FILE_TYPE2);
    }

    public static boolean isDocumentFileType(int fileType) {
        return (fileType >= FIRST_DOC_FILE_TYPE &&
                fileType <= LAST_DOC_FILE_TYPE);
    }

    public static MediaFileType getFileType(String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot < 0)
            return null;
        return sFileTypeMap.get(path.substring(lastDot + 1).toUpperCase(Locale.ROOT));
    }

}
