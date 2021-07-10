import 'dart:async';

import 'package:app/conditions.dart';
import 'package:app/config.dart';
import 'package:app/event_bus.dart';
import 'package:confirm_dialog/confirm_dialog.dart';
import 'package:dio/dio.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:logger/logger.dart';
import 'package:oktoast/oktoast.dart';
import 'package:sn_progress_dialog/sn_progress_dialog.dart';

class EmptyTrashButton extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _EmptyTrashButtonState();
  }
}

class _EmptyTrashButtonState extends State<EmptyTrashButton> {
  @override
  Widget build(BuildContext context) {
    return Visibility(
      visible: Conditions.trashed,
      child: Padding(
        padding: EdgeInsets.only(right: 10),
        child: TextButton(
          onPressed: () async {
            //confirm
            bool empty = await confirm(context,
                title: Text('确认清空回收站？'),
                content: Text('清空回收站后，回收站内的照片将无法找回，请确认清空'));
            if (empty) {
              var resp = await Dio().post(Config.toUrl('/api/emptyTrash'));
              if (resp.statusCode == 200) {
                showProgressDialog(resp.data);
              }
              return print('pressedOK');
            }
            return print('pressedCancel');
          },
          child: Text(
            '清空回收站',
            style: TextStyle(fontWeight: FontWeight.w500, color: Colors.white),
          ),
        ),
      ),
    );
  }

  void showProgressDialog(String taskId) {
    ProgressDialog pd = ProgressDialog(context: context);
    pd.show(max: 100, msg: '正在清空回收站...');
    Timer.periodic(new Duration(seconds: 1), (timer) async {
      var resp = await Dio().get(Config.toUrl("/api/getProgress/$taskId"));
      if (resp.statusCode == 200) {
        Logger().i('Resp ${resp.data}');
        pd.update(value: resp.data['progress']);
        if (!pd.isOpen()) {
          timer.cancel();
          showToast('回收站清空完成');
          BUS.emit(EventBus.ConditionsChanged);
        }
      }
    });
  }
}
