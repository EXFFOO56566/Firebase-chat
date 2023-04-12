import 'package:flutter_emoji/flutter_emoji.dart';

bool isAllEmoji(String text) {
  for (String s in EmojiParser().unemojify(text).split(" "))
    if (!s.startsWith(":") || !s.endsWith(":")) return false;
  return true;
}
