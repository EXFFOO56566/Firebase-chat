import 'package:chat/components/AppLanguageDialog.dart';
import 'package:chat/components/FontSelectionDialog.dart';
import 'package:chat/components/ThemeSelectionDialog.dart';
import 'package:chat/main.dart';
import 'package:chat/screens/PickupLayout.dart';
import 'package:chat/screens/WallpaperScreen.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppCommon.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:chat/utils/AppLocalizations.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:nb_utils/nb_utils.dart';

class ChatSettingScreen extends StatefulWidget {
  @override
  _ChatSettingScreenState createState() => _ChatSettingScreenState();
}

class _ChatSettingScreenState extends State<ChatSettingScreen> {
  bool _isEnterKey = false;

  @override
  void initState() {
    super.initState();
    init();
  }

  init() async {
    _isEnterKey = getBoolAsync(IS_ENTER_KEY);
    setState(() {});
  }

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  Widget build(BuildContext context) {
    appLocalizations = AppLocalizations.of(context);

    return PickupLayout(
      child: SafeArea(
        child: Scaffold(
          appBar: appBarWidget('chats'.translate, textColor: Colors.white),
          body: ListView(
            shrinkWrap: true,
            children: [
              SettingItemWidget(
                title: 'display'.translate,
              ),
              SettingItemWidget(
                leading: Icon(Icons.wb_sunny),
                title: 'theme'.translate,
                subTitle: getThemeModeString(getIntAsync(THEME_MODE_INDEX)),
                onTap: () async {
                  await showInDialog(
                    context,
                    child: ThemeSelectionDialog(),
                    contentPadding: EdgeInsets.zero,
                    title: Text("select_theme".translate, style: boldTextStyle(size: 20)),
                  );
                  setState(() {});
                },
              ),
              SettingItemWidget(
                leading: Icon(Icons.wallpaper),
                title: 'wallpaper'.translate,
                onTap: () {
                  WallpaperScreen().launch(context);
                },
              ),
              Divider(thickness: 1, height: 0),
              SettingItemWidget(
                title: 'chat_settings'.translate,
              ),
              SwitchListTile(
                contentPadding: EdgeInsets.only(right: 16.0, left: 70.0, top: 0.0, bottom: 00.0),
                title: Text('enter_is_send'.translate, style: boldTextStyle()),
                subtitle: Text('enter_key_will_send_your_message'.translate, style: secondaryTextStyle()),
                value: _isEnterKey,
                activeColor: secondaryColor,
                onChanged: (v) {
                  _isEnterKey = v;
                  setBoolAsync(IS_ENTER_KEY, v);
                  setState(() {});
                },
              ),
              SettingItemWidget(
                padding: EdgeInsets.only(right: 16.0, left: 70.0, top: 12.0, bottom: 12.0),
                title: 'font_size'.translate,
                subTitle: getFontSizeString(getIntAsync(FONT_SIZE_INDEX, defaultValue: 1)),
                onTap: () async {
                  await showInDialog(
                    context,
                    child: FontSelectionDialog(),
                    contentPadding: EdgeInsets.zero,
                    title: Text("font_size".translate, style: boldTextStyle(size: 20)),
                  );
                  setState(() {});
                },
              ),
              Divider(thickness: 1, height: 0),
              SettingItemWidget(
                leading: Icon(Icons.language),
                title: 'app_language'.translate,
                onTap: () {
                  return showInDialog(
                    context,
                    child: AppLanguageDialog(),
                    contentPadding: EdgeInsets.zero,
                    title: Text("app_language".translate, style: boldTextStyle(size: 20)),
                  );
                },
                subTitle: "${language!.name.validate()}",
              ),
            ],
          ),
        ),
      ),
    );
  }
}
