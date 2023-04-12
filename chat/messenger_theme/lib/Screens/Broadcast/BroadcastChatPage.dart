//*************   © Copyrighted by Thinkcreative_Technologies. An Exclusive item of Envato market. Make sure you have purchased a Regular License OR Extended license for the Source Code from Envato to use this product. See the License Defination attached with source code. *********************

import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:emoji_picker_flutter/emoji_picker_flutter.dart';
import 'package:fiberchat/Configs/app_constants.dart';
import 'package:fiberchat/Configs/optional_constants.dart';
import 'package:fiberchat/Screens/Broadcast/BroadcastDetails.dart';
import 'package:fiberchat/Screens/Groups/widget/groupChatBubble.dart';
import 'package:fiberchat/Screens/calling_screen/pickup_layout.dart';
import 'package:fiberchat/Screens/chat_screen/chat.dart';
import 'package:fiberchat/Services/Admob/admob.dart';
import 'package:fiberchat/Services/Providers/BroadcastProvider.dart';
import 'package:fiberchat/Screens/chat_screen/utils/uploadMediaWithProgress.dart';
import 'package:fiberchat/Services/Providers/AvailableContactsProvider.dart';
import 'package:fiberchat/Services/Providers/Observer.dart';
import 'package:fiberchat/Services/localization/language_constants.dart';
import 'package:fiberchat/Utils/custom_url_launcher.dart';
import 'package:fiberchat/Utils/emoji_detect.dart';
import 'package:fiberchat/Utils/mime_type.dart';
import 'package:fiberchat/Utils/setStatusBarColor.dart';
import 'package:fiberchat/Utils/utils.dart';
import 'package:fiberchat/widgets/AllinOneCameraGalleryImageVideoPicker/AllinOneCameraGalleryImageVideoPicker.dart';
import 'package:fiberchat/widgets/CameraGalleryImagePicker/camera_image_gallery_picker.dart';
import 'package:fiberchat/widgets/CameraGalleryImagePicker/multiMediaPicker.dart';
import 'package:fiberchat/widgets/DownloadManager/download_all_file_type.dart';
import 'package:fiberchat/widgets/InfiniteList/InfiniteCOLLECTIONListViewWidget.dart';
import 'package:fiberchat/widgets/VideoEditor/video_editor.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';
import 'package:flutter_image_compress/flutter_image_compress.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:link_preview_generator/link_preview_generator.dart';
import 'package:media_info/media_info.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import 'package:provider/provider.dart';
import 'package:fiberchat/Configs/Enum.dart';
import 'package:emoji_picker_flutter/emoji_picker_flutter.dart' as emojipic;
import 'package:fiberchat/Configs/Dbkeys.dart';
import 'package:fiberchat/Configs/Dbpaths.dart';
import 'package:fiberchat/Screens/privacypolicy&TnC/PdfViewFromCachedUrl.dart';
import 'package:fiberchat/widgets/SoundPlayer/SoundPlayerPro.dart';
import 'package:fiberchat/Screens/call_history/callhistory.dart';
import 'package:fiberchat/Screens/contact_screens/ContactsSelect.dart';
import 'package:fiberchat/Models/DataModel.dart';
import 'package:fiberchat/Screens/chat_screen/utils/photo_view.dart';
import 'package:fiberchat/Utils/save.dart';
import 'package:fiberchat/widgets/AudioRecorder/Audiorecord.dart';
import 'package:fiberchat/widgets/DocumentPicker/documentPicker.dart';
import 'package:fiberchat/widgets/VideoPicker/VideoPreview.dart';
import 'package:flutter_linkify/flutter_linkify.dart';
import 'package:geolocator/geolocator.dart';
import 'package:giphy_get/giphy_get.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:video_thumbnail/video_thumbnail.dart';
import 'package:fiberchat/Utils/unawaited.dart';
import 'package:video_compress/video_compress.dart' as compress;
import 'package:path/path.dart' as p;

class BroadcastChatPage extends StatefulWidget {
  final String currentUserno;
  final String broadcastID;
  final DataModel model;
  final SharedPreferences prefs;
  BroadcastChatPage({
    Key? key,
    required this.currentUserno,
    required this.broadcastID,
    required this.model,
    required this.prefs,
  }) : super(key: key);

  @override
  _BroadcastChatPageState createState() => _BroadcastChatPageState();
}

class _BroadcastChatPageState extends State<BroadcastChatPage>
    with WidgetsBindingObserver {
  bool isgeneratingThumbnail = false;

  GlobalKey<ScaffoldState> _scaffold = new GlobalKey<ScaffoldState>();
  GlobalKey<State> _keyLoader =
      new GlobalKey<State>(debugLabel: 'qqqeqessaqsseaadqeqe');
  final ScrollController realtime = new ScrollController();
  late Query firestoreChatquery;
  InterstitialAd? _interstitialAd;
  int _numInterstitialLoadAttempts = 0;
  RewardedAd? _rewardedAd;
  int _numRewardedLoadAttempts = 0;
  @override
  void initState() {
    super.initState();
    firestoreChatquery = FirebaseFirestore.instance
        .collection(DbPaths.collectionbroadcasts)
        .doc(widget.broadcastID)
        .collection(DbPaths.collectionbroadcastsChats)
        .orderBy(Dbkeys.broadcastmsgTIME, descending: true)
        .limit(maxChatMessageDocsLoadAtOnceForGroupChatAndBroadcastLazyLoading);
    setLastSeen(false);

    WidgetsBinding.instance.addPostFrameCallback((_) {
      var firestoreProvider =
          Provider.of<FirestoreDataProviderMESSAGESforBROADCASTCHATPAGE>(
              this.context,
              listen: false);

      final observer = Provider.of<Observer>(this.context, listen: false);
      firestoreProvider.reset();
      Future.delayed(const Duration(milliseconds: 1000), () {
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
    });
  }

  loadMessagesAndListen() async {
    firestoreChatquery.snapshots().listen((snapshot) {
      snapshot.docChanges.forEach((change) {
        if (change.type == DocumentChangeType.added) {
          var chatprovider =
              Provider.of<FirestoreDataProviderMESSAGESforBROADCASTCHATPAGE>(
                  this.context,
                  listen: false);
          DocumentSnapshot newDoc = change.doc;
          if (chatprovider.datalistSnapshot.length == 0) {
          } else if ((chatprovider.checkIfDocAlreadyExits(
                newDoc: newDoc,
              ) ==
              false)) {
            chatprovider.addDoc(newDoc);
            // unawaited(realtime.animateTo(0.0,
            //     duration: Duration(milliseconds: 300), curve: Curves.easeOut));
          }
        } else if (change.type == DocumentChangeType.modified) {
          var chatprovider =
              Provider.of<FirestoreDataProviderMESSAGESforBROADCASTCHATPAGE>(
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
              Provider.of<FirestoreDataProviderMESSAGESforBROADCASTCHATPAGE>(
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
  }

  void setStateIfMounted(f) {
    if (mounted) setState(f);
  }

  setLastSeen(bool iswillpop) {
    setStatusBarColor();
    if (iswillpop == true) {
      Navigator.of(this.context).pop();
    }
  }

  @override
  void dispose() {
    super.dispose();
    WidgetsBinding.instance.removeObserver(this);
    setLastSeen(false);
    if (IsInterstitialAdShow == true) {
      _interstitialAd!.dispose();
    }
    if (IsVideoAdShow == true) {
      _rewardedAd!.dispose();
    }
  }

  File? thumbnailFile;

  getFileData(File image) {
    final observer = Provider.of<Observer>(this.context, listen: false);
    // ignore: unnecessary_null_comparison
    if (image != null) {
      setStateIfMounted(() {
        imageFile = image;
      });
    }
    return observer.isPercentProgressShowWhileUploading
        ? uploadFileWithProgressIndicator(false)
        : uploadFile(false);
  }

  getpickedFileName(broadcastID, timestamp) {
    return "${widget.currentUserno}-$timestamp";
  }

  getThumbnail(String url) async {
    final observer = Provider.of<Observer>(this.context, listen: false);
    // ignore: unnecessary_null_comparison
    setStateIfMounted(() {
      isgeneratingThumbnail = true;
    });
    String? path = await VideoThumbnail.thumbnailFile(
        video: url,
        thumbnailPath: (await getTemporaryDirectory()).path,
        imageFormat: ImageFormat.PNG,
        // maxHeight: 150,
        // maxWidth:300,
        // timeMs: r.timeMs,
        quality: 30);

    thumbnailFile = File(path!);
    setStateIfMounted(() {
      isgeneratingThumbnail = false;
    });
    return observer.isPercentProgressShowWhileUploading
        ? uploadFileWithProgressIndicator(true)
        : uploadFile(true);
  }

  String? videometadata;
  int? uploadTimestamp;
  int? thumnailtimestamp;
  Future uploadFile(bool isthumbnail) async {
    uploadTimestamp = DateTime.now().millisecondsSinceEpoch;
    String fileName = getpickedFileName(
        widget.broadcastID,
        isthumbnail == false
            ? '$uploadTimestamp'
            : '${thumnailtimestamp}Thumbnail');
    Reference reference = FirebaseStorage.instance
        .ref("+00_BROADCAST_MEDIA/${widget.broadcastID}/")
        .child(fileName);

    File fileToCompress;
    File? compressedImage;

    if (isthumbnail == false && isVideo(imageFile!.path) == true) {
      fileToCompress = File(imageFile!.path);
      await compress.VideoCompress.setLogLevel(0);

      final compress.MediaInfo? info =
          await compress.VideoCompress.compressVideo(
        fileToCompress.path,
        quality: IsVideoQualityCompress == true
            ? compress.VideoQuality.MediumQuality
            : compress.VideoQuality.HighestQuality,
        deleteOrigin: false,
        includeAudio: true,
      );
      imageFile = File(info!.path!);
    } else if (isthumbnail == false && isImage(imageFile!.path) == true) {
      final targetPath = imageFile!.absolute.path
              .replaceAll(basename(imageFile!.absolute.path), "") +
          "temp.jpg";

      compressedImage = await FlutterImageCompress.compressAndGetFile(
        imageFile!.absolute.path,
        targetPath,
        quality: ImageQualityCompress,
        rotate: 0,
      );
    } else {}
    TaskSnapshot uploading = await reference.putFile(isthumbnail == true
        ? thumbnailFile!
        : isImage(imageFile!.path) == true
            ? compressedImage!
            : imageFile!);

    if (isthumbnail == false) {
      setStateIfMounted(() {
        thumnailtimestamp = uploadTimestamp;
      });
    }
    if (isthumbnail == true) {
      MediaInfo _mediaInfo = MediaInfo();

      await _mediaInfo.getMediaInfo(thumbnailFile!.path).then((mediaInfo) {
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
        print('ERROR Sending File: $onError');
      });
    } else {
      FirebaseFirestore.instance
          .collection(DbPaths.collectionusers)
          .doc(widget.currentUserno)
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

  Future uploadFileWithProgressIndicator(bool isthumbnail) async {
    uploadTimestamp = DateTime.now().millisecondsSinceEpoch;
    String fileName = getpickedFileName(
        widget.broadcastID,
        isthumbnail == false
            ? '$uploadTimestamp'
            : '${thumnailtimestamp}Thumbnail');
    Reference reference = FirebaseStorage.instance
        .ref("+00_BROADCAST_MEDIA/${widget.broadcastID}/")
        .child(fileName);

    File fileToCompress;
    File? compressedImage;

    if (isthumbnail == false && isVideo(imageFile!.path) == true) {
      fileToCompress = File(imageFile!.path);
      await compress.VideoCompress.setLogLevel(0);

      final compress.MediaInfo? info =
          await compress.VideoCompress.compressVideo(
        fileToCompress.path,
        quality: IsVideoQualityCompress == true
            ? compress.VideoQuality.MediumQuality
            : compress.VideoQuality.HighestQuality,
        deleteOrigin: false,
        includeAudio: true,
      );
      imageFile = File(info!.path!);
    } else if (isthumbnail == false && isImage(imageFile!.path) == true) {
      final targetPath = imageFile!.absolute.path
              .replaceAll(basename(imageFile!.absolute.path), "") +
          "temp.jpg";

      compressedImage = await FlutterImageCompress.compressAndGetFile(
        imageFile!.absolute.path,
        targetPath,
        quality: ImageQualityCompress,
        rotate: 0,
      );
    } else {}
    UploadTask uploading = reference.putFile(isthumbnail == true
        ? thumbnailFile!
        : isImage(imageFile!.path) == true
            ? compressedImage!
            : imageFile!);

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
                                    : getTranslated(context, 'uploading'),
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
                                      : getTranslated(context, 'uploading'),
                                  subtitle: '');
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

      await _mediaInfo.getMediaInfo(thumbnailFile!.path).then((mediaInfo) {
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
          .doc(widget.currentUserno)
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

  void onSendMessage({
    required BuildContext context,
    required String content,
    required MessageType type,
    required List<dynamic> recipientList,
  }) async {
    textEditingController.clear();
    final observer = Provider.of<Observer>(this.context, listen: false);
    await FirebaseBroadcastServices().sendMessageToBroadcastRecipients(
        recipientList: recipientList,
        context: context,
        content: content,
        currentUserNo: widget.currentUserno,
        broadcastId: widget.broadcastID,
        type: type,
        cachedModel: widget.model);

    unawaited(realtime.animateTo(0.0,
        duration: Duration(milliseconds: 300), curve: Curves.easeOut));
    Fiberchat.toast(
        '${getTranslated(context, 'senttorecp')} ${recipientList.length}');
    setStatusBarColor();
    if (type == MessageType.doc ||
        type == MessageType.audio ||
        (type == MessageType.image && !content.contains('giphy')) ||
        type == MessageType.location ||
        type == MessageType.contact) {
      if (IsVideoAdShow == true &&
          observer.isadmobshow == true &&
          IsInterstitialAdShow == false) {
        Future.delayed(const Duration(milliseconds: 1200), () {
          _showRewardedAd();
        });
      } else if (IsInterstitialAdShow == true && observer.isadmobshow == true) {
        _showInterstitialAd();
      }
    } else if (type == MessageType.video) {
      if (IsVideoAdShow == true && observer.isadmobshow == true) {
        Future.delayed(const Duration(milliseconds: 1200), () {
          _showRewardedAd();
        });
      }
    }
  }

  Future uploadSelectedLocalFileWithProgressIndicator(
      File selectedFile, bool isVideo, bool isthumbnail, int timeEpoch,
      {String? filenameoptional}) async {
    String ext = p.extension(selectedFile.path);
    String fileName = filenameoptional != null
        ? filenameoptional
        : isthumbnail == true
            ? 'Thumbnail-$timeEpoch$ext'
            : isVideo
                ? 'Video-$timeEpoch$ext'
                : 'IMG-$timeEpoch$ext';
    // String fileName = getpickedFileName(widget.broadcastID,
    //     isthumbnail == false ? '$timeEpoch' : '${timeEpoch}Thumbnail');
    Reference reference = FirebaseStorage.instance
        .ref("+00_BROADCAST_MEDIA/${widget.broadcastID}/")
        .child(fileName);

    UploadTask uploading = reference.putFile(selectedFile);

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

    if (isthumbnail == true) {
      MediaInfo _mediaInfo = MediaInfo();

      await _mediaInfo.getMediaInfo(selectedFile.path).then((mediaInfo) {
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
          .doc(widget.currentUserno)
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

  final TextEditingController textEditingController =
      new TextEditingController();
  FocusNode keyboardFocusNode = new FocusNode();
  Widget buildInputAndroid(
      BuildContext context,
      bool isemojiShowing,
      Function toggleEmojiKeyboard,
      bool keyboardVisible,
      List<BroadcastModel> broadcastList) {
    final observer = Provider.of<Observer>(context, listen: true);

    return Column(
        mainAxisAlignment: MainAxisAlignment.end,
        mainAxisSize: MainAxisSize.min,
        children: [
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
                              toggleEmojiKeyboard();
                            },
                            icon: Icon(
                              Icons.emoji_emotions,
                              size: 23,
                              color: fiberchatGrey,
                            ),
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
                            onChanged: (f) {
                              if (textEditingController.text.isNotEmpty &&
                                  textEditingController.text.length == 1) {
                                setStateIfMounted(() {});
                              }

                              setStateIfMounted(() {});
                            },
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
                                          onPressed: observer
                                                      .ismediamessagingallowed ==
                                                  false
                                              ? () {
                                                  Fiberchat.showRationale(
                                                      getTranslated(
                                                          this.context,
                                                          'mediamssgnotallowed'));
                                                }
                                              : () {
                                                  hidekeyboard(context);
                                                  shareMedia(
                                                      context, broadcastList);
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
                                                  : () async {
                                                      hidekeyboard(context);
                                                      await Navigator.push(
                                                          context,
                                                          new MaterialPageRoute(
                                                              builder: (context) =>
                                                                  new AllinOneCameraGalleryImageVideoPicker(
                                                                    onTakeFile: (file,
                                                                        isVideo,
                                                                        thumnail) async {
                                                                      setStatusBarColor();
                                                                      int timeStamp =
                                                                          DateTime.now()
                                                                              .millisecondsSinceEpoch;
                                                                      if (isVideo ==
                                                                          true) {
                                                                        String
                                                                            videoFileext =
                                                                            p.extension(file.path);
                                                                        String
                                                                            videofileName =
                                                                            'Video-$timeStamp$videoFileext';
                                                                        String? videoUrl = await uploadSelectedLocalFileWithProgressIndicator(
                                                                            file,
                                                                            true,
                                                                            false,
                                                                            timeStamp,
                                                                            filenameoptional:
                                                                                videofileName);
                                                                        if (videoUrl !=
                                                                            null) {
                                                                          String? thumnailUrl = await uploadSelectedLocalFileWithProgressIndicator(
                                                                              thumnail!,
                                                                              false,
                                                                              true,
                                                                              timeStamp);
                                                                          if (thumnailUrl !=
                                                                              null) {
                                                                            onSendMessage(
                                                                                context: this.context,
                                                                                content: videoUrl + '-BREAK-' + thumnailUrl + '-BREAK-' + videometadata! + '-BREAK-' + videofileName,
                                                                                type: MessageType.video,
                                                                                recipientList: broadcastList.toList().firstWhere((element) => element.docmap[Dbkeys.broadcastID] == widget.broadcastID).docmap[Dbkeys.broadcastMEMBERSLIST]);
                                                                            file.delete();
                                                                            thumnail.delete();
                                                                          }
                                                                        }
                                                                      } else {
                                                                        String
                                                                            imageFileext =
                                                                            p.extension(file.path);
                                                                        String
                                                                            imagefileName =
                                                                            'IMG-$timeStamp$imageFileext';
                                                                        String? url = await uploadSelectedLocalFileWithProgressIndicator(
                                                                            file,
                                                                            false,
                                                                            false,
                                                                            timeStamp,
                                                                            filenameoptional:
                                                                                imagefileName);
                                                                        if (url !=
                                                                            null) {
                                                                          onSendMessage(
                                                                              context: this.context,
                                                                              content: url,
                                                                              type: MessageType.image,
                                                                              recipientList: broadcastList.toList().firstWhere((element) => element.docmap[Dbkeys.broadcastID] == widget.broadcastID).docmap[Dbkeys.broadcastMEMBERSLIST]);
                                                                          file.delete();
                                                                        }
                                                                      }
                                                                    },
                                                                  )));
                                                      // hidekeyboard(context);

                                                      // Navigator.push(
                                                      //     context,
                                                      //     MaterialPageRoute(
                                                      //         builder: (context) =>
                                                      //             SingleImagePicker(
                                                      //               title: getTranslated(
                                                      //                   this.context,
                                                      //                   'pickimage'),
                                                      //               callback:
                                                      //                   getFileData,
                                                      //             ))).then((url) {
                                                      //   if (url != null) {
                                                      //     onSendMessage(
                                                      //         context: this.context,
                                                      //         content: url,
                                                      //         type:
                                                      //             MessageType.image,
                                                      //         recipientList: broadcastList
                                                      //             .toList()
                                                      //             .firstWhere((element) =>
                                                      //                 element.docmap[
                                                      //                     Dbkeys
                                                      //                         .broadcastID] ==
                                                      //                 widget
                                                      //                     .broadcastID)
                                                      //             .docmap[Dbkeys.broadcastMEMBERSLIST]);
                                                      //   }
                                                      // });
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
                                                          context: context,
                                                          content: gif.images!
                                                              .original!.url,
                                                          type:
                                                              MessageType.image,
                                                          recipientList: broadcastList
                                                              .toList()
                                                              .firstWhere((element) =>
                                                                  element.docmap[
                                                                      Dbkeys
                                                                          .broadcastID] ==
                                                                  widget
                                                                      .broadcastID)
                                                              .docmap[Dbkeys.broadcastMEMBERSLIST]);
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
                        textEditingController.text.isNotEmpty == true
                            ? Icons.send
                            : Icons.mic,
                        color: fiberchatWhite.withOpacity(0.99),
                      ),
                      onPressed: observer.ismediamessagingallowed == true
                          ? textEditingController.text.isNotEmpty == false
                              ? () {
                                  hidekeyboard(context);

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
                                          context: context,
                                          content: url +
                                              '-BREAK-' +
                                              uploadTimestamp.toString(),
                                          type: MessageType.audio,
                                          recipientList: broadcastList
                                                  .toList()
                                                  .firstWhere((element) =>
                                                      element.docmap[
                                                          Dbkeys.broadcastID] ==
                                                      widget.broadcastID)
                                                  .docmap[
                                              Dbkeys.broadcastMEMBERSLIST]);
                                    } else {}
                                  });
                                }
                              : observer.istextmessagingallowed == false
                                  ? () {
                                      Fiberchat.showRationale(getTranslated(
                                          this.context, 'textmssgnotallowed'));
                                    }
                                  : () => onSendMessage(
                                      context: context,
                                      content: textEditingController.value.text
                                          .trim(),
                                      type: MessageType.text,
                                      recipientList: broadcastList
                                          .toList()
                                          .firstWhere((element) =>
                                              element
                                                  .docmap[Dbkeys.broadcastID] ==
                                              widget.broadcastID)
                                          .docmap[Dbkeys.broadcastMEMBERSLIST])
                          : () {
                              Fiberchat.showRationale(getTranslated(
                                  this.context, 'mediamssgnotallowed'));
                            },
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

  buildEachMessage(Map<String, dynamic> doc, BroadcastModel broadcastData) {
    if (doc[Dbkeys.broadcastmsgTYPE] ==
        Dbkeys.broadcastmsgTYPEnotificationCreatedbroadcast) {
      return Center(
          child: Chip(
        labelStyle: TextStyle(fontSize: 13, fontWeight: FontWeight.w400),
        backgroundColor: Colors.blueGrey[50],
        label: Text(
          '${getTranslated(this.context, 'createdbroadcast')} ${doc[Dbkeys.broadcastmsgLISToptional].length} ${getTranslated(this.context, 'recipients')}',
          style: TextStyle(fontSize: 13, fontWeight: FontWeight.w400),
        ),
      ));
    } else if (doc[Dbkeys.broadcastmsgTYPE] ==
        Dbkeys.broadcastmsgTYPEnotificationAddedUser) {
      return Center(
          child: Chip(
        labelStyle: TextStyle(fontSize: 13, fontWeight: FontWeight.w400),
        backgroundColor: Colors.blueGrey[50],
        label: Text(
          doc[Dbkeys.broadcastmsgLISToptional].length > 1
              ? '${getTranslated(this.context, 'uhaveadded')} ${doc[Dbkeys.broadcastmsgLISToptional].length} ${getTranslated(this.context, 'recipients')}'
              : '${getTranslated(this.context, 'uhaveadded')} ${doc[Dbkeys.broadcastmsgLISToptional][0]} ',
          style: TextStyle(fontSize: 13, fontWeight: FontWeight.w400),
        ),
      ));
    } else if (doc[Dbkeys.broadcastmsgTYPE] ==
        Dbkeys.broadcastmsgTYPEnotificationUpdatedbroadcastDetails) {
      return Center(
          child: Chip(
        labelStyle: TextStyle(fontSize: 13, fontWeight: FontWeight.w400),
        backgroundColor: Colors.blueGrey[50],
        label: Text(
          getTranslated(this.context, 'uhaveupdatedbroadcast'),
          style: TextStyle(fontSize: 13, fontWeight: FontWeight.w400),
        ),
      ));
    } else if (doc[Dbkeys.broadcastmsgTYPE] ==
        Dbkeys.broadcastmsgTYPEnotificationUpdatedbroadcasticon) {
      return Center(
          child: Chip(
        labelStyle: TextStyle(fontSize: 13, fontWeight: FontWeight.w400),
        backgroundColor: Colors.blueGrey[50],
        label: Text(
          getTranslated(this.context, 'broadcasticonupdtd'),
          style: TextStyle(fontSize: 13, fontWeight: FontWeight.w400),
        ),
      ));
    } else if (doc[Dbkeys.broadcastmsgTYPE] ==
        Dbkeys.broadcastmsgTYPEnotificationDeletedbroadcasticon) {
      return Center(
          child: Chip(
        labelStyle: TextStyle(fontSize: 13, fontWeight: FontWeight.w400),
        backgroundColor: Colors.blueGrey[50],
        label: Text(
          getTranslated(this.context, 'broadcasticondlted'),
          style: TextStyle(fontSize: 13, fontWeight: FontWeight.w400),
        ),
      ));
    } else if (doc[Dbkeys.broadcastmsgTYPE] ==
        Dbkeys.broadcastmsgTYPEnotificationRemovedUser) {
      return Center(
          child: Chip(
        labelStyle: TextStyle(fontSize: 13, fontWeight: FontWeight.w400),
        backgroundColor: Colors.blueGrey[50],
        label: Text(
          '${getTranslated(this.context, 'youhaveremoved')} ${doc[Dbkeys.broadcastmsgLISToptional][0]}',
          style: TextStyle(fontSize: 13, fontWeight: FontWeight.w400),
        ),
      ));
    } else if (doc[Dbkeys.broadcastmsgTYPE] == MessageType.image.index ||
        doc[Dbkeys.broadcastmsgTYPE] == MessageType.doc.index ||
        doc[Dbkeys.broadcastmsgTYPE] == MessageType.text.index ||
        doc[Dbkeys.broadcastmsgTYPE] == MessageType.video.index ||
        doc[Dbkeys.broadcastmsgTYPE] == MessageType.audio.index ||
        doc[Dbkeys.broadcastmsgTYPE] == MessageType.contact.index ||
        doc[Dbkeys.broadcastmsgTYPE] == MessageType.location.index) {
      return buildMediaMessages(doc, broadcastData);
    }

    return Text(doc[Dbkeys.broadcastmsgCONTENT]);
  }

  contextMenu(BuildContext context, Map<String, dynamic> doc,
      {bool saved = false}) {
    List<Widget> tiles = List.from(<Widget>[]);

    if (doc[Dbkeys.broadcastmsgSENDBY] == widget.currentUserno) {
      tiles.add(ListTile(
          dense: true,
          leading: Icon(Icons.delete),
          title: Text(
            (doc[Dbkeys.messageType] == MessageType.image.index &&
                        !doc[Dbkeys.broadcastmsgCONTENT].contains('giphy')) ||
                    (doc[Dbkeys.messageType] == MessageType.doc.index) ||
                    (doc[Dbkeys.messageType] == MessageType.audio.index) ||
                    (doc[Dbkeys.messageType] == MessageType.video.index)
                ? getTranslated(this.context, 'dltforeveryone')
                : getTranslated(this.context, 'dltforme'),
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          onTap: () async {
            Navigator.of(this.context).pop();
            if (doc[Dbkeys.messageType] == MessageType.image.index &&
                !doc[Dbkeys.broadcastmsgCONTENT].contains('giphy')) {
              await FirebaseStorage.instance
                  .refFromURL(doc[Dbkeys.broadcastmsgCONTENT])
                  .delete();
            } else if (doc[Dbkeys.messageType] == MessageType.doc.index) {
              await FirebaseStorage.instance
                  .refFromURL(
                      doc[Dbkeys.broadcastmsgCONTENT].split('-BREAK-')[0])
                  .delete();
            } else if (doc[Dbkeys.messageType] == MessageType.audio.index) {
              await FirebaseStorage.instance
                  .refFromURL(
                      doc[Dbkeys.broadcastmsgCONTENT].split('-BREAK-')[0])
                  .delete();
            } else if (doc[Dbkeys.messageType] == MessageType.video.index) {
              await FirebaseStorage.instance
                  .refFromURL(
                      doc[Dbkeys.broadcastmsgCONTENT].split('-BREAK-')[0])
                  .delete();
              await FirebaseStorage.instance
                  .refFromURL(
                      doc[Dbkeys.broadcastmsgCONTENT].split('-BREAK-')[1])
                  .delete();
            }

            await FirebaseFirestore.instance
                .collection(DbPaths.collectionbroadcasts)
                .doc(widget.broadcastID)
                .collection(DbPaths.collectionbroadcastsChats)
                .doc(
                    '${doc[Dbkeys.broadcastmsgTIME]}--${doc[Dbkeys.broadcastmsgSENDBY]}')
                .delete();
            Fiberchat.toast(getTranslated(this.context, 'deleted'));
          }));
    }

    showDialog(
        context: this.context,
        builder: (context) {
          return SimpleDialog(children: tiles);
        });
  }

  Widget buildMediaMessages(
      Map<String, dynamic> doc, BroadcastModel broadcastData) {
    final observer = Provider.of<Observer>(this.context, listen: false);
    bool isMe = widget.currentUserno == doc[Dbkeys.broadcastmsgSENDBY];
    bool saved = false;
    bool isContainURL = false;
    try {
      isContainURL = Uri.tryParse(doc[Dbkeys.content]!) == null
          ? false
          : Uri.tryParse(doc[Dbkeys.content]!)!.isAbsolute;
    } on Exception catch (_) {
      isContainURL = false;
    }
    return Consumer<AvailableContactsProvider>(
        builder: (context, contactsProvider, _child) => InkWell(
              onLongPress: () {
                contextMenu(context, doc);
                hidekeyboard(context);
              },
              child: GroupChatBubble(
                isURLtext: doc[Dbkeys.messageType] == MessageType.text.index &&
                    isContainURL == true,
                is24hrsFormat: observer.is24hrsTimeformat,
                prefs: widget.prefs,
                currentUserNo: widget.currentUserno,
                model: widget.model,
                savednameifavailable: contactsProvider
                            .contactsBookContactList!.entries
                            .toList()
                            .indexWhere((element) =>
                                element.key ==
                                doc[Dbkeys.broadcastmsgSENDBY]) >=
                        0
                    ? contactsProvider.contactsBookContactList!.entries
                        .toList()[contactsProvider
                            .contactsBookContactList!.entries
                            .toList()
                            .indexWhere((element) =>
                                element.key == doc[Dbkeys.broadcastmsgSENDBY])]
                        .value
                    : null,
                postedbyname: contactsProvider
                            .alreadyJoinedUsersPhoneNameAsInServer
                            .indexWhere((element) =>
                                element.phone ==
                                doc[Dbkeys.broadcastmsgSENDBY]) >=
                        0
                    ? contactsProvider
                            .alreadyJoinedUsersPhoneNameAsInServer[
                                contactsProvider
                                    .alreadyJoinedUsersPhoneNameAsInServer
                                    .indexWhere((element) =>
                                        element.phone ==
                                        doc[Dbkeys.broadcastmsgSENDBY])]
                            .name ??
                        ''
                    : '',
                postedbyphone: doc[Dbkeys.broadcastmsgSENDBY],
                messagetype: doc[Dbkeys.broadcastmsgISDELETED] == true
                    ? MessageType.text
                    : doc[Dbkeys.messageType] == MessageType.text.index
                        ? MessageType.text
                        : doc[Dbkeys.messageType] == MessageType.contact.index
                            ? MessageType.contact
                            : doc[Dbkeys.messageType] ==
                                    MessageType.location.index
                                ? MessageType.location
                                : doc[Dbkeys.messageType] ==
                                        MessageType.image.index
                                    ? MessageType.image
                                    : doc[Dbkeys.messageType] ==
                                            MessageType.video.index
                                        ? MessageType.video
                                        : doc[Dbkeys.messageType] ==
                                                MessageType.doc.index
                                            ? MessageType.doc
                                            : doc[Dbkeys.messageType] ==
                                                    MessageType.audio.index
                                                ? MessageType.audio
                                                : MessageType.text,
                child: doc[Dbkeys.broadcastmsgISDELETED] == true
                    ? getTextMessage(isMe, doc, saved)
                    : doc[Dbkeys.messageType] == MessageType.text.index
                        ? getTextMessage(isMe, doc, saved)
                        : doc[Dbkeys.messageType] == MessageType.location.index
                            ? getLocationMessage(doc[Dbkeys.content],
                                saved: false)
                            : doc[Dbkeys.messageType] == MessageType.doc.index
                                ? getDocmessage(context, doc[Dbkeys.content],
                                    saved: false)
                                : doc[Dbkeys.messageType] ==
                                        MessageType.audio.index
                                    ? getAudiomessage(
                                        context, doc[Dbkeys.content],
                                        isMe: isMe, saved: false)
                                    : doc[Dbkeys.messageType] ==
                                            MessageType.video.index
                                        ? getVideoMessage(
                                            context, doc[Dbkeys.content],
                                            saved: false)
                                        : doc[Dbkeys.messageType] ==
                                                MessageType.contact.index
                                            ? getContactMessage(
                                                context, doc[Dbkeys.content],
                                                saved: false)
                                            : getImageMessage(
                                                doc,
                                                saved: saved,
                                              ),
                isMe: isMe,
                delivered: true,
                isContinuing: true,
                timestamp: doc[Dbkeys.broadcastmsgTIME],
              ),
            ));
  }

  Widget getVideoMessage(BuildContext context, String message,
      {bool saved = false}) {
    Map<dynamic, dynamic>? meta =
        jsonDecode((message.split('-BREAK-')[2]).toString());
    return Container(
      child: InkWell(
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
        child: Container(
          color: Colors.blueGrey,
          width: 230.0,
          height: 230.0,
          child: Stack(
            children: [
              CachedNetworkImage(
                placeholder: (context, url) => Container(
                  child: CircularProgressIndicator(
                    valueColor:
                        AlwaysStoppedAnimation<Color>(Colors.blueGrey[400]!),
                  ),
                  width: 230.0,
                  height: 230.0,
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
                    width: 230.0,
                    height: 230.0,
                    fit: BoxFit.cover,
                  ),
                  borderRadius: BorderRadius.all(
                    Radius.circular(0.0),
                  ),
                  clipBehavior: Clip.hardEdge,
                ),
                imageUrl: message.split('-BREAK-')[1],
                width: 230.0,
                height: 230.0,
                fit: BoxFit.cover,
              ),
              Container(
                color: Colors.black.withOpacity(0.4),
                width: 230.0,
                height: 230.0,
              ),
              Center(
                child: Icon(Icons.play_circle_fill_outlined,
                    color: Colors.white70, size: 65),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget getContactMessage(BuildContext context, String message,
      {bool saved = false}) {
    return SizedBox(
      width: 210,
      height: 75,
      child: Column(
        children: [
          ListTile(
            isThreeLine: false,
            leading: customCircleAvatar(url: null, radius: 20),
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
        ],
      ),
    );
  }

  Widget getTextMessage(bool isMe, Map<String, dynamic> doc, bool saved) {
    return selectablelinkify(
        doc[Dbkeys.broadcastmsgISDELETED] == true
            ? 'Message is deleted'
            : doc[Dbkeys.content],
        15.5,
        isMe ? TextAlign.right : TextAlign.left);
  }

  Widget getLocationMessage(String? message, {bool saved = false}) {
    return InkWell(
      onTap: () {
        custom_url_launcher(message!);
      },
      child: Image.asset(
        'assets/images/mapview.jpg',
        width: MediaQuery.of(this.context).size.width / 1.7,
        height: (MediaQuery.of(this.context).size.width / 1.7) * 0.6,
      ),
    );
  }

  Widget getAudiomessage(BuildContext context, String message,
      {bool saved = false, bool isMe = true}) {
    return Container(
      margin: EdgeInsets.only(bottom: 10),
      // width: 250,
      // height: 116,
      child: Column(
        children: [
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

  Widget getDocmessage(BuildContext context, String message,
      {bool saved = false}) {
    return SizedBox(
      width: 220,
      height: 116,
      child: Column(
        children: [
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
              // ignore: deprecated_member_use
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
    return Container(
      child: saved
          ? Material(
              child: Container(
                decoration: BoxDecoration(
                  image: DecorationImage(
                      image: Save.getImageFromBase64(doc[Dbkeys.content]).image,
                      fit: BoxFit.cover),
                ),
                width: doc[Dbkeys.content].contains('giphy') ? 140 : 230.0,
                height: doc[Dbkeys.content].contains('giphy') ? 140 : 230.0,
              ),
              borderRadius: BorderRadius.all(
                Radius.circular(8.0),
              ),
              clipBehavior: Clip.hardEdge,
            )
          : InkWell(
              onTap: () => Navigator.push(
                  this.context,
                  MaterialPageRoute(
                    builder: (context) => PhotoViewWrapper(
                      keyloader: _keyLoader,
                      imageUrl: doc[Dbkeys.content],
                      message: doc[Dbkeys.content],
                      tag: doc[Dbkeys.broadcastmsgTIME].toString(),
                      imageProvider:
                          CachedNetworkImageProvider(doc[Dbkeys.content]),
                    ),
                  )),
              child: CachedNetworkImage(
                placeholder: (context, url) => Container(
                  child: CircularProgressIndicator(
                    valueColor:
                        AlwaysStoppedAnimation<Color>(Colors.blueGrey[400]!),
                  ),
                  width: doc[Dbkeys.content].contains('giphy') ? 140 : 230.0,
                  height: doc[Dbkeys.content].contains('giphy') ? 140 : 230.0,
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
                    width: doc[Dbkeys.content].contains('giphy') ? 140 : 230.0,
                    height: doc[Dbkeys.content].contains('giphy') ? 140 : 230.0,
                    fit: BoxFit.cover,
                  ),
                  borderRadius: BorderRadius.all(
                    Radius.circular(8.0),
                  ),
                  clipBehavior: Clip.hardEdge,
                ),
                imageUrl: doc[Dbkeys.content],
                width: doc[Dbkeys.content].contains('giphy') ? 140 : 230.0,
                height: doc[Dbkeys.content].contains('giphy') ? 140 : 230.0,
                fit: BoxFit.cover,
              ),
            ),
    );
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
          'Location permissions are pdenied. Please go to settings & allow location tracking permission.');
      return Future.error('Location services are disabled.');
    }

    permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
      if (permission == LocationPermission.deniedForever) {
        // Permissions are denied forever, handle appropriately.
        Fiberchat.toast(
            'Location permissions are pdenied. Please go to settings & allow location tracking permission.');
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
      Fiberchat.toast(getTranslated(this.context, 'detectingloc'));
    }
    // When we reach here, permissions are granted and we can
    // continue accessing the position of the device.
    return await Geolocator.getCurrentPosition();
  }

  Widget buildMessagesUsingProvider(BuildContext context) {
    return Consumer<List<BroadcastModel>>(
        builder: (context, broadcastList, _child) =>
            Consumer<FirestoreDataProviderMESSAGESforBROADCASTCHATPAGE>(
                builder: (context, firestoreDataProvider, _) =>
                    InfiniteCOLLECTIONListViewWidget(
                      scrollController: realtime,
                      isreverse: true,
                      firestoreDataProviderMESSAGESforBROADCASTCHATPAGE:
                          firestoreDataProvider,
                      datatype: Dbkeys.datatypeBROADCASTCMSGS,
                      refdata: firestoreChatquery,
                      list: ListView.builder(
                          reverse: true,
                          padding: EdgeInsets.all(0),
                          physics: ScrollPhysics(),
                          shrinkWrap: true,
                          itemCount: firestoreDataProvider.recievedDocs.length,
                          itemBuilder: (BuildContext context, int i) {
                            var dc = firestoreDataProvider.recievedDocs[i];

                            return buildEachMessage(
                                dc,
                                broadcastList.lastWhere((element) =>
                                    element.docmap[Dbkeys.groupID] ==
                                    widget.broadcastID));
                          }),
                    )));
  }

  Widget buildLoadingThumbnail() {
    return Positioned(
      child: isgeneratingThumbnail
          ? Container(
              child: Center(
                child: CircularProgressIndicator(
                    valueColor: AlwaysStoppedAnimation<Color>(fiberchatBlue)),
              ),
              color: DESIGN_TYPE == Themetype.whatsapp
                  ? fiberchatBlack.withOpacity(0.2)
                  : fiberchatWhite.withOpacity(0.2),
            )
          : Container(),
    );
  }

  File? imageFile;

  shareMedia(BuildContext context, List<BroadcastModel> broadcastList) {
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
                                    builder: (context) => HybridDocumentPicker(
                                          title: getTranslated(
                                              this.context, 'pickdoc'),
                                          callback: getFileData,
                                        ))).then((url) async {
                              if (url != null) {
                                Fiberchat.toast(
                                  getTranslated(this.context, 'plswait'),
                                );

                                onSendMessage(
                                    context: this.context,
                                    content: url +
                                        '-BREAK-' +
                                        basename(imageFile!.path).toString(),
                                    type: MessageType.doc,
                                    recipientList: broadcastList
                                        .toList()
                                        .firstWhere((element) =>
                                            element
                                                .docmap[Dbkeys.broadcastID] ==
                                            widget.broadcastID)
                                        .docmap[Dbkeys.broadcastMEMBERSLIST]);
                                // Fiberchat.toast(
                                //     getTranslated(this.context, 'sent'));
                              } else {}
                            });
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
                          onPressed: () async {
                            hidekeyboard(context);
                            Navigator.of(context).pop();
                            File? selectedMedia =
                                await pickVideoFromgallery(context)
                                    .catchError((err) {
                              Fiberchat.toast(
                                  getTranslated(context, "invalidfile"));
                            });

                            if (selectedMedia == null) {
                              setStatusBarColor();
                            } else {
                              setStatusBarColor();
                              String fileExtension =
                                  p.extension(selectedMedia.path).toLowerCase();

                              if (fileExtension == ".mp4" ||
                                  fileExtension == ".mov") {
                                final tempDir = await getTemporaryDirectory();
                                File file = await File(
                                        '${tempDir.path}/${DateTime.now().millisecondsSinceEpoch}.mp4')
                                    .create();
                                file.writeAsBytesSync(
                                    selectedMedia.readAsBytesSync());

                                await Navigator.push(
                                    this.context,
                                    new MaterialPageRoute(
                                        builder: (context) => new VideoEditor(
                                            onClose: () {
                                              setStatusBarColor();
                                            },
                                            thumbnailQuality: 90,
                                            videoQuality: 100,
                                            maxDuration: 1900,
                                            onEditExported: (videoFile,
                                                thumnailFile) async {
                                              int timeStamp = DateTime.now()
                                                  .millisecondsSinceEpoch;
                                              String videoFileext =
                                                  p.extension(file.path);
                                              String videofileName =
                                                  'Video-$timeStamp$videoFileext';
                                              String? videoUrl =
                                                  await uploadSelectedLocalFileWithProgressIndicator(
                                                      file,
                                                      true,
                                                      false,
                                                      timeStamp,
                                                      filenameoptional:
                                                          videofileName);
                                              if (videoUrl != null) {
                                                String? thumnailUrl =
                                                    await uploadSelectedLocalFileWithProgressIndicator(
                                                        thumnailFile,
                                                        false,
                                                        true,
                                                        timeStamp,
                                                        filenameoptional:
                                                            videofileName);
                                                if (thumnailUrl != null) {
                                                  onSendMessage(
                                                      context: context,
                                                      content: videoUrl +
                                                          '-BREAK-' +
                                                          thumnailUrl +
                                                          '-BREAK-' +
                                                          videometadata! +
                                                          '-BREAK-' +
                                                          videofileName,
                                                      type: MessageType.video,
                                                      recipientList: broadcastList
                                                          .toList()
                                                          .firstWhere((element) =>
                                                              element.docmap[Dbkeys
                                                                  .broadcastID] ==
                                                              widget
                                                                  .broadcastID)
                                                          .docmap[Dbkeys.broadcastMEMBERSLIST]);
                                                  Fiberchat.toast(getTranslated(
                                                      this.context, 'sent'));
                                                  file.delete();
                                                  thumnailFile.delete();
                                                }
                                              }
                                            },
                                            file: File(file.path))));
                              } else {
                                Fiberchat.toast(
                                    "File type not supported. Please choose a valid .mp4, .mov file. \n\nSelected file was $fileExtension ");
                              }
                            }

                            //     onSendMessage(
                            //         context: context,
                            //         content: url +
                            //             '-BREAK-' +
                            //             thumbnailurl +
                            //             '-BREAK-' +
                            //             videometadata,
                            //         type: MessageType.video,
                            //         recipientList: broadcastList
                            //             .toList()
                            //             .firstWhere((element) =>
                            //                 element
                            //                     .docmap[Dbkeys.broadcastID] ==
                            //                 widget.broadcastID)
                            //             .docmap[Dbkeys.broadcastMEMBERSLIST]);
                            //     Fiberchat.toast(
                            //         getTranslated(this.context, 'sent'));
                            //   } else {}
                            // });
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
                          onPressed: () async {
                            hidekeyboard(context);
                            Navigator.of(context).pop();

                            await Navigator.push(
                                context,
                                new MaterialPageRoute(
                                    builder:
                                        (context) =>
                                            new CameraImageGalleryPicker(
                                              onTakeFile: (file) async {
                                                setStatusBarColor();

                                                int timeStamp = DateTime.now()
                                                    .millisecondsSinceEpoch;

                                                String? url =
                                                    await uploadSelectedLocalFileWithProgressIndicator(
                                                        file,
                                                        false,
                                                        false,
                                                        timeStamp);
                                                if (url != null) {
                                                  onSendMessage(
                                                      context: this.context,
                                                      content: url,
                                                      type: MessageType.image,
                                                      recipientList: broadcastList
                                                          .toList()
                                                          .firstWhere((element) =>
                                                              element.docmap[Dbkeys
                                                                  .broadcastID] ==
                                                              widget
                                                                  .broadcastID)
                                                          .docmap[Dbkeys.broadcastMEMBERSLIST]);
                                                  file.delete();
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
                                    context: context,
                                    content: url +
                                        '-BREAK-' +
                                        uploadTimestamp.toString(),
                                    type: MessageType.audio,
                                    recipientList: broadcastList
                                        .toList()
                                        .firstWhere((element) =>
                                            element
                                                .docmap[Dbkeys.broadcastID] ==
                                            widget.broadcastID)
                                        .docmap[Dbkeys.broadcastMEMBERSLIST]);
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
                                    context: context,
                                    content: locationstring,
                                    type: MessageType.location,
                                    recipientList: broadcastList
                                        .toList()
                                        .firstWhere((element) =>
                                            element
                                                .docmap[Dbkeys.broadcastID] ==
                                            widget.broadcastID)
                                        .docmap[Dbkeys.broadcastMEMBERSLIST]);
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
                                        currentUserNo: widget.currentUserno,
                                        model: widget.model,
                                        biometricEnabled: false,
                                        prefs: widget.prefs,
                                        onSelect: (name, phone) {
                                          onSendMessage(
                                              context: context,
                                              content: '$name-BREAK-$phone',
                                              type: MessageType.contact,
                                              recipientList: broadcastList
                                                      .toList()
                                                      .firstWhere((element) =>
                                                          element.docmap[Dbkeys
                                                              .broadcastID] ==
                                                          widget.broadcastID)
                                                      .docmap[
                                                  Dbkeys.broadcastMEMBERSLIST]);
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

  bool isemojiShowing = false;
  Future<bool> onWillPop() {
    if (isemojiShowing == true) {
      setState(() {
        isemojiShowing = false;
      });
      Future.value(false);
    } else {
      Navigator.of(this.context).pop();
      return Future.value(true);
    }
    return Future.value(false);
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
    return PickupLayout(
        prefs: widget.prefs,
        scaffold: Fiberchat.getNTPWrappedWidget(Consumer<List<BroadcastModel>>(
            builder: (context, broadcastList, _child) => WillPopScope(
                  onWillPop: isgeneratingThumbnail == true
                      ? () async {
                          return Future.value(false);
                        }
                      : isemojiShowing == true
                          ? () {
                              setState(() {
                                isemojiShowing = false;
                                keyboardFocusNode.unfocus();
                              });
                              return Future.value(false);
                            }
                          : () async {
                              setLastSeen(
                                false,
                              );

                              return Future.value(true);
                            },
                  child: Stack(
                    children: [
                      Scaffold(
                          key: _scaffold,
                          appBar: AppBar(
                            elevation:
                                DESIGN_TYPE == Themetype.messenger ? 0.4 : 1,
                            titleSpacing: 0,
                            leading: Container(
                              margin: EdgeInsets.only(right: 0),
                              width: 10,
                              child: IconButton(
                                icon: Icon(
                                  Icons.arrow_back,
                                  size: 24,
                                  color: DESIGN_TYPE == Themetype.whatsapp
                                      ? fiberchatWhite
                                      : fiberchatBlack,
                                ),
                                onPressed: onWillPop,
                              ),
                            ),
                            backgroundColor: DESIGN_TYPE == Themetype.whatsapp
                                ? fiberchatDeepGreen
                                : fiberchatWhite,
                            title: InkWell(
                              onTap: () {
                                Navigator.push(
                                    context,
                                    new MaterialPageRoute(
                                        builder: (context) => BroadcastDetails(
                                            model: widget.model,
                                            prefs: widget.prefs,
                                            currentUserno: widget.currentUserno,
                                            broadcastID: widget.broadcastID)));
                              },
                              child: Row(
                                mainAxisAlignment: MainAxisAlignment.start,
                                children: [
                                  Padding(
                                      padding:
                                          const EdgeInsets.fromLTRB(0, 7, 0, 7),
                                      child: customCircleAvatarBroadcast(
                                          radius: 20,
                                          url: broadcastList
                                                  .lastWhere((element) =>
                                                      element.docmap[
                                                          Dbkeys.broadcastID] ==
                                                      widget.broadcastID)
                                                  .docmap[
                                              Dbkeys.broadcastPHOTOURL])),
                                  SizedBox(
                                    width: 7,
                                  ),
                                  Expanded(
                                    child: Column(
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        Text(
                                          broadcastList
                                              .lastWhere((element) =>
                                                  element.docmap[
                                                      Dbkeys.broadcastID] ==
                                                  widget.broadcastID)
                                              .docmap[Dbkeys.broadcastNAME],
                                          maxLines: 1,
                                          overflow: TextOverflow.ellipsis,
                                          style: TextStyle(
                                              color: DESIGN_TYPE ==
                                                      Themetype.whatsapp
                                                  ? fiberchatWhite
                                                  : fiberchatBlack,
                                              fontSize: 17.0,
                                              fontWeight: FontWeight.w500),
                                        ),
                                        SizedBox(
                                          height: 6,
                                        ),
                                        SizedBox(
                                          width: MediaQuery.of(context)
                                                  .size
                                                  .width /
                                              1.3,
                                          child: Text(
                                            getTranslated(this.context,
                                                'tapforbroadcastinfo'),
                                            overflow: TextOverflow.ellipsis,
                                            maxLines: 1,
                                            style: TextStyle(
                                                color: DESIGN_TYPE ==
                                                        Themetype.whatsapp
                                                    ? fiberchatWhite
                                                    : fiberchatGrey,
                                                fontSize: 12,
                                                fontWeight: FontWeight.w400),
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),
                          body: Stack(children: <Widget>[
                            new Container(
                              decoration: new BoxDecoration(
                                color: DESIGN_TYPE == Themetype.whatsapp
                                    ? fiberchatChatbackground
                                    : fiberchatChatbackground,
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
                                    broadcastList
                                                .lastWhere((element) =>
                                                    element.docmap[
                                                        Dbkeys.broadcastID] ==
                                                    widget.broadcastID)
                                                .docmap[
                                                    Dbkeys.broadcastMEMBERSLIST]
                                                .length >
                                            0
                                        // ? Platform.isAndroid
                                        ? buildInputAndroid(
                                            context,
                                            isemojiShowing,
                                            refreshInput,
                                            _keyboardVisible,
                                            broadcastList)
                                        // : buildInputIos(
                                        //     context, broadcastList)
                                        : Container(
                                            alignment: Alignment.center,
                                            padding: EdgeInsets.fromLTRB(
                                                14, 7, 14, 7),
                                            color: Colors.white,
                                            height: 70,
                                            width: MediaQuery.of(context)
                                                .size
                                                .width,
                                            child: Text(
                                              getTranslated(
                                                  this.context, 'norecp'),
                                              textAlign: TextAlign.center,
                                              style: TextStyle(height: 1.3),
                                            ),
                                          ),
                                  ])
                            ]),
                          ])),
                      buildLoadingThumbnail(),
                    ],
                  ),
                ))));
  }

  Widget selectablelinkify(
      String? text, double? fontsize, TextAlign? textalign) {
    bool isContainURL = false;
    try {
      isContainURL =
          Uri.tryParse(text!) == null ? false : Uri.tryParse(text)!.isAbsolute;
    } on Exception catch (_) {
      isContainURL = false;
    }
    return isContainURL == false
        ? SelectableLinkify(
            style: TextStyle(
                fontSize: isAllEmoji(text!) ? fontsize! * 2 : fontsize,
                color: Colors.black87),
            text: text,
            onOpen: (link) async {
              custom_url_launcher(link.url);
            },
          )
        : LinkPreviewGenerator(
            removeElevation: true,
            graphicFit: BoxFit.contain,
            borderRadius: 5,
            showDomain: true,
            titleStyle: TextStyle(
                fontSize: 13, height: 1.4, fontWeight: FontWeight.bold),
            showBody: true,
            bodyStyle: TextStyle(fontSize: 11.6, color: Colors.black45),
            placeholderWidget: SelectableLinkify(
              textAlign: textalign,
              style: TextStyle(fontSize: fontsize, color: Colors.black87),
              text: text!,
              onOpen: (link) async {
                custom_url_launcher(link.url);
              },
            ),
            errorWidget: SelectableLinkify(
              style: TextStyle(fontSize: fontsize, color: Colors.black87),
              text: text,
              textAlign: textalign,
              onOpen: (link) async {
                custom_url_launcher(link.url);
              },
            ),
            link: text,
            linkPreviewStyle: LinkPreviewStyle.large,
          );
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed)
      setLastSeen(false);
    else
      setLastSeen(false);
  }
}

deletedGroupWidget() {
  return Scaffold(
    appBar: AppBar(),
    body: Container(
      child: Center(
        child: Padding(
          padding: const EdgeInsets.all(18.0),
          child: Text(
            'This Broadcast Has been deleted by Admin OR you have been removed from this group.',
            textAlign: TextAlign.center,
          ),
        ),
      ),
    ),
  );
}
