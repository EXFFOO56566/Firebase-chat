import 'package:mobx/mobx.dart';

part 'LoginStore.g.dart';

class LoginStore = LoginStoreBase with _$LoginStore;

abstract class LoginStoreBase with Store {
  @observable
  String? mId;

  @observable
  String? mDisplayName;

  @observable
  String? mEmail;

  @observable
  String? mPhotoUrl;

  @observable
  String? mMobileNumber;

  @observable
  String? mStatus;

  @observable
  bool? mIsEmailLogin;

  @action
  void setId({String? aId}) => mId = aId;

  @action
  void setDisplayName({String? aDisplayName}) => mDisplayName = aDisplayName;

  @action
  void setEmail({String? aEmail}) => mEmail = aEmail;

  @action
  void setPhotoUrl({String? aPhotoUrl}) => mPhotoUrl = aPhotoUrl;

  @action
  void setMobileNumber({String? aMobileNumber}) => mMobileNumber = aMobileNumber;

  @action
  void setStatus({String? aStatus}) => mStatus = aStatus;

  @action
  void setIsEmailLogin({bool? aIsEmailLogin}) => mIsEmailLogin = aIsEmailLogin;
}
