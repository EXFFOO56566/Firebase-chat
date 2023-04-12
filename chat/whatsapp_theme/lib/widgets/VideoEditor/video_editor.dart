import 'dart:io';
import 'dart:typed_data';

import 'package:fiberchat/Configs/app_constants.dart';
import 'package:fiberchat/Services/helpers/size.dart';
import 'package:fiberchat/Services/helpers/transition.dart';
import 'package:fiberchat/Services/helpers/widgets/animated_interactive_viewer.dart';
import 'package:fiberchat/Services/helpers/widgets/widgets.dart';
import 'package:fiberchat/Utils/utils.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';
import 'package:percent_indicator/linear_percent_indicator.dart';
import 'package:video_editor/video_editor.dart';

class VideoEditor extends StatefulWidget {
  VideoEditor(
      {Key? key,
      required this.file,
      this.onClose,
      required this.thumbnailQuality,
      required this.videoQuality,
      required this.maxDuration,
      required this.onEditExported})
      : super(key: key);

  final File file;
  final Function()? onClose;
  final int thumbnailQuality;
  final int videoQuality;
  final int maxDuration;
  final Function(File videoFile, File thumbnailFile) onEditExported;

  @override
  _VideoEditorState createState() => _VideoEditorState();
}

class _VideoEditorState extends State<VideoEditor> {
  final _exportingProgress = ValueNotifier<double>(0.0);
  final _isExporting = ValueNotifier<bool>(false);
  final double height = 60;

  bool _exported = false;
  String _exportText = "";
  late VideoEditorController _controller;

  @override
  void initState() {
    _controller = VideoEditorController.file(widget.file,
        maxDuration: Duration(seconds: widget.maxDuration))
      ..initialize().then((_) => setState(() {})).catchError((onError) {
        Navigator.of(context).pop();
      });
    super.initState();
  }

  @override
  void dispose() {
    _exportingProgress.dispose();
    _isExporting.dispose();
    _controller.dispose();
    super.dispose();
  }

  void _openCropScreen() => Navigator.of(context).push(MaterialPageRoute<void>(
      builder: (BuildContext context) => CropScreen(controller: _controller)));

  void _exportVideo() async {
    _isExporting.value = true;
    setState(() {});
    bool _firstStat = true;
    //NOTE: To use `-crf 1` and [VideoExportPreset] you need `ffmpeg_kit_flutter_min_gpl` package (with `ffmpeg_kit` only it won't work)
    await _controller.exportVideo(
      // preset: VideoExportPreset.medium,
      // customInstruction: "-crf 17",
      onProgress: (statics, val) {
        // First statistics is always wrong so if first one skip it
        if (_firstStat) {
          _firstStat = false;
        } else {
          _exportingProgress.value = statics.getTime() /
              _controller.video.value.duration.inMilliseconds;
        }
      },
      onCompleted: (file) async {
        _isExporting.value = false;
        if (!mounted) return;
        // ignore: unnecessary_null_comparison
        if (file != null) {
          if (_controller.selectedCoverVal == null) {
            _controller
                .extractCover(
                    onCompleted: (coverFile) {
                      Navigator.of(context).pop();
                      widget.onEditExported(file, coverFile);
                    },
                    quality: widget.thumbnailQuality)
                .catchError((err) {
              _exportText = "Error on export video :( \n\nERROR: $err";
              Navigator.of(context).pop();
              Fiberchat.toast(_exportText);
            });
          } else if (_controller.selectedCoverVal!.timeMs == 0) {
            _controller
                .extractCover(
                    onCompleted: (coverFile) {
                      Navigator.of(context).pop();
                      widget.onEditExported(file, coverFile);
                    },
                    quality: widget.thumbnailQuality)
                .catchError((err) {
              _exportText = "Error on export video :( \n\nERROR: $err";
              Navigator.of(context).pop();
              Fiberchat.toast(_exportText);
            });
          } else {
            // File thumbnailFile =
            //     File.fromRawPath(_controller.selectedCoverVal!.thumbData!);
            // Navigator.of(context).pop();
            Uint8List imageInUnit8List =
                _controller.selectedCoverVal!.thumbData!;
            final tempDir = await getTemporaryDirectory();
            File thumbnailFile = await File(
                    '${tempDir.path}/${DateTime.now().millisecondsSinceEpoch}.png')
                .create();
            thumbnailFile.writeAsBytesSync(imageInUnit8List);
            Navigator.of(context).pop();
            widget.onEditExported(file, thumbnailFile);
          }
        } else {
          _exportText = "Error on export video :(";
          Navigator.of(context).pop();
          Fiberchat.toast(_exportText);
        }
      },
    );
  }

  // void _exportCover() async {
  //   setState(() => _exported = false);

  //   await _controller.extractCover(
  //       onCompleted: (cover) {
  //         if (!mounted) return;

  //         if (cover != null) {
  //           _exportText = "Cover exported! ${cover.path}";
  //           showModalBottomSheet(
  //             context: context,
  //             backgroundColor: Colors.black54,
  //             builder: (BuildContext context) =>
  //                 Image.memory(cover.readAsBytesSync()),
  //           );
  //         } else
  //           _exportText = "Error on cover exportation :(";

  //         setState(() => _exported = true);
  //         Misc.delayed(2000, () => setState(() => _exported = false));
  //       },
  //       quality: widget.thumbnailQuality);
  // }

  DateTime? currentBackPressTime = DateTime.now();
  Future<bool> onWillPop() {
    DateTime now = DateTime.now();
    if (now.difference(currentBackPressTime!) > Duration(seconds: 3)) {
      currentBackPressTime = now;
      Fiberchat.toast('Double Tap To Close Editor');
      return Future.value(false);
    } else {
      return Future.value(true);
    }
  }

  @override
  Widget build(BuildContext context) {
    SystemChrome.setSystemUIOverlayStyle(SystemUiOverlayStyle(
        statusBarColor: Colors.black,
        statusBarIconBrightness: Brightness.light));
    return WillPopScope(
        onWillPop: onWillPop,
        child: Scaffold(
          backgroundColor: Colors.black,
          body: _controller.initialized
              ? SafeArea(
                  child: Stack(children: [
                  Column(children: [
                    _topNavBar(),
                    Expanded(
                        child: DefaultTabController(
                            length: 2,
                            child: Column(children: [
                              Expanded(
                                  child: TabBarView(
                                physics: NeverScrollableScrollPhysics(),
                                children: [
                                  Stack(alignment: Alignment.center, children: [
                                    CropGridViewer(
                                      controller: _controller,
                                      showGrid: false,
                                    ),
                                    AnimatedBuilder(
                                      animation: _controller.video,
                                      builder: (_, __) => OpacityTransition(
                                        visible: !_controller.isPlaying,
                                        child: GestureDetector(
                                          onTap: _controller.video.play,
                                          child: Container(
                                            width: 40,
                                            height: 40,
                                            decoration: BoxDecoration(
                                              color: Colors.white,
                                              shape: BoxShape.circle,
                                            ),
                                            child: Icon(Icons.play_arrow,
                                                color: Colors.black),
                                          ),
                                        ),
                                      ),
                                    ),
                                  ]),
                                  CoverViewer(controller: _controller)
                                ],
                              )),
                              Container(
                                  height: 200,
                                  margin: EdgeInsets.only(top: 18),
                                  child: Column(children: [
                                    TabBar(
                                      indicatorWeight: 1,
                                      indicatorColor: Colors.white30,
                                      tabs: [
                                        Padding(
                                          padding: const EdgeInsets.all(8.0),
                                          child: Row(
                                              mainAxisAlignment:
                                                  MainAxisAlignment.center,
                                              children: [
                                                Padding(
                                                    padding: EdgeInsets.all(5),
                                                    child: Icon(
                                                      Icons.content_cut,
                                                      size: 16,
                                                      // color: Colors.white,
                                                    )),
                                                SizedBox(
                                                  width: 4,
                                                ),
                                                Text('Trim')
                                              ]),
                                        ),
                                        Padding(
                                          padding: const EdgeInsets.all(8.0),
                                          child: Row(
                                              mainAxisAlignment:
                                                  MainAxisAlignment.center,
                                              children: [
                                                Padding(
                                                    padding: EdgeInsets.all(5),
                                                    child: Icon(
                                                      Icons.video_label,
                                                      size: 16,
                                                      // color: Colors.white
                                                    )),
                                                SizedBox(
                                                  width: 4,
                                                ),
                                                Text('Cover')
                                              ]),
                                        ),
                                      ],
                                    ),
                                    Expanded(
                                      child: TabBarView(
                                        children: [
                                          Container(
                                              child: Column(
                                                  mainAxisAlignment:
                                                      MainAxisAlignment.center,
                                                  children: _trimSlider())),
                                          Container(
                                            child: Column(
                                                mainAxisAlignment:
                                                    MainAxisAlignment.center,
                                                children: [_coverSelection()]),
                                          ),
                                        ],
                                      ),
                                    )
                                  ])),
                              _customSnackBar(),
                              ValueListenableBuilder(
                                valueListenable: _isExporting,
                                builder: (_, bool export, __) =>
                                    OpacityTransition(
                                        visible: export,
                                        child: Padding(
                                            padding: const EdgeInsets.all(18.0),
                                            child: ValueListenableBuilder(
                                              valueListenable:
                                                  _exportingProgress,
                                              builder: (_, double value, __) =>
                                                  LinearPercentIndicator(
                                                width: MediaQuery.of(context)
                                                        .size
                                                        .width /
                                                    1.2,
                                                lineHeight: 8.0,
                                                percent:
                                                    (value * 100).ceil() / 100,
                                                progressColor:
                                                    Colors.greenAccent,
                                              ),
                                            ))),

                                //     OpacityTransition(
                                //   visible: export,
                                //   child: AlertDialog(
                                //     backgroundColor: Colors.greenAccent,
                                //     title: ValueListenableBuilder(
                                //       valueListenable: _exportingProgress,
                                //       builder: (_, double value, __) => Text(
                                //         "Processing ${(value * 100).ceil()}%",
                                //         style: TextStyle(
                                //           color: Colors.black,
                                //           fontWeight: FontWeight.bold,
                                //         ),
                                //       ),
                                //     ),
                                //   ),
                                // ),
                              )
                            ])))
                  ])
                ]))
              : Center(child: CircularProgressIndicator()),
        ));
  }

  Widget _topNavBar() {
    return SafeArea(
      child: Container(
        height: height,
        child: Row(
          children: [
            Expanded(
              child: GestureDetector(
                onTap: () => _controller.rotate90Degrees(RotateDirection.left),
                child: Icon(
                  Icons.rotate_left,
                  color: _isExporting.value ? Colors.transparent : Colors.white,
                ),
              ),
            ),
            Expanded(
              child: GestureDetector(
                onTap: () => _controller.rotate90Degrees(RotateDirection.right),
                child: Icon(
                  Icons.rotate_right,
                  color: _isExporting.value ? Colors.transparent : Colors.white,
                ),
              ),
            ),
            Expanded(
              child: GestureDetector(
                onTap: _openCropScreen,
                child: Icon(
                  Icons.crop,
                  color: _isExporting.value ? Colors.transparent : Colors.white,
                ),
              ),
            ),
            // Expanded(
            //   child: GestureDetector(
            //     onTap: _exportCover,
            //     child: Icon(Icons.close, color: Colors.grey),
            //   ),
            // ),
            Expanded(
              child: GestureDetector(
                onTap: () {
                  if (widget.onClose != null) {
                    widget.onClose!();
                  }
                  Navigator.of(context).pop();
                },
                child: Icon(Icons.close,
                    color:
                        _isExporting.value ? Colors.transparent : Colors.grey),
              ),
            ),
            Expanded(
              child: GestureDetector(
                onTap: _exportVideo,
                child: Icon(
                  Icons.done,
                  color:
                      _isExporting.value ? Colors.transparent : fiberchatgreen,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  String formatter(Duration duration) => [
        duration.inMinutes.remainder(60).toString().padLeft(2, '0'),
        duration.inSeconds.remainder(60).toString().padLeft(2, '0')
      ].join(":");

  List<Widget> _trimSlider() {
    return [
      AnimatedBuilder(
        animation: _controller.video,
        builder: (_, __) {
          final duration = _controller.video.value.duration.inSeconds;
          final pos = _controller.trimPosition * duration;
          final start = _controller.minTrim * duration;
          final end = _controller.maxTrim * duration;

          return Padding(
            padding: EdgeInsets.only(left: height / 4, right: height / 4),
            child: Row(children: [
              Text(formatter(Duration(seconds: pos.toInt()))),
              Expanded(child: SizedBox()),
              OpacityTransition(
                visible: _controller.isTrimming,
                child: Row(mainAxisSize: MainAxisSize.min, children: [
                  Text(formatter(Duration(seconds: start.toInt()))),
                  SizedBox(width: 10),
                  Text(formatter(Duration(seconds: end.toInt()))),
                ]),
              )
            ]),
          );
        },
      ),
      Container(
        width: MediaQuery.of(context).size.width,
        margin: EdgeInsets.only(top: height / 4, bottom: height / 4),
        child: TrimSlider(
            child: TrimTimeline(
                controller: _controller, margin: EdgeInsets.only(top: 10)),
            controller: _controller,
            height: height,
            horizontalMargin: height / 4),
      )
    ];
  }

  Widget _coverSelection() {
    return Container(
        margin: EdgeInsets.only(left: height / 4, right: height / 4),
        child: CoverSelection(
          quality: widget.thumbnailQuality,
          controller: _controller,
          height: height,
        ));
  }

  Widget _customSnackBar() {
    return Align(
      alignment: Alignment.bottomCenter,
      child: SwipeTransition(
        visible: _exported,
        axisAlignment: 1.0,
        child: Container(
          height: height,
          width: double.infinity,
          color: Colors.black.withOpacity(0.8),
          child: Center(
            child: Text(_exportText,
                style: TextStyle(fontWeight: FontWeight.bold)),
          ),
        ),
      ),
    );
  }
}

//-----------------//
//CROP VIDEO SCREEN//
//-----------------//
class CropScreen extends StatelessWidget {
  CropScreen({Key? key, required this.controller}) : super(key: key);

  final VideoEditorController controller;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: SafeArea(
        child: Padding(
          padding: EdgeInsets.all(30),
          child: Column(children: [
            Row(children: [
              Expanded(
                child: GestureDetector(
                  onTap: () => controller.rotate90Degrees(RotateDirection.left),
                  child: Icon(Icons.rotate_left, color: Colors.white),
                ),
              ),
              Expanded(
                child: GestureDetector(
                  onTap: () =>
                      controller.rotate90Degrees(RotateDirection.right),
                  child: Icon(Icons.rotate_right, color: Colors.white),
                ),
              )
            ]),
            SizedBox(height: 15),
            Expanded(
              child: AnimatedInteractiveViewer(
                maxScale: 2.4,
                child: CropGridViewer(
                    controller: controller, horizontalMargin: 60),
              ),
            ),
            SizedBox(height: 15),
            Row(children: [
              Expanded(
                child: SplashTap(
                  onTap: () {
                    Navigator.of(context).pop();
                  },
                  child: Center(
                    child: Text(
                      "CANCEL",
                      style: TextStyle(
                          fontWeight: FontWeight.bold, color: Colors.white),
                    ),
                  ),
                ),
              ),
              buildSplashTap("16:9", 16 / 9, padding: Margin.horizontal(10)),
              buildSplashTap("1:1", 1 / 1),
              buildSplashTap("4:5", 4 / 5, padding: Margin.horizontal(10)),
              buildSplashTap("NO", null, padding: Margin.right(10)),
              Expanded(
                child: SplashTap(
                  onTap: () {
                    //2 WAYS TO UPDATE CROP
                    //WAY 1:
                    controller.updateCrop();
                    /*WAY 2:
                    controller.minCrop = controller.cacheMinCrop;
                    controller.maxCrop = controller.cacheMaxCrop;
                    */
                    Navigator.of(context).pop();
                  },
                  child: Center(
                    child: Text(
                      "OK",
                      style: TextStyle(
                          fontWeight: FontWeight.bold, color: Colors.white),
                    ),
                  ),
                ),
              ),
            ]),
          ]),
        ),
      ),
    );
  }

  Widget buildSplashTap(
    String title,
    double? aspectRatio, {
    EdgeInsetsGeometry? padding,
  }) {
    return SplashTap(
      onTap: () => controller.preferredCropAspectRatio = aspectRatio,
      child: Padding(
        padding: padding ?? EdgeInsets.all(0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.aspect_ratio, color: Colors.white),
            Text(
              title,
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
          ],
        ),
      ),
    );
  }
}
