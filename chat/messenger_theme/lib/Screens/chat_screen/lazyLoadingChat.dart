//*************   © Copyrighted by Thinkcreative_Technologies. An Exclusive item of Envato market. Make sure you have purchased a Regular License OR Extended license for the Source Code from Envato to use this product. See the License Defination attached with source code. *********************

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:emoji_picker_flutter/emoji_picker_flutter.dart';
import 'package:fiberchat/Configs/app_constants.dart';
import 'package:fiberchat/Configs/optional_constants.dart';
import 'package:fiberchat/Screens/auth_screens/login.dart';
import 'package:fiberchat/Screens/calling_screen/pickup_layout.dart';
import 'package:fiberchat/Screens/chat_screen/Widget/bubble.dart';
import 'package:fiberchat/Screens/chat_screen/chat.dart';
import 'package:fiberchat/Screens/chat_screen/utils/aes_encryption.dart';
import 'package:fiberchat/Screens/chat_screen/utils/audioPlayback.dart';
import 'package:fiberchat/Screens/chat_screen/utils/deleteChatMedia.dart';
import 'package:fiberchat/Screens/chat_screen/utils/photo_view.dart';
import 'package:fiberchat/Screens/chat_screen/utils/uploadMediaWithProgress.dart';
import 'package:fiberchat/Screens/contact_screens/SelectContactsToForward.dart';
import 'package:fiberchat/Screens/profile_settings/profile_view.dart';
import 'package:fiberchat/Screens/security_screens/security.dart';
import 'package:fiberchat/Services/Admob/admob.dart';
import 'package:fiberchat/Services/Providers/AvailableContactsProvider.dart';
import 'package:fiberchat/Services/Providers/GroupChatProvider.dart';
import 'package:fiberchat/Services/Providers/LazyLoadingChatProvider.dart';
import 'package:fiberchat/Services/Providers/Observer.dart';
import 'package:fiberchat/Services/Providers/seen_provider.dart';
import 'package:fiberchat/Services/Providers/seen_state.dart';
import 'package:fiberchat/Services/localization/language_constants.dart';
import 'package:fiberchat/Utils/call_utilities.dart';
import 'package:fiberchat/Utils/chat_controller.dart';
import 'package:fiberchat/Utils/crc.dart';
import 'package:fiberchat/Utils/custom_url_launcher.dart';
import 'package:fiberchat/Utils/open_settings.dart';
import 'package:fiberchat/Utils/permissions.dart';
import 'package:fiberchat/Utils/utils.dart';
import 'package:fiberchat/widgets/CountryPicker/CountryCode.dart';
import 'package:fiberchat/widgets/DownloadManager/download_all_file_type.dart';
import 'package:fiberchat/widgets/ImagePicker/image_picker.dart';
import 'package:fiberchat/widgets/InfiniteList/InfiniteCOLLECTIONListViewWidget.dart';
import 'package:fiberchat/widgets/MultiDocumentPicker/multiDocumentPicker.dart';
import 'package:fiberchat/widgets/MultiImagePicker/multiImagePicker.dart';
import 'package:fiberchat/widgets/MyElevatedButton/MyElevatedButton.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:intl/intl.dart';
import 'package:media_info/media_info.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import 'package:provider/provider.dart';
import 'dart:async';
import 'package:emoji_picker_flutter/emoji_picker_flutter.dart' as emojipic;
import 'dart:convert';
import 'dart:io';
import 'package:fiberchat/Configs/Dbkeys.dart';
import 'package:fiberchat/Configs/Dbpaths.dart';
import 'package:fiberchat/Screens/privacypolicy&TnC/PdfViewFromCachedUrl.dart';
import 'package:fiberchat/widgets/SoundPlayer/SoundPlayerPro.dart';
import 'package:fiberchat/Services/Providers/currentchat_peer.dart';
import 'package:fiberchat/Screens/call_history/callhistory.dart';
import 'package:fiberchat/Screens/contact_screens/ContactsSelect.dart';
import 'package:fiberchat/Models/DataModel.dart';
import 'package:fiberchat/Utils/save.dart';
import 'package:fiberchat/widgets/AudioRecorder/Audiorecord.dart';
import 'package:fiberchat/widgets/VideoPicker/VideoPicker.dart';
import 'package:fiberchat/widgets/VideoPicker/VideoPreview.dart';
import 'package:flutter_linkify/flutter_linkify.dart';
import 'package:geolocator/geolocator.dart';
import 'package:giphy_get/giphy_get.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:receive_sharing_intent/receive_sharing_intent.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:video_thumbnail/video_thumbnail.dart';
import 'package:fiberchat/Configs/Enum.dart';
import 'package:fiberchat/Utils/unawaited.dart';
import 'package:fiberchat/Models/E2EE/e2ee.dart' as e2ee;
import 'package:encrypt/encrypt.dart' as encrypt;

class LazyLoadingChat extends StatefulWidget {
  final String? peerNo, currentUserNo;
  final DataModel model;
  final int unread;
  final SharedPreferences prefs;
  final List<SharedMediaFile>? sharedFiles;
  final MessageType? sharedFilestype;
  final bool isSharingIntentForwarded;
  final String? sharedText;
  LazyLoadingChat({
    Key? key,
    required this.currentUserNo,
    required this.peerNo,
    required this.model,
    required this.prefs,
    required this.unread,
    required this.isSharingIntentForwarded,
    this.sharedFiles,
    this.sharedFilestype,
    this.sharedText,
  });

  @override
  _LazyLoadingChatState createState() => _LazyLoadingChatState();
}

class _LazyLoadingChatState extends State<LazyLoadingChat>
    with WidgetsBindingObserver {
  String? chatId;
  bool isMessageLoading = true;
  bool typing = false;
  late File thumbnailFile;
  File? pickedFile;
  bool isLoading = true;
  bool isgeneratingSomethingLoader = false;
  int tempSendIndex = 0;
  String? imageUrl;
  SeenState? seenState;
  // List<Message> messages = new List.from(<Message>[]);

  int? uploadTimestamp;

  StreamSubscription? seenSubscription,
      msgSubscription,
      deleteUptoSubscription,
      chatStatusSubscriptionForPeer;

  final TextEditingController textEditingController =
      new TextEditingController();
  final TextEditingController reportEditingController =
      new TextEditingController();
  final ScrollController realtime = new ScrollController();
  final ScrollController saved = new ScrollController();
  late DataModel _cachedModel;

  Duration? duration;
  Duration? position;

  String? localFilePath;

  PlayerState playerState = PlayerState.stopped;

  get isPlaying => playerState == PlayerState.playing;
  get isPaused => playerState == PlayerState.paused;

  get durationText =>
      duration != null ? duration.toString().split('.').first : '';

  get positionText =>
      position != null ? position.toString().split('.').first : '';

  bool isMuted = false;
  void setStateIfMounted(f) {
    if (mounted) setState(f);
  }

  InterstitialAd? _interstitialAd;
  int _numInterstitialLoadAttempts = 0;
  RewardedAd? _rewardedAd;
  int _numRewardedLoadAttempts = 0;
  late String messageReplyOwnerName;
  late Stream<QuerySnapshot> groupChatMessages;
  late Query firestoreChatquery;
  GlobalKey<ScaffoldState> _scaffold = new GlobalKey<ScaffoldState>();
  GlobalKey<State> _keyLoader =
      new GlobalKey<State>(debugLabel: 'qqqeqeqsssaadqeqe');
  Map<String, dynamic>? replyDoc;
  bool isReplyKeyboard = false;

  String? peerAvatar, privateKey, sharedSecret;
  late bool locked, hidden;
  Map<String, dynamic>? peer, currentUser;
  int? chatStatus, unread;

  updateLocalUserData(model) {
    peer = model.userData[widget.peerNo];
    currentUser = _cachedModel.currentUser;
    if (currentUser != null && peer != null) {
      hidden = currentUser![Dbkeys.hidden] != null &&
          currentUser![Dbkeys.hidden].contains(widget.peerNo);
      locked = currentUser![Dbkeys.locked] != null &&
          currentUser![Dbkeys.locked].contains(widget.peerNo);
      chatStatus = peer![Dbkeys.chatStatus];
      peerAvatar = peer![Dbkeys.photoUrl];
    }
  }

  @override
  void initState() {
    super.initState();
    readLocal(this.context);
    _cachedModel = widget.model;
    unread = widget.unread;
    // initAudioPlayer();
    // _load();
    Fiberchat.internetLookUp();
    listenToBlock();
    updateLocalUserData(_cachedModel);
    chatId = Fiberchat.getChatId(widget.currentUserNo, widget.peerNo);
    print('CHAT ID IS $chatId');
    firestoreChatquery = FirebaseFirestore.instance
        .collection(DbPaths.collectionmessages)
        .doc(chatId!)
        .collection(chatId!)
        .orderBy(Dbkeys.timestamp, descending: true)
        .limit(maxChatMessageDocsLoadAtOnceForGroupChatAndBroadcastLazyLoading);
    WidgetsBinding.instance.addPostFrameCallback((timeStamp) {
      final observer = Provider.of<Observer>(this.context, listen: false);
      var currentpeer =
          Provider.of<CurrentChatPeer>(this.context, listen: false);
      currentpeer.setpeer(newpeerid: widget.peerNo);
      seenState = new SeenState(false);
      WidgetsBinding.instance.addObserver(this);
      var firestoreProvider =
          Provider.of<FirestoreDataProviderMESSAGESforLAZYLOADINGCHAT>(
              this.context,
              listen: false);

      firestoreProvider.reset();
      Future.delayed(const Duration(milliseconds: 1700), () {
        loadMessagesAndListen();
        Future.delayed(const Duration(milliseconds: 3000), () {
          if (IsVideoAdShow == true && observer.isadmobshow == true) {
            _createRewardedAd();
          }

          if (IsInterstitialAdShow == true && observer.isadmobshow == true) {
            _createInterstitialAd();
          }
        });
      });

      // unread = widget.unread;
      // isLoading = false;
      // imageUrl = '';
      // listenToBlock();
      // loadSavedMessages();
      // readLocal(this.context);
      Future.delayed(const Duration(milliseconds: 3000), () {
        if (IsVideoAdShow == true && observer.isadmobshow == true) {
          _createRewardedAd();
        }

        if (IsInterstitialAdShow == true && observer.isadmobshow == true) {
          _createInterstitialAd();
        }
      });
    });
    isLoading = false;
  }

  // ignore: cancel_subscriptions
  StreamSubscription<QuerySnapshot>? subscription;
  loadMessagesAndListen() async {
    subscription = firestoreChatquery.snapshots().listen((snapshot) {
      snapshot.docChanges.forEach((change) {
        if (change.type == DocumentChangeType.added) {
          var chatprovider =
              Provider.of<FirestoreDataProviderMESSAGESforLAZYLOADINGCHAT>(
                  this.context,
                  listen: false);
          DocumentSnapshot newDoc = change.doc;
          // if (chatprovider.datalistSnapshot.length == 0) {
          // } else if ((chatprovider.checkIfDocAlreadyExits(
          //       newDoc: newDoc,
          //     ) ==
          //     false)) {

          // if (newDoc[Dbkeys.groupmsgSENDBY] != widget.currentUserno) {
          chatprovider.addDoc(newDoc);
          // unawaited(realtime.animateTo(0.0,
          //     duration: Duration(milliseconds: 300), curve: Curves.easeOut));
          // }
          // }
        } else if (change.type == DocumentChangeType.modified) {
          var chatprovider =
              Provider.of<FirestoreDataProviderMESSAGESforLAZYLOADINGCHAT>(
                  this.context,
                  listen: false);
          DocumentSnapshot updatedDoc = change.doc;
          if (chatprovider.checkIfDocAlreadyExits(
                  newDoc: updatedDoc,
                  timestamp: updatedDoc[Dbkeys.timestamp]) ==
              true) {
            chatprovider.updateparticulardocinProvider(updatedDoc: updatedDoc);
          }
        } else if (change.type == DocumentChangeType.removed) {
          var chatprovider =
              Provider.of<FirestoreDataProviderMESSAGESforLAZYLOADINGCHAT>(
                  this.context,
                  listen: false);
          DocumentSnapshot deletedDoc = change.doc;
          if (chatprovider.checkIfDocAlreadyExits(
                  newDoc: deletedDoc,
                  timestamp: deletedDoc[Dbkeys.timestamp]) ==
              true) {
            chatprovider.deleteparticulardocinProvider(deletedDoc: deletedDoc);
          }
        }
      });
    });

    setStateIfMounted(() {});

//       //----sharing intent action:
    if (widget.isSharingIntentForwarded == true) {
      if (widget.sharedText != null) {
        onSendMessage(this.context, widget.sharedText!, MessageType.text,
            DateTime.now().millisecondsSinceEpoch);
      } else if (widget.sharedFiles != null) {
        setStateIfMounted(() {
          isgeneratingSomethingLoader = true;
        });
        uploadEach(0);
      }
    }
  }

  int currentUploadingIndex = 0;
  uploadEach(
    int index,
  ) async {
    File file = new File(widget.sharedFiles![index].path);
    String fileName = file.path.split('/').last.toLowerCase();

    print(fileName);
    if (index > widget.sharedFiles!.length) {
      setStateIfMounted(() {
        isgeneratingSomethingLoader = false;
      });
    } else {
      int messagetime = DateTime.now().millisecondsSinceEpoch;
      setState(() {
        currentUploadingIndex = index;
      });
      await getFileData(File(widget.sharedFiles![index].path),
              timestamp: messagetime, totalFiles: widget.sharedFiles!.length)
          .then((imageUrl) async {
        if (imageUrl != null) {
          MessageType type = fileName.contains('.png') ||
                  fileName.contains('.gif') ||
                  fileName.contains('.jpg') ||
                  fileName.contains('.jpeg') ||
                  fileName.contains('giphy')
              ? MessageType.image
              : fileName.contains('.mp4') || fileName.contains('.mov')
                  ? MessageType.video
                  : fileName.contains('.mp3') || fileName.contains('.aac')
                      ? MessageType.audio
                      : MessageType.doc;
          String? thumbnailurl;
          if (type == MessageType.video) {
            thumbnailurl = await getThumbnail(imageUrl);

            setStateIfMounted(() {});
          }

          String finalUrl = fileName.contains('.png') ||
                  fileName.contains('.gif') ||
                  fileName.contains('.jpg') ||
                  fileName.contains('.jpeg') ||
                  fileName.contains('giphy')
              ? imageUrl
              : fileName.contains('.mp4') || fileName.contains('.mov')
                  ? imageUrl +
                      '-BREAK-' +
                      thumbnailurl +
                      '-BREAK-' +
                      videometadata
                  : fileName.contains('.mp3') || fileName.contains('.aac')
                      ? imageUrl + '-BREAK-' + uploadTimestamp.toString()
                      : imageUrl +
                          '-BREAK-' +
                          basename(pickedFile!.path).toString();
          onSendMessage(this.context, finalUrl, type, messagetime);
        }
      }).then((value) {
        if (widget.sharedFiles!.last == widget.sharedFiles![index]) {
          setStateIfMounted(() {
            isgeneratingSomethingLoader = false;
          });
        } else {
          uploadEach(currentUploadingIndex + 1);
        }
      });
    }
  }

  void onSendMessage(
      BuildContext context, String content, MessageType type, int? timestamp,
      {bool isForward = false}) async {
    final observer = Provider.of<Observer>(this.context, listen: false);
    if (content.trim() != '') {
      try {
        content = content.trim();
        if (chatStatus == null || chatStatus == 4)
          ChatController.request(widget.currentUserNo, widget.peerNo, chatId);
        textEditingController.clear();
        // final encrypted = encryptWithCRC(content);
        final encrypted = AESEncryptData.encryptAES(content, sharedSecret);
        if (encrypted is String) {
          Future messaging = FirebaseFirestore.instance
              .collection(DbPaths.collectionmessages)
              .doc(chatId)
              .collection(chatId!)
              .doc('$timestamp')
              .set({
            Dbkeys.from: widget.currentUserNo,
            Dbkeys.to: widget.peerNo,
            Dbkeys.timestamp: timestamp,
            Dbkeys.content: encrypted,
            Dbkeys.messageType: type.index,
            Dbkeys.hasSenderDeleted: false,
            Dbkeys.hasRecipientDeleted: false,
            Dbkeys.sendername: _cachedModel.currentUser![Dbkeys.nickname],
            Dbkeys.isReply: isReplyKeyboard,
            Dbkeys.replyToMsgDoc: replyDoc,
            Dbkeys.isForward: isForward
          }, SetOptions(merge: true));

          _cachedModel.addMessage(widget.peerNo, timestamp, messaging);
          // var tempDoc = {
          //   Dbkeys.timestamp: timestamp,
          //   Dbkeys.to: widget.peerNo,
          //   Dbkeys.messageType: type.index,
          //   Dbkeys.content: content,
          //   Dbkeys.from: widget.currentUserNo,
          //   Dbkeys.hasSenderDeleted: false,
          //   Dbkeys.hasRecipientDeleted: false,
          //   Dbkeys.sendername: _cachedModel.currentUser![Dbkeys.nickname],
          //   Dbkeys.isReply: isReplyKeyboard,
          //   Dbkeys.replyToMsgDoc: replyDoc,
          //   Dbkeys.isForward: isForward
          // };

          setStateIfMounted(() {
            isReplyKeyboard = false;
            replyDoc = null;
            // messages = List.from(messages)
            //   ..add(Message(
            //     buildTempMessage(
            //         context, type, content, timestamp, messaging, tempDoc),
            //     onTap: (tempDoc[Dbkeys.from] == widget.currentUserNo &&
            //                 tempDoc[Dbkeys.hasSenderDeleted] == true) ==
            //             true
            //         ? () {}
            //         : type == MessageType.image
            //             ? () {
            //                 Navigator.push(
            //                     context,
            //                     MaterialPageRoute(
            //                       builder: (context) => PhotoViewWrapper(
            //                         message: content,
            //                         tag: timestamp.toString(),
            //                         imageProvider:
            //                             CachedNetworkImageProvider(content),
            //                       ),
            //                     ));
            //               }
            //             : null,
            //     onDismiss: tempDoc[Dbkeys.content] == '' ||
            //             tempDoc[Dbkeys.content] == null
            //         ? () {}
            //         : () {
            //             setStateIfMounted(() {
            //               isReplyKeyboard = true;
            //               replyDoc = tempDoc;
            //             });
            //             HapticFeedback.heavyImpact();
            //             keyboardFocusNode.requestFocus();
            //           },
            //     onDoubleTap: () {
            //       // save(tempDoc);
            //     },
            //     onLongPress: () {
            //       if (tempDoc.containsKey(Dbkeys.hasRecipientDeleted) &&
            //           tempDoc.containsKey(Dbkeys.hasSenderDeleted)) {
            //         if ((tempDoc[Dbkeys.from] == widget.currentUserNo &&
            //                 tempDoc[Dbkeys.hasSenderDeleted] == true) ==
            //             false) {
            //           //--Show Menu only if message is not deleted by current user already
            //           contextMenuNew(this.context, tempDoc, true);
            //         }
            //       } else {
            //         contextMenuOld(context, tempDoc);
            //       }
            //     },
            //     from: widgetcurrentUserNo,
            //     timestamp: timestamp,
            //   ));
          });

          unawaited(realtime.animateTo(0.0,
              duration: Duration(milliseconds: 300), curve: Curves.easeOut));

          if (type == MessageType.doc ||
              type == MessageType.audio ||
              // (type == MessageType.image && !content.contains('giphy')) ||
              type == MessageType.location ||
              type == MessageType.contact &&
                  widget.isSharingIntentForwarded == false) {
            if (IsVideoAdShow == true &&
                observer.isadmobshow == true &&
                IsInterstitialAdShow == false) {
              Future.delayed(const Duration(milliseconds: 800), () {
                _showRewardedAd();
              });
            } else if (IsInterstitialAdShow == true &&
                observer.isadmobshow == true) {
              _showInterstitialAd();
            }
          } else if (type == MessageType.video) {
            if (IsVideoAdShow == true && observer.isadmobshow == true) {
              Future.delayed(const Duration(milliseconds: 800), () {
                _showRewardedAd();
              });
            }
          }
          // _playPopSound();
        } else {
          Fiberchat.toast('Nothing to encrypt');
        }
      } on Exception catch (_) {
        print('Exception caught!');
      }
    }
  }

  void setLastSeen() async {
    if (chatStatus != ChatStatus.blocked.index) {
      if (chatId != null) {
        await FirebaseFirestore.instance
            .collection(DbPaths.collectionmessages)
            .doc(chatId)
            .update(
          {'${widget.currentUserNo}': DateTime.now().millisecondsSinceEpoch},
        );
      }
    }
  }

  @override
  void dispose() {
    super.dispose();
    WidgetsBinding.instance.removeObserver(this);
    setLastSeen();
    // audioPlayer.stop();
    msgSubscription?.cancel();

    chatStatusSubscriptionForPeer?.cancel();
    seenSubscription?.cancel();
    deleteUptoSubscription?.cancel();
    if (IsInterstitialAdShow == true) {
      _interstitialAd!.dispose();
    }
    if (IsVideoAdShow == true) {
      _rewardedAd!.dispose();
    }
  }

  getFileData(File image, {int? timestamp, int? totalFiles}) {
    final observer = Provider.of<Observer>(this.context, listen: false);

    setStateIfMounted(() {
      pickedFile = image;
    });

    return observer.isPercentProgressShowWhileUploading
        ? (totalFiles == null
            ? uploadFileWithProgressIndicator(
                false,
                timestamp: timestamp,
              )
            : totalFiles == 1
                ? uploadFileWithProgressIndicator(
                    false,
                    timestamp: timestamp,
                  )
                : uploadFile(false, timestamp: timestamp))
        : uploadFile(false, timestamp: timestamp);
  }

  getFileName(groupid, timestamp) {
    return "${widget.currentUserNo}-$timestamp";
  }

  getThumbnail(String url) async {
    final observer = Provider.of<Observer>(this.context, listen: false);
    setStateIfMounted(() {
      isgeneratingSomethingLoader = true;
    });
    String? path = await VideoThumbnail.thumbnailFile(
        video: url,
        thumbnailPath: (await getTemporaryDirectory()).path,
        imageFormat: ImageFormat.PNG,
        quality: 20);
    thumbnailFile = File(path!);
    setStateIfMounted(() {
      isgeneratingSomethingLoader = false;
    });
    return observer.isPercentProgressShowWhileUploading
        ? uploadFileWithProgressIndicator(true)
        : uploadFile(true);
  }

  String? videometadata;
  int? thumnailtimestamp;

  Future uploadFile(bool isthumbnail, {int? timestamp}) async {
    uploadTimestamp = timestamp ?? DateTime.now().millisecondsSinceEpoch;
    String fileName = getFileName(
        widget.currentUserNo,
        isthumbnail == false
            ? '$uploadTimestamp'
            : '${thumnailtimestamp}Thumbnail');
    Reference reference =
        FirebaseStorage.instance.ref("+00_CHAT_MEDIA/$chatId/").child(fileName);
    TaskSnapshot uploading = await reference
        .putFile(isthumbnail == true ? thumbnailFile : pickedFile!);
    if (isthumbnail == false) {
      setStateIfMounted(() {
        thumnailtimestamp = uploadTimestamp;
      });
    }
    if (isthumbnail == true) {
      MediaInfo _mediaInfo = MediaInfo();

      await _mediaInfo.getMediaInfo(thumbnailFile.path).then((mediaInfo) {
        setStateIfMounted(() {
          videometadata = jsonEncode({
            "width": mediaInfo['width'],
            "height": mediaInfo['height'],
            "orientation": null,
            "duration": mediaInfo['durationMs'],
            "filesize": null,
            "author": null,
            "date": null,
            "framerate": null,
            "location": null,
            "path": null,
            "title": '',
            "mimetype": mediaInfo['mimeType'],
          }).toString();
        });
      }).catchError((onError) {
        Fiberchat.toast('Sending failed !');
        print('ERROR SENDING FILE: $onError');
      });
    } else {
      FirebaseFirestore.instance
          .collection(DbPaths.collectionusers)
          .doc(widget.currentUserNo)
          .set({
        Dbkeys.mssgSent: FieldValue.increment(1),
      }, SetOptions(merge: true));
      FirebaseFirestore.instance
          .collection(DbPaths.collectiondashboard)
          .doc(DbPaths.docchatdata)
          .set({
        Dbkeys.mediamessagessent: FieldValue.increment(1),
      }, SetOptions(merge: true));
    }

    return uploading.ref.getDownloadURL();
  }

  Future uploadFileWithProgressIndicator(
    bool isthumbnail, {
    int? timestamp,
  }) async {
    uploadTimestamp = timestamp ?? DateTime.now().millisecondsSinceEpoch;

    String fileName = getFileName(
        widget.currentUserNo,
        isthumbnail == false
            ? '$uploadTimestamp'
            : '${thumnailtimestamp}Thumbnail');
    Reference reference =
        FirebaseStorage.instance.ref("+00_CHAT_MEDIA/$chatId/").child(fileName);
    UploadTask uploading =
        reference.putFile(isthumbnail == true ? thumbnailFile : pickedFile!);

    showDialog<void>(
        context: this.context,
        barrierDismissible: false,
        builder: (BuildContext context) {
          return new WillPopScope(
              onWillPop: () async => false,
              child: SimpleDialog(
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(7),
                  ),
                  // side: BorderSide(width: 5, color: Colors.green)),
                  key: _keyLoader,
                  backgroundColor: Colors.white,
                  children: <Widget>[
                    Center(
                      child: StreamBuilder(
                          stream: uploading.snapshotEvents,
                          builder: (BuildContext context, snapshot) {
                            if (snapshot.hasData) {
                              final TaskSnapshot snap = uploading.snapshot;

                              return openUploadDialog(
                                context: context,
                                percent: bytesTransferred(snap) / 100,
                                title: isthumbnail == true
                                    ? getTranslated(
                                        context, 'generatingthumbnail')
                                    : getTranslated(context, 'sending'),
                                subtitle:
                                    "${((((snap.bytesTransferred / 1024) / 1000) * 100).roundToDouble()) / 100}/${((((snap.totalBytes / 1024) / 1000) * 100).roundToDouble()) / 100} MB",
                              );
                            } else {
                              return openUploadDialog(
                                context: context,
                                percent: 0.0,
                                title: isthumbnail == true
                                    ? getTranslated(
                                        context, 'generatingthumbnail')
                                    : getTranslated(context, 'sending'),
                                subtitle: '',
                              );
                            }
                          }),
                    ),
                  ]));
        });

    TaskSnapshot downloadTask = await uploading;
    String downloadedurl = await downloadTask.ref.getDownloadURL();

    if (isthumbnail == false) {
      setStateIfMounted(() {
        thumnailtimestamp = uploadTimestamp;
      });
    }
    if (isthumbnail == true) {
      MediaInfo _mediaInfo = MediaInfo();

      await _mediaInfo.getMediaInfo(thumbnailFile.path).then((mediaInfo) {
        setStateIfMounted(() {
          videometadata = jsonEncode({
            "width": mediaInfo['width'],
            "height": mediaInfo['height'],
            "orientation": null,
            "duration": mediaInfo['durationMs'],
            "filesize": null,
            "author": null,
            "date": null,
            "framerate": null,
            "location": null,
            "path": null,
            "title": '',
            "mimetype": mediaInfo['mimeType'],
          }).toString();
        });
      }).catchError((onError) {
        Fiberchat.toast('Sending failed !');
        print('ERROR SENDING FILE: $onError');
      });
    } else {
      FirebaseFirestore.instance
          .collection(DbPaths.collectionusers)
          .doc(widget.currentUserNo)
          .set({
        Dbkeys.mssgSent: FieldValue.increment(1),
      }, SetOptions(merge: true));
      FirebaseFirestore.instance
          .collection(DbPaths.collectiondashboard)
          .doc(DbPaths.docchatdata)
          .set({
        Dbkeys.mediamessagessent: FieldValue.increment(1),
      }, SetOptions(merge: true));
    }
    Navigator.of(_keyLoader.currentContext!, rootNavigator: true).pop(); //
    return downloadedurl;
  }

  getWallpaper(File image) {
    // ignore: unnecessary_null_comparison
    if (image != null) {
      _cachedModel.setWallpaper(widget.peerNo, image);
    }
    return Future.value(false);
  }

  bool hasPeerBlockedMe = false;
  listenToBlock() {
    chatStatusSubscriptionForPeer = FirebaseFirestore.instance
        .collection(DbPaths.collectionusers)
        .doc(widget.peerNo)
        .collection(Dbkeys.chatsWith)
        .doc(Dbkeys.chatsWith)
        .snapshots()
        .listen((doc) {
      if (doc.data()!.containsKey(widget.currentUserNo)) {
        print('CHANGED');
        if (doc.data()![widget.currentUserNo] == 0) {
          hasPeerBlockedMe = true;
          setStateIfMounted(() {});
        } else if (doc.data()![widget.currentUserNo] == 3) {
          hasPeerBlockedMe = false;
          setStateIfMounted(() {});
        }
      }
    });
  }

  void _createInterstitialAd() {
    InterstitialAd.load(
        adUnitId: getInterstitialAdUnitId()!,
        request: AdRequest(
          nonPersonalizedAds: true,
        ),
        adLoadCallback: InterstitialAdLoadCallback(
          onAdLoaded: (InterstitialAd ad) {
            print('$ad loaded');
            _interstitialAd = ad;
            _numInterstitialLoadAttempts = 0;
            _interstitialAd!.setImmersiveMode(true);
          },
          onAdFailedToLoad: (LoadAdError error) {
            print('InterstitialAd failed to load: $error.');
            _numInterstitialLoadAttempts += 1;
            _interstitialAd = null;
            if (_numInterstitialLoadAttempts <= maxAdFailedLoadAttempts) {
              _createInterstitialAd();
            }
          },
        ));
  }

  void _showInterstitialAd() {
    if (_interstitialAd == null) {
      print('Warning: attempt to show interstitial before loaded.');
      return;
    }
    _interstitialAd!.fullScreenContentCallback = FullScreenContentCallback(
      onAdShowedFullScreenContent: (InterstitialAd ad) =>
          print('ad onAdShowedFullScreenContent.'),
      onAdDismissedFullScreenContent: (InterstitialAd ad) {
        print('$ad onAdDismissedFullScreenContent.');
        ad.dispose();
        _createInterstitialAd();
      },
      onAdFailedToShowFullScreenContent: (InterstitialAd ad, AdError error) {
        print('$ad onAdFailedToShowFullScreenContent: $error');
        ad.dispose();
        _createInterstitialAd();
      },
    );
    _interstitialAd!.show();
    _interstitialAd = null;
  }

  void _createRewardedAd() {
    RewardedAd.load(
        adUnitId: getRewardBasedVideoAdUnitId()!,
        request: AdRequest(
          nonPersonalizedAds: true,
        ),
        rewardedAdLoadCallback: RewardedAdLoadCallback(
          onAdLoaded: (RewardedAd ad) {
            print('$ad loaded.');
            _rewardedAd = ad;
            _numRewardedLoadAttempts = 0;
          },
          onAdFailedToLoad: (LoadAdError error) {
            print('RewardedAd failed to load: $error');
            _rewardedAd = null;
            _numRewardedLoadAttempts += 1;
            if (_numRewardedLoadAttempts <= maxAdFailedLoadAttempts) {
              _createRewardedAd();
            }
          },
        ));
  }

  void _showRewardedAd() {
    if (_rewardedAd == null) {
      print('Warning: attempt to show rewarded before loaded.');
      return;
    }
    _rewardedAd!.fullScreenContentCallback = FullScreenContentCallback(
      onAdShowedFullScreenContent: (RewardedAd ad) =>
          print('ad onAdShowedFullScreenContent.'),
      onAdDismissedFullScreenContent: (RewardedAd ad) {
        print('$ad onAdDismissedFullScreenContent.');
        ad.dispose();
        _createRewardedAd();
      },
      onAdFailedToShowFullScreenContent: (RewardedAd ad, AdError error) {
        print('$ad onAdFailedToShowFullScreenContent: $error');
        ad.dispose();
        _createRewardedAd();
      },
    );

    _rewardedAd!.setImmersiveMode(true);
    _rewardedAd!.show(onUserEarnedReward: (a, b) {});
    _rewardedAd = null;
  }

  _onEmojiSelected(Emoji emoji) {
    textEditingController
      ..text += emoji.emoji
      ..selection = TextSelection.fromPosition(
          TextPosition(offset: textEditingController.text.length));
    setStateIfMounted(() {});
    if (textEditingController.text.isNotEmpty &&
        textEditingController.text.length == 1) {
      setStateIfMounted(() {});
    }
    if (textEditingController.text.isEmpty) {
      setStateIfMounted(() {});
    }
  }

  _onBackspacePressed() {
    textEditingController
      ..text = textEditingController.text.characters.skipLast(1).toString()
      ..selection = TextSelection.fromPosition(
          TextPosition(offset: textEditingController.text.length));
    if (textEditingController.text.isNotEmpty &&
        textEditingController.text.length == 1) {
      setStateIfMounted(() {});
    }
    if (textEditingController.text.isEmpty) {
      setStateIfMounted(() {});
    }
  }

  FocusNode keyboardFocusNode = new FocusNode();
  Widget buildInputAndroid(BuildContext context, bool isemojiShowing,
      Function refreshThisInput, bool keyboardVisible) {
    final observer = Provider.of<Observer>(context, listen: true);

    return Column(
        mainAxisAlignment: MainAxisAlignment.end,
        mainAxisSize: MainAxisSize.min,
        children: [
          isReplyKeyboard == true
              ? buildReplyMessageForInput(
                  context,
                )
              : SizedBox(),
          Container(
            margin: EdgeInsets.only(bottom: Platform.isIOS == true ? 20 : 0),
            child: Row(
              children: <Widget>[
                Flexible(
                  child: Container(
                    margin: EdgeInsets.only(
                      left: 10,
                    ),
                    decoration: BoxDecoration(
                        color: fiberchatWhite,
                        borderRadius: BorderRadius.all(Radius.circular(30))),
                    child: Row(
                      children: [
                        SizedBox(
                          width: 40,
                          child: IconButton(
                            onPressed: () {
                              refreshThisInput();
                            },
                            icon: Icon(Icons.emoji_emotions,
                                color: fiberchatGrey, size: 23),
                          ),
                        ),
                        Flexible(
                          child: TextField(
                            onTap: () {
                              if (isemojiShowing == true) {
                              } else {
                                keyboardFocusNode.requestFocus();
                                setStateIfMounted(() {});
                              }
                            },
                            // onChanged: (f) {
                            //   // if (textEditingController.text.length == 0 ||
                            //   //     textEditingController.text.length == 1) {
                            //   //   setStateIfMounted(() {});
                            //   // }
                            // },
                            showCursor: true,
                            focusNode: keyboardFocusNode,
                            maxLines: null,
                            textCapitalization: TextCapitalization.sentences,
                            style: TextStyle(
                                fontSize: 16.0, color: fiberchatBlack),
                            controller: textEditingController,
                            decoration: InputDecoration(
                              enabledBorder: OutlineInputBorder(
                                // width: 0.0 produces a thin "hairline" border
                                borderRadius: BorderRadius.circular(1),
                                borderSide: BorderSide(
                                    color: Colors.transparent, width: 1.5),
                              ),
                              hoverColor: Colors.transparent,
                              focusedBorder: OutlineInputBorder(
                                // width: 0.0 produces a thin "hairline" border
                                borderRadius: BorderRadius.circular(1),
                                borderSide: BorderSide(
                                    color: Colors.transparent, width: 1.5),
                              ),
                              border: OutlineInputBorder(
                                  borderRadius: BorderRadius.circular(1),
                                  borderSide:
                                      BorderSide(color: Colors.transparent)),
                              contentPadding: EdgeInsets.fromLTRB(10, 4, 7, 4),
                              hintText: getTranslated(this.context, 'msg'),
                              hintStyle:
                                  TextStyle(color: Colors.grey, fontSize: 15),
                            ),
                          ),
                        ),
                        Container(
                            margin: EdgeInsets.fromLTRB(0, 0, 5, 0),
                            width: textEditingController.text.isNotEmpty
                                ? 10
                                : IsShowGIFsenderButtonByGIPHY == false
                                    ? 80
                                    : 120,
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.spaceAround,
                              children: [
                                textEditingController.text.isNotEmpty
                                    ? SizedBox()
                                    : SizedBox(
                                        width: 30,
                                        child: IconButton(
                                          icon: new Icon(
                                            Icons.attachment_outlined,
                                            color: fiberchatGrey,
                                          ),
                                          padding: EdgeInsets.all(0.0),
                                          onPressed:
                                              observer.ismediamessagingallowed ==
                                                      false
                                                  ? () {
                                                      Fiberchat.showRationale(
                                                          getTranslated(
                                                              this.context,
                                                              'mediamssgnotallowed'));
                                                    }
                                                  : () {
                                                      hidekeyboard(context);
                                                      shareMedia(context);
                                                    },
                                          color: fiberchatWhite,
                                        ),
                                      ),
                                textEditingController.text.isNotEmpty
                                    ? SizedBox()
                                    : SizedBox(
                                        width: 30,
                                        child: IconButton(
                                          icon: new Icon(
                                            Icons.camera_alt_rounded,
                                            size: 20,
                                            color: fiberchatGrey,
                                          ),
                                          padding: EdgeInsets.all(0.0),
                                          onPressed:
                                              observer.ismediamessagingallowed ==
                                                      false
                                                  ? () {
                                                      Fiberchat.showRationale(
                                                          getTranslated(
                                                              this.context,
                                                              'mediamssgnotallowed'));
                                                    }
                                                  : () {
                                                      hidekeyboard(context);

                                                      Navigator.push(
                                                          context,
                                                          MaterialPageRoute(
                                                              builder: (context) =>
                                                                  MultiImagePicker(
                                                                    title: getTranslated(
                                                                        this.context,
                                                                        'pickimage'),
                                                                    callback:
                                                                        getFileData,
                                                                    writeMessage:
                                                                        (String?
                                                                                url,
                                                                            int time) async {
                                                                      if (url !=
                                                                          null) {
                                                                        onSendMessage(
                                                                            this.context,
                                                                            url,
                                                                            MessageType.image,
                                                                            time);
                                                                      }
                                                                    },
                                                                  )));
                                                    },
                                          color: fiberchatWhite,
                                        ),
                                      ),
                                textEditingController.text.length != 0 ||
                                        IsShowGIFsenderButtonByGIPHY == false
                                    ? SizedBox(
                                        width: 0,
                                      )
                                    : Container(
                                        margin: EdgeInsets.only(bottom: 5),
                                        height: 35,
                                        alignment: Alignment.topLeft,
                                        width: 40,
                                        child: IconButton(
                                            color: fiberchatWhite,
                                            padding: EdgeInsets.all(0.0),
                                            icon: Icon(
                                              Icons.gif_rounded,
                                              size: 40,
                                              color: fiberchatGrey,
                                            ),
                                            onPressed: observer
                                                        .ismediamessagingallowed ==
                                                    false
                                                ? () {
                                                    Fiberchat.showRationale(
                                                        getTranslated(
                                                            this.context,
                                                            'mediamssgnotallowed'));
                                                  }
                                                : () async {
                                                    GiphyGif? gif =
                                                        await GiphyGet.getGif(
                                                      tabColor: fiberchatgreen,
                                                      context: context,
                                                      apiKey:
                                                          GiphyAPIKey, //YOUR API KEY HERE
                                                      lang:
                                                          GiphyLanguage.english,
                                                    );
                                                    if (gif != null &&
                                                        mounted) {
                                                      onSendMessage(
                                                          context,
                                                          gif.images!.original!
                                                              .url,
                                                          MessageType.image,
                                                          DateTime.now()
                                                              .millisecondsSinceEpoch);
                                                      hidekeyboard(context);
                                                      setStateIfMounted(() {});
                                                    }
                                                  }),
                                      ),
                              ],
                            ))
                      ],
                    ),
                  ),
                ),
                Container(
                  height: 47,
                  width: 47,
                  // alignment: Alignment.center,
                  margin: EdgeInsets.only(left: 6, right: 10),
                  decoration: BoxDecoration(
                      color: DESIGN_TYPE == Themetype.whatsapp
                          ? fiberchatgreen
                          : fiberchatLightGreen,
                      // border: Border.all(
                      //   color: Colors.red[500],
                      // ),
                      borderRadius: BorderRadius.all(Radius.circular(30))),
                  child: Padding(
                    padding: const EdgeInsets.all(2.0),
                    child: IconButton(
                      icon: new Icon(
                        // textEditingController.text.isNotEmpty == true
                        //     ?
                        Icons.send,
                        // : Icons.mic,
                        color: fiberchatWhite.withOpacity(0.99),
                      ),
                      onPressed: observer.istextmessagingallowed == false
                          ? () {
                              Fiberchat.showRationale(getTranslated(
                                  this.context, 'textmssgnotallowed'));
                            }
                          : chatStatus == ChatStatus.blocked.index
                              ? null
                              : () => onSendMessage(
                                  context,
                                  textEditingController.text,
                                  MessageType.text,
                                  DateTime.now().millisecondsSinceEpoch),
                      color: fiberchatWhite,
                    ),
                  ),
                ),
              ],
            ),
            width: double.infinity,
            height: 60.0,
            decoration: new BoxDecoration(
              // border: new Border(top: new BorderSide(color: Colors.grey, width: 0.5)),
              color: Colors.transparent,
            ),
          ),
          isemojiShowing == true && keyboardVisible == false
              ? Offstage(
                  offstage: !isemojiShowing,
                  child: SizedBox(
                    height: 300,
                    child: EmojiPicker(
                        onEmojiSelected:
                            (emojipic.Category category, Emoji emoji) {
                          _onEmojiSelected(emoji);
                        },
                        onBackspacePressed: _onBackspacePressed,
                        config: Config(
                            columns: 7,
                            emojiSizeMax: 32.0,
                            verticalSpacing: 0,
                            horizontalSpacing: 0,
                            initCategory: emojipic.Category.RECENT,
                            bgColor: Color(0xFFF2F2F2),
                            indicatorColor: fiberchatgreen,
                            iconColor: Colors.grey,
                            iconColorSelected: fiberchatgreen,
                            progressIndicatorColor: Colors.blue,
                            backspaceColor: fiberchatgreen,
                            showRecentsTab: true,
                            recentsLimit: 28,
                          
                            categoryIcons: CategoryIcons(),
                            buttonMode: ButtonMode.MATERIAL)),
                  ),
                )
              : SizedBox(),
        ]);
  }

  onDismiss(Map<String, dynamic> doc) {
    if ((doc[Dbkeys.content] == '' || doc[Dbkeys.content] == null) == false) {
      final contactsProvider =
          Provider.of<AvailableContactsProvider>(this.context, listen: false);
      setStateIfMounted(() {
        isReplyKeyboard = true;
        replyDoc = doc;
        messageReplyOwnerName = contactsProvider
                    .alreadyJoinedUsersPhoneNameAsInServer
                    .indexWhere((element) =>
                        element.phone == doc[Dbkeys.groupmsgSENDBY]) >=
                0
            ? contactsProvider
                    .alreadyJoinedUsersPhoneNameAsInServer[contactsProvider
                        .alreadyJoinedUsersPhoneNameAsInServer
                        .indexWhere((element) =>
                            element.phone == doc[Dbkeys.groupmsgSENDBY])]
                    .name ??
                doc[Dbkeys.groupmsgSENDBY].toString()
            : doc[Dbkeys.groupmsgSENDBY].toString();
      });
      HapticFeedback.heavyImpact();
      keyboardFocusNode.requestFocus();
    }
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed)
      setIsActive();
    else
      setLastSeen();
  }

  void setIsActive() async {
    await FirebaseFirestore.instance
        .collection(DbPaths.collectionmessages)
        .doc(chatId)
        .set({'${widget.currentUserNo}': true}, SetOptions(merge: true));
  }

  dynamic lastSeen;

  FlutterSecureStorage storage = new FlutterSecureStorage();
  late encrypt.Encrypter cryptor;
  final iv = encrypt.IV.fromLength(8);

  readLocal(
    BuildContext context,
  ) async {
    try {
      privateKey = await storage.read(key: Dbkeys.privateKey);
      sharedSecret = (await e2ee.X25519().calculateSharedSecret(
              e2ee.Key.fromBase64(privateKey!, false),
              e2ee.Key.fromBase64(peer![Dbkeys.publicKey], true)))
          .toBase64();
      final key = encrypt.Key.fromBase64(sharedSecret!);
      cryptor = new encrypt.Encrypter(encrypt.Salsa20(key));
    } catch (e) {
      sharedSecret = null;
    }
    try {
      seenState!.value = widget.prefs.getInt(getLastSeenKey());
    } catch (e) {
      seenState!.value = false;
    }

    textEditingController.addListener(() {
      if (textEditingController.text.isNotEmpty && typing == false) {
        lastSeen = widget.peerNo;
        FirebaseFirestore.instance
            .collection(DbPaths.collectionusers)
            .doc(widget.currentUserNo)
            .update(
          {Dbkeys.lastSeen: widget.peerNo},
        );
        typing = true;
      }
      if (textEditingController.text.isEmpty && typing == true) {
        lastSeen = true;
        FirebaseFirestore.instance
            .collection(DbPaths.collectionusers)
            .doc(widget.currentUserNo)
            .update(
          {Dbkeys.lastSeen: true},
        );
        typing = false;
      }
    });
    setIsActive();
    seenSubscription = FirebaseFirestore.instance
        .collection(DbPaths.collectionmessages)
        .doc(chatId)
        .snapshots()
        .listen((doc) {
      // ignore: unnecessary_null_comparison
      if (doc != null && mounted && doc.data()!.containsKey(widget.peerNo)) {
        seenState!.value = doc[widget.peerNo!] ?? false;
        if (seenState!.value is int) {
          widget.prefs.setInt(getLastSeenKey(), seenState!.value);
        }
      }
    });
    loadMessagesAndListen();
  }

  String getLastSeenKey() {
    return "${widget.peerNo}-${Dbkeys.lastSeen}";
  }

  dynamic encryptWithCRC(String input) {
    try {
      String encrypted = cryptor.encrypt(input, iv: iv).base64;
      int crc = CRC32.compute(input);
      return '$encrypted${Dbkeys.crcSeperator}$crc';
    } catch (e) {
      Fiberchat.toast('Error occured while encrypting !');
      return false;
    }
  }

  Future<Position> _determinePosition() async {
    bool serviceEnabled;
    LocationPermission permission;

    // Test if location services are enabled.
    serviceEnabled = await Geolocator.isLocationServiceEnabled();
    if (!serviceEnabled) {
      // Location services are not enabled don't continue
      // accessing the position and request users of the
      // App to enable the location services.
      Fiberchat.toast(
          'Location permissions are denied. Please go to settings & allow location tracking permission.');
      return Future.error('Location services are disabled.');
    }

    permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
      if (permission == LocationPermission.deniedForever) {
        // Permissions are denied forever, handle appropriately.
        Fiberchat.toast(
            'Location permissions are denied. Please go to settings & allow location tracking permission.');
        return Future.error(
            'Location permissions are permanently denied, we cannot request permissions.');
      }

      if (permission == LocationPermission.denied) {
        Fiberchat.toast(
            'Location permissions are pdenied. Please go to settings & allow location tracking permission.');
        return Future.error('Location permissions are denied');
      }
    }
    if (permission == LocationPermission.always ||
        permission == LocationPermission.whileInUse) {
      Fiberchat.toast(
        getTranslated(this.context, 'detectingloc'),
      );
    }
    // When we reach here, permissions are granted and we can
    // continue accessing the position of the device.
    return await Geolocator.getCurrentPosition();
  }

  Widget buildMessagesUsingProvider(BuildContext context) {
    return Consumer<FirestoreDataProviderMESSAGESforLAZYLOADINGCHAT>(
        builder: (context, firestoreDataProvider, _) =>
            InfiniteCOLLECTIONListViewWidget(
              scrollController: realtime,
              isreverse: true,
              firestoreDataProviderMESSAGESforLAZYLOADINGCHAT:
                  firestoreDataProvider,
              datatype: Dbkeys.datatypeONETOONEMSGS,
              refdata: firestoreChatquery,
              list: ListView.builder(
                  reverse: true,
                  padding: EdgeInsets.all(7),
                  physics: ScrollPhysics(),
                  shrinkWrap: true,
                  itemCount: firestoreDataProvider.recievedDocs.length,
                  itemBuilder: (BuildContext context, int i) {
                    var _doc = firestoreDataProvider.recievedDocs[i];
                    // final bool isMe = _doc[Dbkeys.from] == widget.currentUserNo;
                    int? ts = _doc[Dbkeys.timestamp];
                    return Dismissible(
                        direction: DismissDirection.startToEnd,
                        key: Key(ts.toString()),
                        confirmDismiss: _doc[Dbkeys.content] == '' ||
                                _doc[Dbkeys.content] == null
                            ? (direction) {
                                return Future.value(false);
                              }
                            : (direction) {
                                setStateIfMounted(() {
                                  isReplyKeyboard = true;
                                  replyDoc = _doc;
                                });
                                HapticFeedback.heavyImpact();
                                keyboardFocusNode.requestFocus();
                                return Future.value(false);
                              },
                        child: GestureDetector(
                          child: builEachMsg(context, _doc),
                          onTap: (_doc[Dbkeys.from] == widget.currentUserNo &&
                                      _doc[Dbkeys.hasSenderDeleted] == true) ==
                                  true
                              ? () {}
                              : _doc[Dbkeys.messageType] ==
                                      MessageType.image.index
                                  ? () {
                                      Navigator.push(
                                          this.context,
                                          MaterialPageRoute(
                                            builder: (context) =>
                                                PhotoViewWrapper(
                                              keyloader: _keyLoader,
                                              imageUrl: _doc[Dbkeys.content],
                                              message: _doc[Dbkeys.content],
                                              tag: ts.toString(),
                                              imageProvider:
                                                  CachedNetworkImageProvider(
                                                      _doc[Dbkeys.content]),
                                            ),
                                          ));
                                    }
                                  : null,
                          onDoubleTap: _doc.containsKey(Dbkeys.broadcastID)
                              ? () {}
                              : () {},
                          onLongPress: () {
                            if (_doc.containsKey(Dbkeys.hasRecipientDeleted) &&
                                _doc.containsKey(Dbkeys.hasSenderDeleted)) {
                              if ((_doc[Dbkeys.from] == widget.currentUserNo &&
                                      _doc[Dbkeys.hasSenderDeleted] == true) ==
                                  false) {
                                //--Show Menu only if message is not deleted by current user already
                                contextMenuNew(this.context, _doc, false);
                              }
                            } else {
                              contextMenuOld(this.context, _doc);
                            }
                          },
                        ));
                    // builEachMsg(context, dc);
                    // buildEachMessage(
                    //     dc,
                    //     groupList.lastWhere((element) =>
                    //         element.docmap[Dbkeys.groupID] == widget.groupID));
                  }),
            ));
  }

  //-- New context menu with Delete for Me & Delete For Everyone feature
  contextMenuNew(contextForDialog, Map<String, dynamic> mssgDoc, bool isTemp,
      {bool saved = false}) {
    List<Widget> tiles = List.from(<Widget>[]);
    //####################----------------------- Delete Msgs for SENDER ---------------------------------------------------
    if ((mssgDoc[Dbkeys.from] == widget.currentUserNo &&
            mssgDoc[Dbkeys.hasSenderDeleted] == false) &&
        saved == false) {
      tiles.add(ListTile(
          dense: true,
          leading: Icon(Icons.delete_outline),
          title: Text(
            getTranslated(contextForDialog, 'dltforme'),
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          onTap: () async {
            Fiberchat.toast(getTranslated(contextForDialog, 'deleting'));
            await FirebaseFirestore.instance
                .collection(DbPaths.collectionmessages)
                .doc(chatId)
                .collection(chatId!)
                .doc('${mssgDoc[Dbkeys.timestamp]}')
                .get()
                .then((chatDoc) async {
              if (!chatDoc.exists) {
                Fiberchat.toast('Please reload this screen !');
              } else if (chatDoc.exists) {
                Map<String, dynamic> realtimeDoc = chatDoc.data()!;
                if (realtimeDoc[Dbkeys.hasRecipientDeleted] == true) {
                  if ((mssgDoc.containsKey(Dbkeys.isbroadcast) == true
                          ? mssgDoc[Dbkeys.isbroadcast]
                          : false) ==
                      true) {
                    // -------Delete broadcast message completely as recipient has already deleted
                    await FirebaseFirestore.instance
                        .collection(DbPaths.collectionmessages)
                        .doc(chatId)
                        .collection(chatId!)
                        .doc('${realtimeDoc[Dbkeys.timestamp]}')
                        .delete();
                    delete(realtimeDoc[Dbkeys.timestamp]);

                    Future.delayed(const Duration(milliseconds: 300), () {
                      Navigator.maybePop(
                        contextForDialog,
                      );
                      Fiberchat.toast(
                        getTranslated(contextForDialog, 'deleted'),
                      );
                      hidekeyboard(
                        contextForDialog,
                      );
                    });
                  } else {
                    // -------Delete message completely as recipient has already deleted
                    await deleteMsgMedia(realtimeDoc, chatId!)
                        .then((isDeleted) async {
                      if (isDeleted == false || isDeleted == null) {
                        Fiberchat.toast('Could not delete. Please try again!');
                      } else {
                        await FirebaseFirestore.instance
                            .collection(DbPaths.collectionmessages)
                            .doc(chatId)
                            .collection(chatId!)
                            .doc('${realtimeDoc[Dbkeys.timestamp]}')
                            .delete();
                        delete(realtimeDoc[Dbkeys.timestamp]);

                        Future.delayed(const Duration(milliseconds: 300), () {
                          Navigator.maybePop(
                            contextForDialog,
                          );
                          Fiberchat.toast(
                            getTranslated(contextForDialog, 'deleted'),
                          );
                          hidekeyboard(contextForDialog);
                        });
                      }
                    });
                  }
                } else {
                  //----Don't Delete Media from server, as recipient has not deleted the message from thier message list-----
                  FirebaseFirestore.instance
                      .collection(DbPaths.collectionmessages)
                      .doc(chatId)
                      .collection(chatId!)
                      .doc('${realtimeDoc[Dbkeys.timestamp]}')
                      .set({Dbkeys.hasSenderDeleted: true},
                          SetOptions(merge: true));

                  Map<String, dynamic> tempDoc = realtimeDoc;
                  setStateIfMounted(() {
                    tempDoc[Dbkeys.hasSenderDeleted] = true;
                  });
                  updateDeleteBySenderField(
                      realtimeDoc[Dbkeys.timestamp], tempDoc, contextForDialog);

                  Future.delayed(const Duration(milliseconds: 300), () {
                    Navigator.maybePop(contextForDialog);
                    Fiberchat.toast(
                      getTranslated(contextForDialog, 'deleted'),
                    );
                    hidekeyboard(contextForDialog);
                  });
                }
              }
            });
          }));

      tiles.add(ListTile(
          dense: true,
          leading: Icon(Icons.delete),
          title: Text(
            getTranslated(contextForDialog, 'dltforeveryone'),
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          onTap: () async {
            if ((mssgDoc.containsKey(Dbkeys.isbroadcast) == true
                    ? mssgDoc[Dbkeys.isbroadcast]
                    : false) ==
                true) {
              // -------Delete broadcast message completely for everyone
              await FirebaseFirestore.instance
                  .collection(DbPaths.collectionmessages)
                  .doc(chatId)
                  .collection(chatId!)
                  .doc('${mssgDoc[Dbkeys.timestamp]}')
                  .delete();
              delete(mssgDoc[Dbkeys.timestamp]);

              Future.delayed(const Duration(milliseconds: 300), () {
                Navigator.maybePop(contextForDialog);
                Fiberchat.toast(
                  getTranslated(contextForDialog, 'deleted'),
                );
                hidekeyboard(contextForDialog);
              });
            } else {
              // -------Delete message completely for everyone
              Fiberchat.toast(
                getTranslated(contextForDialog, 'deleting'),
              );
              await deleteMsgMedia(mssgDoc, chatId!).then((isDeleted) async {
                if (isDeleted == false || isDeleted == null) {
                  Fiberchat.toast('Could not delete. Please try again!');
                } else {
                  await FirebaseFirestore.instance
                      .collection(DbPaths.collectionmessages)
                      .doc(chatId)
                      .collection(chatId!)
                      .doc('${mssgDoc[Dbkeys.timestamp]}')
                      .delete();
                  delete(mssgDoc[Dbkeys.timestamp]);

                  Future.delayed(const Duration(milliseconds: 300), () {
                    Navigator.maybePop(contextForDialog);
                    Fiberchat.toast(
                      getTranslated(contextForDialog, 'deleted'),
                    );
                    hidekeyboard(contextForDialog);
                  });
                }
              });
            }
          }));
    }
    //####################-------------------- Delete Msgs for RECIPIENTS---------------------------------------------------
    if ((mssgDoc[Dbkeys.to] == widget.currentUserNo &&
            mssgDoc[Dbkeys.hasRecipientDeleted] == false) &&
        saved == false) {
      tiles.add(ListTile(
          dense: true,
          leading: Icon(Icons.delete_outline),
          title: Text(
            getTranslated(contextForDialog, 'dltforme'),
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          onTap: () async {
            Fiberchat.toast(
              getTranslated(contextForDialog, 'deleting'),
            );
            await FirebaseFirestore.instance
                .collection(DbPaths.collectionmessages)
                .doc(chatId)
                .collection(chatId!)
                .doc('${mssgDoc[Dbkeys.timestamp]}')
                .get()
                .then((chatDoc) async {
              if (!chatDoc.exists) {
                Fiberchat.toast('Please reload this screen !');
              } else if (chatDoc.exists) {
                Map<String, dynamic> realtimeDoc = chatDoc.data()!;
                if (realtimeDoc[Dbkeys.hasSenderDeleted] == true) {
                  if ((mssgDoc.containsKey(Dbkeys.isbroadcast) == true
                          ? mssgDoc[Dbkeys.isbroadcast]
                          : false) ==
                      true) {
                    // -------Delete broadcast message completely as sender has already deleted
                    await FirebaseFirestore.instance
                        .collection(DbPaths.collectionmessages)
                        .doc(chatId)
                        .collection(chatId!)
                        .doc('${realtimeDoc[Dbkeys.timestamp]}')
                        .delete();
                    delete(realtimeDoc[Dbkeys.timestamp]);

                    Future.delayed(const Duration(milliseconds: 300), () {
                      Navigator.maybePop(contextForDialog);
                      Fiberchat.toast(
                        getTranslated(contextForDialog, 'deleted'),
                      );
                      hidekeyboard(contextForDialog);
                    });
                  } else {
                    // -------Delete message completely as sender has already deleted
                    await deleteMsgMedia(realtimeDoc, chatId!)
                        .then((isDeleted) async {
                      if (isDeleted == false || isDeleted == null) {
                        Fiberchat.toast('Could not delete. Please try again!');
                      } else {
                        await FirebaseFirestore.instance
                            .collection(DbPaths.collectionmessages)
                            .doc(chatId)
                            .collection(chatId!)
                            .doc('${realtimeDoc[Dbkeys.timestamp]}')
                            .delete();
                        delete(realtimeDoc[Dbkeys.timestamp]);

                        Future.delayed(const Duration(milliseconds: 300), () {
                          Navigator.maybePop(contextForDialog);
                          Fiberchat.toast(
                            getTranslated(contextForDialog, 'deleted'),
                          );
                          hidekeyboard(contextForDialog);
                        });
                      }
                    });
                  }
                } else {
                  //----Don't Delete Media from server, as recipient has not deleted the message from thier message list-----
                  FirebaseFirestore.instance
                      .collection(DbPaths.collectionmessages)
                      .doc(chatId)
                      .collection(chatId!)
                      .doc('${realtimeDoc[Dbkeys.timestamp]}')
                      .set({Dbkeys.hasRecipientDeleted: true},
                          SetOptions(merge: true));

                  if (isTemp == true) {
                    Map<String, dynamic> tempDoc = realtimeDoc;
                    setStateIfMounted(() {
                      tempDoc[Dbkeys.hasRecipientDeleted] = true;
                    });
                    updateDeleteBySenderField(realtimeDoc[Dbkeys.timestamp],
                        tempDoc, contextForDialog);
                  }
                  Future.delayed(const Duration(milliseconds: 300), () {
                    Navigator.maybePop(contextForDialog);
                    Fiberchat.toast(
                      getTranslated(contextForDialog, 'deleted'),
                    );
                    hidekeyboard(contextForDialog);
                  });
                }
              }
            });
          }));
    }
    if (mssgDoc.containsKey(Dbkeys.broadcastID) &&
        mssgDoc[Dbkeys.to] == widget.currentUserNo) {
      tiles.add(ListTile(
          dense: true,
          leading: Icon(Icons.block),
          title: Text(
            getTranslated(contextForDialog, 'blockbroadcast'),
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          onTap: () {
            Fiberchat.toast(
              getTranslated(contextForDialog, 'plswait'),
            );
            Future.delayed(const Duration(milliseconds: 200), () {
              FirebaseFirestore.instance
                  .collection(DbPaths.collectionbroadcasts)
                  .doc(mssgDoc[Dbkeys.broadcastID])
                  .update({
                Dbkeys.broadcastMEMBERSLIST:
                    FieldValue.arrayRemove([widget.currentUserNo]),
                Dbkeys.broadcastBLACKLISTED:
                    FieldValue.arrayUnion([widget.currentUserNo]),
              }).then((value) {
                Navigator.pop(contextForDialog);
                hidekeyboard(contextForDialog);
                Fiberchat.toast(
                  getTranslated(contextForDialog, 'blockedbroadcast'),
                );
              }).catchError((error) {
                Navigator.pop(contextForDialog);

                hidekeyboard(contextForDialog);
              });
            });
          }));
    }

    if (mssgDoc[Dbkeys.messageType] == MessageType.text.index &&
        !mssgDoc.containsKey(Dbkeys.broadcastID)) {
      tiles.add(ListTile(
          dense: true,
          leading: Icon(Icons.content_copy),
          title: Text(
            getTranslated(contextForDialog, 'copy'),
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          onTap: () {
            Clipboard.setData(ClipboardData(text: mssgDoc[Dbkeys.content]));
            Navigator.pop(contextForDialog);
            hidekeyboard(contextForDialog);
            Fiberchat.toast(
              getTranslated(contextForDialog, 'copied'),
            );
          }));
    }
    if (((mssgDoc[Dbkeys.from] == widget.currentUserNo &&
                mssgDoc[Dbkeys.hasSenderDeleted] == false) ||
            (mssgDoc[Dbkeys.to] == widget.currentUserNo &&
                mssgDoc[Dbkeys.hasRecipientDeleted] == false)) ==
        true) {
      tiles.add(ListTile(
          dense: true,
          leading: Icon(FontAwesomeIcons.share, size: 22),
          title: Text(
            getTranslated(contextForDialog, 'forward'),
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          onTap: () async {
            Navigator.of(contextForDialog).pop();
            Navigator.push(
                contextForDialog,
                MaterialPageRoute(
                    builder: (contextForDialog) => SelectContactsToForward(
                        contentPeerNo: peer![Dbkeys.phone],
                        messageOwnerPhone: widget.peerNo!,
                        currentUserNo: widget.currentUserNo,
                        model: widget.model,
                        prefs: widget.prefs,
                        onSelect: (selectedlist) async {
                          if (selectedlist.length > 0) {
                            setStateIfMounted(() {
                              isgeneratingSomethingLoader = true;
                              tempSendIndex = 0;
                            });

                            String? privateKey =
                                await storage.read(key: Dbkeys.privateKey);

                            sendForwardMessageEach(
                                0, selectedlist, privateKey!, mssgDoc);
                          }
                        })));
          }));
    }

    showDialog(
        context: contextForDialog,
        builder: (contextForDialog) {
          return SimpleDialog(children: tiles);
        });
  }

  delete(int? ts) {
    setStateIfMounted(() {
      // messages.removeWhere((msg) => msg.timestamp == ts);
      // messages = List.from(messages);
    });
  }

  updateDeleteBySenderField(int? ts, updateDoc, context) {
    setStateIfMounted(() {
      // int i = messages.indexWhere((msg) => msg.timestamp == ts);
      // var child = buildTempMessage(
      //     context,
      //     MessageType.text,
      //     updateDoc[Dbkeys.content],
      //     updateDoc[Dbkeys.timestamp],
      //     true,
      //     updateDoc);
      // var timestamp = messages[i].timestamp;
      // var from = messages[i].from;
      // // var onTap = messages[i].onTap;
      // var onDoubleTap = messages[i].onDoubleTap;
      // var onDismiss = messages[i].onDismiss;
      // var onLongPress = () {};
      // if (i >= 0) {
      //   messages.removeWhere((msg) => msg.timestamp == ts);
      //   messages.insert(
      //       i,
      //       Message(child,
      //           timestamp: timestamp,
      //           from: from,
      //           onTap: () {},
      //           onDoubleTap: onDoubleTap,
      //           onDismiss: onDismiss,
      //           onLongPress: onLongPress));
      // }
      // messages = List.from(messages);
    });
  }

  sendForwardMessageEach(
      int index, List<dynamic> list, String privateKey, var mssgDoc) async {
    if (index > list.length) {
      setStateIfMounted(() {
        isgeneratingSomethingLoader = false;
        Navigator.of(this.context).pop();
      });
    } else {
      setStateIfMounted(() {
        tempSendIndex = index;
      });
      if (list[tempSendIndex].containsKey(Dbkeys.groupNAME)) {
        try {
          Map<dynamic, dynamic> groupDoc = list[tempSendIndex].data();
          int timestamp = DateTime.now().millisecondsSinceEpoch;

          FirebaseFirestore.instance
              .collection(DbPaths.collectiongroups)
              .doc(groupDoc[Dbkeys.groupID])
              .collection(DbPaths.collectiongroupChats)
              .doc(timestamp.toString() + '--' + widget.currentUserNo!)
              .set({
            Dbkeys.groupmsgCONTENT: mssgDoc[Dbkeys.content],
            Dbkeys.groupmsgISDELETED: false,
            Dbkeys.groupmsgLISToptional: [],
            Dbkeys.groupmsgTIME: timestamp,
            Dbkeys.groupmsgSENDBY: widget.currentUserNo!,
            Dbkeys.groupmsgISDELETED: false,
            Dbkeys.groupmsgTYPE: mssgDoc[Dbkeys.messageType],
            Dbkeys.groupNAME: groupDoc[Dbkeys.groupNAME],
            Dbkeys.groupID: groupDoc[Dbkeys.groupNAME],
            Dbkeys.sendername: widget.model.currentUser![Dbkeys.nickname],
            Dbkeys.groupIDfiltered: groupDoc[Dbkeys.groupIDfiltered],
            Dbkeys.isReply: false,
            Dbkeys.replyToMsgDoc: null,
            Dbkeys.isForward: true
          }, SetOptions(merge: true)).then((value) {
            unawaited(realtime.animateTo(0.0,
                duration: Duration(milliseconds: 300), curve: Curves.easeOut));
            // _playPopSound();
            FirebaseFirestore.instance
                .collection(DbPaths.collectiongroups)
                .doc(groupDoc[Dbkeys.groupID])
                .update(
              {Dbkeys.groupLATESTMESSAGETIME: timestamp},
            );
          }).then((value) {
            if (list.last[Dbkeys.groupID] ==
                list[tempSendIndex][Dbkeys.groupID]) {
              Fiberchat.toast(
                getTranslated(this.context, 'sent'),
              );
              setStateIfMounted(() {
                isgeneratingSomethingLoader = false;
              });
              Navigator.of(this.context).pop();
            } else {
              sendForwardMessageEach(
                  tempSendIndex + 1, list, privateKey, mssgDoc);
            }
          });
        } catch (e) {
          setStateIfMounted(() {
            isgeneratingSomethingLoader = false;
          });
          Fiberchat.toast('Failed to send');
        }
      } else {
        try {
          String? sharedSecret = (await e2ee.X25519().calculateSharedSecret(
                  e2ee.Key.fromBase64(privateKey, false),
                  e2ee.Key.fromBase64(
                      list[tempSendIndex][Dbkeys.publicKey], true)))
              .toBase64();
          final key = encrypt.Key.fromBase64(sharedSecret);
          cryptor = new encrypt.Encrypter(encrypt.Salsa20(key));
          String content = mssgDoc[Dbkeys.content];
          final encrypted = encryptWithCRC(content);
          if (encrypted is String) {
            int timestamp2 = DateTime.now().millisecondsSinceEpoch;
            var chatId = Fiberchat.getChatId(
                widget.currentUserNo, list[tempSendIndex][Dbkeys.phone]);
            if (content.trim() != '') {
              Map<String, dynamic>? targetPeer =
                  widget.model.userData[list[tempSendIndex][Dbkeys.phone]];
              if (targetPeer == null) {
                await ChatController.request(
                    widget.currentUserNo,
                    list[tempSendIndex][Dbkeys.phone],
                    Fiberchat.getChatId(widget.currentUserNo,
                        list[tempSendIndex][Dbkeys.phone]));
              }

              await FirebaseFirestore.instance
                  .collection(DbPaths.collectionmessages)
                  .doc(chatId)
                  .set({
                widget.currentUserNo!: true,
                list[tempSendIndex][Dbkeys.phone]: list[tempSendIndex]
                    [Dbkeys.lastSeen],
              }, SetOptions(merge: true)).then((value) async {
                Future messaging = FirebaseFirestore.instance
                    .collection(DbPaths.collectionusers)
                    .doc(list[tempSendIndex][Dbkeys.phone])
                    .collection(Dbkeys.chatsWith)
                    .doc(Dbkeys.chatsWith)
                    .set({
                  widget.currentUserNo!: 4,
                }, SetOptions(merge: true));
                await widget.model.addMessage(
                    list[tempSendIndex][Dbkeys.phone], timestamp2, messaging);
              }).then((value) async {
                Future messaging = FirebaseFirestore.instance
                    .collection(DbPaths.collectionmessages)
                    .doc(chatId)
                    .collection(chatId)
                    .doc('$timestamp2')
                    .set({
                  Dbkeys.from: widget.currentUserNo!,
                  Dbkeys.to: list[tempSendIndex][Dbkeys.phone],
                  Dbkeys.timestamp: timestamp2,
                  Dbkeys.content: encrypted,
                  Dbkeys.messageType: mssgDoc[Dbkeys.messageType],
                  Dbkeys.hasSenderDeleted: false,
                  Dbkeys.hasRecipientDeleted: false,
                  Dbkeys.sendername: widget.model.currentUser![Dbkeys.nickname],
                  Dbkeys.isReply: false,
                  Dbkeys.replyToMsgDoc: null,
                  Dbkeys.isForward: true
                }, SetOptions(merge: true));
                await widget.model.addMessage(
                    list[tempSendIndex][Dbkeys.phone], timestamp2, messaging);
              }).then((value) {
                if (list.last[Dbkeys.phone] ==
                    list[tempSendIndex][Dbkeys.phone]) {
                  Fiberchat.toast(
                    getTranslated(this.context, 'sent'),
                  );
                  setStateIfMounted(() {
                    isgeneratingSomethingLoader = false;
                  });
                  Navigator.of(this.context).pop();
                } else {
                  sendForwardMessageEach(
                      tempSendIndex + 1, list, privateKey, mssgDoc);
                }
              });
            }
          } else {
            setStateIfMounted(() {
              isgeneratingSomethingLoader = false;
            });
            Fiberchat.toast('Nothing to send');
          }
        } catch (e) {
          setStateIfMounted(() {
            isgeneratingSomethingLoader = false;
          });
          Fiberchat.toast('Failed to Forward message. Error:$e');
        }
      }
    }
  }

  contextMenuOld(BuildContext context, Map<String, dynamic> doc,
      {bool saved = false}) {
    List<Widget> tiles = List.from(<Widget>[]);
    if ((doc[Dbkeys.from] != widget.currentUserNo) && saved == false) {
      tiles.add(ListTile(
          dense: true,
          leading: Icon(Icons.delete),
          title: Text(
            getTranslated(this.context, 'dltforme'),
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          onTap: () async {
            await FirebaseFirestore.instance
                .collection(DbPaths.collectionmessages)
                .doc(chatId)
                .collection(chatId!)
                .doc('${doc[Dbkeys.timestamp]}')
                .update({Dbkeys.hasRecipientDeleted: true});

            Future.delayed(const Duration(milliseconds: 300), () {
              Navigator.maybePop(context);
              Fiberchat.toast(
                getTranslated(this.context, 'deleted'),
              );
            });
          }));
    }

    if (doc[Dbkeys.messageType] == MessageType.text.index) {
      tiles.add(ListTile(
          dense: true,
          leading: Icon(Icons.content_copy),
          title: Text(
            getTranslated(context, 'copy'),
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          onTap: () {
            Clipboard.setData(ClipboardData(text: doc[Dbkeys.content]));
            Navigator.pop(context);
            Fiberchat.toast(
              getTranslated(this.context, 'copied'),
            );
          }));
    }
    if (doc.containsKey(Dbkeys.broadcastID) &&
        doc[Dbkeys.to] == widget.currentUserNo) {
      tiles.add(ListTile(
          dense: true,
          leading: Icon(Icons.block),
          title: Text(
            getTranslated(this.context, 'blockbroadcast'),
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          onTap: () {
            Fiberchat.toast(
              getTranslated(this.context, 'plswait'),
            );
            Future.delayed(const Duration(milliseconds: 500), () {
              FirebaseFirestore.instance
                  .collection(DbPaths.collectionbroadcasts)
                  .doc(doc[Dbkeys.broadcastID])
                  .update({
                Dbkeys.broadcastMEMBERSLIST:
                    FieldValue.arrayRemove([widget.currentUserNo]),
                Dbkeys.broadcastBLACKLISTED:
                    FieldValue.arrayUnion([widget.currentUserNo]),
              }).then((value) {
                Fiberchat.toast(
                  getTranslated(this.context, 'blockedbroadcast'),
                );
                hidekeyboard(context);
                Navigator.pop(context);
              }).catchError((error) {
                Fiberchat.toast(
                  getTranslated(this.context, 'blockedbroadcast'),
                );
                Navigator.pop(context);
                hidekeyboard(context);
              });
            });
          }));
    }
    showDialog(
        context: context,
        builder: (context) {
          return SimpleDialog(children: tiles);
        });
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
      Fiberchat.toast(getTranslated(this.context, 'msgnotload'));
      return '';
    }
    Fiberchat.toast(getTranslated(this.context, 'msgnotload'));
    return '';
  }

  builEachMsg(
    BuildContext context,
    Map<String, dynamic> doc,
  ) {
    final observer = Provider.of<Observer>(context, listen: false);
    final bool isMe = doc[Dbkeys.from] == widget.currentUserNo;
    bool isContinuing;
    doc[Dbkeys.content] = decryptWithCRC(doc[Dbkeys.content]);
    isContinuing = true;
    // messages.isNotEmpty ? messages.last.from == doc[Dbkeys.from] : false;

    return SeenProvider(
        timestamp: doc[Dbkeys.timestamp].toString(),
        data: seenState,
        child: Bubble(
            isURLtext: doc[Dbkeys.messageType] == MessageType.text.index &&
                Uri.parse(doc[Dbkeys.content]!).isAbsolute,
            mssgDoc: doc,
            is24hrsFormat: observer.is24hrsTimeformat,
            isMssgDeleted: (doc.containsKey(Dbkeys.hasRecipientDeleted) &&
                    doc.containsKey(Dbkeys.hasSenderDeleted))
                ? isMe
                    ? (doc[Dbkeys.from] == widget.currentUserNo
                        ? doc[Dbkeys.hasSenderDeleted]
                        : false)
                    : (doc[Dbkeys.from] != widget.currentUserNo
                        ? doc[Dbkeys.hasRecipientDeleted]
                        : false)
                : false,
            isBroadcastMssg: doc.containsKey(Dbkeys.isbroadcast) == true
                ? doc[Dbkeys.isbroadcast]
                : false,
            messagetype: doc[Dbkeys.messageType] == MessageType.text.index
                ? MessageType.text
                : doc[Dbkeys.messageType] == MessageType.contact.index
                    ? MessageType.contact
                    : doc[Dbkeys.messageType] == MessageType.location.index
                        ? MessageType.location
                        : doc[Dbkeys.messageType] == MessageType.image.index
                            ? MessageType.image
                            : doc[Dbkeys.messageType] == MessageType.video.index
                                ? MessageType.video
                                : doc[Dbkeys.messageType] ==
                                        MessageType.doc.index
                                    ? MessageType.doc
                                    : doc[Dbkeys.messageType] ==
                                            MessageType.audio.index
                                        ? MessageType.audio
                                        : MessageType.text,
            child: doc[Dbkeys.messageType] == MessageType.text.index
                ? getTextMessage(isMe, doc)
                : doc[Dbkeys.messageType] == MessageType.location.index
                    ? getLocationMessage(doc, doc[Dbkeys.content], saved: false)
                    : doc[Dbkeys.messageType] == MessageType.doc.index
                        ? getDocmessage(context, doc, doc[Dbkeys.content],
                            saved: false)
                        : doc[Dbkeys.messageType] == MessageType.audio.index
                            ? getAudiomessage(context, doc, doc[Dbkeys.content],
                                isMe: isMe, saved: false)
                            : doc[Dbkeys.messageType] == MessageType.video.index
                                ? getVideoMessage(
                                    context, doc, doc[Dbkeys.content],
                                    saved: false)
                                : doc[Dbkeys.messageType] ==
                                        MessageType.contact.index
                                    ? getContactMessage(
                                        context, doc, doc[Dbkeys.content],
                                        saved: false)
                                    : getImageMessage(
                                        doc,
                                      ),
            isMe: isMe,
            timestamp: doc[Dbkeys.timestamp],
            delivered: _cachedModel.getMessageStatus(
                widget.peerNo, doc[Dbkeys.timestamp]),
            isContinuing: isContinuing));
  }

  Widget buildLoadingThumbnail() {
    return Positioned(
      child: isgeneratingSomethingLoader
          ? Container(
              child: Center(
                child: CircularProgressIndicator(
                    valueColor: AlwaysStoppedAnimation<Color>(fiberchatBlue)),
              ),
              color: DESIGN_TYPE == Themetype.whatsapp
                  ? fiberchatBlack.withOpacity(0.6)
                  : fiberchatWhite.withOpacity(0.6),
            )
          : Container(),
    );
  }

  Widget getTextMessage(bool isMe, Map<String, dynamic> doc) {
    return doc.containsKey(Dbkeys.isReply) == true
        ? doc[Dbkeys.isReply] == true
            ? Column(
                crossAxisAlignment: isMe == true
                    ? CrossAxisAlignment.end
                    : CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                mainAxisSize: MainAxisSize.min,
                children: [
                  replyAttachedWidget(this.context, doc[Dbkeys.replyToMsgDoc]),
                  SizedBox(
                    height: 10,
                  ),
                  selectablelinkify(doc[Dbkeys.content], 16),
                ],
              )
            : doc.containsKey(Dbkeys.isForward) == true
                ? doc[Dbkeys.isForward] == true
                    ? Column(
                        crossAxisAlignment: isMe
                            ? CrossAxisAlignment.start
                            : CrossAxisAlignment.end,
                        mainAxisAlignment: MainAxisAlignment.end,
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Container(
                              child: Row(
                                  mainAxisAlignment: isMe == true
                                      ? MainAxisAlignment.start
                                      : MainAxisAlignment.end,
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                Icon(
                                  FontAwesomeIcons.share,
                                  size: 12,
                                  color: fiberchatGrey.withOpacity(0.5),
                                ),
                                SizedBox(
                                  width: 5,
                                ),
                                Text(getTranslated(this.context, 'forwarded'),
                                    maxLines: 1,
                                    style: TextStyle(
                                        color: fiberchatGrey.withOpacity(0.7),
                                        fontStyle: FontStyle.italic,
                                        overflow: TextOverflow.ellipsis,
                                        fontSize: 13))
                              ])),
                          SizedBox(
                            height: 10,
                          ),
                          selectablelinkify(doc[Dbkeys.content], 16),
                        ],
                      )
                    : selectablelinkify(doc[Dbkeys.content], 16)
                : selectablelinkify(doc[Dbkeys.content], 16)
        : selectablelinkify(doc[Dbkeys.content], 16);
  }

  Widget getLocationMessage(Map<String, dynamic> doc, String? message,
      {bool saved = false}) {
    final bool isMe = doc[Dbkeys.from] == widget.currentUserNo;
    return InkWell(
      onTap: () async {
        custom_url_launcher(message!);
      },
      child: doc.containsKey(Dbkeys.isForward) == true
          ? doc[Dbkeys.isForward] == true
              ? Column(
                  crossAxisAlignment:
                      isMe ? CrossAxisAlignment.start : CrossAxisAlignment.end,
                  mainAxisAlignment: MainAxisAlignment.end,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Container(
                        child: Row(
                            mainAxisAlignment: isMe == true
                                ? MainAxisAlignment.start
                                : MainAxisAlignment.end,
                            mainAxisSize: MainAxisSize.min,
                            children: [
                          Icon(
                            FontAwesomeIcons.share,
                            size: 12,
                            color: fiberchatGrey.withOpacity(0.5),
                          ),
                          SizedBox(
                            width: 5,
                          ),
                          Text(getTranslated(this.context, 'forwarded'),
                              maxLines: 1,
                              style: TextStyle(
                                  color: fiberchatGrey.withOpacity(0.7),
                                  fontStyle: FontStyle.italic,
                                  overflow: TextOverflow.ellipsis,
                                  fontSize: 13))
                        ])),
                    SizedBox(
                      height: 10,
                    ),
                    Image.asset(
                      'assets/images/mapview.jpg',
                    )
                  ],
                )
              : Image.asset(
                  'assets/images/mapview.jpg',
                )
          : Image.asset(
              'assets/images/mapview.jpg',
            ),
    );
  }

  Widget getAudiomessage(
      BuildContext context, Map<String, dynamic> doc, String message,
      {bool saved = false, bool isMe = true}) {
    return Container(
      margin: EdgeInsets.only(bottom: 10),
      // width: 250,
      // height: 116,
      child: Column(
        crossAxisAlignment:
            isMe == true ? CrossAxisAlignment.start : CrossAxisAlignment.end,
        children: [
          doc.containsKey(Dbkeys.isForward) == true
              ? doc[Dbkeys.isForward] == true
                  ? Container(
                      margin: EdgeInsets.only(bottom: 10),
                      child: Row(
                          mainAxisAlignment: isMe == true
                              ? MainAxisAlignment.start
                              : MainAxisAlignment.end,
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(
                              FontAwesomeIcons.share,
                              size: 12,
                              color: fiberchatGrey.withOpacity(0.5),
                            ),
                            SizedBox(
                              width: 5,
                            ),
                            Text(getTranslated(this.context, 'forwarded'),
                                maxLines: 1,
                                style: TextStyle(
                                    color: fiberchatGrey.withOpacity(0.7),
                                    fontStyle: FontStyle.italic,
                                    overflow: TextOverflow.ellipsis,
                                    fontSize: 13))
                          ]))
                  : SizedBox(height: 0, width: 0)
              : SizedBox(height: 0, width: 0),
          SizedBox(
            width: 200,
            height: 80,
            child: MultiPlayback(
              isMe: isMe,
              onTapDownloadFn: () async {
                await MobileDownloadService().download(
                    keyloader: _keyLoader,
                    url: message.split('-BREAK-')[0],
                    fileName:
                        'Recording_' + message.split('-BREAK-')[1] + '.mp3',
                    context: this.context,
                    isOpenAfterDownload: true);
              },
              url: message.split('-BREAK-')[0],
            ),
          )
        ],
      ),
    );
  }

  Widget getDocmessage(
      BuildContext context, Map<String, dynamic> doc, String message,
      {bool saved = false}) {
    final bool isMe = doc[Dbkeys.from] == widget.currentUserNo;
    return SizedBox(
      width: 220,
      height: 116,
      child: Column(
        crossAxisAlignment:
            isMe == true ? CrossAxisAlignment.start : CrossAxisAlignment.end,
        children: [
          doc.containsKey(Dbkeys.isForward) == true
              ? doc[Dbkeys.isForward] == true
                  ? Container(
                      margin: EdgeInsets.only(bottom: 10),
                      child: Row(
                          mainAxisAlignment: isMe == true
                              ? MainAxisAlignment.start
                              : MainAxisAlignment.end,
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(
                              FontAwesomeIcons.share,
                              size: 12,
                              color: fiberchatGrey.withOpacity(0.5),
                            ),
                            SizedBox(
                              width: 5,
                            ),
                            Text(getTranslated(this.context, 'forwarded'),
                                maxLines: 1,
                                style: TextStyle(
                                    color: fiberchatGrey.withOpacity(0.7),
                                    fontStyle: FontStyle.italic,
                                    overflow: TextOverflow.ellipsis,
                                    fontSize: 13))
                          ]))
                  : SizedBox(height: 0, width: 0)
              : SizedBox(height: 0, width: 0),
          ListTile(
            contentPadding: EdgeInsets.all(4),
            isThreeLine: false,
            leading: Container(
              decoration: BoxDecoration(
                color: Colors.yellow[800],
                borderRadius: BorderRadius.circular(7.0),
              ),
              padding: EdgeInsets.all(12),
              child: Icon(
                Icons.insert_drive_file,
                size: 25,
                color: Colors.white,
              ),
            ),
            title: Text(
              message.split('-BREAK-')[1],
              overflow: TextOverflow.ellipsis,
              maxLines: 2,
              style: TextStyle(
                  height: 1.4,
                  fontWeight: FontWeight.w700,
                  color: Colors.black87),
            ),
          ),
          Divider(
            height: 3,
          ),
          message.split('-BREAK-')[1].endsWith('.pdf')
              ? Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    // ignore: deprecated_member_use
                    FlatButton(
                        onPressed: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute<dynamic>(
                              builder: (_) => PDFViewerCachedFromUrl(
                                prefs: widget.prefs,
                                title: message.split('-BREAK-')[1],
                                url: message.split('-BREAK-')[0],
                                isregistered: true,
                              ),
                            ),
                          );
                        },
                        child: Text(getTranslated(this.context, 'preview'),
                            style: TextStyle(
                                fontWeight: FontWeight.w700,
                                color: Colors.blue[400]))),
                    // ignore: deprecated_member_use
                    FlatButton(
                        onPressed: () async {
                          await MobileDownloadService().download(
                              url: message.split('-BREAK-')[0],
                              fileName: message.split('-BREAK-')[1],
                              context: context,
                              keyloader: _keyLoader,
                              isOpenAfterDownload: true);
                        },
                        child: Text(getTranslated(this.context, 'download'),
                            style: TextStyle(
                                fontWeight: FontWeight.w700,
                                color: Colors.blue[400]))),
                  ],
                )
              //ignore: deprecated_member_use
              : FlatButton(
                  onPressed: () async {
                    await MobileDownloadService().download(
                        url: message.split('-BREAK-')[0],
                        fileName: message.split('-BREAK-')[1],
                        context: context,
                        keyloader: _keyLoader,
                        isOpenAfterDownload: true);
                  },
                  child: Text(getTranslated(this.context, 'download'),
                      style: TextStyle(
                          fontWeight: FontWeight.w700,
                          color: Colors.blue[400]))),
        ],
      ),
    );
  }

  Widget getImageMessage(Map<String, dynamic> doc, {bool saved = false}) {
    final bool isMe = doc[Dbkeys.from] == widget.currentUserNo;
    return Container(
      child: Column(
        crossAxisAlignment:
            isMe == true ? CrossAxisAlignment.start : CrossAxisAlignment.end,
        mainAxisAlignment: MainAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          doc.containsKey(Dbkeys.isForward) == true
              ? doc[Dbkeys.isForward] == true
                  ? Container(
                      margin: EdgeInsets.only(bottom: 10),
                      child: Row(
                          mainAxisAlignment: isMe == true
                              ? MainAxisAlignment.start
                              : MainAxisAlignment.end,
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(
                              FontAwesomeIcons.share,
                              size: 12,
                              color: fiberchatGrey.withOpacity(0.5),
                            ),
                            SizedBox(
                              width: 5,
                            ),
                            Text(getTranslated(this.context, 'forwarded'),
                                maxLines: 1,
                                style: TextStyle(
                                    color: fiberchatGrey.withOpacity(0.7),
                                    fontStyle: FontStyle.italic,
                                    overflow: TextOverflow.ellipsis,
                                    fontSize: 13))
                          ]))
                  : SizedBox(height: 0, width: 0)
              : SizedBox(height: 0, width: 0),
          saved
              ? Material(
                  child: Container(
                    decoration: BoxDecoration(
                      image: DecorationImage(
                          image: Save.getImageFromBase64(doc[Dbkeys.content])
                              .image,
                          fit: BoxFit.cover),
                    ),
                    width: doc[Dbkeys.content].contains('giphy') ? 120 : 200.0,
                    height: doc[Dbkeys.content].contains('giphy') ? 102 : 200.0,
                  ),
                  borderRadius: BorderRadius.all(
                    Radius.circular(8.0),
                  ),
                  clipBehavior: Clip.hardEdge,
                )
              : CachedNetworkImage(
                  placeholder: (context, url) => Container(
                    child: CircularProgressIndicator(
                      valueColor:
                          AlwaysStoppedAnimation<Color>(Colors.blueGrey[400]!),
                    ),
                    width: doc[Dbkeys.content].contains('giphy') ? 120 : 200.0,
                    height: doc[Dbkeys.content].contains('giphy') ? 120 : 200.0,
                    padding: EdgeInsets.all(80.0),
                    decoration: BoxDecoration(
                      color: Colors.blueGrey,
                      borderRadius: BorderRadius.all(
                        Radius.circular(8.0),
                      ),
                    ),
                  ),
                  errorWidget: (context, str, error) => Material(
                    child: Image.asset(
                      'assets/images/img_not_available.jpeg',
                      width:
                          doc[Dbkeys.content].contains('giphy') ? 120 : 200.0,
                      height:
                          doc[Dbkeys.content].contains('giphy') ? 120 : 200.0,
                      fit: BoxFit.cover,
                    ),
                    borderRadius: BorderRadius.all(
                      Radius.circular(8.0),
                    ),
                    clipBehavior: Clip.hardEdge,
                  ),
                  imageUrl: doc[Dbkeys.content],
                  width: doc[Dbkeys.content].contains('giphy') ? 120 : 200.0,
                  height: doc[Dbkeys.content].contains('giphy') ? 120 : 200.0,
                  fit: BoxFit.cover,
                ),
        ],
      ),
    );
  }

  Widget getTempImageMessage({String? url}) {
    return url == null
        ? Container(
            child: Image.file(
              pickedFile!,
              width: url!.contains('giphy') ? 120 : 200.0,
              height: url.contains('giphy') ? 120 : 200.0,
              fit: BoxFit.cover,
            ),
          )
        : getImageMessage({Dbkeys.content: url});
  }

  Widget getVideoMessage(
      BuildContext context, Map<String, dynamic> doc, String message,
      {bool saved = false}) {
    Map<dynamic, dynamic>? meta =
        jsonDecode((message.split('-BREAK-')[2]).toString());
    final bool isMe = doc[Dbkeys.from] == widget.currentUserNo;
    return InkWell(
      onTap: () {
        Navigator.push(
            this.context,
            new MaterialPageRoute(
                builder: (context) => new PreviewVideo(
                      isdownloadallowed: true,
                      filename: message.split('-BREAK-')[1],
                      id: null,
                      videourl: message.split('-BREAK-')[0],
                      aspectratio: meta!["width"] / meta["height"],
                    )));
      },
      child: Column(
        crossAxisAlignment:
            isMe == true ? CrossAxisAlignment.start : CrossAxisAlignment.end,
        children: [
          doc.containsKey(Dbkeys.isForward) == true
              ? doc[Dbkeys.isForward] == true
                  ? Container(
                      margin: EdgeInsets.only(bottom: 10),
                      child: Row(
                          mainAxisAlignment: isMe == true
                              ? MainAxisAlignment.start
                              : MainAxisAlignment.end,
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(
                              FontAwesomeIcons.share,
                              size: 12,
                              color: fiberchatGrey.withOpacity(0.5),
                            ),
                            SizedBox(
                              width: 5,
                            ),
                            Text(getTranslated(this.context, 'forwarded'),
                                maxLines: 1,
                                style: TextStyle(
                                    color: fiberchatGrey.withOpacity(0.7),
                                    fontStyle: FontStyle.italic,
                                    overflow: TextOverflow.ellipsis,
                                    fontSize: 13))
                          ]))
                  : SizedBox(height: 0, width: 0)
              : SizedBox(height: 0, width: 0),
          Container(
            color: Colors.blueGrey,
            height: 197,
            width: 197,
            child: Stack(
              children: [
                CachedNetworkImage(
                  placeholder: (context, url) => Container(
                    child: CircularProgressIndicator(
                      valueColor:
                          AlwaysStoppedAnimation<Color>(Colors.blueGrey[400]!),
                    ),
                    width: 197,
                    height: 197,
                    padding: EdgeInsets.all(80.0),
                    decoration: BoxDecoration(
                      color: Colors.blueGrey,
                      borderRadius: BorderRadius.all(
                        Radius.circular(0.0),
                      ),
                    ),
                  ),
                  errorWidget: (context, str, error) => Material(
                    child: Image.asset(
                      'assets/images/img_not_available.jpeg',
                      width: 197,
                      height: 197,
                      fit: BoxFit.cover,
                    ),
                    borderRadius: BorderRadius.all(
                      Radius.circular(0.0),
                    ),
                    clipBehavior: Clip.hardEdge,
                  ),
                  imageUrl: message.split('-BREAK-')[1],
                  width: 197,
                  height: 197,
                  fit: BoxFit.cover,
                ),
                Container(
                  color: Colors.black.withOpacity(0.4),
                  height: 197,
                  width: 197,
                ),
                Center(
                  child: Icon(Icons.play_circle_fill_outlined,
                      color: Colors.white70, size: 65),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget getContactMessage(
      BuildContext context, Map<String, dynamic> doc, String message,
      {bool saved = false}) {
    final bool isMe = doc[Dbkeys.from] == widget.currentUserNo;
    return SizedBox(
      width: 250,
      height: 130,
      child: Column(
        crossAxisAlignment:
            isMe == true ? CrossAxisAlignment.start : CrossAxisAlignment.end,
        children: [
          doc.containsKey(Dbkeys.isForward) == true
              ? doc[Dbkeys.isForward] == true
                  ? Container(
                      margin: EdgeInsets.only(bottom: 10),
                      child: Row(
                          mainAxisAlignment: isMe == true
                              ? MainAxisAlignment.start
                              : MainAxisAlignment.end,
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(
                              FontAwesomeIcons.share,
                              size: 12,
                              color: fiberchatGrey.withOpacity(0.5),
                            ),
                            SizedBox(
                              width: 5,
                            ),
                            Text(getTranslated(this.context, 'forwarded'),
                                maxLines: 1,
                                style: TextStyle(
                                    color: fiberchatGrey.withOpacity(0.7),
                                    fontStyle: FontStyle.italic,
                                    overflow: TextOverflow.ellipsis,
                                    fontSize: 13))
                          ]))
                  : SizedBox(height: 0, width: 0)
              : SizedBox(height: 0, width: 0),
          ListTile(
            isThreeLine: false,
            leading: customCircleAvatar(url: null),
            title: Text(
              message.split('-BREAK-')[0],
              overflow: TextOverflow.ellipsis,
              maxLines: 1,
              style: TextStyle(
                  height: 1.4,
                  fontWeight: FontWeight.w700,
                  color: Colors.blue[400]),
            ),
            subtitle: Padding(
              padding: const EdgeInsets.only(top: 3),
              child: Text(
                message.split('-BREAK-')[1],
                style: TextStyle(
                    height: 1.4,
                    fontWeight: FontWeight.w500,
                    color: Colors.black87),
              ),
            ),
          ),
          Divider(
            height: 7,
          ),
          // ignore: deprecated_member_use
          FlatButton(
              onPressed: () async {
                String peer = message.split('-BREAK-')[1];
                String? peerphone;
                bool issearching = true;
                bool issearchraw = false;
                bool isUser = false;
                String? formattedphone;

                setStateIfMounted(() {
                  peerphone = peer.replaceAll(new RegExp(r'-'), '');
                  peerphone!.trim();
                });

                formattedphone = peerphone;

                if (!peerphone!.startsWith('+')) {
                  if ((peerphone!.length > 11)) {
                    CountryCodes.forEach((code) {
                      if (peerphone!.startsWith(code) && issearching == true) {
                        setStateIfMounted(() {
                          formattedphone = peerphone!
                              .substring(code.length, peerphone!.length);
                          issearchraw = true;
                          issearching = false;
                        });
                      }
                    });
                  } else {
                    setStateIfMounted(() {
                      setStateIfMounted(() {
                        issearchraw = true;
                        formattedphone = peerphone;
                      });
                    });
                  }
                } else {
                  setStateIfMounted(() {
                    issearchraw = false;
                    formattedphone = peerphone;
                  });
                }

                Query<Map<String, dynamic>> query = issearchraw == true
                    ? FirebaseFirestore.instance
                        .collection(DbPaths.collectionusers)
                        .where(Dbkeys.phoneRaw,
                            isEqualTo: formattedphone ?? peerphone)
                        .limit(1)
                    : FirebaseFirestore.instance
                        .collection(DbPaths.collectionusers)
                        .where(Dbkeys.phone,
                            isEqualTo: formattedphone ?? peerphone)
                        .limit(1);

                await query.get().then((user) {
                  setStateIfMounted(() {
                    isUser = user.docs.length == 0 ? false : true;
                  });
                  if (isUser) {
                    Map<String, dynamic> peer = user.docs[0].data();
                    widget.model.addUser(user.docs[0]);
                    Navigator.pushReplacement(
                        context,
                        new MaterialPageRoute(
                            builder: (context) => new ChatScreen(
                                isSharingIntentForwarded: false,
                                prefs: widget.prefs,
                                unread: 0,
                                currentUserNo: widget.currentUserNo,
                                model: widget.model,
                                peerNo: peer[Dbkeys.phone])));
                  } else {
                    Query<Map<String, dynamic>> queryretrywithoutzero =
                        issearchraw == true
                            ? FirebaseFirestore.instance
                                .collection(DbPaths.collectionusers)
                                .where(Dbkeys.phoneRaw,
                                    isEqualTo: formattedphone == null
                                        ? peerphone!
                                            .substring(1, peerphone!.length)
                                        : formattedphone!.substring(
                                            1, formattedphone!.length))
                                .limit(1)
                            : FirebaseFirestore.instance
                                .collection(DbPaths.collectionusers)
                                .where(Dbkeys.phoneRaw,
                                    isEqualTo: formattedphone == null
                                        ? peerphone!
                                            .substring(1, peerphone!.length)
                                        : formattedphone!.substring(
                                            1, formattedphone!.length))
                                .limit(1);
                    queryretrywithoutzero.get().then((user) {
                      setStateIfMounted(() {
                        isLoading = false;
                        isUser = user.docs.length == 0 ? false : true;
                      });
                      if (isUser) {
                        Map<String, dynamic> peer = user.docs[0].data();
                        widget.model.addUser(user.docs[0]);
                        Navigator.pushReplacement(
                            context,
                            new MaterialPageRoute(
                                builder: (context) => new ChatScreen(
                                    isSharingIntentForwarded: true,
                                    prefs: widget.prefs,
                                    unread: 0,
                                    currentUserNo: widget.currentUserNo,
                                    model: widget.model,
                                    peerNo: peer[Dbkeys.phone])));
                      }
                    });
                  }
                });

                // ignore: unnecessary_null_comparison
                if (isUser == null || isUser == false) {
                  Fiberchat.toast(getTranslated(this.context, 'usernotjoined') +
                      ' $Appname');
                }
              },
              child: Text(getTranslated(this.context, 'msg'),
                  style: TextStyle(
                      fontWeight: FontWeight.w700, color: Colors.blue[400])))
        ],
      ),
    );
  }

  replyAttachedWidget(BuildContext context, var doc) {
    return Flexible(
      child: Container(
          // width: 280,
          height: 70,
          margin: EdgeInsets.only(left: 0, right: 0),
          decoration: BoxDecoration(
              color: fiberchatWhite.withOpacity(0.55),
              borderRadius: BorderRadius.all(Radius.circular(10))),
          child: Stack(
            children: [
              Container(
                  margin: EdgeInsetsDirectional.all(4),
                  decoration: BoxDecoration(
                      color: fiberchatGrey.withOpacity(0.1),
                      borderRadius: BorderRadius.all(Radius.circular(8))),
                  child: Row(children: [
                    Container(
                      decoration: BoxDecoration(
                        color: doc[Dbkeys.from] == widget.currentUserNo
                            ? fiberchatgreen
                            : Colors.purple,
                        borderRadius: BorderRadius.only(
                            topRight: Radius.circular(0),
                            bottomRight: Radius.circular(0),
                            topLeft: Radius.circular(10),
                            bottomLeft: Radius.circular(10)),
                      ),
                      height: 75,
                      width: 3.3,
                    ),
                    SizedBox(
                      width: 5,
                    ),
                    Expanded(
                        child: Container(
                      padding: EdgeInsetsDirectional.all(5),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Padding(
                            padding: EdgeInsets.only(right: 30),
                            child: Text(
                              doc[Dbkeys.from] == widget.currentUserNo
                                  ? getTranslated(this.context, 'you')
                                  : Fiberchat.getNickname(peer!)!,
                              overflow: TextOverflow.ellipsis,
                              style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color:
                                      doc[Dbkeys.from] == widget.currentUserNo
                                          ? fiberchatgreen
                                          : Colors.purple),
                            ),
                          ),
                          SizedBox(
                            height: 8,
                          ),
                          doc[Dbkeys.messageType] == MessageType.text.index
                              ? Text(
                                  doc[Dbkeys.content],
                                  overflow: TextOverflow.ellipsis,
                                  // textAlign:  doc[Dbkeys.from] == currentUserNo? TextAlign.end: TextAlign.start,
                                  maxLines: 2,
                                )
                              : doc[Dbkeys.messageType] == MessageType.doc.index
                                  ? Container(
                                      padding: const EdgeInsets.only(right: 70),
                                      child: Text(
                                        doc[Dbkeys.content].split('-BREAK-')[1],
                                        overflow: TextOverflow.ellipsis,
                                        maxLines: 1,
                                      ),
                                    )
                                  : Text(
                                      getTranslated(
                                          this.context,
                                          doc[Dbkeys.messageType] ==
                                                  MessageType.image.index
                                              ? 'nim'
                                              : doc[Dbkeys.messageType] ==
                                                      MessageType.video.index
                                                  ? 'nvm'
                                                  : doc[Dbkeys.messageType] ==
                                                          MessageType
                                                              .audio.index
                                                      ? 'nam'
                                                      : doc[Dbkeys.messageType] ==
                                                              MessageType
                                                                  .contact.index
                                                          ? 'ncm'
                                                          : doc[Dbkeys.messageType] ==
                                                                  MessageType
                                                                      .location
                                                                      .index
                                                              ? 'nlm'
                                                              : doc[Dbkeys.messageType] ==
                                                                      MessageType
                                                                          .doc
                                                                          .index
                                                                  ? 'ndm'
                                                                  : ''),
                                      overflow: TextOverflow.ellipsis,
                                    ),
                        ],
                      ),
                    ))
                  ])),
              doc[Dbkeys.messageType] == MessageType.text.index ||
                      doc[Dbkeys.messageType] == MessageType.location.index
                  ? SizedBox(
                      width: 0,
                      height: 0,
                    )
                  : doc[Dbkeys.messageType] == MessageType.image.index
                      ? Positioned(
                          right: -2,
                          top: -2,
                          child: Container(
                            width: 74.0,
                            height: 74.0,
                            padding: EdgeInsetsDirectional.all(6),
                            child: ClipRRect(
                              borderRadius: BorderRadius.only(
                                  topRight: Radius.circular(5),
                                  bottomRight: Radius.circular(5),
                                  topLeft: Radius.circular(0),
                                  bottomLeft: Radius.circular(0)),
                              child: CachedNetworkImage(
                                placeholder: (context, url) => Container(
                                  child: CircularProgressIndicator(
                                    valueColor: AlwaysStoppedAnimation<Color>(
                                        fiberchatBlue),
                                  ),
                                  width: doc[Dbkeys.content].contains('giphy')
                                      ? 60
                                      : 60.0,
                                  height: doc[Dbkeys.content].contains('giphy')
                                      ? 60
                                      : 60.0,
                                  padding: EdgeInsets.all(8.0),
                                  decoration: BoxDecoration(
                                    color: Colors.blueGrey[200],
                                    borderRadius: BorderRadius.all(
                                      Radius.circular(8.0),
                                    ),
                                  ),
                                ),
                                errorWidget: (context, str, error) => Material(
                                  child: Image.asset(
                                    'assets/images/img_not_available.jpeg',
                                    width: 60.0,
                                    height: 60,
                                    fit: BoxFit.cover,
                                  ),
                                  borderRadius: BorderRadius.all(
                                    Radius.circular(8.0),
                                  ),
                                  clipBehavior: Clip.hardEdge,
                                ),
                                imageUrl: doc[Dbkeys.messageType] ==
                                        MessageType.video.index
                                    ? ''
                                    : doc[Dbkeys.content],
                                width: 60,
                                height: 60,
                                fit: BoxFit.cover,
                              ),
                            ),
                          ),
                        )
                      : doc[Dbkeys.messageType] == MessageType.video.index
                          ? Positioned(
                              right: -2,
                              top: -2,
                              child: Container(
                                  width: 74.0,
                                  height: 74.0,
                                  padding: EdgeInsetsDirectional.all(6),
                                  child: ClipRRect(
                                      borderRadius: BorderRadius.only(
                                          topRight: Radius.circular(5),
                                          bottomRight: Radius.circular(5),
                                          topLeft: Radius.circular(0),
                                          bottomLeft: Radius.circular(0)),
                                      child: Container(
                                        color: Colors.blueGrey[200],
                                        height: 74,
                                        width: 74,
                                        child: Stack(
                                          children: [
                                            CachedNetworkImage(
                                              placeholder: (context, url) =>
                                                  Container(
                                                child:
                                                    CircularProgressIndicator(
                                                  valueColor:
                                                      AlwaysStoppedAnimation<
                                                          Color>(fiberchatBlue),
                                                ),
                                                width: 74,
                                                height: 74,
                                                padding: EdgeInsets.all(8.0),
                                                decoration: BoxDecoration(
                                                  color: Colors.blueGrey[200],
                                                  borderRadius:
                                                      BorderRadius.all(
                                                    Radius.circular(0.0),
                                                  ),
                                                ),
                                              ),
                                              errorWidget:
                                                  (context, str, error) =>
                                                      Material(
                                                child: Image.asset(
                                                  'assets/images/img_not_available.jpeg',
                                                  width: 60,
                                                  height: 60,
                                                  fit: BoxFit.cover,
                                                ),
                                                borderRadius: BorderRadius.all(
                                                  Radius.circular(0.0),
                                                ),
                                                clipBehavior: Clip.hardEdge,
                                              ),
                                              imageUrl: doc[Dbkeys.content]
                                                  .split('-BREAK-')[1],
                                              width: 74,
                                              height: 74,
                                              fit: BoxFit.cover,
                                            ),
                                            Container(
                                              color:
                                                  Colors.black.withOpacity(0.4),
                                              height: 74,
                                              width: 74,
                                            ),
                                            Center(
                                              child: Icon(
                                                  Icons
                                                      .play_circle_fill_outlined,
                                                  color: Colors.white70,
                                                  size: 25),
                                            ),
                                          ],
                                        ),
                                      ))))
                          : Positioned(
                              right: -2,
                              top: -2,
                              child: Container(
                                  width: 74.0,
                                  height: 74.0,
                                  padding: EdgeInsetsDirectional.all(6),
                                  child: ClipRRect(
                                      borderRadius: BorderRadius.only(
                                          topRight: Radius.circular(5),
                                          bottomRight: Radius.circular(5),
                                          topLeft: Radius.circular(0),
                                          bottomLeft: Radius.circular(0)),
                                      child: Container(
                                          color: doc[Dbkeys.messageType] ==
                                                  MessageType.doc.index
                                              ? Colors.yellow[800]
                                              : doc[Dbkeys.messageType] ==
                                                      MessageType.audio.index
                                                  ? Colors.green[400]
                                                  : doc[Dbkeys.messageType] ==
                                                          MessageType
                                                              .location.index
                                                      ? Colors.red[700]
                                                      : doc[Dbkeys.messageType] ==
                                                              MessageType
                                                                  .contact.index
                                                          ? Colors.blue[400]
                                                          : Colors.cyan[700],
                                          height: 74,
                                          width: 74,
                                          child: Icon(
                                            doc[Dbkeys.messageType] ==
                                                    MessageType.doc.index
                                                ? Icons.insert_drive_file
                                                : doc[Dbkeys.messageType] ==
                                                        MessageType.audio.index
                                                    ? Icons.mic_rounded
                                                    : doc[Dbkeys.messageType] ==
                                                            MessageType
                                                                .location.index
                                                        ? Icons.location_on
                                                        : doc[Dbkeys.messageType] ==
                                                                MessageType
                                                                    .contact
                                                                    .index
                                                            ? Icons
                                                                .contact_page_sharp
                                                            : Icons
                                                                .insert_drive_file,
                                            color: Colors.white,
                                            size: 35,
                                          ))))),
            ],
          )),
    );
  }

  Widget buildReplyMessageForInput(
    BuildContext context,
  ) {
    return Flexible(
      child: Container(
          height: 80,
          margin: EdgeInsets.only(left: 15, right: 70),
          decoration: BoxDecoration(
              color: fiberchatWhite,
              borderRadius: BorderRadius.all(Radius.circular(10))),
          child: Stack(
            children: [
              Container(
                  margin: EdgeInsetsDirectional.all(4),
                  decoration: BoxDecoration(
                      color: fiberchatGrey.withOpacity(0.1),
                      borderRadius: BorderRadius.all(Radius.circular(8))),
                  child: Row(children: [
                    Container(
                      decoration: BoxDecoration(
                        color: replyDoc![Dbkeys.from] == widget.currentUserNo
                            ? fiberchatgreen
                            : Colors.purple,
                        borderRadius: BorderRadius.only(
                            topRight: Radius.circular(0),
                            bottomRight: Radius.circular(0),
                            topLeft: Radius.circular(10),
                            bottomLeft: Radius.circular(10)),
                      ),
                      height: 75,
                      width: 3.3,
                    ),
                    SizedBox(
                      width: 5,
                    ),
                    Expanded(
                        child: Container(
                      padding: EdgeInsetsDirectional.all(5),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Padding(
                            padding: EdgeInsets.only(right: 30),
                            child: Text(
                              replyDoc![Dbkeys.from] == widget.currentUserNo
                                  ? getTranslated(this.context, 'you')
                                  : Fiberchat.getNickname(peer!)!,
                              overflow: TextOverflow.ellipsis,
                              style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: replyDoc![Dbkeys.from] ==
                                          widget.currentUserNo
                                      ? fiberchatgreen
                                      : Colors.purple),
                            ),
                          ),
                          SizedBox(
                            height: 8,
                          ),
                          replyDoc![Dbkeys.messageType] ==
                                  MessageType.text.index
                              ? Text(
                                  replyDoc![Dbkeys.content],
                                  overflow: TextOverflow.ellipsis,
                                  maxLines: 2,
                                )
                              : replyDoc![Dbkeys.messageType] ==
                                      MessageType.doc.index
                                  ? Container(
                                      width: MediaQuery.of(context).size.width -
                                          125,
                                      padding: const EdgeInsets.only(right: 55),
                                      child: Text(
                                        replyDoc![Dbkeys.content]
                                            .split('-BREAK-')[1],
                                        overflow: TextOverflow.ellipsis,
                                        maxLines: 2,
                                      ),
                                    )
                                  : Text(
                                      getTranslated(
                                          this.context,
                                          replyDoc![Dbkeys.messageType] ==
                                                  MessageType.image.index
                                              ? 'nim'
                                              : replyDoc![Dbkeys.messageType] ==
                                                      MessageType.video.index
                                                  ? 'nvm'
                                                  : replyDoc![Dbkeys
                                                              .messageType] ==
                                                          MessageType
                                                              .audio.index
                                                      ? 'nam'
                                                      : replyDoc![Dbkeys
                                                                  .messageType] ==
                                                              MessageType
                                                                  .contact.index
                                                          ? 'ncm'
                                                          : replyDoc![Dbkeys
                                                                      .messageType] ==
                                                                  MessageType
                                                                      .location
                                                                      .index
                                                              ? 'nlm'
                                                              : replyDoc![Dbkeys
                                                                          .messageType] ==
                                                                      MessageType
                                                                          .doc
                                                                          .index
                                                                  ? 'ndm'
                                                                  : ''),
                                      overflow: TextOverflow.ellipsis,
                                      maxLines: 2,
                                    ),
                        ],
                      ),
                    ))
                  ])),
              replyDoc![Dbkeys.messageType] == MessageType.text.index
                  ? SizedBox(
                      width: 0,
                      height: 0,
                    )
                  : replyDoc![Dbkeys.messageType] == MessageType.image.index
                      ? Positioned(
                          right: -2,
                          top: -2,
                          child: Container(
                            width: 84.0,
                            height: 84.0,
                            padding: EdgeInsetsDirectional.all(6),
                            child: ClipRRect(
                              borderRadius: BorderRadius.only(
                                  topRight: Radius.circular(5),
                                  bottomRight: Radius.circular(5),
                                  topLeft: Radius.circular(0),
                                  bottomLeft: Radius.circular(0)),
                              child: CachedNetworkImage(
                                placeholder: (context, url) => Container(
                                  child: CircularProgressIndicator(
                                    valueColor: AlwaysStoppedAnimation<Color>(
                                        fiberchatBlue),
                                  ),
                                  width: replyDoc![Dbkeys.content]
                                          .contains('giphy')
                                      ? 60
                                      : 60.0,
                                  height: replyDoc![Dbkeys.content]
                                          .contains('giphy')
                                      ? 60
                                      : 60.0,
                                  padding: EdgeInsets.all(8.0),
                                  decoration: BoxDecoration(
                                    color: Colors.blueGrey[200],
                                    borderRadius: BorderRadius.all(
                                      Radius.circular(8.0),
                                    ),
                                  ),
                                ),
                                errorWidget: (context, str, error) => Material(
                                  child: Image.asset(
                                    'assets/images/img_not_available.jpeg',
                                    width: 60.0,
                                    height: 60,
                                    fit: BoxFit.cover,
                                  ),
                                  borderRadius: BorderRadius.all(
                                    Radius.circular(8.0),
                                  ),
                                  clipBehavior: Clip.hardEdge,
                                ),
                                imageUrl: replyDoc![Dbkeys.messageType] ==
                                        MessageType.video.index
                                    ? ''
                                    : replyDoc![Dbkeys.content],
                                width: 60,
                                height: 60,
                                fit: BoxFit.cover,
                              ),
                            ),
                          ),
                        )
                      : replyDoc![Dbkeys.messageType] == MessageType.video.index
                          ? Positioned(
                              right: -2,
                              top: -2,
                              child: Container(
                                  width: 84.0,
                                  height: 84.0,
                                  padding: EdgeInsetsDirectional.all(6),
                                  child: ClipRRect(
                                      borderRadius: BorderRadius.only(
                                          topRight: Radius.circular(5),
                                          bottomRight: Radius.circular(5),
                                          topLeft: Radius.circular(0),
                                          bottomLeft: Radius.circular(0)),
                                      child: Container(
                                        color: Colors.blueGrey[200],
                                        height: 84,
                                        width: 84,
                                        child: Stack(
                                          children: [
                                            CachedNetworkImage(
                                              placeholder: (context, url) =>
                                                  Container(
                                                child:
                                                    CircularProgressIndicator(
                                                  valueColor:
                                                      AlwaysStoppedAnimation<
                                                          Color>(fiberchatBlue),
                                                ),
                                                width: 84,
                                                height: 84,
                                                padding: EdgeInsets.all(8.0),
                                                decoration: BoxDecoration(
                                                  color: Colors.blueGrey[200],
                                                  borderRadius:
                                                      BorderRadius.all(
                                                    Radius.circular(0.0),
                                                  ),
                                                ),
                                              ),
                                              errorWidget:
                                                  (context, str, error) =>
                                                      Material(
                                                child: Image.asset(
                                                  'assets/images/img_not_available.jpeg',
                                                  width: 60,
                                                  height: 60,
                                                  fit: BoxFit.cover,
                                                ),
                                                borderRadius: BorderRadius.all(
                                                  Radius.circular(0.0),
                                                ),
                                                clipBehavior: Clip.hardEdge,
                                              ),
                                              imageUrl:
                                                  replyDoc![Dbkeys.content]
                                                      .split('-BREAK-')[1],
                                              width: 84,
                                              height: 84,
                                              fit: BoxFit.cover,
                                            ),
                                            Container(
                                              color:
                                                  Colors.black.withOpacity(0.4),
                                              height: 84,
                                              width: 84,
                                            ),
                                            Center(
                                              child: Icon(
                                                  Icons
                                                      .play_circle_fill_outlined,
                                                  color: Colors.white70,
                                                  size: 25),
                                            ),
                                          ],
                                        ),
                                      ))))
                          : Positioned(
                              right: -2,
                              top: -2,
                              child: Container(
                                  width: 84.0,
                                  height: 84.0,
                                  padding: EdgeInsetsDirectional.all(6),
                                  child: ClipRRect(
                                      borderRadius: BorderRadius.only(
                                          topRight: Radius.circular(5),
                                          bottomRight: Radius.circular(5),
                                          topLeft: Radius.circular(0),
                                          bottomLeft: Radius.circular(0)),
                                      child: Container(
                                          color: replyDoc![
                                                      Dbkeys.messageType] ==
                                                  MessageType.doc.index
                                              ? Colors.yellow[800]
                                              : replyDoc![Dbkeys.messageType] ==
                                                      MessageType.audio.index
                                                  ? Colors.green[400]
                                                  : replyDoc![Dbkeys
                                                              .messageType] ==
                                                          MessageType
                                                              .location.index
                                                      ? Colors.red[700]
                                                      : replyDoc![Dbkeys
                                                                  .messageType] ==
                                                              MessageType
                                                                  .contact.index
                                                          ? Colors.blue[400]
                                                          : Colors.cyan[700],
                                          height: 84,
                                          width: 84,
                                          child: Icon(
                                            replyDoc![Dbkeys.messageType] ==
                                                    MessageType.doc.index
                                                ? Icons.insert_drive_file
                                                : replyDoc![Dbkeys
                                                            .messageType] ==
                                                        MessageType.audio.index
                                                    ? Icons.mic_rounded
                                                    : replyDoc![Dbkeys
                                                                .messageType] ==
                                                            MessageType
                                                                .location.index
                                                        ? Icons.location_on
                                                        : replyDoc![Dbkeys
                                                                    .messageType] ==
                                                                MessageType
                                                                    .contact
                                                                    .index
                                                            ? Icons
                                                                .contact_page_sharp
                                                            : Icons
                                                                .insert_drive_file,
                                            color: Colors.white,
                                            size: 35,
                                          ))))),
              Positioned(
                right: 7,
                top: 7,
                child: InkWell(
                  onTap: () {
                    setStateIfMounted(() {
                      HapticFeedback.heavyImpact();
                      isReplyKeyboard = false;
                      hidekeyboard(context);
                    });
                  },
                  child: Container(
                    width: 15,
                    height: 15,
                    decoration: new BoxDecoration(
                      color: Colors.white.withOpacity(0.9),
                      shape: BoxShape.circle,
                    ),
                    child: new Icon(
                      Icons.close,
                      color: Colors.blueGrey,
                      size: 13,
                    ),
                  ),
                ),
              )
            ],
          )),
    );
  }

  Widget buildTempMessage(BuildContext context, MessageType type, content,
      timestamp, delivered, tempDoc) {
    final observer = Provider.of<Observer>(this.context, listen: false);
    final bool isMe = true;
    return SeenProvider(
        timestamp: timestamp.toString(),
        data: seenState,
        child: Bubble(
          isURLtext: tempDoc[Dbkeys.messageType] == MessageType.text.index &&
              Uri.parse(tempDoc[Dbkeys.content]!).isAbsolute,
          mssgDoc: tempDoc,
          is24hrsFormat: observer.is24hrsTimeformat,
          isMssgDeleted: ((tempDoc.containsKey(Dbkeys.hasRecipientDeleted) &&
                      tempDoc.containsKey(Dbkeys.hasSenderDeleted)) ==
                  true)
              ? (isMe == true
                  ? (tempDoc[Dbkeys.from] == widget.currentUserNo
                      ? tempDoc[Dbkeys.hasSenderDeleted]
                      : false)
                  : (tempDoc[Dbkeys.from] != widget.currentUserNo
                      ? tempDoc[Dbkeys.hasRecipientDeleted]
                      : false))
              : false,
          isBroadcastMssg: false,
          messagetype: type,
          child: type == MessageType.text
              ? getTextMessage(content, tempDoc)
              : type == MessageType.location
                  ? getLocationMessage(tempDoc, content, saved: false)
                  : type == MessageType.doc
                      ? getDocmessage(context, tempDoc, content, saved: false)
                      : type == MessageType.audio
                          ? getAudiomessage(context, tempDoc, content,
                              saved: false, isMe: isMe)
                          : type == MessageType.video
                              ? getVideoMessage(this.context, tempDoc, content,
                                  saved: false)
                              : type == MessageType.contact
                                  ? getContactMessage(context, tempDoc, content,
                                      saved: false)
                                  : getTempImageMessage(url: content),
          isMe: isMe,
          timestamp: timestamp,
          delivered: delivered,
          isContinuing: true,
          // messages.isNotEmpty && messages.last.from == currentUserNo,
        ));
  }

  Widget buildLoading() {
    return Positioned(
      child: isLoading
          ? Container(
              child: Center(
                child: CircularProgressIndicator(
                    valueColor: AlwaysStoppedAnimation<Color>(fiberchatBlue)),
              ),
              color: DESIGN_TYPE == Themetype.whatsapp
                  ? fiberchatBlack.withOpacity(0.6)
                  : fiberchatWhite.withOpacity(0.6),
            )
          : Container(),
    );
  }

  shareMedia(BuildContext context) {
    showModalBottomSheet(
        context: context,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(25.0)),
        ),
        builder: (BuildContext context) {
          // return your layout
          return Container(
            padding: EdgeInsets.all(12),
            height: 250,
            child: Column(children: [
              SizedBox(
                height: 20,
              ),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  SizedBox(
                    width: MediaQuery.of(context).size.width / 3.27,
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.start,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        RawMaterialButton(
                          disabledElevation: 0,
                          onPressed: () {
                            hidekeyboard(context);
                            Navigator.of(context).pop();
                            Navigator.push(
                                context,
                                MaterialPageRoute(
                                    builder: (context) => MultiDocumentPicker(
                                          title: getTranslated(
                                              this.context, 'pickdoc'),
                                          callback: getFileData,
                                          writeMessage:
                                              (String? url, int time) async {
                                            if (url != null) {
                                              String finalUrl = url +
                                                  '-BREAK-' +
                                                  basename(pickedFile!.path)
                                                      .toString();
                                              onSendMessage(
                                                  this.context,
                                                  finalUrl,
                                                  MessageType.doc,
                                                  time);
                                            }
                                          },
                                        )));
                          },
                          elevation: .5,
                          fillColor: Colors.indigo,
                          child: Icon(
                            Icons.file_copy,
                            size: 25.0,
                            color: Colors.white,
                          ),
                          padding: EdgeInsets.all(15.0),
                          shape: CircleBorder(),
                        ),
                        SizedBox(
                          height: 8,
                        ),
                        Text(
                          getTranslated(this.context, 'doc'),
                          textAlign: TextAlign.center,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style:
                              TextStyle(color: Colors.grey[700], fontSize: 14),
                        )
                      ],
                    ),
                  ),
                  SizedBox(
                    width: MediaQuery.of(context).size.width / 3.27,
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.start,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        RawMaterialButton(
                          disabledElevation: 0,
                          onPressed: () {
                            hidekeyboard(context);
                            Navigator.of(context).pop();
                            Navigator.push(
                                context,
                                MaterialPageRoute(
                                    builder: (context) => HybridVideoPicker(
                                          title: getTranslated(
                                              this.context, 'pickvideo'),
                                          callback: getFileData,
                                        ))).then((url) async {
                              if (url != null) {
                                Fiberchat.toast(
                                  getTranslated(this.context, 'plswait'),
                                );
                                String thumbnailurl = await getThumbnail(url);
                                onSendMessage(
                                    context,
                                    url +
                                        '-BREAK-' +
                                        thumbnailurl +
                                        '-BREAK-' +
                                        videometadata,
                                    MessageType.video,
                                    thumnailtimestamp);
                                Fiberchat.toast(
                                    getTranslated(this.context, 'sent'));
                              } else {}
                            });
                          },
                          elevation: .5,
                          fillColor: Colors.pink[600],
                          child: Icon(
                            Icons.video_collection_sharp,
                            size: 25.0,
                            color: Colors.white,
                          ),
                          padding: EdgeInsets.all(15.0),
                          shape: CircleBorder(),
                        ),
                        SizedBox(
                          height: 8,
                        ),
                        Text(
                          getTranslated(this.context, 'video'),
                          textAlign: TextAlign.center,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style:
                              TextStyle(color: Colors.grey[700], fontSize: 14),
                        )
                      ],
                    ),
                  ),
                  SizedBox(
                    width: MediaQuery.of(context).size.width / 3.27,
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.start,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        RawMaterialButton(
                          disabledElevation: 0,
                          onPressed: () {
                            hidekeyboard(context);
                            Navigator.of(context).pop();
                            Navigator.push(
                                context,
                                MaterialPageRoute(
                                    builder: (context) => MultiImagePicker(
                                          title: getTranslated(
                                              this.context, 'pickimage'),
                                          callback: getFileData,
                                          writeMessage:
                                              (String? url, int time) async {
                                            if (url != null) {
                                              onSendMessage(this.context, url,
                                                  MessageType.image, time);
                                            }
                                          },
                                        )));
                          },
                          elevation: .5,
                          fillColor: Colors.purple,
                          child: Icon(
                            Icons.image_rounded,
                            size: 25.0,
                            color: Colors.white,
                          ),
                          padding: EdgeInsets.all(15.0),
                          shape: CircleBorder(),
                        ),
                        SizedBox(
                          height: 8,
                        ),
                        Text(
                          getTranslated(this.context, 'image'),
                          textAlign: TextAlign.center,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style:
                              TextStyle(color: Colors.grey[700], fontSize: 14),
                        )
                      ],
                    ),
                  )
                ],
              ),
              SizedBox(
                height: 20,
              ),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  SizedBox(
                    width: MediaQuery.of(context).size.width / 3.27,
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.start,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        RawMaterialButton(
                          disabledElevation: 0,
                          onPressed: () {
                            hidekeyboard(context);

                            Navigator.of(context).pop();
                            Navigator.push(
                                context,
                                MaterialPageRoute(
                                    builder: (context) => AudioRecord(
                                          title: getTranslated(
                                              this.context, 'record'),
                                          callback: getFileData,
                                        ))).then((url) {
                              if (url != null) {
                                onSendMessage(
                                    context,
                                    url +
                                        '-BREAK-' +
                                        uploadTimestamp.toString(),
                                    MessageType.audio,
                                    uploadTimestamp);
                              } else {}
                            });
                          },
                          elevation: .5,
                          fillColor: Colors.yellow[900],
                          child: Icon(
                            Icons.mic_rounded,
                            size: 25.0,
                            color: Colors.white,
                          ),
                          padding: EdgeInsets.all(15.0),
                          shape: CircleBorder(),
                        ),
                        SizedBox(
                          height: 8,
                        ),
                        Text(
                          getTranslated(this.context, 'audio'),
                          textAlign: TextAlign.center,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style: TextStyle(color: Colors.grey[700]),
                        )
                      ],
                    ),
                  ),
                  SizedBox(
                    width: MediaQuery.of(context).size.width / 3.27,
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.start,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        RawMaterialButton(
                          disabledElevation: 0,
                          onPressed: () async {
                            hidekeyboard(context);
                            Navigator.of(context).pop();
                            await _determinePosition().then(
                              (location) async {
                                var locationstring =
                                    'https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}';
                                onSendMessage(
                                    context,
                                    locationstring,
                                    MessageType.location,
                                    DateTime.now().millisecondsSinceEpoch);
                                setStateIfMounted(() {});
                                Fiberchat.toast(
                                  getTranslated(this.context, 'sent'),
                                );
                              },
                            );
                          },
                          elevation: .5,
                          fillColor: Colors.cyan[700],
                          child: Icon(
                            Icons.location_on,
                            size: 25.0,
                            color: Colors.white,
                          ),
                          padding: EdgeInsets.all(15.0),
                          shape: CircleBorder(),
                        ),
                        SizedBox(
                          height: 8,
                        ),
                        Text(
                          getTranslated(this.context, 'location'),
                          textAlign: TextAlign.center,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style: TextStyle(color: Colors.grey[700]),
                        )
                      ],
                    ),
                  ),
                  SizedBox(
                    width: MediaQuery.of(context).size.width / 3.27,
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.start,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        RawMaterialButton(
                          disabledElevation: 0,
                          onPressed: () async {
                            hidekeyboard(context);
                            Navigator.of(context).pop();
                            await Navigator.push(
                                context,
                                MaterialPageRoute(
                                    builder: (context) => ContactsSelect(
                                        currentUserNo: widget.currentUserNo,
                                        model: widget.model,
                                        biometricEnabled: false,
                                        prefs: widget.prefs,
                                        onSelect: (name, phone) {
                                          onSendMessage(
                                              context,
                                              '$name-BREAK-$phone',
                                              MessageType.contact,
                                              DateTime.now()
                                                  .millisecondsSinceEpoch);
                                        })));
                          },
                          elevation: .5,
                          fillColor: Colors.blue[800],
                          child: Icon(
                            Icons.person,
                            size: 25.0,
                            color: Colors.white,
                          ),
                          padding: EdgeInsets.all(15.0),
                          shape: CircleBorder(),
                        ),
                        SizedBox(
                          height: 8,
                        ),
                        Text(
                          getTranslated(this.context, 'contact'),
                          textAlign: TextAlign.center,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style: TextStyle(color: Colors.grey[700]),
                        )
                      ],
                    ),
                  )
                ],
              ),
            ]),
          );
        });
  }

  Future<bool> onWillPop() {
    if (isemojiShowing == true) {
      setStateIfMounted(() {
        isemojiShowing = false;
      });
      Future.value(false);
    } else {
      setLastSeen();
      return Future.value(true);
    }
    return Future.value(false);
  }

  bool isemojiShowing = false;

  showDialOptions(BuildContext context) {
    showModalBottomSheet(
        context: context,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(25.0)),
        ),
        builder: (BuildContext context) {
          // return your layout
          return Consumer<Observer>(
              builder: (context, observer, _child) => Container(
                  padding: EdgeInsets.all(12),
                  height: 130,
                  child: Row(
                      crossAxisAlignment: CrossAxisAlignment.center,
                      mainAxisAlignment: MainAxisAlignment.spaceAround,
                      children: [
                        InkWell(
                          onTap: observer.iscallsallowed == false
                              ? () {
                                  Navigator.of(this.context).pop();
                                  Fiberchat.showRationale(getTranslated(
                                      this.context, 'callnotallowed'));
                                }
                              : hasPeerBlockedMe == true
                                  ? () {
                                      Navigator.of(this.context).pop();
                                      Fiberchat.toast(
                                        getTranslated(
                                            context, 'userhasblocked'),
                                      );
                                    }
                                  : () async {
                                      final observer = Provider.of<Observer>(
                                          this.context,
                                          listen: false);
                                      if (IsInterstitialAdShow == true &&
                                          observer.isadmobshow == true) {}

                                      await Permissions
                                              .cameraAndMicrophonePermissionsGranted()
                                          .then((isgranted) {
                                        if (isgranted == true) {
                                          Navigator.of(this.context).pop();
                                          call(this.context, false);
                                        } else {
                                          Navigator.of(this.context).pop();
                                          Fiberchat.showRationale(getTranslated(
                                              this.context, 'pmc'));
                                          Navigator.push(
                                              context,
                                              new MaterialPageRoute(
                                                  builder: (context) =>
                                                      OpenSettings()));
                                        }
                                      }).catchError((onError) {
                                        Fiberchat.showRationale(
                                            getTranslated(this.context, 'pmc'));
                                        Navigator.push(
                                            context,
                                            new MaterialPageRoute(
                                                builder: (context) =>
                                                    OpenSettings()));
                                      });
                                    },
                          child: SizedBox(
                            width: MediaQuery.of(context).size.width / 4,
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.center,
                              children: [
                                SizedBox(height: 13),
                                Icon(
                                  Icons.local_phone,
                                  size: 35,
                                  color: fiberchatLightGreen,
                                ),
                                SizedBox(height: 13),
                                Text(
                                  getTranslated(context, 'audiocall'),
                                  textAlign: TextAlign.center,
                                  style: TextStyle(
                                      fontWeight: FontWeight.normal,
                                      fontSize: 14,
                                      color: fiberchatBlack),
                                ),
                              ],
                            ),
                          ),
                        ),
                        InkWell(
                            onTap: observer.iscallsallowed == false
                                ? () {
                                    Navigator.of(this.context).pop();
                                    Fiberchat.showRationale(getTranslated(
                                        this.context, 'callnotallowed'));
                                  }
                                : hasPeerBlockedMe == true
                                    ? () {
                                        Navigator.of(this.context).pop();
                                        Fiberchat.toast(
                                          getTranslated(
                                              context, 'userhasblocked'),
                                        );
                                      }
                                    : () async {
                                        final observer = Provider.of<Observer>(
                                            this.context,
                                            listen: false);

                                        if (IsInterstitialAdShow == true &&
                                            observer.isadmobshow == true) {}

                                        await Permissions
                                                .cameraAndMicrophonePermissionsGranted()
                                            .then((isgranted) {
                                          if (isgranted == true) {
                                            Navigator.of(this.context).pop();
                                            call(this.context, true);
                                          } else {
                                            Navigator.of(this.context).pop();
                                            Fiberchat.showRationale(
                                                getTranslated(
                                                    this.context, 'pmc'));
                                            Navigator.push(
                                                context,
                                                new MaterialPageRoute(
                                                    builder: (context) =>
                                                        OpenSettings()));
                                          }
                                        }).catchError((onError) {
                                          Fiberchat.showRationale(getTranslated(
                                              this.context, 'pmc'));
                                          Navigator.push(
                                              context,
                                              new MaterialPageRoute(
                                                  builder: (context) =>
                                                      OpenSettings()));
                                        });
                                      },
                            child: SizedBox(
                              width: MediaQuery.of(context).size.width / 4,
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.center,
                                children: [
                                  SizedBox(height: 13),
                                  Icon(
                                    Icons.videocam,
                                    size: 39,
                                    color: fiberchatLightGreen,
                                  ),
                                  SizedBox(height: 13),
                                  Text(
                                    getTranslated(context, 'videocall'),
                                    textAlign: TextAlign.center,
                                    style: TextStyle(
                                        fontWeight: FontWeight.normal,
                                        fontSize: 14,
                                        color: fiberchatBlack),
                                  ),
                                ],
                              ),
                            ))
                      ])));
        });
  }

  getWhen(date) {
    DateTime now = DateTime.now();
    String when;
    if (date.day == now.day)
      when = getTranslated(this.context, 'today');
    else if (date.day == now.subtract(Duration(days: 1)).day)
      when = getTranslated(this.context, 'yesterday');
    else
      when = IsShowNativeTimDate == true
          ? getTranslated(this.context, DateFormat.MMMM().format(date)) +
              ' ' +
              DateFormat.d().format(date)
          : when = DateFormat.MMMd().format(date);
    return when;
  }

  getPeerStatus(val) {
    final observer = Provider.of<Observer>(this.context, listen: false);
    if (val is bool && val == true) {
      return getTranslated(this.context, 'online');
    } else if (val is int) {
      DateTime date = DateTime.fromMillisecondsSinceEpoch(val);
      String at = observer.is24hrsTimeformat == false
              ? DateFormat.jm().format(date)
              : DateFormat('HH:mm').format(date),
          when = getWhen(date);
      return getTranslated(this.context, 'lastseen') + ' $when, $at';
    } else if (val is String) {
      if (val == widget.currentUserNo)
        return getTranslated(this.context, 'typing');
      return getTranslated(this.context, 'online');
    }
    return getTranslated(this.context, 'loading');
  }

  bool isBlocked() {
    return chatStatus == ChatStatus.blocked.index;
  }

  call(BuildContext context, bool isvideocall) async {
    var mynickname = widget.prefs.getString(Dbkeys.nickname) ?? '';

    var myphotoUrl = widget.prefs.getString(Dbkeys.photoUrl) ?? '';

    CallUtils.dial(
        prefs: widget.prefs,
        currentuseruid: widget.currentUserNo,
        fromDp: myphotoUrl,
        toDp: peer!["photoUrl"],
        fromUID: widget.currentUserNo,
        fromFullname: mynickname,
        toUID: widget.peerNo,
        toFullname: peer!["nickname"],
        context: context,
        isvideocall: isvideocall);
  }

  refreshInput() {
    setStateIfMounted(() {
      if (isemojiShowing == false) {
        // hidekeyboard(this.context);
        keyboardFocusNode.unfocus();
        isemojiShowing = true;
      } else {
        isemojiShowing = false;
        keyboardFocusNode.requestFocus();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    var _keyboardVisible = MediaQuery.of(context).viewInsets.bottom != 0;
    final observer = Provider.of<Observer>(context, listen: true);
    return PickupLayout(
        prefs: widget.prefs,
        scaffold: Fiberchat.getNTPWrappedWidget(Consumer<List<GroupModel>>(
            builder: (context, groupList, _child) => WillPopScope(
                  onWillPop: isgeneratingSomethingLoader == true
                      ? () async {
                          return Future.value(false);
                        }
                      : isemojiShowing == true
                          ? () {
                              setStateIfMounted(() {
                                isemojiShowing = false;
                                keyboardFocusNode.unfocus();
                              });
                              return Future.value(false);
                            }
                          : () async {
                              WidgetsBinding.instance
                                  .addPostFrameCallback((timeStamp) {
                                var currentpeer = Provider.of<CurrentChatPeer>(
                                    this.context,
                                    listen: false);
                                currentpeer.setpeer(newgroupChatId: '');
                              });
                              setLastSeen();

                              return Future.value(true);
                            },
                  child: Stack(
                    children: [
                      Scaffold(
                          key: _scaffold,
                          appBar: AppBar(
                            elevation:
                                DESIGN_TYPE == Themetype.messenger ? 0.4 : 1,
                            titleSpacing: -14,
                            leading: Container(
                              margin: EdgeInsets.only(right: 0),
                              width: 10,
                              child: IconButton(
                                icon: Icon(
                                  Icons.arrow_back_ios,
                                  size: 20,
                                  color: DESIGN_TYPE == Themetype.whatsapp
                                      ? fiberchatWhite
                                      : fiberchatBlack,
                                ),
                                onPressed: () {
                                  Navigator.pop(context);
                                },
                              ),
                            ),
                            backgroundColor: DESIGN_TYPE == Themetype.whatsapp
                                ? fiberchatDeepGreen
                                : fiberchatWhite,
                            title: InkWell(
                              onTap: () {
                                Navigator.push(
                                    context,
                                    PageRouteBuilder(
                                        opaque: false,
                                        pageBuilder: (context, a1, a2) =>
                                            ProfileView(
                                                peer!,
                                                widget.currentUserNo,
                                                _cachedModel,
                                                widget.prefs, [])));
                              },
                              child: Row(
                                mainAxisAlignment: MainAxisAlignment.start,
                                children: [
                                  Padding(
                                    padding:
                                        const EdgeInsets.fromLTRB(0, 7, 0, 7),
                                    child: Fiberchat.avatar(peer, radius: 20),
                                  ),
                                  SizedBox(
                                    width: 7,
                                  ),
                                  Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      SizedBox(
                                        width: MediaQuery.of(this.context)
                                                .size
                                                .width /
                                            2.3,
                                        child: Text(
                                          Fiberchat.getNickname(peer!)!,
                                          overflow: TextOverflow.ellipsis,
                                          style: TextStyle(
                                              color: DESIGN_TYPE ==
                                                      Themetype.whatsapp
                                                  ? fiberchatWhite
                                                  : fiberchatBlack,
                                              fontSize: 17.0,
                                              fontWeight: FontWeight.w500),
                                        ),
                                      ),
                                      SizedBox(
                                        height: 4,
                                      ),
                                      chatId != null
                                          ? Text(
                                              getPeerStatus(
                                                  peer![Dbkeys.lastSeen]),
                                              style: TextStyle(
                                                  color: DESIGN_TYPE ==
                                                          Themetype.whatsapp
                                                      ? fiberchatWhite
                                                      : fiberchatGrey,
                                                  fontSize: 12,
                                                  fontWeight: FontWeight.w400),
                                            )
                                          : Text(
                                              getTranslated(
                                                  this.context, 'loading'),
                                              style: TextStyle(
                                                  color: DESIGN_TYPE ==
                                                          Themetype.whatsapp
                                                      ? fiberchatWhite
                                                      : fiberchatGrey,
                                                  fontSize: 12,
                                                  fontWeight: FontWeight.w400),
                                            ),
                                    ],
                                  ),
                                ],
                              ),
                            ),
                            actions: [
                              observer.isCallFeatureTotallyHide == true
                                  ? SizedBox()
                                  : SizedBox(
                                      width: 55,
                                      child: IconButton(
                                          icon: Icon(
                                            Icons.add_call,
                                            color: DESIGN_TYPE ==
                                                    Themetype.whatsapp
                                                ? fiberchatWhite
                                                : fiberchatgreen,
                                          ),
                                          onPressed:
                                              observer.iscallsallowed == false
                                                  ? () {
                                                      Fiberchat.showRationale(
                                                          getTranslated(
                                                              this.context,
                                                              'callnotallowed'));
                                                    }
                                                  : hasPeerBlockedMe == true
                                                      ? () {
                                                          Fiberchat.toast(
                                                            getTranslated(
                                                                context,
                                                                'userhasblocked'),
                                                          );
                                                        }
                                                      : () async {
                                                          showDialOptions(
                                                              this.context);
                                                        }),
                                    ),
                              SizedBox(
                                width: observer.isCallFeatureTotallyHide == true
                                    ? 45
                                    : 25,
                                child: PopupMenuButton(
                                    padding: EdgeInsets.all(0),
                                    icon: Padding(
                                      padding: const EdgeInsets.only(right: 0),
                                      child: Icon(
                                        Icons.more_vert_outlined,
                                        color: DESIGN_TYPE == Themetype.whatsapp
                                            ? fiberchatWhite
                                            : fiberchatBlack,
                                      ),
                                    ),
                                    color: fiberchatWhite,
                                    onSelected: (dynamic val) {
                                      switch (val) {
                                        case 'report':
                                          showModalBottomSheet(
                                              isScrollControlled: true,
                                              context: context,
                                              shape: RoundedRectangleBorder(
                                                borderRadius:
                                                    BorderRadius.vertical(
                                                        top: Radius.circular(
                                                            25.0)),
                                              ),
                                              builder: (BuildContext context) {
                                                // return your layout
                                                var w = MediaQuery.of(context)
                                                    .size
                                                    .width;
                                                return Padding(
                                                  padding: EdgeInsets.only(
                                                      bottom:
                                                          MediaQuery.of(context)
                                                              .viewInsets
                                                              .bottom),
                                                  child: Container(
                                                      padding:
                                                          EdgeInsets.all(16),
                                                      height:
                                                          MediaQuery.of(context)
                                                                  .size
                                                                  .height /
                                                              2.6,
                                                      child: Column(
                                                          mainAxisSize:
                                                              MainAxisSize.min,
                                                          crossAxisAlignment:
                                                              CrossAxisAlignment
                                                                  .stretch,
                                                          children: [
                                                            SizedBox(
                                                              height: 12,
                                                            ),
                                                            SizedBox(
                                                              height: 3,
                                                            ),
                                                            Padding(
                                                              padding:
                                                                  const EdgeInsets
                                                                          .only(
                                                                      left: 7),
                                                              child: Text(
                                                                getTranslated(
                                                                    this.context,
                                                                    'reportshort'),
                                                                textAlign:
                                                                    TextAlign
                                                                        .left,
                                                                style: TextStyle(
                                                                    fontWeight:
                                                                        FontWeight
                                                                            .bold,
                                                                    fontSize:
                                                                        16.5),
                                                              ),
                                                            ),
                                                            SizedBox(
                                                              height: 10,
                                                            ),
                                                            Container(
                                                              margin: EdgeInsets
                                                                  .only(
                                                                      top: 10),
                                                              padding:
                                                                  EdgeInsets
                                                                      .fromLTRB(
                                                                          0,
                                                                          0,
                                                                          0,
                                                                          0),
                                                              // height: 63,
                                                              height: 63,
                                                              width: w / 1.24,
                                                              child:
                                                                  InpuTextBox(
                                                                controller:
                                                                    reportEditingController,
                                                                leftrightmargin:
                                                                    0,
                                                                showIconboundary:
                                                                    false,
                                                                boxcornerradius:
                                                                    5.5,
                                                                boxheight: 50,
                                                                hinttext: getTranslated(
                                                                    this.context,
                                                                    'reportdesc'),
                                                                prefixIconbutton:
                                                                    Icon(
                                                                  Icons.message,
                                                                  color: Colors
                                                                      .grey
                                                                      .withOpacity(
                                                                          0.5),
                                                                ),
                                                              ),
                                                            ),
                                                            SizedBox(
                                                              height: w / 10,
                                                            ),
                                                            myElevatedButton(
                                                                color:
                                                                    fiberchatLightGreen,
                                                                child: Padding(
                                                                  padding:
                                                                      const EdgeInsets
                                                                              .fromLTRB(
                                                                          10,
                                                                          15,
                                                                          10,
                                                                          15),
                                                                  child: Text(
                                                                    getTranslated(
                                                                        context,
                                                                        'report'),
                                                                    style: TextStyle(
                                                                        color: Colors
                                                                            .white,
                                                                        fontSize:
                                                                            18),
                                                                  ),
                                                                ),
                                                                onPressed:
                                                                    () async {
                                                                  Navigator.of(
                                                                          context)
                                                                      .pop();

                                                                  DateTime
                                                                      time =
                                                                      DateTime
                                                                          .now();

                                                                  Map<String,
                                                                          dynamic>
                                                                      mapdata =
                                                                      {
                                                                    'title':
                                                                        'New report by User',
                                                                    'desc':
                                                                        '${reportEditingController.text}',
                                                                    'phone':
                                                                        '${widget.currentUserNo}',
                                                                    'type':
                                                                        'Individual Chat',
                                                                    'time': time
                                                                        .millisecondsSinceEpoch,
                                                                    'id': Fiberchat.getChatId(
                                                                        widget
                                                                            .currentUserNo,
                                                                        widget
                                                                            .peerNo),
                                                                  };

                                                                  await FirebaseFirestore
                                                                      .instance
                                                                      .collection(
                                                                          'reports')
                                                                      .doc(time
                                                                          .millisecondsSinceEpoch
                                                                          .toString())
                                                                      .set(
                                                                          mapdata)
                                                                      .then(
                                                                          (value) async {
                                                                    showModalBottomSheet(
                                                                        isScrollControlled:
                                                                            true,
                                                                        context:
                                                                            context,
                                                                        shape:
                                                                            RoundedRectangleBorder(
                                                                          borderRadius:
                                                                              BorderRadius.vertical(top: Radius.circular(25.0)),
                                                                        ),
                                                                        builder:
                                                                            (BuildContext
                                                                                context) {
                                                                          return Container(
                                                                            height:
                                                                                220,
                                                                            child:
                                                                                Padding(
                                                                              padding: const EdgeInsets.all(28.0),
                                                                              child: Column(
                                                                                mainAxisAlignment: MainAxisAlignment.center,
                                                                                children: [
                                                                                  Icon(Icons.check, color: Colors.green[400], size: 40),
                                                                                  SizedBox(
                                                                                    height: 30,
                                                                                  ),
                                                                                  Text(
                                                                                    getTranslated(context, 'reportsuccess'),
                                                                                    textAlign: TextAlign.center,
                                                                                  )
                                                                                ],
                                                                              ),
                                                                            ),
                                                                          );
                                                                        });

                                                                    //----
                                                                  }).catchError(
                                                                          (err) {
                                                                    showModalBottomSheet(
                                                                        isScrollControlled:
                                                                            true,
                                                                        context:
                                                                            this
                                                                                .context,
                                                                        shape:
                                                                            RoundedRectangleBorder(
                                                                          borderRadius:
                                                                              BorderRadius.vertical(top: Radius.circular(25.0)),
                                                                        ),
                                                                        builder:
                                                                            (BuildContext
                                                                                context) {
                                                                          return Container(
                                                                            height:
                                                                                220,
                                                                            child:
                                                                                Padding(
                                                                              padding: const EdgeInsets.all(28.0),
                                                                              child: Column(
                                                                                mainAxisAlignment: MainAxisAlignment.center,
                                                                                children: [
                                                                                  Icon(Icons.check, color: Colors.green[400], size: 40),
                                                                                  SizedBox(
                                                                                    height: 30,
                                                                                  ),
                                                                                  Text(
                                                                                    getTranslated(context, 'reportsuccess'),
                                                                                    textAlign: TextAlign.center,
                                                                                  )
                                                                                ],
                                                                              ),
                                                                            ),
                                                                          );
                                                                        });
                                                                  });
                                                                }),
                                                          ])),
                                                );
                                              });
                                          break;
                                        case 'hide':
                                          ChatController.hideChat(
                                              widget.currentUserNo,
                                              widget.peerNo);
                                          break;
                                        case 'unhide':
                                          ChatController.unhideChat(
                                              widget.currentUserNo,
                                              widget.peerNo);
                                          break;
                                        case 'lock':
                                          if (widget.prefs.getString(
                                                      Dbkeys.isPINsetDone) !=
                                                  widget.currentUserNo ||
                                              widget.prefs.getString(
                                                      Dbkeys.isPINsetDone) ==
                                                  null) {
                                            unawaited(Navigator.push(
                                                this.context,
                                                MaterialPageRoute(
                                                    builder: (context) =>
                                                        Security(
                                                          widget.currentUserNo,
                                                          prefs: widget.prefs,
                                                          setPasscode: true,
                                                          onSuccess:
                                                              (newContext) async {
                                                            ChatController.lockChat(
                                                                widget
                                                                    .currentUserNo,
                                                                widget.peerNo);
                                                            Navigator.pop(
                                                                context);
                                                            Navigator.pop(
                                                                context);
                                                          },
                                                          title: getTranslated(
                                                              this.context,
                                                              'authh'),
                                                        ))));
                                          } else {
                                            ChatController.lockChat(
                                                widget.currentUserNo,
                                                widget.peerNo);
                                            Navigator.pop(context);
                                          }
                                          break;
                                        case 'unlock':
                                          ChatController.unlockChat(
                                              widget.currentUserNo,
                                              widget.peerNo);
                                          break;
                                        case 'block':
                                          // if (hasPeerBlockedMe == true) {
                                          //   Fiberchat.toast(
                                          //     getTranslated(context,
                                          //         'userhasblocked'),
                                          //   );
                                          // } else {
                                          ChatController.block(
                                              widget.currentUserNo,
                                              widget.peerNo);
                                          // }
                                          break;
                                        case 'unblock':
                                          // if (hasPeerBlockedMe == true) {
                                          //   Fiberchat.toast(
                                          //     getTranslated(context,
                                          //         'userhasblocked'),
                                          //   );
                                          // } else {
                                          ChatController.accept(
                                              widget.currentUserNo,
                                              widget.peerNo);
                                          Fiberchat.toast(getTranslated(
                                              this.context, 'unblocked'));
                                          // }

                                          break;
                                        case 'tutorial':
                                          Fiberchat.toast(getTranslated(
                                              this.context, 'vsmsg'));

                                          break;
                                        case 'remove_wallpaper':
                                          _cachedModel
                                              .removeWallpaper(widget.peerNo!);
                                          // Fiberchat.toast('Wallpaper removed.');
                                          break;
                                        case 'set_wallpaper':
                                          Navigator.push(
                                              context,
                                              MaterialPageRoute(
                                                  builder: (context) =>
                                                      SingleImagePicker(
                                                        title: getTranslated(
                                                            this.context,
                                                            'pickimage'),
                                                        callback: getWallpaper,
                                                      )));
                                          break;
                                      }
                                    },
                                    itemBuilder: ((context) =>
                                        <PopupMenuItem<String>>[
                                          PopupMenuItem<String>(
                                            value: hidden ? 'unhide' : 'hide',
                                            child: Text(
                                              '${hidden ? getTranslated(this.context, 'unhidechat') : getTranslated(this.context, 'hidechat')}',
                                            ),
                                          ),
                                          PopupMenuItem<String>(
                                            value: locked ? 'unlock' : 'lock',
                                            child: Text(
                                                '${locked ? getTranslated(this.context, 'unlockchat') : getTranslated(this.context, 'lockchat')}'),
                                          ),
                                          PopupMenuItem<String>(
                                            value: isBlocked()
                                                ? 'unblock'
                                                : 'block',
                                            child: Text(
                                                '${isBlocked() ? getTranslated(this.context, 'unblockchat') : getTranslated(this.context, 'blockchat')}'),
                                          ),
                                          peer![Dbkeys.wallpaper] != null
                                              ? PopupMenuItem<String>(
                                                  value: 'remove_wallpaper',
                                                  child: Text(getTranslated(
                                                      this.context,
                                                      'removewall')))
                                              : PopupMenuItem<String>(
                                                  value: 'set_wallpaper',
                                                  child: Text(getTranslated(
                                                      this.context,
                                                      'setwall'))),
                                          PopupMenuItem<String>(
                                            value: 'report',
                                            child: Text(
                                              '${getTranslated(this.context, 'report')}',
                                            ),
                                          ),
                                          // ignore: unnecessary_null_comparison
                                        ].toList())),
                              ),
                            ],
                          ),
                          body: Stack(children: <Widget>[
                            new Container(
                              decoration: new BoxDecoration(
                                color: DESIGN_TYPE == Themetype.whatsapp
                                    ? fiberchatChatbackground
                                    : fiberchatWhite,
                                image: new DecorationImage(
                                    image: AssetImage(
                                        "assets/images/background.png"),
                                    fit: BoxFit.cover),
                              ),
                            ),
                            PageView(children: <Widget>[
                              Column(
                                  mainAxisAlignment: MainAxisAlignment.end,
                                  children: [
                                    Expanded(
                                        child: buildMessagesUsingProvider(
                                            context)),
                                    // groupList
                                    //                 .lastWhere((element) =>
                                    //                     element.docmap[
                                    //                         Dbkeys.groupID] ==
                                    //                     widget.groupID)
                                    //                 .docmap[Dbkeys.groupTYPE] ==
                                    //             Dbkeys
                                    //                 .groupTYPEallusersmessageallowed ||
                                    //         groupList
                                    //             .lastWhere((element) =>
                                    //                 element.docmap[
                                    //                     Dbkeys.groupID] ==
                                    //                 widget.groupID)
                                    //             .docmap[Dbkeys.groupADMINLIST]
                                    //             .contains(widget.currentUserno)
                                    //     ?
                                    buildInputAndroid(
                                      context,
                                      isemojiShowing,
                                      refreshInput,
                                      _keyboardVisible,
                                    ),
                                    // : Container(
                                    //     alignment: Alignment.center,
                                    //     padding: EdgeInsets.fromLTRB(
                                    //         14, 7, 14, 7),
                                    //     color: Colors.white,
                                    //     height: 70,
                                    //     width: MediaQuery.of(context)
                                    //         .size
                                    //         .width,
                                    //     child: Text(
                                    //       getTranslated(
                                    //           context, 'onlyadminsend'),
                                    //       textAlign: TextAlign.center,
                                    //       style: TextStyle(height: 1.3),
                                    //     ),
                                    //   ),
                                  ])
                            ]),
                          ])),
                      buildLoadingThumbnail()
                    ],
                  ),
                ))));
  }

  Widget selectablelinkify(String? text, double? fontsize) {
    return SelectableLinkify(
      style: TextStyle(fontSize: fontsize, color: Colors.black87),
      text: text ?? "",
      onOpen: (link) async {
        custom_url_launcher(link.url);
      },
    );
  }
}

deletedGroupWidget(BuildContext context) {
  return Scaffold(
    appBar: AppBar(),
    body: Container(
      child: Center(
        child: Padding(
          padding: const EdgeInsets.all(18.0),
          child: Text(
            getTranslated(context, 'deletedgroup'),
            textAlign: TextAlign.center,
          ),
        ),
      ),
    ),
  );
}
