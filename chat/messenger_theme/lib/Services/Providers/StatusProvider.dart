// //*************   © Copyrighted by Thinkcreative_Technologies. An Exclusive item of Envato market. Make sure you have purchased a Regular License OR Extended license for the Source Code from Envato to use this product. See the License Defination attached with source code. *********************

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:fiberchat/Configs/Dbkeys.dart';
import 'package:fiberchat/Configs/Dbpaths.dart';
import 'package:fiberchat/Configs/optional_constants.dart';
import 'package:fiberchat/Services/Providers/AvailableContactsProvider.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

class StatusProvider with ChangeNotifier {
  List<JoinedUserModel> joinedUserPhoneStringAsInServer = [];
  bool isLoading = false;
  bool searchingcontactsstatus = true;
  List<QueryDocumentSnapshot<dynamic>> contactsStatus = [];
  replaceStatus(int position, QueryDocumentSnapshot<dynamic> doc) {
    contactsStatus[position] = doc;
    notifyListeners();
  }

  setIsLoading(bool val) {
    isLoading = val;
    notifyListeners();
  }

  searchContactStatus(String currentuserphone,
      List<JoinedUserModel> alljoinedUserPhoneStringAsInServer) async {
    joinedUserPhoneStringAsInServer = alljoinedUserPhoneStringAsInServer;
    notifyListeners();
    print(
        'SEARCHING STATUS FOR ${joinedUserPhoneStringAsInServer.length} AVAILABLE CONTACTS');
    if (OnlyPeerWhoAreSavedInmyContactCanMessageOrCallMe == true) {
      await FirebaseFirestore.instance
          .collection(DbPaths.collectionusers)
          .doc(currentuserphone)
          .set({
        Dbkeys.deviceSavedLeads: alljoinedUserPhoneStringAsInServer
            .map((e) => e.phone.toString())
            .toList()
      }, SetOptions(merge: true));
    }

    if (joinedUserPhoneStringAsInServer.length == 0) {
      searchingcontactsstatus = false;
      notifyListeners();
    } else {
      joinedUserPhoneStringAsInServer.forEach((user) async {
        await FirebaseFirestore.instance
            .collection(DbPaths.collectionnstatus)
            .where(Dbkeys.statusPUBLISHERPHONEVARIANTS,
                arrayContains: user.phone.toString())
            .get()
            .then((docs) {
          if (docs.docs.length > 0) {
            if (docs.docs.length > 0 &&
                DateTime.now().isBefore(
                    docs.docs[0].data()[Dbkeys.statusEXPIRININGON].toDate()) &&
                docs.docs[0].data()[Dbkeys.statusPUBLISHERPHONE] !=
                    currentuserphone &&
                contactsStatus.indexWhere((element) =>
                        element.data()[Dbkeys.statusPUBLISHERPHONE] ==
                        docs.docs[0].data()[Dbkeys.statusPUBLISHERPHONE]) <
                    0) {
              contactsStatus.add(docs.docs[0]);

              if (user.phone == joinedUserPhoneStringAsInServer.last.phone) {
                searchingcontactsstatus = false;
                if (contactsStatus.length > 8 && contactsStatus.length < 10) {
                  isLoading = false;
                }
              }
              notifyListeners();
            } else {
              if (user.phone == joinedUserPhoneStringAsInServer.last.phone) {
                searchingcontactsstatus = false;
                notifyListeners();
              }
              // if (docs.docs.length == 0) {
              //   if (contactsStatus.contains(docs.docs[0])) {
              //     contactsStatus.remove(docs.docs[0]);
              //     notifyListeners();
              //   }
              // }
            }
          } else {
            if (user.phone == joinedUserPhoneStringAsInServer.last.phone) {
              searchingcontactsstatus = false;
              notifyListeners();
            }
            if (docs.docs.length == 0) {
              int i = contactsStatus.indexWhere((status) =>
                  status[Dbkeys.statusPUBLISHERPHONEVARIANTS]
                      .contains(user.phone.toString()));
              if (i >= 0) {
                contactsStatus.removeAt(i);
                notifyListeners();
              }
            }
          }
        });
        if (user.phone == joinedUserPhoneStringAsInServer.last.phone) {
          searchingcontactsstatus = false;
          notifyListeners();
        }
      });
    }
  }

  triggerDeleteMyExpiredStatus(String myphone) async {
    await FirebaseFirestore.instance
        .collection(DbPaths.collectionnstatus)
        .doc(myphone)
        .get()
        .then((myStatus) async {
      if (myStatus.exists &&
          (DateTime.now()
              .isAfter(myStatus[Dbkeys.statusEXPIRININGON].toDate()))) {
        myStatus.reference.delete();
        //No need to delete the media data from here as it will be deleted automatically using Cloud functions deployed in Firebase once the .doc is deleted .
      }
    });
  }

  triggerDeleteOtherUsersExpiredStatus(String myphone) async {
    await FirebaseFirestore.instance
        .collection(DbPaths.collectionnstatus)
        .where(Dbkeys.statusEXPIRININGON, isLessThan: DateTime.now())
        .limit(2)
        .get()
        .then((allstatus) async {
      if (allstatus.docs.length > 0) {
        allstatus.docs.forEach((eachStatus) async {
          await eachStatus.reference.delete();
          //No need to delete the media data from here as it will be deleted automatically using Cloud functions deployed in Firebase once the .doc is deleted .
        });
      }
    });

    FirebaseFirestore.instance
        .collection(DbPaths.collectionusers)
        .where(Dbkeys.lastSeen, isEqualTo: true)
        .where(Dbkeys.lastOnline,
            isLessThan: DateTime.now()
                .subtract(Duration(minutes: 10))
                .millisecondsSinceEpoch)
        .limit(10)
        .get()
        .then((allusers) async {
      if (allusers.docs.length > 0) {
        allusers.docs.forEach((eachUser) async {
          if (eachUser[Dbkeys.phone] != myphone) {
            if (eachUser.data().containsKey(Dbkeys.lastOnline)) {
              if (DateTime.now()
                      .difference(DateTime.fromMillisecondsSinceEpoch(
                          eachUser[Dbkeys.lastOnline]))
                      .inMinutes >=
                  10) {
                eachUser.reference.update(
                    {Dbkeys.lastSeen: DateTime.now().millisecondsSinceEpoch});
              }
            } else {
              eachUser.reference.update(
                  {Dbkeys.lastSeen: DateTime.now().millisecondsSinceEpoch});
            }
          }
        });
      }
    });
//----command to set every online user force offline:
    // FirebaseFirestore.instance
    //     .collection(DbPaths.collectionusers)
    //     .where(Dbkeys.lastSeen, isEqualTo: true)
    //     .get()
    //     .then((allusers) async {
    //   if (allusers.docs.length > 0) {
    //     allusers.docs.forEach((eachUser) async {
    //       if (eachUser[Dbkeys.phone] != myphone) {
    //         eachUser.reference.update({
    //           Dbkeys.lastSeen: DateTime.now().millisecondsSinceEpoch,
    //           Dbkeys.lastOnline: DateTime.now().millisecondsSinceEpoch
    //         });
    //       }
    //     });
    //   }
    // });
  }
}

// import 'package:cloud_firestore/cloud_firestore.dart';
// import 'package:fiberchat/Configs/Dbkeys.dart';
// import 'package:fiberchat/Configs/Dbpaths.dart';
// import 'package:fiberchat/Configs/optional_constants.dart';
// import 'package:fiberchat/Services/Providers/AvailableContactsProvider.dart';
// import 'package:fiberchat/Utils/utils.dart';
// import 'package:flutter/foundation.dart';
// import 'package:flutter/material.dart';

// class StatusProvider with ChangeNotifier {
//   List<JoinedUserModel> joinedUserPhoneStringAsInServer = [];
//   bool isLoading = false;
//   bool searchingcontactsstatus = true;
//   List<QueryDocumentSnapshot<dynamic>> contactsStatus = [];

//   replaceStatus(int position, QueryDocumentSnapshot<dynamic> doc) {
//     contactsStatus[position] = doc;
//     notifyListeners();
//   }

//   setIsLoading(bool val) {
//     isLoading = val;
//     notifyListeners();
//   }

//   searchContactStatus(String currentuserphone,
//       List<JoinedUserModel> alljoinedUserPhoneStringAsInServer) async {
//     List<String> joinedUserRegistedPhone = alljoinedUserPhoneStringAsInServer
//         .map((e) => e.phone.toString())
//         .toList();
//     joinedUserRegistedPhone.remove(currentuserphone);
//     joinedUserPhoneStringAsInServer = alljoinedUserPhoneStringAsInServer;
//     notifyListeners();

//     print(
//         'SEARCHING STATUS FOR ${joinedUserRegistedPhone.length} AVAILABLE STRING CONTACTS');

//     if (OnlyPeerWhoAreSavedInmyContactCanMessageOrCallMe == true) {
//       await FirebaseFirestore.instance
//           .collection(DbPaths.collectionusers)
//           .doc(currentuserphone)
//           .set({
//         Dbkeys.deviceSavedLeads: alljoinedUserPhoneStringAsInServer
//             .map((e) => e.phone.toString())
//             .toList()
//       }, SetOptions(merge: true));
//     }

//     if (joinedUserPhoneStringAsInServer.length == 0) {
//       searchingcontactsstatus = false;
//       isLoading = false;
//       notifyListeners();
//     } else {
//       await FirebaseFirestore.instance
//           .collection(DbPaths.collectionnstatus)
//           .where(Dbkeys.statusPUBLISHERPHONEVARIANTS,
//               arrayContainsAny: joinedUserRegistedPhone)
//           .get()
//           .then((docs) {
//         if (docs.docs.length > 0) {
//           contactsStatus = docs.docs;

//           contactsStatus.removeWhere((status) =>
//               (DateTime.now().isAfter(
//                       status.data()[Dbkeys.statusEXPIRININGON].toDate()) ||
//                   status.data()[Dbkeys.statusPUBLISHERPHONE] ==
//                       currentuserphone ||
//                   contactsStatus.indexWhere((element) =>
//                           element.data()[Dbkeys.statusPUBLISHERPHONE] ==
//                           status.data()[Dbkeys.statusPUBLISHERPHONE]) >
//                       0) ==
//               true);

//           searchingcontactsstatus = false;
//           notifyListeners();
//           // docs.docs.forEach((doc) {
//           //   if (DateTime.now()
//           //           .isBefore(doc.data()[Dbkeys.statusEXPIRININGON].toDate()) &&
//           //       doc.data()[Dbkeys.statusPUBLISHERPHONE] != currentuserphone &&
//           //       contactsStatus.indexWhere((element) =>
//           //               element.data()[Dbkeys.statusPUBLISHERPHONE] ==
//           //               doc.data()[Dbkeys.statusPUBLISHERPHONE]) <
//           //           0) {
//           //     contactsStatus.add(doc);

//           //     notifyListeners();
//           //   } else {
//           //     searchingcontactsstatus = false;
//           //     notifyListeners();

//           //     // if (docs.docs.length == 0) {
//           //     //   if (contactsStatus.contains(docs.docs[0])) {
//           //     //     contactsStatus.remove(docs.docs[0]);
//           //     //     notifyListeners();
//           //     //   }
//           //     // }
//           //   }
//           // });
//         } else {
//           // if (user.phone == joinedUserPhoneStringAsInServer.last.phone) {
//           contactsStatus = [];
//           searchingcontactsstatus = false;
//           notifyListeners();
//           // }
//           // if (docs.docs.length == 0) {
//           //   int i = contactsStatus.indexWhere((status) =>
//           //       status[Dbkeys.statusPUBLISHERPHONEVARIANTS]
//           //           .contains(user.phone.toString()));
//           //   if (i >= 0) {
//           //     contactsStatus.removeAt(i);
//           //     notifyListeners();
//           //   }
//           // }
//         }
//       });
//     }
//   }

//   triggerDeleteMyExpiredStatus(String myphone) async {
//     await FirebaseFirestore.instance
//         .collection(DbPaths.collectionnstatus)
//         .doc(myphone)
//         .get()
//         .then((myStatus) async {
//       if (myStatus.exists &&
//           (DateTime.now()
//               .isAfter(myStatus[Dbkeys.statusEXPIRININGON].toDate()))) {
//         myStatus.reference.delete();
//         //No need to delete the media data from here as it will be deleted automatically using Cloud functions deployed in Firebase once the .doc is deleted .
//       }
//     });
//   }

//   triggerDeleteOtherUsersExpiredStatus(String myphone) async {
//     await FirebaseFirestore.instance
//         .collection(DbPaths.collectionnstatus)
//         .where(Dbkeys.statusEXPIRININGON, isLessThan: DateTime.now())
//         .limit(2)
//         .get()
//         .then((allstatus) async {
//       if (allstatus.docs.length > 0) {
//         allstatus.docs.forEach((eachStatus) async {
//           await eachStatus.reference.delete();
//           //No need to delete the media data from here as it will be deleted automatically using Cloud functions deployed in Firebase once the .doc is deleted .
//         });
//       }
//     });

//     FirebaseFirestore.instance
//         .collection(DbPaths.collectionusers)
//         .where(Dbkeys.lastSeen, isEqualTo: true)
//         .where(Dbkeys.lastOnline,
//             isLessThan: DateTime.now()
//                 .subtract(Duration(minutes: 10))
//                 .millisecondsSinceEpoch)
//         .limit(10)
//         .get()
//         .then((allusers) async {
//       if (allusers.docs.length > 0) {
//         allusers.docs.forEach((eachUser) async {
//           if (eachUser[Dbkeys.phone] != myphone) {
//             if (eachUser.data().containsKey(Dbkeys.lastOnline)) {
//               if (DateTime.now()
//                       .difference(DateTime.fromMillisecondsSinceEpoch(
//                           eachUser[Dbkeys.lastOnline]))
//                       .inMinutes >=
//                   10) {
//                 eachUser.reference.update(
//                     {Dbkeys.lastSeen: DateTime.now().millisecondsSinceEpoch});
//               }
//             } else {
//               eachUser.reference.update(
//                   {Dbkeys.lastSeen: DateTime.now().millisecondsSinceEpoch});
//             }
//           }
//         });
//       }
//     });
// //----command to set every online user force offline:
//     // FirebaseFirestore.instance
//     //     .collection(DbPaths.collectionusers)
//     //     .where(Dbkeys.lastSeen, isEqualTo: true)
//     //     .get()
//     //     .then((allusers) async {
//     //   if (allusers.docs.length > 0) {
//     //     allusers.docs.forEach((eachUser) async {
//     //       if (eachUser[Dbkeys.phone] != myphone) {
//     //         eachUser.reference.update({
//     //           Dbkeys.lastSeen: DateTime.now().millisecondsSinceEpoch,
//     //           Dbkeys.lastOnline: DateTime.now().millisecondsSinceEpoch
//     //         });
//     //       }
//     //     });
//     //   }
//     // });
//   }
// }
