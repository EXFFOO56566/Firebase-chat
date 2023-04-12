import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppCommon.dart';
import 'package:flutter/material.dart';
import 'package:nb_utils/nb_utils.dart';

import '../main.dart';

class ForgotPasswordScreen extends StatefulWidget {
  static String tag = '/ForgotPasswordScreen';

  @override
  ForgotPasswordScreenState createState() => ForgotPasswordScreenState();
}

class ForgotPasswordScreenState extends State<ForgotPasswordScreen> {
  TextEditingController forgotEmailController = TextEditingController();
  GlobalKey<FormState> _formKey = GlobalKey<FormState>();

  @override
  void initState() {
    super.initState();
    init();
  }

  Future<void> init() async {
    setStatusBarColor(primaryColor);
  }

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  void dispose() {
    super.dispose();
    setStatusBarColor(primaryColor);
  }

  @override
  Widget build(BuildContext context) {
    return Form(
      autovalidateMode: AutovalidateMode.onUserInteraction,
      key: _formKey,
      child: Container(
        padding: EdgeInsets.all(16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('enter_you_email_address'.translate, style: primaryTextStyle(size: 14)),
            16.height,
            AppTextField(
              controller: forgotEmailController,
              textFieldType: TextFieldType.EMAIL,
              keyboardType: TextInputType.emailAddress,
              cursorColor: appStore.isDarkMode ? Colors.white : scaffoldColorDark,
              decoration: inputDecoration(context, labelText: 'Email'),
              errorInvalidEmail: 'Enter valid email',
              errorThisFieldRequired: errorThisFieldRequired,
            ),
            16.height,
            AppButton(
              child: Text('reset_password'.translate, style: boldTextStyle(color: appStore.isDarkMode ? scaffoldColorDark : Colors.white)),
              color: primaryColor,
              width: context.width(),
              onTap: () {
                if (_formKey.currentState!.validate()) {
                  authService.forgotPassword(email: forgotEmailController.text.trim()).then((value) {
                    toast('Reset password link has sent your mail');
                    finish(context);
                  }).catchError((error) {
                    toast(error.toString());
                  });
                }
              },
            ),
          ],
        ),
      ),
    );
  }
}
