//*************   © Copyrighted by Thinkcreative_Technologies. An Exclusive item of Envato market. Make sure you have purchased a Regular License OR Extended license for the Source Code from Envato to use this product. See the License Defination attached with source code. *********************

import 'dart:io';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:fiberchat/Configs/Dbkeys.dart';
import 'package:fiberchat/Configs/Dbpaths.dart';
import 'package:fiberchat/Configs/Enum.dart';
import 'package:fiberchat/Configs/app_constants.dart';
import 'package:fiberchat/Configs/optional_constants.dart';
import 'package:fiberchat/Screens/call_history/callhistory.dart';
import 'package:fiberchat/Screens/calling_screen/pickup_layout.dart';
import 'package:fiberchat/Screens/notifications/AllNotifications.dart';
import 'package:fiberchat/Screens/privacypolicy&TnC/PdfViewFromCachedUrl.dart';
import 'package:fiberchat/Services/Providers/Observer.dart';
import 'package:fiberchat/Services/localization/language_constants.dart';
import 'package:fiberchat/Utils/custom_url_launcher.dart';
import 'package:fiberchat/Utils/utils.dart';
import 'package:fiberchat/widgets/MyElevatedButton/MyElevatedButton.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SettingsOption extends StatefulWidget {
  final bool biometricEnabled;
  final AuthenticationType type;
  final String currentUserNo;
  final Function onTapEditProfile;
  final SharedPreferences prefs;
  final Function onTapLogout;
  const SettingsOption(
      {Key? key,
      required this.biometricEnabled,
      required this.currentUserNo,
      required this.onTapEditProfile,
      required this.onTapLogout,
      required this.prefs,
      required this.type})
      : super(key: key);

  @override
  _SettingsOptionState createState() => _SettingsOptionState();
}

class _SettingsOptionState extends State<SettingsOption> {
  late Stream myDocStream;
  bool isLoading = false;
  @override
  void initState() {
    super.initState();
    myDocStream = FirebaseFirestore.instance
        .collection(DbPaths.collectionusers)
        .doc(widget.currentUserNo)
        .snapshots();
  }

  @override
  Widget build(BuildContext context) {
    var w = MediaQuery.of(context).size.width;
    final observer = Provider.of<Observer>(context, listen: false);
    return PickupLayout(
        prefs: widget.prefs,
        scaffold: Fiberchat.getNTPWrappedWidget(Scaffold(
          backgroundColor: Colors.white,
          appBar: AppBar(
            elevation: DESIGN_TYPE == Themetype.messenger ? 0.4 : 1,
            leading: IconButton(
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
            backgroundColor: DESIGN_TYPE == Themetype.whatsapp
                ? fiberchatDeepGreen
                : fiberchatWhite,
            title: Text(
              getTranslated(context, 'settingsoption'),
              style: TextStyle(
                  color: DESIGN_TYPE == Themetype.whatsapp
                      ? fiberchatWhite
                      : fiberchatBlack,
                  fontSize: 18.5),
            ),
          ),
          body: ListView(
            children: [
              Container(
                // padding: EdgeInsets.fromLTRB(0, 19, 0, 10),
                // height: 100,
                width: w,
                child: StreamBuilder(
                    stream: myDocStream,
                    builder: (context, AsyncSnapshot snapshot) {
                      if (snapshot.hasData && snapshot.data.exists) {
                        var myDoc = snapshot.data;
                        return Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Container(
                                padding: EdgeInsets.fromLTRB(0, 19, 0, 10),
                                child: ListTile(
                                    leading: customCircleAvatar(
                                        radius: 40,
                                        url: myDoc[Dbkeys.photoUrl]),
                                    title: Text(
                                      myDoc[Dbkeys.nickname] ??
                                          widget.currentUserNo,
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                      style: TextStyle(
                                          fontSize: 16, color: fiberchatBlack),
                                    ),
                                    subtitle: Padding(
                                      padding: const EdgeInsets.only(top: 7),
                                      child: Text(
                                        myDoc[Dbkeys.aboutMe] == null ||
                                                myDoc[Dbkeys.aboutMe] == ''
                                            ? myDoc[Dbkeys.phone]
                                            : myDoc[Dbkeys.aboutMe],
                                        maxLines: 1,
                                        overflow: TextOverflow.ellipsis,
                                        style: TextStyle(
                                            fontSize: 14,
                                            color: fiberchatBlack
                                                .withOpacity(0.56)),
                                      ),
                                    ),
                                    trailing: IconButton(
                                        onPressed: () {
                                          widget.onTapEditProfile();
                                        },
                                        icon: Icon(
                                          Icons.edit,
                                          color: fiberchatgreen,
                                        )))),
                            ListTile(
                              trailing: SizedBox(
                                width: 40,
                                child: isLoading == true
                                    ? Align(
                                        child: Padding(
                                        padding: const EdgeInsets.all(3.0),
                                        child: LinearProgressIndicator(
                                          backgroundColor: fiberchatLightGreen
                                              .withOpacity(0.4),
                                          valueColor:
                                              AlwaysStoppedAnimation<Color>(
                                                  fiberchatLightGreen),
                                        ),
                                      ))
                                    : Switch(
                                        activeColor: fiberchatgreen,
                                        inactiveThumbColor: Colors.blueGrey,
                                        inactiveTrackColor: Colors.grey[300],
                                        onChanged: (b) async {
                                          if (b == true) {
                                            setState(() {
                                              isLoading = true;
                                            });
                                            //subscribe to token
                                            await FirebaseMessaging.instance
                                                .subscribeToTopic(
                                                    '${widget.currentUserNo.replaceFirst(new RegExp(r'\+'), '')}')
                                                .catchError((err) {
                                              print(
                                                  'ERROR SUBSCRIBING NOTIFICATION' +
                                                      err.toString());
                                            });
                                            await FirebaseMessaging.instance
                                                .subscribeToTopic(
                                                    Dbkeys.topicUSERS)
                                                .catchError((err) {
                                              print(
                                                  'ERROR SUBSCRIBING NOTIFICATION' +
                                                      err.toString());
                                            });
                                            await FirebaseMessaging.instance
                                                .subscribeToTopic(Platform
                                                        .isAndroid
                                                    ? Dbkeys.topicUSERSandroid
                                                    : Platform.isIOS
                                                        ? Dbkeys.topicUSERSios
                                                        : Dbkeys.topicUSERSweb)
                                                .catchError((err) {
                                              print(
                                                  'ERROR SUBSCRIBING NOTIFICATION' +
                                                      err.toString());
                                            });
                                            String? fcmToken =
                                                await FirebaseMessaging.instance
                                                    .getToken();
                                            await FirebaseFirestore.instance
                                                .collection(
                                                    DbPaths.collectionusers)
                                                .doc(widget.currentUserNo)
                                                .update({
                                              Dbkeys.notificationTokens: [
                                                fcmToken
                                              ],
                                            });
                                            isLoading = false;
                                            setState(() {});
                                          } else {
                                            //unsubscribe to token
                                            setState(() {
                                              isLoading = true;
                                            });

                                            await FirebaseMessaging.instance
                                                .unsubscribeFromTopic(
                                                    '${widget.currentUserNo.replaceFirst(new RegExp(r'\+'), '')}')
                                                .catchError((err) {
                                              print(
                                                  'ERROR SUBSCRIBING NOTIFICATION' +
                                                      err.toString());
                                            });
                                            await FirebaseMessaging.instance
                                                .unsubscribeFromTopic(
                                                    Dbkeys.topicUSERS)
                                                .catchError((err) {
                                              print(
                                                  'ERROR SUBSCRIBING NOTIFICATION' +
                                                      err.toString());
                                            });
                                            await FirebaseMessaging.instance
                                                .unsubscribeFromTopic(Platform
                                                        .isAndroid
                                                    ? Dbkeys.topicUSERSandroid
                                                    : Platform.isIOS
                                                        ? Dbkeys.topicUSERSios
                                                        : Dbkeys.topicUSERSweb)
                                                .catchError((err) {
                                              print(
                                                  'ERROR SUBSCRIBING NOTIFICATION' +
                                                      err.toString());
                                            });

                                            await FirebaseFirestore.instance
                                                .collection(
                                                    DbPaths.collectionusers)
                                                .doc(widget.currentUserNo)
                                                .update({
                                              Dbkeys.notificationTokens: [],
                                            });
                                            isLoading = false;
                                            setState(() {});
                                          }
                                        },
                                        value: myDoc[Dbkeys.notificationTokens]
                                                    .length >
                                                0
                                            ? true
                                            : false,
                                      ),
                              ),
                              onTap: () {
                                widget.onTapEditProfile();
                              },
                              contentPadding: EdgeInsets.fromLTRB(30, 3, 25, 3),
                              leading: Padding(
                                padding: const EdgeInsets.only(top: 6),
                                child: Icon(
                                  Icons.notifications_on,
                                  color: fiberchatgreen.withOpacity(0.75),
                                  size: 26,
                                ),
                              ),
                              title: Text(
                                getTranslated(context, 'generalnotification'),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: TextStyle(
                                    fontSize: 16, color: fiberchatBlack),
                              ),
                              subtitle: Padding(
                                padding: const EdgeInsets.only(top: 4),
                                child: Text(
                                  getTranslated(
                                      context, 'generalnotificationdesc'),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: TextStyle(
                                      fontSize: 14,
                                      color: fiberchatBlack.withOpacity(0.56)),
                                ),
                              ),
                            ),
                          ],
                        );
                      }
                      return Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Container(
                              padding: EdgeInsets.fromLTRB(0, 19, 0, 10),
                              child: ListTile(
                                  leading: customCircleAvatar(radius: 40),
                                  title: Text(
                                    widget.currentUserNo,
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                    style: TextStyle(
                                        fontSize: 16, color: fiberchatBlack),
                                  ),
                                  subtitle: Padding(
                                    padding: const EdgeInsets.only(top: 7),
                                    child: Text(
                                      getTranslated(context, 'myprofile'),
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                      style: TextStyle(
                                          fontSize: 14,
                                          color:
                                              fiberchatBlack.withOpacity(0.56)),
                                    ),
                                  ),
                                  trailing: IconButton(
                                      onPressed: () {
                                        widget.onTapEditProfile();
                                      },
                                      icon: Icon(
                                        Icons.edit,
                                        color: fiberchatgreen,
                                      )))),
                          ListTile(
                            trailing: SizedBox(
                                width: 40,
                                child: Align(
                                    child: Padding(
                                  padding: const EdgeInsets.all(3.0),
                                  child: LinearProgressIndicator(
                                    backgroundColor:
                                        fiberchatLightGreen.withOpacity(0.4),
                                    valueColor: AlwaysStoppedAnimation<Color>(
                                        fiberchatLightGreen),
                                  ),
                                ))),
                            onTap: () {
                              widget.onTapEditProfile();
                            },
                            contentPadding: EdgeInsets.fromLTRB(30, 3, 25, 3),
                            leading: Padding(
                              padding: const EdgeInsets.only(top: 6),
                              child: Icon(
                                Icons.notifications_on,
                                color: fiberchatgreen.withOpacity(0.75),
                                size: 26,
                              ),
                            ),
                            title: Text(
                              getTranslated(context, 'generalnotification'),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style: TextStyle(
                                  fontSize: 16, color: fiberchatBlack),
                            ),
                            subtitle: Padding(
                              padding: const EdgeInsets.only(top: 4),
                              child: Text(
                                getTranslated(
                                    context, 'generalnotificationdesc'),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: TextStyle(
                                    fontSize: 14,
                                    color: fiberchatBlack.withOpacity(0.56)),
                              ),
                            ),
                          ),
                        ],
                      );
                    }),
              ),
              // Divider(),

              // Divider(),
              ListTile(
                onTap: () {
                  widget.onTapEditProfile();
                },
                contentPadding: EdgeInsets.fromLTRB(30, 3, 10, 3),
                leading: Padding(
                  padding: const EdgeInsets.only(top: 6),
                  child: Icon(
                    Icons.account_circle_rounded,
                    color: fiberchatgreen.withOpacity(0.75),
                    size: 26,
                  ),
                ),
                title: Text(
                  getTranslated(context, 'editprofile'),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(fontSize: 16, color: fiberchatBlack),
                ),
                subtitle: Padding(
                  padding: const EdgeInsets.only(top: 4),
                  child: Text(
                    getTranslated(context, 'changednp'),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                        fontSize: 14, color: fiberchatBlack.withOpacity(0.56)),
                  ),
                ),
              ),
              ListTile(
                onTap: () {
                  custom_url_launcher('mailto:${observer.feedbackEmail}');
                },
                contentPadding: EdgeInsets.fromLTRB(30, 3, 10, 3),
                leading: Padding(
                  padding: const EdgeInsets.only(top: 6),
                  child: Icon(
                    Icons.rate_review_outlined,
                    color: fiberchatgreen.withOpacity(0.75),
                    size: 26,
                  ),
                ),
                title: Text(
                  getTranslated(context, 'feedback'),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(fontSize: 16, color: fiberchatBlack),
                ),
                subtitle: Padding(
                  padding: const EdgeInsets.only(top: 4),
                  child: Text(
                    getTranslated(context, 'givesuggestions'),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                        fontSize: 14, color: fiberchatBlack.withOpacity(0.56)),
                  ),
                ),
              ),
              ListTile(
                onTap: () {
                  onTapRateApp();
                },
                contentPadding: EdgeInsets.fromLTRB(30, 3, 10, 3),
                leading: Padding(
                  padding: const EdgeInsets.only(top: 3),
                  child: Icon(
                    Icons.star_outline_rounded,
                    color: fiberchatgreen.withOpacity(0.75),
                    size: 29,
                  ),
                ),
                title: Text(
                  getTranslated(context, 'rate'),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(fontSize: 16, color: fiberchatBlack),
                ),
                subtitle: Padding(
                  padding: const EdgeInsets.only(top: 4),
                  child: Text(
                    getTranslated(context, 'leavereview'),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                        fontSize: 14, color: fiberchatBlack.withOpacity(0.56)),
                  ),
                ),
              ),
              ListTile(
                onTap: () {
                  showModalBottomSheet(
                      isScrollControlled: true,
                      context: context,
                      shape: RoundedRectangleBorder(
                        borderRadius:
                            BorderRadius.vertical(top: Radius.circular(25.0)),
                      ),
                      builder: (BuildContext context) {
                        return Container(
                          height: 220,
                          child: Padding(
                            padding: const EdgeInsets.all(28.0),
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Icon(Icons.check_circle_outline_rounded,
                                    color: Colors.green[400], size: 45),
                                SizedBox(
                                  height: 20,
                                ),
                                Text(
                                  getTranslated(context, 'backupdesc'),
                                  textAlign: TextAlign.center,
                                  style: TextStyle(
                                      height: 1.3, color: fiberchatGrey),
                                )
                              ],
                            ),
                          ),
                        );
                      });
                },
                contentPadding: EdgeInsets.fromLTRB(30, 3, 10, 3),
                leading: Padding(
                  padding: const EdgeInsets.only(top: 3),
                  child: Icon(
                    Icons.cloud_upload_outlined,
                    color: fiberchatgreen.withOpacity(0.75),
                    size: 25,
                  ),
                ),
                title: Text(
                  getTranslated(context, 'backup'),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(fontSize: 16, color: fiberchatBlack),
                ),
                subtitle: Padding(
                  padding: const EdgeInsets.only(top: 4),
                  child: Text(
                    getTranslated(context, 'backupshort'),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                        fontSize: 14, color: fiberchatBlack.withOpacity(0.56)),
                  ),
                ),
              ),
              ListTile(
                onTap: () {
                  Navigator.push(
                      context,
                      new MaterialPageRoute(
                          builder: (context) => AllNotifications(
                                prefs: widget.prefs,
                              )));
                },
                contentPadding: EdgeInsets.fromLTRB(30, 3, 10, 3),
                leading: Padding(
                  padding: const EdgeInsets.only(top: 3),
                  child: Icon(
                    Icons.notifications_none,
                    color: fiberchatgreen.withOpacity(0.75),
                    size: 29,
                  ),
                ),
                title: Text(
                  getTranslated(context, 'pmtevents'),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(fontSize: 16, color: fiberchatBlack),
                ),
                subtitle: Padding(
                  padding: const EdgeInsets.only(top: 4),
                  child: Text(
                    getTranslated(context, 'allnotifications'),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                        fontSize: 14, color: fiberchatBlack.withOpacity(0.56)),
                  ),
                ),
              ),
              ListTile(
                onTap: () {
                  if (ConnectWithAdminApp == false) {
                    custom_url_launcher(TERMS_CONDITION_URL);
                  } else {
                    final observer =
                        Provider.of<Observer>(context, listen: false);
                    if (observer.tncType == 'url') {
                      if (observer.tnc == null) {
                        custom_url_launcher(TERMS_CONDITION_URL);
                      } else {
                        custom_url_launcher(observer.tnc!);
                      }
                    } else if (observer.tncType == 'file') {
                      Navigator.push(
                        context,
                        MaterialPageRoute<dynamic>(
                          builder: (_) => PDFViewerCachedFromUrl(
                            prefs: widget.prefs,
                            title: getTranslated(context, 'tnc'),
                            url: observer.tnc,
                            isregistered: true,
                          ),
                        ),
                      );
                    }
                  }
                },
                contentPadding: EdgeInsets.fromLTRB(30, 3, 10, 3),
                leading: Padding(
                  padding: const EdgeInsets.only(top: 6),
                  child: Icon(
                    Icons.help_outline,
                    color: fiberchatgreen.withOpacity(0.75),
                    size: 26,
                  ),
                ),
                title: Text(
                  getTranslated(context, 'tnc'),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(fontSize: 16, color: fiberchatBlack),
                ),
                subtitle: Padding(
                  padding: const EdgeInsets.only(top: 4),
                  child: Text(
                    getTranslated(context, 'abiderules'),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                        fontSize: 14, color: fiberchatBlack.withOpacity(0.56)),
                  ),
                ),
              ),
              ListTile(
                onTap: () {
                  final observer =
                      Provider.of<Observer>(context, listen: false);
                  if (ConnectWithAdminApp == false) {
                    custom_url_launcher(PRIVACY_POLICY_URL);
                  } else {
                    if (observer.privacypolicyType == 'url') {
                      if (observer.privacypolicy == null) {
                        custom_url_launcher(PRIVACY_POLICY_URL);
                      } else {
                        custom_url_launcher(observer.privacypolicy!);
                      }
                    } else if (observer.privacypolicyType == 'file') {
                      Navigator.push(
                        context,
                        MaterialPageRoute<dynamic>(
                          builder: (_) => PDFViewerCachedFromUrl(
                            prefs: widget.prefs,
                            title: getTranslated(context, 'pp'),
                            url: observer.privacypolicy,
                            isregistered: true,
                          ),
                        ),
                      );
                    }
                  }
                },
                contentPadding: EdgeInsets.fromLTRB(30, 3, 10, 3),
                leading: Padding(
                  padding: const EdgeInsets.only(top: 6),
                  child: Icon(
                    Icons.lock_outline_rounded,
                    color: fiberchatgreen.withOpacity(0.75),
                    size: 26,
                  ),
                ),
                title: Text(
                  getTranslated(context, 'pp'),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(fontSize: 16, color: fiberchatBlack),
                ),
                subtitle: Padding(
                  padding: const EdgeInsets.only(top: 4),
                  child: Text(
                    getTranslated(context, 'processdata'),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                        fontSize: 14, color: fiberchatBlack.withOpacity(0.56)),
                  ),
                ),
              ),
              ListTile(
                onTap: () {
                  showModalBottomSheet(
                      isScrollControlled: true,
                      context: context,
                      shape: RoundedRectangleBorder(
                        borderRadius:
                            BorderRadius.vertical(top: Radius.circular(25.0)),
                      ),
                      builder: (BuildContext context) {
                        // return your layout
                        var w = MediaQuery.of(context).size.width;
                        return Padding(
                          padding: EdgeInsets.only(
                              bottom: MediaQuery.of(context).viewInsets.bottom),
                          child: Container(
                              padding: EdgeInsets.all(16),
                              height: MediaQuery.of(context).size.height / 2.6,
                              child: Column(
                                  mainAxisSize: MainAxisSize.min,
                                  crossAxisAlignment:
                                      CrossAxisAlignment.stretch,
                                  children: [
                                    SizedBox(
                                      height: 12,
                                    ),
                                    SizedBox(
                                      height: 3,
                                    ),
                                    Padding(
                                      padding: const EdgeInsets.only(
                                          left: 12, right: 12),
                                      child: Text(
                                        getTranslated(
                                            this.context, 'raiserequestdesc'),
                                        textAlign: TextAlign.center,
                                        style: TextStyle(
                                            fontWeight: FontWeight.normal,
                                            fontSize: 16.5),
                                      ),
                                    ),
                                    SizedBox(
                                      height: 10,
                                    ),
                                    SizedBox(
                                      height: w / 10,
                                    ),
                                    myElevatedButton(
                                        color: fiberchatLightGreen,
                                        child: Padding(
                                          padding: const EdgeInsets.fromLTRB(
                                              10, 15, 10, 15),
                                          child: Text(
                                            getTranslated(context, 'submit'),
                                            style: TextStyle(
                                                color: Colors.white,
                                                fontSize: 18),
                                          ),
                                        ),
                                        onPressed: () async {
                                          Navigator.of(context).pop();

                                          DateTime time = DateTime.now();

                                          Map<String, dynamic> mapdata = {
                                            'title':
                                                'Account Deletion Request by User',
                                            'desc':
                                                'User ${widget.currentUserNo} has requested to Delete account. Delete it from User Profile page. User Personal Chat, Profile Details will be deleted.',
                                            'phone': '${widget.currentUserNo}',
                                            'type': 'Account',
                                            'time': time.millisecondsSinceEpoch,
                                            'id': widget.currentUserNo
                                          };

                                          await FirebaseFirestore.instance
                                              .collection('reports')
                                              .doc(time.millisecondsSinceEpoch
                                                  .toString())
                                              .set(mapdata)
                                              .then((value) async {
                                            showModalBottomSheet(
                                                isScrollControlled: true,
                                                context: context,
                                                shape: RoundedRectangleBorder(
                                                  borderRadius:
                                                      BorderRadius.vertical(
                                                          top: Radius.circular(
                                                              25.0)),
                                                ),
                                                builder:
                                                    (BuildContext context) {
                                                  return Container(
                                                    height: 220,
                                                    child: Padding(
                                                      padding:
                                                          const EdgeInsets.all(
                                                              28.0),
                                                      child: Column(
                                                        mainAxisAlignment:
                                                            MainAxisAlignment
                                                                .center,
                                                        children: [
                                                          Icon(Icons.check,
                                                              color: Colors
                                                                  .green[400],
                                                              size: 40),
                                                          SizedBox(
                                                            height: 30,
                                                          ),
                                                          Text(
                                                            getTranslated(
                                                                context,
                                                                'requestsubmitted'),
                                                            textAlign: TextAlign
                                                                .center,
                                                          )
                                                        ],
                                                      ),
                                                    ),
                                                  );
                                                });

                                            //----
                                          }).catchError((err) {
                                            showModalBottomSheet(
                                                isScrollControlled: true,
                                                context: this.context,
                                                shape: RoundedRectangleBorder(
                                                  borderRadius:
                                                      BorderRadius.vertical(
                                                          top: Radius.circular(
                                                              25.0)),
                                                ),
                                                builder:
                                                    (BuildContext context) {
                                                  return Container(
                                                    height: 220,
                                                    child: Padding(
                                                      padding:
                                                          const EdgeInsets.all(
                                                              28.0),
                                                      child: Column(
                                                        mainAxisAlignment:
                                                            MainAxisAlignment
                                                                .center,
                                                        children: [
                                                          Icon(Icons.check,
                                                              color: Colors
                                                                  .green[400],
                                                              size: 40),
                                                          SizedBox(
                                                            height: 30,
                                                          ),
                                                          Text(
                                                            getTranslated(
                                                                context,
                                                                'requestsubmitted'),
                                                            textAlign: TextAlign
                                                                .center,
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
                },
                contentPadding: EdgeInsets.fromLTRB(30, 3, 10, 3),
                leading: Padding(
                  padding: const EdgeInsets.only(top: 6),
                  child: Icon(
                    Icons.delete_outlined,
                    color: fiberchatgreen.withOpacity(0.75),
                    size: 26,
                  ),
                ),
                title: Text(
                  getTranslated(context, 'deleteaccount'),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(fontSize: 16, color: fiberchatBlack),
                ),
                subtitle: Padding(
                  padding: const EdgeInsets.only(top: 4),
                  child: Text(
                    getTranslated(context, 'raiserequest'),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                        fontSize: 14, color: fiberchatBlack.withOpacity(0.56)),
                  ),
                ),
              ),
              ListTile(
                onTap: () {
                  Fiberchat.invite(context);
                },
                contentPadding: EdgeInsets.fromLTRB(30, 3, 10, 3),
                leading: Icon(
                  Icons.people_rounded,
                  color: fiberchatgreen.withOpacity(0.75),
                  size: 26,
                ),
                title: Text(
                  getTranslated(context, 'share'),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(fontSize: 16, color: fiberchatBlack),
                ),
              ),
              observer.isLogoutButtonShowInSettingsPage == true
                  ? Divider()
                  : SizedBox(),
              observer.isLogoutButtonShowInSettingsPage == true
                  ? ListTile(
                      onTap: () async {
                        widget.onTapLogout();
                      },
                      contentPadding: EdgeInsets.fromLTRB(30, 0, 10, 6),
                      leading: Icon(
                        Icons.logout_rounded,
                        color: Colors.red,
                        size: 26,
                      ),
                      title: Text(
                        getTranslated(context, 'logout'),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                            fontSize: 16,
                            color: fiberchatBlack,
                            fontWeight: FontWeight.w600),
                      ),
                    )
                  : SizedBox(),
              SizedBox(
                height: 10,
              ),
            ],
          ),
        )));
  }

  onTapRateApp() {
    final observer = Provider.of<Observer>(context, listen: false);
    showDialog(
        context: context,
        builder: (context) {
          return SimpleDialog(
            children: <Widget>[
              ListTile(
                  contentPadding: EdgeInsets.only(top: 20),
                  subtitle: Padding(padding: EdgeInsets.only(top: 10.0)),
                  title: Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          Icons.star,
                          size: 40,
                          color: fiberchatBlack.withOpacity(0.56),
                        ),
                        Icon(
                          Icons.star,
                          size: 40,
                          color: fiberchatBlack.withOpacity(0.56),
                        ),
                        Icon(
                          Icons.star,
                          size: 40,
                          color: fiberchatBlack.withOpacity(0.56),
                        ),
                        Icon(
                          Icons.star,
                          size: 40,
                          color: fiberchatBlack.withOpacity(0.56),
                        ),
                        Icon(
                          Icons.star,
                          size: 40,
                          color: fiberchatBlack.withOpacity(0.56),
                        ),
                      ]),
                  onTap: () {
                    Navigator.of(context).pop();
                    Platform.isAndroid
                        ? custom_url_launcher(ConnectWithAdminApp == true
                            ? observer.userAppSettingsDoc!
                                .data()![Dbkeys.newapplinkandroid]
                            : RateAppUrlAndroid)
                        : custom_url_launcher(ConnectWithAdminApp == true
                            ? observer.userAppSettingsDoc!
                                .data()![Dbkeys.newapplinkios]
                            : RateAppUrlIOS);
                  }),
              Divider(),
              Padding(
                  child: Text(
                    getTranslated(context, 'loved'),
                    style: TextStyle(fontSize: 14, color: fiberchatBlack),
                    textAlign: TextAlign.center,
                  ),
                  padding: EdgeInsets.symmetric(horizontal: 20, vertical: 10)),
              Center(
                  child: myElevatedButton(
                      color: fiberchatgreen,
                      child: Text(
                        getTranslated(context, 'rate'),
                        style: TextStyle(color: Colors.white),
                      ),
                      onPressed: () {
                        Navigator.of(context).pop();
                        Platform.isAndroid
                            ? custom_url_launcher(ConnectWithAdminApp == true
                                ? observer.userAppSettingsDoc!
                                    .data()![Dbkeys.newapplinkandroid]
                                : RateAppUrlAndroid)
                            : custom_url_launcher(ConnectWithAdminApp == true
                                ? observer.userAppSettingsDoc!
                                    .data()![Dbkeys.newapplinkios]
                                : RateAppUrlIOS);
                      }))
            ],
          );
        });
  }
}
