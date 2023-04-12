package com.devlomi.fireapp.model.constants;

/**
 * Created by Devlomi on 19/08/2017.
 */

//indicates the state of a Network Process
public class DownloadUploadStat {
    //this is for non media messages
    public static final int DEFAULT = 0;

    public static final int LOADING = 1;
    public static final int SUCCESS = 2;
    public static final int FAILED = 3;
    //cancelled by user
    public static final int CANCELLED = 4;

}
