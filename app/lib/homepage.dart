import 'package:app/center/pics_grid.dart';
import 'package:app/headers/empty_trash_button.dart';
import 'package:app/headers/sort_selector.dart';
import 'package:app/headers/trash_box_switcher.dart';
import 'package:app/headers/upload_selector.dart';
import 'package:app/left_panel/left_panel.dart';
import 'package:app/model/event_bus.dart';
import 'package:context_menus/context_menus.dart';
import 'package:flutter/material.dart';

class MyHomePage extends StatefulWidget {
  MyHomePage({Key? key, required this.title}) : super(key: key);
  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

final GlobalKey<ScaffoldState> scaffoldKey = GlobalKey<ScaffoldState>();

class _MyHomePageState extends State<MyHomePage> {
  var appTitle = 'My Photo Manager';

  @override
  Widget build(BuildContext context) {
    BUS.on(EventBus.CountChange, (args) {
      appTitle = 'My Photo Manager($args)';
      setState(() {});
    });
    return ContextMenuOverlay(
      child: Scaffold(
        key: scaffoldKey,
        appBar: AppBar(
          title: Text(appTitle),
          centerTitle: true,
          actions: [
            // text 跳转 |
            // order
            // 过滤条件 （只看star）|
            SortSelector(),
            TrashBoxSwitcher(),
            // |
            EmptyTrashButton(),
            UploadSelector(),
            // 回收站
          ],
          toolbarHeight: 25,
        ),
        body: Row(children: [
          LeftPanel(),
          Expanded(
            child: PicsGrid(),
          ),
        ]),
      ),
    );
  }
}
