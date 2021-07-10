import 'package:flutter/material.dart';

class LeftPanel extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _LeftPanelState();
  }
}

class _LeftPanelState extends State<LeftPanel> {
  @override
  Widget build(BuildContext context) {
    //return Text('aaaa');

    return Container(
      width: 250,
      child: DefaultTabController(
        length: 2,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TabBar(tabs: [
              Tab(
                child: Text(
                  '按日期查看',
                  style: TextStyle(fontWeight: FontWeight.w500, color: Colors.black),
                ),
              ),
              Tab(
                child: Text(
                  '按图片库查看',
                  style: TextStyle(fontWeight: FontWeight.w500, color: Colors.black),
                ),
              )
            ]),
            Expanded(
              child: TabBarView(
                children: [
                  Text('Icons.add'),
                  Text('Icons.subject'),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
