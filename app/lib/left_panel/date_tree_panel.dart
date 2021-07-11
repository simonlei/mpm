import 'package:app/conditions.dart';
import 'package:app/config.dart';
import 'package:app/event_bus.dart';
import 'package:dio/dio.dart';
import 'package:flutter/cupertino.dart';
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
              onNodeTap: (key) {
                _selectChange(key);
              },
            );
          } else
            return Text('Loading...');
        });
  }

  void _selectChange(String key) {
    setState(() {
      _treeViewController = _treeViewController.copyWith(selectedKey: key);
      Conditions.dateKey = key;
      BUS.emit(EventBus.ConditionsChanged);
    });
  }

  Future<TreeViewController> doInit() async {
    var resp = await Dio().post(Config.toUrl("/api/getPicsDate"), data: {'trashed': Conditions.trashed});

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
        months.add(Node(label: element['title'], key: '${element['id']}'));
      });
      years.add(Node(label: element['title'], key: '${element['id']}', children: months));
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
    return TreeViewController(children: nodes);
  }

  _expandNodeHandler(String key, bool p2) {
    _selectChange(key);
  }
}
