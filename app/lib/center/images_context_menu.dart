import 'package:app/homepage.dart';
import 'package:app/left_panel/left_panel.dart';
import 'package:app/main.dart';
import 'package:app/model/conditions.dart';
import 'package:app/model/config.dart';
import 'package:app/model/pics_model.dart';
import 'package:app/model/select_model.dart';
import 'package:app/widgets/tags_selector.dart';
import 'package:context_menus/context_menus.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:oktoast/oktoast.dart';
import 'package:prompt_dialog/prompt_dialog.dart';
import 'package:provider/provider.dart';

class ImagesContextMenu extends StatefulWidget {
  final PicsModel _picsModel;
  final bool _detail;

  ImagesContextMenu(this._picsModel, this._detail);

  @override
  State<StatefulWidget> createState() {
    return _ImagesContextMenuState();
  }
}

class _ImagesContextMenuState extends State<ImagesContextMenu>
    with ContextMenuStateMixin {
  @override
  Widget build(BuildContext context) {
    var widgets = [
      buttonBuilder.call(
          context,
          ContextMenuButtonConfig(
            widget._detail ? '修改时间' : '修改选中照片时间',
            onPressed: () => handlePressed(context, _handleChangeDatePressed),
          )),
      buttonBuilder.call(
          context,
          ContextMenuButtonConfig(
            widget._detail ? '修改描述信息' : '修改选中照片描述信息',
            onPressed: () => handlePressed(context, _handleChangeDescPressed),
          )),
      buttonBuilder.call(
          context,
          ContextMenuButtonConfig(
            '拷贝GIS信息',
            onPressed: () => handlePressed(context, _handleCopyGisPressed),
          )),
      buttonBuilder.call(
          context,
          ContextMenuButtonConfig(
            widget._detail ? '修改GIS信息' : '修改选中照片的GIS信息',
            onPressed: () => handlePressed(context, _handleChangeGisPressed),
          )),
      buttonBuilder.call(
          context,
          ContextMenuButtonConfig(
            widget._detail ? '修改标签' : '修改选中照片的标签',
            onPressed: () => handlePressed(context, _handleChangeTagsPressed),
          )),
    ];
    if (!widget._detail)
      widgets.add(buttonBuilder.call(
          context,
          ContextMenuButtonConfig(
            '跳转到文件夹',
            onPressed: () => handlePressed(context, _handleJumpToFolderPressed),
          )));
    return cardBuilder.call(
      context,
      widgets,
    );
  }

  void _handleChangeDatePressed() async {
    if (widget._picsModel.selectNothing) {
      showToast('请选中照片后再试');
      return;
    }

    var result = await showDatePicker(
      context: context,
      initialDate: widget._picsModel
          .imageAt(widget._picsModel.getSelectedIndex())!
          .takenDate,
      firstDate: DateTime(1950),
      lastDate: DateTime.now(),
      initialEntryMode: DatePickerEntryMode.input,
    );
    if (result == null) return;
    print(result.toString());
    widget._picsModel.updateSelectedImages(
        {'takenDate': result.toString()}, ' 拍摄时间到 ${result.toString()}');
    returnFocus();
  }

  void _handleChangeTagsPressed() async {
    if (widget._picsModel.selectNothing) {
      showToast('请选中照片后再试');
      return;
    }

    var resp = await Dio().post(Config.api("/api/getAllTags"));
    if (resp.statusCode != 200) {
      showToast("获取tags失败 ${resp.data}");
    }
    var listToSearch = resp.data as List;
    var selectedIndex = widget._picsModel.getSelectedIndex();
    var image = await widget._picsModel.getImage(selectedIndex);
    var initialValue = image!.tags == null
        ? null
        : image.tags!.split(",").where((s) => s.isNotEmpty).toList();

    List? result = await showDialog(
      context: scaffoldKey.currentContext!,
      builder: (BuildContext context) {
        var tagsSelector = TagsSelector(
          tags: listToSearch,
          initValues: initialValue,
        );
        return AlertDialog(
          title: Text('选择标签'),
          content: tagsSelector,
          actions: [
            TextButton(
              child: Text('确定'),
              onPressed: () =>
                  Navigator.of(context).pop(tagsSelector.selectedValues),
            ),
            TextButton(
              child: Text('取消'),
              onPressed: () => Navigator.of(context).pop(null),
            ),
          ],
        );
      },
    );
    if (result == null) return;

    print(result.join(","));
    widget._picsModel.updateSelectedImages(
        {'tags': result.join(",")}, ' 标签更新为 ${result.join(",")}');
    returnFocus();
  }

  void _handleCopyGisPressed() async {
    if (widget._picsModel.selectNothing) {
      showToast('请选中照片后再试');
      return;
    }

    var selectedIndex = widget._picsModel.getSelectedIndex();
    var image = await widget._picsModel.getImage(selectedIndex);
    await Clipboard.setData(
        ClipboardData(text: '${image!.latitude},${image.longitude}'));
    showToast('已拷贝GIS信息至剪贴板');
  }

  Future<void> _handleChangeDescPressed() async {
    if (widget._picsModel.selectNothing) {
      showToast('请选中照片后再试');
      return;
    }
    var result =
        await prompt(scaffoldKey.currentContext!, title: Text('请输入描述信息'));
    if (result == null) return;
    print(' result is $result');
    widget._picsModel
        .updateSelectedImages({'description': result}, ' 描述信息到 $result');
    returnFocus();
  }

  Future<void> _handleChangeGisPressed() async {
    if (widget._picsModel.selectNothing) {
      showToast('请选中照片后再试');
      return;
    }
    var result = await prompt(scaffoldKey.currentContext!,
        title: Text('请输入纬度,经度，例如 22.57765,113.9504277778'));
    if (result == null) return;
    print(' result is $result');
    var splitted = result.split(',');
    if (splitted.isEmpty || splitted.length != 2) {
      showToast('请按照 纬度,经度 模式来输入');
      return;
    }
    widget._picsModel.updateSelectedImages(
        {'latitude': splitted[0], 'longitude': splitted[1]}, ' GIS信息到 $result');
    returnFocus();
  }

  Future<void> _handleJumpToFolderPressed() async {
    if (widget._picsModel.selectNothing) {
      showToast('请选中照片后再试');
      return;
    }
    var picImage =
        await widget._picsModel.getImage(widget._picsModel.getSelectedIndex());
    var paths = await widget._picsModel.getPathsForSelectedPhoto();
    if (paths.length == 0) {
      showToast('没有找到对应的目录');
      return;
    }
    var index = 0;
    if (paths.length > 1) {
      // select folder if have two or more folders
      index = await selectPath(paths);
    }
    print(paths[index]);
    print(paths[index]['id']);
    // switch tab if is not in folder mode
    // if (leftPanelKey.currentState!.selectedTab != 1) {
    leftPanelKey.currentState!.selectTab(1);
    //}
    // set folder condition
    while (folderTreeKey.currentState == null ||
        !folderTreeKey.currentState!.inited) {
      await new Future.delayed(Duration(milliseconds: 10));
    }
    print(' state is ${folderTreeKey.currentState}');
    await folderTreeKey.currentState!.selectToKey('${paths[index]['id']}');
    await new Future.delayed(Duration(milliseconds: 10));
    // get index of current photo
    var resp = await Dio().post(Config.api('/api/getPicIndex'), data: {
      'condition': Conditions.makeCondition(0, 100),
      'picId': picImage!.id,
    });
    // jump to photo
    if (resp.statusCode == 200) {
      Provider.of<SelectModel>(scaffoldKey.currentContext!, listen: false)
          .select(false, false, resp.data);
    }
    returnFocus();
  }

  Future<int> selectPath(List paths) async {
    return await showDialog(
      context: scaffoldKey.currentContext!,
      builder: (BuildContext context) {
        var child = Column(
          children: <Widget>[
            ListTile(title: Text("请选择")),
            Expanded(
                child: ListView.builder(
              itemCount: paths.length,
              itemBuilder: (BuildContext context, int index) {
                return ListTile(
                  title: Text('${paths[index]['path']}'),
                  onTap: () => Navigator.of(context).pop(index),
                );
              },
            )),
          ],
        );
        return Dialog(child: child);
      },
    );
  }

  void returnFocus() {
    if (widget._detail) {
      if (detailViewKey.currentState != null) {
        detailViewKey.currentState!.focusNode.requestFocus();
      }
    } else {
      if (gridViewKey.currentState != null) {
        print('focus.......${gridViewKey.currentState!.focusNode}');
        gridViewKey.currentState!.focusNode.requestFocus();
      }
    }
  }
}
