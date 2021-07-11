import 'package:app/conditions.dart';
import 'package:app/config.dart';
import 'package:app/event_bus.dart';
import 'package:dio/dio.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_treeview/flutter_treeview.dart';
import 'package:logger/logger.dart';

class FolderTreePanel extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _FolderTreePanelState();
  }
}

class _FolderTreePanelState extends State<FolderTreePanel> {
  late TreeViewController _treeViewController;

  @override
  Widget build(BuildContext context) {
    Logger().i('build folder tree');
    return FutureBuilder<TreeViewController>(
      future: doInit(),
      builder: (context, snapshot) {
        if (snapshot.hasData) {
          _treeViewController = snapshot.data!;
          return TreeView(
            controller: _treeViewController,
            allowParentSelect: true,
            supportParentDoubleTap: true,
            onExpansionChanged: _expandNodeHandler,
            theme: TreeViewTheme(
              colorScheme: ColorScheme.dark().copyWith(onPrimary: Colors.blue),
            ),
            onNodeTap: (key) {
              _selectChange(key);
            },
          );
        } else
          return Text('Loading...');
      },
    );
  }

  _expandNodeHandler(String key, bool p2) {
    _selectChange(key);
  }

  void _selectChange(String key) {
    setState(() {
      _treeViewController = _treeViewController.copyWith(selectedKey: key);
      var node = _treeViewController.getNode(key);
      if (node != null && node.hasData) {
        Conditions.path = node.data;
      } else {
        Conditions.path = '';
      }
      BUS.emit(EventBus.ConditionsChanged);
    });
  }

  Future<TreeViewController> doInit() async {
    var resp = await Dio().post(Config.toUrl("/api/getFoldersData"), data: {'trashed': Conditions.trashed});

    if (resp.statusCode != 200) {
      Logger().e("Can't load folders data: $resp");
      return TreeViewController();
    }
    List results = resp.data;
    List<Node> folders = <Node>[];
    results.forEach((element) {
      folders.add(Node(label: element['title'], key: '${element['id']}', data: element['path']));
    });

    List<Node> nodes = [
      Node(
        label: '全部',
        key: '',
        expanded: true,
        icon: Icons.folder_open,
        children: folders,
      ),
    ];
    return TreeViewController(children: nodes);
  }
}
