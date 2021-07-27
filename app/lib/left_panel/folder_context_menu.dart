import 'package:app/conditions.dart';
import 'package:app/config.dart';
import 'package:app/event_bus.dart';
import 'package:app/homepage.dart';
import 'package:confirm_dialog/confirm_dialog.dart';
import 'package:context_menus/context_menus.dart';
import 'package:dio/dio.dart';
import 'package:flutter/cupertino.dart';
import 'package:oktoast/oktoast.dart';

class FolderContextMenu extends StatefulWidget {
  FolderContextMenu(this._path);

  final String _path;

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

  void _handleDeleteFolderPressed() async {
    bool toTrash = await confirm(
      scaffoldKey.currentContext!,
      title: Text('确认${Conditions.trashed ? '从回收站恢复' : '移到回收站'}？'),
    );
    if (toTrash) {
      var response = await Dio().post(Config.api("/api/switchTrashFolder"), data: {
        'to': !Conditions.trashed,
        'path': widget._path,
      });
      if (response.statusCode == 200) {
        showToast('已 ${Conditions.trashed ? '恢复' : '删除'} ${response.data} 张图片');
        BUS.emit(EventBus.ConditionsChanged);
        BUS.emit(EventBus.LeftTreeConditionsChanged);
      }
    }
  }
}
