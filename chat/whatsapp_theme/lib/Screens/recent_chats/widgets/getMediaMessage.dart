import 'package:fiberchat/Configs/Dbkeys.dart';
import 'package:fiberchat/Configs/Enum.dart';
import 'package:fiberchat/Screens/recent_chats/RecentsChats.dart';
import 'package:fiberchat/Services/localization/language_constants.dart';
import 'package:flutter/material.dart';

Widget getMediaMessage(BuildContext context, bool isBold, var lastMessage) {
  Color textColor = isBold ? darkGrey : lightGrey;
  Color iconColor = isBold ? darkGrey : lightGrey;
  TextStyle style = TextStyle(
    color: textColor,
    fontWeight: isBold ? FontWeight.w600 : FontWeight.normal,
  );
  return lastMessage![Dbkeys.messageType] == MessageType.doc.index
      ? Row(
          children: [
            Icon(Icons.file_copy, size: 17.7, color: iconColor),
            SizedBox(
              width: 4,
            ),
            Text(
              getTranslated(context, "doc"),
              style: style,
              maxLines: 1,
            ),
          ],
        )
      : lastMessage[Dbkeys.messageType] == MessageType.audio.index
          ? Row(
              children: [
                Icon(Icons.mic, size: 17.7, color: iconColor),
                SizedBox(
                  width: 4,
                ),
                Text(
                  getTranslated(context, "audio"),
                  style: style,
                  maxLines: 1,
                ),
              ],
            )
          : lastMessage[Dbkeys.messageType] == MessageType.location.index
              ? Row(
                  children: [
                    Icon(Icons.location_on, size: 17.7, color: iconColor),
                    SizedBox(
                      width: 4,
                    ),
                    Text(
                      getTranslated(context, "location"),
                      style: style,
                      maxLines: 1,
                    ),
                  ],
                )
              : lastMessage[Dbkeys.messageType] == MessageType.contact.index
                  ? Row(
                      children: [
                        Icon(Icons.contact_page, size: 17.7, color: iconColor),
                        SizedBox(
                          width: 4,
                        ),
                        Text(
                          getTranslated(context, "contact"),
                          style: style,
                          maxLines: 1,
                        ),
                      ],
                    )
                  : lastMessage[Dbkeys.messageType] == MessageType.video.index
                      ? Row(
                          children: [
                            Icon(Icons.videocam, size: 18, color: iconColor),
                            SizedBox(
                              width: 4,
                            ),
                            Text(
                              getTranslated(context, "video"),
                              style: style,
                              maxLines: 1,
                            ),
                          ],
                        )
                      : lastMessage[Dbkeys.messageType] ==
                              MessageType.image.index
                          ? Row(
                              children: [
                                Icon(Icons.image, size: 16, color: iconColor),
                                SizedBox(
                                  width: 4,
                                ),
                                Text(
                                  getTranslated(context, "image"),
                                  style: style,
                                  maxLines: 1,
                                ),
                              ],
                            )
                          : SizedBox();
}
