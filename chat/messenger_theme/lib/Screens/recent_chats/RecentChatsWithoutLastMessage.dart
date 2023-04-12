//*************   © Copyrighted by Thinkcreative_Technologies. An Exclusive item of Envato market. Make sure you have purchased a Regular License OR Extended license for the Source Code from Envato to use this product. See the License Defination attached with source code. *********************

import 'dart:async';
import 'dart:core';
import 'package:fiberchat/Configs/optional_constants.dart';
import 'package:fiberchat/Screens/Broadcast/AddContactsToBroadcast.dart';
import 'package:fiberchat/Utils/setStatusBarColor.dart';
import 'package:fiberchat/main.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:fiberchat/Configs/Dbkeys.dart';
import 'package:fiberchat/Configs/Dbpaths.dart';
import 'package:fiberchat/Configs/app_constants.dart';
import 'package:fiberchat/Screens/Broadcast/BroadcastChatPage.dart';
import 'package:fiberchat/Screens/Groups/AddContactsToGroup.dart';
import 'package:fiberchat/Screens/Groups/GroupChatPage.dart';
import 'package:fiberchat/Screens/contact_screens/SmartContactsPage.dart';
import 'package:fiberchat/Services/Admob/admob.dart';
import 'package:fiberchat/Services/Providers/BroadcastProvider.dart';
import 'package:fiberchat/Services/Providers/GroupChatProvider.dart';
import 'package:fiberchat/Services/Providers/Observer.dart';
import 'package:fiberchat/Services/localization/language_constants.dart';
import 'package:fiberchat/Screens/chat_screen/utils/messagedata.dart';
import 'package:fiberchat/Screens/call_history/callhistory.dart';
import 'package:fiberchat/Screens/chat_screen/chat.dart';
import 'package:fiberchat/Models/DataModel.dart';
import 'package:fiberchat/Services/Providers/user_provider.dart';
import 'package:fiberchat/Utils/alias.dart';
import 'package:fiberchat/Utils/chat_controller.dart';
import 'package:fiberchat/Utils/utils.dart';
import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:scoped_model/scoped_model.dart';
import 'package:fiberchat/Utils/unawaited.dart';

class RecentChatsWithoutLastMessage extends StatefulWidget {
  RecentChatsWithoutLastMessage(
      {required this.currentUserNo,
      required this.isSecuritySetupDone,
      required this.prefs,
      key})
      : super(key: key);
  final String? currentUserNo;
  final SharedPreferences prefs;
  final bool isSecuritySetupDone;
  @override
  State createState() =>
      new RecentChatsWithoutLastMessageState(currentUserNo: this.currentUserNo);
}

class RecentChatsWithoutLastMessageState
    extends State<RecentChatsWithoutLastMessage> {
  RecentChatsWithoutLastMessageState({Key? key, this.currentUserNo}) {
    _filter.addListener(() {
      _userQuery.add(_filter.text.isEmpty ? '' : _filter.text);
    });
  }

  final TextEditingController _filter = new TextEditingController();
  bool isAuthenticating = false;

  List<StreamSubscription> unreadSubscriptions = [];

  List<StreamController> controllers = [];
  final BannerAd myBanner = BannerAd(
    adUnitId: getBannerAdUnitId()!,
    size: AdSize.banner,
    request: AdRequest(),
    listener: BannerAdListener(),
  );
  AdWidget? adWidget;

  @override
  void initState() {
    super.initState();
    Fiberchat.internetLookUp();
    WidgetsBinding.instance.addPostFrameCallback((timeStamp) {
      final observer = Provider.of<Observer>(this.context, listen: false);
      if (IsBannerAdShow == true && observer.isadmobshow == true) {
        myBanner.load();
        adWidget = AdWidget(ad: myBanner);
        setState(() {});
      }
    });
  }

  getuid(BuildContext context) {
    final UserProvider userProvider =
        Provider.of<UserProvider>(context, listen: false);
    userProvider.getUserDetails(currentUserNo);
  }

  void cancelUnreadSubscriptions() {
    unreadSubscriptions.forEach((subscription) {
      subscription.cancel();
    });
  }

  DataModel? _cachedModel;
  bool showHidden = false, biometricEnabled = false;

  String? currentUserNo;

  bool isLoading = false;

  //-- New context menu with Set Alias & Delete Chat tile
  showMenuForOneToOneChat(
    contextForDialog,
    Map<String, dynamic> targetUser,
  ) {
    List<Widget> tiles = List.from(<Widget>[]);

    tiles.add(ListTile(
        dense: true,
        leading: Icon(FontAwesomeIcons.userEdit, size: 18),
        title: Text(
          getTranslated(contextForDialog, 'setalias'),
          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
        ),
        onTap: () async {
          Navigator.of(contextForDialog).pop();

          showDialog(
              context: context,
              builder: (context) {
                return AliasForm(targetUser, _cachedModel);
              });
        }));
    if (IsShowDeleteChatOption == true) {
      tiles.add(ListTile(
          dense: true,
          leading: Icon(Icons.delete, size: 22),
          title: Text(
            getTranslated(contextForDialog, 'deletethischat'),
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          onTap: () async {
            Navigator.of(contextForDialog).pop();
            unawaited(showDialog(
              builder: (BuildContext context) {
                return AlertDialog(
                  title: new Text(getTranslated(context, 'deletethischat')),
                  content: new Text(getTranslated(context, 'suredelete')),
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
                        String chatId = Fiberchat.getChatId(
                            currentUserNo, targetUser[Dbkeys.phone]);

                        if (currentUserNo != null &&
                            targetUser[Dbkeys.phone] != null) {
                          // Fiberchat.toast(
                          //     getTranslated(context, 'plswait'));
                          await FirebaseFirestore.instance
                              .collection(DbPaths.collectionmessages)
                              .doc(chatId)
                              .delete()
                              .then((v) async {
                            await FirebaseFirestore.instance
                                .collection(DbPaths.collectionusers)
                                .doc(currentUserNo)
                                .collection(Dbkeys.chatsWith)
                                .doc(Dbkeys.chatsWith)
                                .set({
                              targetUser[Dbkeys.phone]: FieldValue.delete(),
                            }, SetOptions(merge: true));
                            // print('DELETED CHAT DOC 1');
                            await FirebaseFirestore.instance
                                .collection(DbPaths.collectionusers)
                                .doc(targetUser[Dbkeys.phone])
                                .collection(Dbkeys.chatsWith)
                                .doc(Dbkeys.chatsWith)
                                .set({
                              currentUserNo!: FieldValue.delete(),
                            }, SetOptions(merge: true));
                          }).then((value) {
                            Navigator.of(context).pop();
                            Navigator.of(context).pushAndRemoveUntil(
                              // the new route
                              MaterialPageRoute(
                                builder: (BuildContext context) =>
                                    FiberchatWrapper(),
                              ),

                              (Route route) => false,
                            );
                            // unawaited(Navigator.pushReplacement(
                            //     this.context,
                            //     MaterialPageRoute(
                            //         builder: (newContext) =>
                            //             Homepage(
                            //               currentUserNo:
                            //                   currentUserNo,
                            //               isSecuritySetupDone: true,
                            //               prefs: widget.prefs,
                            //             ))));
                          });
                        } else {
                          Fiberchat.toast('Error Occured. Could not delete !');
                        }
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

  showMenuForBroadcastChat(
    contextForDialog,
    var broadcastDoc,
  ) {
    List<Widget> tiles = List.from(<Widget>[]);

    tiles.add(ListTile(
        dense: true,
        leading: Icon(Icons.delete, size: 22),
        title: Text(
          getTranslated(contextForDialog, 'deletebroadcast'),
          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
        ),
        onTap: () async {
          Navigator.of(contextForDialog).pop();
          unawaited(showDialog(
            builder: (BuildContext context) {
              return AlertDialog(
                title: new Text(getTranslated(context, 'deletebroadcast')),
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
                        await FirebaseFirestore.instance
                            .collection(DbPaths.collectionbroadcasts)
                            .doc(broadcastDoc[Dbkeys.broadcastID])
                            .get()
                            .then((doc) async {
                          await doc.reference.delete();
                          //No need to delete the media data from here as it will be deleted automatically using Cloud functions deployed in Firebase once the .doc is deleted .
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

    showDialog(
        context: contextForDialog,
        builder: (contextForDialog) {
          return SimpleDialog(children: tiles);
        });
  }

  showMenuForGroupChat(contextForDialog, var groupDoc) {
    List<Widget> tiles = List.from(<Widget>[]);

    if (groupDoc[Dbkeys.groupCREATEDBY] == widget.currentUserNo) {
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
                          await FirebaseFirestore.instance
                              .collection(DbPaths.collectiongroups)
                              .doc(groupDoc[Dbkeys.groupID])
                              .get()
                              .then((doc) async {
                            await doc.reference.delete();
                            await FirebaseFirestore.instance
                                .collection(
                                    DbPaths.collectiontemptokensforunsubscribe)
                                .doc(groupDoc[Dbkeys.groupID])
                                .delete();
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
                          DateTime time = DateTime.now();
                          try {
                            await FirebaseFirestore.instance
                                .collection(
                                    DbPaths.collectiontemptokensforunsubscribe)
                                .doc(widget.currentUserNo)
                                .delete();
                          } catch (err) {}
                          await FirebaseFirestore.instance
                              .collection(
                                  DbPaths.collectiontemptokensforunsubscribe)
                              .doc(widget.currentUserNo)
                              .set({
                            Dbkeys.groupIDfiltered:
                                '${groupDoc[Dbkeys.groupID].replaceAll(RegExp('-'), '').substring(1, groupDoc[Dbkeys.groupID].replaceAll(RegExp('-'), '').toString().length)}',
                            Dbkeys.notificationTokens: _cachedModel!
                                    .currentUser![Dbkeys.notificationTokens] ??
                                [],
                            'type': 'unsubscribe'
                          }).then((value) async {
                            await FirebaseFirestore.instance
                                .collection(DbPaths.collectiongroups)
                                .doc(groupDoc[Dbkeys.groupID])
                                .update(groupDoc[Dbkeys.groupADMINLIST]
                                        .contains(widget.currentUserNo)
                                    ? {
                                        Dbkeys.groupADMINLIST:
                                            FieldValue.arrayRemove(
                                                [widget.currentUserNo]),
                                        Dbkeys.groupMEMBERSLIST:
                                            FieldValue.arrayRemove(
                                                [widget.currentUserNo]),
                                        widget.currentUserNo!:
                                            FieldValue.delete(),
                                        '${widget.currentUserNo}-joinedOn':
                                            FieldValue.delete()
                                      }
                                    : {
                                        Dbkeys.groupMEMBERSLIST:
                                            FieldValue.arrayRemove(
                                                [widget.currentUserNo]),
                                        widget.currentUserNo!:
                                            FieldValue.delete(),
                                        '${widget.currentUserNo}-joinedOn':
                                            FieldValue.delete()
                                      });

                            await FirebaseFirestore.instance
                                .collection(DbPaths.collectiongroups)
                                .doc(groupDoc[Dbkeys.groupID])
                                .collection(DbPaths.collectiongroupChats)
                                .doc(time.millisecondsSinceEpoch.toString() +
                                    '--' +
                                    groupDoc[Dbkeys.groupID])
                                .set({
                              Dbkeys.groupmsgCONTENT:
                                  '${widget.currentUserNo} ${getTranslated(context, 'leftthegroup')}',
                              Dbkeys.groupmsgLISToptional: [],
                              Dbkeys.groupmsgTIME: time.millisecondsSinceEpoch,
                              Dbkeys.groupmsgSENDBY: widget.currentUserNo,
                              Dbkeys.groupmsgISDELETED: false,
                              Dbkeys.groupmsgTYPE:
                                  Dbkeys.groupmsgTYPEnotificationUserLeft,
                            });

                            try {
                              await FirebaseFirestore.instance
                                  .collection(DbPaths
                                      .collectiontemptokensforunsubscribe)
                                  .doc(widget.currentUserNo)
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

  Widget buildItem(BuildContext context, Map<String, dynamic> user) {
    if (user[Dbkeys.phone] == currentUserNo) {
      return Container(width: 0, height: 0);
    } else {
      return StreamBuilder(
        stream: getUnread(user).asBroadcastStream(),
        builder: (context, AsyncSnapshot<MessageData> unreadData) {
          int unread = unreadData.hasData &&
                  unreadData.data!.snapshot.docs.isNotEmpty
              ? unreadData.data!.snapshot.docs
                  .where((t) => t[Dbkeys.timestamp] > unreadData.data!.lastSeen)
                  .length
              : 0;
          return Theme(
              data: ThemeData(
                  splashColor: fiberchatGrey.withOpacity(0.2),
                  highlightColor: Colors.transparent),
              child: Column(
                children: [
                  ListTile(
                      contentPadding: EdgeInsets.fromLTRB(20, 8, 20, 8),
                      onLongPress: () {
                        showMenuForOneToOneChat(context, user);
                      },
                      leading:
                          customCircleAvatar(url: user['photoUrl'], radius: 22),
                      title: Text(
                        Fiberchat.getNickname(user)!,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                          color: fiberchatBlack,
                          fontSize: 16,
                        ),
                      ),
                      onTap: () {
                        if (_cachedModel!.currentUser![Dbkeys.locked] != null &&
                            _cachedModel!.currentUser![Dbkeys.locked]
                                .contains(user[Dbkeys.phone])) {
                          if (widget.prefs.getString(Dbkeys.isPINsetDone) !=
                                  currentUserNo ||
                              widget.prefs.getString(Dbkeys.isPINsetDone) ==
                                  null) {
                            ChatController.unlockChat(
                                currentUserNo, user[Dbkeys.phone] as String?);
                            Navigator.push(
                                context,
                                new MaterialPageRoute(
                                    builder: (context) => new ChatScreen(
                                        isSharingIntentForwarded: false,
                                        prefs: widget.prefs,
                                        unread: unread,
                                        model: _cachedModel!,
                                        currentUserNo: currentUserNo,
                                        peerNo:
                                            user[Dbkeys.phone] as String?)));
                          } else {
                            NavigatorState state = Navigator.of(context);
                            ChatController.authenticate(_cachedModel!,
                                getTranslated(context, 'auth_neededchat'),
                                state: state,
                                shouldPop: false,
                                type: Fiberchat.getAuthenticationType(
                                    biometricEnabled, _cachedModel),
                                prefs: widget.prefs, onSuccess: () {
                              state.pushReplacement(new MaterialPageRoute(
                                  builder: (context) => new ChatScreen(
                                      isSharingIntentForwarded: false,
                                      prefs: widget.prefs,
                                      unread: unread,
                                      model: _cachedModel!,
                                      currentUserNo: currentUserNo,
                                      peerNo: user[Dbkeys.phone] as String?)));
                            });
                          }
                        } else {
                          Navigator.push(
                              context,
                              new MaterialPageRoute(
                                  builder: (context) => new ChatScreen(
                                      isSharingIntentForwarded: false,
                                      prefs: widget.prefs,
                                      unread: unread,
                                      model: _cachedModel!,
                                      currentUserNo: currentUserNo,
                                      peerNo: user[Dbkeys.phone] as String?)));
                        }
                      },
                      trailing: unread != 0
                          ? Container(
                              child: Text(unread.toString(),
                                  style: TextStyle(
                                      fontSize: 14,
                                      color: Colors.white,
                                      fontWeight: FontWeight.bold)),
                              padding: const EdgeInsets.all(7.0),
                              decoration: new BoxDecoration(
                                shape: BoxShape.circle,
                                color: user[Dbkeys.lastSeen] == true
                                    ? Colors.green[400]
                                    : Colors.blue[400],
                              ),
                            )
                          : user[Dbkeys.lastSeen] == true
                              ? Container(
                                  child: Container(width: 0, height: 0),
                                  padding: const EdgeInsets.all(7.0),
                                  decoration: new BoxDecoration(
                                      shape: BoxShape.circle,
                                      color: Colors.green[400]),
                                )
                              : SizedBox(
                                  height: 0,
                                  width: 0,
                                )),
                  Divider(
                    height: 0,
                  ),
                ],
              ));
        },
      );
    }
  }

  Stream<MessageData> getUnread(Map<String, dynamic> user) {
    String chatId = Fiberchat.getChatId(currentUserNo, user[Dbkeys.phone]);
    var controller = StreamController<MessageData>.broadcast();
    unreadSubscriptions.add(FirebaseFirestore.instance
        .collection(DbPaths.collectionmessages)
        .doc(chatId)
        .snapshots()
        .listen((doc) {
      if (doc[currentUserNo!] != null && doc[currentUserNo!] is int) {
        unreadSubscriptions.add(FirebaseFirestore.instance
            .collection(DbPaths.collectionmessages)
            .doc(chatId)
            .collection(chatId)
            .snapshots()
            .listen((snapshot) {
          controller.add(
              MessageData(snapshot: snapshot, lastSeen: doc[currentUserNo!]));
        }));
      }
    }));
    controllers.add(controller);
    return controller.stream;
  }

  _isHidden(phoneNo) {
    Map<String, dynamic> _currentUser = _cachedModel!.currentUser!;
    return _currentUser[Dbkeys.hidden] != null &&
        _currentUser[Dbkeys.hidden].contains(phoneNo);
  }

  StreamController<String> _userQuery =
      new StreamController<String>.broadcast();

  List<Map<String, dynamic>> _streamDocSnap = [];

  _chats(Map<String?, Map<String, dynamic>?> _userData,
      Map<String, dynamic>? currentUser) {
    return Consumer<List<GroupModel>>(
        builder: (context, groupList, _child) => Consumer<List<BroadcastModel>>(
                builder: (context, broadcastList, _child) {
              _streamDocSnap = Map.from(_userData)
                  .values
                  .where((_user) => _user.keys.contains(Dbkeys.chatStatus))
                  .toList()
                  .cast<Map<String, dynamic>>();
              Map<String?, int?> _lastSpokenAt = _cachedModel!.lastSpokenAt;
              List<Map<String, dynamic>> filtered =
                  List.from(<Map<String, dynamic>>[]);
              groupList.forEach((element) {
                _streamDocSnap.add(element.docmap);
              });
              broadcastList.forEach((element) {
                _streamDocSnap.add(element.docmap);
              });
              _streamDocSnap.sort((a, b) {
                int aTimestamp = a.containsKey(Dbkeys.groupISTYPINGUSERID)
                    ? a[Dbkeys.groupLATESTMESSAGETIME]
                    : a.containsKey(Dbkeys.broadcastBLACKLISTED)
                        ? a[Dbkeys.broadcastLATESTMESSAGETIME]
                        : _lastSpokenAt[a[Dbkeys.phone]] ?? 0;
                int bTimestamp = b.containsKey(Dbkeys.groupISTYPINGUSERID)
                    ? b[Dbkeys.groupLATESTMESSAGETIME]
                    : b.containsKey(Dbkeys.broadcastBLACKLISTED)
                        ? b[Dbkeys.broadcastLATESTMESSAGETIME]
                        : _lastSpokenAt[b[Dbkeys.phone]] ?? 0;
                return bTimestamp - aTimestamp;
              });

              if (!showHidden) {
                _streamDocSnap.removeWhere((_user) =>
                    !_user.containsKey(Dbkeys.groupISTYPINGUSERID) &&
                    !_user.containsKey(Dbkeys.broadcastBLACKLISTED) &&
                    _isHidden(_user[Dbkeys.phone]));
              }

              return ListView(
                physics: const BouncingScrollPhysics(
                    parent: AlwaysScrollableScrollPhysics()),
                shrinkWrap: true,
                children: [
                  Container(
                      child: _streamDocSnap.isNotEmpty
                          ? StreamBuilder(
                              stream: _userQuery.stream.asBroadcastStream(),
                              builder: (context, snapshot) {
                                if (_filter.text.isNotEmpty ||
                                    snapshot.hasData) {
                                  filtered = this._streamDocSnap.where((user) {
                                    return user[Dbkeys.nickname]
                                        .toLowerCase()
                                        .trim()
                                        .contains(new RegExp(r'' +
                                            _filter.text.toLowerCase().trim() +
                                            ''));
                                  }).toList();
                                  if (filtered.isNotEmpty)
                                    return ListView.builder(
                                      physics: AlwaysScrollableScrollPhysics(),
                                      shrinkWrap: true,
                                      padding: EdgeInsets.all(10.0),
                                      itemBuilder: (context, index) =>
                                          buildItem(context,
                                              filtered.elementAt(index)),
                                      itemCount: filtered.length,
                                    );
                                  else
                                    return ListView(
                                        physics:
                                            AlwaysScrollableScrollPhysics(),
                                        shrinkWrap: true,
                                        children: [
                                          Padding(
                                              padding: EdgeInsets.only(
                                                  top: MediaQuery.of(context)
                                                          .size
                                                          .height /
                                                      3.5),
                                              child: Center(
                                                child: Text(
                                                    getTranslated(context,
                                                        'nosearchresult'),
                                                    textAlign: TextAlign.center,
                                                    style: TextStyle(
                                                      fontSize: 18,
                                                      color: fiberchatGrey,
                                                    )),
                                              ))
                                        ]);
                                }
                                return ListView.builder(
                                  physics: NeverScrollableScrollPhysics(),
                                  shrinkWrap: true,
                                  padding: EdgeInsets.fromLTRB(0, 10, 0, 120),
                                  itemBuilder: (context, index) {
                                    if (_streamDocSnap[index].containsKey(
                                        Dbkeys.groupISTYPINGUSERID)) {
                                      ///----- Build Group Chat Tile ----
                                      return Theme(
                                          data: ThemeData(
                                              splashColor: Colors.transparent,
                                              highlightColor:
                                                  Colors.transparent),
                                          child: Column(
                                            children: [
                                              ListTile(
                                                onLongPress: () {
                                                  showMenuForGroupChat(context,
                                                      _streamDocSnap[index]);
                                                },
                                                contentPadding:
                                                    EdgeInsets.fromLTRB(
                                                        20, 0, 20, 0),
                                                leading:
                                                    customCircleAvatarGroup(
                                                        url: _streamDocSnap[
                                                                index][
                                                            Dbkeys
                                                                .groupPHOTOURL],
                                                        radius: 22),
                                                title: Text(
                                                  _streamDocSnap[index]
                                                      [Dbkeys.groupNAME],
                                                  maxLines: 1,
                                                  overflow:
                                                      TextOverflow.ellipsis,
                                                  style: TextStyle(
                                                    color: fiberchatBlack,
                                                    fontSize: 16,
                                                  ),
                                                ),
                                                subtitle: Text(
                                                  '${_streamDocSnap[index][Dbkeys.groupMEMBERSLIST].length} ${getTranslated(context, 'participants')}',
                                                  style: TextStyle(
                                                    color: fiberchatGrey,
                                                    fontSize: 14,
                                                  ),
                                                ),
                                                onTap: () {
                                                  Navigator.push(
                                                      context,
                                                      new MaterialPageRoute(
                                                          builder: (context) => new GroupChatPage(
                                                              isCurrentUserMuted:
                                                                  _streamDocSnap[index].containsKey(Dbkeys.groupMUTEDMEMBERS)
                                                                      ? _streamDocSnap[index][Dbkeys.groupMUTEDMEMBERS]
                                                                          .contains(
                                                                              currentUserNo)
                                                                      : false,
                                                              isSharingIntentForwarded:
                                                                  false,
                                                              model:
                                                                  _cachedModel!,
                                                              prefs:
                                                                  widget.prefs,
                                                              joinedTime:
                                                                  _streamDocSnap[index][
                                                                      '${widget.currentUserNo}-joinedOn'],
                                                              currentUserno: widget
                                                                  .currentUserNo!,
                                                              groupID:
                                                                  _streamDocSnap[
                                                                          index]
                                                                      [Dbkeys.groupID])));
                                                },
                                                trailing: StreamBuilder(
                                                  stream: FirebaseFirestore
                                                      .instance
                                                      .collection(DbPaths
                                                          .collectiongroups)
                                                      .doc(_streamDocSnap[index]
                                                          [Dbkeys.groupID])
                                                      .collection(DbPaths
                                                          .collectiongroupChats)
                                                      .where(
                                                          Dbkeys.groupmsgTIME,
                                                          isGreaterThan:
                                                              _streamDocSnap[
                                                                      index][
                                                                  widget
                                                                      .currentUserNo])
                                                      .snapshots(),
                                                  builder:
                                                      (BuildContext context,
                                                          AsyncSnapshot<
                                                                  QuerySnapshot<
                                                                      dynamic>>
                                                              snapshot) {
                                                    if (snapshot
                                                            .connectionState ==
                                                        ConnectionState
                                                            .waiting) {
                                                      return SizedBox(
                                                        height: 0,
                                                        width: 0,
                                                      );
                                                    } else if (snapshot
                                                            .hasData &&
                                                        snapshot.data!.docs
                                                                .length >
                                                            0) {
                                                      return Container(
                                                        child: Text(
                                                            '${snapshot.data!.docs.length}',
                                                            style: TextStyle(
                                                                fontSize: 14,
                                                                color: Colors
                                                                    .white,
                                                                fontWeight:
                                                                    FontWeight
                                                                        .bold)),
                                                        padding:
                                                            const EdgeInsets
                                                                .all(7.0),
                                                        decoration:
                                                            new BoxDecoration(
                                                          shape:
                                                              BoxShape.circle,
                                                          color:
                                                              Colors.blue[400],
                                                        ),
                                                      );
                                                    }
                                                    return SizedBox(
                                                      height: 0,
                                                      width: 0,
                                                    );
                                                  },
                                                ),
                                              ),
                                              Divider(
                                                height: 0,
                                              ),
                                            ],
                                          ));
                                    } else if (_streamDocSnap[index]
                                        .containsKey(
                                            Dbkeys.broadcastBLACKLISTED)) {
                                      ///----- Build Broadcast Chat Tile ----
                                      return Theme(
                                          data: ThemeData(
                                              splashColor: Colors.transparent,
                                              highlightColor:
                                                  Colors.transparent),
                                          child: Column(
                                            children: [
                                              ListTile(
                                                onLongPress: () {
                                                  showMenuForBroadcastChat(
                                                      context,
                                                      _streamDocSnap[index]);
                                                },
                                                contentPadding:
                                                    EdgeInsets.fromLTRB(
                                                        20, 0, 20, 0),
                                                leading:
                                                    customCircleAvatarBroadcast(
                                                        url: _streamDocSnap[
                                                                index][
                                                            Dbkeys
                                                                .broadcastPHOTOURL],
                                                        radius: 22),
                                                title: Text(
                                                  _streamDocSnap[index]
                                                      [Dbkeys.broadcastNAME],
                                                  maxLines: 1,
                                                  overflow:
                                                      TextOverflow.ellipsis,
                                                  style: TextStyle(
                                                    color: fiberchatBlack,
                                                    fontSize: 16,
                                                  ),
                                                ),
                                                subtitle: Text(
                                                  '${_streamDocSnap[index][Dbkeys.broadcastMEMBERSLIST].length} ${getTranslated(context, 'recipients')}',
                                                  style: TextStyle(
                                                    color: fiberchatGrey,
                                                    fontSize: 14,
                                                  ),
                                                ),
                                                onTap: () {
                                                  Navigator.push(
                                                      context,
                                                      new MaterialPageRoute(
                                                          builder: (context) => new BroadcastChatPage(
                                                              model:
                                                                  _cachedModel!,
                                                              prefs:
                                                                  widget.prefs,
                                                              currentUserno: widget
                                                                  .currentUserNo!,
                                                              broadcastID:
                                                                  _streamDocSnap[
                                                                          index]
                                                                      [Dbkeys
                                                                          .broadcastID])));
                                                },
                                              ),
                                              Divider(height: 0),
                                            ],
                                          ));
                                    } else {
                                      return buildItem(context,
                                          _streamDocSnap.elementAt(index));
                                    }
                                  },
                                  itemCount: _streamDocSnap.length,
                                );
                              })
                          : ListView(
                              physics: NeverScrollableScrollPhysics(),
                              shrinkWrap: true,
                              padding: EdgeInsets.all(0),
                              children: [
                                  Padding(
                                      padding: EdgeInsets.only(
                                          top: MediaQuery.of(context)
                                                  .size
                                                  .height /
                                              3.5),
                                      child: Center(
                                        child: Padding(
                                            padding: EdgeInsets.all(30.0),
                                            child: Text(
                                                groupList.length != 0
                                                    ? ''
                                                    : getTranslated(
                                                        context, 'startchat'),
                                                textAlign: TextAlign.center,
                                                style: TextStyle(
                                                  fontSize: 16,
                                                  height: 1.59,
                                                  color: fiberchatGrey,
                                                ))),
                                      ))
                                ])),
                ],
              );
            }));
  }

  Widget buildGroupitem() {
    return Text(
      Dbkeys.groupNAME,
      maxLines: 1,
      overflow: TextOverflow.ellipsis,
    );
  }

  DataModel? getModel() {
    _cachedModel ??= DataModel(currentUserNo);
    return _cachedModel;
  }

  @override
  void dispose() {
    super.dispose();

    if (IsBannerAdShow == true) {
      myBanner.dispose();
    }
  }

  @override
  Widget build(BuildContext context) {
    final observer = Provider.of<Observer>(this.context, listen: false);
    setStatusBarColor();
    return Fiberchat.getNTPWrappedWidget(ScopedModel<DataModel>(
      model: getModel()!,
      child:
          ScopedModelDescendant<DataModel>(builder: (context, child, _model) {
        _cachedModel = _model;
        return Scaffold(
          // bottomSheet: IsBannerAdShow == true &&
          //         observer.isadmobshow == true &&
          //         adWidget != null
          //     ? Container(
          //         height: 60,
          //         margin: EdgeInsets.only(
          //             bottom: Platform.isIOS == true ? 25.0 : 5, top: 0),
          //         child: Center(child: adWidget),
          //       )
          //     : SizedBox(
          //         height: 0,
          //       ),
          backgroundColor: fiberchatWhite,
          floatingActionButton: Padding(
            padding: EdgeInsets.only(bottom: 0
                // bottom: IsBannerAdShow == true && observer.isadmobshow == true
                //     ? 60
                //     : 0
                ),
            child: FloatingActionButton(
                heroTag: "sdfweewfwq123sdasfw",
                backgroundColor: fiberchatLightGreen,
                child: Icon(
                  Icons.chat,
                  size: 30.0,
                ),
                onPressed: () {
                  Navigator.push(
                      context,
                      new MaterialPageRoute(
                          builder: (context) => new SmartContactsPage(
                              onTapCreateBroadcast: () {
                                if (observer.isAllowCreatingBroadcasts ==
                                    false) {
                                  Fiberchat.showRationale(
                                      getTranslated(this.context, 'disabled'));
                                } else {
                                  Navigator.pushReplacement(
                                      context,
                                      MaterialPageRoute(
                                          builder: (context) =>
                                              AddContactsToBroadcast(
                                                currentUserNo:
                                                    widget.currentUserNo,
                                                model: _cachedModel,
                                                biometricEnabled: false,
                                                prefs: widget.prefs,
                                                isAddingWhileCreatingBroadcast:
                                                    true,
                                              )));
                                }
                              },
                              onTapCreateGroup: () {
                                if (observer.isAllowCreatingGroups == false) {
                                  Fiberchat.showRationale(
                                      getTranslated(this.context, 'disabled'));
                                } else {
                                  Navigator.pushReplacement(
                                      context,
                                      MaterialPageRoute(
                                          builder: (context) =>
                                              AddContactsToGroup(
                                                currentUserNo:
                                                    widget.currentUserNo,
                                                model: _cachedModel,
                                                biometricEnabled: false,
                                                prefs: widget.prefs,
                                                isAddingWhileCreatingGroup:
                                                    true,
                                              )));
                                }
                              },
                              prefs: widget.prefs,
                              biometricEnabled: biometricEnabled,
                              currentUserNo: currentUserNo!,
                              model: _cachedModel!)));
                }),
          ),
          body: RefreshIndicator(
            onRefresh: () {
              isAuthenticating = !isAuthenticating;
              setState(() {
                showHidden = !showHidden;
              });
              return Future.value(true);
            },
            child: _chats(_model.userData, _model.currentUser),
          ),
        );
      }),
    ));
  }
}
