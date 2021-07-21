import 'package:app/conditions.dart';
import 'package:context_menus/context_menus.dart';
import 'package:flutter/cupertino.dart';

class FolderContextMenu extends StatefulWidget {
  FolderContextMenu(this._key);

  final String _key;

  @override
  State<StatefulWidget> createState() {
    return _FolderContextMenuState();
  }
}

class _FolderContextMenuState extends State<FolderContextMenu> with ContextMenuStateMixin {
  @override
  Widget build(BuildContext context) {
    return cardBuilder.call(
      context,
      [
        buttonBuilder.call(
            context,
            ContextMenuButtonConfig(
              Conditions.trashed ? '恢复目录' : '删除目录',
              onPressed: () => handlePressed(context, _handleDeleteFolderPressed),
            )),
      ],
    );
  }

  void _handleDeleteFolderPressed() {
    print('key is ${widget._key}');
  }
}
