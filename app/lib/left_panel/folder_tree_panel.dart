import 'package:app/left_panel/folder_context_menu.dart';
import 'package:app/model/conditions.dart';
import 'package:app/model/config.dart';
import 'package:app/model/event_bus.dart';
import 'package:context_menus/context_menus.dart';
import 'package:dio/dio.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_treeview/flutter_treeview.dart';
import 'package:logger/logger.dart';

class FolderTreePanel extends StatefulWidget {
  final FolderTreePanelState _folderTreePanelState = FolderTreePanelState();
  final _inLeftPanel;

  FolderTreePanel(Key? key, this._inLeftPanel) : super(key: key) {
    print('hello FolderTreePanel');
  }

  @override
  State<StatefulWidget> createState() {
    return _folderTreePanelState;
  }

  String? getSelected() {
    return _folderTreePanelState._treeViewController.selectedKey;
  }
}

class FolderTreePanelState extends State<FolderTreePanel> {
  late TreeViewController _treeViewController;
  bool inited = false;

  _conditionCallback(arg) {
    setState(() {
      inited = false;
    });
  }

  @override
  void initState() {
    super.initState();
    if (widget._inLeftPanel) BUS.on(EventBus.LeftTreeConditionsChanged, _conditionCallback);
    // WidgetsBinding.instance!.addPostFrameCallback((_) => selectToKey(context));
  }

  @override
  void dispose() {
    super.dispose();
    if (widget._inLeftPanel) BUS.off(EventBus.LeftTreeConditionsChanged, _conditionCallback);
  }

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
            nodeBuilder: (context, node) {
              return ContextMenuRegion(
                contextMenu: FolderContextMenu(node.data),
                child: _buildLabel(context, node),
              );
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
        var resp = await Dio().post(Config.api("/api/getFoldersData"), data: {
          'trashed': Conditions.trashed,
          'parentId': key,
          'star': Conditions.star,
        });
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

  Future<void> _selectChange(String key) async {
    Logger().i('select state change $key');
    setState(() {
      _treeViewController = _treeViewController.copyWith(selectedKey: key).withExpandToNode(key);
    });
    await _expandNodeHandler(key, true);
    if (widget._inLeftPanel) {
      var node = _treeViewController.getNode(key);
      if (node != null && node.hasData) {
        Conditions.path = node.data;
      } else {
        Conditions.path = '';
      }
      BUS.emit(EventBus.ConditionsChanged);
    }
  }

  Future<TreeViewController> doInit() async {
    if (inited) return _treeViewController;
    var resp = await Dio().post(Config.api("/api/getFoldersData"), data: {
      'trashed': Conditions.trashed,
      'star': Conditions.star,
    });

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
        data: '',
        expanded: true,
        icon: Icons.folder_open,
        children: folders,
      ),
    ];
    inited = true;
    return TreeViewController(children: nodes);
  }

  Widget _buildLabel(BuildContext context, Node node) {
    // copy from TreeNode.class
    TreeView? _treeView = TreeView.of(context);
    assert(_treeView != null, 'TreeView must exist in context');
    TreeViewTheme _theme = _treeView!.theme;
    bool isSelected = _treeView.controller.selectedKey != null && _treeView.controller.selectedKey == node.key;
    //final icon = _buildNodeIcon(node);
    return Container(
      padding: EdgeInsets.symmetric(
        vertical: _theme.verticalSpacing ?? (_theme.dense ? 10 : 15),
        horizontal: 0,
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: <Widget>[
          // icon,
          Expanded(
            child: Text(
              node.label,
              softWrap: node.isParent ? _theme.parentLabelOverflow == null : _theme.labelOverflow == null,
              overflow: node.isParent ? _theme.parentLabelOverflow : _theme.labelOverflow,
              style: node.isParent
                  ? _theme.parentLabelStyle.copyWith(
                      fontWeight: _theme.parentLabelStyle.fontWeight,
                      color: isSelected ? _theme.colorScheme.onPrimary : _theme.parentLabelStyle.color,
                    )
                  : _theme.labelStyle.copyWith(
                      fontWeight: _theme.labelStyle.fontWeight,
                      color: isSelected ? _theme.colorScheme.onPrimary : null,
                    ),
            ),
          ),
        ],
      ),
    );
  }

  // void selectToKey(TreeViewController treeViewController, initKey) {
  void selectToKey(context) {
    _selectChange(context);
    // print('after widget init... ${widget._initKey}');
    // _selectChange(widget._initKey);
  }
}
