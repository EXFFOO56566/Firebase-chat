import 'package:chat/main.dart';
import 'package:chat/screens/DashboardScreen.dart';
import 'package:chat/screens/SaveProfileScreen.dart';
import 'package:chat/screens/SignInScreen.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppCommon.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:chat/utils/AppLocalizations.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:nb_utils/nb_utils.dart';

class SplashScreen extends StatefulWidget {
  static String tag = '/SplashScreen';

  @override
  SplashScreenState createState() => SplashScreenState();
}

class SplashScreenState extends State<SplashScreen> with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    init();
  }

  init() async {
    setStatusBarColor(Colors.white);
    await Future.delayed(Duration(seconds: 2));
    appLocalizations = AppLocalizations.of(context);

    finish(context);

    int themeModeIndex = getIntAsync(THEME_MODE_INDEX);
    if (themeModeIndex == ThemeModeSystem) {
      appStore.setDarkMode(MediaQuery.of(context).platformBrightness == Brightness.dark);
    }

    if (getBoolAsync(IS_LOGGED_IN)) {
      loginData();
      if (getStringAsync(userMobileNumber) == null || getStringAsync(userMobileNumber).isEmpty) {
        SaveProfileScreen(mIsShowBack: false, mIsFromLogin: true).launch(context, isNewTask: true);
      } else {
        DashboardScreen().launch(context, isNewTask: true);
      }
    } else {
      SignInScreen().launch(context, isNewTask: true);
    }
  }

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  void dispose() {
    super.dispose();
    WidgetsBinding.instance!.removeObserver(this);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: context.scaffoldBackgroundColor,
      body: Stack(
        children: [
          Image.asset(
            "assets/app_icon.png",
            height: 150,
            width: 150,
          ).center(),
          Positioned(
            bottom: 32,
            left: 0,
            right: 0,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text('From', style: secondaryTextStyle()),
                Text("Meet Mighty", style: primaryTextStyle(size: 24, color: primaryColor)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
