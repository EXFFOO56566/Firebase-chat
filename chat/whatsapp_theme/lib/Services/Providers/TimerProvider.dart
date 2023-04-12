//*************   © Copyrighted by Thinkcreative_Technologies. An Exclusive item of Envato market. Make sure you have purchased a Regular License OR Extended license for the Source Code from Envato to use this product. See the License Defination attached with source code. *********************

import 'dart:async';

import 'package:fiberchat/Configs/optional_constants.dart';
import 'package:flutter/foundation.dart';

class TimerProvider with ChangeNotifier {
  bool wait = false;
  int start = timeOutSeconds;
  bool isActionBarShow = false;
  startTimer() {
    const onsec = Duration(seconds: 1);
    // ignore: unused_local_variable
    Timer _timer = Timer.periodic(onsec, (timer) {
      if (start == 0) {
        timer.cancel();
        wait = false;
        isActionBarShow = true;
        notifyListeners();
      } else {
        start--;
        wait = true;
        notifyListeners();
      }
    });
  }

  resetTimer() {
    start = timeOutSeconds;
    isActionBarShow = false;
    notifyListeners();
  }
}
