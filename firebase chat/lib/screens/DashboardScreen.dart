import 'dart:ui';

import 'package:chat/main.dart';
import 'package:chat/screens/CallLogScreen.dart';
import 'package:chat/screens/ChatListScreen.dart';
import 'package:chat/screens/NewChatScreen.dart';
import 'package:chat/screens/PickupLayout.dart';
import 'package:chat/screens/SettingScreen.dart';
import 'package:chat/services/localDB/LogRepository.dart';
import 'package:chat/services/localDB/SqliteMethods.dart';
import 'package:chat/utils/AppColors.dart';
import 'package:chat/utils/AppCommon.dart';
import 'package:chat/utils/AppConstants.dart';
import 'package:chat/utils/AppDataProvider.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:nb_utils/nb_utils.dart';

bool isSearch = false;

class DashboardScreen extends StatefulWidget {
  @override
  _DashboardScreenState createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> with SingleTickerProviderStateMixin {
  TabController? _tabController;
  int tabIndex = 0;

  bool autoFocus = false;
  TextEditingController searchCont = TextEditingController();

  FocusNode searchFocus = FocusNode();

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
    log("Test ${getStringAsync(playerId)}");
    _tabController = TabController(vsync: this, initialIndex: 0, length: 2);
    _tabController!.addListener(() {
      setState(() {
        isSearch = false;
        tabIndex = _tabController!.index;
      });
    });
    window.onPlatformBrightnessChanged = () {
      if (getIntAsync(THEME_MODE_INDEX) == ThemeModeSystem) {
        appStore.setDarkMode(MediaQuery.of(context).platformBrightness == Brightness.light);
        if (appStore.isDarkMode) {
          setStatusBarColor(scaffoldSecondaryDark, statusBarBrightness: Brightness.light, statusBarIconBrightness: Brightness.light);
        } else {
          setStatusBarColor(primaryColor);
        }
      }
    };

    LogRepository.init(dbName: getStringAsync(userId));

    localDbInstance = await SqliteMethods.initInstance();
  }

  @override
  void setState(fn) {
    if (mounted) super.setState(fn);
  }

  @override
  void dispose() {
    super.dispose();
    _tabController?.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 2,
      child: PickupLayout(
        child: SafeArea(
          child: Scaffold(
            appBar: AppBar(
              actions: [
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
                          textAlignVertical: TextAlignVertical.center,
                          cursorColor: Colors.white,
                          onChanged: (s) {
                            LiveStream().emit(SEARCH_KEY, s);
                          },
                          style: TextStyle(color: Colors.white),
                          cursorHeight: 19,
                          controller: searchCont,
                          focusNode: searchFocus,
                          decoration: InputDecoration(
                            border: InputBorder.none,
                            hintText: 'search_here'.translate,
                            hintStyle: TextStyle(color: Colors.white),
                          ),
                        ).expand(),
                      IconButton(
                        icon: isSearch ? Icon(Icons.close) : Icon(Icons.search),
                        onPressed: () async {
                          isSearch = !isSearch;

                          setState(() {});

                          if (isSearch) {
                            300.milliseconds.delay.then((value) {
                              context.requestFocus(searchFocus);
                            });
                          }
                        },
                        color: Colors.white,
                      )
                    ],
                  ),
                  width: isSearch ? context.width() - 86 : 50,
                ),
                PopupMenuButton(
                  color: context.cardColor,
                  onSelected: (dynamic value) async {
                    if (tabIndex == 0) {
                      if (value == 1) {
                        NewChatScreen().launch(context);
                      } else if (value == 2) {
                        SettingScreen().launch(context);
                      }
                    } else {
                      if (value == 1) {
                        bool? res = await showConfirmDialog(context, "log_confirmation".translate, buttonColor: secondaryColor);
                        if (res ?? false) {
                          LogRepository.deleteAllLogs();
                          setState(() {});
                        }
                      }
                    }
                  },
                  itemBuilder: (context) => tabIndex == 1 ? chatLogPopUpMenuItem : dashboardPopUpMenuItem,
                )
              ],
              bottom: TabBar(
                controller: _tabController,
                indicatorColor: Colors.white,
                labelStyle: boldTextStyle(size: 16),
                unselectedLabelColor: Colors.white60,
                labelColor: Colors.white,
                onTap: (index) {
                  setState(() {});
                  isSearch = false;
                  tabIndex = index;
                },
                tabs: [
                  Tab(text: 'chats'.translate.toUpperCase()),
                  Tab(text: 'calls'.translate.toUpperCase()),
                ],
              ),
              backgroundColor: context.primaryColor,
              title: Text(AppName, style: boldTextStyle(color: Colors.white, size: 20)),
            ),
            body: TabBarView(
              controller: _tabController,
              children: [
                ChatListScreen(),
                CallLogScreen(),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
