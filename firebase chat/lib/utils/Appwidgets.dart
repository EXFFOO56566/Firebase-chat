import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:nb_utils/nb_utils.dart';

AppBar appBar(context, String text, {Color backgroundColor = Colors.white, double textSize = 16, Color textColor = Colors.black, showBack = true, List<Widget>? actions}) {
  return AppBar(
    title: Text(text, style: TextStyle(color: textColor, fontSize: textSize)),
    backgroundColor: backgroundColor,
    leading: showBack ? IconButton(icon: Icon(Icons.arrow_back, color: textColor), onPressed: () => finish(context)) : null,
    actions: actions,
    automaticallyImplyLeading: true,
  );
}

Widget cachedImage(String? url, {double? height, double? width, BoxFit? fit, AlignmentGeometry? alignment, bool usePlaceholderIfUrlEmpty = true, double? radius}) {
  if (url.validate().isEmpty) {
    return placeHolderWidget(height: height, width: width, fit: fit, alignment: alignment, radius: radius);
  } else if (url.validate().startsWith('http')) {
    return CachedNetworkImage(
      imageUrl: url!,
      height: height,
      width: width,
      fit: fit,
      alignment: alignment as Alignment? ?? Alignment.center,
      errorWidget: (_, s, d) {
        return placeHolderWidget(height: height, width: width, fit: fit, alignment: alignment, radius: radius);
      },
      placeholder: (_, s) {
        if (!usePlaceholderIfUrlEmpty) return SizedBox();
        return placeHolderWidget(height: height, width: width, fit: fit, alignment: alignment, radius: radius);
      },
    );
  } else {
    return Image.asset(url!, height: height, width: width, fit: fit, alignment: alignment ?? Alignment.center).cornerRadiusWithClipRRect(radius ?? defaultRadius);
  }
}

Widget placeHolderWidget({double? height, double? width, BoxFit? fit, AlignmentGeometry? alignment, double? radius}) {
  return Image.asset('assets/placeholder.jpg', height: height, width: width, fit: fit ?? BoxFit.cover, alignment: alignment ?? Alignment.center).cornerRadiusWithClipRRect(radius ?? defaultRadius);
}

class NoChatWidget extends StatelessWidget {
  const NoChatWidget({
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        Image.asset("assets/no_messages.png", height: 250),
        16.height,
        Text('No Chats', style: boldTextStyle()).center(),
        16.height,
        Text('', style: primaryTextStyle()),
      ],
    );
  }
}

class NoCallLogsWidget extends StatelessWidget {
  const NoCallLogsWidget({
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        Image.asset("assets/no_messages.png", height: 250),
        16.height,
        Text('No Logs', style: boldTextStyle()).center(),
        16.height,
        Text('', style: primaryTextStyle()),
      ],
    );
  }
}
