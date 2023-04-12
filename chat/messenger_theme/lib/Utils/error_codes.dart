//*************   © Copyrighted by Thinkcreative_Technologies. An Exclusive item of Envato market. Make sure you have purchased a Regular License OR Extended license for the Source Code from Envato to use this product. See the License Definition attached with source code. *********************

import 'package:flutter/material.dart';

String errorCode(String err) {
  String s = 'Unexpected Error Occured 909.';
  switch (err) {
    case "7001":
      s = "Project source code or License is tampered";
      break;
    case "7002":
      s = "Project source code or License is tampered. ";
      break;
    case "7003":
      s = "This purchase code is no longer valid.";
      break;
    case "7004":
      s = "License type not defined.";
      break;
    case "7005":
      s = "Failed to validate purchase code. Kindly copy & paste the purchase code & try again. If it still continues please get in touch with us.";
      break;
    case "7006":
      s = "Invalid Purchase Code. Kindly copy & paste the purchase code.  ";
      break;
    case "7007":
      s = "Failed to validate the Purchase code. Please report this issue to the developer.";
      break;
    case "7008":
      s = "Unable to Install the App using this Purchase Code. You have Installed the same project multiple times. Maximum Usage Limit reached for Regular License.";
      break;
    case "7009":
      s = "Project source code is tampered. Kindly download the original source code and start setting up again";
      break;
    case "7010":
      s = "Emulator Not allowed. Please run on a real device.  ";
      break;
    case "7011":
      s = "This purchase code is already used for setting up other Project. Regular License allows only 1 project Installation. ";
      break;
    case "7012":
      s = "Failed to validate purchase code. Please try again. If it still continues report it to contact@tctech.in";
      break;
    case "7013":
      s = "10 out of 10 projects has been already installed using this Extended license. Maximum 10 projects installation allowed per Extended license. ";
      break;
    case "7014":
      s = "Project source code or License is tampered.  Kindly report it to contact@tctech.in . ";
      break;
    case "7015":
      s = "Source code version has already been updated using this License  ";
      break;
    case "7016":
      s = "License is Invalidated due to License violation. You can report it to contact@tctech.in  ";
      break;

    case "7017":
      s = "Failed to validate purchase code. Please try again. If it still continues kindly report it to us. ";
      break;

    case "7018":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us.  ";
      break;

    case "7019":
      s = "Project source code or License is tampered.";
      break;

    case "7020":
      s = "Project source code or License is tampered.  ";
      break;

    case "7021":
      s = "Failed to load. Please report it to administrator/developer.";
      break;

    case "7022":
      s = "Project source code or License is tampered. ";
      break;

    case "7023":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us.";
      break;

    case "7024":
      s = "You have installed a single project mutliple times using the same Extended License. kindly create a new project and then proceed for another Installation. Thank you!";
      break;

    case "7025":
      s = "The current source code you are trying to install is not the latest source code. Kindly use the latest source code available in CodeCanyon. ";
      break;

    case "7026":
      s = "The current project was not installed using this License. Kindly use proper License for this project.";
      break;

    case "7027":
      s = "License is Invalidated due to License violation. You can report it to contact@tctech.in ";
      break;

    case "7028":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us.";
      break;

    case "7029":
      s = "Project source code or License is tampered.";
      break;

    case "7030":
      s = "Failed to validate license. Please try again. ";
      break;

    case "7031":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us. ";
      break;

    case "7032":
      s = "Failed to validate. Project source code is Tampered.";
      break;

    case "7033":
      s = "Failed to validate. Project source code is Tampered.";
      break;

    case "7034":
      s = "Failed to validate. Project source code is Tampered.  ";
      break;

    case "7035":
      s = "Failed to validate license. Please report it to us. ";
      break;

    case "7036":
      s = "Failed to validate. Project source code is Tampered or License is tampered. ";
      break;

    case "7037":
      s = "Failed to validate. Project source code is Tampered or License is tampered. ";
      break;

    case "7038":
      s = "Purchase code is no longer valid.";
      break;

    case "7039":
      s = "Failed to validate license. Please try again. ";
      break;

    case "7040":
      s = "Failed to validate. Project source code is Tampered or License is tampered. ";
      break;

    case "7041":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us. ";
      break;

    case "7042":
      s = "Failed to validate. Project source code is Tampered or License is tampered.";
      break;

    case "7043":
      s = "Failed to validate license   ";
      break;

    case "7044":
      s = "Project source code or License is tampered ";
      break;

    case "7045":
      s = "Failed to update. You are using older version of the source code. Kindly download and use the latest source code available in CodeCanyon. ";
      break;

    case "7046":
      s = "Failed to install. You are using older version of the source code. Kindly download and use the latest source code available in CodeCanyon.  ";
      break;

    case "7047":
      s = "Failed to validate. Project source code is Tampered or License is tampered.";
      break;

    case "7048":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us.  ";
      break;
    case "7049":
      s = "Failed to validate. Project source code is Tampered or License is tampered.   ";
      break;
    case "7050":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us.  ";
      break;
    case "7051":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us. ";
      break;
    case "7052":
      s = "Purchase code is Invalid. Kindly copy & paste the purchase code. If it still continues kindly report it to us. ";
      break;
    case "7053":
      s = "This Project cannot use any License.   ";
      break;
    case "7054":
      s = "10 out of 10 projects has been already installed using this Extended license. Maximum 10 projects installation allowed per extended license. ";
      break;
    case "7055":
      s = "Failed to validate. Project source code is Tampered or License is tampered.  ";
      break;
    case "7056":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us.  ";
      break;
    case "7057":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us. ";
      break;
    case "7058":
      s = "Failed to validate. Kindly report it to contact@tctech.in  ";
      break;
    case "7059":
      s = "This Project cannot use any License.  ";
      break;
    case "7060":
      s = "Failed to validate. Project source code is Tampered or License is tampered.  ";
      break;
    case "7061":
      s = "Failed to validate. Kindly report it to contact@tctech.in  ";
      break;
    case "7062":
      s = "License is Invalidated due to License violation. For any information, you can connect with us at : contact@tctech.in ";
      break;
    case "7063":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us.  ";
      break;
    case "7064":
      s = "Failed to validate. Kindly report it to contact@tctech.in ";
      break;
    case "7065":
      s = "Failed to validate. Kindly report it to contact@tctech.in ";
      break;
    case "7066":
      s = "License is Invalidated due to License violation. You can report it to contact@tctech.in   ";
      break;
    case "7067":
      s = "License is Invalidated due to License violation. You can report it to contact@tctech.in  ";
      break;
    case "7068":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us.   ";
      break;
    case "7069":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us.   ";
      break;
    case "7070":
      s = "Failed to validate license. Please report it to us.  ";
      break;
    case "7071":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us.   ";
      break;
    case "7072":
      s = "The current project was not installed using this License. Kindly use proper License for this project. ";
      break;
    case "7073":
      s = "10 out of 10 projects has been already installed using this Extended license. Maximum 10 projects installation allowed per Extended license. ";
      break;
    case "7074":
      s = "Project source code or License is tampered. ";
      break;
    case "7075":
      s = "Failed to validate purchase code. Kindly copy & paste the purchase code & try again. If it still continues please get in touch with us. ";
      break;
    case "7076":
      s = "License is Invalidated due to License violation. You can report it to contact@tctech.in  ";
      break;
    case "7077":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us. ";
      break;
    case "7078":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us. ";
      break;
    case "7079":
      s = "Unable to Install the App using this Purchase Code. You have Installed the same project multiple times. Maximum Usage Limit reached for Regular License. ";
      break;
    case "7080":
      s = "This purchase code is already used for setting up other Project. Regular License allows only 1 project Installation. ";
      break;
    case "7081":
      s = "Failed to validate purchase code. Please try again. If it still continues kindly report it to us.";
      break;
    case "7082":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us.  ";
      break;
    case "7083":
      s = "You have installed a single project mutliple times using the same Extended License. kindly create a new project and then proceed for another Installation. Thank you! ";
      break;
    case "7084":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us.  ";
      break;
    case "7085":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us. ";
      break;
    case "7086":
      s = "10 out of 10 projects has been already installed using this Extended license. Maximum 10 projects installation allowed per extended license. ";
      break;
    case "7087":
      s = "License type not defined. ";
      break;
    case "7088":
      s = "Failed to validate the Purchase code. Please report this issue to the developer. ";
      break;
    case "7089":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us.  ";
      break;
    case "7090":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us.  ";
      break;
    case "7091":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to us.  ";
      break;
    case "7092":
      s = "Failed to validate. Project source code is Tampered or License is tampered.  ";
      break;

    case "7093":
      s = "Failed to validate. Project source code is Tampered or License is tampered.   ";
      break;
    case "7094":
      s = "Failed to validate.  ";
      break;
    case "7095":
      s = "Failed to validate. If it still continues kindly report it to contact@tctech.in.  ";
      break;
    case "7096":
      s = "Failed to validate. If it still continues kindly report it to contact@tctech.in.  ";
      break;
    case "7097":
      s = "Failed to validate. If it still continues kindly report it to contact@tctech.in. ";
      break;
    case "7100":
      s = "Failed to validate. If it still continues kindly report it to contact@tctech.in. ";
      break;
    case "7101":
      s = "Failed to validate. Project source code is Tampered or License is tampered.   ";
      break;
    case "7102":
      s = "Failed to validate. Project source code is Tampered or License is tampered.   ";
      break;
    case "7103":
      s = "Failed to validate. Project source code is Tampered or License is tampered.   ";
      break;
    case "7104":
      s = "Failed to validate. Project source code is Tampered or License is tampered.  ";
      break;
    case "7105":
      s = "Failed to validate. Project source code is Tampered or License is tampered.   ";
      break;
    case "7106":
      s = "Failed to validate. Project source code is Tampered or License is tampered.  ";
      break;
    case "7107":
      s = "Failed to validate. Please try again. If it still continues kindly report it to contact@tctech.in ";
      break;
    case "7108":
      s = "Failed to validate. Please try again. If it still continues kindly report it to contact@tctech.in";
      break;
    case "7109":
      s = "Project source code or License is tampered.  ";
      break;
    case "7110":
      s = "Failed to validate. Please try again. If it still continues kindly report it to contact@tctech.in ";
      break;
    case "7111":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to contact@tctech.in  ";
      break;
    case "7112":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to contact@tctech.in  ";
      break;
    case "7113":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to contact@tctech.in  ";
      break;
    case "7114":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to contact@tctech.in ";
      break;
    case "7115":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to contact@tctech.in   ";
      break;
    case "7116":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to contact@tctech.in  ";
      break;
    case "7117":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to contact@tctech.in   ";
      break;
    case "7118":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to contact@tctech.in ";
      break;
    case "7119":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to contact@tctech.in   ";
      break;
    case "7120":
      s = "Failed to validate license. Please try again. If it still continues kindly report it to contact@tctech.in  ";
      break;
    case "7121":
      s = "unexpected Error occured. If it still continues kindly report it to contact@tctech.in  ";
      break;
    case "7122":
      s = "unexpected Error occured. If it still continues kindly report it to contact@tctech.in    ";
      break;

    //------

    case "7098":
      s = "Invalid Request  ";
      break;
    case "7099":
      s = "Invalid Request    ";
      break;

    default:
      return s;
  }
  return s;
}

showERRORSheet(BuildContext context, String errorcode,
    {String? message, List<Widget>? optionalWidgets}) {
  showModalBottomSheet(
    isScrollControlled: true,
    shape: RoundedRectangleBorder(
      borderRadius: BorderRadius.only(
        topRight: Radius.circular(10),
        topLeft: Radius.circular(10),
      ),
    ),
    backgroundColor: Colors.white,
    builder: (BuildContext context) {
      return Container(
        height: optionalWidgets == null
            ? MediaQuery.of(context).size.height * 0.49
            : MediaQuery.of(context).size.height * 0.73,
        decoration: new BoxDecoration(
          color: Colors.white,
          borderRadius: new BorderRadius.only(
            topLeft: const Radius.circular(25.0),
            topRight: const Radius.circular(25.0),
          ),
        ),
        child: Padding(
          padding: const EdgeInsets.fromLTRB(28, 17, 28, 17),
          child: Column(
            children: message == null
                ? [
                    Container(
                      alignment: Alignment.topRight,
                      height: 40,
                      child: IconButton(
                          onPressed: () {
                            Navigator.of(context).pop();
                          },
                          icon: Icon(
                            Icons.close_outlined,
                            color: Colors.blueGrey.withOpacity(0.5),
                          )),
                    ),
                    Icon(
                      Icons.error_outline_rounded,
                      color: Colors.pink[400],
                      size: 80,
                    ),
                    SizedBox(
                      height: 25,
                    ),
                    Text(
                      "Failed to Install",
                      textAlign: TextAlign.center,
                      style: TextStyle(
                          height: 1.4,
                          fontSize: 17,
                          fontWeight: FontWeight.w700),
                    ),
                    SizedBox(
                      height: 16,
                    ),
                    Text(
                      errorCode(errorcode) + ".\n\n ERR_CODE_$errorcode",
                      textAlign: TextAlign.center,
                      style: TextStyle(height: 1.4),
                    )
                  ]
                : optionalWidgets == null
                    ? [
                        Container(
                          alignment: Alignment.topRight,
                          height: 40,
                          child: IconButton(
                              onPressed: () {
                                Navigator.of(context).pop();
                              },
                              icon: Icon(
                                Icons.close_outlined,
                                color: Colors.blueGrey.withOpacity(0.5),
                              )),
                        ),
                        Icon(
                          Icons.error_outline_rounded,
                          color: Colors.pink[400],
                          size: 80,
                        ),
                        SizedBox(
                          height: 25,
                        ),
                        Text(
                          "Failed !",
                          textAlign: TextAlign.center,
                          style: TextStyle(
                              height: 1.4,
                              fontSize: 17,
                              fontWeight: FontWeight.w700),
                        ),
                        SizedBox(
                          height: 16,
                        ),
                        Text(
                          errorcode == ""
                              ? message
                              : message + ".\n\n ERR_CODE_$errorcode",
                          textAlign: TextAlign.center,
                          style: TextStyle(height: 1.4),
                        )
                      ]
                    : [
                          Container(
                            alignment: Alignment.topRight,
                            height: 40,
                            child: IconButton(
                                onPressed: () {
                                  Navigator.of(context).pop();
                                },
                                icon: Icon(
                                  Icons.close_outlined,
                                  color: Colors.blueGrey.withOpacity(0.5),
                                )),
                          ),
                          Icon(
                            Icons.error_outline_rounded,
                            color: Colors.pink[400],
                            size: 80,
                          ),
                          SizedBox(
                            height: 25,
                          ),
                          Text(
                            "Failed !",
                            textAlign: TextAlign.center,
                            style: TextStyle(
                                height: 1.4,
                                fontSize: 17,
                                fontWeight: FontWeight.w700),
                          ),
                          SizedBox(
                            height: 16,
                          ),
                          Text(
                            errorcode == ""
                                ? message
                                : message + ".\n\n ERR_CODE_$errorcode",
                            textAlign: TextAlign.center,
                            style: TextStyle(height: 1.4),
                          )
                        ] +
                        optionalWidgets,
          ),
        ),
      );
    },
    context: context,
  );
}
