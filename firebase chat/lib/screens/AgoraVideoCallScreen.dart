import 'dart:async';

import 'package:agora_rtc_engine/rtc_engine.dart';
import 'package:agora_rtc_engine/rtc_local_view.dart' as RtcLocalView;
import 'package:agora_rtc_engine/rtc_remote_view.dart' as RtcRemoteView;
import 'package:chat/main.dart';
import 'package:chat/models/CallModel.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:chat/utils/Appwidgets.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:nb_utils/nb_utils.dart';

class AgoraVideoCallScreen extends StatefulWidget {
  final CallModel? callModel;

  AgoraVideoCallScreen({this.callModel});

  @override
  _AgoraVideoCallScreenState createState() => _AgoraVideoCallScreenState();
}

class _AgoraVideoCallScreenState extends State<AgoraVideoCallScreen> {
  List<int> _users = [];
  bool muted = false;
  late RtcEngine _engine;
  bool switchRender = true;
  bool isUserJoined = false;

  Offset offset = Offset(120, 16);

  String callStatus = "Ringing";

  @override
  void initState() {
    super.initState();
    init();
  }

  late StreamSubscription callStreamSubcription;

  init() async {
    addPostFrameCallBack();
    initialize();
  }

  Future<void> initialize() async {
    await _initAgoraRtcEngine();
    // ignore: deprecated_member_use

    await _engine.joinChannel(null, widget.callModel!.channelId!, null, 0,);
  }

  Future<void> _initAgoraRtcEngine() async {
    _engine = await RtcEngine.createWithContext(RtcEngineContext(agoraVideoCallId));

    this._addAgoraEventHandlers();
    await _engine.enableVideo();
    await _engine.startPreview();
    await _engine.setChannelProfile(ChannelProfile.LiveBroadcasting);
    await _engine.setClientRole(ClientRole.Broadcaster);
  }

  void _addAgoraEventHandlers() {
    _engine.setEventHandler(
      RtcEngineEventHandler(
        error: (code) {
          setState(() {});
        },
        joinChannelSuccess: (channel, uid, elapsed) {
          setState(() {
            log(uid);
            log(_users);
          });
        },
        leaveChannel: (stats) {
          setState(() {
            _users.clear();
          });
        },
        userJoined: (uid, elapsed) {
          setState(() {
            toast("connected");
            isUserJoined = true;
            _users.add(uid);
          });
        },
        userOffline: (uid, elapsed) {
          callService.endCall(callModel: widget.callModel!);
          setState(() {
            _users.remove(uid);
          });
        },
      ),
    );
  }

  void addPostFrameCallBack() {
    SchedulerBinding.instance!.addPostFrameCallback((_) {
      callStreamSubcription = callService.callStream(uid: getStringAsync(userId)).listen((DocumentSnapshot ds) {
        switch (ds.data()) {
          case null:
            finish(context);
            break;

          default:
            break;
        }
      });
      //
    });
  }

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  void dispose() {
    _users.clear();
    _engine.leaveChannel();
    _engine.destroy();
    callStreamSubcription.cancel();
    super.dispose();
  }

  Widget _toolbar() {
    if (ClientRole.Broadcaster == ClientRole.Audience) return Container();
    return Container(
      alignment: Alignment.bottomCenter,
      padding: const EdgeInsets.symmetric(vertical: 48),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          RawMaterialButton(
            onPressed: _onToggleMute,
            child: Icon(
              muted ? Icons.mic_off : Icons.mic,
              color: muted ? Colors.white : Colors.blueAccent,
              size: 20.0,
            ),
            shape: CircleBorder(),
            elevation: 2.0,
            fillColor: muted ? Colors.blueAccent : Colors.white,
            padding: const EdgeInsets.all(12.0),
          ),
          RawMaterialButton(
            onPressed: () => callService.endCall(callModel: widget.callModel!),
            child: Icon(
              Icons.call_end,
              color: Colors.white,
              size: 35.0,
            ),
            shape: CircleBorder(),
            elevation: 2.0,
            fillColor: Colors.redAccent,
            padding: const EdgeInsets.all(15.0),
          ),
          RawMaterialButton(
            onPressed: _onSwitchCamera,
            child: Icon(
              Icons.switch_camera,
              color: Colors.blueAccent,
              size: 20.0,
            ),
            shape: CircleBorder(),
            elevation: 2.0,
            fillColor: Colors.white,
            padding: const EdgeInsets.all(12.0),
          )
        ],
      ),
    );
  }

  void _onToggleMute() {
    setState(() {
      muted = !muted;
    });
    _engine.muteLocalAudioStream(muted);
  }

  void _onSwitchCamera() {
    _engine.switchCamera();
  }

  _switchRender() {
    log("After $_users");

    setState(() {
      switchRender = !switchRender;
      _users = List.of(_users.reversed);
    });
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
          body: Center(
        child: Stack(
          children: <Widget>[
            RtcLocalView.SurfaceView().visible(!isUserJoined),
            Positioned(
              top: 80,
              left: 0,
              right: 0,
              child: Column(
                children: [
                  cachedImage(widget.callModel!.receiverPhotoUrl.validate(), height: 120, width: 120, fit: BoxFit.fill).cornerRadiusWithClipRRect(80),
                  16.height,
                  Text(widget.callModel!.receiverName.validate(), style: boldTextStyle(size: 18, color: Colors.white)),
                  16.height,
                  Text(callStatus, style: boldTextStyle(color: Colors.white)).center(),
                ],
              ),
            ).visible(!isUserJoined),
            Stack(
              children: [
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  child: Row(
                    children: List.of(_users.map(
                      (e) => GestureDetector(
                        onTap: this._switchRender,
                        child: Container(
                          width: context.width(),
                          height: context.height(),
                          child: RtcRemoteView.SurfaceView(
                            uid: e,
                          ),
                        ),
                      ),
                    )),
                  ).visible(isUserJoined),
                ),
                Positioned(
                  bottom: offset.dx,
                  right: offset.dy,
                  child: GestureDetector(
                    onPanUpdate: (details) {
                      setState(() {});
                    },
                    child: Container(
                      height: 180,
                      width: 150,
                      child: RtcLocalView.SurfaceView(),
                    ),
                  ),
                ).visible(isUserJoined)
              ],
            ),
            _toolbar(),
          ],
        ),
      )),
    );
  }
}
