import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:fiberchat/Configs/Dbkeys.dart';
import 'package:fiberchat/Configs/Dbpaths.dart';
import 'package:fiberchat/Configs/Enum.dart';
import 'package:fiberchat/Configs/app_constants.dart';
import 'package:fiberchat/Models/DataModel.dart';
import 'package:fiberchat/Screens/Groups/GroupChatPage.dart';
import 'package:fiberchat/Screens/call_history/callhistory.dart';
import 'package:fiberchat/Screens/recent_chats/RecentsChats.dart';
import 'package:fiberchat/Screens/recent_chats/widgets/getLastMessageTime.dart';
import 'package:fiberchat/Screens/recent_chats/widgets/getMediaMessage.dart';
import 'package:fiberchat/Services/Providers/AvailableContactsProvider.dart';
import 'package:fiberchat/Services/localization/language_constants.dart';
import 'package:fiberchat/Utils/unawaited.dart';
import 'package:fiberchat/Utils/late_load.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

Widget groupMessageTile(
    {required BuildContext context,
    required List<Map<String, dynamic>> streamDocSnap,
    required int index,
    required String currentUserNo,
    required SharedPreferences prefs,
    required DataModel cachedModel,
    required int unRead,
    required bool isGroupChatMuted}) {
  showMenuForGroupChat(contextForDialog, var groupDoc) {
    List<Widget> tiles = List.from(<Widget>[]);
    tiles.add(ListTile(
        dense: true,
        leading: Icon(isGroupChatMuted ? Icons.volume_up : Icons.volume_off,
            size: 22),
        title: Text(
          getTranslated(contextForDialog,
              isGroupChatMuted ? 'unmutenotifications' : 'mutenotifications'),
          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
        ),
        onTap: () async {
          Navigator.of(contextForDialog).pop();

          await FirebaseFirestore.instance
              .collection(DbPaths.collectiongroups)
              .doc(streamDocSnap[index][Dbkeys.groupID])
              .update({
            Dbkeys.groupMUTEDMEMBERS: isGroupChatMuted
                ? FieldValue.arrayRemove([currentUserNo])
                : FieldValue.arrayUnion([currentUserNo]),
          }).then((value) async {
            if (isGroupChatMuted == true) {
              await FirebaseMessaging.instance
                  .subscribeToTopic(
                      "GROUP${streamDocSnap[index][Dbkeys.groupID].replaceAll(RegExp('-'), '').substring(1, streamDocSnap[index][Dbkeys.groupID].replaceAll(RegExp('-'), '').toString().length)}")
                  .catchError((err) {
                FirebaseFirestore.instance
                    .collection(DbPaths.collectiongroups)
                    .doc(streamDocSnap[index][Dbkeys.groupID])
                    .update({
                  Dbkeys.groupMUTEDMEMBERS: !isGroupChatMuted
                      ? FieldValue.arrayRemove([currentUserNo])
                      : FieldValue.arrayUnion([currentUserNo]),
                });
              });
            } else {
              await FirebaseMessaging.instance
                  .unsubscribeFromTopic(
                      "GROUP${streamDocSnap[index][Dbkeys.groupID].replaceAll(RegExp('-'), '').substring(1, streamDocSnap[index][Dbkeys.groupID].replaceAll(RegExp('-'), '').toString().length)}")
                  .catchError((err) {
                FirebaseFirestore.instance
                    .collection(DbPaths.collectiongroups)
                    .doc(streamDocSnap[index][Dbkeys.groupID])
                    .update({
                  Dbkeys.groupMUTEDMEMBERS: !isGroupChatMuted
                      ? FieldValue.arrayRemove([currentUserNo])
                      : FieldValue.arrayUnion([currentUserNo]),
                });
              });
            }
          });
        }));
    if (groupDoc[Dbkeys.groupCREATEDBY] == currentUserNo) {
      tiles.add(ListTile(
          dense: true,
          leading: Icon(Icons.delete, size: 22),
          title: Text(
            getTranslated(context, 'deletegroup'),
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          onTap: () async {
            Navigator.of(contextForDialog).pop();
            unawaited(showDialog(
              builder: (BuildContext context) {
                return AlertDialog(
                  title: new Text(getTranslated(context, 'deletegroup')),
                  actions: [
                    // ignore: deprecated_member_use
                    FlatButton(
                      child: Text(
                        getTranslated(context, 'cancel'),
                        style: TextStyle(color: fiberchatgreen, fontSize: 18),
                      ),
                      onPressed: () {
                        Navigator.of(context).pop();
                      },
                    ),
                    // ignore: deprecated_member_use
                    FlatButton(
                      child: Text(
                        getTranslated(context, 'delete'),
                        style: TextStyle(color: Colors.red, fontSize: 18),
                      ),
                      onPressed: () async {
                        Navigator.of(context).pop();

                        Future.delayed(const Duration(milliseconds: 500),
                            () async {
                          String groupId = groupDoc[Dbkeys.groupID];
                          await FirebaseFirestore.instance
                              .collection(DbPaths.collectiongroups)
                              .doc(groupId)
                              .get()
                              .then((doc) async {
                            await FirebaseFirestore.instance
                                .collection(
                                    DbPaths.collectiontemptokensforunsubscribe)
                                .doc(groupId)
                                .delete();
                            await doc.reference.delete();
                          });

                          //No need to delete the media data from here as it will be deleted automatically using Cloud functions deployed in Firebase once the .doc is deleted .
                        });
                      },
                    )
                  ],
                );
              },
              context: context,
            ));
          }));
    } else {
      tiles.add(ListTile(
          dense: true,
          leading: Icon(Icons.remove_circle_outlined, size: 22),
          title: Text(
            getTranslated(context, 'leavegroup'),
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          onTap: () async {
            Navigator.of(contextForDialog).pop();
            unawaited(showDialog(
              builder: (BuildContext context) {
                return AlertDialog(
                  title: new Text(getTranslated(context, 'leavegroup')),
                  actions: [
                    // ignore: deprecated_member_use
                    FlatButton(
                      child: Text(
                        getTranslated(context, 'cancel'),
                        style: TextStyle(color: fiberchatgreen, fontSize: 18),
                      ),
                      onPressed: () {
                        Navigator.of(context).pop();
                      },
                    ),
                    // ignore: deprecated_member_use
                    FlatButton(
                      child: Text(
                        getTranslated(context, 'leave'),
                        style: TextStyle(color: Colors.red, fontSize: 18),
                      ),
                      onPressed: () async {
                        Navigator.of(context).pop();
                        Future.delayed(const Duration(milliseconds: 300),
                            () async {
                          String groupId = groupDoc[Dbkeys.groupID];
                          DateTime time = DateTime.now();
                          try {
                            await FirebaseFirestore.instance
                                .collection(
                                    DbPaths.collectiontemptokensforunsubscribe)
                                .doc(currentUserNo)
                                .delete();
                          } catch (err) {}
                          await FirebaseFirestore.instance
                              .collection(
                                  DbPaths.collectiontemptokensforunsubscribe)
                              .doc(currentUserNo)
                              .set({
                            Dbkeys.groupIDfiltered:
                                '${groupId.replaceAll(RegExp('-'), '').substring(1, groupId.replaceAll(RegExp('-'), '').toString().length)}',
                            Dbkeys.notificationTokens: cachedModel
                                    .currentUser![Dbkeys.notificationTokens] ??
                                [],
                            'type': 'unsubscribe'
                          }).then((value) async {
                            await FirebaseFirestore.instance
                                .collection(DbPaths.collectiongroups)
                                .doc(groupId)
                                .update(groupDoc[Dbkeys.groupADMINLIST]
                                        .contains(currentUserNo)
                                    ? {
                                        Dbkeys.groupADMINLIST:
                                            FieldValue.arrayRemove(
                                                [currentUserNo]),
                                        Dbkeys.groupMEMBERSLIST:
                                            FieldValue.arrayRemove(
                                                [currentUserNo]),
                                        currentUserNo: FieldValue.delete(),
                                        '$currentUserNo-joinedOn':
                                            FieldValue.delete()
                                      }
                                    : {
                                        Dbkeys.groupMEMBERSLIST:
                                            FieldValue.arrayRemove(
                                                [currentUserNo]),
                                        currentUserNo: FieldValue.delete(),
                                        '$currentUserNo-joinedOn':
                                            FieldValue.delete()
                                      });

                            await FirebaseFirestore.instance
                                .collection(DbPaths.collectiongroups)
                                .doc(groupId)
                                .collection(DbPaths.collectiongroupChats)
                                .doc(time.millisecondsSinceEpoch.toString() +
                                    '--' +
                                    groupId)
                                .set({
                              Dbkeys.groupmsgCONTENT:
                                  '$currentUserNo ${getTranslated(context, 'leftthegroup')}',
                              Dbkeys.groupmsgLISToptional: [],
                              Dbkeys.groupmsgTIME: time.millisecondsSinceEpoch,
                              Dbkeys.groupmsgSENDBY: currentUserNo,
                              Dbkeys.groupmsgISDELETED: false,
                              Dbkeys.groupmsgTYPE:
                                  Dbkeys.groupmsgTYPEnotificationUserLeft,
                            });

                            try {
                              await FirebaseFirestore.instance
                                  .collection(DbPaths
                                      .collectiontemptokensforunsubscribe)
                                  .doc(currentUserNo)
                                  .delete();
                            } catch (err) {}
                          }).catchError((err) {
                            // Fiberchat.toast(
                            //     getTranslated(context,
                            //         'unabletoleavegrp'));
                          });
                        });
                      },
                    )
                  ],
                );
              },
              context: context,
            ));
          }));
    }
    showDialog(
        context: contextForDialog,
        builder: (contextForDialog) {
          return SimpleDialog(children: tiles);
        });
  }

  return Theme(
    data: ThemeData(
        fontFamily: FONTFAMILY_NAME,
        splashColor: Colors.transparent,
        highlightColor: Colors.transparent),
    child: streamLoadCollections(
        stream: FirebaseFirestore.instance
            .collection(DbPaths.collectiongroups)
            .doc(streamDocSnap[index][Dbkeys.groupID])
            .collection(DbPaths.collectiongroupChats)
            .where(Dbkeys.groupmsgTYPE, whereIn: [
              MessageType.text.index,
              MessageType.image.index,
              MessageType.doc.index,
              MessageType.audio.index,
              MessageType.video.index,
              MessageType.contact.index,
              MessageType.location.index
            ])
            .orderBy(Dbkeys.timestamp, descending: true)
            .limit(1)
            .snapshots(),
        placeholder: SizedBox(),
        noDataWidget: Column(
          children: [
            ListTile(
                onLongPress: () {
                  showMenuForGroupChat(context, streamDocSnap[index]);
                },
                contentPadding: EdgeInsets.fromLTRB(20, 0, 20, 0),
                leading: customCircleAvatarGroup(
                    url: streamDocSnap[index][Dbkeys.groupPHOTOURL],
                    radius: 22),
                title: Text(
                  streamDocSnap[index][Dbkeys.groupNAME],
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(
                    color: fiberchatBlack,
                    fontWeight: FontWeight.w600,
                    fontSize: 16.4,
                  ),
                ),
                subtitle: Text(
                  '${streamDocSnap[index][Dbkeys.groupMEMBERSLIST].length} ${getTranslated(context, 'participants')}',
                  style: TextStyle(
                    color: lightGrey,
                    fontSize: 14,
                  ),
                ),
                onTap: () {
                  Navigator.push(
                      context,
                      new MaterialPageRoute(
                          builder: (context) => new GroupChatPage(
                              isCurrentUserMuted: isGroupChatMuted,
                              isSharingIntentForwarded: false,
                              model: cachedModel,
                              prefs: prefs,
                              joinedTime: streamDocSnap[index]
                                  ['$currentUserNo-joinedOn'],
                              currentUserno: currentUserNo,
                              groupID: streamDocSnap[index][Dbkeys.groupID])));
                },
                trailing: Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    unRead == 0
                        ? SizedBox()
                        : Container(
                            child: Text(unRead.toString(),
                                style: TextStyle(
                                    fontSize: 14,
                                    color: Colors.white,
                                    fontWeight: FontWeight.bold)),
                            padding: const EdgeInsets.all(7.0),
                            decoration: new BoxDecoration(
                              shape: BoxShape.circle,
                              color: Colors.green[400],
                            ),
                          ),
                    SizedBox(
                      height: 3,
                    ),
                  ],
                )),
            Divider(
              height: 0,
            ),
          ],
        ),
        onfetchdone: (messages) {
          var lastMessage = messages.last;

          return Column(
            children: [
              ListTile(
                  onLongPress: () {
                    showMenuForGroupChat(context, streamDocSnap[index]);
                  },
                  contentPadding: EdgeInsets.fromLTRB(20, 0, 20, 0),
                  leading: customCircleAvatarGroup(
                      url: streamDocSnap[index][Dbkeys.groupPHOTOURL],
                      radius: 22),
                  title: Text(
                    streamDocSnap[index][Dbkeys.groupNAME],
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      color: fiberchatBlack,
                      fontWeight: FontWeight.w600,
                      fontSize: 16.4,
                    ),
                  ),
                  subtitle: Row(
                    mainAxisAlignment: MainAxisAlignment.start,
                    children: [
                      lastMessage[Dbkeys.groupmsgSENDBY] == currentUserNo
                          ? SizedBox()
                          : Consumer<AvailableContactsProvider>(
                              builder: (context, availableContacts, _child) {
                              // _filtered = availableContacts.filtered;
                              return FutureBuilder<Map<String, dynamic>>(
                                  future: availableContacts.getUserDoc(
                                      lastMessage[Dbkeys.groupmsgSENDBY]),
                                  builder: (BuildContext context,
                                      AsyncSnapshot<Map<String, dynamic>>
                                          snapshot) {
                                    if (snapshot.hasData) {
                                      return Text(
                                          "${snapshot.data![Dbkeys.nickname]}:  ",
                                          style: TextStyle(
                                            fontSize: 14,
                                            color: unRead > 0
                                                ? darkGrey
                                                : lightGrey,
                                          ));
                                    }
                                    return Text(
                                        "${lastMessage[Dbkeys.groupmsgSENDBY]}:  ",
                                        style: TextStyle(
                                          fontSize: 14,
                                          color:
                                              unRead > 0 ? darkGrey : lightGrey,
                                        ));
                                  });
                            }),
                      lastMessage[Dbkeys.groupmsgISDELETED] == true
                          ? Text(getTranslated(context, "msgdeleted"),
                              style: TextStyle(
                                  fontSize: 14,
                                  color: unRead > 0
                                      ? darkGrey.withOpacity(0.4)
                                      : lightGrey.withOpacity(0.4),
                                  fontStyle: FontStyle.italic))
                          : lastMessage[Dbkeys.groupmsgTYPE] ==
                                  MessageType.text.index
                              ? Container(
                                  width: lastMessage[Dbkeys.groupmsgSENDBY] ==
                                          currentUserNo
                                      ? MediaQuery.of(context).size.width / 2.7
                                      : MediaQuery.of(context).size.width / 4.1,
                                  child: Text(
                                      lastMessage[Dbkeys.groupmsgCONTENT],
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                      style: TextStyle(
                                          fontSize: 14,
                                          fontWeight: unRead > 0
                                              ? FontWeight.w600
                                              : FontWeight.normal,
                                          color: unRead > 0
                                              ? darkGrey
                                              : lightGrey)),
                                )
                              : getMediaMessage(context, false, lastMessage),
                    ],
                  ),
                  onTap: () {
                    Navigator.push(
                        context,
                        new MaterialPageRoute(
                            builder: (context) => new GroupChatPage(
                                isCurrentUserMuted: isGroupChatMuted,
                                isSharingIntentForwarded: false,
                                model: cachedModel,
                                prefs: prefs,
                                joinedTime: streamDocSnap[index]
                                    ['$currentUserNo-joinedOn'],
                                currentUserno: currentUserNo,
                                groupID: streamDocSnap[index]
                                    [Dbkeys.groupID])));
                  },
                  trailing: Column(
                    mainAxisSize: MainAxisSize.min,
                    crossAxisAlignment: CrossAxisAlignment.end,
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      lastMessage == {} || lastMessage == null
                          ? SizedBox()
                          : Padding(
                              padding: const EdgeInsets.only(top: 2),
                              child: Text(
                                getLastMessageTime(context, currentUserNo,
                                    lastMessage[Dbkeys.timestamp]),
                                style: TextStyle(
                                    color:
                                        unRead != 0 ? Colors.green : lightGrey,
                                    fontWeight: FontWeight.w400,
                                    fontSize: 12),
                              ),
                            ),
                      SizedBox(
                        height: 6,
                      ),
                      Row(
                        mainAxisSize: MainAxisSize.min,
                        mainAxisAlignment: MainAxisAlignment.end,
                        crossAxisAlignment: CrossAxisAlignment.center,
                        children: [
                          isGroupChatMuted
                              ? Icon(
                                  Icons.volume_off,
                                  size: 20,
                                  color: lightGrey.withOpacity(0.5),
                                )
                              : Icon(
                                  Icons.volume_up,
                                  size: 20,
                                  color: Colors.transparent,
                                ),
                          unRead == 0
                              ? SizedBox()
                              : Container(
                                  margin: EdgeInsets.only(
                                      left: isGroupChatMuted ? 7 : 0),
                                  child: Text(unRead.toString(),
                                      style: TextStyle(
                                          fontSize: 14,
                                          color: Colors.white,
                                          fontWeight: FontWeight.bold)),
                                  padding: const EdgeInsets.all(7.0),
                                  decoration: new BoxDecoration(
                                    shape: BoxShape.circle,
                                    color: Colors.green[400],
                                  ),
                                ),
                        ],
                      ),
                    ],
                  )),
              Divider(
                height: 0,
              ),
            ],
          );
        }),
  );
}
