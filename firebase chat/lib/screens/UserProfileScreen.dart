import 'package:chat/components/FullScreenImageWidget.dart';
import 'package:chat/components/Permissions.dart';
import 'package:chat/models/UserModel.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppCommon.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:chat/utils/CallFunctions.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:nb_utils/nb_utils.dart';

class UserProfileScreen extends StatefulWidget {
  final UserModel? user;
  final bool isFromDashboard;

  UserProfileScreen({this.user, this.isFromDashboard = false});

  @override
  _UserProfileScreenState createState() => _UserProfileScreenState();
}

class _UserProfileScreenState extends State<UserProfileScreen> {
  BannerAd? myBanner;

  @override
  void initState() {
    super.initState();
    myBanner = buildBannerAd()..load();
  }

  BannerAd buildBannerAd() {
    return BannerAd(
      adUnitId: kReleaseMode ? mAdMobBannerId : BannerAd.testAdUnitId,
      size: AdSize.fullBanner,
      listener: BannerAdListener(onAdLoaded: (ad) {
        //
      }),
      request: AdRequest(),
    );
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        backgroundColor: Colors.grey[300],
        body: Stack(
          children: [
            _buildImageAppBar(),
            if (myBanner != null)
              Positioned(
                child: AdWidget(ad: myBanner!),
                bottom: 0,
                height: AdSize.banner.height.toDouble(),
                width: context.width(),
              ),
          ],
        ),
      ),
    );
  }

  _buildImageAppBar() {
    return NestedScrollView(
      physics: BouncingScrollPhysics(),
      scrollDirection: Axis.vertical,
      floatHeaderSlivers: false,
      headerSliverBuilder: (context, isScrollExtended) {
        return [
          SliverAppBar(
            backgroundColor: primaryColor,
            leading: IconButton(
              icon: Icon(Icons.arrow_back, color: Colors.white),
              onPressed: () {
                finish(context);
              },
            ),
            expandedHeight: 300.0,
            pinned: true,
            stretch: true,
            stretchTriggerOffset: 120.0,
            flexibleSpace: FlexibleSpaceBar(
              titlePadding: EdgeInsetsDirectional.only(
                start: 50.0,
                bottom: 16.0,
              ),
              stretchModes: [StretchMode.zoomBackground],
              collapseMode: CollapseMode.parallax,
              title: Text("${widget.user!.name}", style: TextStyle(fontSize: 16.0, color: Colors.white, fontWeight: FontWeight.bold)),
              background: Image.network("${widget.user!.photoUrl}", fit: BoxFit.cover).onTap(() {
                FullScreenImageWidget(photoUrl: widget.user!.photoUrl, name: widget.user!.name).launch(context);
              }),
            ),
          ),
        ];
      },
      body: Container(
        color: context.scaffoldBackgroundColor,
        child: SingleChildScrollView(
          clipBehavior: Clip.antiAliasWithSaveLayer,
          child: Column(
            children: [
              _itemAboutPhone(),
              _buildBlockMSG(),
              _buildReport(),
            ],
          ),
        ),
      ),
    );
  }

  _itemAboutPhone() {
    return Container(
      color: context.scaffoldBackgroundColor,
      margin: EdgeInsets.only(top: 8.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(padding: EdgeInsets.only(left: 16.0, top: 8.0), child: Text("About and Phone number", style: TextStyle(fontSize: 16, color: secondaryColor))),
          ListTile(
            title: Text(widget.user!.userStatus!, style: primaryTextStyle()),
            subtitle: Text(widget.user!.updatedAt!.toDate().timeAgo, style: secondaryTextStyle()),
          ),
          Divider(thickness: 1),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              ListTile(
                title: Text(
                  "${widget.user!.phoneNumber}",
                  style: primaryTextStyle(),
                ),
                subtitle: Text("Mobile", style: secondaryTextStyle()),
              ).expand(),
              IconButton(
                  icon: Icon(Icons.message, color: secondaryColor),
                  onPressed: () {
                    finish(context);
                  }),
              IconButton(
                  icon: Icon(Icons.call, color: secondaryColor),
                  onPressed: () {
                    toast(COMING_SOON);
                  }),
              IconButton(
                  icon: Icon(Icons.videocam, color: secondaryColor),
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
                  }),
            ],
          )
        ],
      ),
    );
  }

  _buildBlockMSG() {
    return Container(
      margin: EdgeInsets.only(top: 8.0),
      padding: EdgeInsets.all(16.0),
      color: context.scaffoldBackgroundColor,
      child: InkWell(
        onTap: () {
          _buildBlockDilaog();
        },
        child: Row(children: [
          Icon(Icons.block, color: Colors.red[900]),
          16.width,
          Text("block".translate, style: TextStyle(fontSize: 16, color: Colors.red[900])),
        ]),
      ),
    );
  }

  _buildReport() {
    return Container(
      margin: EdgeInsets.only(top: 8.0),
      padding: EdgeInsets.all(16.0),
      color: context.scaffoldBackgroundColor,
      child: InkWell(
        onTap: () {
          _buildReportDilaog();
        },
        child: Row(children: [Icon(Icons.thumb_down, color: Colors.red[900]), 16.width, Text("report_contact".translate, style: TextStyle(fontSize: 16, color: Colors.red[900]))]),
      ),
    );
  }

  Future _buildBlockDilaog() async {
    return await showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            content: Text(
              "Block" + " ${widget.user!.name.validate()} " + " blocked_contact_will_no_longer_be_able_to_call_you_or_send_you_message".translate,
              style: boldTextStyle(),
            ),
            actions: [
              // ignore: deprecated_member_use
              FlatButton(
                  onPressed: () {
                    finish(context);
                  },
                  child: Text("cancel".translate, style: TextStyle(color: secondaryColor))),
              // ignore: deprecated_member_use
              FlatButton(
                  onPressed: () {
                    toast(COMING_SOON);
                  },
                  child: Text("block".translate, style: TextStyle(color: secondaryColor))),
            ],
          );
        });
  }

  Future _buildReportDilaog() async {
    return await showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: Text("report_this_contact_to".translate + "$AppName", style: primaryTextStyle(size: 14)),
            content: ListTile(
              contentPadding: EdgeInsets.zero,
              leading: Checkbox(value: true, onChanged: (val) {}, activeColor: secondaryColor),
              title: Text("block_contact_and_delete_this_chat_message".translate, style: primaryTextStyle(size: 14)),
            ),
            actions: [
              // ignore: deprecated_member_use
              FlatButton(
                  child: Text("cancel".translate, style: TextStyle(color: secondaryColor)),
                  onPressed: () {
                    finish(context);
                  }),
              // ignore: deprecated_member_use
              FlatButton(
                  child: Text("report".translate, style: TextStyle(color: secondaryColor)),
                  onPressed: () {
                    toast(COMING_SOON);
                  })
            ],
          );
        });
  }
}
