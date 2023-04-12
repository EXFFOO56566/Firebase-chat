import 'dart:io';

import 'package:chat/main.dart';
import 'package:chat/screens/DashboardScreen.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppCommon.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:chat/utils/Appwidgets.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_mobx/flutter_mobx.dart';
import 'package:image_crop/image_crop.dart';
import 'package:image_picker/image_picker.dart';
import 'package:nb_utils/nb_utils.dart';

// ignore: must_be_immutable
class SaveProfileScreen extends StatefulWidget {
  bool? mIsShowBack = true;
  bool? mIsFromLogin = false;

  SaveProfileScreen({this.mIsShowBack, this.mIsFromLogin});

  @override
  SaveProfileScreenState createState() => SaveProfileScreenState();
}

class SaveProfileScreenState extends State<SaveProfileScreen> {
  var formKey = GlobalKey<FormState>();
  var cropKey = GlobalKey<CropState>();

  PickedFile? image;
  File? cropImage;

  TextEditingController nameCont = TextEditingController();
  TextEditingController emailCont = TextEditingController();
  TextEditingController mobileNumberCont = TextEditingController();
  TextEditingController statusCont = TextEditingController();

  @override
  void initState() {
    super.initState();
    init();
  }

  Future init() async {
    appStore.setLoading(true);

    await userService.getUser(email: getStringAsync(userEmail)).then((value) {
      nameCont.text = value.name!;
      emailCont.text = value.email!;
      mobileNumberCont.text = value.phoneNumber!;
      statusCont.text = value.userStatus!;
      loginStore.setDisplayName(aDisplayName: value.name);

      loginStore.setPhotoUrl(aPhotoUrl: value.photoUrl.validate());
    }).catchError((error) {
      toast(error.toString());
    }).whenComplete(() {
      appStore.setLoading(false);
    });
  }

  void validate() async {
    hideKeyboard(context);

    if (formKey.currentState!.validate()) {
      appStore.isLoading = true;

      formKey.currentState!.save();
      Map<String, dynamic> data = {
        'name': nameCont.text.trim(),
        'updatedAt': Timestamp.now(),
        'phoneNumber': mobileNumberCont.text.trim(),
        "userStatus": statusCont.text.trim(),
        'caseSearch': setSearchParam(nameCont.text.trim()),
      };

      userService.updateUserInfo(data, getStringAsync(userId), profileImage: image != null ? File(image!.path) : null).then((value) {
        toast('Profile Updated');

        loginStore.setDisplayName(aDisplayName: nameCont.text.trim());
        loginStore.setMobileNumber(aMobileNumber: mobileNumberCont.text.trim());
        loginStore.setStatus(aStatus: statusCont.text.trim().validate());
        if (widget.mIsFromLogin!) {
          setStringAsync(userMobileNumber, mobileNumberCont.text.trim());
          DashboardScreen().launch(context, isNewTask: true);
        } else {
          finish(context);
        }
      }).catchError((e) {
        log(e.toString());
        toast(e.toString());
      }).whenComplete(() {
        appStore.isLoading = false;
      });
    }
  }

  Future getImage() async {
    if (getBoolAsync(isEmailLogin)) {
      image = await ImagePicker().getImage(source: ImageSource.gallery, imageQuality: 100);
      setState(() {});
    }
  }

  Widget profileImage() {
    if (getBoolAsync(isEmailLogin)) {
      if (loginStore.mPhotoUrl.isEmptyOrNull) {
        return Text('No Profile Picture', style: boldTextStyle()).center();
      } else {
        if (image != null) {
          return Image.file(File(image!.path), height: 180, width: 180, fit: BoxFit.cover, alignment: Alignment.center).cornerRadiusWithClipRRect(100);
        } else {
          return cachedImage(loginStore.mPhotoUrl.validate(), height: 180, width: 180, fit: BoxFit.cover, alignment: Alignment.center).cornerRadiusWithClipRRect(100);
        }
      }
    } else {
      if (image != null) {
        return Image.file(File(image!.path), height: 180, width: 180, fit: BoxFit.cover, alignment: Alignment.center).cornerRadiusWithClipRRect(100);
      } else {
        return cachedImage(loginStore.mPhotoUrl.validate(), height: 180, width: 180, fit: BoxFit.cover, alignment: Alignment.center).cornerRadiusWithClipRRect(100);
      }
    }
  }

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        appBar: appBarWidget('save_profile'.translate, textColor: Colors.white, showBack: widget.mIsShowBack!),
        body: Observer(
          builder: (_) {
            return Stack(
              children: [
                Container(
                  child: SingleChildScrollView(
                    child: Form(
                      key: formKey,
                      autovalidateMode: AutovalidateMode.onUserInteraction,
                      child: Column(
                        children: [
                          Container(
                            height: 180,
                            width: 180,
                            decoration: boxDecorationWithShadow(boxShape: BoxShape.circle),
                            child: Stack(
                              children: [
                                profileImage(),
                                getBoolAsync(isEmailLogin)
                                    ? AnimatedPositioned(
                                        bottom: 0,
                                        right: 0,
                                        duration: 2.milliseconds,
                                        child: Container(
                                          height: 60,
                                          width: 60,
                                          decoration: boxDecorationWithShadow(boxShape: BoxShape.circle),
                                          child: IconButton(
                                            icon: Icon(Icons.add_a_photo),
                                            onPressed: () {
                                              getImage();
                                            },
                                          ),
                                        ),
                                      )
                                    : Offstage(),
                              ],
                            ),
                          ),
                          32.height,
                          AppTextField(
                            controller: nameCont,
                            textFieldType: TextFieldType.NAME,
                            enabled: getBoolAsync(isEmailLogin) ? false : true,
                            readOnly: getBoolAsync(isEmailLogin) ? true : false,
                            decoration: inputDecoration(context, labelText: "full_name".translate).copyWith(
                              suffixIcon: Icon(Icons.contact_mail, color: secondaryColor),
                            ),
                          ),
                          16.height,
                          AppTextField(
                            controller: emailCont,
                            textFieldType: TextFieldType.EMAIL,
                            decoration: inputDecoration(context, labelText: "email".translate).copyWith(
                              suffixIcon: Icon(Icons.email_outlined, color: secondaryColor),
                            ),
                            enabled: false,
                            readOnly: true,
                          ),
                          16.height,
                          AppTextField(
                            controller: mobileNumberCont,
                            textFieldType: TextFieldType.PHONE,
                            maxLength: 10,
                            decoration: inputDecoration(context, labelText: "mobile_number".translate).copyWith(
                              suffixIcon: Icon(Icons.phone, color: secondaryColor),
                            ),
                          ),
                          16.height,
                          AppTextField(
                            controller: statusCont,
                            textFieldType: TextFieldType.OTHER,
                            minLines: 1,
                            maxLines: 4,
                            maxLength: 130,
                            keyboardType: TextInputType.multiline,
                            textInputAction: TextInputAction.newline,
                            decoration: inputDecoration(context, labelText: "status".translate).copyWith(
                              suffixIcon: Icon(Icons.star_border, color: secondaryColor),
                            ),
                          ),
                          16.height,
                          AppButton(
                            width: context.width(),
                            color: context.primaryColor,
                            text: "Save",
                            hoverColor: Colors.white,
                            onTap: () {
                              validate();
                            },
                          ),
                        ],
                      ).paddingAll(16),
                    ),
                  ),
                ),
              ],
            ).visible(!appStore.isLoading, defaultWidget: Loader());
          },
        ),
      ),
    );
  }
}
