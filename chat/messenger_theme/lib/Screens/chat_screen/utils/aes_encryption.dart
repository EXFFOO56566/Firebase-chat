import 'package:encrypt/encrypt.dart';
// import 'package:encrypt/encrypt.dart';

class AESEncryptData {
//for AES Algorithms

  static Encrypted? encrypted;
  static var decrypted;

  static String? encryptAES(plainText, privatekey) {
    final key = Key.fromUtf8(privatekey.toString().substring(0, 32));
    final iv = IV.fromLength(16);
    final encrypter = Encrypter(AES(key));
    encrypted = encrypter.encrypt(plainText, iv: iv);
    return encrypted!.base64;
  }

  static String? decryptAES(plainText, privatekey) {
    final key = Key.fromUtf8(privatekey.toString().substring(0, 32));
    final iv = IV.fromLength(16);
    final encrypter = Encrypter(AES(key));

    decrypted = encrypter.decrypt(Encrypted.from64(plainText), iv: iv);

    return decrypted;
  }
}
