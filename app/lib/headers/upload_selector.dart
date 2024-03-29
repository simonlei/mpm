import 'dart:convert';
import 'dart:html' as html;

import 'package:app/js_wrap/tecent_cos.dart';
import 'package:app/model/config.dart';
import 'package:app/model/event_bus.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:js/js.dart';
import 'package:logger/logger.dart';
import 'package:oktoast/oktoast.dart';
import 'package:sn_progress_dialog/sn_progress_dialog.dart';

class UploadSelector extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _UploadSelectorState();
  }
}

class _UploadSelectorState extends State<UploadSelector> {
  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(right: 10),
      child: OutlinedButton(
        onPressed: _selectFolder,
        child: Text(
          '上传照片',
          style: TextStyle(fontWeight: FontWeight.w500, color: Colors.white),
        ),
      ),
    );
  }

  void _selectFolder() {
    var input = html.Element.html('<input type="file" webkitdirectory directory/>',
        validator: html.NodeValidatorBuilder()
          ..allowElement('input', attributes: ['webkitdirectory', 'directory'])
          ..allowHtml5()) as html.InputElement;

    // html.InputElement uploadInput = html.FileUploadInputElement();
    input.click();
    input.onChange.listen((e) {
      // Logger().i('attrs ${input.getAttributeNames()}');
      // Logger().i('childNodes ${input.childNodes}');
      // Logger().i('input ${input.files}');

      COS cos = new COS(CosInitParam(getAuthorization: allowInterop((options, callback) async {
        var resp = await Dio().get(Config.api("/tmpCredential"));
        if (resp.statusCode == 200) {
          var data = json.decode(resp.data);
          var credentials = data['credentials'];
          // Logger().i('Resp $credentials');
          callback(AuthData(
            TmpSecretId: credentials['tmpSecretId'],
            TmpSecretKey: credentials['tmpSecretKey'],
            XCosSecurityToken: credentials['sessionToken'],
            StartTime: data['startTime'],
            ExpiredTime: data['expiredTime'],
          ));
        } else {
          showToast('Can not get authorization: $resp.data');
          callback('获取签名出错');
        }
      })));

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
            Date: file.lastModified!,
            Body: file,
          ));
        }
      }
      ProgressDialog pd = ProgressDialog(context: context);
      pd.show(max: cosFiles.length, msg: '正在上传文件...');

      Logger().i("Cosfiles: $cosFiles");
      var count = 0;

      cos.uploadFiles(
          UploadFilesParams(
            files: cosFiles,
            SliceSize: 1024 * 1024,
            onProgress: allowInterop((info) {
              var percent = info.percent * 10000 / 100;
              var speed = info.speed / 1024 / 1024 * 100 / 100;
              pd.update(value: count, msg: '进度：' + percent + '%; 速度：' + speed + 'Mb/s');
            }),
            onFileFinish: allowInterop((err, data, options) async {
              var map = Map<String, dynamic>.from(jsonDecode(stringify(options)));
              var key = map['Key'];
              var resp = await Dio().post(Config.api("/uploadFile"), data: {
                'key': key,
                'date': map['Date'],
                'data': stringify(options),
                'err': err,
              });
              if (resp.statusCode == 200) {
                count++;
                pd.update(
                  value: count,
                  msg: "第 $count/${cosFiles.length}个文件 ${key.split("\/").last} 上传${err != null ? '失败' : '完成'}",
                );
                Logger().i("第 $count/${cosFiles.length}个文件resp: ${resp.data}");
                // showToast("第 $count/${cosFiles.length}个文件 ${key.split("\/").last} 上传${err != null ? '失败' : '完成'}");
                if (count == cosFiles.length) {
                  BUS.emit(EventBus.ConditionsChanged);
                  showToast("上传完成，共 $count 张照片");
                }
              }
              if (err != null) showToast('File finish err $err, data $data');
            }),
          ), allowInterop((err, data) {
        if (err != null) showToast('callback err $err, data $data');
      }));
    });
  }
}
