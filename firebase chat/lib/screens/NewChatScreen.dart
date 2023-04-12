import 'package:chat/main.dart';
import 'package:chat/models/UserModel.dart';
import 'package:chat/screens/ChatScreen.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:chat/utils/Appwidgets.dart';
import 'package:flutter/material.dart';
import 'package:nb_utils/nb_utils.dart';

class NewChatScreen extends StatefulWidget {
  @override
  _NewChatScreenState createState() => _NewChatScreenState();
}

class _NewChatScreenState extends State<NewChatScreen> {
  bool isSearch = false;
  bool autoFocus = false;
  TextEditingController searchCont = TextEditingController();
  String search = '';

  @override
  void initState() {
    super.initState();
    init();
  }

  init() async {}

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        appBar: appBarWidget("New Chat", textColor: Colors.white, actions: [
          AnimatedContainer(
            margin: EdgeInsets.only(left: 8),
            padding: isSearch ? EdgeInsets.only(left: 16, right: 4) : EdgeInsets.all(0),
            duration: Duration(milliseconds: 100),
            curve: Curves.decelerate,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                if (isSearch)
                  TextField(
                    autofocus: true,
                    textAlignVertical: TextAlignVertical.center,
                    cursorColor: Colors.white,
                    onChanged: (s) {
                      setState(() {});
                    },
                    style: TextStyle(color: Colors.white),
                    cursorHeight: 19,
                    controller: searchCont,
                    decoration: InputDecoration(
                      border: InputBorder.none,
                      hintText: 'Search here...',
                      hintStyle: TextStyle(color: Colors.white),
                    ),
                  ).expand(),
                IconButton(
                    icon: isSearch ? Icon(Icons.close) : Icon(Icons.search),
                    onPressed: () async {
                      isSearch = !isSearch;
                      searchCont.clear();
                      search = "";
                      setState(() {});
                    },
                    color: Colors.white)
              ],
            ),
            width: isSearch ? context.width() - 86 : 50,
          ),
        ]),
        body: StreamBuilder<List<UserModel>>(
          stream: userService.users(searchText: searchCont.text),
          builder: (_, snap) {
            if (snap.hasData) {
              if (snap.data!.length == 0) {
                return NoChatWidget().withHeight(context.height()).center();
              }

              return Container(
                height: context.height(),
                child: ListView.separated(
                  itemCount: snap.data!.length,
                  shrinkWrap: true,
                  itemBuilder: (context, index) {
                    UserModel data = snap.data![index];
                    if (data.uid == getStringAsync(userId)) {
                      return 0.height;
                    }
                    return Container(
                      padding: EdgeInsets.symmetric(vertical: 8, horizontal: 16),
                      child: Row(
                        children: [
                          data.photoUrl!.isEmpty
                              ? Hero(
                                  tag: data.uid.validate(),
                                  child: Container(
                                    height: 50,
                                    width: 50,
                                    padding: EdgeInsets.all(10),
                                    color: primaryColor,
                                    child: Text(data.name.validate()[0].toUpperCase(), style: secondaryTextStyle(color: Colors.white)).center().fit(),
                                  ).cornerRadiusWithClipRRect(50),
                                )
                              : cachedImage(data.photoUrl.validate(), width: 50, height: 50, fit: BoxFit.cover).cornerRadiusWithClipRRect(80),
                          12.width,
                          Column(
                            mainAxisAlignment: MainAxisAlignment.start,
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('${data.name.validate().capitalizeFirstLetter()}', style: primaryTextStyle()),
                              Text('${data.userStatus.validate()}', style: secondaryTextStyle()),
                            ],
                          ).expand(),
                        ],
                      ),
                    ).onTap(() {
                      finish(context);
                      ChatScreen(data).launch(context);
                    });
                  },
                  separatorBuilder: (BuildContext context, int index) {
                    if (snap.data![index].uid == getStringAsync(userId)) {
                      return 0.height;
                    }
                    return Divider(indent: 80, height: 0);
                  },
                ),
              );
            }
            return snapWidgetHelper(snap);
          },
        ),
      ),
    );
  }
}
