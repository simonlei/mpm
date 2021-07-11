import 'package:flutter/cupertino.dart';
import 'package:logger/logger.dart';

class FolderTreePanel extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _FolderTreePanelState();
  }
}

class _FolderTreePanelState extends State<FolderTreePanel> {
  @override
  Widget build(BuildContext context) {
    Logger().i('build folder tree');
    return Text('ssss');
  }
}
