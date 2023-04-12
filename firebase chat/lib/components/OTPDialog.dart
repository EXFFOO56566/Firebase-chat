import 'package:chat/screens/DashboardScreen.dart';
import 'package:chat/screens/SignUpScreen.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppCommon.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:country_code_picker/country_code_picker.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter_mobx/flutter_mobx.dart';
import 'package:nb_utils/nb_utils.dart';
import 'package:otp_text_field/otp_field.dart';
import 'package:otp_text_field/style.dart';

import '../main.dart';

class OTPDialog extends StatefulWidget {
  final String? verificationId;
  final String? phoneNumber;
  final bool? isCodeSent;
  final PhoneAuthCredential? credential;

  OTPDialog({this.verificationId, this.isCodeSent, this.phoneNumber, this.credential});

  @override
  OTPDialogState createState() => OTPDialogState();
}

class OTPDialogState extends State<OTPDialog> {
  bool isLoading = false;

  TextEditingController numberController = TextEditingController();

  String? countryCode = '';

  String otpCode = '';

  @override
  void initState() {
    super.initState();
    init();
  }

  Future<void> init() async {
    //
  }

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  Future<void> submit() async {
    appStore.setLoading(true);
    AuthCredential credential = PhoneAuthProvider.credential(verificationId: widget.verificationId!, smsCode: otpCode.validate());

    await FirebaseAuth.instance.signInWithCredential(credential).then((result) async {
      User currentUser = result.user!;

      ///Return user data
      await userService.userByMobileNumber(currentUser.phoneNumber).then((user) async {
        await authService.updateUserData(user);
        await authService.setUserDetailPreference(user);
        DashboardScreen().launch(context, isNewTask: true);
      }).catchError((e) {
        log(e);
        if (e.toString() == EXCEPTION_NO_USER_FOUND) {
          SignUpScreen(user: currentUser, isOTP: true).launch(context).then((value) {
            log(value);
          });
        } else {
          throw e;
        }
      });
    }).catchError((e) {
      log(e);
      toast(e.toString());

      appStore.setLoading(false);
    });
  }

  Future<void> sendOTP() async {
    if (numberController.text.trim().isEmpty) {
      return toast(errorThisFieldRequired);
    }
    appStore.setLoading(true);

    String number = '+$countryCode${numberController.text.trim()}';
    if (!number.startsWith('+')) {
      number = '+$countryCode${numberController.text.trim()}';
    }

    await authService.loginWithOTP(context, number).then((value) {
      //
    }).catchError((e) {
      toast(e.toString());
    });

    appStore.setLoading(false);
  }

  @override
  Widget build(BuildContext context) {
    return Observer(
      builder: (_) => Container(
        width: context.width(),
        child: !widget.isCodeSent.validate()
            ? Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text("enter_your_phone".translate, style: boldTextStyle()),
                  30.height,
                  Container(
                    height: 100,
                    child: Row(
                      children: [
                        CountryCodePicker(
                          initialSelection: 'IN',
                          showCountryOnly: false,
                          showFlag: false,
                          showFlagDialog: true,
                          showOnlyCountryWhenClosed: false,
                          alignLeft: false,
                          showDropDownButton: true,
                          dialogBackgroundColor: context.cardColor,
                          textStyle: primaryTextStyle(),
                          onInit: (c) {
                            countryCode = c!.dialCode;
                          },
                          onChanged: (c) {
                            countryCode = c.dialCode;
                          },
                        ),
                        AppTextField(
                          controller: numberController,
                          textFieldType: TextFieldType.PHONE,
                          decoration: inputDecoration(context, labelText: "mobile_number".translate),
                          autoFocus: true,
                          onFieldSubmitted: (s) {
                            sendOTP();
                          },
                        ).expand(),
                      ],
                    ),
                  ),
                  30.height,
                  Stack(
                    alignment: Alignment.center,
                    children: [
                      AppButton(
                        onTap: () {
                          sendOTP();
                        },
                        text: 'send_otp'.translate,
                        color: appStore.isDarkMode ? scaffoldSecondaryDark : primaryColor,
                        textStyle: boldTextStyle(color: white),
                        width: context.width(),
                      ),
                      Positioned(
                        //right: 16,
                        child: Loader().visible(!appStore.isLoading),
                      ),
                    ],
                  )
                ],
              )
            : Column(
                mainAxisSize: MainAxisSize.min,
                //crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text("enter_otp_received".translate, style: boldTextStyle()),
                  30.height,
                  OTPTextField(
                    length: 6,
                    width: MediaQuery.of(context).size.width,
                    fieldWidth: 35,
                    style: primaryTextStyle(),
                    textFieldAlignment: MainAxisAlignment.spaceAround,
                    fieldStyle: FieldStyle.box,
                    onChanged: (s) {
                      otpCode = s;
                    },
                    onCompleted: (pin) {
                      otpCode = pin;
                      submit();
                    },
                  ),
                  30.height,
                  Stack(
                    alignment: Alignment.center,
                    children: [
                      AppButton(
                        onTap: () {
                          submit();
                        },
                        text: "confirm".translate,
                        color: appStore.isDarkMode ? scaffoldSecondaryDark : primaryColor,
                        textStyle: boldTextStyle(color: white),
                        width: context.width(),
                      ),
                      Positioned(
                        //right: 16,
                        child: Loader().visible(appStore.isLoading),
                      ),
                    ],
                  )
                ],
              ),
      ),
    );
  }
}
