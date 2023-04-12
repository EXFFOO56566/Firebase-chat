import 'package:mobx/mobx.dart';

part 'AppSettingStore.g.dart';

class AppSettingStore = AppSettingStoreBase with _$AppSettingStore;

abstract class AppSettingStoreBase with Store {
  @observable
  int? mFontSize = -1;

  @observable
  bool? mEnterKey = false;

  @action
  void setFontSize({int? aFontSize}) => mFontSize = aFontSize;

  @action
  void setEnterKey({bool? aEnterKey}) => mEnterKey = aEnterKey;
}
