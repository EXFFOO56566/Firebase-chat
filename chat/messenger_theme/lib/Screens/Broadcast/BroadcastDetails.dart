//*************   © Copyrighted by Thinkcreative_Technologies. An Exclusive item of Envato market. Make sure you have purchased a Regular License OR Extended license for the Source Code from Envato to use this product. See the License Defination attached with source code. *********************

import 'dart:io';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:fiberchat/Configs/Dbkeys.dart';
import 'package:fiberchat/Configs/Dbpaths.dart';
import 'package:fiberchat/Configs/app_constants.dart';
import 'package:fiberchat/Configs/optional_constants.dart';
import 'package:fiberchat/Models/DataModel.dart';
import 'package:fiberchat/Screens/Broadcast/AddContactsToBroadcast.dart';
import 'package:fiberchat/Screens/Broadcast/EditBroadcastDetails.dart';
import 'package:fiberchat/Screens/call_history/callhistory.dart';
import 'package:fiberchat/Screens/calling_screen/pickup_layout.dart';
import 'package:fiberchat/Screens/profile_settings/profile_view.dart';
import 'package:fiberchat/Services/Admob/admob.dart';
import 'package:fiberchat/Services/Providers/BroadcastProvider.dart';
import 'package:fiberchat/Services/Providers/AvailableContactsProvider.dart';
import 'package:fiberchat/Services/Providers/Observer.dart';
import 'package:fiberchat/Services/localization/language_constants.dart';
import 'package:fiberchat/Utils/utils.dart';
import 'package:fiberchat/widgets/ImagePicker/image_picker.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:flutter/material.dart';
import 'package:flutter_image_compress/flutter_image_compress.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:intl/intl.dart';
import 'package:path/path.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:fiberchat/Configs/Enum.dart';

class BroadcastDetails extends StatefulWidget {
  final DataModel? model;
  final SharedPreferences prefs;
  final String currentUserno;
  final String broadcastID;
  const BroadcastDetails(
      {Key? key,
      this.model,
      required this.prefs,
      required this.currentUserno,
      required this.broadcastID})
      : super(key: key);

  @override
  _BroadcastDetailsState createState() => _BroadcastDetailsState();
}

class _BroadcastDetailsState extends State<BroadcastDetails> {
  File? imageFile;

  getImage(File image) {
    // ignore: unnecessary_null_comparison
    if (image != null) {
      setStateIfMounted(() {
        imageFile = image;
      });
    }
    return uploadFile(false);
  }

  bool isloading = false;
  String? videometadata;
  int? uploadTimestamp;
  int? thumnailtimestamp;
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
    WidgetsBinding.instance.addPostFrameCallback((timeStamp) {
      final observer = Provider.of<Observer>(this.context, listen: false);
      if (IsBannerAdShow == true && observer.isadmobshow == true) {
        myBanner.load();
        adWidget = AdWidget(ad: myBanner);
        setState(() {});
      }
    });
  }

  Future uploadFile(bool isthumbnail) async {
    uploadTimestamp = DateTime.now().millisecondsSinceEpoch;
    String fileName = 'BROADCAST_ICON';
    Reference reference = FirebaseStorage.instance
        .ref("+00_BROADCAST_MEDIA/${widget.broadcastID}/")
        .child(fileName);
    File? compressedImage;

    final targetPath = imageFile!.absolute.path
            .replaceAll(basename(imageFile!.absolute.path), "") +
        "temp.jpg";

    compressedImage = await FlutterImageCompress.compressAndGetFile(
      imageFile!.absolute.path,
      targetPath,
      quality: DpImageQualityCompress,
      rotate: 0,
    );

    TaskSnapshot uploading = await reference.putFile(compressedImage!);
    if (isthumbnail == false) {
      setStateIfMounted(() {
        thumnailtimestamp = uploadTimestamp;
      });
    }

    return uploading.ref.getDownloadURL();
  }

  void setStateIfMounted(f) {
    if (mounted) setState(f);
  }

  userAction(
    value,
    String targetPhone,
  ) async {
    if (value == 'Remove from List') {
      showDialog(
        builder: (BuildContext context) {
          return AlertDialog(
            title: new Text(
              getTranslated(context, 'removefromlist'),
            ),
            actions: [
              // ignore: deprecated_member_use
              FlatButton(
                  child: Text(
                    getTranslated(context, 'cancel'),
                    style: TextStyle(color: fiberchatgreen, fontSize: 18),
                  ),
                  onPressed: () {
                    Navigator.of(context).pop();
                  }),
              // ignore: deprecated_member_use
              FlatButton(
                child: Text(
                  getTranslated(context, 'remove'),
                  style: TextStyle(color: Colors.red, fontSize: 18),
                ),
                onPressed: () async {
                  Navigator.of(context).pop();
                  setStateIfMounted(() {
                    isloading = true;
                  });
                  await FirebaseFirestore.instance
                      .collection(DbPaths.collectionbroadcasts)
                      .doc(widget.broadcastID)
                      .update({
                    Dbkeys.broadcastMEMBERSLIST:
                        FieldValue.arrayRemove([targetPhone]),
                  }).then((value) async {
                    DateTime time = DateTime.now();
                    await FirebaseFirestore.instance
                        .collection(DbPaths.collectionbroadcasts)
                        .doc(widget.broadcastID)
                        .collection(DbPaths.collectionbroadcastsChats)
                        .doc(time.millisecondsSinceEpoch.toString() +
                            '--' +
                            widget.currentUserno)
                        .set({
                      Dbkeys.broadcastmsgCONTENT:
                          '${getTranslated(context, 'youhaveremoved')} $targetPhone',
                      Dbkeys.broadcastmsgLISToptional: [
                        targetPhone,
                      ],
                      Dbkeys.broadcastmsgTIME: time.millisecondsSinceEpoch,
                      Dbkeys.broadcastmsgSENDBY: widget.currentUserno,
                      Dbkeys.broadcastmsgISDELETED: false,
                      Dbkeys.broadcastmsgTYPE:
                          Dbkeys.broadcastmsgTYPEnotificationRemovedUser,
                    });
                    setStateIfMounted(() {
                      isloading = false;
                    });
                  }).catchError((onError) {
                    setStateIfMounted(() {
                      isloading = false;
                    });
                    // Fiberchat.toast(
                    //     'Failed to remove ! \nError occured -$onError');
                  });
                },
              )
            ],
          );
        },
        context: this.context,
      );
    }
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
    var w = MediaQuery.of(context).size.width;

    return PickupLayout(
        prefs: widget.prefs,
        scaffold: Fiberchat.getNTPWrappedWidget(Consumer<List<BroadcastModel>>(
            builder: (context, broadcastList, _child) {
          final observer = Provider.of<Observer>(context, listen: false);
          Map<dynamic, dynamic> broadcastDoc = broadcastList.indexWhere(
                      (element) =>
                          element.docmap[Dbkeys.broadcastID] ==
                          widget.broadcastID) <
                  0
              ? {}
              : broadcastList
                  .lastWhere((element) =>
                      element.docmap[Dbkeys.broadcastID] == widget.broadcastID)
                  .docmap;
          return Consumer<AvailableContactsProvider>(
              builder: (context, availableContacts, _child) => Scaffold(
                    bottomSheet: IsBannerAdShow == true &&
                            observer.isadmobshow == true &&
                            adWidget != null
                        ? Container(
                            height: 60,
                            margin: EdgeInsets.only(
                                bottom: Platform.isIOS == true ? 25.0 : 5,
                                top: 0),
                            child: Center(child: adWidget),
                          )
                        : SizedBox(
                            height: 0,
                          ),
                    backgroundColor: Color(0xfff2f2f2),
                    appBar: AppBar(
                      elevation: DESIGN_TYPE == Themetype.messenger ? 0.4 : 1,
                      titleSpacing: -5,
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
                          onPressed: () {
                            Navigator.of(context).pop();
                          },
                        ),
                      ),
                      actions: <Widget>[
                        IconButton(
                            onPressed: () {
                              Navigator.push(
                                  this.context,
                                  new MaterialPageRoute(
                                      builder: (context) =>
                                          new EditBroadcastDetails(
                                            prefs: widget.prefs,
                                            currentUserNo: widget.currentUserno,
                                            isadmin: true,
                                            broadcastDesc: broadcastDoc[
                                                Dbkeys.broadcastDESCRIPTION],
                                            broadcastName: broadcastDoc[
                                                Dbkeys.broadcastNAME],
                                            broadcastID: widget.broadcastID,
                                          )));
                            },
                            icon: Icon(
                              Icons.edit,
                              size: 21,
                              color: DESIGN_TYPE == Themetype.whatsapp
                                  ? fiberchatWhite
                                  : fiberchatBlack,
                            ))
                      ],
                      backgroundColor: DESIGN_TYPE == Themetype.whatsapp
                          ? fiberchatDeepGreen
                          : fiberchatWhite,
                      title: InkWell(
                        onTap: () {},
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              broadcastDoc[Dbkeys.broadcastNAME],
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style: TextStyle(
                                  color: DESIGN_TYPE == Themetype.whatsapp
                                      ? fiberchatWhite
                                      : fiberchatBlack,
                                  fontSize: 17.0,
                                  fontWeight: FontWeight.w500),
                            ),
                            SizedBox(
                              height: 4,
                            ),
                            Text(
                              '${getTranslated(context, 'createdbyu')}, ${formatDate(broadcastDoc[Dbkeys.broadcastCREATEDON].toDate())}',
                              style: TextStyle(
                                  color: DESIGN_TYPE == Themetype.whatsapp
                                      ? fiberchatWhite
                                      : fiberchatGrey,
                                  fontSize: 12,
                                  fontWeight: FontWeight.w400),
                            ),
                          ],
                        ),
                      ),
                    ),
                    body: Padding(
                      padding: EdgeInsets.only(
                          bottom: IsBannerAdShow == true &&
                                  observer.isadmobshow == true
                              ? 60
                              : 0),
                      child: Stack(
                        children: [
                          ListView(
                            children: [
                              Stack(
                                children: [
                                  CachedNetworkImage(
                                    imageUrl: broadcastDoc[
                                            Dbkeys.broadcastPHOTOURL] ??
                                        '',
                                    imageBuilder: (context, imageProvider) =>
                                        Container(
                                      width: w,
                                      height: w / 1.2,
                                      decoration: BoxDecoration(
                                        shape: BoxShape.rectangle,
                                        image: DecorationImage(
                                            image: imageProvider,
                                            fit: BoxFit.cover),
                                      ),
                                    ),
                                    placeholder: (context, url) => Container(
                                      width: w,
                                      height: w / 1.2,
                                      decoration: BoxDecoration(
                                        color: Colors.white,
                                        shape: BoxShape.rectangle,
                                      ),
                                      child: Icon(Icons.campaign,
                                          color: fiberchatGrey.withOpacity(0.5),
                                          size: 75),
                                    ),
                                    errorWidget: (context, url, error) =>
                                        Container(
                                      width: w,
                                      height: w / 1.2,
                                      decoration: BoxDecoration(
                                        color: Colors.white,
                                        shape: BoxShape.rectangle,
                                      ),
                                      child: Icon(Icons.campaign,
                                          color: fiberchatGrey.withOpacity(0.5),
                                          size: 75),
                                    ),
                                  ),
                                  Container(
                                    alignment: Alignment.bottomRight,
                                    width: w,
                                    height: w / 1.2,
                                    decoration: BoxDecoration(
                                      color: broadcastDoc[
                                                  Dbkeys.broadcastPHOTOURL] ==
                                              null
                                          ? Colors.black.withOpacity(0.2)
                                          : Colors.black.withOpacity(0.4),
                                      shape: BoxShape.rectangle,
                                    ),
                                    child: Padding(
                                      padding: const EdgeInsets.all(18.0),
                                      child: Row(
                                        mainAxisSize: MainAxisSize.min,
                                        children: [
                                          IconButton(
                                            onPressed: () {
                                              Navigator.push(
                                                  context,
                                                  MaterialPageRoute(
                                                      builder: (context) =>
                                                          SingleImagePicker(
                                                            title: getTranslated(
                                                                this.context,
                                                                'pickimage'),
                                                            callback: getImage,
                                                          ))).then((url) async {
                                                if (url != null) {
                                                  await FirebaseFirestore
                                                      .instance
                                                      .collection(DbPaths
                                                          .collectionbroadcasts)
                                                      .doc(widget.broadcastID)
                                                      .update({
                                                    Dbkeys.broadcastPHOTOURL:
                                                        url
                                                  }).then((value) async {
                                                    DateTime time =
                                                        DateTime.now();
                                                    await FirebaseFirestore
                                                        .instance
                                                        .collection(DbPaths
                                                            .collectionbroadcasts)
                                                        .doc(widget.broadcastID)
                                                        .collection(DbPaths
                                                            .collectionbroadcastsChats)
                                                        .doc(time
                                                                .millisecondsSinceEpoch
                                                                .toString() +
                                                            '--' +
                                                            widget.currentUserno
                                                                .toString())
                                                        .set({
                                                      Dbkeys.broadcastmsgCONTENT:
                                                          getTranslated(context,
                                                              'broadcasticonupdtd'),
                                                      Dbkeys.broadcastmsgLISToptional:
                                                          [],
                                                      Dbkeys.broadcastmsgTIME: time
                                                          .millisecondsSinceEpoch,
                                                      Dbkeys.broadcastmsgSENDBY:
                                                          widget.currentUserno,
                                                      Dbkeys.broadcastmsgISDELETED:
                                                          false,
                                                      Dbkeys.broadcastmsgTYPE:
                                                          Dbkeys
                                                              .broadcastmsgTYPEnotificationUpdatedbroadcasticon,
                                                    });
                                                  });
                                                } else {}
                                              });
                                            },
                                            icon: Icon(Icons.camera_alt_rounded,
                                                color: fiberchatWhite,
                                                size: 35),
                                          ),
                                          broadcastDoc[Dbkeys
                                                      .broadcastPHOTOURL] ==
                                                  null
                                              ? SizedBox()
                                              : IconButton(
                                                  onPressed: () async {
                                                    Fiberchat.toast(
                                                      getTranslated(
                                                          context, 'plswait'),
                                                    );
                                                    await FirebaseStorage
                                                        .instance
                                                        .refFromURL(
                                                            broadcastDoc[Dbkeys
                                                                .broadcastPHOTOURL])
                                                        .delete()
                                                        .then((d) async {
                                                      await FirebaseFirestore
                                                          .instance
                                                          .collection(DbPaths
                                                              .collectionbroadcasts)
                                                          .doc(widget
                                                              .broadcastID)
                                                          .update({
                                                        Dbkeys.broadcastPHOTOURL:
                                                            null,
                                                      });
                                                      DateTime time =
                                                          DateTime.now();
                                                      await FirebaseFirestore
                                                          .instance
                                                          .collection(DbPaths
                                                              .collectionbroadcasts)
                                                          .doc(widget
                                                              .broadcastID)
                                                          .collection(DbPaths
                                                              .collectionbroadcastsChats)
                                                          .doc(time
                                                                  .millisecondsSinceEpoch
                                                                  .toString() +
                                                              '--' +
                                                              widget
                                                                  .currentUserno
                                                                  .toString())
                                                          .set({
                                                        Dbkeys.broadcastmsgCONTENT:
                                                            getTranslated(
                                                                context,
                                                                'broadcasticondlted'),
                                                        Dbkeys.broadcastmsgLISToptional:
                                                            [],
                                                        Dbkeys.broadcastmsgTIME:
                                                            time.millisecondsSinceEpoch,
                                                        Dbkeys.broadcastmsgSENDBY:
                                                            widget
                                                                .currentUserno,
                                                        Dbkeys.broadcastmsgISDELETED:
                                                            false,
                                                        Dbkeys.broadcastmsgTYPE:
                                                            Dbkeys
                                                                .broadcastmsgTYPEnotificationDeletedbroadcasticon,
                                                      });
                                                    }).catchError(
                                                            (error) async {
                                                      if (error.toString().contains(Dbkeys.firebaseStorageNoObjectFound1) ||
                                                          error
                                                              .toString()
                                                              .contains(Dbkeys
                                                                  .firebaseStorageNoObjectFound2) ||
                                                          error
                                                              .toString()
                                                              .contains(Dbkeys
                                                                  .firebaseStorageNoObjectFound3) ||
                                                          error
                                                              .toString()
                                                              .contains(Dbkeys
                                                                  .firebaseStorageNoObjectFound4)) {
                                                        await FirebaseFirestore
                                                            .instance
                                                            .collection(DbPaths
                                                                .collectionbroadcasts)
                                                            .doc(widget
                                                                .broadcastID)
                                                            .update({
                                                          Dbkeys.broadcastPHOTOURL:
                                                              null,
                                                        });
                                                      }
                                                    });
                                                  },
                                                  icon: Icon(
                                                      Icons
                                                          .delete_outline_rounded,
                                                      color: fiberchatWhite,
                                                      size: 35),
                                                ),
                                        ],
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                              Container(
                                color: Colors.white,
                                padding: EdgeInsets.all(12),
                                child: Column(
                                  crossAxisAlignment:
                                      CrossAxisAlignment.stretch,
                                  children: [
                                    Row(
                                      mainAxisSize: MainAxisSize.max,
                                      mainAxisAlignment:
                                          MainAxisAlignment.spaceBetween,
                                      children: [
                                        Text(
                                          getTranslated(context, 'desc'),
                                          textAlign: TextAlign.left,
                                          style: TextStyle(
                                              fontWeight: FontWeight.bold,
                                              color: fiberchatgreen,
                                              fontSize: 16),
                                        ),
                                        IconButton(
                                            onPressed: () {
                                              Navigator.push(
                                                  this.context,
                                                  new MaterialPageRoute(
                                                      builder: (context) =>
                                                          new EditBroadcastDetails(
                                                            prefs: widget.prefs,
                                                            currentUserNo: widget
                                                                .currentUserno,
                                                            isadmin: true,
                                                            broadcastDesc:
                                                                broadcastDoc[Dbkeys
                                                                    .broadcastDESCRIPTION],
                                                            broadcastName:
                                                                broadcastDoc[Dbkeys
                                                                    .broadcastNAME],
                                                            broadcastID: widget
                                                                .broadcastID,
                                                          )));
                                            },
                                            icon: Icon(
                                              Icons.edit,
                                              color: fiberchatGrey,
                                            ))
                                      ],
                                    ),
                                    Divider(),
                                    SizedBox(
                                      height: 7,
                                    ),
                                    Text(
                                      broadcastDoc[Dbkeys
                                                  .broadcastDESCRIPTION] ==
                                              ''
                                          ? getTranslated(context, 'nodesc')
                                          : broadcastList
                                                  .lastWhere((element) =>
                                                      element.docmap[
                                                          Dbkeys.broadcastID] ==
                                                      widget.broadcastID)
                                                  .docmap[
                                              Dbkeys.broadcastDESCRIPTION],
                                      textAlign: TextAlign.left,
                                      style: TextStyle(
                                          fontWeight: FontWeight.normal,
                                          color: fiberchatBlack,
                                          fontSize: 15.3),
                                    ),
                                    SizedBox(
                                      height: 7,
                                    ),
                                  ],
                                ),
                              ),
                              SizedBox(
                                height: 20,
                              ),
                              Container(
                                color: Colors.white,
                                padding: EdgeInsets.all(12),
                                child: Column(
                                  crossAxisAlignment:
                                      CrossAxisAlignment.stretch,
                                  children: [
                                    Row(
                                      mainAxisAlignment:
                                          MainAxisAlignment.spaceBetween,
                                      children: [
                                        SizedBox(
                                          width: 150,
                                          child: Row(
                                            mainAxisAlignment:
                                                MainAxisAlignment.start,
                                            crossAxisAlignment:
                                                CrossAxisAlignment.center,
                                            children: [
                                              Text(
                                                '${broadcastList.lastWhere((element) => element.docmap[Dbkeys.broadcastID] == widget.broadcastID).docmap[Dbkeys.broadcastMEMBERSLIST].length} ${getTranslated(context, 'recipients')}  ',
                                                textAlign: TextAlign.left,
                                                style: TextStyle(
                                                    fontWeight: FontWeight.bold,
                                                    color: fiberchatgreen,
                                                    fontSize: 16),
                                              ),
                                              // Text(
                                              //   '${broadcastList.firstWhere((element) => element.docmap[Dbkeys.broadcastID] == widget.broadcastID).docmap[Dbkeys.groupMEMBERSLIST].length}',
                                              //   style: TextStyle(
                                              //       fontWeight: FontWeight.bold,
                                              //       fontSize: 16),
                                              // ),
                                            ],
                                          ),
                                        ),
                                        (broadcastDoc[Dbkeys
                                                        .broadcastMEMBERSLIST]
                                                    .length >=
                                                observer.broadcastMemberslimit)
                                            ? SizedBox()
                                            : InkWell(
                                                onTap: () {
                                                  final AvailableContactsProvider
                                                      dbcontactsProvider =
                                                      Provider.of<
                                                              AvailableContactsProvider>(
                                                          context,
                                                          listen: false);
                                                  dbcontactsProvider
                                                      .fetchContacts(
                                                          context,
                                                          widget.model,
                                                          widget.currentUserno,
                                                          widget.prefs);
                                                  Navigator.push(
                                                      context,
                                                      MaterialPageRoute(
                                                          builder: (context) =>
                                                              AddContactsToBroadcast(
                                                                currentUserNo:
                                                                    widget
                                                                        .currentUserno,
                                                                model: widget
                                                                    .model,
                                                                biometricEnabled:
                                                                    false,
                                                                prefs: widget
                                                                    .prefs,
                                                                broadcastID: widget
                                                                    .broadcastID,
                                                                isAddingWhileCreatingBroadcast:
                                                                    false,
                                                              )));
                                                },
                                                child: SizedBox(
                                                  height: 50,
                                                  // width: 70,
                                                  child: Row(
                                                    mainAxisAlignment:
                                                        MainAxisAlignment.end,
                                                    crossAxisAlignment:
                                                        CrossAxisAlignment
                                                            .center,
                                                    children: [
                                                      SizedBox(
                                                        width: 30,
                                                        child: Icon(Icons.add,
                                                            size: 19,
                                                            color:
                                                                fiberchatLightGreen),
                                                      ),
                                                      // Text(
                                                      //   'ADD ',
                                                      //   style: TextStyle(
                                                      //       fontWeight:
                                                      //           FontWeight.bold,
                                                      //       color:
                                                      //           fiberchatLightGreen),
                                                      // ),
                                                    ],
                                                  ),
                                                ),
                                              ),
                                      ],
                                    ),
                                    // Divider(),
                                    getUsersList(),
                                  ],
                                ),
                              ),
                              widget.currentUserno ==
                                      broadcastDoc[Dbkeys.broadcastCREATEDBY]
                                  ? InkWell(
                                      onTap: () {
                                        showDialog(
                                          builder: (BuildContext context) {
                                            return AlertDialog(
                                              title: new Text(
                                                getTranslated(
                                                    context, 'deletebroadcast'),
                                              ),
                                              actions: [
                                                // ignore: deprecated_member_use
                                                FlatButton(
                                                  child: Text(
                                                    getTranslated(
                                                        context, 'cancel'),
                                                    style: TextStyle(
                                                        color: fiberchatgreen,
                                                        fontSize: 18),
                                                  ),
                                                  onPressed: () {
                                                    Navigator.of(context).pop();
                                                  },
                                                ),
                                                // ignore: deprecated_member_use
                                                FlatButton(
                                                  child: Text(
                                                    getTranslated(
                                                        context, 'delete'),
                                                    style: TextStyle(
                                                        color: Colors.red,
                                                        fontSize: 18),
                                                  ),
                                                  onPressed: () async {
                                                    Navigator.of(context).pop();
                                                    Navigator.of(context).pop();
                                                    Navigator.of(context).pop();

                                                    Future.delayed(
                                                        const Duration(
                                                            milliseconds: 500),
                                                        () async {
                                                      await FirebaseFirestore
                                                          .instance
                                                          .collection(DbPaths
                                                              .collectionbroadcasts)
                                                          .doc(widget
                                                              .broadcastID)
                                                          .get()
                                                          .then((doc) async {
                                                        await doc.reference
                                                            .delete();
                                                        //No need to delete the media data from here as it will be deleted automatically using Cloud functions deployed in Firebase once the .doc is deleted .
                                                      });
                                                    });
                                                  },
                                                )
                                              ],
                                            );
                                          },
                                          context: context,
                                        );
                                      },
                                      child: Container(
                                          alignment: Alignment.center,
                                          margin: EdgeInsets.fromLTRB(
                                              10, 30, 10, 30),
                                          width:
                                              MediaQuery.of(context).size.width,
                                          height: 48.0,
                                          decoration: new BoxDecoration(
                                            color: Colors.red[700],
                                            borderRadius:
                                                new BorderRadius.circular(5.0),
                                          ),
                                          child: Text(
                                            getTranslated(
                                                context, 'deletebroadcast'),
                                            style: TextStyle(
                                                color: Colors.white,
                                                fontWeight: FontWeight.w500,
                                                fontSize: 16),
                                          )),
                                    )
                                  : SizedBox()
                            ],
                          ),
                          Positioned(
                            child: isloading
                                ? Container(
                                    child: Center(
                                      child: CircularProgressIndicator(
                                          valueColor:
                                              AlwaysStoppedAnimation<Color>(
                                                  fiberchatBlue)),
                                    ),
                                    color: DESIGN_TYPE == Themetype.whatsapp
                                        ? fiberchatBlack.withOpacity(0.6)
                                        : fiberchatWhite.withOpacity(0.6))
                                : Container(),
                          )
                        ],
                      ),
                    ),
                  ));
        })));
  }

  getUsersList() {
    return Consumer<List<BroadcastModel>>(
        builder: (context, broadcastList, _child) {
      Map<dynamic, dynamic> broadcastDoc = broadcastList
          .lastWhere((element) =>
              element.docmap[Dbkeys.broadcastID] == widget.broadcastID)
          .docmap;

      return Consumer<AvailableContactsProvider>(
          builder: (context, availableContacts, _child) {
        List onlyuserslist = broadcastDoc[Dbkeys.broadcastMEMBERSLIST];
        broadcastDoc[Dbkeys.broadcastMEMBERSLIST].toList().forEach((member) {
          if (broadcastDoc[Dbkeys.broadcastADMINLIST].contains(member)) {
            onlyuserslist.remove(member);
          }
        });
        return ListView.builder(
            scrollDirection: Axis.vertical,
            shrinkWrap: true,
            physics: ScrollPhysics(),
            itemCount: onlyuserslist.length,
            itemBuilder: (context, int i) {
              List viewerslist = onlyuserslist;
              return FutureBuilder<Map<String, dynamic>>(
                  future: availableContacts.getUserDoc(viewerslist[i]),
                  builder:
                      (context, AsyncSnapshot<Map<String, dynamic>> snapshot) {
                    if (snapshot.hasData) {
                      return Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Divider(
                            height: 3,
                          ),
                          Stack(
                            children: [
                              ListTile(
                                trailing: SizedBox(
                                  width: 30,
                                  child: PopupMenuButton<String>(
                                      itemBuilder: (BuildContext context) =>
                                          <PopupMenuEntry<String>>[
                                            PopupMenuItem<String>(
                                              value: 'Remove from List',
                                              child: Text(
                                                getTranslated(
                                                    context, 'removefromlist'),
                                              ),
                                            ),
                                          ],
                                      onSelected: (String value) {
                                        userAction(
                                          value,
                                          viewerslist[i],
                                        );
                                      },
                                      child: Icon(
                                        Icons.more_vert_outlined,
                                        size: 20,
                                        color: fiberchatBlack,
                                      )),
                                ),
                                isThreeLine: false,
                                contentPadding: EdgeInsets.fromLTRB(0, 0, 0, 0),
                                leading: Padding(
                                  padding: const EdgeInsets.only(left: 5),
                                  child: Padding(
                                    padding: const EdgeInsets.all(0.0),
                                    child:
                                        snapshot.data![Dbkeys.photoUrl] == null
                                            ? Container(
                                                width: 50.0,
                                                height: 50.0,
                                                child: Icon(Icons.person),
                                                decoration: BoxDecoration(
                                                  color: Colors.grey[300],
                                                  shape: BoxShape.circle,
                                                ),
                                              )
                                            : CachedNetworkImage(
                                                imageUrl: snapshot.data![
                                                        Dbkeys.photoUrl] ??
                                                    '',
                                                imageBuilder:
                                                    (context, imageProvider) =>
                                                        Container(
                                                          width: 40.0,
                                                          height: 40.0,
                                                          decoration:
                                                              BoxDecoration(
                                                            shape:
                                                                BoxShape.circle,
                                                            image: DecorationImage(
                                                                image:
                                                                    imageProvider,
                                                                fit: BoxFit
                                                                    .cover),
                                                          ),
                                                        ),
                                                placeholder: (context, url) =>
                                                    Container(
                                                      width: 40.0,
                                                      height: 40.0,
                                                      decoration: BoxDecoration(
                                                        color: Colors.grey[300],
                                                        shape: BoxShape.circle,
                                                      ),
                                                    ),
                                                errorWidget: (context, url,
                                                        error) =>
                                                    SizedBox(
                                                      width: 40,
                                                      height: 40,
                                                      child: customCircleAvatar(
                                                          radius: 40),
                                                    )),
                                  ),
                                ),
                                title: Text(
                                  availableContacts
                                              .contactsBookContactList!.entries
                                              .toList()
                                              .indexWhere((element) =>
                                                  element.key ==
                                                  viewerslist[i]) >
                                          0
                                      ? availableContacts
                                          .contactsBookContactList!.entries
                                          .elementAt(availableContacts
                                              .contactsBookContactList!.entries
                                              .toList()
                                              .indexWhere((element) =>
                                                  element.key ==
                                                  viewerslist[i]))
                                          .value
                                          .toString()
                                      : snapshot.data![Dbkeys.nickname],
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style:
                                      TextStyle(fontWeight: FontWeight.normal),
                                ),
                                subtitle: Text(
                                  //-- or about me
                                  snapshot.data![Dbkeys.phone],
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: TextStyle(height: 1.4),
                                ),
                                onTap: widget.currentUserno ==
                                        snapshot.data![Dbkeys.phone]
                                    ? () {}
                                    : () {
                                        Navigator.push(
                                            context,
                                            new MaterialPageRoute(
                                                builder: (context) =>
                                                    new ProfileView(
                                                        snapshot.data!,
                                                        widget.currentUserno,
                                                        widget.model,
                                                        widget.prefs,
                                                        [],
                                                        firestoreUserDoc:
                                                            null)));
                                      },
                              ),
                            ],
                          ),
                        ],
                      );
                    }
                    return Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Divider(
                          height: 3,
                        ),
                        Stack(
                          children: [
                            ListTile(
                              isThreeLine: false,
                              contentPadding: EdgeInsets.fromLTRB(0, 0, 0, 0),
                              leading: Padding(
                                padding: const EdgeInsets.only(left: 5),
                                child: Padding(
                                  padding: const EdgeInsets.all(0.0),
                                  child: CachedNetworkImage(
                                      imageUrl: '',
                                      imageBuilder: (context, imageProvider) =>
                                          Container(
                                            width: 40.0,
                                            height: 40.0,
                                            decoration: BoxDecoration(
                                              shape: BoxShape.circle,
                                              image: DecorationImage(
                                                  image: imageProvider,
                                                  fit: BoxFit.cover),
                                            ),
                                          ),
                                      placeholder: (context, url) => Container(
                                            width: 40.0,
                                            height: 40.0,
                                            decoration: BoxDecoration(
                                              color: Colors.grey[300],
                                              shape: BoxShape.circle,
                                            ),
                                          ),
                                      errorWidget: (context, url, error) =>
                                          SizedBox(
                                            width: 40,
                                            height: 40,
                                            child:
                                                customCircleAvatar(radius: 40),
                                          )),
                                ),
                              ),
                              title: Text(
                                availableContacts
                                            .contactsBookContactList!.entries
                                            .toList()
                                            .indexWhere((element) =>
                                                element.key == viewerslist[i]) >
                                        0
                                    ? availableContacts
                                        .contactsBookContactList!.entries
                                        .elementAt(availableContacts
                                            .contactsBookContactList!.entries
                                            .toList()
                                            .indexWhere((element) =>
                                                element.key == viewerslist[i]))
                                        .value
                                        .toString()
                                    : viewerslist[i],
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: TextStyle(fontWeight: FontWeight.normal),
                              ),
                              subtitle: Text(
                                '',
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: TextStyle(height: 1.4),
                              ),
                            ),
                          ],
                        ),
                      ],
                    );
                  });
            });
      });
    });
  }
}

formatDate(DateTime timeToFormat) {
  final DateFormat formatter = DateFormat('dd/MM/yyyy');
  final String formatted = formatter.format(timeToFormat);
  return formatted;
}
