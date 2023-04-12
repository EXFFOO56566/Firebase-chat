import 'package:chat/models/FontSizeModel.dart';
import 'package:chat/utils/AppCommon.dart';
import 'package:flutter/material.dart';
import 'package:flutter_mobx/flutter_mobx.dart';
import 'package:nb_utils/nb_utils.dart';

import 'AppConstants.dart';

List<PopupMenuItem> dashboardPopUpMenuItem = [
  PopupMenuItem(value: 1, child: Observer(builder: (_) => Text('new_chat'.translate, style: primaryTextStyle()))),
  PopupMenuItem(value: 2, child: Observer(builder: (_) => Text('settings'.translate, style: primaryTextStyle()))),
];

List<PopupMenuItem> chatScreenPopUpMenuItem = [
  PopupMenuItem(value: 1, child: Observer(builder: (_) => Text('view_Contact'.translate, style: primaryTextStyle()))),
  PopupMenuItem(value: 2, child: Observer(builder: (_) => Text('report'.translate, style: primaryTextStyle()))),
  PopupMenuItem(value: 3, child: Observer(builder: (_) => Text('block'.translate, style: primaryTextStyle()))),
  PopupMenuItem(value: 4, child: Observer(builder: (_) => Text('clear_Chat'.translate, style: primaryTextStyle()))),
];

List<PopupMenuItem> chatLogPopUpMenuItem = [
  PopupMenuItem(value: 1, child: Observer(builder: (_) => Text('clear_Call_Logs'.translate, style: primaryTextStyle()))),
];

List<FontSizeModel> fontSizes() {
  List<FontSizeModel> list = [];

  list.add(FontSizeModel(fontSize: FONT_SIZE_SMALL, name: 'small'.translate));
  list.add(FontSizeModel(fontSize: FONT_SIZE_MEDIUM, name: 'medium'.translate));
  list.add(FontSizeModel(fontSize: FONT_SIZE_LARGE, name: 'large'.translate));

  return list;
}
