import 'package:chat/models/UserModel.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppCommon.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:flutter/material.dart';
import 'package:nb_utils/nb_utils.dart';

import '../main.dart';

class ChatOptionDialog extends StatefulWidget {
  final UserModel? receiverUser;

  ChatOptionDialog({this.receiverUser});

  @override
  ChatOptionDialogState createState() => ChatOptionDialogState();
}

class ChatOptionDialogState extends State<ChatOptionDialog> {
  List<String> chatOptionList = ['clear_chat'.translate, 'delete_chat'.translate, 'pin_chat'.translate];

  int currentIndex = 0;

  UserModel sender = UserModel(
    name: getStringAsync(userDisplayName),
    photoUrl: getStringAsync(userPhotoUrl),
    uid: getStringAsync(userId),
    oneSignalPlayerId: getStringAsync(playerId),
  );

  @override
  void initState() {
    super.initState();
    init();
  }

  Future<void> init() async {
    currentIndex = getIntAsync(THEME_MODE_INDEX);
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
        itemCount: chatOptionList.length,
        itemBuilder: (BuildContext context, int index) {
          return ListTile(
            onTap: () async {
              if (chatOptionList[index] == 'Clear Chat') {
                bool? res = await showConfirmDialog(context, "chat_cleared".translate, buttonColor: secondaryColor);
                if (res ?? false) {
                  appStore.setLoading(true);
                  chatMessageService.clearAllMessages(senderId: sender.uid, receiverId: widget.receiverUser!.uid!).then((value) {
                    toast("Chat cleared");
                    hideKeyboard(context);
                    appStore.setLoading(false);
                    finish(context);
                  }).catchError((e) {
                    toast(e);
                  });
                }
              } else if (chatOptionList[index] == 'Delete Chat') {
                bool? res = await showConfirmDialog(context, "chat_cleared_deleted".translate, buttonColor: secondaryColor);
                if (res ?? false) {
                  chatMessageService.deleteChat(senderId: sender.uid, receiverId: widget.receiverUser!.uid!).then((value) {
                    toast("Chat deleted");
                    chatMessageService.clearAllMessages(senderId: sender.uid, receiverId: widget.receiverUser!.uid!).then((value) => null).catchError((e) {
                      toast(e.toString());
                    });
                    hideKeyboard(context);
                    appStore.setLoading(false);
                    finish(context);
                  }).catchError((e) {
                    toast(e);
                  });
                }
              } else {
                toast(COMING_SOON);
                finish(context);
              }
            },
            title: Text(chatOptionList[index], style: primaryTextStyle()),
          );
        },
      ),
    );
  }
}
