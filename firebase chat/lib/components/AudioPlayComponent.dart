import 'package:audioplayers/audioplayers.dart';
import 'package:chat/models/ChatMessageModel.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:nb_utils/nb_utils.dart';

class AudioPlayComponent extends StatefulWidget {
  final ChatMessageModel? data;
  final String? time;
  AudioPlayComponent({this.data, this.time});

  @override
  _AudioPlayComponentState createState() => _AudioPlayComponentState();
}

class _AudioPlayComponentState extends State<AudioPlayComponent> {
  AudioPlayer audioPlayer = AudioPlayer();

  Duration duration = Duration();
  Duration position = Duration();
  double minValue = 0.0;
  bool isPlaying = false;
  @override
  void initState() {
    super.initState();
    init();
  }

  init() async {
    audioPlayer.setUrl(widget.data!.photoUrl.validate(), isLocal: true, respectSilence: true);
    setState(() {});
    audioPlayer.getDuration().then((value) => log("$value"));
  }

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  Widget build(BuildContext context) {
    return Container(
        child: Stack(
      children: [
        Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              height: 50,
              decoration: boxDecorationWithShadow(backgroundColor: chatColor.withOpacity(0.8), borderRadius: BorderRadius.all(Radius.circular(8))),
              margin: EdgeInsets.all(2),
              width: 50,
              child: Icon(Icons.headset_outlined, color: Colors.white54),
            ),
            widget.data!.photoUrl == null
                ? CircularProgressIndicator().withHeight(25).withWidth(25).paddingAll(8)
                : IconButton(
                    icon: Icon(
                      isPlaying ? Icons.pause : Icons.play_arrow,
                      color: !widget.data!.isMe! ? chatColor : Colors.white,
                    ).center(),
                    onPressed: () async {
                      if (isPlaying) {
                        int res = await audioPlayer.pause();
                        if (res == 1) {
                          isPlaying = false;
                        }
                      } else {
                        int res = await audioPlayer.play(widget.data!.photoUrl!, isLocal: true);
                        if (res == 1) {
                          isPlaying = true;
                        }
                      }
                      setState(() {});

                      audioPlayer.onDurationChanged.listen((Duration dd) {
                        duration = dd;

                        setState(() {});
                      });

                      audioPlayer.onAudioPositionChanged.listen((Duration dd) {
                        position = dd;
                        setState(() {});
                      });
                    },
                  ),
            SliderTheme(
              data: SliderThemeData(
                trackHeight: 3.0,
                thumbShape: RoundSliderThumbShape(enabledThumbRadius: 8.0),
                overlayColor: Colors.purple.withAlpha(32),
                overlayShape: RoundSliderOverlayShape(overlayRadius: 14.0),
              ),
              child: Slider(
                min: minValue,
                activeColor: !widget.data!.isMe! ? chatColor : Colors.white,
                inactiveColor: !widget.data!.isMe! ? chatColor : Colors.white12,
                max: duration.inSeconds.toDouble(),
                value: position.inSeconds.toDouble(),
                onChanged: (value) {
                  audioPlayer.seek(Duration(seconds: value.toInt()));
                },
              ).expand(),
            )
          ],
        ),
        Positioned(
            bottom: 2,
            right: 8,
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  widget.time.validate(),
                  style: primaryTextStyle(
                    color: !widget.data!.isMe.validate() ? Colors.blueGrey.withOpacity(0.6) : whiteColor.withOpacity(0.6),
                    size: 10,
                  ),
                ),
                2.width,
                widget.data!.isMe!
                    ? !widget.data!.isMessageRead!
                        ? Icon(Icons.done, size: 12, color: Colors.white60)
                        : Icon(Icons.done_all, size: 12, color: Colors.white60)
                    : Offstage()
              ],
            ))
      ],
    ));
  }
}
