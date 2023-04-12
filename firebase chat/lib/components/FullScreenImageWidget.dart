import 'package:cached_video_player/cached_video_player.dart';
import 'package:chat/utils/Appwidgets.dart';
import 'package:flutter/material.dart';
import 'package:nb_utils/nb_utils.dart';

class FullScreenImageWidget extends StatefulWidget {
  final String? photoUrl;
  final String? name;
  final bool isFromChat;
  final bool isVideo;
  FullScreenImageWidget({this.photoUrl, this.name, this.isFromChat = false, this.isVideo = false});

  _FullScreenImageWidgetState createState() => _FullScreenImageWidgetState();
}

class _FullScreenImageWidgetState extends State<FullScreenImageWidget> {
  late VideoPlayerController _controller;
  @override
  void initState() {
    super.initState();
    init();
  }

  init() async {
    if (widget.isVideo) {
      _controller = VideoPlayerController.network(widget.photoUrl!)
        ..initialize().then((_) {
          // Ensure the first frame is shown after the video is initialized, even before the play button has been pressed.
          setState(() {});
          _controller.play();
          _controller.addListener(() {
            checkVideo();
          });
        });
    }
  }

  void checkVideo() {
    // Implement your calls inside these conditions' bodies :
    if (_controller.value.position == Duration(seconds: 0, minutes: 0, hours: 0)) {
      print('video Started');
    }

    if (_controller.value.position == _controller.value.duration) {
      print('video Ended');
      setState(() {});
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    Widget body() {
      if (widget.isVideo) {
        return Container(
          height: context.height() * 0.8,
          width: context.width(),
          child: Stack(
            children: [
              VideoPlayer(_controller),
              IconButton(
                icon: Container(
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: Colors.black38,
                  ),
                  alignment: Alignment.center,
                  child: Icon(
                    _controller.value.isPlaying ? Icons.pause : Icons.play_arrow,
                    color: Colors.white,
                  ).center(),
                ),
                onPressed: () {
                  _controller.value.isPlaying ? _controller.pause() : _controller.play();
                  setState(() {});
                },
              ).center(),
            ],
          ),
        );
      }
      return SizedBox.expand(
        child: Stack(
          children: <Widget>[
            Align(
              alignment: Alignment.center,
              child: Hero(
                tag: widget.photoUrl!,
                child: cachedImage(
                  widget.photoUrl,
                  fit: widget.isFromChat ? null : BoxFit.contain,
                  height: widget.isFromChat ? null : 400,
                  width: double.infinity,
                ),
              ),
            ),
          ],
        ),
      );
    }

    return SafeArea(
      child: Scaffold(
        backgroundColor: Colors.black,
        appBar: appBarWidget("${widget.name.validate().capitalizeFirstLetter()} by", textColor: Colors.white, color: Colors.black),
        body: body(),
      ),
    );
  }
}
