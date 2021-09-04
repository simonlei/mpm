import 'package:app/left_panel/date_tree_panel.dart';
import 'package:app/left_panel/folder_tree_panel.dart';
import 'package:app/model/conditions.dart';
import 'package:flutter/material.dart';
import 'package:logger/logger.dart';

class LeftPanel extends StatefulWidget {
  LeftPanel(Key? key) : super(key: key);

  @override
  State<StatefulWidget> createState() {
    return LeftPanelState();
  }
}

final GlobalKey<FolderTreePanelState> folderTreeKey = GlobalKey<FolderTreePanelState>();

class LeftPanelState extends State<LeftPanel> with SingleTickerProviderStateMixin {
  late TabController _tabController;

  get selectedTab => _tabController.index;

  @override
  void initState() {
    super.initState();
    _tabController = new TabController(length: 2, vsync: this);
    _tabController.addListener(() {
      if (_tabController.indexIsChanging) {
        Logger().i("Changing index ${_tabController.index} from ${_tabController.previousIndex}");
        if (_tabController.index == 0) {
          Conditions.path = '';
        } else {
          Conditions.dateKey = '';
        }
      }
    });
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 250,
      child: Builder(builder: (BuildContext context) {
        return Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TabBar(
              controller: _tabController,
              tabs: [
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
              ],
            ),
            Expanded(
              child: TabBarView(
                controller: _tabController,
                children: [
                  DateTreePanel(),
                  FolderTreePanel(folderTreeKey, true),
                ],
              ),
            ),
          ],
        );
      }),
    );
  }

  void selectTab(int i) {
    setState(() {
      _tabController.animateTo(i);
    });
  }
}
