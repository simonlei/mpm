import 'package:flutter/cupertino.dart';

class TagsSelector extends StatefulWidget {
  final List tags;
  final List initValues;
  final List selectedValues = [];

  TagsSelector({required this.tags, required this.initValues});

  @override
  State<StatefulWidget> createState() {
    tags.add('1');
    return TagsSelectorState();
  }
}

class TagsSelectorState extends State<TagsSelector> {
  @override
  Widget build(BuildContext context) {
    throw UnimplementedError();
  }
}
