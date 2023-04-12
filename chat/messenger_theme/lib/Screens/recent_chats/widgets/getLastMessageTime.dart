import 'package:fiberchat/Screens/status/components/formatStatusTime.dart';
import 'package:fiberchat/Services/Providers/Observer.dart';
import 'package:fiberchat/Services/localization/language_constants.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';

final DateFormat formatter = DateFormat('dd/MM/yy');

getLastMessageTime(BuildContext context, String currentUserNo, val) {
  final observer = Provider.of<Observer>(context, listen: false);
  if (val is bool && val == true) {
    return getTranslated(context, 'online');
  } else if (val is int) {
    DateTime now = DateTime.now();
    DateTime date = DateTime.fromMillisecondsSinceEpoch(val);
    String at = observer.is24hrsTimeformat == false
            ? DateFormat.jm().format(date)
            : DateFormat('HH:mm').format(date),
        when = date.day == now.subtract(Duration(days: 1)).day
            ? getTranslated(context, 'yesterday')
            : getWhen(date, context);

    return date.day == now.day ? '$at' : '$when';

    // DateTime date = DateTime.fromMillisecondsSinceEpoch(val);
    // DateTime now = DateTime.now();

    // String at = observer.is24hrsTimeformat == false
    //         ? DateFormat.jm().format(date)
    //         : DateFormat('HH:mm').format(date),
    //     when = date.day == now.subtract(Duration(days: 1)).day
    //         ? getTranslated(context, 'yesterday')
    //         : getJoinTime(val, context);
    // return date.day == now.day ? '$at' : '$when';
  } else if (val is String) {
    if (val == currentUserNo) return getTranslated(context, 'typing');
    return getTranslated(context, 'online');
  }
  return "";
}
