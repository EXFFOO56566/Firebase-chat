import 'dart:async';
import 'dart:io';
import 'dart:typed_data';

import 'package:extended_image/extended_image.dart';
import 'package:fiberchat/Utils/utils.dart';
import 'package:fiberchat/widgets/PhotoEditor/widgets/common_widget.dart';
import 'package:fiberchat/widgets/PhotoEditor/widgets/crop_editor_helper.dart';
import 'package:fiberchat/widgets/PhotoEditor/widgets/image_picker/image_picker.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

class PhotoEditor extends StatefulWidget {
  final File? imageFilePreSelected;
  final bool isPNG;
  final Function(File finalEditedimage) onImageEdit;
  final Function()? onClose;
  const PhotoEditor(
      {Key? key,
      this.imageFilePreSelected,
      this.onClose,
      required this.onImageEdit,
      required this.isPNG})
      : super(key: key);
  @override
  _PhotoEditorState createState() => _PhotoEditorState();
}

class _PhotoEditorState extends State<PhotoEditor> {
  final GlobalKey<ExtendedImageEditorState> editorKey =
      GlobalKey<ExtendedImageEditorState>();
  final GlobalKey<PopupMenuButtonState<EditorCropLayerPainter>> popupMenuKey =
      GlobalKey<PopupMenuButtonState<EditorCropLayerPainter>>();
  final List<AspectRatioItem> _aspectRatios = <AspectRatioItem>[
    AspectRatioItem(text: 'custom', value: CropAspectRatios.custom),
    AspectRatioItem(text: 'original', value: CropAspectRatios.original),
    AspectRatioItem(text: '1*1', value: CropAspectRatios.ratio1_1),
    AspectRatioItem(text: '4*3', value: CropAspectRatios.ratio4_3),
    AspectRatioItem(text: '3*4', value: CropAspectRatios.ratio3_4),
    AspectRatioItem(text: '16*9', value: CropAspectRatios.ratio16_9),
    AspectRatioItem(text: '9*16', value: CropAspectRatios.ratio9_16)
  ];
  AspectRatioItem? _aspectRatio;

  EditorCropLayerPainter? _cropLayerPainter;

  @override
  void initState() {
    if (widget.imageFilePreSelected != null) {
      convertFileToImage();
    } else {
      _getImage();
    }
    _aspectRatio = _aspectRatios.first;
    _cropLayerPainter = const EditorCropLayerPainter();
    super.initState();
  }

  convertFileToImage() async {
    Uint8List bytes = widget.imageFilePreSelected!.readAsBytesSync();
    _memoryImage = bytes;
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    Color bottomIconColor = Colors.white70;
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        leading: IconButton(
          icon: Icon(
            Icons.close_outlined,
            color: Colors.white70,
          ),
          onPressed: () {
            if (widget.onClose != null) {
              widget.onClose!();
            }
            Navigator.of(context).pop();
          },
        ),
        elevation: 0,
        backgroundColor: Colors.black,
        actions: <Widget>[
          _memoryImage == null
              ? SizedBox()
              : IconButton(
                  icon: const Icon(Icons.done),
                  onPressed: () async {
                    if (kIsWeb) {
                      _cropImage(false);
                    } else {
                      // _showCropDialog(context);
                      _cropImage(true);
                    }
                  },
                ),
        ],
      ),
      body: Column(children: <Widget>[
        Expanded(
            child: _memoryImage != null
                ? ExtendedImage.memory(
                    _memoryImage!,
                    fit: BoxFit.contain,
                    mode: ExtendedImageMode.editor,
                    enableLoadState: true,
                    extendedImageEditorKey: editorKey,
                    initEditorConfigHandler: (ExtendedImageState? state) {
                      return EditorConfig(
                        maxScale: 8.0,
                        cropRectPadding: const EdgeInsets.all(20.0),
                        hitTestSize: 20.0,
                        cropLayerPainter: _cropLayerPainter!,
                        editorMaskColorHandler: (context, boo) {
                          return Colors.black.withOpacity(0.6);
                        },
                        initCropRectType: InitCropRectType.imageRect,
                        cropAspectRatio: _aspectRatio!.value,
                      );
                    },
                    cacheRawData: true,
                  )
                : Center(
                    child: Padding(
                      padding: EdgeInsets.all(0),
                      child: IconButton(
                        icon: Icon(
                          Icons.add_photo_alternate_rounded,
                          color: Colors.white70,
                          size: 60,
                        ),
                        onPressed: _getImage,
                      ),
                    ),
                  )
            // ExtendedImage.asset(
            //     'assets/image.jpg',
            //     fit: BoxFit.contain,
            //     mode: ExtendedImageMode.editor,
            //     enableLoadState: true,
            //     extendedImageEditorKey: editorKey,
            //     initEditorConfigHandler: (ExtendedImageState? state) {
            //       return EditorConfig(
            //         maxScale: 8.0,
            //         cropRectPadding: const EdgeInsets.all(20.0),
            //         hitTestSize: 20.0,
            //         cropLayerPainter: _cropLayerPainter!,
            //         initCropRectType: InitCropRectType.imageRect,
            //         cropAspectRatio: _aspectRatio!.value,
            //       );
            //     },
            //     cacheRawData: true,
            //   ),
            ),
      ]),
      bottomNavigationBar: BottomAppBar(
          elevation: 0,
          color: Colors.black,
          shape: const CircularNotchedRectangle(),
          child: Container(
            height: 80,
            child: ListView(
              physics: AlwaysScrollableScrollPhysics(),
              shrinkWrap: true,
              scrollDirection: Axis.horizontal,
              children: [
                FlatButtonWithIcon(
                  icon: Icon(Icons.crop, color: bottomIconColor),
                  label: Text(
                    'Crop',
                    style: TextStyle(fontSize: 10.0, color: bottomIconColor),
                  ),
                  textColor: Colors.white70,
                  onPressed: () {
                    showDialog<void>(
                        context: context,
                        builder: (BuildContext context) {
                          return Column(
                            children: <Widget>[
                              const Expanded(
                                child: SizedBox(),
                              ),
                              SizedBox(
                                height: 100,
                                child: ListView.builder(
                                  shrinkWrap: true,
                                  scrollDirection: Axis.horizontal,
                                  padding: const EdgeInsets.all(20.0),
                                  itemBuilder: (_, int index) {
                                    final AspectRatioItem item =
                                        _aspectRatios[index];
                                    return GestureDetector(
                                      child: AspectRatioWidget(
                                        aspectRatio: item.value,
                                        aspectRatioS: item.text,
                                        isSelected: item == _aspectRatio,
                                      ),
                                      onTap: () {
                                        Navigator.pop(context);
                                        setState(() {
                                          _aspectRatio = item;
                                        });
                                      },
                                    );
                                  },
                                  itemCount: _aspectRatios.length,
                                ),
                              ),
                            ],
                          );
                        });
                  },
                ),
                FlatButtonWithIcon(
                  icon: Icon(
                    Icons.flip,
                    color: bottomIconColor,
                  ),
                  label: Text(
                    'Flip',
                    style: TextStyle(fontSize: 10.0, color: bottomIconColor),
                  ),
                  textColor: Colors.white,
                  onPressed: () {
                    editorKey.currentState!.flip();
                  },
                ),
                FlatButtonWithIcon(
                  icon: Icon(
                    Icons.rotate_left,
                    color: bottomIconColor,
                  ),
                  label: Text(
                    'Rotate Left',
                    style: TextStyle(
                      fontSize: 8.0,
                      color: bottomIconColor,
                    ),
                  ),
                  textColor: Colors.white,
                  onPressed: () {
                    editorKey.currentState!.rotate(right: false);
                  },
                ),
                FlatButtonWithIcon(
                  icon: Icon(
                    Icons.rotate_right,
                    color: bottomIconColor,
                  ),
                  label: Text(
                    'Rotate Right',
                    style: TextStyle(
                      fontSize: 8.0,
                      color: bottomIconColor,
                    ),
                  ),
                  textColor: Colors.white,
                  onPressed: () {
                    editorKey.currentState!.rotate(right: true);
                  },
                ),
                FlatButtonWithIcon(
                  icon: Icon(
                    Icons.rounded_corner_sharp,
                    color: bottomIconColor,
                  ),
                  label: PopupMenuButton<EditorCropLayerPainter>(
                    key: popupMenuKey,
                    enabled: false,
                    offset: const Offset(100, -300),
                    child: Text(
                      'Painter',
                      style: TextStyle(
                        fontSize: 8.0,
                        color: bottomIconColor,
                      ),
                    ),
                    initialValue: _cropLayerPainter,
                    itemBuilder: (BuildContext context) {
                      return <PopupMenuEntry<EditorCropLayerPainter>>[
                        PopupMenuItem<EditorCropLayerPainter>(
                          child: Row(
                            children: const <Widget>[
                              Icon(
                                Icons.rounded_corner_sharp,
                                color: Colors.blue,
                              ),
                              SizedBox(
                                width: 5,
                              ),
                              Text('Default'),
                            ],
                          ),
                          value: const EditorCropLayerPainter(),
                        ),
                        const PopupMenuDivider(),
                        PopupMenuItem<EditorCropLayerPainter>(
                          child: Row(
                            children: const <Widget>[
                              Icon(
                                Icons.circle,
                                color: Colors.blue,
                              ),
                              SizedBox(
                                width: 5,
                              ),
                              Text('Custom'),
                            ],
                          ),
                          value: const CustomEditorCropLayerPainter(),
                        ),
                        const PopupMenuDivider(),
                        PopupMenuItem<EditorCropLayerPainter>(
                          child: Row(
                            children: const <Widget>[
                              Icon(
                                CupertinoIcons.circle,
                                color: Colors.blue,
                              ),
                              SizedBox(
                                width: 5,
                              ),
                              Text('Circle'),
                            ],
                          ),
                          value: const CircleEditorCropLayerPainter(),
                        ),
                      ];
                    },
                    onSelected: (EditorCropLayerPainter value) {
                      if (_cropLayerPainter != value) {
                        setState(() {
                          if (value is CircleEditorCropLayerPainter) {
                            _aspectRatio = _aspectRatios[2];
                          }
                          _cropLayerPainter = value;
                        });
                      }
                    },
                  ),
                  textColor: Colors.white,
                  onPressed: () {
                    popupMenuKey.currentState!.showButtonMenu();
                  },
                ),
                FlatButtonWithIcon(
                  icon: Icon(
                    Icons.restore,
                    color: bottomIconColor,
                  ),
                  label: Text(
                    'Reset',
                    style: TextStyle(
                      fontSize: 10.0,
                      color: bottomIconColor,
                    ),
                  ),
                  textColor: Colors.white,
                  onPressed: () {
                    editorKey.currentState!.reset();
                  },
                ),
              ],
            ),
          )

          // ButtonTheme(
          //   focusColor: Colors.white,
          //   buttonColor: Colors.white,
          //   splashColor: Colors.white,
          //   hoverColor: Colors.white,
          //   highlightColor: Colors.white,
          //   disabledColor: Colors.white,
          //   minWidth: 0.0,
          //   padding: EdgeInsets.zero,
          //   child: Row(
          //     mainAxisAlignment: MainAxisAlignment.spaceAround,
          //     mainAxisSize: MainAxisSize.max,
          //     children: <Widget>[

          ),
    );
  }

  Future<void> _cropImage(bool useNative) async {
    // if (_cropping) {
    //   return;
    // }
    try {
      //await showBusyingDialog();

      Uint8List? fileData;

      /// native library
      if (useNative) {
        fileData = await cropImageDataWithNativeLibrary(
            state: editorKey.currentState!);
      } else {
        ///delay due to cropImageDataWithDartLibrary is time consuming on main thread
        ///it will block showBusyingDialog
        ///if you don't want to block ui, use compute/isolate,but it costs more time.
        //await Future.delayed(Duration(milliseconds: 200));

        ///if you don't want to block ui, use compute/isolate,but it costs more time.
        fileData =
            await cropImageDataWithDartLibrary(state: editorKey.currentState!);
      }
      final String? filePath = await ImageSaver.save(
          '${DateTime.now().millisecondsSinceEpoch}.jpg', fileData!);
      // var filePath = await ImagePickerSaver.saveFile(fileData: fileData);

      Navigator.of(context).pop();
      widget.onImageEdit(File(filePath!));
    } catch (e) {
      Fiberchat.toast("Failed. ERROR: $e");
    }

    //Navigator.of(context).pop();
  }

  Uint8List? _memoryImage;
  Future<void> _getImage() async {
    _memoryImage = await pickImage(context);
    //when back to current page, may be editorKey.currentState is not ready.
    Future<void>.delayed(const Duration(milliseconds: 200), () {
      if (editorKey.currentState != null) {
        setState(() {
          editorKey.currentState!.reset();
        });
      }
    });
  }
}

class CustomEditorCropLayerPainter extends EditorCropLayerPainter {
  const CustomEditorCropLayerPainter();
  @override
  void paintCorners(
      Canvas canvas, Size size, ExtendedImageCropLayerPainter painter) {
    final Paint paint = Paint()
      ..color = painter.cornerColor
      ..style = PaintingStyle.fill;
    final Rect cropRect = painter.cropRect;
    const double radius = 6;
    canvas.drawCircle(Offset(cropRect.left, cropRect.top), radius, paint);
    canvas.drawCircle(Offset(cropRect.right, cropRect.top), radius, paint);
    canvas.drawCircle(Offset(cropRect.left, cropRect.bottom), radius, paint);
    canvas.drawCircle(Offset(cropRect.right, cropRect.bottom), radius, paint);
  }
}

class CircleEditorCropLayerPainter extends EditorCropLayerPainter {
  const CircleEditorCropLayerPainter();

  @override
  void paintCorners(
      Canvas canvas, Size size, ExtendedImageCropLayerPainter painter) {
    // do nothing
  }

  @override
  void paintMask(
      Canvas canvas, Size size, ExtendedImageCropLayerPainter painter) {
    final Rect rect = Offset.zero & size;
    final Rect cropRect = painter.cropRect;
    final Color maskColor = painter.maskColor;
    canvas.saveLayer(rect, Paint());
    canvas.drawRect(
        rect,
        Paint()
          ..style = PaintingStyle.fill
          ..color = maskColor);
    canvas.drawCircle(cropRect.center, cropRect.width / 2.0,
        Paint()..blendMode = BlendMode.clear);
    canvas.restore();
  }

  @override
  void paintLines(
      Canvas canvas, Size size, ExtendedImageCropLayerPainter painter) {
    final Rect cropRect = painter.cropRect;
    if (painter.pointerDown) {
      canvas.save();
      canvas.clipPath(Path()..addOval(cropRect));
      super.paintLines(canvas, size, painter);
      canvas.restore();
    }
  }
}
