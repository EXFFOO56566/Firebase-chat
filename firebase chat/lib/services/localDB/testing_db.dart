import 'dart:io';

import 'package:chat/utils/AppConstants.dart';
import 'package:flutter/services.dart';
import 'package:nb_utils/nb_utils.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import 'package:sqflite/sqflite.dart';

class example {
  static Future databaseConnect() async {
    Directory dir = await getApplicationDocumentsDirectory();
    String path = join(dir.path, getStringAsync(userId));

    /// It is used to get the current version number
    List<String> files = [];
    String query1 = "CREATE TABLE Company (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)";
    var db = await openDatabase(
      path,
      version: files.length + 1,
      onCreate: (db, version) async {
        await db.execute(query1);
        log('Table created');
      },
      onUpgrade: (db, oldVersion, newVersion) async {
        Future.forEach(files, (element) async {
          String file = await rootBundle.loadString('assets/from${oldVersion}to$newVersion.txt');

          await Future.forEach(file.split(';'), (String element) => db.execute(element));
        });
      },
    );
  }
}
