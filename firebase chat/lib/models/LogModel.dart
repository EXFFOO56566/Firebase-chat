class LogModel {
  int? logId;
  String? callerName;
  String? callerPic;
  String? receiverName;
  String? receiverPic;
  String? callStatus;
  String? callType;
  String? timestamp;

  LogModel({
    this.logId,
    this.callerName,
    this.callerPic,
    this.receiverName,
    this.receiverPic,
    this.callStatus,
    this.timestamp,
  });
  factory LogModel.fromJson(Map<String, dynamic> json) {
    return LogModel(
      logId: json['log_id'],
      callerName: json['caller_name'],
      callerPic: json['caller_pic'],
      receiverName: json['receiver_name'],
      receiverPic: json['receiver_pic'],
      callStatus: json['call_status'],
      timestamp: json['timestamp'],
    );
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['log_id'] = this.logId;
    data['caller_name'] = this.callerName;
    data['caller_pic'] = this.callerPic;
    data['receiver_name'] = this.receiverName;
    data['receiver_pic'] = this.receiverPic;
    data['call_status'] = this.callStatus;
    data['timestamp'] = this.timestamp;

    return data;
  }
}
// to map
