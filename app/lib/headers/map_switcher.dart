import 'package:flutter/material.dart';

class MapSwitcher extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _MapSwitcherState();
  }
}

class _MapSwitcherState extends State<MapSwitcher> {
  @override
  Widget build(BuildContext context) {
    return TextButton(
      child: Text(
        '地图模式',
        style: TextStyle(fontWeight: FontWeight.w500, color: Colors.white),
      ),
      onPressed: () {
        Navigator.of(context).pushNamed('/map');
      },
    );
  }
}
