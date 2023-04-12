import 'package:attendancewithfingerprint/utils/strings.dart';
import 'package:flutter/material.dart';
import 'package:rflutter_alert/rflutter_alert.dart';

class Utils {
  var alertStyle = AlertStyle(
    animationType: AnimationType.fromTop,
    isCloseButton: false,
    isOverlayTapDismiss: true,
    descStyle: TextStyle(fontSize: 18.0),
    animationDuration: Duration(milliseconds: 400),
    alertBorder: RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(0.0),
      side: BorderSide(
        color: Colors.grey,
      ),
    ),
    titleStyle: TextStyle(
      color: Colors.red,
    ),
  );

  // Show snack bar
  void showAlertDialog(String message, String title, AlertType alertType,
      GlobalKey<ScaffoldState> _scaffoldKey, bool isAnyButton) {
    if (isAnyButton == true) {
      Alert(
        context: _scaffoldKey.currentContext,
        style: alertStyle,
        type: alertType,
        title: title,
        desc: message,
        buttons: [
          DialogButton(
            child: Text(
              ok_text,
              style: TextStyle(color: Colors.white, fontSize: 20),
            ),
            onPressed: () => Navigator.pop(_scaffoldKey.currentContext),
            width: 120,
          )
        ],
      ).show();
    } else {
      Alert(
        context: _scaffoldKey.currentContext,
        style: alertStyle,
        type: alertType,
        title: title,
        desc: message,
        buttons: [],
      ).show();
    }
  }

  // Get the url, and this function will check if the last is any slash (/) or not
  getRealUrl(String url, String path) {
    var realUrl;
    var count = url.length - 1;
    var getLast = url[count];
    if (getLast == '/') {
      realUrl = url + '' + path;
    } else {
      realUrl = url + '/' + path;
    }
    return realUrl;
  }
}
