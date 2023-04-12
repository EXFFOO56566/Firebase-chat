import 'package:chat/screens/AboutUsScreen.dart';
import 'package:chat/utils/AppCommon.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:flutter/material.dart';
import 'package:nb_utils/nb_utils.dart';

class SettingHelpScreen extends StatefulWidget {
  @override
  _SettingHelpScreenState createState() => _SettingHelpScreenState();
}

class _SettingHelpScreenState extends State<SettingHelpScreen> {
  @override
  void initState() {
    super.initState();
    init();
  }

  init() async {}

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        appBar: appBarWidget("help".translate, textColor: Colors.white),
        body: ListView(
          children: [
            SettingItemWidget(
              titleTextStyle: primaryTextStyle(),
              leading: Icon(Icons.support),
              title: 'support'.translate,
              subTitle: 'your_quires_are_always_welcomed'.translate,
              onTap: () {
                launchUrl(supportURL);
              },
            ),
            SettingItemWidget(
              titleTextStyle: primaryTextStyle(),
              leading: Icon(Icons.mail_outline),
              title: 'contact_us'.translate,
              subTitle: 'for_all_enquires_please_email_us'.translate,
              onTap: () {
                launchUrl('mailto: $mailto');
              },
            ),
            SettingItemWidget(
              titleTextStyle: primaryTextStyle(),
              leading: Icon(Icons.shopping_cart_outlined),
              title: 'download_source_code'.translate,
              subTitle: 'visit_our_other_products'.translate,
              onTap: () {
                launchUrl(codeCanyonURL);
              },
            ),
            SettingItemWidget(
              titleTextStyle: primaryTextStyle(),
              leading: Icon(Icons.star_rate_outlined),
              title: 'rate_us'.translate,
              subTitle: "your_review_counts".translate,
              onTap: () {
                if (isIos) {
                  launchUrl(appStoreBaseURL);
                } else {
                  launchUrl(playStoreBaseURL + packageName);
                }
              },
            ),
            SettingItemWidget(
              titleTextStyle: primaryTextStyle(),
              leading: Icon(Icons.privacy_tip_outlined),
              title: 'terms_and_conditions'.translate,
              subTitle: "read_our_t_and_c".translate,
            ),
            SettingItemWidget(
              titleTextStyle: primaryTextStyle(),
              leading: Icon(Icons.info_outline),
              title: 'app_info'.translate,
              onTap: () {
                AboutUsScreen().launch(context);
              },
            ),
          ],
        ),
      ),
    );
  }
}
