import 'package:chat/main.dart';
import 'package:chat/models/CallModel.dart';
import 'package:chat/screens/PickUpScreen.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:nb_utils/nb_utils.dart';

class PickupLayout extends StatelessWidget {
  final Widget? child;

  PickupLayout({this.child});

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<DocumentSnapshot>(
      stream: callService.callStream(uid: getStringAsync(userId)),
      builder: (context, snap) {
        if (snap.hasData && snap.data!.data() != null) {
          CallModel call = CallModel.fromJson(snap.data!.data() as Map<String, dynamic>);

          if (!call.hasDialed!) {
            return PickUpScreen(callModel: call);
          } else
            return child!;
        }
        return child!;
      },
    );
  }
}
