import 'package:app/model/conditions.dart';
import 'package:app/model/event_bus.dart';
import 'package:app/widgets/labeld_checkbox.dart';
import 'package:flutter/material.dart';

class StarFilter extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _StarFilterState();
  }
}

class _StarFilterState extends State<StarFilter> {
  bool _star = false;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 110,
      padding: EdgeInsets.only(right: 5),
      child: LabeledCheckbox(
        label: '只看星标',
        value: _star,
        onChanged: (bool? value) {
          Conditions.star = value! ? true : null;
          BUS.emit(EventBus.ConditionsChanged);
          BUS.emit(EventBus.LeftTreeConditionsChanged);

          setState(() {
            _star = value;
          });
        },
        padding: const EdgeInsets.only(left: 5),
      ),
    );
  }
}
