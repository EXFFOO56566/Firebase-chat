import 'package:chat/components/ForgotPasswordDialog.dart';
import 'package:chat/components/SocialLoginWidget.dart';
import 'package:chat/main.dart';
import 'package:chat/screens/DashboardScreen.dart';
import 'package:chat/screens/SaveProfileScreen.dart';
import 'package:chat/screens/SignUpScreen.dart';
import 'package:chat/services/AuthService.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppCommon.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_mobx/flutter_mobx.dart';
import 'package:nb_utils/nb_utils.dart';
import 'package:the_apple_sign_in/the_apple_sign_in.dart';

class SignInScreen extends StatefulWidget {
  @override
  _SignInScreenState createState() => _SignInScreenState();
}

class _SignInScreenState extends State<SignInScreen> {
  var _formKey = GlobalKey<FormState>();

  AuthService authService = AuthService();
  TextEditingController emailCont = TextEditingController();
  TextEditingController passCont = TextEditingController();

  FocusNode emailFocus = FocusNode();
  FocusNode passFocus = FocusNode();

  @override
  void initState() {
    super.initState();
    init();
  }

  init() async {
    if (appStore.isDarkMode) {
      setStatusBarColor(scaffoldSecondaryDark, statusBarBrightness: Brightness.light, statusBarIconBrightness: Brightness.light);
    } else {
      setStatusBarColor(primaryColor);
    }
    if (isIos) {
      TheAppleSignIn.onCredentialRevoked!.listen((_) {
        log("Credentials revoked");
      });
    }
  }

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  void dispose() {
    super.dispose();
    if (appStore.isDarkMode) {
      setStatusBarColor(scaffoldSecondaryDark, statusBarBrightness: Brightness.light, statusBarIconBrightness: Brightness.light);
    } else {
      setStatusBarColor(primaryColor);
    }
  }

  @override
  Widget build(BuildContext context) {
    void loginWithEmail() {
      if (_formKey.currentState!.validate()) {
        _formKey.currentState!.save();
        appStore.setLoading(true);
        authService.signInWithEmailPassword(email: emailCont.text, password: passCont.text).then((value) {
          appStore.setLoading(false);
          appSetting();
          DashboardScreen().launch(context, isNewTask: true);
        }).catchError((e) {
          toast(e.toString());
        }).whenComplete(
          () {
            appStore.setLoading(false);
          },
        );
      }
    }

    return SafeArea(
      child: Scaffold(
        body: Container(
          height: context.height(),
          child: Stack(
            children: [
              Positioned(
                top: -450,
                left: -40,
                right: -40,
                child: Container(
                  height: 700,
                  width: context.width(),
                  decoration: BoxDecoration(shape: BoxShape.circle, color: context.primaryColor),
                ),
              ),
              Positioned(
                top: -440,
                left: -40,
                right: -40,
                child: Container(
                  height: 700,
                  width: context.width(),
                  decoration: BoxDecoration(shape: BoxShape.circle, color: context.primaryColor.withOpacity(0.5)),
                ),
              ),
              Form(
                key: _formKey,
                autovalidateMode: AutovalidateMode.onUserInteraction,
                child: SingleChildScrollView(
                  padding: EdgeInsets.fromLTRB(16, 0, 16, 8),
                  child: Column(
                    mainAxisSize: MainAxisSize.max,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      70.height,
                      Image.asset("assets/app_icon.png", height: 80),
                      8.height,
                      Text(AppName, style: boldTextStyle(size: 18)),
                      30.height,
                      AppTextField(
                        controller: emailCont,
                        nextFocus: passFocus,
                        textFieldType: TextFieldType.EMAIL,
                        decoration: inputDecoration(context, labelText: "email".translate),
                      ),
                      16.height,
                      AppTextField(
                        controller: passCont,
                        focus: passFocus,
                        textFieldType: TextFieldType.PASSWORD,
                        decoration: inputDecoration(context, labelText: "password".translate),
                      ),
                      8.height,
                      Align(
                        alignment: Alignment.centerRight,
                        child: Text('forgot_password'.translate, style: primaryTextStyle(), textAlign: TextAlign.end).paddingSymmetric(vertical: 8, horizontal: 4).onTap(() {
                          return showInDialog(
                            context,
                            child: ForgotPasswordScreen(),
                            contentPadding: EdgeInsets.zero,
                            title: Text("you_forgot_your_password".translate, style: boldTextStyle(size: 20)),
                          );
                          ForgotPasswordScreen().launch(context);
                        }),
                      ),
                      16.height,
                      AppButton(
                        text: 'sign_in'.translate,
                        textStyle: boldTextStyle(color: CupertinoColors.white),
                        color: primaryColor,
                        width: context.width(),
                        onTap: () {
                          loginWithEmail();
                          hideKeyboard(context);
                        },
                      ),
                      16.height,
                      AppButton(
                        text: 'sign_up'.translate,
                        textStyle: boldTextStyle(color: textPrimaryColorGlobal),
                        color: context.cardColor,
                        width: context.width(),
                        onTap: () {
                          SignUpScreen(isOTP: false).launch(context);
                          hideKeyboard(context);
                        },
                      ),
                      26.height,
                      Text('or'.translate, style: secondaryTextStyle(), textAlign: TextAlign.center),
                      Text('login_with'.translate, style: boldTextStyle(), textAlign: TextAlign.center),
                      16.height,
                      SocialLoginWidget(
                        voidCallback: () {
                          appSetting();
                          if (getStringAsync(userMobileNumber) == null || getStringAsync(userMobileNumber).isEmpty) {
                            SaveProfileScreen(mIsShowBack: false, mIsFromLogin: true).launch(context, isNewTask: true);
                          } else {
                            DashboardScreen().launch(context, isNewTask: true);
                          }
                        },
                      ),
                    ],
                  ),
                ),
              ).center(),
              Observer(builder: (_) => Loader().visible(appStore.isLoading)),
            ],
          ),
        ),
      ),
    );
  }
}
