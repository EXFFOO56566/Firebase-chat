import 'package:chat/models/LogModel.dart';
import 'package:chat/services/BaseService.dart';
import 'package:cloud_firestore/cloud_firestore.dart';

class CallLogService extends BaseService {
  FirebaseFirestore firestore = FirebaseFirestore.instance;

  CallLogService({String? userId}) {
    ref = firestore.collection('users').doc(userId).collection("calllogs");
  }

  Stream<List<LogModel>> getCallLogs() {
    return ref!.orderBy("timestamp", descending: true).snapshots().map((event) => event.docs.map((e) => LogModel.fromJson(e.data() as Map<String, dynamic>)).toList());
  }
}
