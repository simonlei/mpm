import 'dart:html' as html;

import 'package:app/headers/UploadSelector.dart';
import 'package:app/pics_grid.dart';
import 'package:flutter/material.dart';
import 'package:logger/logger.dart';

class MyHomePage extends StatefulWidget {
  MyHomePage({Key? key, required this.title}) : super(key: key);
  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  @override
  Widget build(BuildContext context) {
    var picker = TextButton(
      onPressed: _selectFile,
      child: Text(
        'Click to select file',
        style: TextStyle(fontWeight: FontWeight.w500, color: Colors.white),
      ),
    );

    return Scaffold(
      appBar: AppBar(
        title: Text('xxxxx'),
        actions: [
          // text 跳转 |
          // order
          // 回收站
          // 过滤条件 （只看star）|
          // 导入
          picker,
          UploadSelector(),
        ],
        toolbarHeight: 25,
      ),
      body: PicsGrid(),
    );
  }

  void _selectFile() {
    var input = html.Element.html(
        '<input type="file" webkitdirectory directory/>',
        validator: html.NodeValidatorBuilder()
          ..allowElement('input', attributes: ['webkitdirectory', 'directory'])
          ..allowHtml5()) as html.InputElement;

    // html.InputElement uploadInput = html.FileUploadInputElement();
    input.click();
    input.onChange.listen((e) {
      Logger().i('attrs ${input.getAttributeNames()}');
      Logger().i('childNodes ${input.childNodes}');
      Logger().i('input ${input.files}');

/*
      final files = input.files;
      var fileItem = UploadFileItem(files[0]);
      setState(() {
        _files.add(fileItem);
      });
*/
    });
  }
}
