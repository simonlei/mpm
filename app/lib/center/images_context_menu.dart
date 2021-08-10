import 'package:app/homepage.dart';
import 'package:app/model/pics_model.dart';
import 'package:context_menus/context_menus.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:oktoast/oktoast.dart';
import 'package:prompt_dialog/prompt_dialog.dart';

class ImagesContextMenu extends StatefulWidget {
  final PicsModel _picsModel;
  final bool _detail;

  ImagesContextMenu(this._picsModel, this._detail);

  @override
  State<StatefulWidget> createState() {
    return _ImagesContextMenuState();
  }
}

class _ImagesContextMenuState extends State<ImagesContextMenu> with ContextMenuStateMixin {
  @override
  Widget build(BuildContext context) {
    return cardBuilder.call(
      context,
      [
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
      ],
    );
  }

  void _handleChangeDatePressed() async {
    if (widget._picsModel.selectNothing) {
      showToast('请选中照片后再试');
      return;
    }

    var result = await showDatePicker(
      context: context,
      initialDate: DateTime.now(),
      firstDate: DateTime(1950),
      lastDate: DateTime.now(),
    );
    if (result == null) return;
    print(result.toString());
    widget._picsModel.updateSelectedImages({'takenDate': result.toString()}, ' 拍摄时间到 ${result.toString()}');
  }

  void _handleCopyGisPressed() async {
    if (widget._picsModel.selectNothing) {
      showToast('请选中照片后再试');
      return;
    }

    var selectedIndex = widget._picsModel.getSelectedIndex();
    var image = await widget._picsModel.getImage(selectedIndex);
    await Clipboard.setData(ClipboardData(text: '${image!.latitude},${image.longitude}'));
    showToast('已拷贝GIS信息至剪贴板');
  }

  Future<void> _handleChangeDescPressed() async {
    if (widget._picsModel.selectNothing) {
      showToast('请选中照片后再试');
      return;
    }
    var result = await prompt(scaffoldKey.currentContext!, title: Text('请输入描述信息'));
    if (result == null) return;
    print(' result is $result');
    widget._picsModel.updateSelectedImages({'description': result}, ' 描述信息到 $result');
  }

  Future<void> _handleChangeGisPressed() async {
    if (widget._picsModel.selectNothing) {
      showToast('请选中照片后再试');
      return;
    }
    var result = await prompt(scaffoldKey.currentContext!, title: Text('请输入纬度,经度，例如 22.57765,113.9504277778'));
    if (result == null) return;
    print(' result is $result');
    var splitted = result.split(',');
    if (splitted.isEmpty || splitted.length != 2) {
      showToast('请按照 纬度,经度 模式来输入');
      return;
    }
    widget._picsModel.updateSelectedImages({'latitude': splitted[0], 'longitude': splitted[1]}, ' GIS信息到 $result');
  }
}
