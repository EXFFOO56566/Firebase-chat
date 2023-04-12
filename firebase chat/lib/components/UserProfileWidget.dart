import 'package:chat/main.dart';
import 'package:chat/screens/PickupLayout.dart';
import 'package:chat/screens/SaveProfileScreen.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:flutter/material.dart';
import 'package:flutter_mobx/flutter_mobx.dart';
import 'package:nb_utils/nb_utils.dart';

class UserProfileWidget extends StatelessWidget {
  const UserProfileWidget({
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return PickupLayout(
      child: Observer(
        builder: (_) {
          return Container(
            child: InkWell(
              onTap: () {
                SaveProfileScreen(mIsShowBack: true, mIsFromLogin: false).launch(context);
              },
              child: Row(
                children: [
                  loginStore.mPhotoUrl.validate().isNotEmpty
                      ? Hero(
                          tag: "profile_image",
                          child: CircleAvatar(
                            radius: 32.0,
                            backgroundImage: Image.network(loginStore.mPhotoUrl.validate()).image,
                          ),
                        )
                      : Hero(
                          tag: "profile_image",
                          child: CircleAvatar(
                            radius: 32.0,
                            child: Text(
                              loginStore.mDisplayName.validate()[0],
                              style: primaryTextStyle(size: 24, color: Colors.white),
                            ),
                          ),
                        ),
                  10.width,
                  Column(
                    mainAxisAlignment: MainAxisAlignment.start,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(loginStore.mDisplayName.validate(), style: boldTextStyle()),
                      4.height,
                      Text(
                        loginStore.mStatus.validate(),
                        style: primaryTextStyle(),
                        maxLines: 3,
                      ),
                    ],
                  ).expand(),
                  IconButton(
                    icon: Icon(Icons.qr_code_scanner),
                    onPressed: () {
                      toast(COMING_SOON);
                    },
                  )
                ],
              ).paddingAll(16),
            ),
          );
        },
      ),
    );
  }
}
