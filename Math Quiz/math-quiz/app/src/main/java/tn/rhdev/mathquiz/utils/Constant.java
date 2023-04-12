package tn.rhdev.mathquiz.utils;
import java.io.Serializable;


public class Constant  implements Serializable{

    //please change publisher id in androidmanifest

    public static final boolean show_admob_banner=true ; // show AdMob Smart banner
    public static final boolean show_admob_interstitial = true; // show AdMob Interstitial
    public static final String adMob_key_banner = "ca-app-pub-3940256099942544/6300978111";
    public static final String adMob_key_interstitial = "ca-app-pub-3940256099942544/1033173712";

    public static Boolean show_banner_ad = true; // show reward video
    public static String rewarded_ad_id="ca-app-pub-3940256099942544/5224354917";
    public static final int reward_time_inseconds=10;//set seconds to win after watching the reward video


    public static final int time_inseconds=60;
    public static final int questions_number = 21;
    public static final int max__number_value = 11;



}
