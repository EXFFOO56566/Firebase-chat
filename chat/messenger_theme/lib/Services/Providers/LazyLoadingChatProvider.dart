//  _________ Group Chat Messages ____________
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:fiberchat/Configs/Dbkeys.dart';
import 'package:fiberchat/Configs/optional_constants.dart';
import 'package:fiberchat/Services/Providers/FirebaseAPIProvider.dart';
import 'package:flutter/foundation.dart';

class FirestoreDataProviderMESSAGESforLAZYLOADINGCHAT extends ChangeNotifier {
  List<DocumentSnapshot> datalistSnapshot = <DocumentSnapshot>[];
  String _errorMessage = '';
  bool _hasNext = true;
  bool _isFetchingData = false;
  String? parentid;
  String get errorMessage => _errorMessage;

  bool get hasNext => _hasNext;

  List get recievedDocs => datalistSnapshot.map((snap) {
        final recievedData = snap.data();

        return recievedData;
      }).toList();

  reset() {
    _hasNext = true;
    datalistSnapshot.clear();
    _isFetchingData = false;
    _errorMessage = '';
    recievedDocs.clear();
    notifyListeners();
  }

  Future fetchNextData(
      String? dataType, Query? refdataa, bool isAfterNewdocCreated) async {
    if (_isFetchingData) return;

    _errorMessage = '';
    _isFetchingData = true;

    try {
      final snap = isAfterNewdocCreated == true
          ? await FirebaseApi.getFirestoreCOLLECTIONData(
              maxChatMessageDocsLoadAtOnceForGroupChatAndBroadcastLazyLoading,
              // startAfter: null,
              refdata: refdataa)
          : await FirebaseApi.getFirestoreCOLLECTIONData(
              maxChatMessageDocsLoadAtOnceForGroupChatAndBroadcastLazyLoading,
              startAfter:
                  datalistSnapshot.isNotEmpty ? datalistSnapshot.last : null,
              refdata: refdataa);
      if (isAfterNewdocCreated == true) {
        datalistSnapshot.clear();
        datalistSnapshot.addAll(snap.docs);
      } else {
        datalistSnapshot.addAll(snap.docs);
      }
      // notifyListeners();
      if (snap.docs.length <
          maxChatMessageDocsLoadAtOnceForGroupChatAndBroadcastLazyLoading) {
        _hasNext = false;
      }
      notifyListeners();
    } catch (error) {
      _errorMessage = error.toString();
      notifyListeners();
    }

    _isFetchingData = false;
  }

  addDoc(DocumentSnapshot newDoc) {
    int index = datalistSnapshot
        .indexWhere((doc) => doc[Dbkeys.timestamp] == newDoc[Dbkeys.timestamp]);
    if (index < 0) {
      // List<DocumentSnapshot> list = datalistSnapshot.reversed.toList();
      datalistSnapshot.insert(0, newDoc);
      // datalistSnapshot = list;
      notifyListeners();
    }
  }

  bool checkIfDocAlreadyExits(
      {required DocumentSnapshot newDoc, int? timestamp}) {
    return timestamp != null
        ? datalistSnapshot.indexWhere(
                (doc) => doc[Dbkeys.timestamp] == newDoc[Dbkeys.timestamp]) >=
            0
        : datalistSnapshot.contains(newDoc);
  }

  int totalDocsLoadedLength() {
    return datalistSnapshot.length;
  }

  updateparticulardocinProvider({
    required DocumentSnapshot updatedDoc,
  }) async {
    int index = datalistSnapshot.indexWhere(
        (doc) => doc[Dbkeys.timestamp] == updatedDoc[Dbkeys.timestamp]);

    datalistSnapshot.removeAt(index);
    datalistSnapshot.insert(index, updatedDoc);
    notifyListeners();
  }

  deleteparticulardocinProvider({required DocumentSnapshot deletedDoc}) async {
    int index = datalistSnapshot.indexWhere(
        (doc) => doc[Dbkeys.timestamp] == deletedDoc[Dbkeys.timestamp]);

    if (index >= 0) {
      datalistSnapshot.removeAt(index);
      notifyListeners();
    }
  }
}
