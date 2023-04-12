//*************   © Copyrighted by Thinkcreative_Technologies. An Exclusive item of Envato market. Make sure you have purchased a Regular License OR Extended license for the Source Code from Envato to use this product. See the License Defination attached with source code. *********************

import 'package:fiberchat/Configs/Enum.dart';
import 'package:fiberchat/Configs/app_constants.dart';

const IsCallFeatureTotallyHide =
    false; // This is just the initial default value.  Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const Is24hrsTimeformat =
    true; // This is just the initial default value.  Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const int GroupMemberslimit =
    500; // This is just the initial default value.  Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const int BroadcastMemberslimit =
    500; // This is just the initial default value.  Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const int StatusDeleteAfterInHours =
    24; // This is just the initial default value.  Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const IsLogoutButtonShowInSettingsPage =
    true; // This is just the initial default value.  Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const FeedbackEmail =
    ''; // This is just the initial default value.  Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const IsAllowCreatingGroups =
    true; // This is just the initial default value.  Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const IsAllowCreatingBroadcasts =
    true; // This is just the initial default value.  Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const IsAllowCreatingStatus =
    true; // This is just the initial default value.  Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const IsPercentProgressShowWhileUploading =
    true; // This is just the initial default value.  Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const int MaxFileSizeAllowedInMB =
    60; // This is just the initial default value.  Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const int MaxNoOfFilesInMultiSharing =
    10; // This is just the initial default value.  Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const int MaxNoOfContactsSelectForForward =
    7; // This is just the initial default value.  Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.

//---- ####### Below Details Not neccsarily required unless you are using the Admin App:
const ConnectWithAdminApp =
    true; // If you are planning to use the admin app, set it to "true". We recommend it to always set it to true for Advance features whether you use the admin app or not.
const dynamic RateAppUrlAndroid =
    null; // Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const dynamic RateAppUrlIOS =
    null; // Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const TERMS_CONDITION_URL =
    'YOUR_TNC'; // Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
const PRIVACY_POLICY_URL =
    'YOUR_PRIVACY_POLICY'; // Once the database is written, It can only be changed from Admin App OR directly inside Firestore database - appsettings/userapp document.
//--
const int ImageQualityCompress =
    50; // This is compress the chat image size in percent while uploading to firesbase storage
const int DpImageQualityCompress =
    34; // This is compress the user display picture  size in percent while uploading to firesbase storage

const bool IsVideoQualityCompress =
    true; // This is compress the video size  to medium qulaity while uploading to firesbase storage

int maxChatMessageDocsLoadAtOnceForGroupChatAndBroadcastLazyLoading =
    25; //Minimum Value should be 15.
int maxAdFailedLoadAttempts = 3; //Minimum Value should be 3.
const int timeOutSeconds = 50; // Default phone Auth Code auto retrival timeout
const IsShowNativeTimDate =
    true; // Show Date Time in the user selected langauge
const IsShowDeleteChatOption =
    true; // Show Delete Chat Button in the All Chats Screens.
const IsLazyLoadingChat = false; //## under development yet
const IsApplyBtoomNavBar = false; //## under development yet
const IsRemovePhoneNumberFromCallingPageWhenOnCall =
    false; //## under development yet
const OnlyPeerWhoAreSavedInmyContactCanMessageOrCallMe =
    false; //If this is true, then only contacts saved in my device can send a message or call me.
const DEFAULT_LANGUAGE_FILE_CODE =
    'en'; //default language code if file is present is localization folder example-> en.json
const IsShowLanguageNameInNativeLanguage =
    false; // if "true", users can see the language name in respective language
const IsAdaptiveWidthTab =
    false; //Automatically adapt the Tab size in tab bar homepage as per the content length. Set it to "true" if your default language code is any of these ["pt", "nl", "vi", "tr", "id", "fr", "es", "ka"]
const IsShowLastMessageInChatTileWithTime =
    true; //If true, The "CHATS" screen will show lastmessage time, last message content, unread count in each chat Tile in All Chats page.
const IsShowUserFullNameAsSavedInYourContacts =
    false; //Warning: UNDER DEVELOPMENT //If true,All the users /peer name will be show as you have saved in contact. If "false", then the name will be the one which user has saved in his profile.
const IsShowGIFsenderButtonByGIPHY =
    true; //If true, GIF sending button will be shown to users in the text input area in chatrooms.

final loginPageTopColor =
    DESIGN_TYPE == Themetype.whatsapp ? fiberchatgreen : fiberchatWhite;

final loginPageBottomColor =
    DESIGN_TYPE == Themetype.whatsapp ? fiberchatDeepGreen : fiberchatWhite;
