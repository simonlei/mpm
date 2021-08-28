import 'package:app/homepage.dart';
import 'package:app/model/select_model.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class LocationJumper extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _LocationJumperState();
  }
}

class _LocationJumperState extends State<LocationJumper> {
  late TextEditingController _controller;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      child: Row(
        children: [
          SizedBox(
            width: 50,
            child: Consumer<SelectModel>(
              builder: (context, selectModel, child) {
                _controller.text = '${selectModel.lastIndex + 1}';
                if (gridViewKey.currentState != null) gridViewKey.currentState!.scrollToIndex(selectModel);
                return TextField(
                  textAlign: TextAlign.center,
                  textAlignVertical: TextAlignVertical.center,
                  controller: _controller,
                );
              },
            ),
          ),
          TextButton(
            child: Text(
              '跳转',
              style: TextStyle(fontWeight: FontWeight.w500, color: Colors.white),
            ),
            onPressed: () {
              Provider.of<SelectModel>(context, listen: false).select(false, false, int.parse(_controller.text) - 1);
              if (gridViewKey.currentState != null) {
                print('focus.......${gridViewKey.currentState!.focusNode}');
                gridViewKey.currentState!.focusNode.requestFocus();
              }
              print('text is ${_controller.text}');
            },
          ),
        ],
      ),
    );
  }
}
