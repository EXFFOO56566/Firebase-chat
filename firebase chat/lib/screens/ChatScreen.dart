import 'dart:io';

import 'package:chat/components/ChatItemWidget.dart';
import 'package:chat/components/Permissions.dart';
import 'package:chat/components/SelectedAttachmentComponent.dart';
import 'package:chat/main.dart';
import 'package:chat/models/ChatMessageModel.dart';
import 'package:chat/models/FileModel.dart';
import 'package:chat/models/UserModel.dart';
import 'package:chat/screens/PickupLayout.dart';
import 'package:chat/screens/UserProfileScreen.dart';
import 'package:chat/services/ChatMessageService.dart';
import 'package:chat/services/UserService.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppCommon.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:chat/utils/AppDataProvider.dart';
import 'package:chat/utils/CallFunctions.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:intl/intl.dart';
import 'package:nb_utils/nb_utils.dart';
import 'package:paginate_firestore/paginate_firestore.dart';

class ChatScreen extends StatefulWidget {
  final UserModel? user;

  ChatScreen(this.user);

  @override
  ChatScreenState createState() => ChatScreenState();
}

class ChatScreenState extends State<ChatScreen> with WidgetsBindingObserver {
  late ChatMessageService chatMessageService;
  String id = '';

  InterstitialAd? myInterstitial;

  var messageCont = TextEditingController();
  var messageFocus = FocusNode();

  UserModel sender = UserModel(
    name: getStringAsync(userDisplayName),
    photoUrl: getStringAsync(userPhotoUrl),
    uid: getStringAsync(userId),
    oneSignalPlayerId: getStringAsync(playerId),
  );

  Position? userLocation;

  @override
  void initState() {
    super.initState();
    init();

    if (mAdShowCount < 5) {
      mAdShowCount++;
    } else {
      mAdShowCount = 0;
      buildInterstitialAd();
    }
  }

  InterstitialAd? buildInterstitialAd() {
    InterstitialAd.load(
        adUnitId: kReleaseMode ? mAdMobInterstitialId : InterstitialAd.testAdUnitId,
        request: AdRequest(),
        adLoadCallback: InterstitialAdLoadCallback(onAdFailedToLoad: (LoadAdError error) {
          throw error.message;
        }, onAdLoaded: (InterstitialAd ad) {
          ad.show();
        }));
  }

  init() async {
    if (appStore.isDarkMode) {
      setStatusBarColor(scaffoldSecondaryDark, statusBarBrightness: Brightness.light, statusBarIconBrightness: Brightness.light);
    } else {
      setStatusBarColor(primaryColor);
    }
    WidgetsBinding.instance!.addObserver(this);
    oneSignal.disablePush(true);

    id = getStringAsync(userId);
    mChatFontSize = getIntAsync(FONT_SIZE_PREF, defaultValue: 16);
    mIsEnterKey = getBoolAsync(IS_ENTER_KEY, defaultValue: false);
    mSelectedImage = getStringAsync(SELECTED_WALLPAPER, defaultValue: "assets/default_wallpaper.png");

    chatMessageService = ChatMessageService();
    chatMessageService.setUnReadStatusToTrue(senderId: sender.uid!, receiverId: widget.user!.uid!);
    setState(() {});
  }

  sendMessage({FilePickerResult? result}) async {
    if (result == null) {
      if (messageCont.text.trim().isEmpty) {
        messageFocus.requestFocus();
        return;
      }
    }
    ChatMessageModel data = ChatMessageModel();
    data.receiverId = widget.user!.uid;
    data.senderId = sender.uid;
    data.message = messageCont.text;
    data.isMessageRead = false;
    data.createdAt = DateTime.now().millisecondsSinceEpoch;

    if (widget.user!.uid == getStringAsync(userId)) {
      //
    }
    if (result != null) {
      if (result.files.single.path.isImage) {
        data.messageType = MessageType.IMAGE.name;
      } else if (result.files.single.path.isVideo) {
        data.messageType = MessageType.VIDEO.name;
      } else if (result.files.single.path.isAudio) {
        data.messageType = MessageType.AUDIO.name;
      } else {
        data.messageType = MessageType.TEXT.name;
      }
    } else {
      data.messageType = MessageType.TEXT.name;
    }

    notificationService.sendPushNotifications(getStringAsync(userDisplayName), messageCont.text, receiverPlayerId: widget.user!.oneSignalPlayerId).catchError(log);
    messageCont.clear();
    setState(() {});

    await chatMessageService.addMessage(data).then((value) async {
      if (result != null) {
        FileModel fileModel = FileModel();
        fileModel.id = value.id;
        fileModel.file = File(result.files.single.path!);
        fileList.add(fileModel);

        setState(() {});
      }

      await chatMessageService.addMessageToDb(value, data, sender, widget.user, image: result != null ? File(result.files.single.path!) : null).then((value) {
        //
      });
    });

    userService.fireStore.collection(USER_COLLECTION).doc(getStringAsync(userId)).collection(CONTACT_COLLECTION).doc(widget.user!.uid).update({'lastMessageTime': DateTime.now().millisecondsSinceEpoch}).catchError((e) {
      log(e);
    });
    userService.fireStore.collection(USER_COLLECTION).doc(widget.user!.uid).collection(CONTACT_COLLECTION).doc(getStringAsync(userId)).update({'lastMessageTime': DateTime.now().millisecondsSinceEpoch}).catchError((e) {
      log(e);
    });
  }

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) async {
    super.didChangeAppLifecycleState(state);

    if (state == AppLifecycleState.detached) {
      oneSignal.disablePush(false);
    }

    if (state == AppLifecycleState.paused) {
      oneSignal.disablePush(false);
    }
    if (state == AppLifecycleState.resumed) {
      oneSignal.disablePush(true);
    }
  }

  @override
  void dispose() async {
    myInterstitial?.show();
    if (appStore.isDarkMode) {
      setStatusBarColor(scaffoldSecondaryDark, statusBarBrightness: Brightness.light, statusBarIconBrightness: Brightness.light);
    } else {
      setStatusBarColor(primaryColor);
    }
    oneSignal.disablePush(false);
    WidgetsBinding.instance!.removeObserver(this);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return PickupLayout(
      child: SafeArea(
        child: Scaffold(
          appBar: AppBar(
            automaticallyImplyLeading: false,
            title: StreamBuilder<UserModel>(
              stream: UserService().singleUser(widget.user!.uid),
              builder: (context, snap) {
                if (snap.hasData) {
                  UserModel data = snap.data!;
                  String time = "";
                  DateTime date = DateTime.fromMicrosecondsSinceEpoch(data.lastSeen.validate() * 1000);
                  if (date.day == DateTime.now().day) {
                    time = "at ${DateFormat('hh:mm a').format(date)}";
                  } else {
                    time = date.timeAgo;
                  }
                  log(time);
                  return Row(
                    children: [
                      Row(
                        children: [
                          Icon(Icons.arrow_back, color: whiteColor),
                          4.width,
                          data.photoUrl!.isEmpty
                              ? Hero(
                                  tag: data.uid.validate(),
                                  child: Image.asset(
                                    "assets/app_icon.png",
                                    height: 35,
                                    width: 35,
                                    fit: BoxFit.cover,
                                  ).cornerRadiusWithClipRRect(50))
                              : Hero(
                                  tag: data.uid!,
                                  child: Image.network(
                                    data.photoUrl.validate(),
                                    height: 35,
                                    width: 35,
                                    fit: BoxFit.cover,
                                  ).cornerRadiusWithClipRRect(50)),
                        ],
                      ).paddingSymmetric(vertical: 16).onTap(() => finish(context)),
                      10.width,
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(data.name!, style: TextStyle(color: whiteColor)),
                          data.isPresence!
                              ? Text('Online', style: secondaryTextStyle(color: Colors.white70))
                              : Text(
                                  "Last seen $time",
                                  style: secondaryTextStyle(color: Colors.white70),
                                ),
                        ],
                      ).paddingSymmetric(vertical: 16).onTap(
                        () {
                          UserProfileScreen(user: data).launch(context);
                        },
                      ).expand(),
                    ],
                  );
                }

                return snapWidgetHelper(snap, loadingWidget: Offstage());
              },
            ),
            actions: [
              IconButton(
                icon: Icon(Icons.video_call),
                onPressed: () async {
                  UserModel sender = UserModel(
                    name: getStringAsync(userDisplayName),
                    photoUrl: getStringAsync(userPhotoUrl),
                    uid: getStringAsync(userId),
                    oneSignalPlayerId: getStringAsync(playerId),
                  );
                  return await Permissions.cameraAndMicrophonePermissionsGranted()
                      ? CallFunctions.dial(
                          context: context,
                          from: sender,
                          to: widget.user!,
                        )
                      : {};
                },
              ),
              IconButton(
                icon: Icon(Icons.call),
                onPressed: () async {
                  UserModel sender = UserModel(
                    name: getStringAsync(userDisplayName),
                    photoUrl: getStringAsync(userPhotoUrl),
                    uid: getStringAsync(userId),
                    oneSignalPlayerId: getStringAsync(playerId),
                  );
                  return await Permissions.cameraAndMicrophonePermissionsGranted()
                      ? CallFunctions.voiceDial(
                          context: context,
                          from: sender,
                          to: widget.user!,
                        )
                      : {};
                },
              ),
              PopupMenuButton(
                padding: EdgeInsets.zero,
                offset: Offset(10, -50),
                icon: Icon(Icons.more_vert),
                color: context.cardColor,
                onSelected: (dynamic value) async {
                  if (value == 1) {
                    UserProfileScreen(user: widget.user).launch(context);
                  } else if (value == 2) {
                    toast(COMING_SOON);
                  } else if (value == 3) {
                    toast(COMING_SOON);
                  } else if (value == 4) {
                    bool? res = await showConfirmDialog(context, "Clear chats", buttonColor: secondaryColor);
                    if (res ?? false) {
                      chatMessageService.clearAllMessages(senderId: sender.uid, receiverId: widget.user!.uid!).then((value) {
                        toast("Chat cleared");
                        hideKeyboard(context);
                      }).catchError((e) {
                        toast(e);
                      });
                    }
                  }
                },
                itemBuilder: (context) => chatScreenPopUpMenuItem,
              ),
            ],
            backgroundColor: context.primaryColor,
          ),
          body: Container(
            height: context.height(),
            width: context.width(),
            child: Stack(
              children: [
                Container(
                  decoration: BoxDecoration(
                    image: DecorationImage(
                      image: Image.asset(mSelectedImage).image,
                      fit: BoxFit.cover,
                      colorFilter: appStore.isDarkMode
                          ? ColorFilter.mode(
                              Colors.black54,
                              BlendMode.luminosity,
                            )
                          : null,
                    ),
                  ),
                  height: context.height(),
                  width: context.width(),
                  child: PaginateFirestore(
                    reverse: true,
                    isLive: true,
                    padding: EdgeInsets.only(left: 8, top: 8, right: 8, bottom: 0),
                    physics: BouncingScrollPhysics(),
                    query: chatMessageService.chatMessagesWithPagination(
                      currentUserId: getStringAsync(userId),
                      receiverUserId: widget.user!.uid!,
                    ),
                    itemsPerPage: PER_PAGE_CHAT_COUNT,
                    shrinkWrap: true,
                    emptyDisplay: Offstage(),
                    itemBuilderType: PaginateBuilderType.listView,
                    itemBuilder: (int, context, snap) {
                      ChatMessageModel data = ChatMessageModel.fromJson(snap.data() as Map<String, dynamic>);
                      data.isMe = data.senderId == id;

                      return ChatItemWidget(data: data);
                    },
                  ).paddingBottom(76),
                ),
                Positioned(
                  bottom: 16,
                  left: 16,
                  right: 16,
                  child: Container(
                    decoration: boxDecorationWithShadow(
                      borderRadius: BorderRadius.circular(30),
                      spreadRadius: 0,
                      blurRadius: 0,
                      backgroundColor: context.cardColor,
                    ),
                    padding: EdgeInsets.only(left: 8, right: 8),
                    child: Row(
                      children: [
                        IconButton(
                          icon: Icon(Icons.attach_file),
                          iconSize: 25.0,
                          color: Colors.grey,
                          onPressed: () {
                            setState(() {});

                            _showAttachmentDialog();
                            hideKeyboard(context);
                          },
                        ),
                        TextField(
                          controller: messageCont,
                          decoration: InputDecoration(
                            border: InputBorder.none,
                            hintText: 'Write a message...',
                            hintStyle: primaryTextStyle(),
                            contentPadding: EdgeInsets.symmetric(vertical: 18),
                          ),
                          cursorColor: appStore.isDarkMode ? Colors.white : Colors.black,
                          focusNode: messageFocus,
                          textCapitalization: TextCapitalization.sentences,
                          keyboardType: TextInputType.multiline,
                          minLines: 1,
                          style: primaryTextStyle(),
                          textInputAction: mIsEnterKey ? TextInputAction.send : TextInputAction.newline,
                          onSubmitted: (s) {
                            sendMessage();
                          },
                          cursorHeight: 20,
                          maxLines: 5,
                        ).expand(),
                        IconButton(
                          icon: Icon(Icons.send, color: primaryColor),
                          onPressed: () {
                            sendMessage();
                          },
                        )
                      ],
                    ),
                    width: context.width(),
                  ),
                )
              ],
            ),
          ).onTap(() {
            hideKeyboard(context);
          }),
        ),
      ),
    );
  }

  _showAttachmentDialog() {
    return showDialog(
      barrierColor: Colors.transparent,
      context: context,
      builder: (context) {
        return Align(
          alignment: Alignment.bottomLeft,
          child: Container(
            padding: EdgeInsets.only(top: 16, bottom: 16, left: 12, right: 12),
            margin: EdgeInsets.only(bottom: 70, left: 12, right: 12),
            decoration: BoxDecoration(
              color: context.scaffoldBackgroundColor,
              borderRadius: BorderRadius.circular(40),
            ),
            child: Material(
              color: context.scaffoldBackgroundColor,
              child: Wrap(
                spacing: 16,
                runSpacing: 16,
                children: [
                  Container(
                    width: context.width() / 3 - 32,
                    color: context.scaffoldBackgroundColor,
                    child: Column(
                      children: [
                        CircleAvatar(child: Icon(Icons.panorama, size: 30, color: Colors.white), backgroundColor: Colors.purple, radius: 30),
                        8.height,
                        Text("Gallery", style: boldTextStyle()),
                      ],
                    ),
                  ).onTap(() async {
                    FilePickerResult? result = await FilePicker.platform.pickFiles(type: FileType.image);
                    if (result != null) {
                      File file = File(result.files.single.path!);
                      finish(context);
                      bool res = await (SelectedAttachmentComponent(file: file, userModel: widget.user).launch(context));
                      if (res) {
                        sendMessage(result: result);
                      }
                    } else {
                      // User canceled the picker
                    }
                  }),
                  Container(
                    width: context.width() / 3 - 32,
                    color: context.scaffoldBackgroundColor,
                    child: Column(
                      children: [
                        CircleAvatar(child: Icon(Icons.videocam, size: 30, color: Colors.white), backgroundColor: Colors.pink[800], radius: 30),
                        8.height,
                        Text("Video", style: boldTextStyle()),
                      ],
                    ),
                  ).onTap(() async {
                    FilePickerResult? result = await FilePicker.platform.pickFiles(type: FileType.video);
                    if (result != null) {
                      File file = File(result.files.single.path!);
                      finish(context);
                      bool res = await (SelectedAttachmentComponent(file: file, userModel: widget.user, isVideo: true).launch(context));
                      if (res) {
                        sendMessage(result: result);
                      }
                    } else {
                      // User canceled the picker
                    }
                  }),
                  Container(
                    width: context.width() / 3 - 32,
                    color: context.scaffoldBackgroundColor,
                    child: Column(
                      children: [
                        CircleAvatar(child: Icon(Icons.videocam, size: 30, color: Colors.white), backgroundColor: Colors.green[800], radius: 30),
                        8.height,
                        Text("Audio", style: boldTextStyle()),
                      ],
                    ),
                  ).onTap(() async {
                    FilePickerResult? result = await FilePicker.platform.pickFiles(type: FileType.audio);
                    if (result != null) {
                      File file = File(result.files.single.path!);
                      finish(context);
                      bool res = await (SelectedAttachmentComponent(file: file, userModel: widget.user, isAudio: true).launch(context));
                      if (res) {
                        sendMessage(result: result);
                      }
                    } else {
                      // User canceled the picker
                    }
                  }),
                  Container(
                    width: context.width() / 3 - 32,
                    color: context.scaffoldBackgroundColor,
                    child: Column(
                      children: [
                        CircleAvatar(child: Icon(Icons.location_on, size: 30, color: Colors.white), backgroundColor: Colors.green[700], radius: 30),
                        8.height,
                        Text("location", style: boldTextStyle()),
                      ],
                    ),
                  ).onTap(
                    () async {
                      toast(COMING_SOON);
                      return;
                      LocationPermission permission = await Geolocator.requestPermission();
                      if (permission == LocationPermission.always || permission == LocationPermission.whileInUse) {
                        Position? position = await Geolocator.getLastKnownPosition();
                        if (position == null) {
                          position = await Geolocator.getCurrentPosition();
                        }
                        launchUrl("https://www.google.com/maps/search/?api=1&query=${position.latitude},${position.longitude}");
                      }
                    },
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}
