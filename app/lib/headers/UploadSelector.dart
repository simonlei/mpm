import 'package:flutter/material.dart';
import 'package:flutter_widget_from_html_core/flutter_widget_from_html_core.dart';

class UploadSelector extends Row {
  UploadSelector()
      : super(
          children: [
            Text(
              'upload...',
            ),
/*
            HtmlWidget(
                '<input width=200px type="file" webkitdirectory directory>hahah</input>'),
*/
          ],
          /*
            child: createInput(),
            onPressed: () async {
                var file = await FilePickerCross.importFromStorage();
              var listInternalFiles =
                  await FilePickerCross.listInternalFiles(at: file.directory);
              Logger().i("Files length:${listInternalFiles.length}");
              var files = await FilePickerCross.importMultipleFromStorage();
              Logger().i("Files length:${files.length}");
              COS cos = new COS({
                'getAuthorization': allowInterop((options, callback) {
                  Logger().i("Options $options, callback $callback");
                  callback('获取签名出错');
                })
              });
              Logger().i("Cos is $cos");
              Logger().i("Cos is ${cos.uploadFiles}");
              cos.uploadFiles({}, allowInterop(() {}));
            }*/
        );

  static Widget createInput() {
    /*
    var input = html.Element.html(
        '<input type="file" webkitdirectory directory/>',
        validator: html.NodeValidatorBuilder()
          ..allowElement('input', attributes: ['webkitdirectory', 'directory'])
          ..allowHtml5());

     */
    return HtmlWidget('<input type="file" webkitdirectory directory/>');
  }
}
