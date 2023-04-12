import 'dart:async';
import 'dart:io';

import 'package:camera/camera.dart';
import 'package:circular_countdown_timer/circular_countdown_timer.dart';
import 'package:fiberchat/Utils/utils.dart';
import 'package:fiberchat/main.dart';
import 'package:fiberchat/widgets/CameraGalleryImagePicker/image_pick.dart';
import 'package:fiberchat/widgets/PhotoEditor/photoeditor.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';
import 'package:video_player/video_player.dart';
import 'package:path/path.dart' as p;

class CameraImageGalleryPicker extends StatefulWidget {
  final Function(File file) onTakeFile;
  const CameraImageGalleryPicker({Key? key, required this.onTakeFile})
      : super(key: key);
  @override
  _CameraImageGalleryPickerState createState() {
    return _CameraImageGalleryPickerState();
  }
}

/// Returns a suitable camera icon for [direction].
IconData getCameraLensIcon(CameraLensDirection direction) {
  switch (direction) {
    case CameraLensDirection.back:
      return Icons.camera_rear;
    case CameraLensDirection.front:
      return Icons.camera_front;
    case CameraLensDirection.external:
      return Icons.camera;
    default:
      throw ArgumentError('Unknown lens direction');
  }
}

class _CameraImageGalleryPickerState extends State<CameraImageGalleryPicker>
    with WidgetsBindingObserver, TickerProviderStateMixin {
  CameraController? controller;
  XFile? imageFile;
  XFile? videoFile;
  VideoPlayerController? videoController;
  VoidCallback? videoPlayerListener;
  bool enableAudio = true;
  // ignore: unused_field
  double _minAvailableExposureOffset = 0.0;
  // ignore: unused_field
  double _maxAvailableExposureOffset = 0.0;
  // ignore: unused_field
  double _currentExposureOffset = 0.0;
  late AnimationController _flashModeControlRowAnimationController;
  // ignore: unused_field
  late Animation<double> _flashModeControlRowAnimation;
  late AnimationController _exposureModeControlRowAnimationController;
  // ignore: unused_field
  late Animation<double> _exposureModeControlRowAnimation;
  late AnimationController _focusModeControlRowAnimationController;
  // ignore: unused_field
  late Animation<double> _focusModeControlRowAnimation;
  double _minAvailableZoom = 1.0;
  double _maxAvailableZoom = 1.0;
  double _currentScale = 1.0;
  double _baseScale = 1.0;

  // Counting pointers (number of user fingers on screen)
  int _pointers = 0;

  @override
  void initState() {
    super.initState();
    _ambiguate(WidgetsBinding.instance)?.addObserver(this);
    onNewCameraSelected(cameras[0]);
    _flashModeControlRowAnimationController = AnimationController(
      duration: const Duration(milliseconds: 300),
      vsync: this,
    );
    _flashModeControlRowAnimation = CurvedAnimation(
      parent: _flashModeControlRowAnimationController,
      curve: Curves.easeInCubic,
    );
    _exposureModeControlRowAnimationController = AnimationController(
      duration: const Duration(milliseconds: 300),
      vsync: this,
    );
    _exposureModeControlRowAnimation = CurvedAnimation(
      parent: _exposureModeControlRowAnimationController,
      curve: Curves.easeInCubic,
    );
    _focusModeControlRowAnimationController = AnimationController(
      duration: const Duration(milliseconds: 300),
      vsync: this,
    );
    _focusModeControlRowAnimation = CurvedAnimation(
      parent: _focusModeControlRowAnimationController,
      curve: Curves.easeInCubic,
    );
  }

  @override
  void dispose() {
    _ambiguate(WidgetsBinding.instance)?.removeObserver(this);
    controller!.dispose();
    _flashModeControlRowAnimationController.dispose();
    _exposureModeControlRowAnimationController.dispose();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    // App state changed before we got the chance to initialize.
    if (controller == null || !controller!.value.isInitialized) {
      return;
    }
    if (state == AppLifecycleState.inactive) {
      controller?.dispose();
    } else if (state == AppLifecycleState.resumed) {
      if (controller != null) {
        onNewCameraSelected(controller!.description);
      }
    }
  }

  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();
  final CountDownController _timerController = CountDownController();
  DateTime? currentBackPressTime = DateTime.now();
  Future<bool> onWillPop() {
    if (controller != null &&
        controller!.value.isInitialized &&
        controller!.value.isRecordingVideo) {
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
          key: _scaffoldKey,
          // appBar: AppBar(
          //   elevation: 0,
          //   primary: true,
          //   backgroundColor: Colors.transparent,
          //   leading: IconButton(
          //       onPressed: () {
          //         Navigator.of(context).pop();
          //       },
          //       icon: Icon(
          //         Icons.close,
          //         size: 25,
          //         color: Colors.white,
          //       )),
          // ),
          body: Stack(
            fit: StackFit.expand,
            alignment: Alignment.topCenter,
            children: [
              Align(
                  alignment: Alignment.topCenter,
                  child: Padding(
                    padding: EdgeInsets.only(
                        top: MediaQuery.of(context).padding.top + 30),
                    child: _cameraPreviewWidget(),
                  )),

              Align(
                alignment: Alignment.bottomCenter,
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(15, 10, 15, 20),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    mainAxisSize: MainAxisSize.max,
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      controller != null &&
                              controller!.value.isInitialized &&
                              controller!.value.isRecordingVideo
                          ? SizedBox(
                              width: 47,
                            )
                          // IconButton(
                          //     onPressed: controller != null &&
                          //             controller!.value.isInitialized &&
                          //             controller!.value.isRecordingVideo
                          //         ? (controller!.value.isRecordingPaused)
                          //             ? onResumeButtonPressed
                          //             : onPauseButtonPressed
                          //         : null,
                          //     icon: Icon(
                          //       controller != null &&
                          //               controller!.value.isRecordingPaused
                          //           ? Icons.play_arrow
                          //           : Icons.pause,
                          //       size: 47,
                          //       color: Colors.white,
                          //     ))
                          : IconButton(
                              onPressed: () {
                                onNewCameraSelected(
                                    controller!.description == cameras[0]
                                        ? cameras[1]
                                        : cameras[0]);
                              },
                              icon: Icon(
                                Icons.cameraswitch_rounded,
                                size: 40,
                                color: Colors.white,
                              )),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.center,
                        mainAxisAlignment: MainAxisAlignment.end,
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          InkWell(
                            onTap: () {
                              HapticFeedback.mediumImpact();
                              onTakePictureButtonPressed();
                            },
                            child: Container(
                              width: 72,
                              height: 72,
                              decoration: BoxDecoration(
                                shape: BoxShape.circle,
                                border:
                                    Border.all(color: Colors.white, width: 3.8),
                                color: Colors.green,
                              ),
                            ),
                          )
                          // ElevatedButton.icon(
                          //   onPressed: () {},
                          //   label: Text("onTimeout"),
                          //   icon: Icon(Icons.timer_3),
                          // ),

                          // GestureDetector(
                          //   onTap: controller != null &&
                          //           controller!.value.isInitialized &&
                          //           !controller!.value.isRecordingVideo
                          //       ? onTakePictureButtonPressed
                          //       : onStopButtonPressed,
                          //   onLongPressStart: (_) =>
                          //       onVideoRecordButtonPressed(),
                          //   onLongPressEnd: (_) => onStopButtonPressed(),
                          //   // onTapUp: (_) => onVideoRecordButtonPressed(),
                          //   // onTapDown: (_) => onStopButtonPressed(),
                          //   child: controller != null &&
                          //           controller!.value.isInitialized &&
                          //           controller!.value.isRecordingVideo
                          //       ? Container(
                          //           width: 72,
                          //           height: 72,
                          //           decoration: BoxDecoration(
                          //             shape: BoxShape.circle,
                          //             border: Border.all(
                          //                 color: Colors.white70, width: 3.8),
                          //             color: Colors.redAccent,
                          //           ),
                          //         )
                          //       : Container(
                          //           width: 72,
                          //           height: 72,
                          //           decoration: BoxDecoration(
                          //             shape: BoxShape.circle,
                          //             border: Border.all(
                          //                 color: Colors.white, width: 3.8),
                          //             color: Colors.green,
                          //           ),
                          //         ),
                          // )
                        ],
                      ),
                      // IconButton(
                      //   icon: const Icon(Icons.flash_on),
                      //   color: Colors.blue,
                      //   onPressed: controller != null
                      //       ? onFlashModeButtonPressed
                      //       : null,
                      // ),
                      IconButton(
                          onPressed: () async {
                            // Navigator.of(context).pop();
                            File? selectedMedia = await pickSingleImage(context)
                                .catchError((err) {
                              Fiberchat.toast(
                                  "Invalid file. Cannot Select this file !");
                            });

                            if (selectedMedia == null) {
                            } else {
                              String fileExtension =
                                  p.extension(selectedMedia.path).toLowerCase();

                              if (fileExtension == ".png" ||
                                  fileExtension == ".jpg" ||
                                  fileExtension == ".jpeg") {
                                final tempDir = await getTemporaryDirectory();
                                File file = await File(fileExtension == ".png"
                                        ? '${tempDir.path}/${DateTime.now().millisecondsSinceEpoch}.png'
                                        : '${tempDir.path}/${DateTime.now().millisecondsSinceEpoch}.jpg')
                                    .create();
                                file.writeAsBytesSync(
                                    selectedMedia.readAsBytesSync());
                                controller!.dispose();
                                _flashModeControlRowAnimationController
                                    .dispose();
                                _exposureModeControlRowAnimationController
                                    .dispose();
                                await Navigator.pushReplacement(
                                    context,
                                    new MaterialPageRoute(
                                        builder: (context) => PhotoEditor(
                                              isPNG: false,
                                              onImageEdit: (editedImage) {
                                                widget.onTakeFile(editedImage);
                                              },
                                              imageFilePreSelected:
                                                  File(file.path),
                                            )));
                              } else {
                                Fiberchat.toast(
                                    "File type not supported. Please choose a .jpg, .jpeg, .png file. \n\nSelected file was $fileExtension ");
                              }
                            }
                          },
                          icon: Icon(
                            Icons.image,
                            size: 30,
                            color: Colors.white,
                          )),
                    ],
                  ),
                ),
              ),
              Platform.isIOS
                  ? Positioned(
                      top: MediaQuery.of(context).padding.top - 10,
                      right: 12,
                      child: Align(
                          alignment: Alignment.topCenter,
                          child: IconButton(
                            icon: Icon(
                              Icons.close_rounded,
                              color: Colors.white,
                            ),
                            onPressed: () {
                              Navigator.of(context).pop();
                            },
                          )),
                    )
                  : SizedBox()
              // Positioned(
              //   top: MediaQuery.of(context).padding.top + 30 + 10,
              //   child: Align(
              //     alignment: Alignment.topCenter,
              //     child:
              //   ),
              // )
              // _captureControlRowWidget()
            ],
          ),

          // Column(
          //   children: <Widget>[
          //     Expanded(
          //       child: Container(
          //         child: Padding(
          //           padding: const EdgeInsets.all(1.0),
          //           child: Center(
          //             child: _cameraPreviewWidget(),
          //           ),
          //         ),
          //         decoration: BoxDecoration(
          //           color: Colors.black,
          //           border: Border.all(
          //             color:
          //                 controller != null && controller!.value.isRecordingVideo
          //                     ? Colors.redAccent
          //                     : Colors.grey,
          //             width: 3.0,
          //           ),
          //         ),
          //       ),
          //     ),
          //     _captureControlRowWidget(),
          //     _modeControlRowWidget(),
          //     Padding(
          //       padding: const EdgeInsets.all(5.0),
          //       child: Row(
          //         mainAxisAlignment: MainAxisAlignment.start,
          //         children: <Widget>[
          //           _cameraTogglesRowWidget(),
          //           _thumbnailWidget(),
          //         ],
          //       ),
          //     ),
          //   ],
        ) // ),
        );
  }

  /// Display the preview from the camera (or a message if the preview is not available).
  Widget _cameraPreviewWidget() {
    final CameraController? cameraController = controller;

    if (cameraController == null || !cameraController.value.isInitialized) {
      return const Text(
        '',
        style: TextStyle(
          color: Colors.white,
          fontSize: 24.0,
          fontWeight: FontWeight.w900,
        ),
      );
    } else {
      return Listener(
        onPointerDown: (_) => _pointers++,
        onPointerUp: (_) => _pointers--,
        child: CameraPreview(
          controller!,
          child: LayoutBuilder(
              builder: (BuildContext context, BoxConstraints constraints) {
            return GestureDetector(
              behavior: HitTestBehavior.opaque,
              onScaleStart: _handleScaleStart,
              onScaleUpdate: _handleScaleUpdate,
              onTapDown: (TapDownDetails details) =>
                  onViewFinderTap(details, constraints),
            );
          }),
        ),
      );
    }
  }

  void _handleScaleStart(ScaleStartDetails details) {
    _baseScale = _currentScale;
  }

  Future<void> _handleScaleUpdate(ScaleUpdateDetails details) async {
    // When there are not exactly two fingers on screen don't scale
    if (controller == null || _pointers != 2) {
      return;
    }

    _currentScale = (_baseScale * details.scale)
        .clamp(_minAvailableZoom, _maxAvailableZoom);

    await controller!.setZoomLevel(_currentScale);
  }

  /// Display the thumbnail of the captured image or video.
  // ignore: unused_element
  Widget _thumbnailWidget() {
    final VideoPlayerController? localVideoController = videoController;

    return Expanded(
      child: Align(
        alignment: Alignment.centerRight,
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            if (localVideoController == null && imageFile == null)
              Container()
            else
              SizedBox(
                child: (localVideoController == null)
                    ? (
                        // The captured image on the web contains a network-accessible URL
                        // pointing to a location within the browser. It may be displayed
                        // either with Image.network or Image.memory after loading the image
                        // bytes to memory.
                        kIsWeb
                            ? Image.network(imageFile!.path)
                            : Image.file(File(imageFile!.path)))
                    : Container(
                        child: Center(
                          child: AspectRatio(
                              aspectRatio:
                                  localVideoController.value.aspectRatio,
                              child: VideoPlayer(localVideoController)),
                        ),
                        decoration: BoxDecoration(
                            border: Border.all(color: Colors.pink)),
                      ),
                width: 64.0,
                height: 64.0,
              ),
          ],
        ),
      ),
    );
  }

  /// Display a bar with buttons to change the flash and exposure modes
  // Widget _modeControlRowWidget() {
  //   return Column(
  //     children: <Widget>[
  //       Row(
  //         mainAxisAlignment: MainAxisAlignment.spaceEvenly,
  //         mainAxisSize: MainAxisSize.max,
  //         children: <Widget>[
  //           IconButton(
  //             icon: const Icon(Icons.flash_on),
  //             color: Colors.blue,
  //             onPressed: controller != null ? onFlashModeButtonPressed : null,
  //           ),
  //           // The exposure and focus mode are currently not supported on the web.
  //           ...!kIsWeb
  //               ? <Widget>[
  //                   IconButton(
  //                     icon: const Icon(Icons.exposure),
  //                     color: Colors.blue,
  //                     onPressed: controller != null
  //                         ? onExposureModeButtonPressed
  //                         : null,
  //                   ),
  //                   IconButton(
  //                     icon: const Icon(Icons.filter_center_focus),
  //                     color: Colors.blue,
  //                     onPressed:
  //                         controller != null ? onFocusModeButtonPressed : null,
  //                   )
  //                 ]
  //               : <Widget>[],
  //           IconButton(
  //             icon: Icon(enableAudio ? Icons.volume_up : Icons.volume_mute),
  //             color: Colors.blue,
  //             onPressed: controller != null ? onAudioModeButtonPressed : null,
  //           ),
  //           IconButton(
  //             icon: Icon(controller?.value.isCaptureOrientationLocked ?? false
  //                 ? Icons.screen_lock_rotation
  //                 : Icons.screen_rotation),
  //             color: Colors.blue,
  //             onPressed: controller != null
  //                 ? onCaptureOrientationLockButtonPressed
  //                 : null,
  //           ),
  //         ],
  //       ),
  //       _flashModeControlRowWidget(),
  //       _exposureModeControlRowWidget(),
  //       _focusModeControlRowWidget(),
  //     ],
  //   );
  // }

  // Widget _flashModeControlRowWidget() {
  //   return SizeTransition(
  //     sizeFactor: _flashModeControlRowAnimation,
  //     child: ClipRect(
  //       child: Row(
  //         mainAxisAlignment: MainAxisAlignment.spaceEvenly,
  //         mainAxisSize: MainAxisSize.max,
  //         children: <Widget>[
  //           IconButton(
  //             icon: const Icon(Icons.flash_off),
  //             color: controller?.value.flashMode == FlashMode.off
  //                 ? Colors.orange
  //                 : Colors.blue,
  //             onPressed: controller != null
  //                 ? () => onSetFlashModeButtonPressed(FlashMode.off)
  //                 : null,
  //           ),
  //           IconButton(
  //             icon: const Icon(Icons.flash_auto),
  //             color: controller?.value.flashMode == FlashMode.auto
  //                 ? Colors.orange
  //                 : Colors.blue,
  //             onPressed: controller != null
  //                 ? () => onSetFlashModeButtonPressed(FlashMode.auto)
  //                 : null,
  //           ),
  //           IconButton(
  //             icon: const Icon(Icons.flash_on),
  //             color: controller?.value.flashMode == FlashMode.always
  //                 ? Colors.orange
  //                 : Colors.blue,
  //             onPressed: controller != null
  //                 ? () => onSetFlashModeButtonPressed(FlashMode.always)
  //                 : null,
  //           ),
  //           IconButton(
  //             icon: const Icon(Icons.highlight),
  //             color: controller?.value.flashMode == FlashMode.torch
  //                 ? Colors.orange
  //                 : Colors.blue,
  //             onPressed: controller != null
  //                 ? () => onSetFlashModeButtonPressed(FlashMode.torch)
  //                 : null,
  //           ),
  //         ],
  //       ),
  //     ),
  //   );
  // }

  // Widget _exposureModeControlRowWidget() {
  //   final ButtonStyle styleAuto = TextButton.styleFrom(
  //     primary: controller?.value.exposureMode == ExposureMode.auto
  //         ? Colors.orange
  //         : Colors.blue,
  //   );
  //   final ButtonStyle styleLocked = TextButton.styleFrom(
  //     primary: controller?.value.exposureMode == ExposureMode.locked
  //         ? Colors.orange
  //         : Colors.blue,
  //   );

  //   return SizeTransition(
  //     sizeFactor: _exposureModeControlRowAnimation,
  //     child: ClipRect(
  //       child: Container(
  //         color: Colors.grey.shade50,
  //         child: Column(
  //           children: <Widget>[
  //             const Center(
  //               child: Text('Exposure Mode'),
  //             ),
  //             Row(
  //               mainAxisAlignment: MainAxisAlignment.spaceEvenly,
  //               mainAxisSize: MainAxisSize.max,
  //               children: <Widget>[
  //                 TextButton(
  //                   child: const Text('AUTO'),
  //                   style: styleAuto,
  //                   onPressed: controller != null
  //                       ? () =>
  //                           onSetExposureModeButtonPressed(ExposureMode.auto)
  //                       : null,
  //                   onLongPress: () {
  //                     if (controller != null) {
  //                       controller!.setExposurePoint(null);
  //                       showInSnackBar('Resetting exposure point');
  //                     }
  //                   },
  //                 ),
  //                 TextButton(
  //                   child: const Text('LOCKED'),
  //                   style: styleLocked,
  //                   onPressed: controller != null
  //                       ? () =>
  //                           onSetExposureModeButtonPressed(ExposureMode.locked)
  //                       : null,
  //                 ),
  //                 TextButton(
  //                   child: const Text('RESET OFFSET'),
  //                   style: styleLocked,
  //                   onPressed: controller != null
  //                       ? () => controller!.setExposureOffset(0.0)
  //                       : null,
  //                 ),
  //               ],
  //             ),
  //             const Center(
  //               child: Text('Exposure Offset'),
  //             ),
  //             Row(
  //               mainAxisAlignment: MainAxisAlignment.spaceEvenly,
  //               mainAxisSize: MainAxisSize.max,
  //               children: <Widget>[
  //                 Text(_minAvailableExposureOffset.toString()),
  //                 Slider(
  //                   value: _currentExposureOffset,
  //                   min: _minAvailableExposureOffset,
  //                   max: _maxAvailableExposureOffset,
  //                   label: _currentExposureOffset.toString(),
  //                   onChanged: _minAvailableExposureOffset ==
  //                           _maxAvailableExposureOffset
  //                       ? null
  //                       : setExposureOffset,
  //                 ),
  //                 Text(_maxAvailableExposureOffset.toString()),
  //               ],
  //             ),
  //           ],
  //         ),
  //       ),
  //     ),
  //   );
  // }

  // Widget _focusModeControlRowWidget() {
  //   final ButtonStyle styleAuto = TextButton.styleFrom(
  //     primary: controller?.value.focusMode == FocusMode.auto
  //         ? Colors.orange
  //         : Colors.blue,
  //   );
  //   final ButtonStyle styleLocked = TextButton.styleFrom(
  //     primary: controller?.value.focusMode == FocusMode.locked
  //         ? Colors.orange
  //         : Colors.blue,
  //   );

  //   return SizeTransition(
  //     sizeFactor: _focusModeControlRowAnimation,
  //     child: ClipRect(
  //       child: Container(
  //         color: Colors.grey.shade50,
  //         child: Column(
  //           children: <Widget>[
  //             const Center(
  //               child: Text('Focus Mode'),
  //             ),
  //             Row(
  //               mainAxisAlignment: MainAxisAlignment.spaceEvenly,
  //               mainAxisSize: MainAxisSize.max,
  //               children: <Widget>[
  //                 TextButton(
  //                   child: const Text('AUTO'),
  //                   style: styleAuto,
  //                   onPressed: controller != null
  //                       ? () => onSetFocusModeButtonPressed(FocusMode.auto)
  //                       : null,
  //                   onLongPress: () {
  //                     if (controller != null) {
  //                       controller!.setFocusPoint(null);
  //                     }
  //                     showInSnackBar('Resetting focus point');
  //                   },
  //                 ),
  //                 TextButton(
  //                   child: const Text('LOCKED'),
  //                   style: styleLocked,
  //                   onPressed: controller != null
  //                       ? () => onSetFocusModeButtonPressed(FocusMode.locked)
  //                       : null,
  //                 ),
  //               ],
  //             ),
  //           ],
  //         ),
  //       ),
  //     ),
  //   );
  // }

  // /// Display the control bar with buttons to take pictures and record videos.
  // Widget _captureControlRowWidget() {
  //   final CameraController? cameraController = controller;

  //   return Row(
  //     mainAxisAlignment: MainAxisAlignment.spaceEvenly,
  //     mainAxisSize: MainAxisSize.max,
  //     children: <Widget>[
  //       IconButton(
  //         icon: const Icon(Icons.camera_alt),
  //         color: Colors.blue,
  //         onPressed: cameraController != null &&
  //                 cameraController.value.isInitialized &&
  //                 !cameraController.value.isRecordingVideo
  //             ? onTakePictureButtonPressed
  //             : null,
  //       ),
  //       IconButton(
  //         icon: const Icon(Icons.videocam),
  //         color: Colors.blue,
  //         onPressed: cameraController != null &&
  //                 cameraController.value.isInitialized &&
  //                 !cameraController.value.isRecordingVideo
  //             ? onVideoRecordButtonPressed
  //             : null,
  //       ),
  //       IconButton(
  //         icon: cameraController != null &&
  //                 cameraController.value.isRecordingPaused
  //             ? const Icon(Icons.play_arrow)
  //             : const Icon(Icons.pause),
  //         color: Colors.blue,
  //         onPressed: cameraController != null &&
  //                 cameraController.value.isInitialized &&
  //                 cameraController.value.isRecordingVideo
  //             ? (cameraController.value.isRecordingPaused)
  //                 ? onResumeButtonPressed
  //                 : onPauseButtonPressed
  //             : null,
  //       ),
  //       IconButton(
  //         icon: const Icon(Icons.stop),
  //         color: Colors.red,
  //         onPressed: cameraController != null &&
  //                 cameraController.value.isInitialized &&
  //                 cameraController.value.isRecordingVideo
  //             ? onStopButtonPressed
  //             : null,
  //       ),
  //       IconButton(
  //         icon: const Icon(Icons.pause_presentation),
  //         color:
  //             cameraController != null && cameraController.value.isPreviewPaused
  //                 ? Colors.red
  //                 : Colors.blue,
  //         onPressed:
  //             cameraController == null ? null : onPausePreviewButtonPressed,
  //       ),
  //     ],
  //   );
  // }

  /// Display a row of toggle to select the camera (or a message if no camera is available).
  // Widget _cameraTogglesRowWidget() {
  //   final List<Widget> toggles = <Widget>[];

  //   final Null Function(CameraDescription? description) onChanged =
  //       (CameraDescription? description) {
  //     if (description == null) {
  //       return;
  //     }

  //     onNewCameraSelected(description);
  //   };

  //   if (cameras.isEmpty) {
  //     return const Text('No camera found');
  //   } else {
  //     for (final CameraDescription cameraDescription in cameras) {
  //       toggles.add(
  //         SizedBox(
  //           width: 90.0,
  //           child: RadioListTile<CameraDescription>(
  //             title: Icon(getCameraLensIcon(cameraDescription.lensDirection)),
  //             groupValue: controller?.description,
  //             value: cameraDescription,
  //             onChanged:
  //                 controller != null && controller!.value.isRecordingVideo
  //                     ? null
  //                     : onChanged,
  //           ),
  //         ),
  //       );
  //     }
  //   }

  //   return Row(children: toggles);
  // }

  String timestamp() => DateTime.now().millisecondsSinceEpoch.toString();

  void showInSnackBar(String message) {
    // ignore: deprecated_member_use
    _scaffoldKey.currentState?.showSnackBar(SnackBar(content: Text(message)));
  }

  void onViewFinderTap(TapDownDetails details, BoxConstraints constraints) {
    if (controller == null) {
      return;
    }

    final CameraController cameraController = controller!;

    final Offset offset = Offset(
      details.localPosition.dx / constraints.maxWidth,
      details.localPosition.dy / constraints.maxHeight,
    );
    cameraController.setExposurePoint(offset);
    cameraController.setFocusPoint(offset);
  }

  Future<void> onNewCameraSelected(CameraDescription cameraDescription) async {
    if (controller != null) {
      await controller!.dispose();
    }

    final CameraController cameraController = CameraController(
      cameraDescription,
      kIsWeb ? ResolutionPreset.max : ResolutionPreset.medium,
      enableAudio: enableAudio,
      imageFormatGroup: ImageFormatGroup.jpeg,
    );

    controller = cameraController;

    // If the controller is updated then update the UI.
    cameraController.addListener(() {
      if (mounted) {
        setState(() {});
      }
      if (cameraController.value.hasError) {
        showInSnackBar(
            'Camera error ${cameraController.value.errorDescription}');
      }
    });

    try {
      await cameraController.initialize();
      await Future.wait(<Future<Object?>>[
        // The exposure mode is currently not supported on the web.
        ...!kIsWeb
            ? <Future<Object?>>[
                cameraController.getMinExposureOffset().then(
                    (double value) => _minAvailableExposureOffset = value),
                cameraController
                    .getMaxExposureOffset()
                    .then((double value) => _maxAvailableExposureOffset = value)
              ]
            : <Future<Object?>>[],
        cameraController
            .getMaxZoomLevel()
            .then((double value) => _maxAvailableZoom = value),
        cameraController
            .getMinZoomLevel()
            .then((double value) => _minAvailableZoom = value),
      ]);
    } on CameraException catch (e) {
      _showCameraException(e);
    }

    if (mounted) {
      setState(() {});
    }
  }

  void onTakePictureButtonPressed() {
    takePicture().then((XFile? file) async {
      if (mounted) {
        setState(() {
          imageFile = file;
          videoController?.dispose();
          videoController = null;
        });
        if (file != null) {
          //-----
          // Navigator.of(context).pop();

          // controller!.dispose();
          // _flashModeControlRowAnimationController.dispose();
          // _exposureModeControlRowAnimationController.dispose();
          await Navigator.pushReplacement(
              context,
              new MaterialPageRoute(
                  builder: (context) => PhotoEditor(
                        isPNG: false,
                        onImageEdit: (editedImage) {
                          widget.onTakeFile(editedImage);
                        },
                        imageFilePreSelected: File(file.path),
                      )));
        }
      }
    });
  }

  void onFlashModeButtonPressed() {
    if (_flashModeControlRowAnimationController.value == 1) {
      _flashModeControlRowAnimationController.reverse();
    } else {
      _flashModeControlRowAnimationController.forward();
      _exposureModeControlRowAnimationController.reverse();
      _focusModeControlRowAnimationController.reverse();
    }
  }

  void onExposureModeButtonPressed() {
    if (_exposureModeControlRowAnimationController.value == 1) {
      _exposureModeControlRowAnimationController.reverse();
    } else {
      _exposureModeControlRowAnimationController.forward();
      _flashModeControlRowAnimationController.reverse();
      _focusModeControlRowAnimationController.reverse();
    }
  }

  void onFocusModeButtonPressed() {
    if (_focusModeControlRowAnimationController.value == 1) {
      _focusModeControlRowAnimationController.reverse();
    } else {
      _focusModeControlRowAnimationController.forward();
      _flashModeControlRowAnimationController.reverse();
      _exposureModeControlRowAnimationController.reverse();
    }
  }

  void onAudioModeButtonPressed() {
    enableAudio = !enableAudio;
    if (controller != null) {
      onNewCameraSelected(controller!.description);
    }
  }

  Future<void> onCaptureOrientationLockButtonPressed() async {
    try {
      if (controller != null) {
        final CameraController cameraController = controller!;
        if (cameraController.value.isCaptureOrientationLocked) {
          await cameraController.unlockCaptureOrientation();
          showInSnackBar('Capture orientation unlocked');
        } else {
          await cameraController.lockCaptureOrientation();
          showInSnackBar(
              'Capture orientation locked to ${cameraController.value.lockedCaptureOrientation.toString().split('.').last}');
        }
      }
    } on CameraException catch (e) {
      _showCameraException(e);
    }
  }

  void onSetFlashModeButtonPressed(FlashMode mode) {
    setFlashMode(mode).then((_) {
      if (mounted) {
        setState(() {});
      }
      showInSnackBar('Flash mode set to ${mode.toString().split('.').last}');
    });
  }

  void onSetExposureModeButtonPressed(ExposureMode mode) {
    setExposureMode(mode).then((_) {
      if (mounted) {
        setState(() {});
      }
      showInSnackBar('Exposure mode set to ${mode.toString().split('.').last}');
    });
  }

  void onSetFocusModeButtonPressed(FocusMode mode) {
    setFocusMode(mode).then((_) {
      if (mounted) {
        setState(() {});
      }
      showInSnackBar('Focus mode set to ${mode.toString().split('.').last}');
    });
  }

  void onVideoRecordButtonPressed() {
    HapticFeedback.heavyImpact();
    startVideoRecording().then((_) {
      if (mounted) {
        setState(() {});
      }
    });
  }

  void onStopButtonPressed() {
    // HapticFeedback.heavyImpact();
    // stopVideoRecording().then((XFile? file) {
    //   if (mounted) {
    //     setState(() {});
    //   }
    //   if (file != null) {
    //     // showInSnackBar('Video recorded to ${file.path}');
    //     videoFile = file;
    //     // _startVideoPlayer();
    //     //-----
    //     Navigator.of(context).pop();
    //     widget.onTakeFile(file);
    //   }
    // }).catchError((ee) {
    //   Navigator.of(context).pop();
    // });
  }

  Future<void> onPausePreviewButtonPressed() async {
    final CameraController? cameraController = controller;

    if (cameraController == null || !cameraController.value.isInitialized) {
      showInSnackBar('Error: select a camera first.');
      return;
    }

    if (cameraController.value.isPreviewPaused) {
      await cameraController.resumePreview();
    } else {
      await cameraController.pausePreview();
    }

    if (mounted) {
      setState(() {});
    }
  }

  void onPauseButtonPressed() {
    pauseVideoRecording().then((_) {
      if (mounted) {
        setState(() {});
      }
      showInSnackBar('Video recording paused');
    });
  }

  void onResumeButtonPressed() {
    resumeVideoRecording().then((_) {
      if (mounted) {
        setState(() {});
      }
      showInSnackBar('Video recording resumed');
    });
  }

  Future<void> startVideoRecording() async {
    final CameraController? cameraController = controller;

    if (cameraController == null || !cameraController.value.isInitialized) {
      showInSnackBar('Error: select a camera first.');
      return;
    }

    if (cameraController.value.isRecordingVideo) {
      // A recording is already started, do nothing.
      return;
    }

    try {
      _timerController.start();
      await cameraController.startVideoRecording();
    } on CameraException catch (e) {
      _showCameraException(e);
      return;
    }
  }

  Future<XFile?> stopVideoRecording() async {
    final CameraController? cameraController = controller;

    if (cameraController == null || !cameraController.value.isRecordingVideo) {
      return null;
    }

    try {
      return cameraController.stopVideoRecording();
    } on CameraException catch (e) {
      _showCameraException(e);
      return null;
    }
  }

  Future<void> pauseVideoRecording() async {
    final CameraController? cameraController = controller;

    if (cameraController == null || !cameraController.value.isRecordingVideo) {
      return;
    }

    try {
      await cameraController.pauseVideoRecording();
    } on CameraException catch (e) {
      _showCameraException(e);
      rethrow;
    }
  }

  Future<void> resumeVideoRecording() async {
    final CameraController? cameraController = controller;

    if (cameraController == null || !cameraController.value.isRecordingVideo) {
      return;
    }

    try {
      await cameraController.resumeVideoRecording();
    } on CameraException catch (e) {
      _showCameraException(e);
      rethrow;
    }
  }

  Future<void> setFlashMode(FlashMode mode) async {
    if (controller == null) {
      return;
    }

    try {
      await controller!.setFlashMode(mode);
    } on CameraException catch (e) {
      _showCameraException(e);
      rethrow;
    }
  }

  Future<void> setExposureMode(ExposureMode mode) async {
    if (controller == null) {
      return;
    }

    try {
      await controller!.setExposureMode(mode);
    } on CameraException catch (e) {
      _showCameraException(e);
      rethrow;
    }
  }

  Future<void> setExposureOffset(double offset) async {
    if (controller == null) {
      return;
    }

    setState(() {
      _currentExposureOffset = offset;
    });
    try {
      offset = await controller!.setExposureOffset(offset);
    } on CameraException catch (e) {
      _showCameraException(e);
      rethrow;
    }
  }

  Future<void> setFocusMode(FocusMode mode) async {
    if (controller == null) {
      return;
    }

    try {
      await controller!.setFocusMode(mode);
    } on CameraException catch (e) {
      _showCameraException(e);
      rethrow;
    }
  }

  // Future<void> _startVideoPlayer() async {
  //   if (videoFile == null) {
  //     return;
  //   }

  //   final VideoPlayerController vController = kIsWeb
  //       ? VideoPlayerController.network(videoFile!.path)
  //       : VideoPlayerController.file(File(videoFile!.path));

  //   videoPlayerListener = () {
  //     if (videoController != null) {
  //       // Refreshing the state to update video player with the correct ratio.
  //       if (mounted) {
  //         setState(() {});
  //       }
  //       videoController!.removeListener(videoPlayerListener!);
  //     }
  //   };
  //   vController.addListener(videoPlayerListener!);
  //   await vController.setLooping(true);
  //   await vController.initialize();
  //   await videoController?.dispose();
  //   if (mounted) {
  //     setState(() {
  //       imageFile = null;
  //       videoController = vController;
  //     });
  //   }
  //   await vController.play();
  // }

  Future<XFile?> takePicture() async {
    final CameraController? cameraController = controller;
    if (cameraController == null || !cameraController.value.isInitialized) {
      showInSnackBar('Error: select a camera first.');
      return null;
    }

    if (cameraController.value.isTakingPicture) {
      // A capture is already pending, do nothing.
      return null;
    }

    try {
      final XFile file = await cameraController.takePicture();
      return file;
    } on CameraException catch (e) {
      _showCameraException(e);
      return null;
    }
  }

  void _showCameraException(CameraException e) {
    logError(e.code, e.description);
    showInSnackBar('Error: ${e.code}\n${e.description}');
  }
}

/// This allows a value of type T or T? to be treated as a value of type T?.
///
/// We use this so that APIs that have become non-nullable can still be used
/// with `!` and `?` on the stable branch.
T? _ambiguate<T>(T? value) => value;
