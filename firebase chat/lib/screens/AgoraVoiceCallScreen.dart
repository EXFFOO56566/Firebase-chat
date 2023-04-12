import 'dart:async';

import 'package:agora_rtc_engine/rtc_engine.dart';
import 'package:chat/main.dart';
import 'package:chat/models/CallModel.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:chat/utils/Appwidgets.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter_vector_icons/flutter_vector_icons.dart';
import 'package:nb_utils/nb_utils.dart';

// ignore: must_be_immutable
class AgoraVoiceCallScreen extends StatefulWidget {
  late final RtcEngine _engine;
  final CallModel? callModel;

  AgoraVoiceCallScreen({this.callModel});

  @override
  State<StatefulWidget> createState() => _State();
}

class _State extends State<AgoraVoiceCallScreen> {
  bool isJoined = false;
  bool openMicrophone = false;
  bool enableSpeakerphone = false;

  late StreamSubscription callStreamSubcription;
  final _infoStrings = <String>[];
  final _users = <int>[];

  @override
  void initState() {
    super.initState();
    init();
  }

  init() async {
    addPostFrameCallBack();
    initialize();
  }

  @override
  void dispose() {
    super.dispose();
    callStreamSubcription.cancel();
    widget._engine.destroy();
  }

  Future<void> initialize() async {
    if (agoraVideoCallId.isEmpty) {
      setState(() {
        _infoStrings.add(
          'videoAppId missing, please provide your videoAppId in settings.dart',
        );
        _infoStrings.add('Agora Engine is not starting');
      });
      return;
    }

    await _initAgoraRtcEngine();
    await widget._engine.joinChannel(null, widget.callModel!.channelId!, null, 0);
  }

  Future<void> _initAgoraRtcEngine() async {
    widget._engine = await RtcEngine.createWithContext(RtcEngineContext(agoraVideoCallId));
    _addListeners();

    await widget._engine.enableAudio();
    await widget._engine.setChannelProfile(ChannelProfile.LiveBroadcasting);
    await widget._engine.setClientRole(ClientRole.Broadcaster);
  }

  void _addListeners() {
    widget._engine.setEventHandler(
      RtcEngineEventHandler(
        joinChannelSuccess: (channel, uid, elapsed) {
          setState(() {
            final info = 'onJoinChannel: $channel, uid: $uid';
            _infoStrings.add(info);
          });
        },
        leaveChannel: (stats) async {
          setState(() {
            _infoStrings.add('onLeaveChannel');
            _users.clear();
          });
        },
        userJoined: (uid, elapsed) {
          setState(() {
            final info = 'userJoined: $uid';
            _infoStrings.add(info);
            _users.add(uid);
          });
        },
        userOffline: (uid, elapsed) {
          callService.endCall(callModel: widget.callModel!);
          setState(() {
            final info = 'userOffline: $uid';
            _infoStrings.add(info);
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

  _switchMicrophone() {
    widget._engine.enableLocalAudio(openMicrophone).then((value) {
      setState(() {
        openMicrophone = !openMicrophone;
      });
    }).catchError((err) {
      log('enableLocalAudio $err');
    });
  }

  _switchSpeakerphone() {
    widget._engine.setEnableSpeakerphone(enableSpeakerphone).then((value) {
      log(enableSpeakerphone);
      setState(() {
        enableSpeakerphone = !enableSpeakerphone;
      });
    }).catchError((err) {
      log('setEnableSpeakerphone $err');
    });
  }

  @override
  Widget build(BuildContext context) {
    Widget getCallScreen({bool? value}) {
      if (value!) {
        return Container(
          child: Stack(
            children: [
              Positioned(
                top: 0,
                left: 0,
                right: 0,
                child: Container(
                  height: context.height() * 0.15,
                  width: context.width(),
                  color: context.primaryColor,
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(widget.callModel!.receiverName!, style: primaryTextStyle(color: Colors.white, size: 22)),
                      16.height,
                      Text('Calling...', style: secondaryTextStyle(color: Colors.white70, size: 16)),
                    ],
                  ),
                ),
              ),
              Container(
                margin: EdgeInsets.only(top: context.height() * 0.15),
                width: context.width(),
                height: context.height(),
                child: cachedImage(widget.callModel!.receiverPhotoUrl, fit: BoxFit.fill),
              ),
              Positioned(
                bottom: 0,
                left: 0,
                right: 0,
                child: Container(
                  height: context.height() * 0.15,
                  width: context.width(),
                  color: context.primaryColor,
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      RawMaterialButton(
                        onPressed: _switchSpeakerphone,
                        child: Icon(
                          Octicons.unmute,
                          color: Colors.white,
                          size: 24.0,
                        ),
                        shape: CircleBorder(),
                        elevation: 0,
                        fillColor: enableSpeakerphone ? Colors.white38 : context.primaryColor,
                        padding: const EdgeInsets.all(16.0),
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
                        onPressed: _switchMicrophone,
                        child: Icon(
                          MaterialIcons.mic_off,
                          color: Colors.white,
                          size: 24.0,
                        ),
                        shape: CircleBorder(),
                        elevation: 0.0,
                        fillColor: openMicrophone ? Colors.white38 : context.primaryColor,
                        padding: const EdgeInsets.all(16.0),
                      )
                    ],
                  ),
                ),
              ),
            ],
          ),
        );
      } else {
        return Container(
          child: Stack(
            children: [
              Positioned(
                top: 0,
                left: 0,
                right: 0,
                child: Container(
                  height: context.height() * 0.15,
                  width: context.width(),
                  color: context.primaryColor,
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(widget.callModel!.callerName!, style: primaryTextStyle(color: Colors.white, size: 22)),
                      16.height,
                      Text('Connected', style: secondaryTextStyle(color: Colors.white70, size: 16)),
                    ],
                  ),
                ),
              ),
              Container(
                margin: EdgeInsets.only(top: context.height() * 0.15),
                width: context.width(),
                height: context.height(),
                child: cachedImage(widget.callModel!.callerPhotoUrl, fit: BoxFit.fill),
              ),
              Positioned(
                bottom: 0,
                left: 0,
                right: 0,
                child: Container(
                  height: context.height() * 0.15,
                  width: context.width(),
                  color: context.primaryColor,
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      RawMaterialButton(
                        onPressed: _switchSpeakerphone,
                        child: Icon(
                          Octicons.unmute,
                          color: Colors.white,
                          size: 24.0,
                        ),
                        shape: CircleBorder(),
                        elevation: 0,
                        fillColor: enableSpeakerphone ? Colors.white38 : context.primaryColor,
                        padding: const EdgeInsets.all(16.0),
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
                        onPressed: _switchMicrophone,
                        child: Icon(
                          MaterialIcons.mic_off,
                          color: Colors.white,
                          size: 24.0,
                        ),
                        shape: CircleBorder(),
                        elevation: 0.0,
                        fillColor: openMicrophone ? Colors.white38 : context.primaryColor,
                        padding: const EdgeInsets.all(16.0),
                      )
                    ],
                  ),
                ),
              ),
            ],
          ),
        );
      }
    }

    return SafeArea(
      child: Scaffold(
        body: getCallScreen(value: widget.callModel?.callerId == getStringAsync(userId)),
      ),
    );
  }
}
