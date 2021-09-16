import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:oktoast/oktoast.dart';

class TagsSelector extends StatefulWidget {
  final List tags;
  final List? initValues;
  final List selectedValues = [];

  TagsSelector({required this.tags, required this.initValues}) {
    if (this.initValues != null) this.selectedValues.addAll(this.initValues!);
  }

  @override
  State<StatefulWidget> createState() {
    return TagsSelectorState();
  }
}

class TagsSelectorState extends State<TagsSelector> {
  final TextEditingController _textController = new TextEditingController();

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Wrap(
          spacing: 8.0, // 主轴(水平)方向间距
          runSpacing: 4.0, // 纵轴（垂直）方向间距
          children: buildChips(),
        ),
        Row(
          children: [
            SizedBox(
              width: 100,
              child: TextField(
                controller: _textController,
              ),
            ),
            TextButton(
              onPressed: () {
                if (widget.tags.contains(_textController.text)) {
                  showToast("标签已存在");
                  return;
                }
                setState(() {
                  widget.tags.add(_textController.text);
                  widget.selectedValues.add(_textController.text);
                });
              },
              child: Text('添加新标签'),
            ),
          ],
        ),
      ],
    );
  }

  List<Widget> buildChips() {
    List<Widget> chips = [];
    print(widget.tags.length);
    for (int i = 0; i < widget.tags.length; i++) {
      print(widget.tags[i]);

      FilterChip chip = FilterChip(
        label: Text(widget.tags[i]),
        selected: widget.selectedValues.contains(widget.tags[i]),
        onSelected: (bool value) {
          setState(() {
            if (value) {
              widget.selectedValues.add(widget.tags[i]);
            } else {
              widget.selectedValues.remove(widget.tags[i]);
            }
          });
        },
      );
      chips.add(chip);
    }
    return chips;
  }
}
