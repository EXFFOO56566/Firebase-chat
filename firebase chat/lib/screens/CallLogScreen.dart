import 'package:chat/components/Permissions.dart';
import 'package:chat/models/LogModel.dart';
import 'package:chat/models/UserModel.dart';
import 'package:chat/services/localDB/LogRepository.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppCommon.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:chat/utils/Appwidgets.dart';
import 'package:chat/utils/CallFunctions.dart';
import 'package:flutter/material.dart';
import 'package:nb_utils/nb_utils.dart';

class CallLogScreen extends StatefulWidget {
  @override
  _CallLogScreenState createState() => _CallLogScreenState();
}

class _CallLogScreenState extends State<CallLogScreen> {
  @override
  void initState() {
    super.initState();
    init();
  }

  init() async {}

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: FutureBuilder<List<LogModel>?>(
        future: LogRepository?.getLogs(),
        builder: (context, snap) {
          if (snap.hasData) {
            if (snap.data!.length == 0) {
              return NoCallLogsWidget();
            }
            return ListView.separated(
              shrinkWrap: true,
              padding: EdgeInsets.only(top: 0, bottom: 80),
              itemCount: snap.data!.length,
              itemBuilder: (context, index) {
                LogModel data = snap.data![index];

                bool hasDialled = data.callStatus == CALLED_STATUS_DIALLED;

                return InkWell(
                  onLongPress: () async {
                    bool? res = await showConfirmDialog(context, 'log_confirmation'.translate);
                    if (res ?? false) {
                      LogRepository.deleteLogs(data.logId);
                      setState(() {});
                    }
                  },
                  child: Container(
                    padding: EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    child: Row(
                      children: [
                        cachedImage(hasDialled ? data.receiverPic.validate() : data.callerPic.validate(), height: 55, width: 55, fit: BoxFit.cover).cornerRadiusWithClipRRect(30).onTap(() {
                          //
                        }),
                        15.width,
                        Column(
                          mainAxisAlignment: MainAxisAlignment.start,
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(hasDialled ? data.receiverName.validate() : data.callerName.validate(), style: boldTextStyle()),
                            5.height,
                            Row(
                              children: [
                                getCallStatusIcon(data.callStatus),
                                5.width,
                                Text('${formatDateString(data.timestamp!)}', style: primaryTextStyle()),
                              ],
                            ),
                          ],
                        ).expand(),
                        data.callType.validate(value: 'Video') == "voice"
                            ? IconButton(
                                icon: Icon(Icons.phone, color: secondaryColor),
                                onPressed: () async {
                                  UserModel receiverData = UserModel(
                                    name: data.receiverName,
                                    photoUrl: data.receiverPic,
                                  );
                                  UserModel sender = UserModel(
                                    name: getStringAsync(userDisplayName),
                                    photoUrl: getStringAsync(userPhotoUrl),
                                    uid: getStringAsync(userId),
                                    oneSignalPlayerId: getStringAsync(playerId),
                                  );
                                  return await Permissions.cameraAndMicrophonePermissionsGranted()
                                      ? CallFunctions.dial(
                                          context: context,
                                          from: sender,
                                          to: receiverData,
                                        )
                                      : {};
                                },
                              )
                            : IconButton(
                                icon: Icon(Icons.videocam_rounded, color: secondaryColor),
                                onPressed: () async {
                                  UserModel receiverData = UserModel(
                                    name: data.receiverName,
                                    photoUrl: data.receiverPic,
                                  );
                                  UserModel sender = UserModel(
                                    name: getStringAsync(userDisplayName),
                                    photoUrl: getStringAsync(userPhotoUrl),
                                    uid: getStringAsync(userId),
                                    oneSignalPlayerId: getStringAsync(playerId),
                                  );
                                  return await Permissions.cameraAndMicrophonePermissionsGranted()
                                      ? CallFunctions.dial(
                                          context: context,
                                          from: sender,
                                          to: receiverData,
                                        )
                                      : {};
                                },
                              ),
                      ],
                    ),
                  ),
                );
              },
              separatorBuilder: (BuildContext context, int index) {
                return Divider(indent: 80, height: 0);
              },
            );
          }
          return snapWidgetHelper(snap);
        },
      ),
    );
  }

  static String formatDateString(String dateString) {
    DateTime dateTime = DateTime.parse(dateString);

    return dateTime.timeAgo;
  }
}
