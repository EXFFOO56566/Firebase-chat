//*************   © Copyrighted by Thinkcreative_Technologies. An Exclusive item of Envato market. Make sure you have purchased a Regular License OR Extended license for the Source Code from Envato to use this product. See the License Defination attached with source code. *********************

import 'dart:async';
import 'dart:core';
import 'dart:io';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:fiberchat/Configs/Dbkeys.dart';
import 'package:fiberchat/Configs/Dbpaths.dart';
import 'package:fiberchat/Configs/app_constants.dart';
import 'package:fiberchat/Screens/Broadcast/AddContactsToBroadcast.dart';
import 'package:fiberchat/Screens/Groups/AddContactsToGroup.dart';
import 'package:fiberchat/Screens/chat_screen/utils/aes_encryption.dart';
import 'package:fiberchat/Screens/contact_screens/SmartContactsPage.dart';
import 'package:fiberchat/Screens/recent_chats/widgets/getBroadcastMessageTile.dart';
import 'package:fiberchat/Screens/recent_chats/widgets/getGroupMessageTile.dart';
import 'package:fiberchat/Screens/recent_chats/widgets/getPersonalMessageTile.dart';
import 'package:fiberchat/Services/Admob/admob.dart';
import 'package:fiberchat/Services/Providers/BroadcastProvider.dart';
import 'package:fiberchat/Services/Providers/GroupChatProvider.dart';
import 'package:fiberchat/Services/Providers/Observer.dart';
import 'package:fiberchat/Services/localization/language_constants.dart';
import 'package:fiberchat/Models/DataModel.dart';
import 'package:fiberchat/Services/Providers/user_provider.dart';
import 'package:fiberchat/Utils/crc.dart';
import 'package:fiberchat/Utils/setStatusBarColor.dart';
import 'package:fiberchat/Utils/utils.dart';
import 'package:fiberchat/Utils/late_load.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:scoped_model/scoped_model.dart';
import 'package:fiberchat/Models/E2EE/e2ee.dart' as e2ee;
import 'package:encrypt/encrypt.dart' as encrypt;

Color darkGrey = Colors.blueGrey[700]!;
Color lightGrey = Colors.blueGrey[400]!;

class RecentChats extends StatefulWidget {
  RecentChats(
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
      new RecentChatsState(currentUserNo: this.currentUserNo);
}

class RecentChatsState extends State<RecentChats> {
  RecentChatsState({Key? key, this.currentUserNo}) {
    _filter.addListener(() {
      _userQuery.add(_filter.text.isEmpty ? '' : _filter.text);
    });
  }

  final TextEditingController _filter = new TextEditingController();
  bool isAuthenticating = false;

  // List<StreamSubscription> unreadSubscriptions = [];

  List<StreamController> controllers = [];
  final BannerAd myBanner = BannerAd(
    adUnitId: getBannerAdUnitId()!,
    size: AdSize.banner,
    request: AdRequest(),
    listener: BannerAdListener(),
  );
  AdWidget? adWidget;

  FlutterSecureStorage storage = new FlutterSecureStorage();
  late encrypt.Encrypter cryptor;
  final iv = encrypt.IV.fromLength(8);
  String? privateKey, sharedSecret;
  Future<String?> readPersonalMessage(
      peer, String inputMssg, bool isAESencryption) async {
    try {
      privateKey = await storage.read(key: Dbkeys.privateKey);
      sharedSecret = (await e2ee.X25519().calculateSharedSecret(
              e2ee.Key.fromBase64(privateKey!, false),
              e2ee.Key.fromBase64(peer![Dbkeys.publicKey], true)))
          .toBase64();
      final key = encrypt.Key.fromBase64(sharedSecret!);
      cryptor = new encrypt.Encrypter(encrypt.Salsa20(key));
      return isAESencryption == true
          ? AESEncryptData.decryptAES(inputMssg, sharedSecret)
          : decryptWithCRC(inputMssg);
    } catch (e) {
      sharedSecret = null;
      return "";
    }
  }

  String decryptWithCRC(String input) {
    try {
      if (input.contains(Dbkeys.crcSeperator)) {
        int idx = input.lastIndexOf(Dbkeys.crcSeperator);
        String msgPart = input.substring(0, idx);
        String crcPart = input.substring(idx + 1);
        int? crc = int.tryParse(crcPart);
        if (crc != null) {
          msgPart =
              cryptor.decrypt(encrypt.Encrypted.fromBase64(msgPart), iv: iv);
          if (CRC32.compute(msgPart) == crc) return msgPart;
        }
      }
    } on FormatException {
      return '';
    }
    // Fiberchat.toast(getTranslated(this.context, 'msgnotload'));
    return '';
  }

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

  // void cancelUnreadSubscriptions() {
  //   unreadSubscriptions.forEach((subscription) {
  //     subscription.cancel();
  //   });
  // }

  DataModel? _cachedModel;
  bool showHidden = false, biometricEnabled = false;

  String? currentUserNo;

  bool isLoading = false;

  _isHidden(phoneNo) {
    Map<String, dynamic> _currentUser = _cachedModel!.currentUser!;
    return _currentUser[Dbkeys.hidden] != null &&
        _currentUser[Dbkeys.hidden].contains(phoneNo);
  }

  StreamController<String> _userQuery =
      new StreamController<String>.broadcast();

  List<Map<String, dynamic>> _streamDocSnap = [];

  buildPersonalMessage(
    Map<String, dynamic> realTimePeerData,
  ) {
    String chatId =
        Fiberchat.getChatId(currentUserNo, realTimePeerData[Dbkeys.phone]);
    return streamLoad(
        stream: FirebaseFirestore.instance
            .collection(DbPaths.collectionmessages)
            .doc(chatId)
            .snapshots(),
        placeholder: SizedBox(),
        onfetchdone: (chatDoc) {
          return streamLoadCollections(
              stream: FirebaseFirestore.instance
                  .collection(DbPaths.collectionmessages)
                  .doc(chatId)
                  .collection(chatId)
                  .where(Dbkeys.timestamp,
                      isGreaterThan: chatDoc[currentUserNo])
                  .snapshots(),
              placeholder: getPersonalMessageTile(
                  peerSeenStatus: chatDoc[realTimePeerData[Dbkeys.phone]],
                  unRead: 0,
                  peer: realTimePeerData,
                  context: this.context,
                  cachedModel: _cachedModel!,
                  currentUserNo: currentUserNo!,
                  lastMessage: null,
                  prefs: widget.prefs,
                  readFunction: null,
                  isPeerChatMuted:
                      chatDoc.containsKey("${widget.currentUserNo}-muted")
                          ? chatDoc["${widget.currentUserNo}-muted"]
                          : false),
              noDataWidget: streamLoadCollections(
                  stream: FirebaseFirestore.instance
                      .collection(DbPaths.collectionmessages)
                      .doc(chatId)
                      .collection(chatId)
                      .orderBy(Dbkeys.timestamp, descending: true)
                      .limit(1)
                      .snapshots(),
                  placeholder: getPersonalMessageTile(
                      peerSeenStatus: chatDoc[realTimePeerData[Dbkeys.phone]],
                      unRead: 0,
                      peer: realTimePeerData,
                      context: this.context,
                      cachedModel: _cachedModel!,
                      currentUserNo: currentUserNo!,
                      lastMessage: null,
                      prefs: widget.prefs,
                      readFunction: null,
                      isPeerChatMuted:
                          chatDoc.containsKey("${widget.currentUserNo}-muted")
                              ? chatDoc["${widget.currentUserNo}-muted"]
                              : false),
                  noDataWidget: SizedBox(),
                  onfetchdone: (messages) {
                    return getPersonalMessageTile(
                        peerSeenStatus: chatDoc[realTimePeerData[Dbkeys.phone]],
                        unRead: 0,
                        peer: realTimePeerData,
                        context: this.context,
                        cachedModel: _cachedModel!,
                        currentUserNo: currentUserNo!,
                        lastMessage: messages.last,
                        prefs: widget.prefs,
                        readFunction: readPersonalMessage(
                            realTimePeerData,
                            messages.last[Dbkeys.content],
                            messages.last
                                .data()
                                .containsKey(Dbkeys.latestEncrypted)),
                        isPeerChatMuted:
                            chatDoc.containsKey("${widget.currentUserNo}-muted")
                                ? chatDoc["${widget.currentUserNo}-muted"]
                                : false);
                  }),
              onfetchdone: (messages) {
                return getPersonalMessageTile(
                    peerSeenStatus: chatDoc[realTimePeerData[Dbkeys.phone]],
                    unRead: messages.length,
                    peer: realTimePeerData,
                    context: this.context,
                    cachedModel: _cachedModel!,
                    currentUserNo: currentUserNo!,
                    lastMessage: messages.last,
                    prefs: widget.prefs,
                    readFunction: readPersonalMessage(
                        realTimePeerData,
                        messages.last[Dbkeys.content],
                        messages.last
                            .data()
                            .containsKey(Dbkeys.latestEncrypted)),
                    isPeerChatMuted:
                        chatDoc.containsKey("${widget.currentUserNo}-muted")
                            ? chatDoc["${widget.currentUserNo}-muted"]
                            : false);
              });
        });
  }

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
                                    return Text('filtered');
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
                                      return streamLoadCollections(
                                        stream: FirebaseFirestore.instance
                                            .collection(
                                                DbPaths.collectiongroups)
                                            .doc(_streamDocSnap[index]
                                                [Dbkeys.groupID])
                                            .collection(
                                                DbPaths.collectiongroupChats)
                                            .where(Dbkeys.groupmsgTIME,
                                                isGreaterThan:
                                                    _streamDocSnap[index]
                                                        [currentUserNo])
                                            .snapshots(),
                                        placeholder: groupMessageTile(
                                            context: context,
                                            streamDocSnap: _streamDocSnap,
                                            index: index,
                                            currentUserNo:
                                                widget.currentUserNo!,
                                            prefs: widget.prefs,
                                            cachedModel: _cachedModel!,
                                            unRead: 0,
                                            isGroupChatMuted: _streamDocSnap[
                                                        index]
                                                    .containsKey(Dbkeys
                                                        .groupMUTEDMEMBERS)
                                                ? _streamDocSnap[index][Dbkeys
                                                        .groupMUTEDMEMBERS]
                                                    .contains(currentUserNo)
                                                : false),
                                        noDataWidget: groupMessageTile(
                                            context: context,
                                            streamDocSnap: _streamDocSnap,
                                            index: index,
                                            currentUserNo:
                                                widget.currentUserNo!,
                                            prefs: widget.prefs,
                                            cachedModel: _cachedModel!,
                                            unRead: 0,
                                            isGroupChatMuted: _streamDocSnap[
                                                        index]
                                                    .containsKey(Dbkeys
                                                        .groupMUTEDMEMBERS)
                                                ? _streamDocSnap[index][Dbkeys
                                                        .groupMUTEDMEMBERS]
                                                    .contains(currentUserNo)
                                                : false),
                                        onfetchdone: (docs) {
                                          return groupMessageTile(
                                              context: context,
                                              streamDocSnap: _streamDocSnap,
                                              index: index,
                                              currentUserNo:
                                                  widget.currentUserNo!,
                                              prefs: widget.prefs,
                                              cachedModel: _cachedModel!,
                                              unRead: docs
                                                  .where((mssg) =>
                                                      mssg[Dbkeys
                                                          .groupmsgSENDBY] !=
                                                      currentUserNo)
                                                  .toList()
                                                  .length,
                                              isGroupChatMuted: _streamDocSnap[
                                                          index]
                                                      .containsKey(Dbkeys
                                                          .groupMUTEDMEMBERS)
                                                  ? _streamDocSnap[index][Dbkeys
                                                          .groupMUTEDMEMBERS]
                                                      .contains(currentUserNo)
                                                  : false);
                                        },
                                      );
                                    } else if (_streamDocSnap[index]
                                        .containsKey(
                                            Dbkeys.broadcastBLACKLISTED)) {
                                      ///----- Build Broadcast Chat Tile ----
                                      return broadcastMessageTile(
                                        context: context,
                                        streamDocSnap: _streamDocSnap,
                                        index: index,
                                        currentUserNo: widget.currentUserNo!,
                                        prefs: widget.prefs,
                                        cachedModel: _cachedModel!,
                                      );
                                    } else {
                                      return buildPersonalMessage(
                                          _streamDocSnap.elementAt(index));
                                      // return Consumer<
                                      //         AvailableContactsProvider>(
                                      //     builder: (context, availableContacts,
                                      //         _child) {
                                      //   // _filtered = availableContacts.filtered;
                                      //   return FutureBuilder(
                                      //       future:
                                      //           availableContacts.getUserDoc(
                                      //               _streamDocSnap.elementAt(
                                      //                   index)[Dbkeys.phone]),
                                      //       builder: (BuildContext context,
                                      //           AsyncSnapshot snapshot3) {
                                      //         if (snapshot3.hasData) {
                                      //           return buildPersonalMessage(
                                      //               _streamDocSnap
                                      //                   .elementAt(index));
                                      //         }
                                      //         return buildPersonalMessage(
                                      //             _streamDocSnap
                                      //                 .elementAt(index));
                                      //       });
                                      // });
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
          bottomSheet: IsBannerAdShow == true &&
                  observer.isadmobshow == true &&
                  adWidget != null
              ? Container(
                  height: 60,
                  margin: EdgeInsets.only(
                      bottom: Platform.isIOS == true ? 25.0 : 5, top: 0),
                  child: Center(child: adWidget),
                )
              : SizedBox(
                  height: 0,
                ),
          backgroundColor: fiberchatWhite,
          floatingActionButton: Padding(
            padding: EdgeInsets.only(
                bottom: IsBannerAdShow == true && observer.isadmobshow == true
                    ? 60
                    : 0),
            child: FloatingActionButton(
                heroTag: "dfsf4e8t4yaddweqewt834",
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
