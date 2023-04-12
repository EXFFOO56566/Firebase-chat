import 'dart:async';
import 'dart:convert';

import 'package:attendancewithfingerprint/database/db_helper.dart';
import 'package:attendancewithfingerprint/model/settings.dart';
import 'package:attendancewithfingerprint/utils/strings.dart';
import 'package:barcode_scan/barcode_scan.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:rflutter_alert/rflutter_alert.dart';

import '../utils/utils.dart';

class SettingPage extends StatefulWidget {
  @override
  _SettingPageState createState() => new _SettingPageState();
}

class _SettingPageState extends State<SettingPage> {
  DbHelper dbHelper = DbHelper();
  Utils utils = Utils();
  final GlobalKey<ScaffoldState> _scaffoldKey = new GlobalKey<ScaffoldState>();
  String _barcode = "";
  Settings settings;

  Future scan() async {
    try {
      var barcode = await BarcodeScanner.scan();
      // The value of Qr Code
      // Return the json data
      // We need replaceAll because Json from web use single-quote ({' '}) not double-quote ({" "})
      final newJsonData = barcode.replaceAll("'", '"');
      var data = jsonDecode(newJsonData);

      if (data['url'] != null && data['key'] != null) {
        // Decode the json data form QR
        String getUrl = data['url'];
        String getKey = data['key'];

        // Set the url and key
        settings = Settings(id: 1, url: getUrl, key: getKey);
        // Update the settings
        updateSettings(settings);
      } else {
        utils.showAlertDialog(
            format_barcode_wrong, "Error", AlertType.error, _scaffoldKey, true);
      }
    } on PlatformException catch (e) {
      if (e.code == BarcodeScanner.CameraAccessDenied) {
        _barcode = camera_permission;
        utils.showAlertDialog(
            _barcode, "Warning", AlertType.warning, _scaffoldKey, true);
      } else {
        _barcode = '$barcode_unknown_error $e';
        utils.showAlertDialog(
            _barcode, "Error", AlertType.error, _scaffoldKey, true);
      }
    } catch (e) {
      _barcode = '$barcode_unknown_error : $e';
      print(_barcode);
    }
  }

  // Insert the URL and KEY
  updateSettings(Settings object) async {
    await dbHelper.updateSettings(object);
    goToMainMenu();
  }

  goToMainMenu() {
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _scaffoldKey,
      appBar: AppBar(
        title: Text(setting_title),
      ),
      body: Container(
        margin: EdgeInsets.all(20.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: <Widget>[
            Container(
              width: double.infinity,
              height: 100.0,
              child: Image(
                image: AssetImage('images/logo.png'),
              ),
            ),
            SizedBox(
              height: 40.0,
            ),
            RaisedButton(
              child: Text(button_scan),
              color: Color(0xFFf7c846),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(18.0),
              ),
              textColor: Colors.black,
              onPressed: () => scan(),
            ),
            SizedBox(
              height: 10.0,
            ),
            Text(
              setting_info,
              style: TextStyle(color: Colors.grey, fontSize: 12.0),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}
