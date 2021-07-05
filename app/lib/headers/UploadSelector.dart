import 'dart:convert';
import 'dart:html' as html;

import 'package:app/config.dart';
import 'package:app/tecent_cos.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:js/js.dart';
import 'package:logger/logger.dart';

class UploadSelector extends TextButton {
  UploadSelector()
      : super(
          onPressed: _selectFolder,
          child: Text(
            '上传照片',
            style: TextStyle(fontWeight: FontWeight.w500, color: Colors.white),
          ),

/*
            HtmlWidget(
                '<input width=200px type="file" webkitdirectory directory>hahah</input>'),
*/

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

  static void _selectFolder() {
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

      COS cos = new COS(CosInitParam(
          getAuthorization: allowInterop((options, callback) async {
        var resp = await Dio().get(Config.toUrl("/tmpCredential"));
        if (resp.statusCode == 200) {
          var data = json.decode(resp.data);
          var credentials = data['credentials'];
          Logger().i('Resp $credentials');
          callback(AuthData(
            TmpSecretId: credentials['tmpSecretId'],
            TmpSecretKey: credentials['tmpSecretKey'],
            XCosSecurityToken: credentials['sessionToken'],
            StartTime: data['startTime'],
            ExpiredTime: data['expiredTime'],
          ));
        } else {
          Logger().e('Can not get authorization: $resp.data');
          callback('获取签名出错');
        }
      })));
      Logger().i("Cos is $cos");
      var cosFiles = <CosFile>[];
      var id = DateTime.now().millisecondsSinceEpoch;
      var files = input.files;
      for (var i = 0; i < files!.length; i++) {
        var file = files[i];
        if (file.type.startsWith("image") || file.type.startsWith("video")) {
          cosFiles.add(CosFile(
            Bucket: Config.bucket,
            Region: Config.region,
            Key: 'upload/${id}_${file.relativePath!}',
            Body: file,
          ));
        }
      }
      Logger().i("Cosfiles: $cosFiles");

      cos.uploadFiles(
          UploadFilesParams(
            files: cosFiles,
            onProgress: allowInterop((info) {
              Logger().i('progress $info');
            }),
            onFileFinish: allowInterop((err, data, options) {
              Logger().i('File finish err $err, data $data');
            }),
          ), allowInterop((err, data) {
        Logger().i('callback err $err, data $data');
      }));
    });
  }
}
