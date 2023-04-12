import 'dart:convert';
import 'dart:io';

import 'package:chat/utils/AppConstants.dart';
import 'package:http/http.dart';
import 'package:nb_utils/nb_utils.dart';

class NotificationService {
  Future<void> sendPushNotifications(String title, String content, {String? id, String? image, String? receiverPlayerId}) async {
    log(receiverPlayerId);
    Map req = {
      'headings': {
        'en': title,
      },
      'contents': {
        'en': content,
      },
      'big_picture': image.validate().isNotEmpty ? image.validate() : '',
      'large_icon': image.validate().isNotEmpty ? image.validate() : '',
      'small_icon': mAppIconUrl,
      /*'data': {
        'id': id,
      },*/
      'app_id': mOneSignalAppId,
      'android_channel_id': mOneSignalChannelId,
      'include_player_ids': [receiverPlayerId],
      'android_group': AppName,
/*      c58378a5-a94a-4341-8f7a-24c200e7331b*/
    };
    var header = {
      HttpHeaders.authorizationHeader: 'Basic $mOneSignalRestKey',
      HttpHeaders.contentTypeHeader: 'application/json; charset=utf-8',
    };

    Response res = await post(
      Uri.parse('https://onesignal.com/api/v1/notifications'),
      body: jsonEncode(req),
      headers: header,
    );

    log(res.statusCode);
    log(res.body);

    if (res.statusCode.isSuccessful()) {
    } else {
      throw errorSomethingWentWrong;
    }
  }
}
