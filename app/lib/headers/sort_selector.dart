import 'package:app/model/conditions.dart';
import 'package:app/model/event_bus.dart';
import 'package:flutter/material.dart';
import 'package:logger/logger.dart';

class SortSelector extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _SortSelectorState();
  }
}

class _SortSelectorState extends State<SortSelector> {
  String sortString = '-id';

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(right: 10),
      child: DropdownButton<String>(
        value: sortString,
        items: [
          DropdownMenuItem(value: '-id', child: Text('导入逆序')),
          DropdownMenuItem(value: 'id', child: Text('导入顺序')),
          DropdownMenuItem(value: '-takenDate', child: Text('时间逆序')),
          DropdownMenuItem(value: 'takenDate', child: Text('时间顺序')),
        ],
        onChanged: (String? newValue) {
          Logger().i(newValue);
          Conditions.order = newValue!;
          BUS.emit(EventBus.ConditionsChanged);
          setState(() {
            sortString = newValue;
          });
        },
        style: TextStyle(fontWeight: FontWeight.w500, color: Colors.black),
      ),
    );
  }
}
