import 'package:chat/models/Language.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:flutter/material.dart';
import 'package:nb_utils/nb_utils.dart';

import '../main.dart';

class AppLanguageDialog extends StatefulWidget {
  static String tag = '/ThemeSelectionDialog';

  @override
  AppLanguageDialogState createState() => AppLanguageDialogState();
}

class AppLanguageDialogState extends State<AppLanguageDialog> {
  int? currentIndex = 0;

  @override
  void initState() {
    super.initState();
    init();
  }

  Future<void> init() async {
    currentIndex = getIntAsync(SELECTED_LANGUAGE, defaultValue: 0);
    setState(() {});
  }

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      width: context.width(),
      padding: EdgeInsets.symmetric(vertical: 16),
      child: ListView.builder(
        shrinkWrap: true,
        itemCount: Language.getLanguages().length,
        itemBuilder: (BuildContext context, int index) {
          Language data = Language.getLanguages()[index];
          return RadioListTile(
            value: index,
            groupValue: currentIndex,
            title: Text(data.name.validate(), style: primaryTextStyle()),
            secondary: Image.asset(data.flag.validate(), width: 30, height: 30),
            onChanged: (dynamic val) async {
              hideKeyboard(context);
              currentIndex = val;
              setIntAsync(SELECTED_LANGUAGE, val);
              appStore.setLanguage(Language.getLanguages()[index].languageCode, context: context);
              finish(context);
            },
          );
        },
      ),
    );
  }
}
