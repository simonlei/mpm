import 'package:flutter/material.dart';
import 'package:logger/logger.dart';

class SortSelector extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _SortSelectorState();
  }
}

class _SortSelectorState extends State<SortSelector> {
  String sortString = 'xx';

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(right: 10),
      child: DropdownButton<String>(
        value: sortString,
        items: [
          DropdownMenuItem(value: 'xx', child: Text('xxx')),
          DropdownMenuItem(value: 'yy', child: Text('yyy')),
        ],
        onChanged: (String? newValue) {
          Logger().i(newValue);
          setState(() {
            sortString = newValue!;
          });
        },
      ),
    );
  }
}
