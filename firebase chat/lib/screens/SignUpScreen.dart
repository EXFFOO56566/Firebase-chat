import 'package:chat/main.dart';
import 'package:chat/models/UserModel.dart';
import 'package:chat/screens/DashboardScreen.dart';
import 'package:chat/services/AuthService.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppCommon.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter_mobx/flutter_mobx.dart';
import 'package:nb_utils/nb_utils.dart';

class SignUpScreen extends StatefulWidget {
  final bool? isOTP;
  final User? user;

  SignUpScreen({this.isOTP, this.user});

  @override
  _SignUpScreenState createState() => _SignUpScreenState();
}

class _SignUpScreenState extends State<SignUpScreen> {
  var _formKey = GlobalKey<FormState>();
  AuthService authService = AuthService();

  TextEditingController nameCont = TextEditingController();
  TextEditingController emailCont = TextEditingController();
  TextEditingController passCont = TextEditingController();
  TextEditingController confirmPassCont = TextEditingController();
  TextEditingController mobileNumberCont = TextEditingController();

  FocusNode nameFocus = FocusNode();
  FocusNode emailFocus = FocusNode();
  FocusNode passFocus = FocusNode();
  FocusNode confirmPasswordFocus = FocusNode();
  FocusNode mobileNumberFocus = FocusNode();

  String photo = '';

  FocusNode workAddressFocus = FocusNode();

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

    if (widget.isOTP!) {
      appStore.setLoading(false);
      mobileNumberCont.text = widget.user!.phoneNumber!;
    }
  }

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  void dispose() {
    if (appStore.isDarkMode) {
      setStatusBarColor(scaffoldSecondaryDark, statusBarBrightness: Brightness.light, statusBarIconBrightness: Brightness.light);
    } else {
      setStatusBarColor(primaryColor);
    }
    super.dispose();
  }

  void signUpWithEmail() {
    if (_formKey.currentState!.validate()) {
      _formKey.currentState!.save();
      appStore.setLoading(true);
      authService
          .signUpWithEmailPassword(
        name: nameCont.text.trim(),
        email: emailCont.text.trim(),
        password: passCont.text.trim(),
        mobileNumber: mobileNumberCont.text.trim(),
      )
          .then((value) {
        DashboardScreen().launch(context, isNewTask: true);
      }).catchError((e) {
        toast(e.toString());
      }).whenComplete(() {
        appStore.setLoading(false);
      });
    }
  }

  void signUpWithOtp() async {
    if (_formKey.currentState!.validate()) {
      _formKey.currentState!.save();
      appStore.setLoading(true);
      if (await userService.isUserExist(emailCont.text)) {
        toast("user already register with email");
        appStore.setLoading(false);
      } else {
        UserModel userModel = UserModel();
        userModel.uid = widget.user!.uid.validate();
        userModel.email = emailCont.text.validate();
        userModel.name = nameCont.text.validate();
        userModel.phoneNumber = widget.user!.phoneNumber.validate();
        userModel.photoUrl = widget.user!.photoURL.validate();
        userModel.createdAt = Timestamp.now();
        userModel.updatedAt = Timestamp.now();
        userModel.isEmailLogin = true;
        userModel.isPresence = true;
        userModel.userStatus = "Hey there! i am using MightyChat";
        userModel.lastSeen = DateTime.now().millisecondsSinceEpoch;
        userModel.caseSearch = setSearchParam(nameCont.text);

        userModel.oneSignalPlayerId = getStringAsync(playerId);
        await userService.addDocumentWithCustomId(widget.user!.uid, userModel.toJson()).then((value) async {
          UserModel user = await value.get().then((value) => UserModel.fromJson(value.data() as Map<String, dynamic>));
          await authService.updateUserData(user);
          await authService.setUserDetailPreference(user);
          DashboardScreen().launch(context, isNewTask: true);
        }).catchError((e) {
          throw e;
        }).whenComplete(() => appStore.setLoading(false));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        appBar: appBarWidget('sign_up'.translate, textColor: Colors.white),
        body: Observer(builder: (_) {
          return body().visible(!appStore.isLoading, defaultWidget: Loader());
        }),
      ),
    );
  }

  Widget body() {
    return Container(
      padding: EdgeInsets.fromLTRB(8, 0, 8, 0),
      child: SingleChildScrollView(
        padding: EdgeInsets.all(8),
        child: Form(
          key: _formKey,
          autovalidateMode: AutovalidateMode.onUserInteraction,
          child: Column(
            children: [
              Image.asset("assets/app_icon.png", height: 100),
              8.height,
              Text(AppName, style: boldTextStyle()),
              8.height,
              Text('sign_up_to_continue'.translate, style: secondaryTextStyle()),
              50.height,
              AppTextField(
                focus: nameFocus,
                controller: nameCont,
                nextFocus: emailFocus,
                textFieldType: TextFieldType.NAME,
                decoration: inputDecoration(context, labelText: "name".translate),
              ),
              16.height,
              AppTextField(
                focus: emailFocus,
                controller: emailCont,
                nextFocus: passFocus,
                textFieldType: TextFieldType.EMAIL,
                decoration: inputDecoration(context, labelText: "email".translate),
              ),
              16.height,
              AppTextField(
                focus: passFocus,
                controller: passCont,
                nextFocus: confirmPasswordFocus,
                textFieldType: TextFieldType.PASSWORD,
                decoration: inputDecoration(context, labelText: "password".translate),
              ).visible(!widget.isOTP!),
              16.height,
              AppTextField(
                controller: confirmPassCont,
                textFieldType: TextFieldType.PASSWORD,
                focus: confirmPasswordFocus,
                nextFocus: mobileNumberFocus,
                decoration: inputDecoration(context, labelText: 'confirm_password'.translate),
                validator: (value) {
                  if (value!.trim().isEmpty) return errorThisFieldRequired;
                  if (value.trim().length < passwordLengthGlobal) return 'Password length should be more than six';
                  return passCont.text == value.trim() ? null : 'Password does not match';
                },
                autoFillHints: [AutofillHints.newPassword],
              ).visible(!widget.isOTP!),
              16.height.visible(!widget.isOTP!),
              AppTextField(
                focus: mobileNumberFocus,
                controller: mobileNumberCont,
                textFieldType: TextFieldType.PHONE,
                decoration: inputDecoration(context, labelText: "mobile_number".translate),
              ),
              36.height,
              AppButton(
                width: context.width(),
                color: primaryColor,
                text: "Sign Up",
                hoverColor: Colors.white,
                onTap: () {
                  widget.isOTP! ? signUpWithOtp() : signUpWithEmail();
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
