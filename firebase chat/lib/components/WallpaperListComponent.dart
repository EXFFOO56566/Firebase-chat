import 'package:chat/models/WallpaperModel.dart';
import 'package:chat/screens/WallpaperChatPreviewScreen.dart';
import 'package:flutter/material.dart';
import 'package:flutter/painting.dart';
import 'package:nb_utils/nb_utils.dart';

class WallpaperListComponent extends StatefulWidget {
  final String? name;
  final bool? mIsIndividual;
  final List<WallpaperModel>? wallpaperList;

  WallpaperListComponent({this.name, this.wallpaperList, this.mIsIndividual});
  @override
  _WallpaperListComponentState createState() => _WallpaperListComponentState();
}

class _WallpaperListComponentState extends State<WallpaperListComponent> {
  @override
  void initState() {
    super.initState();
    init();
  }

  init() async {
    //
  }

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        appBar: appBarWidget("${widget.name.validate()} Wallpaper", textColor: Colors.white),
        body: body(),
      ),
    );
  }

  Widget body() {
    return Container(
      padding: EdgeInsets.all(8),
      child: SingleChildScrollView(
        child: Wrap(
          runSpacing: 8,
          spacing: 8,
          children: List.generate(
            widget.wallpaperList!.length,
            (index) {
              return Container(
                height: 200,
                width: context.width() / 3 - 12,
                decoration: BoxDecoration(
                  image: DecorationImage(
                    fit: BoxFit.cover,
                    image: Image.asset(widget.wallpaperList![index].path!).image,
                  ),
                ),
                alignment: Alignment.bottomRight,
              ).onTap(() {
                WallpaperChatPreviewScreen(index: index, wallpaperList: widget.wallpaperList, mIsIndividual: widget.mIsIndividual).launch(context);
              });
            },
          ),
        ),
      ),
    );
  }
}
