import 'dart:convert';

Attendance attendanceFromJson(String str) {
  final jsonData = json.decode(str);
  return Attendance.fromMap(jsonData);
}

String attendanceToJson(Attendance data) {
  final dyn = data.toMap();
  return json.encode(dyn);
}

class Attendance {
  int id;
  String date;
  String time;
  String location;
  String type;

  Attendance({this.id, this.date, this.time, this.location, this.type});

  factory Attendance.fromMap(Map<String, dynamic> json) => Attendance(
        id: json["id"],
        date: json["date"],
        time: json["time"],
        location: json["location"],
        type: json["type"],
      );

  Map<String, dynamic> toMap() => {
        "id": id,
        "date": date,
        "time": time,
        "location": location,
        "type": type,
      };
}
