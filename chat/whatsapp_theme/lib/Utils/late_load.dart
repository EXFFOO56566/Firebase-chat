import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';

Widget lateLoad(
    {required Widget placeholder,
    required Widget actualwidget,
    int? timeinseconds = 1,
    Future<dynamic>? future}) {
  return FutureBuilder(
      future: future ?? Future.delayed(Duration(seconds: timeinseconds ?? 1)),
      builder: (c, s) => s.connectionState == ConnectionState.done
          ? actualwidget
          : placeholder);
}

// class CustomStreamWidget extends StatelessWidget {
//   final Map<String, dynamic> map;
//   final Widget widget;

//   const CustomStreamWidget({required this.map, required this.widget});

//   @override
//   Widget build(BuildContext context) {
//     return widget;
//   }
// }

Widget streamLoad({
  required Stream<DocumentSnapshot> stream,
  required Widget placeholder,
  required onfetchdone(m),
}) {
  return StreamBuilder<DocumentSnapshot>(
    stream: stream,
    builder: (context, snapshot) {
      if (snapshot.hasData && snapshot.data!.data() != null) {
        return onfetchdone(snapshot.data!.data());
      }
      return placeholder;
    },
  );
}

Widget futureLoadString({
  required Future<String?> future,
  required Widget placeholder,
  required onfetchdone(m),
}) {
  return FutureBuilder<String?>(
    future: future,
    builder: (context, snapshot) {
      if (snapshot.hasData && snapshot.data != null) {
        return onfetchdone(snapshot.data!);
      }
      return placeholder;
    },
  );
}

Widget futureLoad({
  required Future<DocumentSnapshot> future,
  required Widget placeholder,
  required onfetchdone(m),
}) {
  return FutureBuilder<DocumentSnapshot>(
    future: future,
    builder: (context, snapshot) {
      if (snapshot.hasData && snapshot.data!.data() != null) {
        return onfetchdone(snapshot.data!.data());
      }
      return placeholder;
    },
  );
}

Widget futureLoadCollections({
  required Future<QuerySnapshot> future,
  required Widget placeholder,
  required Widget noDataWidget,
  required onfetchdone(m),
}) {
  return FutureBuilder<QuerySnapshot>(
    future: future,
    builder: (context, snapshot) {
      if (snapshot.hasData && snapshot.data != null) {
        if (snapshot.data!.docs.length == 0) {
          return noDataWidget;
        } else {
          return onfetchdone(snapshot.data!.docs);
        }
      }
      return placeholder;
    },
  );
}

Widget streamLoadCollections({
  required Stream<QuerySnapshot> stream,
  required Widget placeholder,
  required Widget noDataWidget,
  required onfetchdone(m),
}) {
  return StreamBuilder<QuerySnapshot>(
    stream: stream,
    builder: (context, snapshot) {
      if (snapshot.hasData && snapshot.data != null) {
        if (snapshot.data!.docs.length == 0) {
          return noDataWidget;
        } else {
          return onfetchdone(snapshot.data!.docs);
        }
      }
      return placeholder;
    },
  );
}
