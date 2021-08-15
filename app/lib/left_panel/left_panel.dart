import 'package:app/left_panel/date_tree_panel.dart';
import 'package:app/left_panel/folder_tree_panel.dart';
import 'package:app/model/conditions.dart';
import 'package:flutter/material.dart';
import 'package:logger/logger.dart';

class LeftPanel extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _LeftPanelState();
  }
}

class _LeftPanelState extends State<LeftPanel> {
  late Container _container;
  bool _inited = false;

  @override
  Widget build(BuildContext context) {
    if (_inited) return _container;
    _inited = true;
    _container = Container(
      width: 250,
      child: DefaultTabController(
        length: 2,
        child: Builder(builder: (BuildContext context) {
          TabController controller = DefaultTabController.of(context)!;
          controller.addListener(() {
            if (controller.indexIsChanging) {
              Logger().i("Changing index ${controller.index} from ${controller.previousIndex}");
              if (controller.index == 0) {
                Conditions.path = '';
              } else {
                Conditions.dateKey = '';
              }
            }
          });

          return Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TabBar(tabs: [
                Tab(
                  child: Text(
                    '按日期查看',
                    style: TextStyle(fontWeight: FontWeight.w500, color: Colors.black),
                  ),
                ),
                Tab(
                  child: Text(
                    '按图片库查看',
                    style: TextStyle(fontWeight: FontWeight.w500, color: Colors.black),
                  ),
                )
              ]),
              Expanded(
                child: TabBarView(
                  children: [
                    DateTreePanel(),
                    FolderTreePanel(true),
                  ],
                ),
              ),
            ],
          );
        }),
      ),
    );
    return _container;
  }
}
