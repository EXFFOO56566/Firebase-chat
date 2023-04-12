import 'package:chat/main.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:chat/utils/AppLocalizations.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:mobx/mobx.dart';
import 'package:nb_utils/nb_utils.dart';

part 'AppStore.g.dart';

class AppStore = AppStoreBase with _$AppStore;

abstract class AppStoreBase with Store {
  @observable
  bool isLoggedIn = false;

  @observable
  bool isDarkMode = false;

  @observable
  bool isLoading = false;

  @observable
  String selectedLanguageCode = defaultLanguage;

  @observable
  AppBarTheme appBarTheme = AppBarTheme();

  @action
  Future<void> setLoggedIn(bool val) async {
    isLoggedIn = val;
    setBoolAsync(IS_LOGGED_IN, val);
  }

  @action
  Future<void> setLoading(bool aVal) async {
    isLoading = aVal;
  }

  @action
  Future<void> setDarkMode(bool aIsDarkMode) async {
    isDarkMode = aIsDarkMode;

    if (isDarkMode) {
      textPrimaryColorGlobal = Colors.white;
      textSecondaryColorGlobal = textSecondaryColor;

      defaultLoaderBgColorGlobal = scaffoldSecondaryDark;
      appButtonBackgroundColorGlobal = appButtonColorDark;
      appBarBackgroundColorGlobal = scaffoldSecondaryDark;
      shadowColorGlobal = Colors.white12;
      setStatusBarColor(scaffoldSecondaryDark, statusBarIconBrightness: Brightness.light, statusBarBrightness: Brightness.light);
    } else {
      textPrimaryColorGlobal = textPrimaryColor;
      textSecondaryColorGlobal = textSecondaryColor;

      defaultLoaderBgColorGlobal = Colors.white;
      appButtonBackgroundColorGlobal = Colors.white;
      appBarBackgroundColorGlobal = primaryColor;
      shadowColorGlobal = Colors.black12;
      setStatusBarColor(primaryColor, statusBarIconBrightness: Brightness.light, statusBarBrightness: Brightness.light);
    }
  }

  @action
  Future<void> setLanguage(String aSelectedLanguageCode, {BuildContext? context}) async {
    selectedLanguageCode = aSelectedLanguageCode;

    language = languages.firstWhere((element) => element.languageCode == aSelectedLanguageCode);
    await setValue(LANGUAGE, aSelectedLanguageCode);

    if (context != null) {
      appLocalizations = AppLocalizations.of(context);
      errorThisFieldRequired = appLocalizations!.translate('field_Required');
    }
  }
}
