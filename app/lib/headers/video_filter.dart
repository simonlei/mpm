import 'package:app/model/conditions.dart';
import 'package:app/model/event_bus.dart';
import 'package:app/widgets/labeld_checkbox.dart';
import 'package:flutter/material.dart';

class VideoFilter extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _VideoFilterState();
  }
}

class _VideoFilterState extends State<VideoFilter> {
  bool _video = false;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 110,
      padding: EdgeInsets.only(right: 5),
      child: LabeledCheckbox(
        label: '只看视频',
        value: _video,
        onChanged: (bool? value) {
          Conditions.video = value! ? true : null;
          BUS.emit(EventBus.ConditionsChanged);
          BUS.emit(EventBus.LeftTreeConditionsChanged);

          setState(() {
            _video = value;
          });
        },
        padding: const EdgeInsets.only(left: 5),
      ),
    );
  }
}
