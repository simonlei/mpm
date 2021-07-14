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
  bool _inited = false;

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

  _expandNodeHandler(String key, bool expend) async {
    Logger().i('going to expand $expend $key');
    if (expend) {
      var node = _treeViewController.getNode(key);
      if (node != null && node.children.isEmpty) {
        var resp =
            await Dio().post(Config.api("/api/getFoldersData"), data: {'trashed': Conditions.trashed, 'parentId': key});
        if (resp.statusCode != 200) {
          Logger().e("Can't load folders data: $resp");
          return;
        }
        List<Node> folders = <Node>[];
        resp.data.forEach((element) {
          folders.add(Node(label: element['title'], key: '${element['id']}', data: element['path'], parent: true));
        });
        setState(() {
          if (folders.isEmpty) {
            _treeViewController =
                _treeViewController.withUpdateNode(key, _treeViewController.getNode(key)!.copyWith(parent: false));
          }
          folders.forEach((element) {
            _treeViewController = _treeViewController.withAddNode(key, element);
          });
        });
      }
    }
  }

  void _selectChange(String key) {
    Logger().i('select state change $key');
    setState(() {
      _treeViewController = _treeViewController.copyWith(selectedKey: key).withExpandToNode(key);
      _expandNodeHandler(key, true);
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
    if (_inited) return _treeViewController;
    var resp = await Dio().post(Config.api("/api/getFoldersData"), data: {'trashed': Conditions.trashed});

    if (resp.statusCode != 200) {
      Logger().e("Can't load folders data: $resp");
      return TreeViewController();
    }
    List results = resp.data;
    List<Node> folders = <Node>[];
    results.forEach((element) {
      folders.add(Node(label: element['title'], key: '${element['id']}', data: element['path'], parent: true));
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
    _inited = true;
    return TreeViewController(children: nodes);
  }
}
