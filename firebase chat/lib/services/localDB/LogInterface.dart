import 'package:chat/models/LogModel.dart';

abstract class LogInterface {
  openDb(dbName);

  init();

  addLogs(LogModel log);

  // returns a list of logs
  Future<List<LogModel>?> getLogs();

  deleteLogs(int logId);

  close();
}
