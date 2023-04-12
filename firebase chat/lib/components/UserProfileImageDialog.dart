import 'package:chat/components/FullScreenImageWidget.dart';
import 'package:chat/components/Permissions.dart';
import 'package:chat/models/UserModel.dart';
import 'package:chat/screens/ChatScreen.dart';
import 'package:chat/screens/UserProfileScreen.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:chat/utils/CallFunctions.dart';
import 'package:flutter/material.dart';
import 'package:nb_utils/nb_utils.dart';

class UserProfileImageDialog extends StatelessWidget {
  final UserModel? data;

  UserProfileImageDialog({this.data});

  @override
  Widget build(BuildContext context) {
    return Dialog(
      elevation: 0,
      backgroundColor: Colors.transparent,
      child: Stack(
        children: [
          data!.photoUrl == null
              ? Container(
                  height: 50,
                  width: 50,
                  padding: EdgeInsets.all(10),
                  color: primaryColor,
                  child: Text(data!.name.validate()[1].toUpperCase(), style: secondaryTextStyle(color: Colors.white)).center().fit(),
                ).cornerRadiusWithClipRRect(50)
              : InkWell(
                  onTap: () {
                    finish(context);
                    FullScreenImageWidget(photoUrl: data!.photoUrl, name: data!.name).launch(context);
                  },
                  child: Hero(
                    tag: data!.photoUrl.validate(),
                    child: Image.network(data!.photoUrl.validate(), fit: BoxFit.cover, height: 350, width: context.width()),
                  ),
                ),
          Container(
            padding: EdgeInsets.symmetric(horizontal: 10, vertical: 10),
            decoration: BoxDecoration(color: Colors.black26, backgroundBlendMode: BlendMode.luminosity),
            width: double.infinity,
            child: Text(
              data!.name!,
              style: boldTextStyle(color: Colors.white, size: 20),
            ),
          ),
          Positioned(
            bottom: 0,
            right: 0,
            left: 0,
            child: Container(
              height: 45,
              color: context.scaffoldBackgroundColor,
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  IconButton(
                      icon: Icon(Icons.message_rounded),
                      onPressed: () {
                        finish(context);
                        ChatScreen(data).launch(context);
                      }),
                  IconButton(
                      icon: Icon(Icons.call),
                      onPressed: () {
                        toast(COMING_SOON);
                        finish(context);
                      }),
                  IconButton(
                      icon: Icon(Icons.videocam),
                      onPressed: () async {
                        UserModel sender = UserModel(
                          name: getStringAsync(userDisplayName),
                          photoUrl: getStringAsync(userPhotoUrl),
                          uid: getStringAsync(userId),
                          oneSignalPlayerId: getStringAsync(playerId),
                        );
                        // ignore: unnecessary_statements
                        await Permissions.cameraAndMicrophonePermissionsGranted() ? CallFunctions.dial(context: context, from: sender, to: data!) : {};
                      }),
                  IconButton(
                    icon: Icon(Icons.info_outline),
                    onPressed: () {
                      finish(context);
                      UserProfileScreen(user: data, isFromDashboard: true).launch(context);
                    },
                  ),
                ],
              ),
            ),
          )
        ],
      ),
    );
  }
}
