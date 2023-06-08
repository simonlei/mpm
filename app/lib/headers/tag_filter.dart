import 'package:app/model/conditions.dart';
import 'package:app/model/config.dart';
import 'package:app/model/event_bus.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:logger/logger.dart';

class TagFilter extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _TagFilterState();
  }
}

class _TagFilterState extends State<TagFilter> {
  String? selectedTag;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(right: 10),
      child: FutureBuilder<List>(
          future: loadTags(),
          builder: (BuildContext context, AsyncSnapshot<List?> snapshot) {
            if (snapshot.connectionState == ConnectionState.done) {
              var tags = snapshot.data;
              if ( tags == null) tags = [];
              List<DropdownMenuItem<String>> items = [];
              items.add(DropdownMenuItem(value: null, child: Text('ALL')));
              for (int i = 0; i < tags!.length; i++) {
                items.add(DropdownMenuItem(value: tags[i], child: Text(tags[i])));
              }
              return DropdownButton<String>(
                value: selectedTag,
                items: items,
                onChanged: (String? newValue) {
                  Logger().i(newValue);
                  Conditions.tag = newValue;
                  BUS.emit(EventBus.ConditionsChanged);
                  setState(() {
                    selectedTag = newValue;
                  });
                },
                style: TextStyle(fontWeight: FontWeight.w500, color: Colors.black),
              );
            } else if (snapshot.hasError)
              return Text('Error:${snapshot.error}');
            else
              return CircularProgressIndicator();
          }),
    );
  }

  Future<List> loadTags() async {
    var resp = await Dio().post(Config.api("/api/getAllTags"));
    if (resp.statusCode == 200) return (resp.data as String).split(",").where((s) => s.isNotEmpty).toList();
    return [];
  }
}
