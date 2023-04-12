import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:permission_handler_platform_interface/permission_handler_platform_interface.dart';

class Permissions {
  static PermissionHandlerPlatform get _handler => PermissionHandlerPlatform.instance;

  static Future<bool> cameraAndMicrophonePermissionsGranted() async {
    PermissionStatus cameraPermissionStatus = await _getCameraPermission();
    PermissionStatus microphonePermissionStatus = await _getMicrophonePermission();

    if (cameraPermissionStatus == PermissionStatus.granted && microphonePermissionStatus == PermissionStatus.granted) {
      return true;
    } else {
      _handleInvalidPermissions(cameraPermissionStatus, microphonePermissionStatus);
      return false;
    }
  }

  static Future<PermissionStatus> _getCameraPermission() async {
    PermissionStatus permission = await _handler.checkPermissionStatus(Permission.camera);
    if (permission != PermissionStatus.granted) {
      Map<Permission, PermissionStatus> permissionStatus = await _handler.requestPermissions([Permission.camera]);
      return permissionStatus[Permission.camera] ?? PermissionStatus.denied;
    } else {
      return permission;
    }
  }

  static Future<PermissionStatus> _getMicrophonePermission() async {
    PermissionStatus permission = await _handler.checkPermissionStatus(Permission.microphone);
    if (permission != PermissionStatus.granted) {
      Map<Permission, PermissionStatus> permissionStatus = await _handler.requestPermissions([Permission.microphone]);
      return permissionStatus[Permission.microphone] ?? PermissionStatus.denied;
    } else {
      return permission;
    }
  }

  static void _handleInvalidPermissions(
    PermissionStatus cameraPermissionStatus,
    PermissionStatus microphonePermissionStatus,
  ) {
    if (cameraPermissionStatus == PermissionStatus.denied && microphonePermissionStatus == PermissionStatus.denied) {
      throw new PlatformException(code: "PERMISSION_DENIED", message: "Access to camera and microphone denied", details: null);
    } else if (cameraPermissionStatus == PermissionStatus.restricted && microphonePermissionStatus == PermissionStatus.restricted) {
      throw new PlatformException(code: "PERMISSION_DISABLED", message: "Location data is not available on device", details: null);
    }
  }
}
