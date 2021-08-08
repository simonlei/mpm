import 'package:app/model/conditions.dart';
import 'package:app/model/config.dart';
import 'package:app/model/event_bus.dart';
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
    return Padding(
        padding: EdgeInsets.only(right: 10),
        child: FutureBuilder<String>(
          future: loadTitle(),
          builder: (context, snapshot) {
            if (snapshot.hasData) {
              return OutlinedButton(
                onPressed: () {
                  Conditions.trashed = !Conditions.trashed;
                  setState(() {});
                  BUS.emit(EventBus.ConditionsChanged);
                  BUS.emit(EventBus.LeftTreeConditionsChanged);
                },
                child: Text(
                  snapshot.data!,
                  style: TextStyle(fontWeight: FontWeight.w500, color: Colors.white),
                ),
              );
            } else
              return Text('loading...');
          },
        ));
  }

  Future<String> loadTitle() async {
    var response = await Dio().post(Config.api('/api/getCount'), data: !Conditions.trashed);
    return Conditions.trashed ? '正常照片(${response.data})' : '回收站(${response.data})';
  }
}
