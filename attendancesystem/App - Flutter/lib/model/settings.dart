import 'dart:convert';

Settings settingsFromJson(String str) {
  final jsonData = json.decode(str);
  return Settings.fromMap(jsonData);
}

String settingsToJson(Settings data) {
  final dyn = data.toMap();
  return json.encode(dyn);
}

class Settings {
  int id;
  String url;
  String key;

  Settings({this.id, this.url, this.key});

  factory Settings.fromMap(Map<String, dynamic> json) => Settings(
        id: json["id"],
        key: json["key"],
        url: json["url"],
      );

  Map<String, dynamic> toMap() => {
        "id": id,
        "url": url,
        "key": key,
      };
}
