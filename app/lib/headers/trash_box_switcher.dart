import 'package:app/conditions.dart';
import 'package:app/config.dart';
import 'package:app/event_bus.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';

class TrashBoxSwitcher extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _TrashBoxSwitcherState();
  }
}

class _TrashBoxSwitcherState extends State<TrashBoxSwitcher> {
  @override
  Widget build(BuildContext context) {
    return FutureBuilder<String>(
      future: loadTitle(),
      builder: (context, snapshot) {
        return TextButton(
          onPressed: () async {
            Conditions.trashed = !Conditions.trashed;
            setState(() {});
            BUS.emit(EventBus.ConditionsChanged);
          },
          child: Text(
            snapshot.requireData,
            style: TextStyle(fontWeight: FontWeight.w500, color: Colors.white),
          ),
        );
      },
    );
  }

  Future<String> loadTitle() async {
    var response = await Dio().post(Config.toUrl('/api/getCount'), data: !Conditions.trashed);
    return Conditions.trashed ? '正常照片(${response.data})' : '回收站(${response.data})';
  }
}
