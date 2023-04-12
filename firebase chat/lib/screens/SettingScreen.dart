import 'package:chat/components/ChatSettingScreeen.dart';
import 'package:chat/components/UserProfileWidget.dart';
import 'package:chat/main.dart';
import 'package:chat/screens/PickupLayout.dart';
import 'package:chat/screens/SettingHelpScreen.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppCommon.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:nb_utils/nb_utils.dart';
import 'package:package_info/package_info.dart';
import 'package:share/share.dart';

class SettingScreen extends StatefulWidget {
  @override
  _SettingScreenState createState() => _SettingScreenState();
}

class _SettingScreenState extends State<SettingScreen> {
  BannerAd? myBanner;

  @override
  void initState() {
    super.initState();
    init();
  }

  init() async {
    myBanner = buildBannerAd()..load();
  }

  BannerAd buildBannerAd() {
    return BannerAd(
      adUnitId: kReleaseMode ? mAdMobBannerId : BannerAd.testAdUnitId,
      size: AdSize.fullBanner,
      listener: BannerAdListener(onAdLoaded: (ad) {
        //
      }),
      request: AdRequest(),
    );
  }

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return PickupLayout(
      child: SafeArea(
        child: Scaffold(
          appBar: appBarWidget("setting".translate, textColor: Colors.white),
          body: Container(
            child: Stack(
              children: [
                ListView(
                  children: [
                    UserProfileWidget(),
                    Divider(height: 0),
                    SettingItemWidget(
                      titleTextStyle: primaryTextStyle(),
                      title: 'chats'.translate,
                      leading: Icon(Icons.chat),
                      subTitle: 'theme_wallpaper'.translate,
                      onTap: () {
                        ChatSettingScreen().launch(context).then((value) {
                          setState(() {});
                        });
                      },
                    ),
                    SettingItemWidget(
                      titleTextStyle: primaryTextStyle(),
                      title: 'notifications'.translate,
                      leading: Icon(Icons.notifications),
                      subTitle: 'message_group_call_tones'.translate,
                      onTap: () {
                        toast(COMING_SOON);
                      },
                    ),
                    SettingItemWidget(
                      titleTextStyle: primaryTextStyle(),
                      title: 'help'.translate,
                      leading: Icon(Icons.help),
                      subTitle: 'contact_us_privacy_policy'.translate,
                      onTap: () {
                        SettingHelpScreen().launch(context);
                      },
                    ),
                    SettingItemWidget(
                      titleTextStyle: primaryTextStyle(),
                      title: 'logout'.translate,
                      subTitle: 'visit_again'.translate,
                      leading: Icon(Icons.logout),
                      onTap: () async {
                        bool? res = await showConfirmDialog(context, "Are you sure you want to logout?", buttonColor: secondaryColor);
                        if (res ?? false) {
                          Map<String, dynamic> presenceStatusFalse = {
                            'isPresence': false,
                            'lastSeen': DateTime.now().millisecondsSinceEpoch,
                            'oneSignalPlayerId': '',
                          };
                          userService.updateUserStatus(presenceStatusFalse, getStringAsync(userId));
                          authService.logout(context);
                        }
                      },
                    ),
                    Divider(indent: 55),
                    SettingItemWidget(
                      titleTextStyle: primaryTextStyle(),
                      title: 'invite_a_friend'.translate,
                      leading: Icon(Icons.group),
                      onTap: () {
                        PackageInfo.fromPlatform().then((value) {
                          Share.share('Share $AppName app\n\n$playStoreBaseURL${value.packageName}');
                        });
                      },
                    ),
                    32.height,
                    Text('from'.translate, style: secondaryTextStyle()).center(),
                    4.height,
                    Text('MeetMighty', style: boldTextStyle(letterSpacing: 2)).center(),
                  ],
                ),
                if (myBanner != null)
                  Positioned(
                    child: AdWidget(ad: myBanner!),
                    bottom: 0,
                    height: AdSize.banner.height.toDouble(),
                    width: context.width(),
                  ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
