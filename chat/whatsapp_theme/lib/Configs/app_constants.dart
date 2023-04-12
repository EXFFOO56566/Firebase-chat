//*************   © Copyrighted by Thinkcreative_Technologies. An Exclusive item of Envato market. Make sure you have purchased a Regular License OR Extended license for the Source Code from Envato to use this product. See the License Defination attached with source code. *********************

import 'package:fiberchat/Configs/Enum.dart';
import 'package:flutter/material.dart';

//*--App Colors : Replace with your own colours---
//-**********---------- WHATSAPP Color Theme: -------------------------
final fiberchatBlack = new Color(0xFF1E1E1E);
final fiberchatBlue = new Color(0xFF02ac88);
final fiberchatDeepGreen = new Color(0xFF01826b);
final fiberchatLightGreen = new Color(0xFF02ac88);
final fiberchatgreen = new Color(0xFF098b74);
final fiberchatteagreen = new Color(0xFFe9fedf);
final fiberchatWhite = Colors.white;
final fiberchatGrey = Color(0xff85959f);
final fiberchatChatbackground = new Color(0xffe8ded5);
const DESIGN_TYPE = Themetype.whatsapp;
const IsSplashOnlySolidColor = false;
const SplashBackgroundSolidColor = Color(
    0xFF01826b); //applies this colors to fill the areas around splash screen.  Color Code: 0xFF01826b for Whatsapp theme & 0xFFFFFFFF for messenger theme.
final statusBarColor = fiberchatDeepGreen;
final isDarkIconsinStatusBar =
    false; // This Color will be applied to status bar across the App if App is messenger theme. For whatsapp theme, it picks the color automatically.

//-*********---------- MESSENGER Color Theme: ---------------// Remove below comments for Messenger theme //------------
// final fiberchatBlack = new Color(0xFF353f58);
// final fiberchatBlue = new Color(0xFF3d9df5);
// final fiberchatDeepGreen = new Color(0xFF296ac6);
// final fiberchatLightGreen = new Color(0xFF036eff);
// final fiberchatgreen = new Color(0xFF06a2ff);
// final fiberchatteagreen = Color(0xFFeefcf8);
// final fiberchatWhite = Colors.white;
// final fiberchatGrey = Colors.grey;
// final fiberchatChatbackground = new Color(0xffdde6ea);
// const DESIGN_TYPE = Themetype.messenger;
// const IsSplashOnlySolidColor = false;
// const SplashBackgroundSolidColor = Color(
//     0xFFFFFFFF); //applies this colors if "IsSplashOnlySolidColor" is set to true. Color Code: 0xFF005f56 for Whatsapp theme & 0xFFFFFFFF for messenger theme.
// final statusBarColor = fiberchatWhite;
// final isDarkIconsinStatusBar =
//     true; // This Color will be applied to status bar across the App if App is messenger theme. For whatsapp theme, it picks the color automatically.

//*--Admob Configurations- (By default Test Ad Units pasted)----------
const IsBannerAdShow =
    false; // Set this to 'true' if you want to show Banner ads throughout the app
const Admob_BannerAdUnitID_Android =
    'ca-app-pub-3940256099942544/6300978111'; // Test Id: 'ca-app-pub-3940256099942544/6300978111'
const Admob_BannerAdUnitID_Ios =
    'ca-app-pub-3940256099942544/2934735716'; // Test Id: 'ca-app-pub-3940256099942544/2934735716'
const IsInterstitialAdShow =
    false; // Set this to 'true' if you want to show Interstitial ads throughout the app
const Admob_InterstitialAdUnitID_Android =
    'ca-app-pub-3940256099942544/1033173712'; // Test Id:  'ca-app-pub-3940256099942544/1033173712'
const Admob_InterstitialAdUnitID_Ios =
    'ca-app-pub-3940256099942544/4411468910'; // Test Id: 'ca-app-pub-3940256099942544/4411468910'
const IsVideoAdShow =
    false; // Set this to 'true' if you want to show Video ads throughout the app
const Admob_RewardedAdUnitID_Android =
    'ca-app-pub-3940256099942544/5224354917'; // Test Id: 'ca-app-pub-3940256099942544/5224354917'
const Admob_RewardedAdUnitID_Ios =
    'ca-app-pub-3940256099942544/1712485313'; // Test Id: 'ca-app-pub-3940256099942544/1712485313'
//Also don't forget to Change the Admob App Id in "fiberchat/android/app/src/main/AndroidManifest.xml" & "fiberchat/ios/Runner/Info.plist"

//*--Agora Configurations---
const Agora_APP_IDD =
    'PASTE YOUR AGORA APP ID HERE'; // Grab it from: https://www.agora.io/en/
const dynamic Agora_TOKEN =
    null; // not required until you have planned to setup high level of authentication of users in Agora.

//*--Giphy Configurations---
const GiphyAPIKey =
    'PASTE YOUR GIPHY API KEY'; // Grab it from: https://developers.giphy.com/

//*--App Configurations---
const Appname =
    'Fiberchat'; //app name shown evrywhere with the app where required
const DEFAULT_COUNTTRYCODE_ISO =
    'US'; //default country ISO 2 letter for login screen
const DEFAULT_COUNTTRYCODE_NUMBER =
    '+1'; //default country code number for login screen
const FONTFAMILY_NAME =
    null; // make sure you have registered the font in pubspec.yaml

//--WARNING----- PLEASE DONT EDIT THE BELOW LINES UNLESS YOU ARE A DEVELOPER -------
const SplashPath = 'assets/images/splash.jpeg';
const AppLogoPath = 'assets/images/applogo.png';
