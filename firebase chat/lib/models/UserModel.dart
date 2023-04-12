import 'package:cloud_firestore/cloud_firestore.dart';

class UserModel {
  String? uid;
  String? name;
  String? email;
  String? photoUrl;
  String? userStatus;
  bool? isPresence;
  List<String>? caseSearch;
  String? phoneNumber;
  Timestamp? createdAt;
  Timestamp? updatedAt;
  int? lastSeen;
  bool? isEmailLogin;
  String? oneSignalPlayerId;

  UserModel({
    this.uid,
    this.name,
    this.email,
    this.photoUrl,
    this.userStatus,
    this.phoneNumber,
    this.createdAt,
    this.updatedAt,
    this.isEmailLogin,
    this.caseSearch,
    this.isPresence,
    this.lastSeen,
    this.oneSignalPlayerId,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) {
    return UserModel(
      uid: json['uid'],
      name: json['name'],
      email: json['email'],
      userStatus: json['userStatus'],
      phoneNumber: json['phoneNumber'],
      photoUrl: json['photoUrl'],
      isEmailLogin: json['isEmailLogin'],
      createdAt: json['createdAt'],
      updatedAt: json['updatedAt'],
      isPresence: json['isPresence'],
      lastSeen: json['lastSeen'],
      oneSignalPlayerId: json['oneSignalPlayerId'],
      caseSearch: json['caseSearch'] != null ? List<String>.from(json['caseSearch']) : [],
    );
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['uid'] = this.uid;
    data['name'] = this.name;
    data['email'] = this.email;
    data['userStatus'] = this.userStatus;
    data['phoneNumber'] = this.phoneNumber;
    data['photoUrl'] = this.photoUrl;
    data['createdAt'] = this.createdAt;
    data['updatedAt'] = this.updatedAt;
    data['isEmailLogin'] = this.isEmailLogin;
    data['caseSearch'] = this.caseSearch;
    data['isPresence'] = this.isPresence;
    data['lastSeen'] = this.lastSeen;
    data['oneSignalPlayerId'] = this.oneSignalPlayerId;

    return data;
  }
}
