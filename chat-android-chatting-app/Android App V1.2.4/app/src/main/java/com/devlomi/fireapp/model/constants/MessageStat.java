package com.devlomi.fireapp.model.constants;

/**
 * Created by Devlomi on 14/08/2017.
 */
//indicates message state
public class MessageStat {
    //pending = message is in process  (show clock icon for example)
    public static final int PENDING = 0;
    //message has arrived to the Server but not received by other user (one checkmark)
    public static final int SENT = 1;
    //message received by the other user (two checkmarks)
    public static final int RECEIVED = 2;
    //the other user has read the message (two colored checkmarks)
    public static final int READ = 3;
}
