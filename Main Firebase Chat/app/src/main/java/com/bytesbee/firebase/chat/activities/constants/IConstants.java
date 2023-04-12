package com.bytesbee.firebase.chat.activities.constants;

import java.io.File;

public interface IConstants {
    //ADS_SHOWN variable now moved into build.gradle file. Please look into i.

    String SDPATH = "/storage/emulated/0/";
    String IMG_FOLDER = "MyImages";
    String SLASH = File.separator;
    String SENT_FILE = "/Sent/";
    String EXT_MP3 = ".mp3";
    String EXT_VCF = ".vcf";

    String REF_USERS = "Users";
    String REF_CHATS = "Chats_v2";
    String REF_UPLOAD = "Uploads";
    String REF_BLOCK_USERS = "BlockUsers";

    String REF_GROUP_UPLOAD = "GroupUploads";
    String REF_GROUP_PHOTO_UPLOAD = "GroupPhotos";
    String REF_GROUP_ATTACHMENT = "GroupAttachment";
    String REF_CHAT_PHOTO_UPLOAD = "ChatPhotos";
    String REF_CHAT_ATTACHMENT = "ChatAttachment";
    String REF_VIDEO_THUMBS = "Thumbnails";

    String REF_TOKENS = "Tokens";
    String REF_OTHERS = "Others";

    String REF_GROUPS = "Groups";
    String REF_GROUPS_S = REF_GROUPS + SLASH;
    String REF_GROUP_MEMBERS = "MembersGroups";
    String REF_GROUP_MEMBERS_S = REF_GROUP_MEMBERS + SLASH;
    String REF_GROUPS_MESSAGES = "GroupsMessages";

    String IMG_DEFAULTS = "default";

    String EXTRA_USER_ID = "userId";

    String EXTRA_SENDER = "sender";
    String EXTRA_RECEIVER = "receiver";
    String EXTRA_MESSAGE = "message";
    String EXTRA_TYPE = "type";
    String EXTRA_IMGPATH = "imgPath";
    String EXTRA_ATTACH_TYPE = "attachmentType";
    String EXTRA_ATTACH_PATH = "attachmentPath";
    String EXTRA_ATTACH_NAME = "attachmentName";
    String EXTRA_ATTACH_FILE = "attachmentFileName";
    String EXTRA_ATTACH_SIZE = "attachmentSize";
    String EXTRA_ATTACH_DATA = "attachmentData";
    String EXTRA_ATTACH_DURATION = "attachmentDuration";
    String EXTRA_DATETIME = "datetime";
    String EXTRA_SEEN = "msgseen";
    String EXTRA_STATUS = "status";
    String EXTRA_IS_ONLINE = "isOnline";
    String EXTRA_SEARCH = "search";
    String EXTRA_VERSION = "version";
    String EXTRA_VERSION_CODE = "version_code";
    String EXTRA_VERSION_NAME = "version_name";
    String EXTRA_HIDE_EMAIL = "hideEmail";
    String EXTRA_HIDE_PROFILE_PHOTO = "hideProfilePhoto";
    String EXTRA_SIGNUP_TYPE = "signup_type";
    String EXTRA_SOCIAL_TOKEN = "social_token";

    String EXTRA_ID = "id";
    String EXTRA_EMAIL = "email";
    String EXTRA_USERNAME = "username";
    String EXTRA_PASSWORD = "password";
    String EXTRA_IMAGEURL = "imageURL";
    String EXTRA_ACTIVE = "active";
    String EXTRA_TYPING = "typing";
    String EXTRA_TYPINGWITH = "typingwith";
    String EXTRA_LINK = "linkPath";
    String EXTRA_ABOUT = "about";
    String EXTRA_GENDER = "genders";
    String EXTRA_LASTSEEN = "lastSeen";
    String EXTRA_GROUPS_IN = "groupsIn";
    String EXTRA_GROUPS_IN_BOTH = SLASH + EXTRA_GROUPS_IN + SLASH;

    String EXTRA_GROUP_ID = "groupId";
    String EXTRA_GROUP_NAME = "name";
    String EXTRA_ADMIN = "admin";
    String EXTRA_GROUP_MEMBERS = "members";
    String EXTRA_GROUP_IMG = "groupImg";
    String EXTRA_LAST_MSG = "lastMsg";
    String EXTRA_LAST_TIME = "lastMsgTime";
    String EXTRA_CREATED_AT = "createdAt";
    String EXTRA_SEND_MESSAGES = "sendMessageSetting";

    String EXTRA_OBJ_GROUP = "groupObject";

    boolean TRUE = true;
    boolean FALSE = false;

    String FCM_ICON = "icon";
    String FCM_USER = "user";
    String FCM_SENT = "sent";
    String FCM_TITLE = "title";
    String FCM_BODY = "body";
    String FCM_GROUPS = "groups";
    String FCM_USERNAME = "username";
    String FCM_TYPE = "type";

    String FCM_URL = "https://fcm.googleapis.com/";

    long CLICK_DELAY_TIME = 250;
    int EXTRA_TYPING_DELAY = 800;
    int EXTRA_DELAY = 1000;
    int SPLASH_DELAY = 4000;
    int ZERO = 0; // Don't change
    int ONE = 1; // Don't change
    int TWO = 2; //Don't edit this
    int THREE = 3; //Minimum groups member

    int REQUEST_PARTICIPATE = 1487;

    int DELAY_ONE_SEC = 1000;
    int VIBRATE_HUNDRED = 100;
    int REQUEST_CODE_CONTACT = 2002;
    int REQUEST_CODE_PLAY_SERVICES = 2003;
    int PERMISSION_CONTACT = 2014;
    int PERMISSION_AUDIO = 2025;
    int PERMISSION_DOCUMENT = 2058;
    int PERMISSION_VIDEO = 2041;
    int REQUEST_PERMISSION_RECORD = 2059;

    String TYPE_TEXT = "TEXT";
    String TYPE_IMAGE = "IMAGE";
    String TYPE_AUDIO = "AUDIO";
    String TYPE_VIDEO = "VIDEO";
    String TYPE_CONTACT = "CONTACT";
    String TYPE_DOCUMENT = "DOCUMENT";
    String TYPE_LOCATION = "LOCATION";
    String TYPE_RECORDING = "RECORDING";

    String DOWNLOAD_DATA = "download_data";
    String BROADCAST_DOWNLOAD_EVENT = "com.bytesbee.firebase.chat.activities.DOWNLOAD_EVENT";

    String PLAYING_DATA = "playing_data";
    String BROADCAST_PLAY_RECORDING_EVENT = "com.bytesbee.firebase.chat.activities.PLAY_RECORDING_EVENT";

    int STARTED = 0;
    int COMPLETED = 1;

    String PATH_ABOUT_US = "about_us.html";
    String PATH_PRIVACY_POLICY = "privacy_policy.html";
    String DEFAULT_UPDATE_URL = "https://play.google.com/store/apps/details?id=";
    String DEFAULT_UPDATE_URL_2 = "market://details?id=";

    // 0 = All Participants, 1 = Only Admin
    int SETTING_ALL_PARTICIPANTS = 0;
    int SETTING_ONLY_ADMIN = 1;

    int GEN_UNSPECIFIED = -1;
    int GEN_MALE = 1;
    int GEN_FEMALE = 2;

    int STATUS_ONLINE = 1;
    int STATUS_OFFLINE = 2;

    //From here, you may configure Private Image Preview. You are free to modify as necessary.
    String IMG_PREVIEW = "https://i.ibb.co/mTX9q3c/img.png"; //"https://i.ibb.co/J3vHTrt/img.png";

    String TYPE_EMAIL = "Email";
    String TYPE_GOOGLE = "Google";
    String EMPTY = "";
}
