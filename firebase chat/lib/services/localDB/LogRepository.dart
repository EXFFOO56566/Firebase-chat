import 'package:chat/models/LogModel.dart';
import 'package:chat/services/localDB/SqliteMethods.dart';
import 'package:meta/meta.dart';

class LogRepository {
  static late var dbObject;

  static init({required String dbName}) {
    dbObject = SqliteMethods();
    dbObject.openDb(dbName);
    dbObject.init();
  }

  static addLogs(LogModel log) => dbObject.addLogs(log);

  static deleteLogs(int? logId) => dbObject.deleteLogs(logId);

  static deleteAllLogs() => dbObject.deleteAllLogs();

  static getLogs() => dbObject.getLogs();

  static close() => dbObject.close();
}
