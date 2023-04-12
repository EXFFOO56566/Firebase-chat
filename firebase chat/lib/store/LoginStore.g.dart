// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'LoginStore.dart';

// **************************************************************************
// StoreGenerator
// **************************************************************************

// ignore_for_file: non_constant_identifier_names, unnecessary_brace_in_string_interps, unnecessary_lambdas, prefer_expression_function_bodies, lines_longer_than_80_chars, avoid_as, avoid_annotating_with_dynamic

mixin _$LoginStore on LoginStoreBase, Store {
  final _$mIdAtom = Atom(name: 'LoginStoreBase.mId');

  @override
  String? get mId {
    _$mIdAtom.reportRead();
    return super.mId;
  }

  @override
  set mId(String? value) {
    _$mIdAtom.reportWrite(value, super.mId, () {
      super.mId = value;
    });
  }

  final _$mDisplayNameAtom = Atom(name: 'LoginStoreBase.mDisplayName');

  @override
  String? get mDisplayName {
    _$mDisplayNameAtom.reportRead();
    return super.mDisplayName;
  }

  @override
  set mDisplayName(String? value) {
    _$mDisplayNameAtom.reportWrite(value, super.mDisplayName, () {
      super.mDisplayName = value;
    });
  }

  final _$mEmailAtom = Atom(name: 'LoginStoreBase.mEmail');

  @override
  String? get mEmail {
    _$mEmailAtom.reportRead();
    return super.mEmail;
  }

  @override
  set mEmail(String? value) {
    _$mEmailAtom.reportWrite(value, super.mEmail, () {
      super.mEmail = value;
    });
  }

  final _$mPhotoUrlAtom = Atom(name: 'LoginStoreBase.mPhotoUrl');

  @override
  String? get mPhotoUrl {
    _$mPhotoUrlAtom.reportRead();
    return super.mPhotoUrl;
  }

  @override
  set mPhotoUrl(String? value) {
    _$mPhotoUrlAtom.reportWrite(value, super.mPhotoUrl, () {
      super.mPhotoUrl = value;
    });
  }

  final _$mMobileNumberAtom = Atom(name: 'LoginStoreBase.mMobileNumber');

  @override
  String? get mMobileNumber {
    _$mMobileNumberAtom.reportRead();
    return super.mMobileNumber;
  }

  @override
  set mMobileNumber(String? value) {
    _$mMobileNumberAtom.reportWrite(value, super.mMobileNumber, () {
      super.mMobileNumber = value;
    });
  }

  final _$mStatusAtom = Atom(name: 'LoginStoreBase.mStatus');

  @override
  String? get mStatus {
    _$mStatusAtom.reportRead();
    return super.mStatus;
  }

  @override
  set mStatus(String? value) {
    _$mStatusAtom.reportWrite(value, super.mStatus, () {
      super.mStatus = value;
    });
  }

  final _$mIsEmailLoginAtom = Atom(name: 'LoginStoreBase.mIsEmailLogin');

  @override
  bool? get mIsEmailLogin {
    _$mIsEmailLoginAtom.reportRead();
    return super.mIsEmailLogin;
  }

  @override
  set mIsEmailLogin(bool? value) {
    _$mIsEmailLoginAtom.reportWrite(value, super.mIsEmailLogin, () {
      super.mIsEmailLogin = value;
    });
  }

  final _$LoginStoreBaseActionController =
      ActionController(name: 'LoginStoreBase');

  @override
  void setId({String? aId}) {
    final _$actionInfo = _$LoginStoreBaseActionController.startAction(
        name: 'LoginStoreBase.setId');
    try {
      return super.setId(aId: aId);
    } finally {
      _$LoginStoreBaseActionController.endAction(_$actionInfo);
    }
  }

  @override
  void setDisplayName({String? aDisplayName}) {
    final _$actionInfo = _$LoginStoreBaseActionController.startAction(
        name: 'LoginStoreBase.setDisplayName');
    try {
      return super.setDisplayName(aDisplayName: aDisplayName);
    } finally {
      _$LoginStoreBaseActionController.endAction(_$actionInfo);
    }
  }

  @override
  void setEmail({String? aEmail}) {
    final _$actionInfo = _$LoginStoreBaseActionController.startAction(
        name: 'LoginStoreBase.setEmail');
    try {
      return super.setEmail(aEmail: aEmail);
    } finally {
      _$LoginStoreBaseActionController.endAction(_$actionInfo);
    }
  }

  @override
  void setPhotoUrl({String? aPhotoUrl}) {
    final _$actionInfo = _$LoginStoreBaseActionController.startAction(
        name: 'LoginStoreBase.setPhotoUrl');
    try {
      return super.setPhotoUrl(aPhotoUrl: aPhotoUrl);
    } finally {
      _$LoginStoreBaseActionController.endAction(_$actionInfo);
    }
  }

  @override
  void setMobileNumber({String? aMobileNumber}) {
    final _$actionInfo = _$LoginStoreBaseActionController.startAction(
        name: 'LoginStoreBase.setMobileNumber');
    try {
      return super.setMobileNumber(aMobileNumber: aMobileNumber);
    } finally {
      _$LoginStoreBaseActionController.endAction(_$actionInfo);
    }
  }

  @override
  void setStatus({String? aStatus}) {
    final _$actionInfo = _$LoginStoreBaseActionController.startAction(
        name: 'LoginStoreBase.setStatus');
    try {
      return super.setStatus(aStatus: aStatus);
    } finally {
      _$LoginStoreBaseActionController.endAction(_$actionInfo);
    }
  }

  @override
  void setIsEmailLogin({bool? aIsEmailLogin}) {
    final _$actionInfo = _$LoginStoreBaseActionController.startAction(
        name: 'LoginStoreBase.setIsEmailLogin');
    try {
      return super.setIsEmailLogin(aIsEmailLogin: aIsEmailLogin);
    } finally {
      _$LoginStoreBaseActionController.endAction(_$actionInfo);
    }
  }

  @override
  String toString() {
    return '''
mId: ${mId},
mDisplayName: ${mDisplayName},
mEmail: ${mEmail},
mPhotoUrl: ${mPhotoUrl},
mMobileNumber: ${mMobileNumber},
mStatus: ${mStatus},
mIsEmailLogin: ${mIsEmailLogin}
    ''';
  }
}
