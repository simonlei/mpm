import 'package:app/model/pics_model.dart';
import 'package:context_menus/context_menus.dart';
import 'package:flutter/material.dart';
import 'package:oktoast/oktoast.dart';

class ImagesContextMenu extends StatefulWidget {
  final PicsModel _picsModel;

  ImagesContextMenu(this._picsModel);

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
              '修改选中照片时间',
              onPressed: () => handlePressed(context, _handleChangeDatePressed),
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
        context: context, initialDate: DateTime.now(), firstDate: DateTime(1950), lastDate: DateTime.now());
    if (result == null) return;
    print(result.toString());
    widget._picsModel.updateSelectedImages({'takenDate': result.toString()}, ' 拍摄时间到 ${result.toString()}');
  }
}
