//*************   © Copyrighted by Thinkcreative_Technologies. An Exclusive item of Envato market. Make sure you have purchased a Regular License OR Extended license for the Source Code from Envato to use this product. See the License Defination attached with source code. *********************

import 'dart:async';
import 'dart:core';
import 'dart:io';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:device_info/device_info.dart';
import 'package:fiberchat/Configs/Dbkeys.dart';
import 'package:fiberchat/Configs/Dbpaths.dart';
import 'package:fiberchat/Configs/optional_constants.dart';
import 'package:fiberchat/Screens/Broadcast/AddContactsToBroadcast.dart';
import 'package:fiberchat/Screens/Groups/AddContactsToGroup.dart';
import 'package:fiberchat/Screens/SettingsOption/settingsOption.dart';
import 'package:fiberchat/Screens/homepage/Setupdata.dart';
import 'package:fiberchat/Screens/notifications/AllNotifications.dart';
import 'package:fiberchat/Screens/recent_chats/RecentChatsWithoutLastMessage.dart';
import 'package:fiberchat/Screens/sharing_intent/SelectContactToShare.dart';
import 'package:fiberchat/Screens/splash_screen/splash_screen.dart';
import 'package:fiberchat/Screens/status/status.dart';
import 'package:fiberchat/Services/Providers/AvailableContactsProvider.dart';
import 'package:fiberchat/Services/Providers/Observer.dart';
import 'package:fiberchat/Services/Providers/StatusProvider.dart';
import 'package:fiberchat/Services/Providers/call_history_provider.dart';
import 'package:fiberchat/Services/localization/language.dart';
import 'package:fiberchat/Utils/custom_url_launcher.dart';
import 'package:fiberchat/Utils/error_codes.dart';
import 'package:fiberchat/Utils/phonenumberVariantsGenerator.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart'
    as local;
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:fiberchat/Configs/app_constants.dart';
import 'package:fiberchat/Screens/auth_screens/login.dart';
import 'package:fiberchat/Services/Providers/currentchat_peer.dart';
import 'package:fiberchat/Services/localization/language_constants.dart';
import 'package:fiberchat/Screens/profile_settings/profileSettings.dart';
import 'package:fiberchat/main.dart';
import 'package:fiberchat/Screens/recent_chats/RecentsChats.dart';
import 'package:fiberchat/Screens/call_history/callhistory.dart';
import 'package:fiberchat/Models/DataModel.dart';
import 'package:fiberchat/Services/Providers/user_provider.dart';
import 'package:fiberchat/Screens/calling_screen/pickup_layout.dart';
import 'package:fiberchat/Utils/utils.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:overlay_support/overlay_support.dart';
import 'package:package_info/package_info.dart';
import 'package:provider/provider.dart';
import 'package:receive_sharing_intent/receive_sharing_intent.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:local_auth/local_auth.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:fiberchat/Configs/Enum.dart';
import 'package:fiberchat/Utils/unawaited.dart';

class Homepage extends StatefulWidget {
  Homepage(
      {required this.currentUserNo,
      required this.prefs,
      required this.doc,
      this.isShowOnlyCircularSpin = false,
      key})
      : super(key: key);
  final String? currentUserNo;
  final DocumentSnapshot<Map<String, dynamic>> doc;
  final bool? isShowOnlyCircularSpin;
  final SharedPreferences prefs;
  @override
  State createState() => new HomepageState(doc: this.doc);
}

class HomepageState extends State<Homepage>
    with
        WidgetsBindingObserver,
        AutomaticKeepAliveClientMixin,
        TickerProviderStateMixin {
  HomepageState({Key? key, doc}) {
    _filter.addListener(() {
      _userQuery.add(_filter.text.isEmpty ? '' : _filter.text);
    });
  }
  TabController? controllerIfcallallowed;
  TabController? controllerIfcallNotallowed;
  late StreamSubscription _intentDataStreamSubscription;
  List<SharedMediaFile>? _sharedFiles = [];
  String? _sharedText;
  @override
  bool get wantKeepAlive => true;

  bool isFetching = true;
  List phoneNumberVariants = [];
  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed)
      setIsActive();
    else
      setLastSeen();
  }

  void setIsActive() async {
    if (widget.currentUserNo != null)
      await FirebaseFirestore.instance
          .collection(DbPaths.collectionusers)
          .doc(widget.currentUserNo)
          .update(
        {
          Dbkeys.lastSeen: true,
          Dbkeys.lastOnline: DateTime.now().millisecondsSinceEpoch
        },
      );
  }

  void setLastSeen() async {
    if (widget.currentUserNo != null)
      await FirebaseFirestore.instance
          .collection(DbPaths.collectionusers)
          .doc(widget.currentUserNo)
          .update(
        {Dbkeys.lastSeen: DateTime.now().millisecondsSinceEpoch},
      );
  }

  final TextEditingController _filter = new TextEditingController();
  bool isAuthenticating = false;

  StreamSubscription? spokenSubscription;
  List<StreamSubscription> unreadSubscriptions =
      List.from(<StreamSubscription>[]);

  List<StreamController> controllers = List.from(<StreamController>[]);
  DeviceInfoPlugin deviceInfo = DeviceInfoPlugin();

  String? deviceid;
  var mapDeviceInfo = {};
  String? maintainanceMessage;
  bool isNotAllowEmulator = false;
  bool? isblockNewlogins = false;
  bool? isApprovalNeededbyAdminForNewUser = false;
  String? accountApprovalMessage = 'Account Approved';
  String? accountstatus;
  String? accountactionmessage;
  String? userPhotourl;
  String? userFullname;

  @override
  void initState() {
    listenToSharingintent();
    listenToNotification();
    super.initState();
    getSignedInUserOrRedirect();
    setdeviceinfo();
    registerNotification();

    controllerIfcallallowed = TabController(length: 3, vsync: this);
    controllerIfcallallowed!.index = 0;
    controllerIfcallNotallowed = TabController(length: 2, vsync: this);
    controllerIfcallNotallowed!.index = 0;

    Fiberchat.internetLookUp();
    WidgetsBinding.instance.addObserver(this);

    LocalAuthentication().canCheckBiometrics.then((res) {
      if (res) biometricEnabled = true;
    });
    getModel();
    WidgetsBinding.instance.addPostFrameCallback((timeStamp) {
      controllerIfcallallowed!.addListener(() {
        if (controllerIfcallallowed!.index == 2) {
          final statusProvider =
              Provider.of<StatusProvider>(context, listen: false);
          final contactsProvider =
              Provider.of<AvailableContactsProvider>(context, listen: false);
          statusProvider.searchContactStatus(widget.currentUserNo!,
              contactsProvider.alreadyJoinedUsersPhoneNameAsInServer);
        }
      });
      controllerIfcallNotallowed!.addListener(() {
        if (controllerIfcallNotallowed!.index == 2) {
          final statusProvider =
              Provider.of<StatusProvider>(context, listen: false);
          final contactsProvider =
              Provider.of<AvailableContactsProvider>(context, listen: false);
          statusProvider.searchContactStatus(widget.currentUserNo!,
              contactsProvider.alreadyJoinedUsersPhoneNameAsInServer);
        }
      });
    });
  }

  // detectLocale() async {
  //   await Devicelocale.currentLocale.then((locale) async {
  //     if (locale == 'ja_JP' &&
  //         (widget.prefs.getBool('islanguageselected') == false ||
  //             widget.prefs.getBool('islanguageselected') == null)) {
  //       Locale _locale = await setLocale('ja');
  //       FiberchatWrapper.setLocale(context, _locale);
  //       setState(() {});
  //     }
  //   }).catchError((onError) {
  //     Fiberchat.toast(
  //       'Error occured while fetching Locale :$onError',
  //     );
  //   });
  // }

  incrementSessionCount(String myphone) async {
    final StatusProvider statusProvider =
        Provider.of<StatusProvider>(context, listen: false);
    final AvailableContactsProvider contactsProvider =
        Provider.of<AvailableContactsProvider>(context, listen: false);
    final FirestoreDataProviderCALLHISTORY firestoreDataProviderCALLHISTORY =
        Provider.of<FirestoreDataProviderCALLHISTORY>(context, listen: false);
    await FirebaseFirestore.instance
        .collection(DbPaths.collectiondashboard)
        .doc(DbPaths.docuserscount)
        .set(
            Platform.isAndroid
                ? {
                    Dbkeys.totalvisitsANDROID: FieldValue.increment(1),
                  }
                : {
                    Dbkeys.totalvisitsIOS: FieldValue.increment(1),
                  },
            SetOptions(merge: true));
    await FirebaseFirestore.instance
        .collection(DbPaths.collectionusers)
        .doc(widget.currentUserNo)
        .set(
            Platform.isAndroid
                ? {
                    Dbkeys.isNotificationStringsMulitilanguageEnabled: true,
                    Dbkeys.notificationStringsMap:
                        getTranslateNotificationStringsMap(this.context),
                    Dbkeys.totalvisitsANDROID: FieldValue.increment(1),
                  }
                : {
                    Dbkeys.isNotificationStringsMulitilanguageEnabled: true,
                    Dbkeys.notificationStringsMap:
                        getTranslateNotificationStringsMap(this.context),
                    Dbkeys.totalvisitsIOS: FieldValue.increment(1),
                  },
            SetOptions(merge: true));
    firestoreDataProviderCALLHISTORY.fetchNextData(
        'CALLHISTORY',
        FirebaseFirestore.instance
            .collection(DbPaths.collectionusers)
            .doc(widget.currentUserNo)
            .collection(DbPaths.collectioncallhistory)
            .orderBy('TIME', descending: true)
            .limit(10),
        true);
    if (OnlyPeerWhoAreSavedInmyContactCanMessageOrCallMe == false) {
      await contactsProvider.fetchContacts(
          context, _cachedModel, myphone, widget.prefs,
          currentuserphoneNumberVariants: phoneNumberVariants);
    }

    //  await statusProvider.searchContactStatus(
    //       myphone, contactsProvider.joinedUserPhoneStringAsInServer);
    statusProvider.triggerDeleteMyExpiredStatus(myphone);
    statusProvider.triggerDeleteOtherUsersExpiredStatus(myphone);
    if (_sharedFiles!.length > 0 || _sharedText != null) {
      triggerSharing();
    }
  }

  triggerSharing() {
    final observer = Provider.of<Observer>(this.context, listen: false);
    if (_sharedText != null) {
      Navigator.push(
          context,
          new MaterialPageRoute(
              builder: (context) => new SelectContactToShare(
                  prefs: widget.prefs,
                  model: _cachedModel!,
                  currentUserNo: widget.currentUserNo,
                  sharedFiles: _sharedFiles!,
                  sharedText: _sharedText)));
    } else if (_sharedFiles != null) {
      if (_sharedFiles!.length > observer.maxNoOfFilesInMultiSharing) {
        Fiberchat.toast(getTranslated(context, 'maxnooffiles') +
            ' ' +
            '${observer.maxNoOfFilesInMultiSharing}');
      } else {
        Navigator.push(
            context,
            new MaterialPageRoute(
                builder: (context) => new SelectContactToShare(
                    prefs: widget.prefs,
                    model: _cachedModel!,
                    currentUserNo: widget.currentUserNo,
                    sharedFiles: _sharedFiles!,
                    sharedText: _sharedText)));
      }
    }
  }

  listenToSharingintent() {
    // For sharing images coming from outside the app while the app is in the memory
    _intentDataStreamSubscription = ReceiveSharingIntent.getMediaStream()
        .listen((List<SharedMediaFile> value) {
      setState(() {
        _sharedFiles = value;
      });
    }, onError: (err) {
      print("getIntentDataStream error: $err");
    });

    // For sharing images coming from outside the app while the app is closed
    ReceiveSharingIntent.getInitialMedia().then((List<SharedMediaFile> value) {
      setState(() {
        _sharedFiles = value;
      });
    });

    // For sharing or opening urls/text coming from outside the app while the app is in the memory
    _intentDataStreamSubscription =
        ReceiveSharingIntent.getTextStream().listen((String value) {
      setState(() {
        _sharedText = value;
      });
    }, onError: (err) {
      print("getLinkStream error: $err");
    });

    // For sharing or opening urls/text coming from outside the app while the app is closed
    ReceiveSharingIntent.getInitialText().then((String? value) {
      setState(() {
        _sharedText = value;
      });
    });
  }

  unsubscribeToNotification(String? userphone) async {
    if (userphone != null) {
      await FirebaseMessaging.instance.unsubscribeFromTopic(
          '${userphone.replaceFirst(new RegExp(r'\+'), '')}');
    }

    await FirebaseMessaging.instance
        .unsubscribeFromTopic(Dbkeys.topicUSERS)
        .catchError((err) {
      print(err.toString());
    });
    await FirebaseMessaging.instance
        .unsubscribeFromTopic(Platform.isAndroid
            ? Dbkeys.topicUSERSandroid
            : Platform.isIOS
                ? Dbkeys.topicUSERSios
                : Dbkeys.topicUSERSweb)
        .catchError((err) {
      print(err.toString());
    });
  }

  void registerNotification() async {
    await FirebaseMessaging.instance.requestPermission(
      alert: true,
      badge: true,
      provisional: false,
      sound: true,
    );
  }

  setdeviceinfo() async {
    if (Platform.isAndroid == true) {
      AndroidDeviceInfo androidInfo = await deviceInfo.androidInfo;
      setState(() {
        deviceid = androidInfo.id + androidInfo.androidId;
        mapDeviceInfo = {
          Dbkeys.deviceInfoMODEL: androidInfo.model,
          Dbkeys.deviceInfoOS: 'android',
          Dbkeys.deviceInfoISPHYSICAL: androidInfo.isPhysicalDevice,
          Dbkeys.deviceInfoDEVICEID: androidInfo.id,
          Dbkeys.deviceInfoOSID: androidInfo.androidId,
          Dbkeys.deviceInfoOSVERSION: androidInfo.version.baseOS,
          Dbkeys.deviceInfoMANUFACTURER: androidInfo.manufacturer,
          Dbkeys.deviceInfoLOGINTIMESTAMP: DateTime.now(),
        };
      });
    } else if (Platform.isIOS == true) {
      IosDeviceInfo iosInfo = await deviceInfo.iosInfo;
      setState(() {
        deviceid = iosInfo.systemName + iosInfo.model + iosInfo.systemVersion;
        mapDeviceInfo = {
          Dbkeys.deviceInfoMODEL: iosInfo.model,
          Dbkeys.deviceInfoOS: 'ios',
          Dbkeys.deviceInfoISPHYSICAL: iosInfo.isPhysicalDevice,
          Dbkeys.deviceInfoDEVICEID: iosInfo.identifierForVendor,
          Dbkeys.deviceInfoOSID: iosInfo.name,
          Dbkeys.deviceInfoOSVERSION: iosInfo.name,
          Dbkeys.deviceInfoMANUFACTURER: iosInfo.name,
          Dbkeys.deviceInfoLOGINTIMESTAMP: DateTime.now(),
        };
      });
    }
  }

  getuid(BuildContext context) {
    final UserProvider userProvider =
        Provider.of<UserProvider>(context, listen: false);
    userProvider.getUserDetails(widget.currentUserNo);
  }

  logout(BuildContext context) async {
    final FirebaseAuth firebaseAuth = FirebaseAuth.instance;
    await firebaseAuth.signOut();

    await widget.prefs.clear();

    FlutterSecureStorage storage = new FlutterSecureStorage();
    // ignore: await_only_futures
    await storage.delete;
    if (widget.currentUserNo != null) {
      await FirebaseFirestore.instance
          .collection(DbPaths.collectionusers)
          .doc(widget.currentUserNo)
          .update({
        Dbkeys.notificationTokens: [],
      });
    }

    await widget.prefs.setBool(Dbkeys.isTokenGenerated, false);
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(
        builder: (BuildContext context) => FiberchatWrapper(),
      ),
      (Route route) => false,
    );
  }

  @override
  void dispose() {
    super.dispose();
    WidgetsBinding.instance.removeObserver(this);
    controllers.forEach((controller) {
      controller.close();
    });
    _filter.dispose();
    spokenSubscription?.cancel();
    _userQuery.close();
    cancelUnreadSubscriptions();
    setLastSeen();

    _intentDataStreamSubscription.cancel();
  }

  void cancelUnreadSubscriptions() {
    unreadSubscriptions.forEach((subscription) {
      subscription.cancel();
    });
  }

  void listenToNotification() async {
    //FOR ANDROID  background notification is handled here whereas for iOS it is handled at the very top of main.dart ------
    if (Platform.isAndroid) {
      FirebaseMessaging.onBackgroundMessage(myBackgroundMessageHandlerAndroid);
    }
    //ANDROID & iOS  OnMessage callback
    FirebaseMessaging.onMessage.listen((RemoteMessage message) async {
      // ignore: unnecessary_null_comparison
      flutterLocalNotificationsPlugin..cancelAll();

      if (message.data['title'] != 'Call Ended' &&
          message.data['title'] != 'Missed Call' &&
          message.data['title'] != 'You have new message(s)' &&
          message.data['title'] != 'Incoming Video Call...' &&
          message.data['title'] != 'Incoming Audio Call...' &&
          message.data['title'] != 'Incoming Call ended' &&
          message.data['title'] != 'New message in Group') {
        Fiberchat.toast(getTranslated(this.context, 'newnotifications'));
      } else {
        if (message.data['title'] == 'New message in Group') {
          // var currentpeer =
          //     Provider.of<CurrentChatPeer>(this.context, listen: false);
          // if (currentpeer.groupChatId != message.data['groupid']) {
          //   flutterLocalNotificationsPlugin..cancelAll();

          //   showOverlayNotification((context) {
          //     return Card(
          //       margin: const EdgeInsets.symmetric(horizontal: 4),
          //       child: SafeArea(
          //         child: ListTile(
          //           title: Text(
          //             message.data['titleMultilang'],
          //             maxLines: 1,
          //             overflow: TextOverflow.ellipsis,
          //           ),
          //           subtitle: Text(
          //             message.data['bodyMultilang'],
          //             maxLines: 2,
          //             overflow: TextOverflow.ellipsis,
          //           ),
          //           trailing: IconButton(
          //               icon: Icon(Icons.close),
          //               onPressed: () {
          //                 OverlaySupportEntry.of(context)!.dismiss();
          //               }),
          //         ),
          //       ),
          //     );
          //   }, duration: Duration(milliseconds: 2000));
          // }
        } else if (message.data['title'] == 'Call Ended') {
          flutterLocalNotificationsPlugin..cancelAll();
        } else {
          if (message.data['title'] == 'Incoming Audio Call...' ||
              message.data['title'] == 'Incoming Video Call...') {
            final data = message.data;
            final title = data['title'];
            final body = data['body'];
            final titleMultilang = data['titleMultilang'];
            final bodyMultilang = data['bodyMultilang'];
            await _showNotificationWithDefaultSound(
                title, body, titleMultilang, bodyMultilang);
          } else if (message.data['title'] == 'You have new message(s)') {
            var currentpeer =
                Provider.of<CurrentChatPeer>(this.context, listen: false);
            if (currentpeer.peerid != message.data['peerid']) {
              // FlutterRingtonePlayer.playNotification();
              showOverlayNotification((context) {
                return Card(
                  margin: const EdgeInsets.symmetric(horizontal: 4),
                  child: SafeArea(
                    child: ListTile(
                      title: Text(
                        message.data['titleMultilang'],
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                      subtitle: Text(
                        message.data['bodyMultilang'],
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                      trailing: IconButton(
                          icon: Icon(Icons.close),
                          onPressed: () {
                            OverlaySupportEntry.of(context)!.dismiss();
                          }),
                    ),
                  ),
                );
              }, duration: Duration(milliseconds: 2000));
            }
          } else {
            showOverlayNotification((context) {
              return Card(
                margin: const EdgeInsets.symmetric(horizontal: 4),
                child: SafeArea(
                  child: ListTile(
                    leading: message.data.containsKey("image")
                        ? SizedBox()
                        : message.data["image"] == null
                            ? SizedBox()
                            : Image.network(
                                message.data['image'],
                                width: 50,
                                height: 70,
                                fit: BoxFit.cover,
                              ),
                    title: Text(
                      message.data['titleMultilang'],
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    subtitle: Text(
                      message.data['bodyMultilang'],
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                    trailing: IconButton(
                        icon: Icon(Icons.close),
                        onPressed: () {
                          OverlaySupportEntry.of(context)!.dismiss();
                        }),
                  ),
                ),
              );
            }, duration: Duration(milliseconds: 2000));
          }
        }
      }
    });
    //ANDROID & iOS  onMessageOpenedApp callback
    FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) async {
      flutterLocalNotificationsPlugin..cancelAll();
      Map<String, dynamic> notificationData = message.data;
      AndroidNotification? android = message.notification?.android;
      if (android != null) {
        if (notificationData['title'] == 'Call Ended') {
          flutterLocalNotificationsPlugin..cancelAll();
        } else if (notificationData['title'] != 'Call Ended' &&
            notificationData['title'] != 'You have new message(s)' &&
            notificationData['title'] != 'Missed Call' &&
            notificationData['title'] != 'Incoming Video Call...' &&
            notificationData['title'] != 'Incoming Audio Call...' &&
            notificationData['title'] != 'Incoming Call ended' &&
            notificationData['title'] != 'New message in Group') {
          flutterLocalNotificationsPlugin..cancelAll();

          Navigator.push(
              context,
              new MaterialPageRoute(
                  builder: (context) => AllNotifications(
                        prefs: widget.prefs,
                      )));
        } else {
          flutterLocalNotificationsPlugin..cancelAll();
        }
      }
    });
    FirebaseMessaging.instance.getInitialMessage().then((message) {
      if (message != null) {
        flutterLocalNotificationsPlugin..cancelAll();
        Map<String, dynamic>? notificationData = message.data;
        if (notificationData['title'] != 'Call Ended' &&
            notificationData['title'] != 'You have new message(s)' &&
            notificationData['title'] != 'Missed Call' &&
            notificationData['title'] != 'Incoming Video Call...' &&
            notificationData['title'] != 'Incoming Audio Call...' &&
            notificationData['title'] != 'Incoming Call ended' &&
            notificationData['title'] != 'New message in Group') {
          flutterLocalNotificationsPlugin..cancelAll();

          Navigator.push(
              context,
              new MaterialPageRoute(
                  builder: (context) => AllNotifications(
                        prefs: widget.prefs,
                      )));
        }
      }
    });
  }

  DataModel? _cachedModel;
  bool showHidden = false, biometricEnabled = false;

  DataModel? getModel() {
    _cachedModel ??= DataModel(widget.currentUserNo);
    return _cachedModel;
  }

  getSignedInUserOrRedirect() async {
    try {
      setState(() {
        isblockNewlogins = widget.doc.data()![Dbkeys.isblocknewlogins];
        isApprovalNeededbyAdminForNewUser =
            widget.doc[Dbkeys.isaccountapprovalbyadminneeded];
        accountApprovalMessage = widget.doc[Dbkeys.accountapprovalmessage];
      });
      if (widget.doc.data()![Dbkeys.isemulatorallowed] == false &&
          mapDeviceInfo[Dbkeys.deviceInfoISPHYSICAL] == false) {
        setState(() {
          isNotAllowEmulator = true;
        });
      } else {
        if (widget.doc[Platform.isAndroid
                ? Dbkeys.isappunderconstructionandroid
                : Platform.isIOS
                    ? Dbkeys.isappunderconstructionios
                    : Dbkeys.isappunderconstructionweb] ==
            true) {
          await unsubscribeToNotification(widget.currentUserNo);
          maintainanceMessage = widget.doc[Dbkeys.maintainancemessage];
          setState(() {});
        } else {
          final PackageInfo info = await PackageInfo.fromPlatform();

          int currentAppVersionInPhone = int.tryParse(info.version
                      .trim()
                      .split(".")[0]
                      .toString()
                      .padLeft(3, '0') +
                  info.version.trim().split(".")[1].toString().padLeft(3, '0') +
                  info.version
                      .trim()
                      .split(".")[2]
                      .toString()
                      .padLeft(3, '0')) ??
              0;
          int currentNewAppVersionInServer =
              int.tryParse(widget.doc[Platform.isAndroid
                              ? Dbkeys.latestappversionandroid
                              : Platform.isIOS
                                  ? Dbkeys.latestappversionios
                                  : Dbkeys.latestappversionweb]
                          .trim()
                          .split(".")[0]
                          .toString()
                          .padLeft(3, '0') +
                      widget.doc[Platform.isAndroid
                              ? Dbkeys.latestappversionandroid
                              : Platform.isIOS
                                  ? Dbkeys.latestappversionios
                                  : Dbkeys.latestappversionweb]
                          .trim()
                          .split(".")[1]
                          .toString()
                          .padLeft(3, '0') +
                      widget.doc[Platform.isAndroid
                              ? Dbkeys.latestappversionandroid
                              : Platform.isIOS
                                  ? Dbkeys.latestappversionios
                                  : Dbkeys.latestappversionweb]
                          .trim()
                          .split(".")[2]
                          .toString()
                          .padLeft(3, '0')) ??
                  0;
          if (currentAppVersionInPhone < currentNewAppVersionInServer) {
            showDialog<String>(
              context: context,
              barrierDismissible: false,
              builder: (BuildContext context) {
                String title = getTranslated(context, 'updateavl');
                String message = getTranslated(context, 'updateavlmsg');

                String btnLabel = getTranslated(context, 'updatnow');

                return new WillPopScope(
                    onWillPop: () async => false,
                    child: AlertDialog(
                      title: Text(
                        title,
                        style: TextStyle(color: fiberchatDeepGreen),
                      ),
                      content: Text(message),
                      actions: <Widget>[
                        TextButton(
                            child: Text(
                              btnLabel,
                              style: TextStyle(color: fiberchatLightGreen),
                            ),
                            onPressed: () => custom_url_launcher(
                                widget.doc[Platform.isAndroid
                                    ? Dbkeys.newapplinkandroid
                                    : Platform.isIOS
                                        ? Dbkeys.newapplinkios
                                        : Dbkeys.newapplinkweb])),
                      ],
                    ));
              },
            );
          } else {
            final observer = Provider.of<Observer>(this.context, listen: false);

            observer.setObserver(
              getuserAppSettingsDoc: widget.doc,
              getandroidapplink: widget.doc[Dbkeys.newapplinkandroid],
              getiosapplink: widget.doc[Dbkeys.newapplinkios],
              getisadmobshow: widget.doc[Dbkeys.isadmobshow],
              getismediamessagingallowed:
                  widget.doc[Dbkeys.ismediamessageallowed],
              getistextmessagingallowed:
                  widget.doc[Dbkeys.istextmessageallowed],
              getiscallsallowed: widget.doc[Dbkeys.iscallsallowed],
              gettnc: widget.doc[Dbkeys.tnc],
              gettncType: widget.doc[Dbkeys.tncTYPE],
              getprivacypolicy: widget.doc[Dbkeys.privacypolicy],
              getprivacypolicyType: widget.doc[Dbkeys.privacypolicyTYPE],
              getis24hrsTimeformat: widget.doc[Dbkeys.is24hrsTimeformat],
              getmaxFileSizeAllowedInMB:
                  widget.doc[Dbkeys.maxFileSizeAllowedInMB],
              getisPercentProgressShowWhileUploading:
                  widget.doc[Dbkeys.isPercentProgressShowWhileUploading],
              getisCallFeatureTotallyHide:
                  widget.doc[Dbkeys.isCallFeatureTotallyHide],
              getgroupMemberslimit: widget.doc[Dbkeys.groupMemberslimit],
              getbroadcastMemberslimit:
                  widget.doc[Dbkeys.broadcastMemberslimit],
              getstatusDeleteAfterInHours:
                  widget.doc[Dbkeys.statusDeleteAfterInHours],
              getfeedbackEmail: widget.doc[Dbkeys.feedbackEmail],
              getisLogoutButtonShowInSettingsPage:
                  widget.doc[Dbkeys.isLogoutButtonShowInSettingsPage],
              getisAllowCreatingGroups:
                  widget.doc[Dbkeys.isAllowCreatingGroups],
              getisAllowCreatingBroadcasts:
                  widget.doc[Dbkeys.isAllowCreatingBroadcasts],
              getisAllowCreatingStatus:
                  widget.doc[Dbkeys.isAllowCreatingStatus],
              getmaxNoOfFilesInMultiSharing:
                  widget.doc[Dbkeys.maxNoOfFilesInMultiSharing],
              getmaxNoOfContactsSelectForForward:
                  widget.doc[Dbkeys.maxNoOfContactsSelectForForward],
              getappShareMessageStringAndroid:
                  widget.doc[Dbkeys.appShareMessageStringAndroid],
              getappShareMessageStringiOS:
                  widget.doc[Dbkeys.appShareMessageStringiOS],
              getisCustomAppShareLink: widget.doc[Dbkeys.isCustomAppShareLink],
            );

            if (widget.currentUserNo == null || widget.currentUserNo!.isEmpty) {
              // await unsubscribeToNotification(widget.currentUserNo);

              unawaited(Navigator.pushReplacement(
                  context,
                  new MaterialPageRoute(
                      builder: (context) => new LoginScreen(
                            prefs: widget.prefs,
                            accountApprovalMessage: accountApprovalMessage,
                            isaccountapprovalbyadminneeded:
                                isApprovalNeededbyAdminForNewUser,
                            isblocknewlogins: isblockNewlogins,
                            title: getTranslated(context, 'signin'),
                            doc: widget.doc,
                          ))));
            } else {
              await FirebaseFirestore.instance
                  .collection(DbPaths.collectionusers)
                  .doc(widget.currentUserNo ?? widget.currentUserNo)
                  .get()
                  .then((userDoc) async {
                if (deviceid != userDoc[Dbkeys.currentDeviceID] ||
                    !userDoc.data()!.containsKey(Dbkeys.currentDeviceID)) {
                  if (ConnectWithAdminApp == true) {
                    await unsubscribeToNotification(widget.currentUserNo);
                  }
                  await logout(context);
                } else {
                  if (!userDoc.data()!.containsKey(Dbkeys.accountstatus)) {
                    await logout(context);
                  } else if (userDoc[Dbkeys.accountstatus] !=
                      Dbkeys.sTATUSallowed) {
                    if (userDoc[Dbkeys.accountstatus] == Dbkeys.sTATUSdeleted) {
                      setState(() {
                        accountstatus = userDoc[Dbkeys.accountstatus];
                        accountactionmessage = userDoc[Dbkeys.actionmessage];
                      });
                    } else {
                      setState(() {
                        accountstatus = userDoc[Dbkeys.accountstatus];
                        accountactionmessage = userDoc[Dbkeys.actionmessage];
                      });
                    }
                  } else {
                    setState(() {
                      userFullname = userDoc[Dbkeys.nickname];
                      userPhotourl = userDoc[Dbkeys.photoUrl];
                      phoneNumberVariants = phoneNumberVariantsList(
                          countrycode: userDoc[Dbkeys.countryCode],
                          phonenumber: userDoc[Dbkeys.phoneRaw]);
                      isFetching = false;
                    });
                    getuid(context);
                    setIsActive();

                    incrementSessionCount(userDoc[Dbkeys.phone]);
                  }
                }
              });
            }
          }
        }
      }
    } catch (e) {
      showERRORSheet(this.context, "", message: e.toString());
    }

    // await FirebaseFirestore.instance
    //     .collection(Dbkeys.appsettings)
    //     .doc(Dbkeys.userapp)
    //     .get()
    //     .then((doc) async {
    //   if (doc.exists && doc.data()!.containsKey(Dbkeys.usersidesetupdone)) {
    //     if (!doc.data()!.containsKey(Dbkeys.updateV7done)) {
    //       doc.reference.update({
    //         Dbkeys.maxNoOfFilesInMultiSharing: MaxNoOfFilesInMultiSharing,
    //         Dbkeys.maxNoOfContactsSelectForForward:
    //             MaxNoOfContactsSelectForForward,
    //         Dbkeys.appShareMessageStringAndroid: '',
    //         Dbkeys.appShareMessageStringiOS: '',
    //         Dbkeys.isCustomAppShareLink: false,
    //         Dbkeys.updateV7done: true,
    //       });
    //       Fiberchat.toast(getTranslated(this.context, 'erroroccured'));
    //     } else {
    //       setState(() {
    //         isblockNewlogins = doc[Dbkeys.isblocknewlogins];
    //         isApprovalNeededbyAdminForNewUser =
    //             doc[Dbkeys.isaccountapprovalbyadminneeded];
    //         accountApprovalMessage = doc[Dbkeys.accountapprovalmessage];
    //       });
    //       if (doc[Dbkeys.isemulatorallowed] == false &&
    //           mapDeviceInfo[Dbkeys.deviceInfoISPHYSICAL] == false) {
    //         setState(() {
    //           isNotAllowEmulator = true;
    //         });
    //       } else {
    //         if (doc[Platform.isAndroid
    //                 ? Dbkeys.isappunderconstructionandroid
    //                 : Platform.isIOS
    //                     ? Dbkeys.isappunderconstructionios
    //                     : Dbkeys.isappunderconstructionweb] ==
    //             true) {
    //           await unsubscribeToNotification(widget.currentUserNo);
    //           maintainanceMessage = doc[Dbkeys.maintainancemessage];
    //           setState(() {});
    //         } else {
    //           final PackageInfo info = await PackageInfo.fromPlatform();

    //           int currentAppVersionInPhone = int.tryParse(info.version
    //                       .trim()
    //                       .split(".")[0]
    //                       .toString()
    //                       .padLeft(3, '0') +
    //                   info.version
    //                       .trim()
    //                       .split(".")[1]
    //                       .toString()
    //                       .padLeft(3, '0') +
    //                   info.version
    //                       .trim()
    //                       .split(".")[2]
    //                       .toString()
    //                       .padLeft(3, '0')) ??
    //               0;
    //           int currentNewAppVersionInServer =
    //               int.tryParse(doc[Platform.isAndroid
    //                               ? Dbkeys.latestappversionandroid
    //                               : Platform.isIOS
    //                                   ? Dbkeys.latestappversionios
    //                                   : Dbkeys.latestappversionweb]
    //                           .trim()
    //                           .split(".")[0]
    //                           .toString()
    //                           .padLeft(3, '0') +
    //                       doc[Platform.isAndroid
    //                               ? Dbkeys.latestappversionandroid
    //                               : Platform.isIOS
    //                                   ? Dbkeys.latestappversionios
    //                                   : Dbkeys.latestappversionweb]
    //                           .trim()
    //                           .split(".")[1]
    //                           .toString()
    //                           .padLeft(3, '0') +
    //                       doc[Platform.isAndroid
    //                               ? Dbkeys.latestappversionandroid
    //                               : Platform.isIOS
    //                                   ? Dbkeys.latestappversionios
    //                                   : Dbkeys.latestappversionweb]
    //                           .trim()
    //                           .split(".")[2]
    //                           .toString()
    //                           .padLeft(3, '0')) ??
    //                   0;
    //           if (currentAppVersionInPhone < currentNewAppVersionInServer) {
    //             showDialog<String>(
    //               context: context,
    //               barrierDismissible: false,
    //               builder: (BuildContext context) {
    //                 String title = getTranslated(context, 'updateavl');
    //                 String message = getTranslated(context, 'updateavlmsg');

    //                 String btnLabel = getTranslated(context, 'updatnow');

    //                 return new WillPopScope(
    //                     onWillPop: () async => false,
    //                     child: AlertDialog(
    //                       title: Text(
    //                         title,
    //                         style: TextStyle(color: fiberchatDeepGreen),
    //                       ),
    //                       content: Text(message),
    //                       actions: <Widget>[
    //                         TextButton(
    //                             child: Text(
    //                               btnLabel,
    //                               style: TextStyle(color: fiberchatLightGreen),
    //                             ),
    //                             onPressed: () =>
    //                                 custom_url_launcher(doc[Platform.isAndroid
    //                                     ? Dbkeys.newapplinkandroid
    //                                     : Platform.isIOS
    //                                         ? Dbkeys.newapplinkios
    //                                         : Dbkeys.newapplinkweb])),
    //                       ],
    //                     ));
    //               },
    //             );
    //           } else {
    //             final observer =
    //                 Provider.of<Observer>(this.context, listen: false);

    //             observer.setObserver(
    //               getuserAppSettingsDoc: doc.data(),
    //               getandroidapplink: doc[Dbkeys.newapplinkandroid],
    //               getiosapplink: doc[Dbkeys.newapplinkios],
    //               getisadmobshow: doc[Dbkeys.isadmobshow],
    //               getismediamessagingallowed: doc[Dbkeys.ismediamessageallowed],
    //               getistextmessagingallowed: doc[Dbkeys.istextmessageallowed],
    //               getiscallsallowed: doc[Dbkeys.iscallsallowed],
    //               gettnc: doc[Dbkeys.tnc],
    //               gettncType: doc[Dbkeys.tncTYPE],
    //               getprivacypolicy: doc[Dbkeys.privacypolicy],
    //               getprivacypolicyType: doc[Dbkeys.privacypolicyTYPE],
    //               getis24hrsTimeformat: doc[Dbkeys.is24hrsTimeformat],
    //               getmaxFileSizeAllowedInMB: doc[Dbkeys.maxFileSizeAllowedInMB],
    //               getisPercentProgressShowWhileUploading:
    //                   doc[Dbkeys.isPercentProgressShowWhileUploading],
    //               getisCallFeatureTotallyHide:
    //                   doc[Dbkeys.isCallFeatureTotallyHide],
    //               getgroupMemberslimit: doc[Dbkeys.groupMemberslimit],
    //               getbroadcastMemberslimit: doc[Dbkeys.broadcastMemberslimit],
    //               getstatusDeleteAfterInHours:
    //                   doc[Dbkeys.statusDeleteAfterInHours],
    //               getfeedbackEmail: doc[Dbkeys.feedbackEmail],
    //               getisLogoutButtonShowInSettingsPage:
    //                   doc[Dbkeys.isLogoutButtonShowInSettingsPage],
    //               getisAllowCreatingGroups: doc[Dbkeys.isAllowCreatingGroups],
    //               getisAllowCreatingBroadcasts:
    //                   doc[Dbkeys.isAllowCreatingBroadcasts],
    //               getisAllowCreatingStatus: doc[Dbkeys.isAllowCreatingStatus],
    //               getmaxNoOfFilesInMultiSharing:
    //                   doc[Dbkeys.maxNoOfFilesInMultiSharing],
    //               getmaxNoOfContactsSelectForForward:
    //                   doc[Dbkeys.maxNoOfContactsSelectForForward],
    //               getappShareMessageStringAndroid:
    //                   doc[Dbkeys.appShareMessageStringAndroid],
    //               getappShareMessageStringiOS:
    //                   doc[Dbkeys.appShareMessageStringiOS],
    //               getisCustomAppShareLink: doc[Dbkeys.isCustomAppShareLink],
    //             );

    //             if (currentUserNo == null ||
    //                 currentUserNo!.isEmpty ||
    //                 widget.isSecuritySetupDone == false) {
    //               await unsubscribeToNotification(widget.currentUserNo);
    //               unawaited(Navigator.pushReplacement(
    //                   context,
    //                   new MaterialPageRoute(
    //                       builder: (context) => new LoginScreen(
    //                         doc: widget.doc,
    //                             prefs: widget.prefs,
    //                             accountApprovalMessage: accountApprovalMessage,
    //                             isaccountapprovalbyadminneeded:
    //                                 isApprovalNeededbyAdminForNewUser,
    //                             isblocknewlogins: isblockNewlogins,
    //                             title: getTranslated(context, 'signin'),
    //                             issecutitysetupdone: widget.isSecuritySetupDone,
    //                           ))));
    //             } else {
    //               await FirebaseFirestore.instance
    //                   .collection(DbPaths.collectionusers)
    //                   .doc(widget.currentUserNo ?? currentUserNo)
    //                   .get()
    //                   .then((userDoc) async {
    //                 if (deviceid != userDoc[Dbkeys.currentDeviceID] ||
    //                     !userDoc.data()!.containsKey(Dbkeys.currentDeviceID)) {
    //                   if (ConnectWithAdminApp == true) {
    //                     await unsubscribeToNotification(widget.currentUserNo);
    //                   }
    //                   await logout(context);
    //                 } else {
    //                   if (!userDoc.data()!.containsKey(Dbkeys.accountstatus)) {
    //                     await logout(context);
    //                   } else if (userDoc[Dbkeys.accountstatus] !=
    //                       Dbkeys.sTATUSallowed) {
    //                     setState(() {
    //                       accountstatus = userDoc[Dbkeys.accountstatus];
    //                       accountactionmessage = userDoc[Dbkeys.actionmessage];
    //                     });
    //                   } else {
    //                     setState(() {
    //                       userFullname = userDoc[Dbkeys.nickname];
    //                       userPhotourl = userDoc[Dbkeys.photoUrl];
    //                       phoneNumberVariants = phoneNumberVariantsList(
    //                           countrycode: userDoc[Dbkeys.countryCode],
    //                           phonenumber: userDoc[Dbkeys.phoneRaw]);
    //                       isFetching = false;
    //                     });
    //                     getuid(context);
    //                     setIsActive();

    //                     incrementSessionCount(userDoc[Dbkeys.phone]);
    //                   }
    //                 }
    //               });
    //             }
    //           }
    //         }
    //       }
    //     }
    //   } else {
    //     await setupAdminAppCompatibleDataForFirstTime().then((result) {
    //       if (result == true) {
    //         Fiberchat.toast(getTranslated(this.context, 'erroroccured'));
    //       } else if (result == false) {
    //         Fiberchat.toast(
    //           'Error occured while writing setupAdminAppCompatibleDataForFirstTime().Please restart the app.',
    //         );
    //       }
    //     });
    //   }
    // }).catchError((err) {
    //   Fiberchat.toast(
    //     'Error occured while fetching appsettings/userapp. ERROR: $err',
    //   );
    // });}
  }

  StreamController<String> _userQuery =
      new StreamController<String>.broadcast();
  void _changeLanguage(Language language) async {
    Locale _locale = await setLocale(language.languageCode);
    FiberchatWrapper.setLocale(context, _locale);
    if (widget.currentUserNo != null) {
      Future.delayed(const Duration(milliseconds: 800), () {
        FirebaseFirestore.instance
            .collection(DbPaths.collectionusers)
            .doc(widget.currentUserNo)
            .update({
          Dbkeys.notificationStringsMap:
              getTranslateNotificationStringsMap(this.context),
        });
      });
    }
    setState(() {
      // seletedlanguage = language;
    });

    await widget.prefs.setBool('islanguageselected', true);
  }

  DateTime? currentBackPressTime = DateTime.now();
  Future<bool> onWillPop() {
    DateTime now = DateTime.now();
    if (now.difference(currentBackPressTime!) > Duration(seconds: 3)) {
      currentBackPressTime = now;
      Fiberchat.toast('Double Tap To Go Back');
      return Future.value(false);
    } else {
      if (!isAuthenticating) setLastSeen();
      return Future.value(true);
    }
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);

    final observer = Provider.of<Observer>(context, listen: true);
    return isNotAllowEmulator == true
        ? errorScreen(
            'Emulator Not Allowed.', ' Please use any real device & Try again.')
        : accountstatus != null
            ? errorScreen(accountstatus, accountactionmessage)
            : ConnectWithAdminApp == true && maintainanceMessage != null
                ? errorScreen('App Under maintainance', maintainanceMessage)
                : ConnectWithAdminApp == true && isFetching == true
                    ? Splashscreen(
                        isShowOnlySpinner: widget.isShowOnlyCircularSpin,
                      )
                    : PickupLayout(
                        prefs: widget.prefs,
                        scaffold: Fiberchat.getNTPWrappedWidget(WillPopScope(
                          onWillPop: onWillPop,
                          child: Scaffold(
                              backgroundColor: Colors.white,
                              appBar: AppBar(
                                  elevation: DESIGN_TYPE == Themetype.messenger
                                      ? 0.4
                                      : 1,
                                  backgroundColor:
                                      DESIGN_TYPE == Themetype.whatsapp
                                          ? fiberchatDeepGreen
                                          : fiberchatWhite,
                                  title: Text(
                                    Appname,
                                    style: TextStyle(
                                      color: DESIGN_TYPE == Themetype.whatsapp
                                          ? fiberchatWhite
                                          : fiberchatBlack,
                                      fontSize: 20.0,
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                  // title: Align(
                                  //   alignment: Alignment.centerLeft,
                                  //   child: Image.asset(
                                  //       'assets/images/applogo.png',
                                  //       height: 80,
                                  //       width: 140,
                                  //       fit: BoxFit.fitHeight),
                                  // ),
                                  // titleSpacing: 14,
                                  actions: <Widget>[
//
                                    Language.languageList().length < 2
                                        ? SizedBox()
                                        : Container(
                                            alignment: Alignment.centerRight,
                                            margin: EdgeInsets.only(top: 4),
                                            width: 120,
                                            child: DropdownButton<Language>(
                                              // iconSize: 40,

                                              isExpanded: true,
                                              underline: SizedBox(),
                                              icon: Container(
                                                width: 60,
                                                height: 30,
                                                child: Row(
                                                  crossAxisAlignment:
                                                      CrossAxisAlignment.center,
                                                  children: [
                                                    Icon(
                                                      Icons.language_outlined,
                                                      color: DESIGN_TYPE ==
                                                              Themetype.whatsapp
                                                          ? fiberchatWhite
                                                          : fiberchatBlack
                                                              .withOpacity(0.7),
                                                      size: 22,
                                                    ),
                                                    SizedBox(
                                                      width: 4,
                                                    ),
                                                    Icon(
                                                      Icons.keyboard_arrow_down,
                                                      color: DESIGN_TYPE ==
                                                              Themetype.whatsapp
                                                          ? fiberchatLightGreen
                                                          : fiberchatLightGreen,
                                                      size: 27,
                                                    )
                                                  ],
                                                ),
                                              ),
                                              onChanged: (Language? language) {
                                                _changeLanguage(language!);
                                              },
                                              items: Language.languageList()
                                                  .map<
                                                      DropdownMenuItem<
                                                          Language>>(
                                                    (e) => DropdownMenuItem<
                                                        Language>(
                                                      value: e,
                                                      child: Row(
                                                        mainAxisAlignment:
                                                            MainAxisAlignment
                                                                .end,
                                                        children: <Widget>[
                                                          Text(
                                                            IsShowLanguageNameInNativeLanguage ==
                                                                    true
                                                                ? '' +
                                                                    e.name +
                                                                    '  ' +
                                                                    e.flag +
                                                                    ' '
                                                                : ' ' +
                                                                    e.languageNameInEnglish +
                                                                    '  ' +
                                                                    e.flag +
                                                                    ' ',
                                                            style: TextStyle(
                                                                fontSize: 15),
                                                          ),
                                                        ],
                                                      ),
                                                    ),
                                                  )
                                                  .toList(),
                                            ),
                                          ),
// // //---- All localizations settings----
                                    PopupMenuButton(
                                        padding: EdgeInsets.all(0),
                                        icon: Padding(
                                          padding:
                                              const EdgeInsets.only(right: 1),
                                          child: Icon(
                                            Icons.more_vert_outlined,
                                            color: DESIGN_TYPE ==
                                                    Themetype.whatsapp
                                                ? fiberchatWhite
                                                : fiberchatBlack,
                                          ),
                                        ),
                                        color: fiberchatWhite,
                                        onSelected: (dynamic val) async {
                                          switch (val) {
                                            case 'rate':
                                              break;
                                            case 'tutorials':
                                              showDialog(
                                                  context: context,
                                                  builder: (context) {
                                                    return SimpleDialog(
                                                      contentPadding:
                                                          EdgeInsets.all(20),
                                                      children: <Widget>[
                                                        ListTile(
                                                          title: Text(
                                                            getTranslated(
                                                                context,
                                                                'swipeview'),
                                                          ),
                                                        ),
                                                        SizedBox(
                                                          height: 10,
                                                        ),
                                                        ListTile(
                                                            title: Text(
                                                          getTranslated(context,
                                                              'swipehide'),
                                                        )),
                                                        SizedBox(
                                                          height: 10,
                                                        ),
                                                        ListTile(
                                                            title: Text(
                                                          getTranslated(context,
                                                              'lp_setalias'),
                                                        ))
                                                      ],
                                                    );
                                                  });
                                              break;
                                            case 'privacy':
                                              break;
                                            case 'tnc':
                                              break;
                                            case 'share':
                                              break;
                                            case 'notifications':
                                              Navigator.push(
                                                  context,
                                                  new MaterialPageRoute(
                                                      builder: (context) =>
                                                          AllNotifications(
                                                            prefs: widget.prefs,
                                                          )));

                                              break;
                                            case 'feedback':
                                              break;
                                            case 'logout':
                                              break;
                                            case 'settings':
                                              Navigator.push(
                                                  context,
                                                  new MaterialPageRoute(
                                                      builder:
                                                          (context) =>
                                                              SettingsOption(
                                                                prefs: widget
                                                                    .prefs,
                                                                onTapLogout:
                                                                    () async {
                                                                  await logout(
                                                                      context);
                                                                },
                                                                onTapEditProfile:
                                                                    () {
                                                                  Navigator.push(
                                                                      context,
                                                                      new MaterialPageRoute(
                                                                          builder: (context) => ProfileSetting(
                                                                                prefs: widget.prefs,
                                                                                biometricEnabled: biometricEnabled,
                                                                                type: Fiberchat.getAuthenticationType(biometricEnabled, _cachedModel),
                                                                              )));
                                                                },
                                                                currentUserNo:
                                                                    widget
                                                                        .currentUserNo!,
                                                                biometricEnabled:
                                                                    biometricEnabled,
                                                                type: Fiberchat
                                                                    .getAuthenticationType(
                                                                        biometricEnabled,
                                                                        _cachedModel),
                                                              )));

                                              break;
                                            case 'group':
                                              if (observer
                                                      .isAllowCreatingGroups ==
                                                  false) {
                                                Fiberchat.showRationale(
                                                    getTranslated(this.context,
                                                        'disabled'));
                                              } else {
                                                final AvailableContactsProvider
                                                    dbcontactsProvider =
                                                    Provider.of<
                                                            AvailableContactsProvider>(
                                                        context,
                                                        listen: false);
                                                dbcontactsProvider
                                                    .fetchContacts(
                                                        context,
                                                        _cachedModel,
                                                        widget.currentUserNo!,
                                                        widget.prefs);
                                                Navigator.push(
                                                    context,
                                                    MaterialPageRoute(
                                                        builder: (context) =>
                                                            AddContactsToGroup(
                                                              currentUserNo: widget
                                                                  .currentUserNo,
                                                              model:
                                                                  _cachedModel,
                                                              biometricEnabled:
                                                                  false,
                                                              prefs:
                                                                  widget.prefs,
                                                              isAddingWhileCreatingGroup:
                                                                  true,
                                                            )));
                                              }
                                              break;

                                            case 'broadcast':
                                              if (observer
                                                      .isAllowCreatingBroadcasts ==
                                                  false) {
                                                Fiberchat.showRationale(
                                                    getTranslated(this.context,
                                                        'disabled'));
                                              } else {
                                                final AvailableContactsProvider
                                                    dbcontactsProvider =
                                                    Provider.of<
                                                            AvailableContactsProvider>(
                                                        context,
                                                        listen: false);
                                                dbcontactsProvider
                                                    .fetchContacts(
                                                        context,
                                                        _cachedModel,
                                                        widget.currentUserNo!,
                                                        widget.prefs);
                                                await Navigator.push(
                                                    context,
                                                    MaterialPageRoute(
                                                        builder: (context) =>
                                                            AddContactsToBroadcast(
                                                              currentUserNo: widget
                                                                  .currentUserNo,
                                                              model:
                                                                  _cachedModel,
                                                              biometricEnabled:
                                                                  false,
                                                              prefs:
                                                                  widget.prefs,
                                                              isAddingWhileCreatingBroadcast:
                                                                  true,
                                                            )));
                                              }
                                              break;
                                          }
                                        },
                                        itemBuilder: (context) =>
                                            <PopupMenuItem<String>>[
                                              PopupMenuItem<String>(
                                                  value: 'group',
                                                  child: Text(
                                                    getTranslated(
                                                        context, 'newgroup'),
                                                  )),
                                              PopupMenuItem<String>(
                                                  value: 'broadcast',
                                                  child: Text(
                                                    getTranslated(context,
                                                        'newbroadcast'),
                                                  )),
                                              PopupMenuItem<String>(
                                                value: 'tutorials',
                                                child: Text(
                                                  getTranslated(
                                                      context, 'tutorials'),
                                                ),
                                              ),
                                              PopupMenuItem<String>(
                                                  value: 'settings',
                                                  child: Text(
                                                    getTranslated(context,
                                                        'settingsoption'),
                                                  )),
                                            ]),
                                  ],
                                  bottom: TabBar(
                                    isScrollable: IsAdaptiveWidthTab == true
                                        ? true
                                        : DEFAULT_LANGUAGE_FILE_CODE == "en" &&
                                                (widget.prefs.getString(
                                                            LAGUAGE_CODE) ==
                                                        null ||
                                                    widget.prefs
                                                            .getString(
                                                                LAGUAGE_CODE) ==
                                                        "en")
                                            ? false
                                            : widget
                                                            .prefs
                                                            .getString(
                                                                LAGUAGE_CODE) ==
                                                        'pt' ||
                                                    widget
                                                            .prefs
                                                            .getString(
                                                                LAGUAGE_CODE) ==
                                                        'my' ||
                                                    widget
                                                            .prefs
                                                            .getString(
                                                                LAGUAGE_CODE) ==
                                                        'nl' ||
                                                    widget
                                                            .prefs
                                                            .getString(
                                                                LAGUAGE_CODE) ==
                                                        'vi' ||
                                                    widget
                                                            .prefs
                                                            .getString(
                                                                LAGUAGE_CODE) ==
                                                        'tr' ||
                                                    widget
                                                            .prefs
                                                            .getString(
                                                                LAGUAGE_CODE) ==
                                                        'id' ||
                                                    widget.prefs.getString(
                                                            LAGUAGE_CODE) ==
                                                        'ka' ||
                                                    widget.prefs.getString(
                                                            LAGUAGE_CODE) ==
                                                        'fr' ||
                                                    widget.prefs.getString(
                                                            LAGUAGE_CODE) ==
                                                        'es'
                                                ? true
                                                : false,
                                    labelStyle: TextStyle(
                                      fontWeight: FontWeight.bold,
                                      fontFamily: FONTFAMILY_NAME,
                                    ),
                                    unselectedLabelStyle: TextStyle(
                                      fontWeight: FontWeight.bold,
                                      fontFamily: FONTFAMILY_NAME,
                                    ),
                                    labelColor:
                                        DESIGN_TYPE == Themetype.whatsapp
                                            ? fiberchatWhite
                                            : fiberchatBlack,
                                    unselectedLabelColor:
                                        DESIGN_TYPE == Themetype.whatsapp
                                            ? fiberchatWhite.withOpacity(0.6)
                                            : fiberchatBlack.withOpacity(0.6),
                                    indicatorWeight: 3,
                                    indicatorColor:
                                        DESIGN_TYPE == Themetype.whatsapp
                                            ? fiberchatWhite
                                            : fiberchatgreen,
                                    controller:
                                        observer.isCallFeatureTotallyHide ==
                                                false
                                            ? controllerIfcallallowed
                                            : controllerIfcallNotallowed,
                                    tabs: observer.isCallFeatureTotallyHide ==
                                            false
                                        ? <Widget>[
                                            Tab(
                                              child: Text(
                                                getTranslated(context, 'chats'),
                                                textAlign: TextAlign.center,
                                                style: TextStyle(
                                                    fontFamily:
                                                        FONTFAMILY_NAME),
                                              ),
                                            ),
                                            Tab(
                                              child: Text(
                                                getTranslated(
                                                    context, 'status'),
                                                textAlign: TextAlign.center,
                                                style: TextStyle(
                                                    fontFamily:
                                                        FONTFAMILY_NAME),
                                              ),
                                            ),
                                            Tab(
                                              child: Text(
                                                getTranslated(context, 'calls'),
                                                textAlign: TextAlign.center,
                                                style: TextStyle(
                                                    fontFamily:
                                                        FONTFAMILY_NAME),
                                              ),
                                            ),
                                          ]
                                        : <Widget>[
                                            Tab(
                                              child: Text(
                                                getTranslated(context, 'chats'),
                                                textAlign: TextAlign.center,
                                                style: TextStyle(
                                                    fontFamily:
                                                        FONTFAMILY_NAME),
                                              ),
                                            ),
                                            Tab(
                                              child: Text(
                                                getTranslated(
                                                    context, 'status'),
                                                textAlign: TextAlign.center,
                                                style: TextStyle(
                                                    fontFamily:
                                                        FONTFAMILY_NAME),
                                              ),
                                            ),
                                          ],
                                  )),
                              body: TabBarView(
                                controller:
                                    observer.isCallFeatureTotallyHide == false
                                        ? controllerIfcallallowed
                                        : controllerIfcallNotallowed,
                                children: observer.isCallFeatureTotallyHide ==
                                        false
                                    ? <Widget>[
                                        IsShowLastMessageInChatTileWithTime ==
                                                false
                                            ? RecentChatsWithoutLastMessage(
                                                prefs: widget.prefs,
                                                currentUserNo:
                                                    widget.currentUserNo,
                                                isSecuritySetupDone: false)
                                            : RecentChats(
                                                prefs: widget.prefs,
                                                currentUserNo:
                                                    widget.currentUserNo,
                                                isSecuritySetupDone: false),
                                        Status(
                                            currentUserFullname: userFullname,
                                            currentUserPhotourl: userPhotourl,
                                            phoneNumberVariants:
                                                this.phoneNumberVariants,
                                            currentUserNo: widget.currentUserNo,
                                            model: _cachedModel,
                                            biometricEnabled: biometricEnabled,
                                            prefs: widget.prefs),
                                        CallHistory(
                                          model: _cachedModel,
                                          userphone: widget.currentUserNo,
                                          prefs: widget.prefs,
                                        ),
                                      ]
                                    : <Widget>[
                                        IsShowLastMessageInChatTileWithTime ==
                                                false
                                            ? RecentChatsWithoutLastMessage(
                                                prefs: widget.prefs,
                                                currentUserNo:
                                                    widget.currentUserNo,
                                                isSecuritySetupDone: false)
                                            : RecentChats(
                                                prefs: widget.prefs,
                                                currentUserNo:
                                                    widget.currentUserNo,
                                                isSecuritySetupDone: false),
                                        Status(
                                            currentUserFullname: userFullname,
                                            currentUserPhotourl: userPhotourl,
                                            phoneNumberVariants:
                                                this.phoneNumberVariants,
                                            currentUserNo: widget.currentUserNo,
                                            model: _cachedModel,
                                            biometricEnabled: biometricEnabled,
                                            prefs: widget.prefs),
                                      ],
                              )),
                        )));
  }
}

Future<dynamic> myBackgroundMessageHandlerAndroid(RemoteMessage message) async {
  if (message.data['title'] == 'Call Ended' ||
      message.data['title'] == 'Missed Call') {
    flutterLocalNotificationsPlugin..cancelAll();
    final data = message.data;
    final titleMultilang = data['titleMultilang'];
    final bodyMultilang = data['bodyMultilang'];

    await _showNotificationWithDefaultSound(
        'Missed Call', 'You have Missed a Call', titleMultilang, bodyMultilang);
  } else {
    if (message.data['title'] == 'You have new message(s)' ||
        message.data['title'] == 'New message in Group') {
      //-- need not to do anythig for these message type as it will be automatically popped up.

    } else if (message.data['title'] == 'Incoming Audio Call...' ||
        message.data['title'] == 'Incoming Video Call...') {
      final data = message.data;
      final title = data['title'];
      final body = data['body'];
      final titleMultilang = data['titleMultilang'];
      final bodyMultilang = data['bodyMultilang'];

      await _showNotificationWithDefaultSound(
          title, body, titleMultilang, bodyMultilang);
    }
  }

  return Future<void>.value();
}

// Future<dynamic> myBackgroundMessageHandlerIos(RemoteMessage message) async {
//   await Firebase.initializeApp();

//   if (message.data['title'] == 'Call Ended') {
//     final data = message.data;

//     final titleMultilang = data['titleMultilang'];
//     final bodyMultilang = data['bodyMultilang'];
//     flutterLocalNotificationsPlugin..cancelAll();
//     await _showNotificationWithDefaultSound(
//         'Missed Call', 'You have Missed a Call', titleMultilang, bodyMultilang);
//   } else {
//     if (message.data['title'] == 'You have new message(s)') {
//     } else if (message.data['title'] == 'Incoming Audio Call...' ||
//         message.data['title'] == 'Incoming Video Call...') {
//       final data = message.data;
//       final title = data['title'];
//       final body = data['body'];
//       final titleMultilang = data['titleMultilang'];
//       final bodyMultilang = data['bodyMultilang'];
//       await _showNotificationWithDefaultSound(
//           title, body, titleMultilang, bodyMultilang);
//     }
//   }

//   return Future<void>.value();
// }

final FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
    FlutterLocalNotificationsPlugin();
Future _showNotificationWithDefaultSound(String? title, String? message,
    String? titleMultilang, String? bodyMultilang) async {
  if (Platform.isAndroid) {
    flutterLocalNotificationsPlugin.cancelAll();
  }

  var initializationSettingsAndroid =
      new AndroidInitializationSettings('@mipmap/ic_launcher');
  var initializationSettingsIOS = IOSInitializationSettings();
  var initializationSettings = InitializationSettings(
      android: initializationSettingsAndroid, iOS: initializationSettingsIOS);
  flutterLocalNotificationsPlugin.initialize(initializationSettings);
  var androidPlatformChannelSpecifics =
      title == 'Missed Call' || title == 'Call Ended'
          ? local.AndroidNotificationDetails('channel_id', 'channel_name',
              importance: local.Importance.max,
              priority: local.Priority.high,
              sound: RawResourceAndroidNotificationSound('whistle2'),
              playSound: true,
              ongoing: true,
              visibility: NotificationVisibility.public,
              timeoutAfter: 28000)
          : local.AndroidNotificationDetails('channel_id', 'channel_name',
              sound: RawResourceAndroidNotificationSound('ringtone'),
              playSound: true,
              ongoing: true,
              importance: local.Importance.max,
              priority: local.Priority.high,
              visibility: NotificationVisibility.public,
              timeoutAfter: 28000);
  var iOSPlatformChannelSpecifics = local.IOSNotificationDetails(
    presentAlert: true,
    presentBadge: true,
    sound:
        title == 'Missed Call' || title == 'Call Ended' ? '' : 'ringtone.caf',
    presentSound: true,
  );
  var platformChannelSpecifics = local.NotificationDetails(
      android: androidPlatformChannelSpecifics,
      iOS: iOSPlatformChannelSpecifics);
  await flutterLocalNotificationsPlugin
      .show(
    0,
    '$titleMultilang',
    '$bodyMultilang',
    platformChannelSpecifics,
    payload: 'payload',
  )
      .catchError((err) {
    print('ERROR DISPLAYING NOTIFICATION: $err');
  });
}

Widget errorScreen(String? title, String? subtitle) {
  return Scaffold(
    backgroundColor: fiberchatDeepGreen,
    body: Center(
      child: Padding(
        padding: const EdgeInsets.all(30.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error_outline_outlined,
              size: 60,
              color: Colors.yellowAccent,
            ),
            SizedBox(
              height: 30,
            ),
            Text(
              '$title',
              textAlign: TextAlign.center,
              style: TextStyle(
                  fontSize: 20,
                  color: fiberchatWhite,
                  fontWeight: FontWeight.w700),
            ),
            SizedBox(
              height: 20,
            ),
            Text(
              '$subtitle',
              textAlign: TextAlign.center,
              style: TextStyle(
                  fontSize: 17,
                  color: fiberchatWhite.withOpacity(0.7),
                  fontWeight: FontWeight.w400),
            )
          ],
        ),
      ),
    ),
  );
}
