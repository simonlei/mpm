import 'package:app/headers/trash_box_switcher.dart';
import 'package:app/headers/upload_selector.dart';
import 'package:app/pics_grid.dart';
import 'package:flutter/material.dart';

class MyHomePage extends StatefulWidget {
  MyHomePage({Key? key, required this.title}) : super(key: key);
  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('xxxxx'),
        actions: [
          // text 跳转 |
          // order
          // 过滤条件 （只看star）|
          TrashBoxSwitcher(),
          UploadSelector(),
          // 回收站
        ],
        toolbarHeight: 25,
      ),
      body: PicsGrid(),
    );
  }
}
