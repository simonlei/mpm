import 'package:app/homepage.dart';
import 'package:app/model/conditions.dart';
import 'package:app/model/config.dart';
import 'package:app/model/event_bus.dart';
import 'package:confirm_dialog/confirm_dialog.dart';
import 'package:context_menus/context_menus.dart';
import 'package:dio/dio.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:oktoast/oktoast.dart';
import 'package:prompt_dialog/prompt_dialog.dart';

import 'folder_tree_panel.dart';

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
              '修改目录下所有照片时间...',
              onPressed: () => handlePressed(context, _handleChangeDatePressed),
            )),
        buttonBuilder.call(
            context,
            ContextMenuButtonConfig(
              '修改目录下所有照片GIS信息...',
              onPressed: () => handlePressed(context, _handleChangeGisPressed),
            )),
        buttonBuilder.call(
            context,
            ContextMenuButtonConfig(
              Conditions.trashed ? '恢复目录' : '删除目录',
              onPressed: () => handlePressed(context, _handleDeleteFolderPressed),
            )),
        buttonBuilder.call(
            context,
            ContextMenuButtonConfig(
              '移动目录至...',
              onPressed: () => handlePressed(context, _handleMoveFolderPressed),
            )),
        buttonBuilder.call(
            context,
            ContextMenuButtonConfig(
              '合并目录至...',
              onPressed: () => handlePressed(context, _handleMergeFolderPressed),
            )),
      ],
    );
  }

  void _handleMergeFolderPressed() async {
    var folder = await showFolderSelectDialog('合并至目录');
    if (folder == null) return;
    await Dio().post(Config.api("/api/moveFolder"), data: {'fromPath': widget._path, 'toId': folder, 'merge': true});
    BUS.emit(EventBus.LeftTreeConditionsChanged);
  }

  void _handleMoveFolderPressed() async {
    var folder = await showFolderSelectDialog('移动至目录');
    if (folder == null) return;
    await Dio().post(Config.api("/api/moveFolder"), data: {'fromPath': widget._path, 'toId': folder, 'merge': false});
    BUS.emit(EventBus.LeftTreeConditionsChanged);
  }

  Future<String?> showFolderSelectDialog(title) async {
    var folder = await showDialog<String>(
      context: scaffoldKey.currentContext!,
      builder: (context) {
        var treePanel = FolderTreePanel(false);
        return AlertDialog(
          title: Text(title),
          content: Container(
            width: 640,
            height: 480,
            child: treePanel,
          ),
          actions: <Widget>[
            TextButton(
              child: Text('确定'),
              onPressed: () {
                Navigator.of(context).pop(treePanel.getSelected());
              },
            ),
            TextButton(
              child: Text('取消'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
    print('select folder is $folder');
    return folder;
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

  void _handleChangeGisPressed() async {
    // 这里最好能抽象出一个方法来，重复了
    var result = await prompt(scaffoldKey.currentContext!, title: Text('请输入纬度,经度，例如 22.57765,113.9504277778'));
    if (result == null) return;
    print(' result is $result');
    var splitted = result.split(',');
    if (splitted.isEmpty || splitted.length != 2) {
      showToast('请按照 纬度,经度 模式来输入');
      return;
    }
    var response = await Dio().post(Config.api("/api/updateFolderGis"), data: {
      'path': widget._path,
      'latitude': splitted[0],
      'longitude': splitted[1],
    });
    if (response.statusCode == 200) {
      showToast('已设置目录下${response.data}张照片GIS信息至 ${result.toString()}');
      BUS.emit(EventBus.ConditionsChanged);
    }
  }

  void _handleChangeDatePressed() async {
    var result = await showDatePicker(
        context: context, initialDate: DateTime.now(), firstDate: DateTime(1950), lastDate: DateTime.now());
    if (result == null) return;
    print(result.toString());
    var response = await Dio().post(Config.api("/api/updateFolderDate"), data: {
      'path': widget._path,
      'toDate': result.toString(),
    });
    if (response.statusCode == 200) {
      showToast('已设置目录下${response.data}张照片拍摄时间至 ${result.toString()}');
      BUS.emit(EventBus.ConditionsChanged);
    }
  }
}
