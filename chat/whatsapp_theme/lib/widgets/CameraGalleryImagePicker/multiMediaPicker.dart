import 'dart:io';
import 'package:flutter/material.dart';
import 'package:wechat_assets_picker/wechat_assets_picker.dart';

Future<File?> pickMultiMedia(BuildContext context) async {
  final List<AssetEntity>? result = await AssetPicker.pickAssets(
    context,
    pickerConfig: AssetPickerConfig(
      maxAssets: 1,
      pathThumbnailSize: ThumbnailSize.square(84),
      gridCount: 3,
      pageSize: 900,
      requestType: RequestType.common,
      textDelegate: EnglishAssetPickerTextDelegate(),
    ),
  );
  if (result != null) {
    return result.first.file;
  }
  return null;
}

Future<File?> pickVideoFromgallery(BuildContext context) async {
  final List<AssetEntity>? result = await AssetPicker.pickAssets(
    context,
    pickerConfig: AssetPickerConfig(
      maxAssets: 1,
      pathThumbnailSize: ThumbnailSize.square(84),
      gridCount: 3,
      pageSize: 900,
      requestType: RequestType.video,
      textDelegate: EnglishAssetPickerTextDelegate(),
    ),
  );
  if (result != null) {
    return result.first.file;
  }
  return null;
}
