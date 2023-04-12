import 'dart:io';

import 'package:chat/main.dart';
import 'package:chat/models/LogModel.dart';
import 'package:chat/services/localDB/LogInterface.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:nb_utils/nb_utils.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import 'package:sqflite/sqflite.dart';

class SqliteMethods implements LogInterface {
  String databaseName = "";

  static String tableName = "Call_Logs";

  // columns
  static String id = 'log_id';
  static String callerName = 'caller_name';
  static String callerPic = 'caller_pic';
  static String receiverName = 'receiver_name';
  static String receiverPic = 'receiver_pic';
  static String callStatus = 'call_status';
  static String timestamp = 'timestamp';

  static Future<Database>? initInstance() async {
    Directory dir = await getApplicationDocumentsDirectory();
    String path = join(dir.path, getStringAsync(userId));

    var db = await openDatabase(path, version: 1, onCreate: (Database db, int version) async {
      String createTableQuery = "CREATE TABLE $tableName ($id INTEGER PRIMARY KEY, $callerName TEXT, $callerPic TEXT, $receiverName TEXT, $receiverPic TEXT, $callStatus TEXT, $timestamp TEXT)";

      await db.execute(createTableQuery);
      print("table created");
    });
    return db;
  }

  @override
  openDb(dbName) => (databaseName = dbName);

  @override
  init() async {
    Directory dir = await getApplicationDocumentsDirectory();
    String path = join(dir.path, databaseName);
    var db = await openDatabase(path, version: 1, onCreate: _onCreate);
    return db;
  }

  _onCreate(Database db, int version) async {
    String createTableQuery = "CREATE TABLE $tableName ($id INTEGER PRIMARY KEY, $callerName TEXT, $callerPic TEXT, $receiverName TEXT, $receiverPic TEXT, $callStatus TEXT, $timestamp TEXT)";

    await db.execute(createTableQuery);
    print("table created");
  }

  @override
  addLogs(LogModel log) async {
    await localDbInstance!.insert(tableName, log.toJson()).catchError((e) {
      print(e.toString());
    });
  }

  updateLogs(LogModel log) async {
    await localDbInstance!.update(
      tableName,
      log.toJson(),
      where: '$id = ?',
      whereArgs: [log.logId],
    ).catchError((e) {
      print(e.toString());
    });
  }

  @override
  Future<List<LogModel>?> getLogs() async {
    try {
      List<Map<String, dynamic>> maps = await localDbInstance!.rawQuery("SELECT * FROM $tableName ORDER BY $timestamp DESC");
      /*List<Map> maps = await dbClient.query(
        tableName,
        columns: [
          id,
          callerName,
          callerPic,
          receiverName,
          receiverPic,
          callStatus,
          timestamp,
        ],
      );*/
      log(maps);
      List<LogModel> logList = [];

      if (maps.isNotEmpty) {
        for (Map<String, dynamic> map in maps) {
          logList.add(LogModel.fromJson(map));
        }
      }

      log(logList);

      return logList;
    } catch (e) {
      toast(e.toString());
      print(e);
      return null;
    }
  }

  @override
  deleteLogs(int logId) async {
    return await localDbInstance!.delete(tableName, where: '$id = ?', whereArgs: [logId]).catchError((e) {
      print(e.toString());
    });
  }

  @override
  deleteAllLogs() async {
    return await localDbInstance!.delete(tableName).catchError((e) {
      print(e.toString());
    });
  }

  @override
  close() async {
    localDbInstance!.close().catchError((e) {
      print(e.toString());
    });
  }
}
