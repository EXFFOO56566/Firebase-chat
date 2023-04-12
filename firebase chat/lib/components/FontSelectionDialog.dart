import 'package:chat/utils/AppConstants.dart';
import 'package:chat/utils/AppDataProvider.dart';
import 'package:flutter/material.dart';
import 'package:nb_utils/nb_utils.dart';

class FontSelectionDialog extends StatefulWidget {
  static String tag = '/ThemeSelectionDialog';

  @override
  FontSelectionDialogState createState() => FontSelectionDialogState();
}

class FontSelectionDialogState extends State<FontSelectionDialog> {
  int? currentIndex = 0;

  @override
  void initState() {
    super.initState();
    init();
  }

  Future<void> init() async {
    currentIndex = getIntAsync(FONT_SIZE_INDEX, defaultValue: 1);
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
        itemCount: fontSizes().length,
        itemBuilder: (BuildContext context, int index) {
          return RadioListTile(
            value: index,
            groupValue: currentIndex,
            title: Text(fontSizes()[index].name.validate(), style: primaryTextStyle()),
            onChanged: (dynamic val) {
              setState(() {
                currentIndex = val;
                setValue(FONT_SIZE_PREF, fontSizes()[index].fontSize.validate());

                setValue(FONT_SIZE_INDEX, val);
              });

              finish(context);
            },
          );
        },
      ),
    );
  }
}
