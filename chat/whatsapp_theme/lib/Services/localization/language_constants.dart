//*************   © Copyrighted by Thinkcreative_Technologies. An Exclusive item of Envato market. Make sure you have purchased a Regular License OR Extended license for the Source Code from Envato to use this product. See the License Defination attached with source code. *********************

import 'package:fiberchat/Configs/optional_constants.dart';
import 'package:fiberchat/Services/localization/demo_localization.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

// ignore: todo
//TODO:---- All localizations settings----
const String LAGUAGE_CODE = 'languageCode';

//languages code
const String ENGLISH = 'en';
const String VIETNAMESE = 'vi';
const String ARABIC = 'ar';
const String HINDI = 'hi';
const String GERMAN = 'de';
const String SPANISH = 'es';
const String FRENCH = 'fr';
const String INDONESIAN = 'id';
const String JAPANESE = 'ja';
const String KOREAN = 'ko';
const String TURKISH = 'tr';
const String CHINESE = 'zh';
const String DUTCH = 'nl';
const String BANGLA = 'bn';
//----
const String PORTUGUESE = 'pt';
const String URDU = 'ur';
const String SWAHILI = 'sw';
const String RUSSIAN = 'ru';
const String PERSIAN = 'fa';
const String GEORGIAN = 'ka';
const String MALAY = 'my';

List languagelist = [
  ENGLISH,
  // BANGLA,
  // ARABIC,
  // HINDI,
  // GERMAN,
  // SPANISH,
  // FRENCH,
  // INDONESIAN,
  // JAPANESE,
  // KOREAN,
  // TURKISH,
  // CHINESE,
  // VIETNAMESE,
  // DUTCH,
  // //-----
  // URDU,
  // PORTUGUESE,
  // SWAHILI,
  // RUSSIAN,
  // PERSIAN,
  // GEORGIAN,
  // MALAY
];
List<Locale> supportedlocale = [
  Locale(ENGLISH, "US"),
  // Locale(ARABIC, "SA"),
  // Locale(HINDI, "IN"),
  // Locale(BANGLA, "BD"),
  // Locale(GERMAN, "DE"),
  // Locale(SPANISH, "ES"),
  // Locale(FRENCH, "FR"),
  // Locale(INDONESIAN, "ID"),
  // Locale(JAPANESE, "JP"),
  // Locale(KOREAN, "KR"),
  // Locale(TURKISH, "TR"),
  // Locale(CHINESE, "CN"),
  // Locale(VIETNAMESE, 'VN'),
  // Locale(DUTCH, 'NZ'),
  // //----
  // Locale(URDU, 'PK'),
  // Locale(PORTUGUESE, 'PT'),
  // Locale(SWAHILI, 'KE'),
  // Locale(RUSSIAN, 'RU'),
  // Locale(PERSIAN, 'IR'),
  // Locale(GEORGIAN, 'GE'),
  // Locale(MALAY, 'MY'),
];

Future<Locale> setLocale(String languageCode) async {
  print(languageCode);
  SharedPreferences _prefs = await SharedPreferences.getInstance();
  await _prefs.setString(LAGUAGE_CODE, languageCode);
  return _locale(languageCode);
}

Future<Locale> getLocale() async {
  print(LAGUAGE_CODE);
  SharedPreferences _prefs = await SharedPreferences.getInstance();
  String languageCode =
      _prefs.getString(LAGUAGE_CODE) ?? DEFAULT_LANGUAGE_FILE_CODE;
  return _locale(languageCode);
}

Locale _locale(String languageCode) {
  switch (languageCode) {
    case ENGLISH:
      return Locale(ENGLISH, 'US');
    case BANGLA:
      return Locale(BANGLA, 'BD');
    case VIETNAMESE:
      return Locale(VIETNAMESE, "VN");
    case ARABIC:
      return Locale(ARABIC, "SA");
    case HINDI:
      return Locale(HINDI, "IN");
    case GERMAN:
      return Locale(GERMAN, "DE");
    case SPANISH:
      return Locale(SPANISH, "ES");
    case FRENCH:
      return Locale(FRENCH, "FR");
    case INDONESIAN:
      return Locale(INDONESIAN, "ID");
    case JAPANESE:
      return Locale(JAPANESE, "JP");
    case KOREAN:
      return Locale(KOREAN, "KR");
    case TURKISH:
      return Locale(TURKISH, "TR");
    case DUTCH:
      return Locale(DUTCH, "NZ");
    case CHINESE:
      // return Locale.fromSubtags(languageCode: CHINESE);
      return Locale(CHINESE, "CN");
    //---
    case URDU:
      return Locale(URDU, 'PK');
    case PORTUGUESE:
      return Locale(PORTUGUESE, 'PT');
    case SWAHILI:
      return Locale(SWAHILI, 'KE');
    case RUSSIAN:
      return Locale(RUSSIAN, 'RU');

    case PERSIAN:
      return Locale(PERSIAN, 'IR');
    case GEORGIAN:
      return Locale(GEORGIAN, 'GE');
    case MALAY:
      return Locale(MALAY, 'MY');

    default:
      return Locale(ENGLISH, 'US');
  }
}

String getTranslated(BuildContext context, String key) {
  return DemoLocalization.of(context)!.translate(key) ?? '';
}
