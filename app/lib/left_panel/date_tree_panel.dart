import 'package:app/model/conditions.dart';
import 'package:app/model/config.dart';
import 'package:app/model/event_bus.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_treeview/flutter_treeview.dart';
import 'package:logger/logger.dart';

class DateTreePanel extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _DateTreePanelState();
  }
}

class _DateTreePanelState extends State<DateTreePanel> {
  late TreeViewController _treeViewController;
  bool _inited = false;

  _conditionCallback(arg) {
    setState(() {
      _inited = false;
    });
  }

  @override
  void initState() {
    super.initState();
    print('Initing date tree');
    BUS.on(EventBus.LeftTreeConditionsChanged, _conditionCallback);
  }

  @override
  void dispose() {
    super.dispose();
    print('dispose date tree');
    BUS.off(EventBus.LeftTreeConditionsChanged, _conditionCallback);
  }

  @override
  Widget build(BuildContext context) {
    Logger().i('build date tree');
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

  void _selectChange(String key) {
    setState(() {
      _treeViewController =
          _treeViewController.copyWith(selectedKey: key).withExpandToNode(key);
      Conditions.dateKey = key;
      BUS.emit(EventBus.ConditionsChanged);
    });
  }

  Future<TreeViewController> doInit() async {
    if (_inited) return _treeViewController;

    print('I am going to init controller...');

    var resp = await Dio().post(Config.api("/api/getPicsDate"), data: {
      'trashed': Conditions.trashed,
      'star': Conditions.star,
    });

    if (resp.statusCode != 200) {
      Logger().e("Can't load picsDate: $resp");
      return TreeViewController();
    }
    List results = resp.data;
    List<Node> years = <Node>[];
    results.forEach((element) {
      List<Node> months = <Node>[];
      List monthResults = element['months'] as List;
      monthResults.forEach((element) {
        months.add(Node(
            label: element['title'],
            key: '${element['id']}',
            icon: Icons.calendar_view_day));
      });
      years.add(Node(
          label: element['title'],
          key: '${element['id']}',
          children: months,
          icon: Icons.calendar_view_month));
    });

    List<Node> nodes = [
      Node(
        label: '全部',
        key: '',
        expanded: true,
        icon: Icons.folder_open,
        children: years,
      ),
    ];
    _inited = true;
    return TreeViewController(children: nodes);
  }

  _expandNodeHandler(String key, bool p2) {
    _selectChange(key);
  }
}
