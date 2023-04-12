//*************   © Copyrighted by Thinkcreative_Technologies. An Exclusive item of Envato market. Make sure you have purchased a Regular License OR Extended license for the Source Code from Envato to use this product. See the License Defination attached with source code. *********************

import 'dart:convert';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:fiberchat/Configs/Dbkeys.dart';
import 'package:fiberchat/Configs/Dbpaths.dart';
import 'package:fiberchat/Configs/optional_constants.dart';
import 'package:fiberchat/Services/Providers/StatusProvider.dart';
import 'package:fiberchat/Services/localization/language_constants.dart';
import 'package:contacts_service/contacts_service.dart';
import 'package:fiberchat/Models/DataModel.dart';
import 'package:fiberchat/Utils/open_settings.dart';
import 'package:fiberchat/Utils/utils.dart';
import 'package:flutter/material.dart';
import 'dart:async';
import 'package:localstorage/localstorage.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AvailableContactsProvider with ChangeNotifier {
  Map<String?, String?>? contactsBookContactList = new Map<String, String>();

  bool searchingcontactsindatabase = true;
  List<JoinedUserModel> previouslyFetchedKEYPhoneInSharedPrefs = [];
  List<JoinedUserModel> alreadyJoinedUsersPhoneNameAsInServer = [];

  List<dynamic> currentUserPhoneNumberVariants = [];

  fetchContacts(BuildContext context, DataModel? model, String currentuserphone,
      SharedPreferences prefs,
      {List<dynamic>? currentuserphoneNumberVariants,
      bool isClearCache = false}) async {
    if (isClearCache == true) {
      await prefs.remove('lastTimeCheckedContactBookSavedCopy');
    }
    if (currentuserphoneNumberVariants != null) {
      currentUserPhoneNumberVariants = currentuserphoneNumberVariants;
    }
    await getContacts(context, model).then((value) async {
      final List<JoinedUserModel> decodedPhoneStrings = prefs
                      .getString('availablePhoneString') ==
                  null ||
              prefs.getString('availablePhoneString') == ''
          ? []
          : JoinedUserModel.decode(prefs.getString('availablePhoneString')!);
      final List<JoinedUserModel> decodedPhoneAndNameStrings =
          prefs.getString('availablePhoneAndNameString') == null ||
                  prefs.getString('availablePhoneAndNameString') == ''
              ? []
              : JoinedUserModel.decode(
                  prefs.getString('availablePhoneAndNameString')!);
      previouslyFetchedKEYPhoneInSharedPrefs = decodedPhoneStrings;
      alreadyJoinedUsersPhoneNameAsInServer = decodedPhoneAndNameStrings;

      await searchAvailableContactsInDb(
        context,
        currentuserphone,
        prefs,
      );

      // notifyListeners();
    });
  }

  setIsLoading(bool val) {
    searchingcontactsindatabase = val;
    notifyListeners();
  }

  Future<Map<String?, String?>> getContacts(
      BuildContext context, DataModel? model,
      {bool refresh = false}) async {
    Completer<Map<String?, String?>> completer =
        new Completer<Map<String?, String?>>();

    LocalStorage storage = LocalStorage(Dbkeys.cachedContacts);

    Map<String?, String?> _cachedContacts = {};

    completer.future.then((c) {
      c.removeWhere((key, val) => _isHidden(key, model));

      this.contactsBookContactList = c;
    });

    Fiberchat.checkAndRequestPermission(Permission.contacts).then((res) {
      if (res) {
        storage.ready.then((ready) async {
          if (ready) {
            String? getNormalizedNumber(String? number) {
              if (number == null) return null;
              return number.replaceAll(new RegExp('[^0-9+]'), '');
            }

            ContactsService.getContacts(withThumbnails: false)
                .then((Iterable<Contact> contacts) async {
              contacts.where((c) => c.phones!.isNotEmpty).forEach((Contact p) {
                if (p.displayName != null && p.phones!.isNotEmpty) {
                  List<String?> numbers = p.phones!
                      .map((number) {
                        String? _phone = getNormalizedNumber(number.value);

                        return _phone;
                      })
                      .toList()
                      .where((s) => s != null)
                      .toList();

                  numbers.forEach((number) {
                    _cachedContacts[number] = p.displayName;
                  });
                }
              });

              completer.complete(_cachedContacts);
            });
          }
          // }
        });
      } else {
        Fiberchat.showRationale(getTranslated(context, 'perm_contact'));
        Navigator.pushReplacement(context,
            new MaterialPageRoute(builder: (context) => OpenSettings()));
      }
    }).catchError((onError) {
      Fiberchat.showRationale('Error occured: $onError');
    });
    notifyListeners();
    return completer.future;
  }

  String? getNormalizedNumber(String number) {
    if (number.isEmpty) {
      return null;
    }

    return number.replaceAll(new RegExp('[^0-9+]'), '');
  }

  _isHidden(String? phoneNo, DataModel? model) {
    Map<String, dynamic> _currentUser = model!.currentUser!;
    return _currentUser[Dbkeys.hidden] != null &&
        _currentUser[Dbkeys.hidden].contains(phoneNo);
  }

  // List<DocumentSnapshot<dynamic>> contactsAvailable = [];
  searchAvailableContactsInDb(
    BuildContext context,
    String currentuserphone,
    SharedPreferences existingPrefs,
  ) async {
    if (existingPrefs.getString('lastTimeCheckedContactBookSavedCopy') ==
        contactsBookContactList.toString()) {
      searchingcontactsindatabase = false;
      if (previouslyFetchedKEYPhoneInSharedPrefs.length == 0 ||
          alreadyJoinedUsersPhoneNameAsInServer.length == 0) {
        final List<JoinedUserModel> decodedPhoneStrings =
            existingPrefs.getString('availablePhoneString') == null ||
                    existingPrefs.getString('availablePhoneString') == ''
                ? []
                : JoinedUserModel.decode(
                    existingPrefs.getString('availablePhoneString')!);
        final List<JoinedUserModel> decodedPhoneAndNameStrings =
            existingPrefs.getString('availablePhoneAndNameString') == null ||
                    existingPrefs.getString('availablePhoneAndNameString') == ''
                ? []
                : JoinedUserModel.decode(
                    existingPrefs.getString('availablePhoneAndNameString')!);
        previouslyFetchedKEYPhoneInSharedPrefs = decodedPhoneStrings;
        alreadyJoinedUsersPhoneNameAsInServer = decodedPhoneAndNameStrings;
      }
      notifyListeners();
      print(
          '11. SKIPPED SEARCHING - AS ${contactsBookContactList!.entries.length} CONTACTS ALREADY CHECKED IN DATABASE, ${alreadyJoinedUsersPhoneNameAsInServer.length} EXISTS');
      final StatusProvider statusProvider =
          Provider.of<StatusProvider>(context, listen: false);
      await statusProvider.searchContactStatus(
          currentuserphone, alreadyJoinedUsersPhoneNameAsInServer);
    } else {
      print(
          '22. STARTED SEARCHING : ${contactsBookContactList!.entries.length} CONTACTS  IN DATABASE');

      contactsBookContactList!.forEach((key, value) async {
        if ((previouslyFetchedKEYPhoneInSharedPrefs
                    .indexWhere((element) => element.phone == key) <
                0) &&
            (!currentUserPhoneNumberVariants.contains(key))) {
          // if (!availableContactslastTime.contains(key)) {
          await FirebaseFirestore.instance
              .collection(DbPaths.collectionusers)
              .where(Dbkeys.phonenumbervariants, arrayContains: key)
              .get()
              .then((docs) async {
            if (docs.docs.length > 0) {
              print('23. FOUND a USER in DATABASE after searching:  $key');

              if (docs.docs[0].data().containsKey(Dbkeys.joinedOn)) {
                if (alreadyJoinedUsersPhoneNameAsInServer.indexWhere(
                            (element) =>
                                element.phone == docs.docs[0][Dbkeys.phone]) <
                        0 &&
                    docs.docs[0][Dbkeys.phone] != currentuserphone) {
                  docs.docs[0]
                      .data()[Dbkeys.phonenumbervariants]
                      .toList()
                      .forEach((phone) async {
                    previouslyFetchedKEYPhoneInSharedPrefs
                        .add(JoinedUserModel(phone: phone ?? ''));
                  });
                  alreadyJoinedUsersPhoneNameAsInServer.add(JoinedUserModel(
                      phone: docs.docs[0].data()[Dbkeys.phone] ?? '',
                      name: value ?? docs.docs[0].data()[Dbkeys.phone]));

                  int i = alreadyJoinedUsersPhoneNameAsInServer.indexWhere(
                      (element) => element.phone == currentuserphone);
                  if (i >= 0) {
                    alreadyJoinedUsersPhoneNameAsInServer..removeAt(i);
                    previouslyFetchedKEYPhoneInSharedPrefs.removeAt(i);
                  }
                }

                if (key == contactsBookContactList!.entries.last.key) {
                  finishLoadingTasks(context, existingPrefs, currentuserphone,
                      "24. SEARCHING STOPPED as users search completed in database.");
                } else {
                  if (alreadyJoinedUsersPhoneNameAsInServer.length == 11) {
                    searchingcontactsindatabase = false;
                    notifyListeners();
                    print(
                        '25. Now it will search in background , ${alreadyJoinedUsersPhoneNameAsInServer.length} CONTACTS searched and found');
                  }
                }
              } else {
                if (key == contactsBookContactList!.entries.last.key) {
                  finishLoadingTasks(context, existingPrefs, currentuserphone,
                      '96. SEARCH COMPLETED , ${alreadyJoinedUsersPhoneNameAsInServer.length}CONTACTS EXISTS IN DATABASE');
                }
              }
            } else {
              if (key == contactsBookContactList!.entries.last.key) {
                finishLoadingTasks(context, existingPrefs, currentuserphone,
                    '97. SEARCH COMPLETED - NO NEED TO SEARCH _ LAST KEY COMPLETED  , ${alreadyJoinedUsersPhoneNameAsInServer.length} CONTACTS EXISTS IN DATABASE');
              } else if (contactsBookContactList!.length == 0) {
                searchingcontactsindatabase = false;
                Fiberchat.toast('Contact Book Empty');
                notifyListeners();
                final StatusProvider statusProvider =
                    Provider.of<StatusProvider>(context, listen: false);
                await statusProvider.searchContactStatus(
                    currentuserphone, alreadyJoinedUsersPhoneNameAsInServer);
              }
            }
          });
          // } else {
          //   if (key == contactsBookContactList!.entries.last.key) {
          //     searchingcontactsindatabase = false;
          //     notifyListeners();
          //     Fiberchat.toast(
          //         '2. SEARCH COMPLETED , ${alreadyJoinedUsersPhoneNameAsInServer.length} CONTACTS EXISTS IN DATABASE');
          //     final StatusProvider statusProvider =
          //         Provider.of<StatusProvider>(context, listen: false);
          //     await statusProvider.searchContactStatus(
          //         currentuserphone, alreadyJoinedUsersPhoneNameAsInServer);
          //   }
          // }
        } else {
          print('NO NEED TO SEARCH $key, ALREADY SEARCHED & EXISTS');
          if (key == contactsBookContactList!.entries.last.key) {
            finishLoadingTasks(context, existingPrefs, currentuserphone,
                '99.. SEARCH COMPLETED - NO NEED TO SEARCH _ LAST KEY COMPLETED  , ${alreadyJoinedUsersPhoneNameAsInServer.length} CONTACTS EXISTS IN DATABASE');
          }
        }
      });
    }
  }

  finishLoadingTasks(BuildContext context, SharedPreferences existingPrefs,
      String currentuserphone, String printStatement) async {
    searchingcontactsindatabase = false;
    final String encodedavailablePhoneString =
        JoinedUserModel.encode(previouslyFetchedKEYPhoneInSharedPrefs);
    await existingPrefs.setString(
        'availablePhoneString', encodedavailablePhoneString);

    final String encodedalreadyJoinedUsersPhoneNameAsInServer =
        JoinedUserModel.encode(alreadyJoinedUsersPhoneNameAsInServer);
    await existingPrefs.setString('availablePhoneAndNameString',
        encodedalreadyJoinedUsersPhoneNameAsInServer);

    await existingPrefs.setString('lastTimeCheckedContactBookSavedCopy',
        contactsBookContactList.toString());

    notifyListeners();

    print(printStatement);
    final StatusProvider statusProvider =
        Provider.of<StatusProvider>(context, listen: false);
    await statusProvider.searchContactStatus(
        currentuserphone, alreadyJoinedUsersPhoneNameAsInServer);
  }

  List<Map<String, dynamic>> storedUserDoc = [];
  List<Map<String, dynamic>> storedUserMappedDoc = [];

  Future<Map<String, dynamic>> getUserDoc(String phone) async {
    if (IsShowUserFullNameAsSavedInYourContacts == true) {
      int presentInd = storedUserDoc.indexWhere(
          (element) => element[Dbkeys.phonenumbervariants].contains(phone));
      if (presentInd >= 0) {
        int savedIndex = alreadyJoinedUsersPhoneNameAsInServer
            .indexWhere((element) => element.phone == phone);
        if (savedIndex >= 0) {
          JoinedUserModel user =
              alreadyJoinedUsersPhoneNameAsInServer[savedIndex];
          Map<String, dynamic> map = storedUserDoc[presentInd];
          map.update(Dbkeys.nickname, (value) => user.name);
          storedUserDoc[presentInd] = map;

          // notifyListeners();
          return map;
        } else {
          return storedUserDoc[presentInd];
        }
      } else {
        var doc = await FirebaseFirestore.instance
            .collection(DbPaths.collectionusers)
            .doc(phone)
            .get();
        int savedIndex = alreadyJoinedUsersPhoneNameAsInServer
            .indexWhere((element) => element.phone == phone);
        if (IsShowUserFullNameAsSavedInYourContacts == true &&
            savedIndex >= 0) {
          JoinedUserModel user =
              alreadyJoinedUsersPhoneNameAsInServer[savedIndex];
          Map<String, dynamic> map = doc.data()!;
          map.update(Dbkeys.nickname, (value) => user.name);
          storedUserDoc.add(map);
          notifyListeners();
          return map;
        } else {
          Map<String, dynamic> map = doc.data()!;
          storedUserDoc.add(map);
          notifyListeners();
          return map;
        }
      }
    } else {
      var doc = await FirebaseFirestore.instance
          .collection(DbPaths.collectionusers)
          .doc(phone)
          .get();
      Map<String, dynamic> map = doc.data()!;
      storedUserDoc.add(map);
      notifyListeners();
      return map;
    }
  }

  updateUserData(String phone) async {
    var doc = await FirebaseFirestore.instance
        .collection(DbPaths.collectionusers)
        .doc(phone)
        .get();
    if (storedUserDoc.indexWhere((element) => element[Dbkeys.phone] == phone) >=
        0) {
      storedUserDoc.removeAt(storedUserDoc
          .indexWhere((element) => element[Dbkeys.phone] == phone));
    } else {
      if (doc.data() != null) {
        storedUserDoc.add(doc.data()!);
      }
    }
    notifyListeners();
  }
}

class JoinedUserModel {
  final String phone;
  final String? name;

  JoinedUserModel({
    required this.phone,
    this.name,
  });

  factory JoinedUserModel.fromJson(Map<String, dynamic> jsonData) {
    return JoinedUserModel(
      phone: jsonData['phone'],
      name: jsonData['name'],
    );
  }

  static Map<String, dynamic> toMap(JoinedUserModel contact) => {
        'phone': contact.phone,
        'name': contact.name,
      };

  static String encode(List<JoinedUserModel> contacts) => json.encode(
        contacts
            .map<Map<String, dynamic>>(
                (contact) => JoinedUserModel.toMap(contact))
            .toList(),
      );

  static List<JoinedUserModel> decode(String contacts) =>
      (json.decode(contacts) as List<dynamic>)
          .map<JoinedUserModel>((item) => JoinedUserModel.fromJson(item))
          .toList();
}
