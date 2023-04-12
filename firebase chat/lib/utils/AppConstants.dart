import 'package:nb_utils/nb_utils.dart';

const AppName = 'Mighty Chat';
const chatMsgRadius = 12.0;

const agoraVideoCallId = "2d01195ef1eb4737836d0cf192ede63a";

//region Notification
const mFirebaseAppId = 'mighty-chat-app.appspot.com';
const mAppIconUrl = 'https://firebasestorage.googleapis.com/v0/b/$mFirebaseAppId/o/app_icon.png?alt=media&token=738b073d-c575-4a79-a257-de052dadd2e3';

const mOneSignalAppId = '249444f0-94a6-4cdc-a143-33faa2a4e3f3';
const mOneSignalRestKey = 'NDkyYjgwNDgtMjMyMC00YWRjLWExNzUtYmM0NDY3MzhkZDEw';
const mOneSignalChannelId = '582d0ac5-749f-4a21-9d9e-1c7b02b1321b';

//endregion

//region AppUrls
const packageName = "com.mighty.chat";
const termsAndConditionURL = 'https://meetmighty.com/codecanyon/document/mightychat/#mm-help-support';
const supportURL = 'https://support.meetmighty.com/';
const codeCanyonURL = 'https://codecanyon.net/user/meetmighty/portfolio';
const appShareURL = '$playStoreBaseURL$packageName';
const mailto = 'app.meetmighty@gmail.com';

//endregion

List<String> RTLLanguage = ['ar', 'ur'];

const EXCEPTION_NO_USER_FOUND = "EXCEPTION_NO_USER_FOUND";

const COMING_SOON = 'Coming soon';

//region AdMobIntegration
const mAdMobAppId = 'ca-app-pub-1399327544318575~4295532067';
const mAdMobBannerId = 'ca-app-pub-1399327544318575/9356287059';
const mAdMobInterstitialId = 'ca-app-pub-1399327544318575/1286225341';
//endregion

const SEARCH_KEY = "Search";

const defaultLanguage = 'en';
const LANGUAGE = "LANGUAGE";
const SELECTED_LANGUAGE = "SELECTED_LANGUAGE";
enum MessageType {
  TEXT,
  IMAGE,
  VIDEO,
  AUDIO,
}

const TEXT = "TEXT";
const IMAGE = "IMAGE";
const VIDEO = "VIDEO";
const AUDIO = "AUDIO";

extension MessageExtension on MessageType {
  String? get name {
    switch (this) {
      case MessageType.TEXT:
        return 'TEXT';
      case MessageType.IMAGE:
        return 'IMAGE';
      case MessageType.VIDEO:
        return 'VIDEO';
      case MessageType.AUDIO:
        return 'AUDIO';
      default:
        return null;
    }
  }
}

//FireBase Collection Name
const MESSAGES_COLLECTION = "messages";
const USER_COLLECTION = "users";
const CONTACT_COLLECTION = "contact";

const USER_PROFILE_IMAGE = "userProfileImage";
const CHAT_DATA_IMAGES = "chatImages";

// Call Status For Call Logs
const CALLED_STATUS_DIALLED = "dialled";
const CALLED_STATUS_RECEIVED = "received";
const CALLED_STATUS_MISSED = "missed";

/* Theme Mode Type */
const ThemeModeLight = 0;
const ThemeModeDark = 1;
const ThemeModeSystem = 2;

//Default Font Size
const FONT_SIZE_SMALL = 12;
const FONT_SIZE_MEDIUM = 16;
const FONT_SIZE_LARGE = 20;

//Pagination Setting
const PER_PAGE_CHAT_COUNT = 50;

//region SharePreference Key
const IS_LOGGED_IN = 'IS_LOGGED_IN';
const userId = 'userId';
const userDisplayName = 'userDisplayName';
const userEmail = 'userEmail';
const userPhotoUrl = 'userPhotoUrl';
const isEmailLogin = "isEmailLogin";
const userStatus = "userStatus";
const userMobileNumber = "userMobileNumber";
const playerId = "playerId";
//endregion

//region DefaultSettingConstant
const FONT_SIZE_INDEX = "FONT_SIZE_INDEX";
const FONT_SIZE_PREF = "FONT_SIZE_PREF";
const IS_ENTER_KEY = "IS_ENTER_KEY";
const SELECTED_WALLPAPER = "SELECTED_WALLPAPER";

//endregion
